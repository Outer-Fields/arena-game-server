package io.mindspice.okragameserver.game.cards;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.CollectionSet;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.PowerEnums;
import io.mindspice.okragameserver.game.gameroom.action.ActionReturn;
import io.mindspice.okragameserver.game.gameroom.action.ActivePower;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;
import io.mindspice.okragameserver.util.CardUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public enum PowerCard implements Card {
    STONE_WALL(CollectionSet.ORIGINS, 2,
            new Power[]{
                    new Power(PowerEnums.PowerType.SHIELD_MELEE, 0.90, 1),
                    new Power(PowerEnums.PowerType.SHIELD_RANGED, 0.90, 1)
            },
            "Imparts light resistance to both melee and ranged attacks."
    ),
    IMPRENETRATION(CollectionSet.ORIGINS,2,
            new Power[]{
                    new Power(PowerEnums.PowerType.SHIELD_RANGED, 0.80, 1)
            },
            "Imparts moderate resistance to ranged attacks."
    ),
    BLADE_BANE(CollectionSet.ORIGINS,2,
            new Power[]{
                    new Power(PowerEnums.PowerType.SHIELD_MELEE, 0.80, 1)
            },
            "Imparts moderate resistance to melee attacks."
    ),
    CELESTIAL_SHIELD(CollectionSet.ORIGINS,2,
            new Power[]{
                    new Power(PowerEnums.PowerType.SHIELD_MELEE, 0.80, 1)
            },
            "Imparts moderate resistance to magic based damage attacks."
    ),
    WARRIORS_BLESSING(CollectionSet.ORIGINS,2,
            new Power[]{
                    new Power(PowerEnums.PowerType.SHIELD_MELEE, 0.93, 1),
                    new Power(PowerEnums.PowerType.SHIELD_MAGIC, 0.93, 1),
                    new Power(PowerEnums.PowerType.SHIELD_RANGED, 0.93, 1),
            },
            "Imparts very light resistance to all attacks."
    ),
    THE_UNSEEN(CollectionSet.ORIGINS,3,
            new Power[]{
                    new Power(PowerEnums.PowerType.RESIST_INSIGHT, 1, .7),
            },
            "Imparts a strong resistance to insight."
    ),
    THE_UNSEEN_GOLD(CollectionSet.ORIGINS,4,
            new Power[]{
                    new Power(PowerEnums.PowerType.RESIST_INSIGHT, 1, 1),
            },
            "Imparts near full resistance to insight."
    ),
    CURSE_BANE(CollectionSet.ORIGINS,2,
            new Power[]{
                    new Power(PowerEnums.PowerType.RESIST_PARALYSIS, 1, .2),
                    new Power(PowerEnums.PowerType.RESIST_SLEEP, 1, .2),
                    new Power(PowerEnums.PowerType.RESIST_DEBUFF, 1, .2),
                    new Power(PowerEnums.PowerType.RESIST_CONFUSION, 1, .2),
            },
            "Imparts light resistance to all curses and de buffs."
    ),
    CURSE_BANE_GOLD(CollectionSet.ORIGINS,4,
            new Power[]{
                    new Power(PowerEnums.PowerType.RESIST_PARALYSIS, 1, .3),
                    new Power(PowerEnums.PowerType.RESIST_SLEEP, 1, .3),
                    new Power(PowerEnums.PowerType.RESIST_DEBUFF, 1, .3),
                    new Power(PowerEnums.PowerType.RESIST_CONFUSION, 1, .3),
            },
            "Imparts moderate resistance to all curses and de buffs."
    ),
    IRON_JAW(CollectionSet.ORIGINS,3,
            new Power[]{
                    new Power(PowerEnums.PowerType.RESIST_PARALYSIS, 1, .35),
                    new Power(PowerEnums.PowerType.RESIST_CONFUSION, 1, .35),
                    new Power(PowerEnums.PowerType.RESIST_SLEEP, 1, .35),
            },
            "Imparts light resistance to paralysis, confusion and sleep."
    ),
    IRON_JAW_GOLD(CollectionSet.ORIGINS,4,
            new Power[]{
                    new Power(PowerEnums.PowerType.RESIST_PARALYSIS, 1, .5),
                    new Power(PowerEnums.PowerType.RESIST_CONFUSION, 1, .5),
                    new Power(PowerEnums.PowerType.RESIST_SLEEP, 1, .5),
            },
            "Imparts moderate resistance to paralysis, confusion and sleep."
    ),
    CRAFT_MASTER(CollectionSet.ORIGINS,1,
            new Power[]{
                    new Power(PowerEnums.PowerType.RESIST_DEBUFF, 1, .3),
            },
            "Imparts moderate resistance to de buffs."
    ),
    CRAFT_MASTER_GOLD(CollectionSet.ORIGINS,2,
            new Power[]{
                    new Power(PowerEnums.PowerType.RESIST_DEBUFF, 1, .45),
            },
            "Imparts moderate resistance to de buffs."
    ),
    DOUBLER(CollectionSet.ORIGINS,4,
            new Power[]{
                    new Power(PowerEnums.PowerType.DOUBLE, 1, .2),
            },
            "Imparts a small chance of doubling of attacks."
    ),
    MIRROR_SKIN(CollectionSet.ORIGINS,2,
            new Power[]{
                    new Power(PowerEnums.PowerType.REFLECTION, 1, .1),
            },
            "Imparts a small chance of reflecting attacks."
    ),
    MIRROR_SKIN_GOLD(CollectionSet.ORIGINS,4,
            new Power[]{
                    new Power(PowerEnums.PowerType.REFLECTION, 1, .2),
            },
            "Imparts a moderate chance of reflecting attacks."
    ),
    LUCKY_CLOVER(CollectionSet.ORIGINS,2,
            new Power[]{
                    new Power(PowerEnums.PowerType.INCREASE_LUCK, 2, 1)
            },
            "Imparts a noticeable increase in luck."
    ),
    LUCKY_CLOVER_GOLD(CollectionSet.ORIGINS,4,
            new Power[]{
                    new Power(PowerEnums.PowerType.INCREASE_LUCK, 4, 1)
            },
            "Imparts a large increase in luck."
    ),
    MARTIAL_MASTER(CollectionSet.ORIGINS,2,
            new Power[]{
                    new Power(PowerEnums.PowerType.BUFF_MELEE, 1.06, 1)
            },
            "Imparts a moderate buff to melee attack damage"
    ),
    MARTIAL_MASTER_GOLD(CollectionSet.ORIGINS,4,
            new Power[]{
                    new Power(PowerEnums.PowerType.BUFF_MELEE, 1.12, 1)
            },
            "Imparts a large buff to melee attack damage"
    ),
    ARCANE_MASTER(CollectionSet.ORIGINS,2,
            new Power[]{
                    new Power(PowerEnums.PowerType.BUFF_MAGIC, 1.06, 1)
            },
            "Imparts a moderate buff to magic based attack damage"
    ),
    ARCANE_MASTER_GOLD(CollectionSet.ORIGINS,4,
            new Power[]{
                    new Power(PowerEnums.PowerType.BUFF_MAGIC, 1.12, 1)
            },
            "Imparts a large buff to magic based attack damage"
    ),
    RANGED_MASTER(CollectionSet.ORIGINS,2,
            new Power[]{
                    new Power(PowerEnums.PowerType.INCREASE_LUCK, 1.06, 1)
            },
            "Imparts a moderate buff to ranged based attack damage"
    ),
    RANGED_MASTER_GOLD(CollectionSet.ORIGINS,4,
            new Power[]{
                    new Power(PowerEnums.PowerType.INCREASE_LUCK, 1.12, 1)
            },
            "Imparts a moderate large to ranged based attack damage"
    );
    public final CollectionSet collectionSet;
    public final int level;
    public final Power[] powers;
    public final String description;
    private String uid = null;
    public static final String prefix = "POW";

    PowerCard(CollectionSet collectionSet, int level, Power[] powers, String description) {
        this.collectionSet = collectionSet;
        this.powers = powers;
        this.level = level;
        this.description = description;
    }

    public List<ActivePower> getActivePowers() {
        List<ActivePower> activePowers = new ArrayList<>(powers.length);
        for (var power : powers) {
            activePowers.add(new ActivePower(this, power.power, power.chance, power.scalar));
        }
        return activePowers;
    }

    @Override
    public ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx) {
        return null;
    }

    @Override
    public CardStats getStats() {
        return null;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return this.name();
    }

    public static class Power {
        final PowerEnums.PowerType power;
        final double scalar;
        final double chance;

        public Power(PowerEnums.PowerType power, double scalar, double chance) {
            this.power = power;
            this.scalar = scalar;
            this.chance = chance;
        }

        public ObjectNode toJson() {
            var obj = JsonUtils.getMapper().createObjectNode();
            obj.put("power", power.toString());
            obj.put("scalar", scalar);
            return obj;
        }
    }

    public String toStringLog() {
        return "PowerCard{" +
                "card=" + this +
                ", level=" + level +
                ", powers=" + Arrays.toString(powers) +
                '}';
    }

    public String getUid() {
        if (uid == null) { this.uid = prefix + "-" + CardUtil.getHash(this.name()); }
        return uid;
    }

    public ObjectNode toJson() {
        var json = JsonUtils.getMapper();
        var obj = json.createObjectNode();
        obj.put("uid", getUid());
        obj.put("is_gold", this.toString().contains("GOLD"));
        obj.put("level", level);
        var pow = json.createObjectNode();
        for (int i = 0; i < powers.length; ++i) {
            pow.putIfAbsent(("power" + i), powers[i].toJson());
        }
        obj.putIfAbsent("powers", pow);
        obj.put("description", description);
        return obj;
    }

    public CollectionSet getCollectionSet() {
        return collectionSet;
    }
}


