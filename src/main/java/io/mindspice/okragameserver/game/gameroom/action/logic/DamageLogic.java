package io.mindspice.okragameserver.game.gameroom.action.logic;

import io.mindspice.okragameserver.game.enums.*;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;
import io.mindspice.okragameserver.game.gameroom.gameutil.DamageModifier;
import io.mindspice.okragameserver.game.gameroom.gameutil.Utils;

import java.util.Map;


public class DamageLogic {

    private DamageLogic() { }

    public static class Basic implements IDamage {

        public static final Basic GET = new Basic();

        private Basic() { }

        @Override
        public void doDamage(PawnInterimState player, PawnInterimState target, ActionType actionType,
                SpecialAction special, Map<StatType, Integer> damage, boolean isSelf) {

            Pawn playerPawn = player.getPawn();
            Pawn targetPawn = target.getPawn();

            var enemyPowerMod = targetPawn.getPowerAction(false, actionType);
            //If enemy resists exit, and ~null damage~ skip damage
            if (enemyPowerMod.containsKey(PowerEnums.PowerReturn.RESIST)) {
                target.addFlag(ActionFlag.RESISTED);
                //target.nullDamage();
                return;
            }
            var playerPowerMod = playerPawn.getPowerAction(true, actionType);
            //Get and calculate power buff if exists
            if (playerPowerMod.containsKey(PowerEnums.PowerReturn.BUFF)) {
                Utils.scaleDamageMap(damage, playerPowerMod.get(PowerEnums.PowerReturn.BUFF)); //  Mutates the damage map in place
            }

            // Calculated for later use to avoid redundant checks
            boolean isIgnoreDefense = (actionType != ActionType.MAGIC && special == SpecialAction.IGNORE_DP)
                    || (actionType == ActionType.MAGIC && special == SpecialAction.IGNORE_MP);

            // If enemy reflects, assign damage to player if not resisted, if resisted return, else continue defense calc
            // First if check is to make sure self damage isn't reflected
            if ((!isSelf) && enemyPowerMod.containsKey(PowerEnums.PowerReturn.REFLECT)) {
                target.addFlag(ActionFlag.REFLECTED);
                var playerPowerDefense = playerPawn.getPowerAction(false, actionType);

                if (playerPowerDefense.containsKey(PowerEnums.PowerReturn.RESIST)) { // Resist reflect damage?
                    player.addFlag(ActionFlag.RESISTED);
                    return;
                }

                if (enemyPowerMod.containsKey(PowerEnums.PowerReturn.DOUBLE)) {
                    Utils.scaleDamageMap(damage, 2);
                    target.addFlag(ActionFlag._2X);
                }

                // scale if shield
                if (playerPowerDefense.containsKey(PowerEnums.PowerReturn.SHIELD)) {
                    Utils.scaleDamageMap(damage, playerPowerDefense.get(PowerEnums.PowerReturn.SHIELD));
                }
                // Defend if no ignore special
                if (!isIgnoreDefense) {
                    DamageModifier.defendDamage(playerPawn, actionType, damage);
                } else {
                    player.addFlag(ActionFlag.DEFENSE_FAILED);
                }

                player.addDamage(damage);
                player.addFlag(ActionFlag.DAMAGED);
                return;
            } else { // Calc enemy defenses and assign damage if not reflected.
                //calculate enemies defenses

                // double if player has successful double power
                if (playerPowerMod.containsKey(PowerEnums.PowerReturn.DOUBLE)) {
                    Utils.scaleDamageMap(damage, 2);
                    target.addFlag(ActionFlag._2X);
                }

                if (enemyPowerMod.containsKey(PowerEnums.PowerReturn.SHIELD)) {
                    Utils.scaleDamageMap(damage, enemyPowerMod.get(PowerEnums.PowerReturn.SHIELD));
                }

                // Defend if no ignore special
                if (!isIgnoreDefense) {
                    DamageModifier.defendDamage(targetPawn, actionType, damage);
                } else {
                    target.addFlag(ActionFlag.DEFENSE_FAILED);
                }

                target.addDamage(damage); //assign damage and return
                target.addFlag(ActionFlag.DAMAGED);
            }
        }
    }
}
