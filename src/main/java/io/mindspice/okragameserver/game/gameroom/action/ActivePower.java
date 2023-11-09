package io.mindspice.okragameserver.game.gameroom.action;

import io.mindspice.okragameserver.game.cards.PowerCard;
import io.mindspice.okragameserver.game.enums.ActionType;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PowerEnums;
import io.mindspice.okragameserver.game.gameroom.gameutil.LuckModifier;
import io.mindspice.okragameserver.util.gamelogger.PowerRecord;

import java.util.EnumMap;

import static io.mindspice.okragameserver.game.enums.PowerEnums.PowerReturn.FALSE;


public class ActivePower {
    private final PowerEnums.PowerType type;
    private final PowerCard card;
    private final double chance;
    private final double scalar;

    public ActivePower(PowerCard card, PowerEnums.PowerType type, double chance, double scalar) {
        this.card = card;
        this.type = type;
        this.chance = chance;
        this.scalar = scalar;
    }

    public PowerRecord getPowerRecord() {
        return new PowerRecord(
                type,
                chance,
                scalar
        );
    }

    public EnumMap<PowerEnums.PowerReturn, Double> getActionDefense(ActionType actionType, int luck) {

        EnumMap<PowerEnums.PowerReturn, Double> returnMap = new EnumMap<>(PowerEnums.PowerReturn.class);
        var rtn = type.getDamageDefense(actionType);
        if (rtn == FALSE) {
            returnMap.put(rtn, 0.0);
            return returnMap;
        }

        switch (rtn) {
            case RESIST, REFLECT -> {
                if (LuckModifier.chanceCalc(chance, luck)) {
                    returnMap.put(rtn, 999.9);
                }
            }
            case SHIELD -> {
                double inverseScalar = 1 - scalar;
                returnMap.put(rtn, inverseScalar);
            }
        }
        return returnMap;
    }

    public EnumMap<PowerEnums.PowerReturn, Double> getActionOffense(ActionType actionType, int luck) {

        EnumMap<PowerEnums.PowerReturn, Double> returnMap = new EnumMap<>(PowerEnums.PowerReturn.class);

        var rtn = type.getOffense(actionType);
        if (rtn == FALSE) {
            //         returnMap.put(rtn, 0.0);
            return returnMap;
        }
        switch (rtn) {
            // TODO maybe use luck to scale buff?
            case BUFF -> returnMap.put(rtn, scalar);
            case DOUBLE -> {
                if (LuckModifier.chanceCalc(chance, luck)) {
                    returnMap.put(rtn, 999.9);
                }
            }
        }
        return returnMap;
    }

    public EnumMap<PowerEnums.PowerReturn, Double> getEffectDefense(EffectType effectType, int luck) {

        EnumMap<PowerEnums.PowerReturn, Double> returnMap = new EnumMap<>(PowerEnums.PowerReturn.class);
        var rtn = type.getEffectDefense(effectType);
        if (rtn == FALSE) {
            returnMap.put(rtn, 0.0);
            return returnMap;
        }

        switch (rtn) {
            case RESIST, REFLECT -> {
                if (LuckModifier.chanceCalc(chance, luck)) {
                    returnMap.put(rtn, 999.9);
                }
            }
        }
        return returnMap;
    }

    public double getLuckMod() {
        if (this.type != PowerEnums.PowerType.INCREASE_LUCK) return 0;
        return scalar;
    }

    public PowerEnums.PowerType getType() {
        return type;
    }

    public PowerCard getCard() {
        return card;
    }
}



