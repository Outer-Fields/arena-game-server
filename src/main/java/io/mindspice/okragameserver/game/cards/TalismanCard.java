package io.mindspice.okragameserver.game.cards;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.Alignment;
import io.mindspice.okragameserver.game.enums.CollectionSet;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.action.StatMap;
import io.mindspice.okragameserver.util.CardUtil;

import java.util.EnumMap;
import java.util.Map;

import static io.mindspice.okragameserver.game.enums.Alignment.*;
import static io.mindspice.okragameserver.game.enums.StatType.*;
import static io.mindspice.okragameserver.game.enums.StatType.LP;
import static java.util.Map.entry;


public enum TalismanCard {

    ///////////
    // ORDER //
    ///////////
    STABILITY_STONE(CollectionSet.ORIGINS, ORDER,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(DP, 100))),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(DP, 100))),
            "Imparts increased starting defense. Sets pawn alignment to order."),
    STABILITY_STONE_GOLD(CollectionSet.ORIGINS, ORDER,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(DP, 150))),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(DP, 75))),
            "Imparts increased defense, gold variant add more starting defense for less max defense boost. Sets pawn alignment to order."),
    PENDANT_OF_DEFENSE(CollectionSet.ORIGINS, ORDER,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(DP, 150),
                    entry(LP, -2))),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(DP, 200))),
            "Imparts a moderate increase in max defense, as well as a starting boost to defense, in exchange for less starting luck. Sets pawn alignment to order."),
    PENDANT_OF_DEFENSE_GOLD(CollectionSet.ORIGINS, ORDER,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(DP, 300),
                    entry(LP, -4))),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(DP, 200))),
            "Imparts a moderate increase in max defense, as well as a large starting boost to defense, in exchange for less starting luck. Sets pawn alignment to order."),
    JEWEL_OF_CONQUEST(CollectionSet.ORIGINS,ORDER,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(SP, 200),
                    entry(DP, -125))),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(SP, 100),
                    entry(DP, -75))),
            "Trades starting and max defense, for higher starting and max strength. Sets pawn alignment to order."),
    RELIC_OF_THE_ARCANE(CollectionSet.ORIGINS,ORDER,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(MP, 200),
                    entry(DP, -100))),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(MP, 100),
                    entry(DP, -50))),
            "Trades starting and max defense, for higher starting and max mana. Sets pawn alignment to order."),
    OKRITHRIAL_PENDANT_GOLD(CollectionSet.OKRA,ORDER,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(WP, 2)
            )),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, -400),
                    entry(SP, -150),
                    entry(MP, -150),
                    entry(DP, -100))),
            "A pendant forged from ancient okranite aligned to order, imparts increased willpower in exchange for maximum stats. Awarded to Okra Folk Holders"),
    ///////////
    // CHAOS //
    ///////////
    CHAOS_CRYSTAL(CollectionSet.ORIGINS,CHAOS,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(LP, 2))),
            /*Max*/
            new StatMap(0, 0, 0, 0, 0, 0).asMap(),
            "Slight increase to starting luck. Sets pawn alignment to chaos."),
    AMULET_OF_ARCANE_CHAOS(CollectionSet.ORIGINS,CHAOS,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(MP, 350),
                    entry(DP, -150),
                    entry(HP, -250),
                    entry(LP, 4))),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(MP, 175),
                    entry(HP, -125),
                    entry(DP, -75))),
            "Increases mana in exchange for less health and defense. Sets pawn alignment to chaos."),
    WARRIORS_ORNAMENT(CollectionSet.ORIGINS,CHAOS,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(SP, 350),
                    entry(HP, -150),
                    entry(DP, -250),
                    entry(LP, 4))),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(SP, 100),
                    entry(HP, -75),
                    entry(DP, -150))),
            "imparts a large increase strength in exchange for less health and defense Sets pawn alignment to chaos."),
    FRAGMENT_OF_THE_LUCKMEISTER(CollectionSet.ORIGINS,CHAOS,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(SP, -125),
                    entry(HP, -100),
                    entry(MP, -125),
                    entry(LP, 4))),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(SP, -75),
                    entry(HP, -75),
                    entry(DP, -74))),
            "Imparts a large increase in luck while lowering all other stats. Sets pawn alignment to chaos."),
    FRAGMENT_OF_THE_LUCKMEISTER_GOLD(CollectionSet.ORIGINS,CHAOS,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(SP, -250),
                    entry(HP, -200),
                    entry(MP, -250),
                    entry(LP, 6))),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(SP, -100),
                    entry(HP, -75),
                    entry(DP, -150))),
            "Imparts a huge increase in luck at a large detriment to other stats. Sets pawn alignment to chaos."),
    OKRITHRIAL_PENDANT(CollectionSet.OKRA,CHAOS,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(WP, 2)
            )),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, -400),
                    entry(SP, -150),
                    entry(MP, -150),
                    entry(DP, -100))),
            "A pendant forged from ancient okranite aligned to chaos, imparts increased willpower in exchange for maximum stats. Awarded to Okra Folk Holders."),
    /////////////
    // NEUTRAL //
    /////////////
    BALANCE_BEAD(CollectionSet.ORIGINS,NEUTRAL,
            /*Stats*/
            new EnumMap<StatType, Integer>(Map.ofEntries(
                    entry(HP, 250))),
            /*Max*/
            new EnumMap<StatType, Integer>(Map.ofEntries(

                    entry(HP, 100))),
            "Boost starting and max health. Set pawn alignment to neutral");

    public final CollectionSet collectionSet;
    public final Alignment alignment;
    public final Map<StatType, Integer> statChange;
    public final Map<StatType, Integer> maxChange;
    public final String description;
    private String uid = null;
    public static final String prefix = "TAL";

    TalismanCard(CollectionSet collectionSet, Alignment alignment, Map<StatType, Integer> statChange, Map<StatType,
            Integer> maxChange, String description) {
        this.collectionSet = collectionSet;
        this.alignment = alignment;
        this.statChange = statChange;
        this.maxChange = maxChange;
        this.description = description;
    }

    public ObjectNode toJson() {
        var json = JsonUtils.getMapper();
        var obj = json.createObjectNode();
        var stat = json.createObjectNode();
        obj.put("uid", getUid());
        obj.put("is_gold", this.toString().contains("GOLD"));
        obj.put("level", 2);
        stat.put("HP", statChange.get(HP) != null ? statChange.get(HP) : 0);
        stat.put("DP", statChange.get(DP) != null ? statChange.get(DP) : 0);
        stat.put("SP", statChange.get(SP) != null ? statChange.get(SP) : 0);
        stat.put("MP", statChange.get(MP) != null ? statChange.get(MP) : 0);
        stat.put("WP", statChange.get(WP) != null ? statChange.get(WP) : 0);
        stat.put("LP", statChange.get(LP) != null ? statChange.get(LP) : 0);
        obj.putIfAbsent("stats_change", stat);
        var statMax = json.createObjectNode();
        statMax.put("HP", maxChange.get(HP) != null ? maxChange.get(HP) : 0);
        statMax.put("DP", maxChange.get(DP) != null ? maxChange.get(DP) : 0);
        statMax.put("SP", maxChange.get(SP) != null ? maxChange.get(SP) : 0);
        statMax.put("MP", maxChange.get(MP) != null ? maxChange.get(MP) : 0);
        statMax.put("WP", maxChange.get(WP) != null ? maxChange.get(WP) : 0);
        statMax.put("LP", maxChange.get(LP) != null ? maxChange.get(LP) : 0);
        obj.putIfAbsent("max_change", statMax);
        obj.put("description", description);
        return obj;
    }

    // This method is implemented in the card interface,
    // Since talisman doesn't implement card it must be remembered to manually add

    public String getUid() {
        if (uid == null) { this.uid = prefix + "-" + CardUtil.getHash(this.name()); }
        return uid;
    }

    public CollectionSet getCollectionSet() {
        return collectionSet;
    }
}
