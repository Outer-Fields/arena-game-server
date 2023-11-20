package io.mindspice.okragameserver.game.cards;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.*;
import io.mindspice.okragameserver.game.gameroom.action.ActionReturn;
import io.mindspice.okragameserver.game.gameroom.action.StatMap;
import io.mindspice.okragameserver.game.gameroom.action.logic.Cost;
import io.mindspice.okragameserver.game.gameroom.action.logic.EffectCalc;
import io.mindspice.okragameserver.game.gameroom.action.logic.EffectLogic;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;
import io.mindspice.okragameserver.util.CardUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Objects;

import static io.mindspice.okragameserver.game.enums.ActionClass.MULTI;
import static io.mindspice.okragameserver.game.enums.ActionClass.SINGLE;
import static io.mindspice.okragameserver.game.enums.ActionType.*;
import static io.mindspice.okragameserver.game.enums.Alignment.CHAOS;
import static io.mindspice.okragameserver.game.enums.Alignment.ORDER;
import static io.mindspice.okragameserver.game.enums.EffectType.*;
import static io.mindspice.okragameserver.game.enums.StatType.*;


public enum AbilityCard implements Card {
    SACRIFICIAL_DEMISE(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, CHAOS)
            .setCost(new StatMap(0, 0, 0, 225))
            .setCostLogic(Cost.GET)
            .setAnimation(Animation.build(AnimType.DEMISE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Calling upon the gods of chaos, you offer yourself as a sacrifice for your teams greater good." +
                    "Kills playing pawn in exchange for the death of an opponents pawns of equal or less health. " +
                    "Opponents health must less than 60% of their max health")
            .skipValidation()
            .build()) {
        @Override
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getSingleActionReturn(player.getPawn(playerIdx), target.getPawn(targetIdx), getStats().getAnimation());
            if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }
            var playerPawn = player.getPawn(playerIdx);
            var targetPawn = target.getPawn(targetIdx);
            if (playerPawn.getStat(HP) < targetPawn.getStat(HP) || (float) targetPawn.getStat(HP) / targetPawn.getStatMax(HP) > 0.6) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_CONSTRAINT_HP, getName());

            }
            var playerDamage = new EnumMap<StatType, Integer>(StatType.class);
            playerDamage.put(HP, playerPawn.getStat(HP) + 1000);

            var targetDamage = new EnumMap<StatType, Integer>(StatType.class);
            targetDamage.put(HP, playerPawn.getStat(HP) + 1000);

            actionReturn.playerPawnStates.add(new PawnInterimState(playerPawn));
            var pis = actionReturn.playerPawnStates.get(0);
            pis.addDamage(playerDamage);
            pis.addFlag(ActionFlag.DAMAGED);

            actionReturn.targetPawnStates.add(new PawnInterimState(targetPawn));
            var eis = actionReturn.targetPawnStates.get(0);
            eis.addDamage(targetDamage);
            eis.addFlag(ActionFlag.DAMAGED);
            return actionReturn;
        }
    },
    ACTION_PLUNDER(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, CHAOS)
            .setCost(new StatMap(0, 0, 60, 60))
            .setCostLogic(Cost.GET)
            .setAnimation(Animation.build(AnimType.PLUNDER, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Calling upon the power of your ancestors to swap one of your highest level attack cards with one of your " +
                    "opponent's highest level. Only works if both your pawns are of the same class, and can only swap for cards less than " +
                    "or equal to one level higher.")
            .skipValidation()
            .build()) {
        @Override
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getSingleActionReturn(player.getPawn(playerIdx), target.getPawn(targetIdx), getStats().getAnimation());
            if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }

            var playerPawn = player.getPawn(playerIdx);
            var targetPawn = target.getPawn(targetIdx);

            var tCards = targetPawn.getActionCards().values().stream()
                    .sorted(Comparator.comparingInt(Card::getLevel).reversed()).toList();

            var pCards = playerPawn.getActionCards().values().stream()
                    .sorted(Comparator.comparingInt(Card::getLevel).reversed()).toList();

            Card tCard = null;
            Card pCard = null;

            loop:
            for (var pC : pCards) {
                for (var tC : tCards) {
                    if (tC.getLevel() <= pC.getLevel() + 1) {
                        tCard = tC;
                        pCard = pC;
                        break loop;
                    }
                }
            }

            if (tCard != null && pCard != null) {
                playerPawn.replaceCard(pCard, tCard);
                targetPawn.replaceCard(tCard, pCard);
                actionReturn.targetPawnStates.get(0).addFlag(ActionFlag.EFFECTED);
                player.getCombatManager().sendCardUpdate(true);
                target.getCombatManager().sendCardUpdate(true);
            } else {
                actionReturn.isInvalid = true;
                return actionReturn;
            }
            return actionReturn;
        }
    },
    ABILITY_PLUNDER(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, CHAOS)
            .setCost(new StatMap(0, 0, 0, 160))
            .setCostLogic(Cost.GET)
            .setAnimation(Animation.build(AnimType.PLUNDER, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription(" Calling upon the power of your ancestors to swap your ability card with your " +
                    "opponents ability card. Only works if opponents card is equal to or less than one level higher and " +
                    "both pawns still possess a ability card.")
            .skipValidation()
            .build()) {
        @Override
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getSingleActionReturn(player.getPawn(playerIdx), target.getPawn(targetIdx), getStats().getAnimation());
            if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }

            var playerPawn = player.getPawn(playerIdx);
            var targetPawn = target.getPawn(targetIdx);

            var tCards = targetPawn.getAbilityCards().values().stream()
                    .sorted(Comparator.comparingInt(Card::getLevel).reversed()).toList();

            var pCards = playerPawn.getAbilityCards().values().stream()
                    .filter(c -> !Objects.equals(c.getName(), this.getName()))
                    .sorted(Comparator.comparingInt(Card::getLevel).reversed()).toList();

            Card tCard = null;
            Card pCard = null;

            loop:
            for (var pC : pCards) {
                for (var tC : tCards) {
                    if (tC.getLevel() <= pC.getLevel() + 1) {
                        tCard = tC;
                        pCard = pC;
                        break loop;
                    }
                }
            }

            if (tCard != null && pCard != null) {
                playerPawn.replaceCard(pCard, tCard);
                targetPawn.replaceCard(tCard, pCard);
                actionReturn.targetPawnStates.get(0).addFlag(ActionFlag.EFFECTED);
                player.getCombatManager().sendCardUpdate(true);
                target.getCombatManager().sendCardUpdate(true);
            } else {
                actionReturn.isInvalid = true;
                return actionReturn;
            }
            return actionReturn;
        }

    },
    POWER_PLUNDER(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, CHAOS)
            .setCost(new StatMap(0, 0, 75, 75))
            .setCostLogic(Cost.GET)
            .setDescription("Calling upon the power of your ancestors to swap your power card with your opponents power card." +
                    " Only works if opponents card is equal to  or less than one level higher.")
            .setAnimation(Animation.build(AnimType.PLUNDER, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .skipValidation()
            .build()) {
        @Override
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getSingleActionReturn(player.getPawn(playerIdx), target.getPawn(targetIdx), getStats().getAnimation());
            if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }
            var playerPawn = player.getPawn(playerIdx);
            var targetPawn = target.getPawn(targetIdx);

            if (playerPawn.getPowerCard() == null) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_P_CARD, getName());
            }
            if (targetPawn.getPowerCard() == null) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_E_CARD, getName());
            }
            PowerCard pCard = playerPawn.getPowerCard();
            PowerCard tCard = targetPawn.getPowerCard();
            if (tCard.level - pCard.level > 1) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_CONSTRAINT_LEVEL, getName());
            }
            playerPawn.setPowerCard(tCard);
            targetPawn.setPowerCard(pCard);
            actionReturn.targetPawnStates.get(0).addFlag(ActionFlag.EFFECTED);
            player.getCombatManager().sendCardUpdate(true);
            target.getCombatManager().sendCardUpdate(true);
            return actionReturn;
        }
    },
    ARMS_TRADE(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, CHAOS)
            .setCost(new StatMap(0, 0, 160, 0))
            .setCostLogic(Cost.GET)
            .setAnimation(Animation.build(AnimType.CAPITULATION, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("In a macho exchange you and your opponent swap weapons, deeming yourselves better versed" +
                    "in each others offense.")
            .skipValidation()
            .build()) {
        @Override
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getSingleActionReturn(player.getPawn(playerIdx), target.getPawn(targetIdx), getStats().getAnimation());
            if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }
            var playerPawn = player.getPawn(playerIdx);
            var targetPawn = target.getPawn(targetIdx);

            var tWeapons = targetPawn.getWeaponCards().values().stream()
                    .sorted(Comparator.comparingInt(Card::getLevel).reversed()).toList();

            var pWeapons = playerPawn.getWeaponCards().values().stream()
                    .sorted(Comparator.comparingInt(Card::getLevel).reversed()).toList();

            Card tCard = null;
            Card pCard = null;

            weapLoop:
            for (var pWeap : pWeapons) {
                for (var tWeap : tWeapons) {
                    if (tWeap.getLevel() <= pWeap.getLevel()) {
                        tCard = tWeap;
                        pCard = pWeap;
                        break weapLoop;
                    }
                }
            }

            if (tCard != null && pCard != null) {
                playerPawn.replaceCard(pCard, tCard);
                targetPawn.replaceCard(tCard, pCard);
                actionReturn.targetPawnStates.get(0).addFlag(ActionFlag.EFFECTED);
                player.getCombatManager().sendCardUpdate(true);
                target.getCombatManager().sendCardUpdate(true);
            } else {
                actionReturn.isInvalid = true;
                return actionReturn;
            }
            return actionReturn;
        }
    },
    DIVINATION_OF_STATE(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 100))
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, INSIGHT_STATUS, false, 1.5, 1, false, 1.12, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setCostLogic(Cost.GET)
            .setDescription("Through the ancient art of divination you gain a glimpse in to the status effects of an " +
                    "opponents pawn, including their buffs and de-buffs")
            .setAnimation(Animation.build(AnimType.INSIGHT, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .build()) {
        @Override
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getSingleActionReturn(player.getPawn(playerIdx), target.getPawn(targetIdx), getStats().getAnimation());
            if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }
            getStats().getTargetEffectLogic().doInsight(target.getPawn(targetIdx), getStats().getTargetEffects()[0], actionReturn);
            target.getPawn(targetIdx).addStatusEffect(new Effect(SINGLE, INSIGHT_STATUS, false, 1, 100, false));
            actionReturn.targetPawnStates.get(0).addFlag(ActionFlag.EFFECTED);
            return actionReturn;
        }
    },
    DIVINATION_OF_STATE_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 200))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, INSIGHT_STATUS, false, 1.5, 1, false, 1.12, 1, 1.12, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setDescription("Through the ancient art of divination you gain a glimpse in to the status effects of your all your" +
                    " opponent's pawns, including their buffs and de-buffs")
            .setAnimation(Animation.build(AnimType.INSIGHT, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .build()) {
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getSingleActionReturn(player.getPawn(playerIdx), target.getPawn(targetIdx), getStats().getAnimation());
            if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }
            for (var tps : actionReturn.targetPawnStates) {
                getStats().getTargetEffectLogic().doInsight(tps.getPawn(), getStats().getTargetEffects()[0], actionReturn);
            }
            return actionReturn;
        }
    },
    ORACLE_OF_HAND(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, ORDER)
            .setCost(new StatMap(0, 0, 0, 100))
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, INSIGHT_HAND, false, 1.5, 1, false, 1.12, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setCostLogic(Cost.GET)
            .setDescription("Astral projecting to the plane of the oracles you find your self hovering above an opponents " +
                    "pawn, getting a glimpse of their current hand.")
            .setAnimation(Animation.build(AnimType.INSIGHT, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .build()) {
        @Override
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getSingleActionReturn(player.getPawn(playerIdx), target.getPawn(targetIdx), getStats().getAnimation());
            if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }
            getStats().getTargetEffectLogic().doInsight(target.getPawn(targetIdx), getStats().getTargetEffects()[0], actionReturn);
            return actionReturn;
        }
    },
    ORACLE_OF_HAND_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 175))
            .setTargetEffects(new Effect[]{
                    new Effect(MULTI, INSIGHT_HAND, false, 1.5, 1, false, 1.12, 1, 1.12, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setCostLogic(Cost.GET)
            .setDescription("Astral projecting to the plane of the oracles you find your self hovering above an opponents " +
                    "pawns, getting a glimpse each ones current hand.")
            .setAnimation(Animation.build(AnimType.INSIGHT, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .build()) {
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getMultiActionReturn(player, playerIdx, target, targetIdx, getStats().getAnimation());
            if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }
            for (var tps : actionReturn.targetPawnStates) {
                getStats().getTargetEffectLogic().doInsight(tps.getPawn(), getStats().getTargetEffects()[0], actionReturn);
            }
            return actionReturn;
        }
    },
    CLAIRVOYANT_INSIGHT(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 200))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, INSIGHT_ACTION_DECK, false, 1.5, 1, false, 1.12, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setDescription("In the mist of battle you find yourself suddenly calm as you see visions of the future and " +
                    "the fate that lies before you upon revelation of the contents of your opponent's pawn's action deck.")
            .setAnimation(Animation.build(AnimType.INSIGHT, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .build()) {
        @Override
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getSingleActionReturn(player.getPawn(playerIdx), target.getPawn(targetIdx), getStats().getAnimation());
            if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }
            getStats().getTargetEffectLogic().doInsight(target.getPawn(targetIdx), getStats().getTargetEffects()[0], actionReturn);
            return actionReturn;
        }
    },
    CLAIRVOYANT_INSIGHT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 200))
            .setCostLogic(Cost.GET)
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, INSIGHT_ABILITY_DECK, false, 1.5, 1, false, 1.12, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.INSIGHT, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("In the mist of battle you find yourself suddenly calm as you see visions of the future and " +
                    "the fate that lies before you upon revelation of the contents of your opponent's pawn's ability deck.")
            .build()) {
        @Override
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getSingleActionReturn(player.getPawn(playerIdx), target.getPawn(targetIdx), getStats().getAnimation());
            if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }
            getStats().getTargetEffectLogic().doInsight(target.getPawn(targetIdx), getStats().getTargetEffects()[0], actionReturn);
            return actionReturn;
        }
    },
    EYE_OF_THE_SEER_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, ORDER)
            .setCost(new StatMap(0, 0, 0, 300))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, INSIGHT_ALL_DECKS, false, 1.5, 1, false, 1.12, 1)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.INSIGHT, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Through a halting and sudden vision you find yourself able to see the full deck of one of your opponent's pawns," +
                    " though you find no solace in the experience and find your mana severely drained.")
            .skipValidation()
            .build()) {
        @Override
        public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
            ActionReturn actionReturn = getMultiActionReturn(player, playerIdx, target, targetIdx, getStats().getAnimation());
            if (!getStats().getCostLogic().doCost(player.getPawn(playerIdx), getStats().getCost().asMap(), actionReturn)) {
                return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
            }
            var tPawn = target.getPawn(targetIdx);

            var defense = tPawn.getPowerAbilityDefense(INSIGHT_ALL_DECKS);
            if (defense.containsKey(PowerEnums.PowerReturn.RESIST)) {
                actionReturn.targetPawnStates.add(new PawnInterimState(tPawn));
                actionReturn.targetPawnStates.get(0).addFlag(ActionFlag.RESISTED);
                return actionReturn;
            }

            getStats().getTargetEffectLogic().doInsight(target.getPawn(targetIdx), getStats().getTargetEffects()[0], actionReturn);
            return actionReturn;
        }
    },
    STONE_STRICKEN(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 125))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, PARALYSIS, false, 1.5, 1, false, 1.12, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setDescription("Casting a weak but timely spell you turn one of your opponents pawns temporarily and incurably to stone" +
                    " lasting for the length of their next turn.")
            .setAnimation(Animation.build(AnimType.STONE, AnimTime.LONG, AnimSpeed.NORMAL))
            .build()),
    STONE_STRICKEN_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 225))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, PARALYSIS, false, 1.5, 2, false, 1.12, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.STONE, AnimTime.LONG, AnimSpeed.NORMAL))
            .setDescription("Casting a timely spell you turn one of your opponents pawns temporarily and incurably to stone" +
                    " lasting for the length of their next two turns.")
            .build()),
    RELEGATION_OF_DESTINY(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, ORDER)
            .setCost(new StatMap(0, 0, 0, 100))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, MISFORTUNE, false, 4, 2, false, 1.12, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Muttering a curse from your elders, you render your opponent's pawns with a short bout of misfortune for two rounds.")
            .build()),
    RELEGATION_OF_DESTINY_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 200))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, MISFORTUNE, false, 4, 4, false, 1.12, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Muttering a curse from your elders, you render you opponents' pawns with a sizable bout of misfortune for four rounds.")
            .build()),
    ENFEEBLED_HEART(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 100))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, FEEBLENESS, false, 2, 4, false, 1.12, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Magically whispering de-motivations in the air your voice carries to your opponent's pawn who hears the words " +
                    "reflected in their thoughts and find their willpower slightly lowered for the next four rounds.")
            .build()),
    ENFEEBLED_HEART_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 180))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, FEEBLENESS, false, 3, 6, false, 1.12, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Magically whispering de-motivations in the air you voice carries to your opponent's pawn who hears the words " +
                    "reflected in their thoughts and find their willpower slightly lowered for the next four rounds.")
            .build()),
    WARRIORS_FALTER(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 0, 120))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, DE_CORE, false, 150, 2, false, 1.12, 1, 0, 0),
                    new Effect(SINGLE, DE_FORTIFY, false, 150, 2, false, 1.12, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Shouting a manta across the land your enemy feels their armor and weapon began to reverberate, " +
                    "Strickening them with fatigue and lowering their strength and defense for two rounds.")
            .build()),
    WARRIORS_FALTER_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 0, 175))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, DE_CORE, false, 150, 4, false, 1.12, 1, 0, 0),
                    new Effect(SINGLE, DE_FORTIFY, false, 150, 4, false, 1.12, 1, 0, 0)
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Shouting a strong manta across the land your enemy feels their armor and weapon began to reverberate, " +
                    "Strickening them with fatigue and lowering their strength and defense for four rounds.")
            .build()),
    SPITE_OF_THE_WARRIOR(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .setCost(new StatMap(0, 0, 70, 50))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, DE_MEDITATE, false, 175, 2, false, 1.12, 1, 0, 0),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Releasing a spiteful and haunting shout against your enemy feels their mana begin to drain, lasting for two rounds.")
            .build()),
    SPITE_OF_THE_WARRIOR_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .setCost(new StatMap(0, 0, 120, 80))
            .setCostLogic(Cost.GET)
            .setTargetEffects(new Effect[]{
                    new Effect(SINGLE, DE_MEDITATE, false, 175, 4, false, 1.12, 1, 0, 0),
            })
            .setTargetEffectLogic(EffectCalc.BasicTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURSE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Releasing a spiteful and haunting shout of pure vengeance against your enemy feels their mana begin to drain, lasting for four rounds.")
            .build()),
    MAGES_TURMOIL(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, CHAOS)
            .isPlayer()
            .setCost(new StatMap(0, 200, 0, 0))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, MEDITATE, false, 200, 10, false, 1.12, 1, 0, 0),
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BUFF, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("In a tumultuous decision you call upon ancient chaos magic to channel your defense into mana.")
            .build()),
    WARRIORS_TURMOIL(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, CHAOS)
            .isPlayer()
            .setCost(new StatMap(0, 100, 0, 100))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, CORE, false, 200, 10, false, 1.12, 1, 0, 0),
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BUFF, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("In a tumultuous decision you call upon ancient chaos magic to channel your defense and mana into strength.")
            .build()),
    RANGERS_TURMOIL(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, CHAOS)
            .isPlayer()
            .setCost(new StatMap(0, 200, 0, 0))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, CORE, false, 200, 10, false, 1.12, 1, 0, 0),
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BUFF, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("In a tumultuous decision you call upon ancient chaos magic to channel your defense into strength.")
            .build()),
    REJUVENATING_LIGHT(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 0, 0, 125))
            .setCostLogic(Cost.GET)
            .setAnimation(Animation.build(AnimType.CURE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setPosSelfTargetEffects(new Effect[]{
                    new Effect(SINGLE, CURE_ANY, false, 250, 1, false, 1.12, 1)
            })
            .setPosSelfTargetEffectLogic(EffectCalc.BasicSelfPosTarget.GET(1), EffectLogic.Basic.GET)
            .setDescription("Casting a bright aura of light around your target you heal up to 150 points of negative status effects.")
            .build()),
    REJUVENATING_LIGHT_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 0, 0, 200))
            .setCostLogic(Cost.GET)
            .setAnimation(Animation.build(AnimType.CURE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setPosSelfTargetEffects(new Effect[]{
                    new Effect(SINGLE, CURE_ANY, false, 250, 1, false, 1.12, 1)
            })
            .setPosSelfTargetEffectLogic(EffectCalc.BasicSelfPosTarget.GET(1), EffectLogic.Basic.GET)
            .setDescription("Casting a bright aura of light around targeted pawn you heal up to 250 points of negative status effects.")
            .build()),
    POISON_REMEDY(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 100, 0, 50))
            .setCostLogic(Cost.GET)
            .setPosSelfTargetEffects(new Effect[]{
                    new Effect(SINGLE, CURE_POISON, false, 50, 0, false, 1.12, 1)
            })
            .setPosSelfTargetEffectLogic(EffectCalc.BasicSelfPosTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Remedies up to 50 points of poison damage on targeted pawn.")
            .build()),
    POISON_REMEDY_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 0, 0, 125))
            .setCostLogic(Cost.GET)
            .setPosSelfTargetEffects(new Effect[]{
                    new Effect(SINGLE, CURE_POISON, false, 100, 0, false, 1.12, 1)
            })
            .setPosSelfTargetEffectLogic(EffectCalc.BasicSelfPosTarget.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.CURE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Remedies up to 100 points of poison damage on targeted pawn.")
            .build()),
    SUDDEN_AWAKENING(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 100, 0, 120))
            .setCostLogic(Cost.GET)
            .setPosSelfTargetEffects(new Effect[]{
                    new Effect(SINGLE, AWAKEN, false, 50, 0, false, 1.12, 1)
            })
            .setPosSelfTargetEffectLogic(EffectCalc.BasicSelfPosTarget.GET(1), EffectLogic.Basic.GET)
            .setDescription("From a deep sleep the target pawns jumps up awake, ready once again for battle.")
            .setAnimation(Animation.build(AnimType.CURE, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .build()),
    FORTUNES_FAVOR(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 100, 0, 120))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, FORTUNE, false, 3, 4, false, 1.12, 1)
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BUFF, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Imparts a blessing of fortune upon the pawn it is played on.")
            .build()),
    FORTUNES_FAVOR_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 100, 0, 200))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, FORTUNE, false, 5, 4, false, 1.12, 1)
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BUFF, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Imparts a strong blessing of strong fortune upon the pawn it is played on.")
            .build()),
    FRIENDLY_MEDITATION(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 75, 0, 100))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, MEDITATE, false, 250, 4, false, 1.12, 1)
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BUFF, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Taking a short break from battle you meld your mind and mana, leaving you with more than you " +
                    "started with at the cost of slightly less defense.")
            .build()),
    FRIENDLY_MEDITATION_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 125, 0, 100))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, MEDITATE, false, 350, 3, false, 1.12, 1)
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BUFF, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Taking a short break from battle you meld your mind and mana, leaving you with more than you " +
                    "started with at the cost of noticeably less defense.")
            .build()),
    FORTILITY(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 2, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 0, 0, 125))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, FORTIFY, false, 125, 4, false, 1.12, 1)
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BUFF, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Focusing hard you channel your mana into fortifying your defense.")
            .build()),
    FORTILITY_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 3, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 0, 0, 200))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, FORTIFY, false, 200, 4, false, 1.12, 1)
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BUFF, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Focusing hard you channel a large amount of your mana into fortifying your defense.")
            .build()),
    CORE_VIGOR(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 1, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 0, 0, 80))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, CORE, false, 125, 6, false, 1.12, 1)
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BUFF, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Focusing hard you channel your mana into increasing your strength.")
            .build()),
    CORE_VIGOR_GOLD(new CardStats.Builder(CollectionSet.ORIGINS, MAGIC, 4, ORDER)
            .isPlayer()
            .setCost(new StatMap(0, 0, 0, 160))
            .setCostLogic(Cost.GET)
            .setPosSelfEffects(new Effect[]{
                    new Effect(SINGLE, CORE, false, 250, 6, false, 1.12, 1)
            })
            .setPosSelfEffectLogic(EffectCalc.BasicSelfPos.GET(1), EffectLogic.Basic.GET)
            .setAnimation(Animation.build(AnimType.BUFF, AnimTime.EXTRA_LONG, AnimSpeed.NORMAL))
            .setDescription("Focusing hard you channel a large amount of your mana into increasing your strength.")
            .build());

    private final CardStats stats;
    private String uid = null;
    public static final String prefix = "ABL";

    AbilityCard(CardStats stats) {
        this.stats = stats;
    }

    public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
        // Call the default implementation with included stats
        return playCard(player, target, playerIdx, targetIdx, stats);
    }

    public CardStats getStats() {
        return stats;
    }

    public int getLevel() {
        return stats.getLevel();
    }

    public String getUid() {
        if (uid == null) { this.uid = prefix + "-" + CardUtil.getHash(this.name()); }
        return uid;
    }

    public String getName() {
        return this.name();
    }

    public String toStringLog() {
        return this + "{" +
                "animation=" + stats.getAnimation() +
                ", mpCost=" + stats.getCost() +
                ", level=" + stats.getLevel() +
                ", effects=" + Arrays.stream((stats.getTargetEffects())).map(Effect::toString).toList() +
                '}';
    }

    public String toUID() {
        return "AB-" + stats.getLevel() + "-" + this + (this.name().contains("GOLD") ? "-GOLD" : "-REG");
    }

    public ObjectNode toJson() {
        // Better to just use the mutable mapper than the JsonUtils builder here with this many fields
        var json = JsonUtils.getMapper();
        var obj = json.createObjectNode();
        obj.put("uid", getUid());
        obj.put("is_gold", this.toString().contains("GOLD"));
        obj.put("damage_class", stats.getDamageClass() != null ? stats.getDamageClass().toString() : "N/A");
        obj.put("is_player", stats.isPlayer());
        obj.put("alignment", stats.getAlignment().toString());
        obj.put("action_type", stats.getActionType().toString());
        obj.put("self_damage_class", stats.getSelfDamageClass() != null ? stats.getSelfDamageClass().toString() : "N/A");
        obj.put("special", stats.getSpecial() != null ? stats.getSpecial().toString() : "N/A");
        obj.put("animation", stats.getAnimation() != null ? stats.getAnimation().toString() : "N/A");
        obj.put("level", stats.getLevel());
        obj.putIfAbsent("cost", stats.getCost().toJson());
        obj.putIfAbsent("damage", stats.getDamage().toJson());
        obj.put("enemy_damage_reps", stats.getDamageCalc() != null ? stats.getDamageCalc().getMulti() : 0);
        obj.put("self_damage_reps", stats.getSelfDamageCalc() != null ? stats.getSelfDamageCalc().getMulti() : 0);
        obj.put("enemy_effect_reps", stats.getTargetEffectCalc() != null ? stats.getTargetEffectCalc().getMulti() : 0);
        obj.put("self_effect_reps_neg", stats.getNegSelfEffectCalc() != null ? stats.getNegSelfEffectCalc().getMulti() : 0);
        obj.put("self_effect_reps_pos", stats.getPosSelfEffectCalc() != null ? stats.getPosSelfEffectCalc().getMulti() : 0);
        var eff = json.createObjectNode();
        if (stats.getTargetEffects() != null) {
            for (int i = 0; i < stats.getTargetEffects().length; ++i) {
                eff.putIfAbsent(("effect" + i), stats.getTargetEffects()[i].toJson());
            }
        }
        obj.putIfAbsent("effects", eff);
        var seff = json.createObjectNode();
        if (stats.getNegSelfEffects() != null) {
            for (int i = 0; i < stats.getNegSelfEffects().length; ++i) {
                seff.putIfAbsent(("effect" + i), stats.getNegSelfEffects()[i].toJson());
            }
        }
        if (stats.getPosSelfEffects() != null) {
            for (int i = 0; i < stats.getPosSelfEffects().length; ++i) {
                seff.putIfAbsent(("effect" + i), stats.getPosSelfEffects()[i].toJson());
            }
        }
        obj.putIfAbsent("self_effects", seff);
        if (stats.getSelfDamage() != null) {
            obj.putIfAbsent("self_damage", stats.getSelfDamage().toJson());
        } else {
            obj.put("self_damage", "null");
        }
        obj.put("description", stats.getDescription());
        return obj;
    }
}

