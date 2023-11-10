package io.mindspice.okragameserver.cards;


import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.cards.*;
import io.mindspice.okragameserver.game.enums.*;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;
import io.mindspice.okragameserver.game.player.Player;
import io.mindspice.okragameserver.schema.PawnSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static io.mindspice.okragameserver.game.enums.StatType.*;
import static org.mockito.Mockito.mock;


public class CardSim {


    @BeforeEach
    void setUp() {
    }


    @Test
    void getDamageStats(){
        var damagePerCostsRand = new EnumMap<WeaponCard, Double>(WeaponCard.class);
        var damagePerLevelRand = new EnumMap<WeaponCard, Double>(WeaponCard.class);
        var damagePerCostsChaos = new EnumMap<WeaponCard, Double>(WeaponCard.class);
        var damagePerLevelChaos= new EnumMap<WeaponCard, Double>(WeaponCard.class);
        var damagePerCostsChaosSame = new EnumMap<WeaponCard, Double>(WeaponCard.class);
        var damagePerLevelChaosSame = new EnumMap<WeaponCard, Double>(WeaponCard.class);
        var damagePerCostsOrder = new EnumMap<WeaponCard, Double>(WeaponCard.class);
        var damagePerLevelOrder= new EnumMap<WeaponCard, Double>(WeaponCard.class);
        var damagePerCostsOrderSame = new EnumMap<WeaponCard, Double>(WeaponCard.class);
        var damagePerLevelOrderSame= new EnumMap<WeaponCard, Double>(WeaponCard.class);

        for (var card : WeaponCard.values()) {
            var stats = getCardStats(card, null, null, true);
            damagePerCostsRand.put(card, stats[0]);
            damagePerLevelRand.put(card, stats[1]);
            stats = getCardStats(card, Alignment.CHAOS, Alignment.ORDER, false);
            damagePerCostsChaos.put(card, stats[0]);
            damagePerLevelChaos.put(card, stats[1]);
            stats = getCardStats(card, Alignment.ORDER, Alignment.CHAOS, false);
            damagePerCostsOrder.put(card, stats[0]);
            damagePerLevelOrder.put(card, stats[1]);
            stats = getCardStats(card, Alignment.CHAOS, Alignment.CHAOS, false);
            damagePerCostsChaosSame.put(card, stats[0]);
            damagePerLevelChaosSame.put(card, stats[1]);
            stats = getCardStats(card, Alignment.ORDER, Alignment.ORDER, false);
            damagePerCostsOrderSame.put(card, stats[0]);
            damagePerLevelOrderSame.put(card, stats[1]);
        }

        for (var dpc : damagePerCostsRand.entrySet()) {
            System.out.println("----------------------------");
            System.out.println(dpc.getKey());
            System.out.println("Level: " + dpc.getKey().getStats().getLevel());
            System.out.println("Avg:");
            System.out.println("\tDPC: " + dpc.getValue());
            System.out.println("\tDPL: " + damagePerLevelRand.get(dpc.getKey()));
            System.out.println("Chaos -> Order:");
            System.out.println("\tDPC: " + damagePerCostsChaos.get(dpc.getKey()));
            System.out.println("\tDPL: " + damagePerLevelChaos.get(dpc.getKey()));
            System.out.println("Order -> Chaos:");
            System.out.println("\tDPC: " + damagePerCostsOrder.get(dpc.getKey()));
            System.out.println("\tDPL: " + damagePerLevelOrder.get(dpc.getKey()));
            System.out.println("Chaos -> Chaos:");
            System.out.println("\tDPC: " + damagePerCostsChaosSame.get(dpc.getKey()));
            System.out.println("\tDPL: " + damagePerLevelChaosSame.get(dpc.getKey()));
            System.out.println("Order -> Order:");
            System.out.println("\tDPC: " + damagePerCostsOrderSame.get(dpc.getKey()));
            System.out.println("\tDPL: " + damagePerLevelOrderSame.get(dpc.getKey()));
        }
    }


    @Test
    void debug() {
        var states = getFreshStates();
        var card = WeaponCard.ZWEIHANDER_OF_EXCELLENCE_GOLD;
       var actionReturn = card.playCard(states[0], states[1], PawnIndex.PAWN1, PawnIndex.PAWN1);
        System.out.println(actionReturn);
        getCardStats(WeaponCard.ZWEIHANDER_OF_EXCELLENCE_GOLD,Alignment.CHAOS,Alignment.ORDER,true);
    }


    private EnumMap<StatType, Integer> getDamageStats(List<PawnInterimState> interimStates) {
        var damage = new EnumMap<StatType, Integer>(StatType.class);
        damage.put(HP, 0);
        damage.put(DP, 0);
        damage.put(SP, 0);
        damage.put(MP, 0);
        damage.put(LP, 0);
        damage.put(WP, 0);
        for (var interim : interimStates) {
            if (interim.getDamageMap() == null) {
                continue;
            }
            interim.getDamageMap().forEach((k, v) -> {
                damage.put(k, damage.get(k) + v);
            });
        }
        for (var entry : damage.entrySet()) {
            if (entry.getKey() == DP || entry.getKey() == SP || entry.getKey() == MP) {
                damage.put(entry.getKey(), damage.get(entry.getKey()) * 4);
            } else if (entry.getKey() == LP) {
                damage.put(entry.getKey(), damage.get(entry.getKey()) * 100);
            } else if (entry.getKey() == WP) {
                damage.put(entry.getKey(), damage.get(entry.getKey()) * 250);
            }
        }
        return damage;
    }

    private EnumMap<StatType, Integer> getEffectStats(Effect[] effects, int multi) {
        var damage = new EnumMap<StatType, Integer>(StatType.class);
        damage.put(HP, 0);
        damage.put(DP, 0);
        damage.put(SP, 0);
        damage.put(MP, 0);
        damage.put(WP, 0);
        damage.put(LP, 0);
        if (effects == null) {
            return damage;
        }


        for (var effect : effects) {
            var amount = 0.0;
            var statType = effect.type.statType;
            if (effect.type.effectClass == EffectType.EffectClass.CURSE) {
                amount += effectOverTime(effect.amount * effect.scalar, effect.isRollOffChance ? effect.rollOffChance : effect.rollOffRounds, effect.isRollOffChance, effect.chance, multi);
                if (effect.actionClass == ActionClass.MULTI) {
                    amount += effectOverTime(effect.amount * effect.altScalar * 2, effect.isRollOffChance ? effect.rollOffChance : effect.rollOffRounds, effect.isRollOffChance, effect.chance, multi);
                }
                switch (effect.type) {
                    case PARALYSIS -> {
                        amount *= 200;
                        statType = HP;

                    }
                    case SLEEP, CONFUSION -> {
                        amount *= 300;
                        statType = HP;
                    }
                }
            } else {
                amount += effect.amount * effect.scalar * effect.chance * multi;
                if (effect.type.effectClass == EffectType.EffectClass.MODIFIER) {
                    if (effect.type.statType != WP && effect.type.statType != LP) {
                        amount = (int) (amount / (effect.chance * 10));

                    }
                }
                if (effect.actionClass == ActionClass.MULTI) {
                    amount += effect.amount * effect.altScalar * effect.altChance * 2 * multi;
                    if (effect.type.effectClass == EffectType.EffectClass.MODIFIER) {
                        amount = (int)(amount / (effect.altChance * 10));
                    }

                }
            }

            if (statType == null) continue;
            if (effect.type.effectClass == EffectType.EffectClass.MODIFIER) {
                if (effect.type.statType != WP && effect.type.statType != LP) {
                    amount = (int) (amount / (effect.chance * 10));
                } else {
                   // amount *= effect.chance;
                }
            }

            damage.put(statType, damage.get(statType) + (int)amount);
        }
        for (var entry : damage.entrySet()) {
            if (entry.getKey() == DP || entry.getKey() == SP || entry.getKey() == MP) {
                damage.put(entry.getKey(), damage.get(entry.getKey()) * 4);
            } else if(entry.getKey() == LP) {
                damage.put(entry.getKey(), damage.get(entry.getKey()) * 100);
            } else if (entry.getKey() == WP) {
                damage.put(entry.getKey(), damage.get(entry.getKey()) * 250);
            }
        }


        return damage;
    }

    private double effectOverTime(double amount, double rolloff, boolean isChanceRollOff, double chance, int multi){
        double finAmount = 0;
        for (int i =0; i < multi; ++i) {
            if (ThreadLocalRandom.current().nextDouble(1) > chance) {
                continue;
            }

            if (!isChanceRollOff) {
                finAmount += (amount * rolloff);
            } else {
                while (ThreadLocalRandom.current().nextDouble(1) > rolloff) {
                    finAmount += amount;
                }
            }
        }
        return finAmount;
    }



    private PlayerGameState[] getFreshStates() {
        PlayerGameState playerState = new PlayerGameState(mock(Player.class));
        PlayerGameState enemyState = new PlayerGameState(mock(Player.class));

        Pawn p_magePawn = new Pawn(
                PawnIndex.PAWN1,
                PawnCard.DARK_SENTINEL,
                TalismanCard.BALANCE_BEAD,
                WeaponCard.STAFF_OF_FORTILITY,
                WeaponCard.STAFF_OF_FORTILITY,
                new ArrayList<>(List.of(ActionCard.values())),
                new ArrayList<>(List.of(AbilityCard.values())),
                new ArrayList<>(List.of(PowerCard.values())));
        Pawn p_rangerPawn = new Pawn(
                PawnIndex.PAWN2,
                PawnCard.IMPERIAL_MAGI,
                TalismanCard.BALANCE_BEAD,
                WeaponCard.BOW_OF_POISONING,
                WeaponCard.BOW_OF_POISONING,
                new ArrayList<>(List.of(ActionCard.values())),
                new ArrayList<>(List.of(AbilityCard.values())),
                new ArrayList<>(List.of(PowerCard.values())));
        Pawn p_warriorPawn = new Pawn(
                PawnIndex.PAWN3,
                PawnCard.OKRA_WARRIOR,
                TalismanCard.BALANCE_BEAD,
                WeaponCard.CUTLASS_OF_FURY,
                WeaponCard.CUTLASS_OF_FURY,
                new ArrayList<>(List.of(ActionCard.values())),
                new ArrayList<>(List.of(AbilityCard.values())),
                new ArrayList<>(List.of(PowerCard.values())));
        playerState.setPawns(List.of(p_magePawn, p_rangerPawn, p_warriorPawn));

        Pawn e_magePawn = new Pawn(
                PawnIndex.PAWN1,
                PawnCard.DARK_SENTINEL,
                TalismanCard.BALANCE_BEAD,
                WeaponCard.STAFF_OF_FORTILITY,
                WeaponCard.STAFF_OF_FORTILITY,
                new ArrayList<>(List.of(ActionCard.values())),
                new ArrayList<>(List.of(AbilityCard.values())),
                new ArrayList<>(List.of(PowerCard.IRON_JAW)));
        Pawn e_rangerPawn = new Pawn(
                PawnIndex.PAWN2,
                PawnCard.IMPERIAL_MAGI,
                TalismanCard.BALANCE_BEAD,
                WeaponCard.BOW_OF_POISONING,
                WeaponCard.BOW_OF_POISONING,
                new ArrayList<>(List.of(ActionCard.values())),
                new ArrayList<>(List.of(AbilityCard.values())),
                new ArrayList<>(List.of(PowerCard.IRON_JAW)));
        Pawn e_warriorPawn = new Pawn(
                PawnIndex.PAWN3,
                PawnCard.IMPERIAL_RANGER,
                TalismanCard.BALANCE_BEAD,
                WeaponCard.CUTLASS_OF_FURY,
                WeaponCard.CUTLASS_OF_FURY,
                new ArrayList<>(List.of(ActionCard.values())),
                new ArrayList<>(List.of(AbilityCard.values())),
                new ArrayList<>(List.of(PowerCard.IRON_JAW)));
        enemyState.setPawns(List.of(e_magePawn, e_rangerPawn, e_warriorPawn));

        return new PlayerGameState[]{playerState, enemyState};
    }

    double[] getCardStats(WeaponCard card, Alignment pAlignOverride, Alignment tAlignOverride, boolean print) {


        var cost = card.getStats().getCost().asMap().values().stream().mapToInt(Integer::intValue).sum();

        var totalEnemyDamage = new ArrayList<Integer>();
        var totalSelfDamage = new ArrayList<Integer>();
        var totalEnemyEffectDamage = new ArrayList<Integer>();
        var totalSelfEffectDamage = new ArrayList<Integer>();
        var fullEnemyDamage = new ArrayList<Integer>();
        var fullSelfDamage = new ArrayList<Integer>();
        var offsetDamageDealt = new ArrayList<Integer>();
        var damagePerCost = new ArrayList<Double>();
        var damagePerLevel = new ArrayList<Double>();
        Map<StatType, Integer> costMap = null;
        EnumMap<StatType, Integer> enemyDamageMap = null;
        EnumMap<StatType, Integer> selfDamageMap = null;
        EnumMap<StatType, Integer> enemyEffectDamageMap = null;
        EnumMap<StatType, Integer> selfEffectDamageMap = null;


        var inc = 0;
        for (int i = 0; i < 100; ++i) {
            for (var align1 : Alignment.values()) {
                for (var align2 : Alignment.values()) {
                    inc++;
                    var states = getFreshStates();
                    states[0].getPawn(PawnIndex.PAWN1).setAlignment( pAlignOverride == null ? align1 : pAlignOverride);
                    states[1].getPawn(PawnIndex.PAWN1).setAlignment(tAlignOverride == null ? align2 : tAlignOverride);
                    states[0].getPawn(PawnIndex.PAWN2).setAlignment( pAlignOverride == null ? align2 : pAlignOverride);
                    states[1].getPawn(PawnIndex.PAWN2).setAlignment(tAlignOverride == null ? align1 : tAlignOverride);
                    states[0].getPawn(PawnIndex.PAWN3).setAlignment( pAlignOverride == null ? align1 : pAlignOverride);
                    states[1].getPawn(PawnIndex.PAWN3).setAlignment(tAlignOverride == null ? align2 : tAlignOverride);

                    costMap = card.getStats().getCost().asMap();
                    var actionReturn = card.playCard(states[0], states[1], PawnIndex.PAWN1, PawnIndex.PAWN1);
                    enemyDamageMap = getDamageStats(actionReturn.targetPawnStates);
                    selfDamageMap = getDamageStats(actionReturn.playerPawnStates);

                    totalEnemyDamage.add(enemyDamageMap.values()
                            .stream()
                            .mapToInt(Integer::intValue)
                            .sum());

                    totalSelfDamage.add(selfDamageMap.values()
                            .stream()
                            .mapToInt(Integer::intValue)
                            .sum());

                    enemyEffectDamageMap = getEffectStats(card.getStats().getTargetEffects(), card.getStats().getTargetEffectCalc() != null ? card.getStats().getTargetEffectCalc().getMulti() : 1);
                    selfEffectDamageMap = getEffectStats(card.getStats().getNegSelfEffects(), card.getStats().getNegSelfEffectCalc() != null ? card.getStats().getNegSelfEffectCalc().getMulti() : 1);

                    totalEnemyEffectDamage.add(enemyEffectDamageMap.values()
                            .stream()
                            .mapToInt(Integer::intValue)
                            .sum());

                    totalSelfEffectDamage.add(selfEffectDamageMap.values()
                            .stream()
                            .mapToInt(Integer::intValue)
                            .sum());
                }
            }
        }

        var fed =  totalEnemyDamage.stream().mapToInt(Integer::intValue).average().getAsDouble()
                + totalEnemyEffectDamage.stream().mapToInt(Integer::intValue).average().getAsDouble();
        var fsd = totalSelfDamage.stream().mapToInt(Integer::intValue).average().getAsDouble()
                + totalSelfEffectDamage.stream().mapToInt(Integer::intValue).average().getAsDouble();
        var osd = fed - fsd;
        var dpc = (osd / card.getStats().getCost().asMap().values().stream().mapToInt(Integer::intValue).sum());
        var dpl = osd / card.getStats().getLevel();
        var rpc = (600 / Math.max((cost - 60), 1));
        if (!print) {
            return new double[]{dpc, dpl};
        }

        System.out.println("----------------------------------------------------");
        System.out.println("Card: " + card.toString());
        System.out.println("Type: " + card.getStats().getAlignment());
        System.out.println("Level: " + card.getStats().getLevel());
        System.out.println("Cost: " + costMap);
        System.out.println("Total Cost: " + cost );
        System.out.println("Card Enemy Damage: " +  (card.getStats().getDamage() != null ? card.getStats().getDamage().asMap() : "null"));
        System.out.println("Enemy Damage Map: " + enemyDamageMap);
        System.out.println("Enemy Effect Damage Map: " + enemyEffectDamageMap);
        System.out.println("Enemy Effect Damage: " + totalEnemyEffectDamage.stream().mapToInt(Integer::intValue).average().getAsDouble());
        System.out.println("Target Effects: " + Arrays.toString(card.getStats().getTargetEffects()));
        System.out.println("Card Self Damage: " + (card.getStats().getSelfDamage() != null ? card.getStats().getSelfDamage().asMap() : "null"));
        System.out.println("Self Damage Map: " + selfDamageMap);
        System.out.println("Self Effect Damage Map: " + selfEffectDamageMap);
        System.out.println("Self Effect Damage: " + totalSelfEffectDamage.stream().mapToInt(Integer::intValue).average().getAsDouble());
        System.out.println("Self Neg Effects: " + Arrays.toString(card.getStats().getNegSelfEffects()));
        System.out.println("Self Pos Effects: " + Arrays.toString(card.getStats().getPosSelfEffects()));
        System.out.println("Full Enemy Damage: " + fed);
        System.out.println("Full Self Damage: " + fsd);
        System.out.println("Offset Dealt Damage: " + osd);
        System.out.println("Damage Per Cost: " + dpc);
        System.out.println("Damage Per Level: " + dpl);
        System.out.println("Rounds For Cost:" + rpc);
        System.out.println("viability: " + (rpc / osd) * 1000);


        return new double[]{dpc, dpl};
    }

}
