package io.mindspice.okragameserver.game.bot.behavior.logic;

import io.mindspice.mindlib.data.tuples.Triple;
import io.mindspice.mindlib.functional.consumers.QuadConsumer;
import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.bot.state.TreeFocusState;
import io.mindspice.okragameserver.game.cards.Card;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.PlayerAction;
import io.mindspice.okragameserver.game.enums.*;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.util.Log;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.stream.Collectors;


public class Actions {

    static Comparator<Triple<Pawn, StatType, Integer>> statComparator = (t1, t2) -> {
        int healing1 = Math.abs((t1.first().getStat(t1.second()) + t1.third()) - t1.first().getStatMax(t1.second()));
        int healing2 = Math.abs((t2.first().getStat(t2.second()) + t2.third()) - t2.first().getStatMax(t2.second()));
        return Integer.compare(healing1, healing2);
    };

    // botPlayerState cardAction, selfIndex, focusIndex
    static QuadConsumer<BotPlayerState, PlayerAction, PawnIndex, PawnIndex> playCard = BotPlayerState::doAction;

    // A lot of potion logic is left in and commented out for when potions are added later
    public static final Action selectBestCure = new Action() {
        @Override
        public boolean doAction(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            Pawn playerPawn = botPlayerState.getPawn(selfIndex);

            /* Select Health Cure */
            if (focusState.effect == null) {
                Optional<PlayerAction> playerAction = playerPawn.getAbilityCards()
                        .entrySet()
                        .stream()
                        .filter(ac -> canDoAction(playerPawn, ac.getValue().getStats().getCost().asMap()))
                        .filter(ac -> ac.getValue().getStats().hasPosSelfEffects())
                        .filter(ac -> Arrays.stream(ac.getValue().getStats().getPosSelfEffects())
                                .anyMatch(e -> (
                                        e.type.effectClass == EffectType.EffectClass.CURE
                                                || e.type.effectClass == EffectType.EffectClass.SIPHON
                                ) && e.type.statType == StatType.HP)
                        )
                        .min((c1, c2) -> statComparator.compare(
                                new Triple<>(playerPawn, StatType.HP, c1.getValue().getStats().getBuffSum()),
                                new Triple<>(playerPawn, StatType.HP, c2.getValue().getStats().getBuffSum())
                        ))
                        .map(Map.Entry::getKey);

                if (playerAction.isPresent()) {
                    botPlayerState.doAction(playerAction.get(), selfIndex, focusState.focusPawn);
                    return true;
                }
            } else { // Select status cure
                Optional<PlayerAction> playerAction = playerPawn.getAbilityCards().entrySet().stream()
                        .filter(ac -> canDoAction(playerPawn, ac.getValue().getStats().getCost().asMap()))
                        .filter(ac -> ac.getValue().getStats().hasPosSelfEffects())
                        .filter(ac -> Arrays.stream(ac.getValue().getStats().getPosSelfEffects())
                                .anyMatch(e -> e.type.effectClass == EffectType.EffectClass.CURE
                                        && e.type.cureType.equals(focusState.effect.toString())))
                        .findFirst().map(Map.Entry::getKey);

                if (playerAction.isPresent()) {
                    botPlayerState.doAction(playerAction.get(), selfIndex, focusState.focusPawn);
                    return true;
                }
            }
            return false;
        }
    };

    // FIXME, atm siphon just mirrors player pawn index as its focus pawn
    public static final Action selectBestBuff = new Action() {
        @Override
        public boolean doAction(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            Pawn playerPawn = botPlayerState.getPawn(selfIndex);

            // Select a buff
            Optional<PlayerAction> playerAction = playerPawn.getAbilityCards().entrySet().stream()
                    .filter(ac -> canDoSmart(playerPawn, ac.getValue().getStats().getCost().asMap()))
                    .filter(ac -> ac.getValue().getStats().hasPosSelfEffects())
                    .filter(ac -> Arrays.stream(ac.getValue().getStats().getPosSelfEffects())
                            .anyMatch(e -> (e.type.effectClass == EffectType.EffectClass.MODIFIER && !e.type.isNegative)
                                    || e.type.effectClass == EffectType.EffectClass.SIPHON))
                    .max(Comparator.comparingInt(c -> c.getValue().getStats().getBuffSum()))
                    .map(Map.Entry::getKey);

            if (playerAction.isPresent()) {
                botPlayerState.doAction(playerAction.get(), selfIndex, focusState.focusPawn);
                return true;
            }
            return false;
        }
    };

    public static final Action selectCurse = new Action() {
        @Override
        public boolean doAction(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            //TODO factor in weapon enchantments on both this and the decision node for it

            Pawn playerPawn = botPlayerState.getPawn(selfIndex);

            Optional<PlayerAction> playerAction = playerPawn.getAllCards().entrySet().stream()
                    //.filter(c -> c.getValue() instanceof WeaponCard || c.getValue() instanceof  AbilityCard)
                    .filter(ac -> canDoSmart(playerPawn, ac.getValue().getStats().getCost().asMap()))
                    .filter(c -> c.getValue().getStats().hasTargetEffects())
                    .filter(ac -> Arrays.stream(ac.getValue().getStats().getTargetEffects())
                            .anyMatch(e -> (e.type.effectClass == EffectType.EffectClass.MODIFIER && e.type.isNegative)
                                    || e.type.effectClass == EffectType.EffectClass.CURSE))
                    .findFirst().map(Map.Entry::getKey);

            if (playerAction.isPresent()) {
                botPlayerState.doAction(playerAction.get(), selfIndex, focusState.focusPawn);
                return true;
            }
            return false;
        }

    };

    public static final Action selectBestAttack = new Action() {
        @Override
        public boolean doAction(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            Pawn playerPawn = botPlayerState.getPawn(selfIndex);
            int enemyHp = botPlayerState.getEnemyState().getPawn(focusState.focusPawn).getStat(StatType.HP);

            Map<PlayerAction, Card> attackCards = playerPawn.getAllCards().entrySet().stream()
                    .filter(c -> canDoAction(playerPawn, c.getValue().getStats().getCost().asMap()))
                    .filter(c -> c.getValue().getStats().hasDamage())
                    .sorted(Comparator.comparingInt(entry -> entry.getValue().getStats().getRelativeSelfDamage()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (existingValue, newValue) -> existingValue,
                            LinkedHashMap::new // Collect into a LinkedHashMap to maintain order
                    ));

            if (attackCards.isEmpty()) { return false; }

            Optional<PlayerAction> killCard = attackCards.entrySet().stream().filter(c -> c.getValue().getStats().getHpDamage() > enemyHp)
                    .max(Comparator.comparingInt(c -> c.getValue().getStats().getRelativeEnemyDamage()))
                    .map(Map.Entry::getKey);

            if (killCard.isPresent()) {
                botPlayerState.doAction(killCard.get(), selfIndex, focusState.focusPawn);
                return true;
            }

            if (playerPawn.getPawnCard().actionType == ActionType.MELEE && playerPawn.getStat(StatType.SP) < 100) {
                Optional<PlayerAction> abilityCard = playerPawn.getAbilityCards().entrySet().stream()
                        .filter(c -> canDoSmart(playerPawn, c.getValue().getStats().getCost().asMap()))
                        .findFirst().map(Map.Entry::getKey);
                if (abilityCard.isPresent()) {
                    botPlayerState.doAction(abilityCard.get(), selfIndex, focusState.focusPawn);
                    return true;
                }
                abilityCard = playerPawn.getAbilityCards().entrySet().stream()
                        .filter(c -> canDoAction(playerPawn, c.getValue().getStats().getCost().asMap()))
                        .findFirst().map(Map.Entry::getKey);
                if (abilityCard.isPresent()) {
                    botPlayerState.doAction(abilityCard.get(), selfIndex, focusState.focusPawn);
                    return true;
                }
            }

            Optional<PlayerAction> attackCard = attackCards.entrySet().stream()
                    .filter(c -> canDoSmart(playerPawn, c.getValue().getStats().getCost().asMap()))
                    .findFirst().map(Map.Entry::getKey);

            if (attackCard.isPresent()) {
                botPlayerState.doAction(attackCard.get(), selfIndex, focusState.focusPawn);
                return true;
            }

            attackCard = attackCards.entrySet().stream()
                    .min(Comparator.comparingInt(c -> c.getValue().getStats().getCost().getSum()))
                    .map(Map.Entry::getKey);

            if (attackCard.isPresent()) {
                botPlayerState.doAction(attackCard.get(), selfIndex, focusState.focusPawn);
                return true;
            }

            return false;
        }
    };

    public static final Action selectRandomAction = new Action() {
        @Override
        public boolean doAction(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            Pawn playerPawn = botPlayerState.getPawn(selfIndex);

            BiFunction<Pawn, Map<StatType, Integer>, Boolean> costCalc;
            costCalc = ThreadLocalRandom.current().nextFloat(1.0f) <= 0.5f
                    ? Actions::canDoSmart : Actions::canDoLight;

            Map<PlayerAction, Card> attackCards = playerPawn.getAllCards().entrySet().stream()
                    .filter(c -> canDoAction(playerPawn, c.getValue().getStats().getCost().asMap()))
                    .filter(c -> c.getValue().getStats().hasDamage())
                    .sorted(Comparator.comparingInt(entry -> entry.getValue().getStats().getRelativeSelfDamage()))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (existingValue, newValue) -> existingValue,
                            LinkedHashMap::new // Collect into a LinkedHashMap to maintain order
                    ));

            BiFunction<Pawn, Map<StatType, Integer>, Boolean> finalCostCalc = costCalc;
            Optional<PlayerAction> attackCard = attackCards.entrySet().stream()
                    .filter(c -> finalCostCalc.apply(playerPawn, c.getValue().getStats().getCost().asMap()))
                    .findFirst().map(Map.Entry::getKey);

            if (attackCard.isPresent()) {
                botPlayerState.doAction(attackCard.get(), selfIndex, focusState.focusPawn);
                focusState.decisions.add("Smart_Or_Light_action");

                return true;
            }

            attackCard = attackCards.entrySet().stream()
                    .filter(c -> costUnderHalf(playerPawn, c.getValue().getStats().getCost().asMap()))
                    .findFirst().map(Map.Entry::getKey);

            if (attackCard.isPresent()) {
                botPlayerState.doAction(attackCard.get(), selfIndex, focusState.focusPawn);
                focusState.decisions.add("Cost_Under_Half_Action");

                return true;
            }

            Log.SERVER.debug(this.getClass(), "GameRoom: " + botPlayerState.getRoomId()
                    + " | BotPlayerId: " + botPlayerState.getId() + " | Skipped Turn"
                    + " Card Info:"
                    + playerPawn.getAllCards().entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().getStats().getCost().asMap()))
                    + "Stats: " + playerPawn.getStatsMap());

            botPlayerState.doAction(PlayerAction.SKIP_PAWN, selfIndex, focusState.focusPawn);
            focusState.decisions.add("SKIP_PAWN");
            return true;
        }

    };

    public static final Action doSetAction = new Action() {
        @Override
        public boolean doAction(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            Pawn playerPawn = botPlayerState.getPawn(selfIndex);
            if (focusState.playerAction == null
                    || !canDoAction(playerPawn, playerPawn.getAction(focusState.playerAction).getStats().getCost().asMap())) {
                return false;
            }
            botPlayerState.doAction(focusState.playerAction, selfIndex, focusState.focusPawn);
            return true;
        }
    };

//    public static final Action doDemise = new Action() {
//        @Override
//        public boolean doAction(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
//            if (focusState.playerAction == null) { return false; }
//            botPlayerState.doAction(focusState.playerAction, selfIndex, focusState.focusPawn);
//            return true;
//        }
//    };
//
//    public static final Action doCapitulate = new Action() {
//        @Override
//        public boolean doAction(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
//            if (focusState.playerAction == null) { return false; }
//            botPlayerState.doAction(focusState.playerAction, selfIndex, focusState.focusPawn);
//            return true;
//        }
//    };

    public static final Action doNothing = new Action() {
        @Override
        public boolean doAction(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            botPlayerState.doAction(PlayerAction.SKIP_PAWN, selfIndex, selfIndex);
            return true;
        }
    };

    public static boolean canDoAction(Pawn pawn, Map<StatType, Integer> statMap) {
        return statMap.entrySet().stream()
                .filter(e -> StatType.costStats().contains(e.getKey()))
                .allMatch(entry -> pawn.getStat(entry.getKey()) >= entry.getValue());
    }

    public static boolean canDoLight(Pawn pawn, Map<StatType, Integer> statMap) {
        return statMap.entrySet().stream()
                .filter(e -> StatType.costStats().contains(e.getKey()))
                .allMatch(entry -> pawn.getStat(entry.getKey()) / 5 >= entry.getValue());
    }

    public static boolean canDoSmart(Pawn pawn, Map<StatType, Integer> statMap) {
        var isMpLow = pawn.getStat(StatType.MP) < pawn.getStatMax(StatType.MP) / 4;
        var isSpLow = pawn.getStat(StatType.SP) < pawn.getStatMax(StatType.SP) / 4;
        for (var entry : statMap.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            if (key == StatType.MP || key == StatType.SP) {
                if (isMpLow && value > pawn.getStat(StatType.MP) / 3) { return false; }
                if (isSpLow && value > pawn.getStat(StatType.SP) / 3) { return false; }
            }
            if (value > pawn.getStat(key)) { return false; }
        }
        return true;
    }

    public static boolean costUnderHalf(Pawn pawn, Map<StatType, Integer> statMap) {
        return statMap.entrySet().stream()
                .allMatch(entry -> pawn.getStat(entry.getKey()) / 2 >= entry.getValue());
    }
}

