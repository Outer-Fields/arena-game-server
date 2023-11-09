package io.mindspice.okragameserver.game.gameroom.state;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.ActionFlag;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;

import java.util.*;


public class PawnInterimState {
    private final PawnIndex pawnIndex;
    private final Pawn pawn;
    private Map<StatType, Integer> damageMap;
    private Map<StatType, Integer> buffMap;
    private List<Effect> effectList;
    private final List<ActionFlag> actionFlags = new ArrayList<>();

    public PawnInterimState(Pawn pawn) {
        this.pawnIndex = pawn.getIndex();
        this.pawn = pawn;
    }

    public void addDamage(StatType stateType, int amount) {
        if (damageMap == null) {
            damageMap = new EnumMap<>(StatType.class);
        }
        if (damageMap.containsKey(stateType)) {
            int currDmg = damageMap.get(stateType);
            damageMap.put(stateType, currDmg + amount);
        } else {
            damageMap.put(stateType, amount);
        }
    }

    public void addBuff(StatType stateType, int amount) {
        if (buffMap == null) {
            buffMap = new EnumMap<>(StatType.class);
        }
        if (buffMap.containsKey(stateType)) {
            int currDmg = buffMap.get(stateType);
            buffMap.put(stateType, currDmg + amount);
        } else {
            buffMap.put(stateType, amount);
        }
    }

    public void addDamage(Map<StatType, Integer> damageMap) {
        if (damageMap == null) {
            return;
        }
        if (this.damageMap == null) {
            this.damageMap = new EnumMap<>(StatType.class);
        }
        damageMap.forEach((key, value) -> {
            if (this.damageMap.containsKey(key)) {
                this.damageMap.put(key, this.damageMap.get(key) + value);
            } else {
                this.damageMap.put(key, value);
            }
        });
    }

    public void addBuff(EnumMap<StatType, Integer> buffMap) {
        if (buffMap == null) {
            return;
        }
        if (this.buffMap == null) {
            this.buffMap = new EnumMap<>(StatType.class);
        }
        buffMap.forEach((key, value) -> {
            if (this.buffMap.containsKey(key)) {
                this.buffMap.put(key, this.buffMap.get(key) + value);
            } else {
                this.buffMap.put(key, value);
            }
        });
    }

    public boolean hasDamage() { return damageMap != null; }

    public boolean hasEffects() { return effectList != null && !effectList.isEmpty(); }

    public void addEffect(Effect effect) {
        if (effectList == null) effectList = new ArrayList<>();
        if (effect == null) return;
        effectList.add(effect);
    }

    public void nullDamage() {
        damageMap = null;
        actionFlags.removeIf(p -> p == ActionFlag.DAMAGED);
    }

    public void nullEffects() {
        this.effectList = null;
        actionFlags.removeIf(p -> p == ActionFlag.EFFECTED);
    }

    public void addFlag(ActionFlag flag) {
        actionFlags.add(flag);
    }


    public List<ActionFlag> getActionFlags() {
        return actionFlags;
    }

    public void scaleDamage(double scalar) {
        if (damageMap == null) {
            return;
        }
        damageMap.forEach((key, value) -> {
            damageMap.put(key, (int) (value * (scalar)));
        });
    }

    public void doDamage() {
        if (damageMap != null) {
            pawn.updateStats(damageMap, false);
        }
        if (buffMap != null) {
            pawn.updateStats(buffMap, true);
        }

    }

    public void doEffect() {
        if (effectList == null) {
            return;
        }
        for (Effect e : effectList) {
            pawn.addStatusEffect(e);
        }
    }

    public Pawn getPawn() {
        return pawn;
    }

    public Map<StatType, Integer> getDamageMap() {
        return damageMap;
    }

    public List<Effect> getEffectList() {
        return effectList;
    }

    public PawnIndex getPawnIndex() {
        return pawnIndex;
    }

    public List<Effect> getEffects() {
        return effectList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PawnInterimState:\n");
        sb.append("  pawnIndex: ").append(pawnIndex);
        sb.append("\n");
        sb.append("  pawn: ").append(pawn);
        sb.append("\n");
        sb.append("  damageMap: ").append(damageMap);
        sb.append("\n");
        sb.append("  buffMap: ").append(buffMap);
        sb.append("\n");
        sb.append("  effectList: ").append(effectList);
        sb.append("\n");
        sb.append("  actionFlags: ").append(actionFlags);
        sb.append("\n");
        return sb.toString();
    }

    public ObjectNode getLogInfo() {
        var jNode = JsonUtils.getMapper().createObjectNode();
        jNode.putPOJO("pawn_index", pawnIndex);
        jNode.putPOJO("damage", damageMap != null ? damageMap : Map.of());
        jNode.putPOJO("buff", buffMap != null ? buffMap : Map.of());
        jNode.putPOJO("effects", effectList != null ? effectList : List.of());
        jNode.putPOJO("action_flags", actionFlags);
        return jNode;
    }
}