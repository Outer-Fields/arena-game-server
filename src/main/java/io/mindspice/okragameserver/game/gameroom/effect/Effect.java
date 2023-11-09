package io.mindspice.okragameserver.game.gameroom.effect;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.ActionClass;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;


import java.util.Arrays;

import static io.mindspice.okragameserver.game.enums.ActionClass.MULTI;

public class Effect implements Cloneable{
    public final EffectType type;
    public final ActionClass actionClass;
    public double amount;
    public double rollOffChance;
    public int rollOffRounds;
    public final boolean isRollOffChance;
    public double chance = 1; //Set to 1.2 to allow up to .85 luck modifier to still allow a 100% chance
    public double scalar = 1;
    public double altChance = -1;
    public double altScalar = -1;
    public final boolean isCurable;

    @JsonIgnore
    public JsonNode getAsJsonNode() {
        return new JsonUtils.ObjectBuilder()
                .put("type", type)
                .put("actionClass", actionClass)
                .put("amount", amount)
                .put(isRollOffChance ? "rollOffChance" : "rollOffRounds", isRollOffChance ? rollOffChance : rollOffRounds)
                .put("chance", chance)
                .put("scalar", scalar)
                .put("altChance", altChance)
                .put("altScalar", altScalar)
                .buildNode();
    }



    public Effect(ActionClass actionClass, EffectType type, boolean isCurable, double amount, double rollOff, boolean isRollOffChance) {
        this.actionClass = actionClass;
        this.type = type;
        this.amount = amount;
        this.isRollOffChance = isRollOffChance;
        this.isCurable = isCurable;
        if(isRollOffChance) {
            rollOffChance = rollOff;
        } else {
            rollOffRounds =  (int) rollOff;
        }
        validate();
    }

    public Effect(ActionClass actionClass, EffectType type, boolean isCurable, double amount, double rollOff, boolean isRollOffChance,
                  double chance, double scalar) {
        this.actionClass = actionClass;
        this.type = type;
        this.amount = amount;
        this.chance = chance;
        this.scalar = scalar;
        this.isRollOffChance = isRollOffChance;
        this.isCurable = isCurable;
        if(isRollOffChance) {
            rollOffChance = rollOff;
        } else {
            rollOffRounds =  (int) rollOff;
        }
        validate();
    }

    public Effect(ActionClass actionClass, EffectType type, boolean isCurable, double amount, double rollOff, boolean isRollOffChance,
                  double chance, double scalar, double altChance, double altScalar) {
        this.actionClass = actionClass;
        this.type = type;
        this.amount = amount;
        this.chance = chance;
        this.scalar = scalar;
        this.altChance = altChance;
        this.altScalar = altScalar;
        this.isRollOffChance = isRollOffChance;
        this.isCurable = isCurable;
        if(isRollOffChance) {
            rollOffChance = rollOff;
        } else {
            rollOffRounds =  (int) rollOff;
        }
        validate();
    }

    public double getAmount() {
        return amount;
    }

    private void validate() {
        if (chance > 1.3 || chance < 0) {
            System.out.println("Possible bad chance input, printing stack trace");
            System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
        } else if (scalar > 1.1 || scalar < 0) {
            System.out.println("Possible bad scalar input, printing stack trace");
            System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
        } else if (actionClass == MULTI) {
            if (altChance > 1.3 || altScalar > 1.1) {
                System.out.println("Possible bad scalar input, printing stack trace");
                System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
            }
        }

        if (actionClass == MULTI && (altChance < 0 || altScalar < 0)) {
            throw new IllegalStateException("No alt chance and scalar set for MULTI");
        }
    }
    public ActiveEffect getActiveEffect(Pawn pawn) {
        return new ActiveEffect(this, pawn);
    }
    public Effect clone() {
        try {
            return (Effect) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public String toString() {
        return "Effect{" +
                "pawn=" + type +
                ", actionClass=" + actionClass +
                ", amount=" + amount +
                ", rollOffChance=" + rollOffChance +
                ", rollOffRounds=" + rollOffRounds +
                ", isRollOffChance=" + isRollOffChance +
                ", chance=" + chance +
                ", scalar=" + scalar +
                ", altChance=" + altChance +
                ", altScalar=" + altScalar +
                '}';
    }

    public ObjectNode toJson() {
        var obj = JsonUtils.getMapper().createObjectNode();
        obj.put("effect_type", type.toString());
        obj.put("effect_class", actionClass.toString());
        obj.put("amount", amount);
        obj.put("roll_off_chance", rollOffChance);
        obj.put("roll_off_rounds", rollOffRounds);
        obj.put("is_roll_off_chance", isRollOffChance);
        obj.put("chance", chance);
        obj.put("scalar", scalar);
        obj.put("alt_chance", altChance);
        obj.put("alt_scalar", altScalar);
        return obj;
    }


}
