package io.mindspice.okragameserver.game.gameroom.gameutil;

import io.mindspice.mindlib.data.tuples.Triple;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.effect.ActiveEffect;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;

import static io.mindspice.okragameserver.game.enums.GameConst.*;


public class Utils {

    public static void scaleDamageMap(Map<StatType, Integer> damageMap, double scalar) {
        damageMap.forEach((key, value) -> {
            damageMap.put(key, (int) (value * scalar));
        });
    }

    public static Map<StatType, Integer> joinStatMap(Map<StatType, Integer> map1, Map<StatType, Integer> map2) {
        Map<StatType, Integer> rtnMap = new EnumMap<>(StatType.class);
        for (var stat : StatType.values()) {
            Integer val1 = map1.getOrDefault(stat, 0);
            Integer val2 = map2.getOrDefault(stat, 0);
            rtnMap.put(stat, val1 + val2);
        }
        return rtnMap;
    }

    public static Map<StatType, Integer> relativeStateScale(Map<StatType, Integer> map) {
        Map<StatType, Integer> rtnMap = new EnumMap<>(StatType.class);
        for (var entry : map.entrySet()) {
            if (entry.getKey() == StatType.MP || entry.getKey() == StatType.DP || entry.getKey() == StatType.SP) {
                rtnMap.put(entry.getKey(), entry.getValue() * RELATIVE_STAT_SCALE_ALT);
            } else if (entry.getKey() == StatType.LP) {
                rtnMap.put(entry.getKey(), entry.getValue() * RELATIVE_STAT_SCALE_LP);
            } else if (entry.getKey() == StatType.WP) {
                rtnMap.put(entry.getKey(), entry.getValue() * RELATIVE_STAT_SCALE_WP);
            }
        }
        return rtnMap;
    }

    public static EnumMap<StatType, Integer> offSetDamage(
            EnumMap<StatType, Integer> enemyDamage, EnumMap<StatType, Integer> selfDamage) {
        for (var entry : selfDamage.entrySet()) {
            if (enemyDamage.containsKey(entry.getKey())) {
                enemyDamage.put(entry.getKey(), enemyDamage.get(entry.getKey()) - entry.getValue());
            } else {
                enemyDamage.put(entry.getKey(), -entry.getValue());
            }
        }
        return enemyDamage;
    }

    public static Collector<ActiveEffect, Triple<Integer, Integer, Boolean>, Triple<Integer, Integer, Boolean>>
            EffectStatCollector = Collector.of(
            () -> new Triple<>(0, 0, true),
            (triple, effect) -> new Triple<>(
                    triple.first() + effect.getAmount(),
                    triple.second() + (effect.isRollOffChance() ? effect.getRollOffRounds() : effect.getExpectedRollOffRound()),
                    triple.third() && effect.isCurable() //
            ),
            (t1, t2) -> new Triple<>(
                    t1.first() + t2.first(), t1.second() + t2.second(), t1.third() && t2.third()
            ), Function.identity()
    );
}