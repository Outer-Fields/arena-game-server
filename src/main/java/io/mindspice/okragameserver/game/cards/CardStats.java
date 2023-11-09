package io.mindspice.okragameserver.game.cards;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.*;
import io.mindspice.okragameserver.game.gameroom.action.StatMap;
import io.mindspice.okragameserver.game.gameroom.action.logic.*;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import static io.mindspice.okragameserver.game.enums.ActionClass.MULTI;
import static io.mindspice.okragameserver.game.enums.ActionClass.SINGLE;
import static io.mindspice.okragameserver.game.gameroom.gameutil.Utils.*;


public class CardStats {
    private final CollectionSet collectionSet;
    private final boolean isPlayer;
    private final ActionClass damageClass;
    private final ActionType actionType;
    private final ActionClass selfDamageClass;
    private final int level;
    private final Alignment alignment;
    private final StatMap cost;
    private final StatMap damage;
    private final StatMap selfDamage;
    private final Effect[] targetEffects;
    private final Effect[] negSelfEffects;
    private final Effect[] posSelfEffects;
    private final Effect[] posSelfTargetEffects;
    private final SpecialAction special;
    private final Animation animation;
    private final String description;
    private final Cost costLogic;
    private final IDamageCalc damageCalc;
    private final IDamage damageLogic;
    private final IDamageCalc selfDamageCalc;
    private final IDamage selfDamageLogic;
    private final IEffectCalc targetEffectCalc;
    private final IEffect targetEffectLogic;
    private final IEffectCalc negSelfEffectCalc;
    private final IEffect negSelfEffectLogic;
    private final IEffectCalc posSelfEffectCalc;
    private final IEffect posSelfEffectLogic;
    private final IEffectCalc posSelfTargetEffectCalc;
    private final IEffect posSelfTargetEffectLogic;
    private final ActionClass targetEffectsClass;
    private final ActionClass posSelfEffectsClass;
    private final ActionClass negSelfEffectsClass;

    public String toJsonLog() {
        var node = JsonUtils.newEmptyNode()
                            .put("isPlayer", isPlayer)
                            .put("alignment", alignment.name())
                            .putPOJO("cost", cost.getAsJsonNode());

        if (damage != null) { node.putPOJO("damage", damage.getAsJsonNode()); }
        if (damageCalc != null) { node.put("damageMulti", damageCalc.getMulti()); }
        if (selfDamage != null) { node.putPOJO("selfDamage", selfDamage.getAsJsonNode()); }
        if (selfDamageCalc != null) { node.put("selfDamageMulti", selfDamageCalc.getMulti()); }
        if (targetEffects != null) {
            node.putPOJO("targetEffects", Arrays.stream(targetEffects).map(Effect::getAsJsonNode).toList());
        }
        if (negSelfEffects != null) {
            node.putPOJO("negSelfEffects", Arrays.stream(negSelfEffects).map(Effect::getAsJsonNode).toList());
        }
        if (posSelfEffects != null) {
            node.putPOJO("posSelfEffects", Arrays.stream(posSelfEffects).map(Effect::getAsJsonNode).toList());
        }
        if (special != null) { node.put("special", special.name()); }
        try {
            return JsonUtils.writeString(node);
        } catch (JsonProcessingException e) {
            return "LOG ERROR!" + e.getMessage();
        }
    }

    private CardStats(Builder b) {
        this.collectionSet = b.collectionSet;
        this.isPlayer = b.isPlayer;
        this.damageClass = b.damageClass;
        this.actionType = b.actionType;
        this.selfDamageClass = b.selfDamageClass;
        this.level = b.level;
        this.alignment = b.alignment;
        this.cost = b.cost;
        this.damage = b.damage;
        this.selfDamage = b.selfDamage;
        this.targetEffects = b.targetEffects;
        this.negSelfEffects = b.negSelfEffects;
        this.posSelfEffects = b.posSelfEffects;
        this.posSelfTargetEffects = b.posSelfTargetEffects;
        this.special = b.special;
        this.animation = b.animation;
        this.description = b.description;
        this.costLogic = b.costLogic;
        this.damageCalc = b.damageCalc;
        this.damageLogic = b.damageLogic;
        this.selfDamageCalc = b.selfDamageCalc;
        this.selfDamageLogic = b.selfDamageLogic;
        this.targetEffectCalc = b.targetEffectCalc;
        this.targetEffectLogic = b.targetEffectLogic;
        this.negSelfEffectCalc = b.negSelfEffectCalc;
        this.negSelfEffectLogic = b.negSelfEffectLogic;
        this.posSelfEffectCalc = b.posSelfEffectCalc;
        this.posSelfEffectLogic = b.posSelfEffectLogic;
        this.posSelfTargetEffectCalc = b.posSelfTargetEffectCalc;
        this.posSelfTargetEffectLogic = b.posSelfTargetEffectLogic;
        this.targetEffectsClass = b.targetEffectsClass;
        this.posSelfEffectsClass = b.posSelfEffectsClass;
        this.negSelfEffectsClass = b.negSelfEffectsClass;
    }

    public boolean hasDamage() {
        return damage != null;
    }

    public boolean hasSelfDamage() {
        return selfDamage != null;
    }

    public boolean hasTargetEffects() {
        return targetEffects != null;
    }

    public boolean hasNegSelfEffects() {
        return negSelfEffects != null;
    }

    public boolean hasPosSelfEffects() {
        return posSelfEffects != null;
    }

    public boolean hasPosSelfTargetEffects() {
        return posSelfTargetEffects != null;
    }

    public int getRelativeEnemyDamage() {
        Map<StatType, Integer> returnMap = new EnumMap<>(StatType.class);

        if (targetEffects != null) {
            for (var effect : targetEffects) {
                if (effect.type.effectClass == EffectType.EffectClass.MODIFIER && effect.type.isNegative) {
                    returnMap.put(effect.type.statType, getEffectTotal(effect));
                }
            }
        }
        if (damage != null) {
            if (returnMap.isEmpty()) {
                returnMap = damage.asMap();
            } else {
                joinStatMap(returnMap, damage.asMap());
            }
        }
        return relativeStateScale(returnMap).values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getRelativeSelfDamage() {
        Map<StatType, Integer> returnMap = new EnumMap<StatType, Integer>(StatType.class);

        if (negSelfEffects != null) {
            for (var effect : negSelfEffects) {
                if (effect.type.effectClass == EffectType.EffectClass.MODIFIER && effect.type.isNegative) {
                    returnMap.put(effect.type.statType, getEffectTotal(effect));
                }
            }
        }
        if (selfDamage != null) {
            if (returnMap.isEmpty()) {
                returnMap = selfDamage.asMap();
            } else {
                joinStatMap(returnMap, selfDamage.asMap());
            }
        }
        return relativeStateScale(returnMap).values().stream().mapToInt(Integer::intValue).sum();
    }

    public int getCureAmount() {
        if (posSelfEffects == null) { return 0; }
        return (int) Arrays.stream(posSelfEffects).filter(e -> e.type.effectClass == EffectType.EffectClass.CURE
                && e.type.statType == StatType.HP).mapToDouble(Effect::getAmount).sum();
    }

    public int getBuffSum() {
        if (posSelfEffects == null) { return 0; }
        return (int) Arrays.stream(posSelfEffects).filter(e -> e.type.effectClass == EffectType.EffectClass.MODIFIER)
                           .mapToDouble(Effect::getAmount).sum();
    }

    public int getOffsetDamage() {
        return getRelativeEnemyDamage() - getRelativeSelfDamage();
    }

    private int getEffectTotal(Effect effect) {
        var amount = effect.amount * effect.scalar * effect.chance;
        if (effect.actionClass == MULTI) {
            amount += effect.amount * effect.altScalar * effect.altChance * 2;
        }
        return (int) amount;
    }

    public int getHpDamage() {
        if (damage == null) { return 0; }
        return damage.asMap().get(StatType.HP);
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public CollectionSet getCollectionSet() {
        return collectionSet;
    }

    public ActionClass getDamageClass() {
        return damageClass;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public ActionClass getSelfDamageClass() {
        return selfDamageClass;
    }

    public int getLevel() {
        return level;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public StatMap getCost() {
        return cost;
    }

    public StatMap getDamage() {
        return Objects.requireNonNullElseGet(damage, () -> new StatMap(0, 0, 0, 0));
    }

    public StatMap getSelfDamage() {
        return Objects.requireNonNullElseGet(selfDamage, () -> new StatMap(0, 0, 0, 0));
    }

    public Effect[] getTargetEffects() {
        return Objects.requireNonNullElseGet(targetEffects, () -> new Effect[]{});
    }

    public Effect[] getNegSelfEffects() {
        return Objects.requireNonNullElseGet(negSelfEffects, () -> new Effect[]{});
    }

    public Effect[] getPosSelfEffects() {
        return Objects.requireNonNullElseGet(posSelfEffects, () -> new Effect[]{});
    }

    public Effect[] getPosSelfTargetEffects() {
        return posSelfTargetEffects;
    }

    public SpecialAction getSpecial() {
        return special;
    }

    public Animation getAnimation() {
        return animation;
    }

    public String getDescription() {
        return description;
    }

    public Cost getCostLogic() {
        return costLogic;
    }

    public IDamageCalc getDamageCalc() {
        return damageCalc;
    }

    public IDamage getDamageLogic() {
        return damageLogic;
    }

    public IDamageCalc getSelfDamageCalc() {
        return selfDamageCalc;
    }

    public IDamage getSelfDamageLogic() {
        return selfDamageLogic;
    }

    public IEffectCalc getTargetEffectCalc() {
        return targetEffectCalc;
    }

    public IEffect getTargetEffectLogic() {
        return targetEffectLogic;
    }

    public IEffectCalc getNegSelfEffectCalc() {
        return negSelfEffectCalc;
    }

    public IEffect getNegSelfEffectLogic() {
        return negSelfEffectLogic;
    }

    public IEffectCalc getPosSelfEffectCalc() {
        return posSelfEffectCalc;
    }

    public IEffect getPosSelfEffectLogic() {
        return posSelfEffectLogic;
    }

    public IEffectCalc getPosSelfTargetEffectCalc() {
        return posSelfTargetEffectCalc;
    }

    public IEffect getPosSelfTargetEffectLogic() {
        return posSelfTargetEffectLogic;
    }

    public ActionClass getTargetEffectsClass() {
        return targetEffectsClass;
    }

    public ActionClass getPosSelfEffectsClass() {
        return posSelfEffectsClass;
    }

    public ActionClass getNegSelfEffectsClass() {
        return negSelfEffectsClass;
    }

    public boolean isPlayerMulti() {
        return selfDamageClass == MULTI || posSelfEffectsClass == MULTI || negSelfEffectsClass == MULTI;
    }

    public boolean isTargetMulti() {
        return targetEffectsClass == MULTI || damageClass == MULTI;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CardStats{");
        sb.append("isPlayer=").append(isPlayer);
        sb.append(",\n damageClass=").append(damageClass);
        sb.append(",\n actionType=").append(actionType);
        sb.append(",\n selfDamageClass=").append(selfDamageClass);
        sb.append(",\n level=").append(level);
        sb.append(",\n alignment=").append(alignment);
        sb.append(",\n cost=").append(cost);
        sb.append(",\n damage=").append(damage);
        sb.append(",\n selfDamage=").append(selfDamage);
        sb.append(",\n targetEffects=").append(Arrays.toString(targetEffects));
        sb.append(",\n negSelfEffects=").append(Arrays.toString(negSelfEffects));
        sb.append(",\n posSelfEffects=").append(Arrays.toString(posSelfEffects));
        sb.append(",\n special=").append(special);
        sb.append(",\n animation=").append(animation);
        sb.append(",\n description='").append(description).append('\'');
        sb.append(",\n costLogic=").append(costLogic);
        sb.append(",\n damageCalc=").append(damageCalc);
        sb.append(",\n damageLogic=").append(damageLogic);
        sb.append(",\n selfDamageCalc=").append(selfDamageCalc);
        sb.append(",\n selfDamageLogic=").append(selfDamageLogic);
        sb.append(",\n targetEffectCalc=").append(targetEffectCalc);
        sb.append(",\n targetEffectLogic=").append(targetEffectLogic);
        sb.append(",\n negSelfEffectCalc=").append(negSelfEffectCalc);
        sb.append(",\n negSelfEffectLogic=").append(negSelfEffectLogic);
        sb.append(",\n posSelfEffectCalc=").append(posSelfEffectCalc);
        sb.append(",\n posSelfEffectLogic=").append(posSelfEffectLogic);
        sb.append(",\n targetEffectsClass=").append(targetEffectsClass);
        sb.append(",\n posSelfEffectsClass=").append(posSelfEffectsClass);
        sb.append(",\n negSelfEffectsClass=").append(negSelfEffectsClass);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private final CollectionSet collectionSet;
        private final ActionType actionType;
        private int level = -1;
        private final Alignment alignment;
        private boolean isPlayer = false;
        private StatMap cost;
        private ActionClass damageClass;
        private StatMap damage;
        private StatMap selfDamage;
        private ActionClass selfDamageClass;
        private SpecialAction special = null;
        private Animation animation;
        private Effect[] targetEffects;
        private Effect[] negSelfEffects;
        private Effect[] posSelfEffects;
        private Effect[] posSelfTargetEffects;
        private String description;
        private Cost costLogic;
        private IDamageCalc damageCalc;
        private IDamage damageLogic;
        private IDamageCalc selfDamageCalc;
        private IDamage selfDamageLogic;
        private IEffectCalc targetEffectCalc;
        private IEffect targetEffectLogic;
        private IEffectCalc negSelfEffectCalc;
        private IEffect negSelfEffectLogic;
        private IEffectCalc posSelfEffectCalc;
        private IEffectCalc posSelfTargetEffectCalc;
        private IEffect posSelfEffectLogic;
        private IEffect posSelfTargetEffectLogic;
        private ActionClass targetEffectsClass;
        private ActionClass posSelfEffectsClass;
        private ActionClass negSelfEffectsClass;
        private boolean skipValidation = false;

        public Builder(CollectionSet collectionSet, ActionType actionType, int level, Alignment alignment) {
            this.collectionSet = collectionSet;
            this.actionType = actionType;
            this.level = level;
            this.alignment = alignment;
        }

        public CardStats build() {
            if (validate()) {
                calcEffectsMulti();
                return new CardStats(this);
            } else {
                throw new RuntimeException("Error constructing CardStat object");
            }
        }

        public Builder isPlayer() {
            this.isPlayer = true;
            return this;
        }

        public Builder setCost(StatMap cost) {
            this.cost = cost;
            return this;
        }

        public Builder setDamage(ActionClass actionClass, StatMap damage) {
            this.damageClass = actionClass;
            this.damage = damage;
            return this;
        }

        public Builder setSelfDamage(ActionClass selfDamageClass, StatMap selfDamage) {
            this.selfDamageClass = selfDamageClass;
            this.selfDamage = selfDamage;
            return this;
        }

        public Builder setSpecial(SpecialAction special) {
            this.special = special;
            return this;
        }

        public Builder setAnimation(Animation animation) {
            this.animation = animation;
            return this;
        }

        public Builder setTargetEffects(Effect[] targetEffects) {
            this.targetEffects = targetEffects;
            return this;
        }

        public Builder setNegSelfEffects(Effect[] negSelfEffects) {
            this.negSelfEffects = negSelfEffects;
            return this;
        }

        public Builder setPosSelfEffects(Effect[] posSelfEffects) {
            this.posSelfEffects = posSelfEffects;
            return this;
        }

        public Builder setPosSelfTargetEffects(Effect[] posSelfTargetEffects) {
            this.posSelfTargetEffects = posSelfTargetEffects;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setCostLogic(Cost costLogic) {
            this.costLogic = costLogic;
            return this;
        }

        public Builder setDamageLogic(IDamageCalc damageCalc, IDamage damageLogic) {
            this.damageCalc = damageCalc;
            this.damageLogic = damageLogic;
            return this;
        }

        public Builder setSelfDamageLogic(IDamageCalc selfDamageCalc, IDamage selfIDamageLogic) {
            this.selfDamageCalc = selfDamageCalc;
            this.selfDamageLogic = selfIDamageLogic;
            return this;
        }

        public Builder setTargetEffectLogic(IEffectCalc effectCalc, IEffect effectLogic) {
            this.targetEffectCalc = effectCalc;
            this.targetEffectLogic = effectLogic;
            return this;
        }

        public Builder setNegSelfEffectLogic(IEffectCalc selfEffectCalc, IEffect selfEffectLogic) {
            this.negSelfEffectCalc = selfEffectCalc;
            this.negSelfEffectLogic = selfEffectLogic;
            return this;
        }

        public Builder setPosSelfEffectLogic(IEffectCalc selfEffectCalc, IEffect selfEffectLogic) {
            this.posSelfEffectCalc = selfEffectCalc;
            this.posSelfEffectLogic = selfEffectLogic;
            return this;
        }

        public Builder setPosSelfTargetEffectLogic(IEffectCalc selfTargetEffectCalc, IEffect selfTargetEffectLogic) {
            this.posSelfTargetEffectCalc = selfTargetEffectCalc;
            this.posSelfTargetEffectLogic = selfTargetEffectLogic;
            return this;
        }

        public CardStats.Builder skipValidation() {
            skipValidation = true;
            return this;
        }

        private boolean validate() {
            if (actionType == null
                    || (level < 1 || level > 4)
                    || alignment == null
                    || cost == null
                    || (damageClass == null && damage != null)
                    || animation == null
                    || costLogic == null
                    || description == null) {
                throw new IllegalStateException("Improperly built class - null where unexpected or improper level");
            }
            if (skipValidation) {
                return true;
            }
            if ((damage != null && damageCalc == null)
                    || (selfDamage != null && selfDamageCalc == null)
                    || (targetEffects != null && targetEffectCalc == null)
                    || (negSelfEffects != null && negSelfEffectCalc == null)
                    || (posSelfEffects != null && posSelfEffectCalc == null)
                    || (posSelfTargetEffects != null && posSelfTargetEffectCalc == null)) {
                throw new IllegalStateException("Logic missing for a stats entry.");
            }
            if ((damageCalc != null && damage == null)
                    || (selfDamageCalc != null && selfDamage == null)
                    || (targetEffectCalc != null && targetEffects == null)
                    || (negSelfEffectCalc != null && negSelfEffects == null)
                    || (posSelfEffectCalc != null && posSelfEffects == null)
                    || (posSelfTargetEffectCalc != null && posSelfTargetEffects == null)) {
                throw new IllegalStateException("Stats missing for a logic entry");
            }

            if (damage == null && targetEffects == null && posSelfEffects == null && posSelfTargetEffects == null) {
                throw new IllegalStateException("No damage, target, or self positive effects, must have at least one");
            }
            if (damage != null && damageClass == MULTI && (damage.altChance == -1.0 || damage.altScalar == -1.0)) {
                throw new IllegalStateException("Alt stats for multi damage not set");
            }
            if (selfDamage != null && damageClass == MULTI && (selfDamage.altChance == -1.0 || selfDamage.altScalar == -1.0)) {
                throw new IllegalStateException("Alt stats for multi self damage not set");
            }
            if (damageCalc != null && damageCalc.isSelf()) {
                throw new IllegalStateException("Self damage logic used for target damage.");
            }
            if (selfDamageCalc != null && !selfDamageCalc.isSelf()) {
                throw new IllegalStateException("Target damage logic used for self.");
            }
            if (targetEffectCalc != null && targetEffectCalc.isSelf()) {
                throw new IllegalStateException("Self effect logic used for target effects.");
            }
            if (negSelfEffectCalc != null && (!negSelfEffectCalc.isSelf() || negSelfEffectCalc.isPos())) {
                throw new IllegalStateException("Wrong effect calc for negative self effects");
            }
            if (posSelfEffectCalc != null && (!posSelfEffectCalc.isPos() || !posSelfEffectCalc.isSelf())) {
                throw new IllegalStateException("Wrong effect calc for positive self effects");
            }
            if (posSelfTargetEffectCalc != null && (!posSelfTargetEffectCalc.isPos() || !posSelfTargetEffectCalc.isSelf())) {
                throw new IllegalStateException("Wrong effect calc for positive self target effects");
            }
            if (negSelfEffectCalc != null && negSelfEffectCalc.isPos()) {
                throw new IllegalStateException("Positive effect calc used for negative effect");
            }
            if (isPlayer && ((posSelfEffectCalc == null || posSelfEffects == null)
                    && (posSelfTargetEffects == null || posSelfTargetEffectCalc == null))) {
                throw new IllegalStateException("Is player, but has no pos effect calc and/or effect");
            }

            if (damage != null) {
                if (damageClass == MULTI && (damage.altChance < 0 || damage.altChance > 1.3)) {
                    System.out.println("Possible bad damage input on alt chance, printing stack trace");
                    System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
                }
                if (damageClass == MULTI && (damage.altScalar < 0 || damage.altScalar > 1.1)) {
                    System.out.println("Possible bad damage input on alt scalar, printing stack trace");
                    System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
                }
            }
            if (selfDamage != null) {
                if (damageClass == MULTI && (selfDamage.altChance < 0 || selfDamage.altChance > 1.3)) {
                    System.out.println("Possible bad damage input on alt chance, printing stack trace");
                    System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
                }
                if (selfDamageClass == MULTI && (selfDamage.altScalar < 0 || selfDamage.altScalar > 1.1)) {
                    System.out.println("Possible bad damage input on alt scalar, printing stack trace");
                    System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
                }
            }

            // Effects self validate stats internally
            return true;
        }

        private void calcEffectsMulti() {
            if (targetEffects != null) {
                for (var effect : targetEffects) {
                    if (effect.actionClass == MULTI) {
                        targetEffectsClass = MULTI;
                        break;
                    }
                }
                if (targetEffectsClass == null) {
                    targetEffectsClass = SINGLE;
                }
            }
            if (posSelfEffects != null) {
                for (var effect : posSelfEffects) {
                    if (effect.actionClass == MULTI) {
                        posSelfEffectsClass = MULTI;
                        break;
                    }
                }
                if (posSelfEffectsClass == null) {
                    posSelfEffectsClass = SINGLE;
                }
            }
            if (negSelfEffects != null) {
                for (var effect : negSelfEffects) {
                    if (effect.actionClass == MULTI) {
                        negSelfEffectsClass = MULTI;
                        break;
                    }
                }
                if (negSelfEffectsClass == null) {
                    negSelfEffectsClass = SINGLE;
                }
            }
        }
    }
}
