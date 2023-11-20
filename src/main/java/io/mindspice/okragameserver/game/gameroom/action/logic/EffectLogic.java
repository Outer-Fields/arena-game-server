package io.mindspice.okragameserver.game.gameroom.action.logic;

import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.okragameserver.game.enums.ActionFlag;
import io.mindspice.okragameserver.game.enums.ActionType;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PowerEnums;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.gameutil.CureLogic;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;
import io.mindspice.okragameserver.util.Log;
import org.springframework.security.core.parameters.P;

import static io.mindspice.okragameserver.game.enums.EffectType.CURE_ANY;
import static io.mindspice.okragameserver.game.enums.EffectType.HEAL;


public class EffectLogic {

    private EffectLogic() {
    }

    public static class Basic implements IEffect {
        public static final Basic GET = new Basic();

        private Basic() {
        }

        // FIXME, this could all be cleaned up

        @Override
        public void doEffect(PawnInterimState playerInterimState, PawnInterimState targetInterimState, Effect effect,
                double scalar) {
            double scaledAmount = effect.amount * scalar;

            // Get player effects out of the way
            if (effect.type.isPlayer) {
                if (effect.type.effectClass == EffectType.EffectClass.MODIFIER) {
                    doPosModifier(targetInterimState, effect, scaledAmount);
                } else if (effect.type.effectClass == EffectType.EffectClass.CURE) {
                    doCure(targetInterimState, effect, scaledAmount);
                    return;
                } else {
                    Log.SERVER.error(this.getClass(), "Unhandled positive effect: " + effect);
                }
            }

            // Swap player <-> target if target reflects
            var targetPowerMods = targetInterimState.getPawn().getPowerAbilityDefense(effect.type);
            if (targetPowerMods.containsKey(PowerEnums.PowerReturn.REFLECT)) {
                var tempInterimVar = playerInterimState;
                playerInterimState = targetInterimState;
                targetInterimState = tempInterimVar;
            }

            switch (effect.type.effectClass) {
                case MODIFIER -> doNegModifier(playerInterimState, targetInterimState, effect, scaledAmount);
                case SIPHON -> doSiphon(playerInterimState, targetInterimState, effect, scaledAmount);
                case CURSE -> doCurse(playerInterimState, targetInterimState, effect, scaledAmount);
                case INSIGHT ->
                        Log.SERVER.debug(this.getClass(), "Insight encountered, not applied here, ensure it is applied elsewhere");
                case CURE ->
                        Log.SERVER.error(this.getClass(), "Cure branch reached when it should be, cure should have been handled prior");
            }
        }

        private void doSiphon(PawnInterimState playingPawn, PawnInterimState targetPawn,
                Effect effect, double scaledAmount) {
            var powerMod = calcNegPowerMods(playingPawn, targetPawn, effect, scaledAmount);
            if (!powerMod.first()) {
                return;
            } else {
                scaledAmount = powerMod.second();
            }
            targetPawn.addDamage(effect.type.statType, (int) Math.round(scaledAmount));
            targetPawn.addFlag(ActionFlag.DRAIN);
            playingPawn.addBuff(effect.type.statType, (int) Math.round(scaledAmount));
            playingPawn.addFlag(ActionFlag.SIPHON);
        }

        private void doCurse(PawnInterimState playingPawn, PawnInterimState targetPawn,
                Effect effect, double scaledAmount) {
            var powerMod = calcNegPowerMods(playingPawn, targetPawn, effect, scaledAmount);
            if (!powerMod.first()) {
                return;
            } else {
                scaledAmount = powerMod.second();
            }
            effect.amount = scaledAmount;
            targetPawn.addEffect(effect);
            targetPawn.addFlag(ActionFlag.CURSED);
        }

        private void doCure(PawnInterimState targetPawn, Effect effect, double scaledAmount) {
            scaledAmount = calcPosPowerMods(targetPawn, scaledAmount);
            if (effect.type == HEAL) {
                targetPawn.addBuff(effect.type.statType, (int) Math.round(scaledAmount));
                targetPawn.addFlag(ActionFlag.HEAL);
            } else if (effect.type == CURE_ANY) {
                CureLogic.cureAny(targetPawn.getPawn(), scaledAmount);
                targetPawn.addFlag(ActionFlag.CURE);
            } else {
                CureLogic.cureEffect(targetPawn.getPawn(), effect.type, scaledAmount);
                targetPawn.addFlag(ActionFlag.CURE);
            }
        }

        private void doNegModifier(PawnInterimState playingPawn, PawnInterimState targetPawn,
                Effect effect, double scaledAmount) {
            var powerMod = calcNegPowerMods(playingPawn, targetPawn, effect, scaledAmount);
            if (!powerMod.first()) {
                return;
            } else {
                scaledAmount = powerMod.second();
            }
            effect.amount = scaledAmount;
            targetPawn.addEffect(effect);
            targetPawn.addFlag(ActionFlag.DE_BUFF);
        }

        private void doPosModifier(PawnInterimState targetPawn, Effect effect, double scaledAmount) {
            scaledAmount = calcPosPowerMods(targetPawn, scaledAmount);
            effect.amount = scaledAmount;
            targetPawn.addEffect(effect);
            targetPawn.addFlag(ActionFlag.BUFF);
        }

        private double calcPosPowerMods(PawnInterimState playingPawn, double scaledAmount) {
            // We use offense here, but its just checking if player has a boost on magic, which is reflected on self
            var powerOffense = playingPawn.getPawn().getPowerAction(true, ActionType.MAGIC);
            if (powerOffense.containsKey(PowerEnums.PowerReturn.BUFF)) {
                scaledAmount *= powerOffense.get(PowerEnums.PowerReturn.BUFF);
            }
            if (powerOffense.containsKey(PowerEnums.PowerReturn.DOUBLE)) {
                scaledAmount *= 2;
                playingPawn.addFlag(ActionFlag._2X);
            }
            return scaledAmount;
        }

        private Pair<Boolean, Double> calcNegPowerMods(PawnInterimState playingPawn, PawnInterimState targetPawn,
                Effect effect, double scaledAmount) {
            var powerDefense = targetPawn.getPawn().getPowerAbilityDefense(effect.type);
            var powerOffense = playingPawn.getPawn().getPowerAction(true, ActionType.MAGIC);
            if (powerDefense.containsKey(PowerEnums.PowerReturn.RESIST)) {
                targetPawn.addFlag(ActionFlag.RESISTED);
                return new Pair<>(false, 0.0);
            }
            if (powerOffense.containsKey(PowerEnums.PowerReturn.BUFF)) {
                scaledAmount *= powerOffense.get(PowerEnums.PowerReturn.BUFF);
            }
            if (powerOffense.containsKey(PowerEnums.PowerReturn.DOUBLE)) {
                scaledAmount *= 2;
                targetPawn.addFlag(ActionFlag._2X);
            }
            if (powerDefense.containsKey(PowerEnums.PowerReturn.SHIELD)) {
                scaledAmount *= powerDefense.get(PowerEnums.PowerReturn.SHIELD);
                targetPawn.addFlag(ActionFlag.SHIELDED);
            }
            return new Pair<>(true, scaledAmount);
        }
    }
}

