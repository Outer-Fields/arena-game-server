package io.mindspice.okragameserver.game.bot.behavior.logic;

import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.bot.state.TreeFocusState;
import io.mindspice.okragameserver.game.cards.AbilityCard;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.PlayerAction;
import io.mindspice.okragameserver.game.gameroom.effect.ActiveEffect;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static io.mindspice.okragameserver.game.enums.StatType.HP;


public class Decisions {

    public static Decision playerCountHigher = new Decision("playerCountHigher") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return botPlayerState.activePawnCount() > botPlayerState.getEnemyState().activePawnCount();
        }
    };

    public static Decision playerCountLower = new Decision("playerCountLower") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return botPlayerState.activePawnCount() < botPlayerState.getEnemyState().activePawnCount();
        }
    };

    public static Decision playerTotalHpLower = new Decision("playerTotalHpLower") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return botPlayerState.getTotalHP() < botPlayerState.getEnemyState().getTotalHP();
        }
    };

    public static Decision playerTotalHPHigher = new Decision("playerTotalHPHigher") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return botPlayerState.getTotalHP() > botPlayerState.getEnemyState().getTotalHP();
        }
    };

    public static Decision playerHasMortalPawns = new Decision("playerHasMortalPawns") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return botPlayerState.hasMortallyLowPawn();
        }
    };

    public static Decision enemyHasMortalPawns = new Decision("enemyHasMortalPawns") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return botPlayerState.getEnemyState().hasMortallyLowPawn();
        }
    };

    public static Decision confusionOver50Pct = new Decision("confusionOver50Pct") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            var confusion = botPlayerState.getPawn(selfIndex)
                    .getStatusEffects()
                    .stream()
                    .filter(e -> e.getType() == EffectType.CONFUSION)
                    .toList();

            for (ActiveEffect e : confusion) {
                if (e.getEffect().amount >= .5) return true;
            }
            return false;
        }
    };

    public static Decision confusionUnder50Pct = new Decision("confusionUnder50Pct") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            var confusion = botPlayerState.getPawn(selfIndex)
                    .getStatusEffects()
                    .stream()
                    .filter(e -> e.getType() == EffectType.CONFUSION)
                    .toList();

            for (ActiveEffect e : confusion) {
                if (e.getEffect().amount <= .5) {
                    focusState.focusPawn = selfIndex;
                    return true;
                }
            }
            return false;
        }
    };

    public static Decision confusionOver67Pct = new Decision("confusionOver67Pct") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            var confusion = botPlayerState.getPawn(selfIndex)
                    .getStatusEffects()
                    .stream()
                    .filter(e -> e.getType() == EffectType.CONFUSION)
                    .toList();

            for (ActiveEffect e : confusion) {
                if (e.getEffect().amount > .67) {
                    focusState.focusPawn = selfIndex;
                    return true;
                }
            }
            return false;
        }
    };

    public static Decision confusionRollOffSoon = new Decision("confusionRollOffSoon") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            var confusion = botPlayerState.getPawn(selfIndex)
                    .getStatusEffects()
                    .stream()
                    .filter(e -> e.getType() == EffectType.CONFUSION)
                    .toList();

            for (ActiveEffect e : confusion) {
                if (e.getEffect().isRollOffChance && e.getEffect().rollOffChance < .65) return false;
                if (!e.getEffect().isRollOffChance && e.getRollOffRounds() > 1) return false;
            }
            return true;
        }
    };

    public static Decision canCureConfusion = new Decision("canCureConfusion") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return botPlayerState.getPawn(selfIndex).getAbilityCards().entrySet().stream()
                    .flatMap(ac -> Arrays.stream(ac.getValue().getStats().getTargetEffects()))
                    .anyMatch(a -> a.type == EffectType.CLEAR_CONFUSION);
        }
    };

    public static Decision isSelfConfused = new Decision("isSelfConfused") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            var isConfused = botPlayerState.getPawn(selfIndex).getStatusEffects()
                    .stream()
                    .anyMatch(e -> e.getType() == EffectType.CONFUSION);
            if (isConfused) {
                focusState.effect = EffectType.CONFUSION;
                return true;
            }
            return false;
        }
    };

    // Is a combination of multiple "decisions" in one
    public static Decision playerNegStatusPawns = new Decision("playerNegStatusPawns-multi") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return botPlayerState.getLivingPawns()
                    .stream()
                    .filter(p -> p.getEffects().stream().anyMatch(e -> e.isNegative && e.effectClass == EffectType.EffectClass.CURSE))
                    .max(Comparator.comparingDouble(Pawn::getActionPotential))
                    .map(pawn -> {
                        focusState.focusPawn = pawn.getIndex();
                        focusState.effect = pawn.getEffects().stream()
                                .filter(e -> e.isNegative && e.effectClass == EffectType.EffectClass.CURSE)
                                .findFirst()
                                .get();
                        return true;
                    })
                    .orElse(false);
        }
    };

    public static Decision isSelf = new Decision("isSelf") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return focusState.focusPawn == selfIndex;
        }
    };

    //TODO check the cure process
    public static Decision canCure = new Decision("canCure") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {

            return botPlayerState.getPawn(selfIndex).getAbilityCards().entrySet().stream()
                    .flatMap(ac -> Arrays.stream(ac.getValue().getStats().getTargetEffects()))
                    .anyMatch(e -> e.type.effectClass == EffectType.EffectClass.CURE
                            && e.type.cureType.equals(focusState.effect.toString()));

        }
    };

    public static Decision calcPlayerHighestAP = new Decision("calcPlayerHighestAP") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            List<Pawn> pawns = botPlayerState.getLivingPawns();

            return pawns.stream()
                    .max(Comparator.comparingDouble(Pawn::getActionPotential))
                    .map(pawn -> {
                        focusState.focusPawn = pawn.getIndex();
                        return true;
                    })
                    .orElse(false);
        }
    };

    public static Decision setEnemyHighestAP = new Decision("setEnemyHighestAP") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return botPlayerState.getEnemyState().getLivingPawns()
                    .stream()
                    .sorted(Comparator.comparing(Pawn::getActionPotential).reversed())
                    .limit(2)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            pawns -> {
                                if (pawns.size() == 0) { return false; }
                                int index = pawns.size() > 1 && ThreadLocalRandom.current().nextDouble() < 0.5 ? 1 : 0;
                                focusState.focusPawn = pawns.get(index).getIndex();
                                return true;
                            }));
        }
    };

    public static Decision checkAndSetEnemyMortal = new Decision("checkAndSetEnemyMortal") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return botPlayerState.getEnemyState().getMortalPawns()
                    .stream()
                    .max(Comparator.comparingDouble(Pawn::getActionPotential))
                    .map(pawn -> {
                        focusState.focusPawn = pawn.getIndex();
                        return true;
                    }).orElse(false);
        }
    };

    public static Decision setFocusSelf = new Decision("setFocusSelf") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            focusState.focusPawn = selfIndex;
            return true;
        }
    };

    public static Decision player50PctLowerHP = new Decision("player50PctLowerHP") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return getTotalHPDifference(botPlayerState) <= .5;
        }
    };

    public static Decision player30PctLowerHP = new Decision("player30PctLowerHP") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return getTotalHPDifference(botPlayerState) <= .7;
        }
    };

    public static Decision playerEqualHP = new Decision("playerEqualHP") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            double diff = getTotalHPDifference(botPlayerState);
            return diff > .8 && diff < 1.2;
        }
    };

    public static Decision player30PctHigherHP = new Decision("player30PctHigherHP") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return getTotalHPDifference(botPlayerState) >= 1.3;
        }
    };

    public static Decision player50PctHigherHP = new Decision("player50PctHigherHP") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return getTotalHPDifference(botPlayerState) >= 1.5;
        }
    };

    public static Decision setLowPawnHighestAP = new Decision("setLowPawnHighestAP") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return botPlayerState.getLowPawns()
                    .stream().min(
                            Comparator.comparingDouble(Pawn::getActionPotential)
                                    .reversed()
                                    .thenComparing(p -> p.getStat(HP))
                    ).map(pawn -> {
                        focusState.focusPawn = pawn.getIndex();
                        return true;
                    })
                    .orElse(false);
        }

    };

    public static Decision setFocusPlayerMortal = new Decision("setFocusPlayerMortal") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            focusState.focusPawn = botPlayerState.getMortalPawns()
                    .stream()
                    .min(Comparator.comparingInt(p -> p.getStat(HP)))
                    .get()// This is fine as we only enter this if mortal pawns are detected
                    .getIndex();
            return true;
        }
    };

    public static Decision canHeal = new Decision("canHeal") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            return botPlayerState.getPawn(selfIndex).getAllCards().entrySet().stream()
                    .filter(ac -> ac.getValue().getStats().hasPosSelfEffects())
                    .flatMap(ac -> Arrays.stream(ac.getValue().getStats().getPosSelfEffects()))
                    .anyMatch(e -> e.type == EffectType.HEAL
                            || (e.type.effectClass == EffectType.EffectClass.SIPHON && e.type.statType == HP));
        }
    };

    public static Decision chance50Pct = new Decision("chance50Pct") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            int r = ThreadLocalRandom.current().nextInt(0, 101);
            return (r <= 50);
        }
    };

    public static double getTotalHPDifference(BotPlayerState botPlayerState) {
        if (botPlayerState.getEnemyState().getTotalHP() == 0) {
            return 0;
        }
        return botPlayerState.getTotalHP() / botPlayerState.getEnemyState().getTotalHP();
    }


    public static Decision canBuff = new Decision("canBuff") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            boolean canBuffAbility = botPlayerState.getPawn(selfIndex).getAllCards().entrySet().stream()
                    .filter(ac -> ac.getValue().getStats().hasPosSelfEffects())
                    .flatMap(ac -> Arrays.stream(ac.getValue().getStats().getPosSelfEffects()))
                    .anyMatch(a -> (a.type.effectClass == EffectType.EffectClass.MODIFIER && !a.type.isNegative)
                            || a.type.effectClass == EffectType.EffectClass.SIPHON);

            if (canBuffAbility) {
                focusState.focusPawn = botPlayerState.getLivingPawns()
                        .stream()// Will always have a living pawn if entering this
                        .max(Comparator.comparingDouble(Pawn::getActionPotential)).get().getIndex();
                return true;
            }
            return false;
        }
    };

    public static Decision canCurse = new Decision("canCurse") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {

            return botPlayerState.getPawn(selfIndex).getAllCards().entrySet().stream()
                    .filter(ac -> ac.getValue().getStats().hasTargetEffects())
                    .flatMap(ac -> Arrays.stream(ac.getValue().getStats().getTargetEffects()))
                    .anyMatch(a -> a.type.effectClass == EffectType.EffectClass.CURSE
                            || (a.type.effectClass == EffectType.EffectClass.MODIFIER && a.type.isNegative));
        }
    };

    public static Decision getRandomEnemy = new Decision("getRandomEnemy") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            var enemyList = botPlayerState.getEnemyState().getLivingPawns();
            if (enemyList.isEmpty()) { return false; }
            focusState.focusPawn = enemyList.get(ThreadLocalRandom.current().nextInt(0, enemyList.size())).getIndex();
            return true;
        }
    };

    public static Decision canPlunder = new Decision("canPlunderSetFocus") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            Pawn playerPawn = botPlayerState.getPawn(selfIndex);
            if (playerPawn.getAbilityCards().containsValue(AbilityCard.ABILITY_PLUNDER)) {
                Optional<Pawn> enemyFocus = botPlayerState.getEnemyState().getLivingPawns().stream()
                        .filter(e -> {
                            int p1Lvl = playerPawn.getActionCard(1) != null ? playerPawn.getActionCard(1).getLevel() : 0;
                            int p2Lvl = playerPawn.getActionCard(2) != null ? playerPawn.getActionCard(2).getLevel() : 0;
                            int e1Lvl = e.getActionCard(1) != null ? e.getActionCard(1).getLevel() : 99;
                            int e2Lvl = e.getActionCard(2) != null ? e.getActionCard(2).getLevel() : 99;
                            if (e1Lvl < p1Lvl || e1Lvl < p2Lvl) { return true; }
                            return e2Lvl < p1Lvl || e2Lvl < p2Lvl;
                        })
                        .max(Comparator.comparingInt(Pawn::getActionPotential));
                if (enemyFocus.isPresent()) {
                    focusState.focusPawn = enemyFocus.get().getIndex();
                    focusState.playerAction = playerPawn.getCardSlot(AbilityCard.ABILITY_PLUNDER);
                    return true;
                }
            }

            if (!playerPawn.getActionCards().containsValue(AbilityCard.ACTION_PLUNDER) && playerPawn.getActionCards().size() == 2) {
                return false;
            }

            Optional<Pawn> enemyFocus = botPlayerState.getEnemyState().getLivingPawns().stream()
                    .filter(e -> e.getPawnCard().actionType == playerPawn.getPawnCard().actionType)
                    .filter(e -> e.getActionCards().size() > 0)
                    .filter(e -> {
                        int p1Lvl = playerPawn.getAbilityCard(1) != null ? playerPawn.getAbilityCard(1).getLevel() : 0;
                        int p2Lvl = playerPawn.getAbilityCard(2) != null ? playerPawn.getAbilityCard(2).getLevel() : 0;
                        int e1Lvl = e.getAbilityCard(1) != null ? e.getAbilityCard(1).getLevel() : 99;
                        int e2Lvl = e.getAbilityCard(2) != null ? e.getAbilityCard(2).getLevel() : 99;
                        if (e1Lvl < p1Lvl || e1Lvl < p2Lvl) { return true; }
                        return e2Lvl < p1Lvl || e2Lvl < p2Lvl;
                    })
                    .max(Comparator.comparingInt(Pawn::getActionPotential));

            if (enemyFocus.isPresent()) {
                focusState.focusPawn = enemyFocus.get().getIndex();
                focusState.playerAction = playerPawn.getCardSlot(AbilityCard.ACTION_PLUNDER);
                return true;
            }

            return false;
        }
    };

    public static Decision canDemise = new Decision("canDemise") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {

            Pawn playerPawn = botPlayerState.getPawn(selfIndex);
            Optional<PlayerAction> action = playerPawn.getAbilityCards().entrySet().stream()
                    .filter(c -> c.getValue() == AbilityCard.SACRIFICIAL_DEMISE)
                    .findFirst().map(Map.Entry::getKey);

            if (action.isEmpty()) { return false; }

            Optional<Pawn> focusPawn = botPlayerState.getEnemyState().getLivingPawns().stream()
                    .filter(p -> (float) (p.getStat(HP) / p.getStatMax(HP)) <= 0.6
                            && p.getStat(HP) < playerPawn.getStat(HP))
                    .max(Comparator.comparingInt(Pawn::getActionPotential));

            if (focusPawn.isEmpty()) { return false; }

            focusState.playerAction = action.get();
            focusState.focusPawn = focusPawn.get().getIndex();
            return true;
        }
    };

    public static Decision canCapitulate = new Decision("canCapitulate") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {

            Pawn playerPawn = botPlayerState.getPawn(selfIndex);
            Optional<PlayerAction> action = playerPawn.getAbilityCards().entrySet().stream()
                    .filter(c -> c.getValue() == AbilityCard.ARMS_TRADE)
                    .findFirst().map(Map.Entry::getKey);

            if (action.isEmpty()) { return false; }

            Optional<Pawn> enemyFocus = botPlayerState.getEnemyState().getLivingPawns().stream()
                    .filter(e -> {
                        int p1Lvl = playerPawn.getWeaponCard(1) != null ? playerPawn.getWeaponCard(1).getLevel() : 0;
                        int p2Lvl = playerPawn.getWeaponCard(2) != null ? playerPawn.getWeaponCard(2).getLevel() : 0;
                        int e1Lvl = e.getWeaponCard(1) != null ? e.getWeaponCard(1).getLevel() : 99;
                        int e2Lvl = e.getWeaponCard(2) != null ? e.getWeaponCard(2).getLevel() : 99;
                        if (e1Lvl < p1Lvl || e1Lvl < p2Lvl) { return true; }
                        return e2Lvl < p1Lvl || e2Lvl < p2Lvl;
                    }).max(Comparator.comparingInt(Pawn::getActionPotential));

            if (enemyFocus.isEmpty()) { return false; }

            focusState.playerAction = action.get();
            focusState.focusPawn = enemyFocus.get().getIndex();
            return true;

        }
    };

    public static Decision currentPawnLow = new Decision("currentPawnLow") {
        @Override
        public boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
            Pawn playerPawn = botPlayerState.getPawn(selfIndex);
            return (playerPawn.getStat(HP) <= playerPawn.getStatMax(HP) * .3);
        }
    };
}



