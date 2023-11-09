package io.mindspice.okragameserver.game.cards;

import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.action.StatMap;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;

import java.util.EnumMap;
import java.util.Map;


public class PotionStats {
    public PotionCard.PotionClass potionClass;
    public EffectType effectType;
    public Effect buffEffect;
    public double amount;
    public double rollOff;
    public boolean isRollOffChance;
    public EnumMap<StatType, Integer> statIncrease;
    public EnumMap<StatType, Integer> maxIncrease;


    public PotionStats(Builder b) {
        this.potionClass = b.potionClass;
        this.effectType = b.effectType;
        this.buffEffect = b.buffEffect;
        this.amount = b.amount;
        this.rollOff = b.rollOff;
        this.isRollOffChance = b.isRollOffChance;
    }

    public static class Builder {
        public PotionCard.PotionClass potionClass;
        public EffectType effectType;
        public Effect buffEffect;
        public double amount = -1;
        public double rollOff = -1;
        public boolean isRollOffChance;
        public Map<StatType, Integer> statIncrease;
        public Map<StatType, Integer>  maxIncrease;


        public Builder setClass(PotionCard.PotionClass potionClass) {
            this.potionClass = potionClass;
            return this;
        }

        public Builder setEffectType(EffectType effectType) {
            this.effectType = effectType;
            return this;
        }

        public Builder setBuffEffect(Effect buffEffect) {
            this.buffEffect = buffEffect;
            return this;
        }

        public Builder setAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder setRoundRollOff(int roundCount) {
            this.rollOff = roundCount;
            this.isRollOffChance = false;
            return this;
        }

        public Builder setRollOffChance(double chance) {
            this.rollOff = chance;
            this.isRollOffChance = true;
            return this;
        }

        public Builder setBuffStats(StatMap stats, StatMap statsMax) {
            this.statIncrease = stats.asMap();
            this.maxIncrease = statsMax.asMap();
            return this;
        }

        public PotionStats build() {
            if (validate()) {
                return new PotionStats(this);
            } else {
                throw new IllegalStateException("Improperly constructed PStat object");
            }
        }

        public boolean validate() {
            switch (potionClass) {

                case CURE_ANY -> {
                    if (amount < 0) return false;
                }
                case CURE -> {
                    if (effectType == null || amount < 0) return false;
                }
                case POS_EFFECT -> {
                    if (amount < 0 || rollOff < 0) return false;
                }
                case STAT_BUFF -> {
                    if (statIncrease == null) return false;
                }
            }
            return true;
        }
    }
}