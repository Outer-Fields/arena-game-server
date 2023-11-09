package io.mindspice.okragameserver.game.gameroom.action;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.StatType;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;


public class StatMap {
    private final int[] stats;
    public double chance = 1;
    public double scalar = 1;
    public double altChance = -1;
    public double altScalar = -1;
    private Map<StatType, Integer> statMap;

    public StatMap(int hp, int dp, int sp, int mp, double chance, double scalar) {
        stats = new int[]{hp, dp, sp, mp};
        this.chance = chance;
        this.scalar = scalar;
        // copy chance/scale onto alts to avoid bugs
        validate();
    }

    public StatMap(int hp, int dp, int sp, int mp, double chance, double scalar, double altChance, double altScalar) {
        stats = new int[]{hp, dp, sp, mp};
        this.chance = chance;
        this.scalar = scalar;
        this.altChance = altChance;
        this.altScalar = altScalar;
        validate();
    }

    public StatMap(int hp, int dp, int sp, int mp) {
        stats = new int[]{hp, dp, sp, mp};
    }

    private void validate() {
        if (chance > 1.3 || chance < 0) {
            System.out.println("Possible bad chance input, printing stack trace");
            System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
        } else if (scalar > 1.1 || scalar < 0) {
            System.out.println("Possible bad scalar input, printing stack trace");
            System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
        }
    }

    // Since the StatMap is used with card singletons, and is used for mutable operations, a new instance
    // needs to be return as to not mutate global card stats.
    public Map<StatType, Integer> asMap() {
        EnumMap<StatType, Integer> statMap = new EnumMap<>(StatType.class);
        statMap.put(StatType.HP, stats[0]);
        statMap.put(StatType.DP, stats[1]);
        statMap.put(StatType.SP, stats[2]);
        statMap.put(StatType.MP, stats[3]);
        statMap.put(StatType.WP, 0);
        statMap.put(StatType.LP, 0);
        return statMap;
    }

    @JsonIgnore
    public JsonNode getAsJsonNode() {
        return new JsonUtils.ObjectBuilder()
                .put(StatType.HP.name(), stats[0])
                .put(StatType.DP.name(), stats[1])
                .put(StatType.SP.name(), stats[2])
                .put(StatType.MP.name(), stats[3])
                .put(StatType.WP.name(), 0)
                .put(StatType.LP.name(), 0)
                .buildNode();
    }

    @Override
    public String toString() {
        return "StatMap{" +
                "stats=" + asMap().toString() +
                ", chance=" + chance +
                ", scalar=" + scalar +
                ", altChance=" + altChance +
                ", altScalar=" + altScalar +
                '}';
    }

    public int getSum() {
        int sum = 0;
        for (int stat : stats) { sum += stat; }
        return sum;
    }

    public ObjectNode toJson() {
        var json = JsonUtils.getMapper();
        var obj = json.createObjectNode();
        var stat = json.createObjectNode();
        stat.put("HP", stats[0]);
        stat.put("DP", stats[1]);
        stat.put("SP", stats[2]);
        stat.put("MP", stats[3]);
        obj.putIfAbsent("stats", stat);
        obj.put("chance", chance);
        obj.put("scalar", scalar);
        obj.put("alt_chance", altChance);
        obj.put("alt_scalar", altScalar);
        return obj;
    }
}
