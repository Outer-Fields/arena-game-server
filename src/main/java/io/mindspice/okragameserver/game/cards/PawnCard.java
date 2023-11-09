package io.mindspice.okragameserver.game.cards;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.ActionType;
import io.mindspice.okragameserver.game.enums.CollectionSet;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.gameutil.LuckModifier;
import io.mindspice.okragameserver.game.gameroom.gameutil.Regenerate;
import io.mindspice.okragameserver.util.CardUtil;

import java.util.EnumMap;
import java.util.Map;

import static io.mindspice.okragameserver.game.enums.StatType.*;
import static java.util.Map.entry;


public enum PawnCard {

    DARK_SENTINEL(CollectionSet.ORIGINS, ActionType.MELEE, 3,
            //Stats
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 3200),
                    entry(DP, 500),
                    entry(SP, 700),
                    entry(MP, 300),
                    entry(WP, 3),
                    entry(LP, 10))),
            //StatMax
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 3600),
                    entry(DP, 700),
                    entry(SP, 800),
                    entry(MP, 300),
                    entry(WP, 4),
                    entry(LP, 20))),
            //Regen
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 25),
                    entry(DP, 0),
                    entry(SP, 25),
                    entry(MP, 10))),
            "A strong dark sentinel, guardian of the imperial empire during dire times.",
            false),
    IMPERIAL_RANGER(CollectionSet.ORIGINS, ActionType.RANGED, 2,
            //Stats
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 2800),
                    entry(DP, 300),
                    entry(SP, 600),
                    entry(MP, 400),
                    entry(WP, 4),
                    entry(LP, 10))),

            //StatMax
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 3200),
                    entry(DP, 600),
                    entry(SP, 800),
                    entry(MP, 500),
                    entry(WP, 6),
                    entry(LP, 20))),
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 20),
                    entry(DP, 0),
                    entry(SP, 20),
                    entry(MP, 20))),
            "A basic ranger from the imperial school of archery.",
            false),
    IMPERIAL_MAGI(CollectionSet.ORIGINS, ActionType.MAGIC, 2,
            //Stats
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 2800),
                    entry(DP, 300),
                    entry(SP, 150),
                    entry(MP, 750),
                    entry(WP, 3),
                    entry(LP, 10))),

            //StatMax
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 3200),
                    entry(DP, 400),
                    entry(SP, 250),
                    entry(MP, 900),
                    entry(WP, 8),
                    entry(LP, 20))),
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 25),
                    entry(DP, 0),
                    entry(SP, 10),
                    entry(MP, 25))),
            "A basic mage from the imperial school of magic.",
            false),
    OKRA_WARRIOR(CollectionSet.OKRA, ActionType.MELEE, 4,
            //Stats
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 2800),
                    entry(DP, 400),
                    entry(SP, 600),
                    entry(MP, 400),
                    entry(WP, 4),
                    entry(LP, 12))),

            //StatMax
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 3000),
                    entry(DP, 400),
                    entry(SP, 800),
                    entry(MP, 600),
                    entry(WP, 8),
                    entry(LP, 20))),
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 20),
                    entry(DP, 0),
                    entry(SP, 15),
                    entry(MP, 10))),
            "A strange and elusive warrior from the okra tribe. Awarded to Okra Folk holders.",
            false);

    public final CollectionSet collectionSet;
    public final ActionType actionType;
    public final int level;
    public final EnumMap<StatType, Integer> stats;
    public final EnumMap<StatType, Integer> statsMax;
    public final EnumMap<StatType, Integer> regen;
    public final String description;
    public final boolean isGold;
    public static final String prefix = "PAW";
    private String uid = null;

    PawnCard(CollectionSet collectionSet, ActionType actionType, int level, EnumMap<StatType, Integer> stats,
            EnumMap<StatType, Integer> statsMax, EnumMap<StatType, Integer> regen, String description, boolean isGold) {
        this.collectionSet = collectionSet;
        this.actionType = actionType;
        this.level = level;
        this.stats = stats;
        this.statsMax = statsMax;
        this.regen = regen;
        this.description = description;
        this.isGold = isGold;
    }

    public ObjectNode toJson() {
        var json = JsonUtils.getMapper();
        var obj = json.createObjectNode();
        obj.put("uid", getUid());
        obj.put("action_type", actionType.toString());
        obj.put("level", level);
        obj.put("is_gold", isGold);
        var stat = json.createObjectNode();
        stat.put("HP", stats.get(HP));
        stat.put("DP", stats.get(DP));
        stat.put("SP", stats.get(SP));
        stat.put("MP", stats.get(MP));
        stat.put("WP", stats.get(WP));
        stat.put("LP", stats.get(LP));
        obj.putIfAbsent("stats", stat);
        var statMax = json.createObjectNode();
        statMax.put("HP", statsMax.get(HP));
        statMax.put("DP", statsMax.get(DP));
        statMax.put("SP", statsMax.get(SP));
        statMax.put("MP", statsMax.get(MP));
        statMax.put("WP", statsMax.get(WP));
        statMax.put("LP", statsMax.get(LP));
        obj.putIfAbsent("stats_max", statMax);
        obj.put("description", description);
        obj.putPOJO("regeneration", regen);
        return obj;
    }

    // This function is shared with the ownedCards that implement Card
    // we don't implement it for Pawn, so it must be remembered to be included
    public String getUid() {
        if (uid == null) { uid = prefix + "-" + CardUtil.getHash(name()); }
        return uid;
    }

    public CollectionSet getCollectionSet() {
        return collectionSet;
    }

}
