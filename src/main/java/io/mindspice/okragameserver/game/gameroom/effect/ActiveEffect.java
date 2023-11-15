package io.mindspice.okragameserver.game.gameroom.effect;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.core.Settings;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.gameutil.DamageModifier;
import io.mindspice.okragameserver.game.gameroom.gameutil.LuckModifier;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.util.gamelogger.EffectRecord;

import java.util.UUID;

import static io.mindspice.okragameserver.game.enums.StatType.*;


public class ActiveEffect {

    protected final UUID id = UUID.randomUUID();
    protected final Pawn affectedPawn;
    protected final Effect effect;
    protected final EffectType effectType;
    protected final boolean isCurable;
    protected volatile double amount;
    protected volatile double rollOffChance;
    protected volatile int rollOffRounds;
    private volatile int expectedRollOffRound;
    private volatile boolean haveDoneFirst = false;

    public ActiveEffect(Effect effect, Pawn affectedPawn) {
        this.effect = effect;
        this.effectType = effect.type;
        this.affectedPawn = affectedPawn;
        this.isCurable = effect.isCurable;
        if (effect.isRollOffChance) {
            rollOffChance = effect.rollOffChance;
            expectedRollOffRound = (int) Math.round(1 / effect.rollOffChance);
        } else {
            rollOffRounds = effect.rollOffRounds;
            expectedRollOffRound = rollOffRounds;
        }
        this.amount = effect.amount;
        if (effectType.effectClass == EffectType.EffectClass.MODIFIER) {
            startStat();
        }
    }

    public String getJsonLog() {
        try {
            var chance = effect.isRollOffChance;
            return new JsonUtils.ObjectBuilder()
                    .put("Type", effectType)
                    .put("amount", amount)
                    .put(chance ? "rollOffChance" : "rollOffRound", chance ? rollOffChance : rollOffRounds)
                    .buildString();
        } catch (JsonProcessingException e) {
            return "LOG ERROR!" + e.getMessage();
        }
    }

    public EffectRecord getEffectRecord() {
        return new EffectRecord(
                id,
                effectType,
                amount,
                rollOffRounds,
                rollOffChance,
                expectedRollOffRound
        );
    }

    public boolean update() {
        if (effect.isRollOffChance) {
            if (expectedRollOffRound > 0) { --expectedRollOffRound; }

            if (!haveDoneFirst) { // Needed to keep from rolling off before it hits at least once.
                return true;
            }

            if (LuckModifier.chanceCalc(rollOffChance, affectedPawn.getStat(LP))) {
                endEffect();
                return false;
            }
            if (amount <= 0) {
                endEffect();
                return false;
            }
        } else {
            if (rollOffRounds-- <= 0) {

                endEffect();
                return false;
            }
        }
        return true;
    }

    // returns if pawn should be active/confused/paralyzed
    public  boolean doEffect() {
        haveDoneFirst = true;
        switch (effectType) {
            case POISON -> {
                int poisonDmgMod = DamageModifier.poisonDamage(affectedPawn, (int) amount);
                affectedPawn.updateStat(StatType.HP, poisonDmgMod, false);
                return true;
            }
            case CONFUSION, PARALYSIS -> {
                return LuckModifier.inverseChanceCalc(amount, affectedPawn.getStat(LP));
            }
            case SLEEP -> {
                return true;
            }
        }

//        if (effectType.effectClass == INSIGHT)  sendInsight(); FIXME
        return true;
    }

    public void endEffect() {
        if (effectType.effectClass == EffectType.EffectClass.MODIFIER) {
            endStat();
        }
    }

    public UUID getId() {
        return id;
    }

    public boolean isCurable() {
        return isCurable;
    }

    public void cancel() {
        rollOffRounds = 0;
    }

    public EffectType getType() {
        return effectType;
    }

    public int getRollOffRounds() {
        return rollOffRounds;
    }

    public int getExpectedRollOffRound() {
        return expectedRollOffRound;
    }

    public double getRollOffChance() {
        return rollOffChance;
    }

    public boolean isRollOffChance() {
        return effect.isRollOffChance;
    }

    public void subtractAmount(double amount) {
        System.out.println("Modified Effect AMount");
        if (effectType.effectClass == EffectType.EffectClass.MODIFIER) {
            endStat();
            this.amount -= amount;
            if (this.amount < 0) {
                this.amount = 0;
            }
            startStat();
        } else {
            this.amount -= amount;
            if (this.amount < 0) {
                this.amount = 0;
            }
        }
    }

    // Increase state if players own pawn, decrease if not
    public void startStat() {
        if (effectType.isNegative) {
            amount = DamageModifier.defendDamageStat(affectedPawn, (int) amount);
            affectedPawn.updateStatMax(effectType.statType, (int) amount, false);
            affectedPawn.updateStat(effectType.statType, (int) amount, false);
        } else {
            amount = DamageModifier.posStatScale(affectedPawn, (int) amount);
            affectedPawn.updateStatMax(effectType.statType, (int) amount, true);
            affectedPawn.updateStat(effectType.statType, (int) amount, true);
        }
    }

    // Decrease to roll off if players own pawn, Increase to roll off if not
    private void endStat() {
        if (effectType.isNegative) {
            affectedPawn.updateStat(effectType.statType, (int) amount, true);
            affectedPawn.updateStatMax(effectType.statType, (int) amount, true);
        } else {
            if (effectType.statType != HP) {
                affectedPawn.updateStat(effectType.statType, (int) amount, false);
            }
            affectedPawn.updateStatMax(effectType.statType, (int) amount, false);
        }
    }

    public int getAmount() {
        return (int) amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    /* FOR TESTING */

    public EffectType getEffectType() {
        return effectType;
    }

    public Effect getEffect() {
        return effect;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActiveEffect{");
        sb.append("id='").append(id).append('\'');
        sb.append(", affectedPawn=").append(affectedPawn.getIndex());
        sb.append(", effect=").append(effect);
        sb.append(", effectType=").append(effectType);
        sb.append(", isCurable=").append(isCurable);
        sb.append(", amount=").append(amount);
        sb.append(", rollOffChance=").append(rollOffChance);
        sb.append(", rollOffRounds=").append(rollOffRounds);
        sb.append('}');
        return sb.toString();
    }
}
