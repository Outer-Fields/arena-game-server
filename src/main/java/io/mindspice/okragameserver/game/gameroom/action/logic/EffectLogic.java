package io.mindspice.okragameserver.game.gameroom.action.logic;

import io.mindspice.okragameserver.game.enums.ActionFlag;
import io.mindspice.okragameserver.game.enums.PowerEnums;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.gameutil.CureLogic;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;
import io.mindspice.okragameserver.util.Log;

import static io.mindspice.okragameserver.game.enums.EffectType.CURE_ANY;
import static io.mindspice.okragameserver.game.enums.EffectType.HEAL;


public class EffectLogic {

    private EffectLogic() {
    }

    public static class Basic implements IEffect {
        public static final Basic GET = new Basic();

        private Basic() {
        }

        @Override
        public void doEffect(PawnInterimState playerInterimState, PawnInterimState targetInterimState, Effect effect,
                double scalar) {
            Pawn playerPawn = playerInterimState.getPawn();
            Pawn targetPawn = targetInterimState.getPawn();

            var enemyPowerDefense = targetPawn.getPowerAbilityDefense(effect.type);
            if (!effect.type.isPlayer && enemyPowerDefense.containsKey(PowerEnums.PowerReturn.RESIST)) {
                targetInterimState.addFlag(ActionFlag.RESISTED);
                return;
            }

            effect.amount = (int) (effect.amount * scalar);
            switch (effect.type.effectClass) {
                case MODIFIER -> {
                    if (effect.type.isPlayer) {
                        targetInterimState.addEffect(effect);
                        targetInterimState.addFlag(ActionFlag.BUFF);
                    } else if (enemyPowerDefense.containsKey(PowerEnums.PowerReturn.REFLECT)) {
                        targetInterimState.addFlag(ActionFlag.REFLECTED);
                        var playerPowerDefense = playerPawn.getPowerAbilityDefense(effect.type);
                        if (playerPowerDefense.containsKey(PowerEnums.PowerReturn.RESIST)) {
                            playerInterimState.addFlag(ActionFlag.RESISTED);
                            return;
                        }
                        playerInterimState.addEffect(effect);
                        playerInterimState.addFlag(ActionFlag.EFFECTED);
                    } else {
                        targetInterimState.addEffect(effect);
                        targetInterimState.addFlag(ActionFlag.EFFECTED);
                    }
                }
                //todo this should be better reflected in the interim (make interim handle pos effects  and apply them? pos damage map and neg?
                case SIPHON -> {
                    if (enemyPowerDefense.containsKey(PowerEnums.PowerReturn.REFLECT)) {
                        targetInterimState.addFlag(ActionFlag.REFLECTED);
                        var playerPowerDefense = playerPawn.getPowerAbilityDefense(effect.type);
                        if (playerPowerDefense.containsKey(PowerEnums.PowerReturn.RESIST)) {
                            playerInterimState.addFlag(ActionFlag.RESISTED);
                            return;
                        }
                        targetInterimState.addBuff(effect.type.statType, (int) effect.amount);
                        playerInterimState.addDamage(effect.type.statType, (int) effect.amount);
                        playerInterimState.addFlag(ActionFlag.EFFECTED);
                        targetInterimState.addFlag(ActionFlag.EFFECTED);
                    } else {
                        targetInterimState.addDamage(effect.type.statType, (int) effect.amount);
                        playerInterimState.addBuff(effect.type.statType, (int) effect.amount);
                        targetInterimState.addFlag(ActionFlag.EFFECTED);
                    }
                }
                case CURSE -> {
                    if (enemyPowerDefense.containsKey(PowerEnums.PowerReturn.REFLECT)) {
                        targetInterimState.addFlag(ActionFlag.REFLECTED);
                        var playerPowerDefense = playerPawn.getPowerAbilityDefense(effect.type);
                        if (playerPowerDefense.containsKey(PowerEnums.PowerReturn.RESIST)) {
                            playerInterimState.addFlag(ActionFlag.RESISTED);
                            return;
                        }
                        playerInterimState.addEffect(effect);
                        playerInterimState.addFlag(ActionFlag.CURSED);
                    } else {
                        targetInterimState.addEffect(effect);
                        targetInterimState.addFlag(ActionFlag.CURSED);
                    }
                }
                case CURE -> {
                    Log.SERVER.debug(this.getClass(), "Before Cure Effect Sum:" +
                            targetInterimState.getPawn().getStatusEffects().stream()
                                    .mapToDouble(e -> effect.amount).sum());

                    if (effect.type == HEAL) {
                        targetInterimState.addBuff(effect.type.statType, (int) effect.amount);
                    } else if (effect.type == CURE_ANY) {
                        CureLogic.cureAny(targetPawn, effect.amount);
                    } else {
                        CureLogic.cureEffect(targetPawn, effect.type, effect.amount);
                    }
                    targetInterimState.addFlag(ActionFlag.CURE);
                    Log.SERVER.debug(this.getClass(), "After Cure Effect Sum:" +
                            targetInterimState.getPawn().getStatusEffects().stream()
                                    .mapToDouble(e -> effect.amount).sum());
                }
            }
        }
    }
}

