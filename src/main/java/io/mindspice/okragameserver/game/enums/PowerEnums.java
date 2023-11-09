package io.mindspice.okragameserver.game.enums;


import static io.mindspice.okragameserver.game.enums.PowerEnums.Defense.*;
import static io.mindspice.okragameserver.game.enums.PowerEnums.Offense.*;
import static io.mindspice.okragameserver.game.enums.PowerEnums.PowerReturn.*;


public class PowerEnums {

    public enum Offense {
        BUFF_ACTION,
        OFFENSE_CHANCE,
        LUCK
    }

    public enum Defense {
        RESIST_ACTION,
        RESIST_ABILITY,
        DEFENSE_CHANCE,
        SHIELD,
        LUCK
    }

    public enum PowerReturn {
        RESIST,
        SHIELD,
        BUFF,
        LUCK,
        REFLECT,
        DOUBLE,
        FALSE
    }

    /* This is a "Strong-Armed" implementation that uses strings as a workaround to have multiple types
     * The pawn field is used for enum comparisons of attack and effect types this simplifies logic on the caller
     * But at the expense of a more messy and error prone implementation of this class
     * FIXME COULD WORK AROUND THE STRING THING
     *  TODO MAKE LIST AND MANUALLY CHECK ALL ARE COVERED
     */
    public enum PowerType {
        /* DEFENSE */

        // ACTION
        SHIELD_MELEE(null, Defense.SHIELD, ActionType.MELEE.toString(), PowerReturn.SHIELD),
        SHIELD_MAGIC(null, Defense.SHIELD, ActionType.MAGIC.toString(), PowerReturn.SHIELD),
        SHIELD_RANGED(null, Defense.SHIELD, ActionType.RANGED.toString(), PowerReturn.SHIELD),

        // Ability
        RESIST_MAGIC(null, RESIST_ACTION, ActionType.MAGIC.toString(), RESIST),
        RESIST_POISON(null, RESIST_ABILITY, EffectType.POISON.toString(), RESIST),
        RESIST_PARALYSIS(null, RESIST_ABILITY, EffectType.PARALYSIS.toString(), RESIST),
        RESIST_SLEEP(null, RESIST_ABILITY, EffectType.SLEEP.toString(), RESIST),
        RESIST_DEBUFF(null, RESIST_ABILITY,"MULTI",RESIST), //Wonky, debuff resists several effects
        RESIST_CONFUSION(null, RESIST_ABILITY, EffectType.CONFUSION.toString(), RESIST),
        RESIST_INSIGHT(null, RESIST_ABILITY, "MULTI", RESIST),
        REFLECTION(null, DEFENSE_CHANCE,"REFLECTION", REFLECT),

        /* OFFENSE */
        BUFF_MELEE(BUFF_ACTION,null, ActionType.MELEE.toString(), BUFF),
        BUFF_MAGIC(BUFF_ACTION,null, ActionType.MAGIC.toString(), BUFF),
        BUFF_RANGED(BUFF_ACTION,null, ActionType.RANGED.toString(), BUFF),
        INCREASE_LUCK(Offense.LUCK,Defense.LUCK, "LUCK", PowerReturn.LUCK),
        DOUBLE(OFFENSE_CHANCE,null, "DOUBLE", PowerReturn.DOUBLE);

        public  Offense offense;
        public  Defense defense;
        private String type;
        public PowerReturn powerReturn;



        PowerType(Offense offenseEnum, Defense defenseEnum, String type, PowerReturn powerReturn){
            this.offense = offenseEnum;
            this.defense = defenseEnum;
            this.type = type;
            this.powerReturn = powerReturn;
        }

        public PowerReturn getDamageDefense(ActionType action) {
            if (defense == null) return PowerReturn.FALSE;
            String actionType = action.toString();

            if (defense == Defense.SHIELD && type.equals(actionType)) {
                return PowerReturn.SHIELD;
            }
            else if (defense == RESIST_ACTION && type.equals(actionType)){
                return PowerReturn.RESIST;
            }
            else if (this == REFLECTION) {

                if (action == ActionType.MAGIC) {
                    return PowerReturn.REFLECT;
                } else {
                    return FALSE;
                }
            } else {
                return PowerReturn.FALSE;
            }
        }

        public PowerReturn getEffectDefense(EffectType effect) {
            if (defense == null) return PowerReturn.FALSE;
            String effectType = effect.toString();

            if (defense == RESIST_ABILITY && type.equals(effectType)) {
                return PowerReturn.RESIST;
            }
            else if (this == REFLECTION) {
                return PowerReturn.REFLECT;
            }
            else if(this == RESIST_DEBUFF) {
                EffectType[] effectTypeTypes = {
                        EffectType.DE_INVIGORATE,
                        EffectType.DE_CORE,
                        EffectType.DE_MEDITATE,
                        EffectType.DE_FORTIFY,
                        EffectType.MISFORTUNE};

                for (EffectType e : effectTypeTypes) {
                    if (e.toString().equals(effectType)) {
                        return PowerReturn.RESIST;
                    }
                }
                return PowerReturn.FALSE;
            }

            else if (this == RESIST_INSIGHT) {
                EffectType[] insightType = {
                        EffectType.INSIGHT_STATUS,
                        EffectType.INSIGHT_HAND,
                        EffectType.INSIGHT_ALL_DECKS,
                        EffectType.INSIGHT_ABILITY_DECK,
                        EffectType.INSIGHT_ACTION_DECK
                };

                for (EffectType e : insightType) {
                    if (e.toString().equals(effectType)) {
                        return PowerReturn.RESIST;
                    }
                }
            } else {
                return PowerReturn.FALSE;
            }
            return PowerReturn.FALSE;
        }

        public PowerReturn getOffense(ActionType action) {
            if (offense == null) return PowerReturn.FALSE;
            String actionType = action.toString();

            if (offense == BUFF_ACTION && type.equals(actionType)) {
                return PowerReturn.BUFF;
            }
//            else if (offense == Offense.LUCK){
//                return PowerReturn.LUCK;
//            }
            else if (this == DOUBLE) {
                return PowerReturn.DOUBLE;
            } else {
                return PowerReturn.FALSE;
            }
        }
    }

}
