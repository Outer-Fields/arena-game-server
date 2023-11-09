package io.mindspice.okragameserver.game.cards;

import io.mindspice.okragameserver.game.enums.ActionFlag;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.action.ActionReturn;
import io.mindspice.okragameserver.game.gameroom.gameutil.CureLogic;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;

import java.util.List;

public enum PotionCard implements Potion {
    NOT_IMPLEMENTED_FIX_ME(-1, List.of());




    // FIXME make more advanced, need status buffs/de-buf

    public final int level;
    public final List<PotionStats> stats;

    // Not in active use
    @Override
    public ActionReturn consumePotion(PlayerGameState player, PawnIndex targetIndex) {
//        var actionReturn = null //new ActionReturn(null);
//        actionReturn.playerPawnStates.add(new PawnInterimState(player.getPawn(targetIndex)));
//        if (!player.doPotion(this)) {
//            //TODO log this
//            actionReturn.targetPawnStates.get(0).addFlag(ActionFlag.UNSUCCESSFUL);
//            actionReturn.isInvalid = true;
//            return actionReturn;
//        }
//
//        for (var stat : stats) {
//            switch (stat.potionClass) {
//                case CURE_ANY -> CureLogic.cureAny(player.getPawn(targetIndex), stat.amount);
//                case CURE -> CureLogic.cureEffect(player.getPawn(targetIndex), stat.effectType, stat.amount);
//                case POS_EFFECT -> player.getPawn(targetIndex).addStatusEffect(stat.buffEffect);
//                case STAT_BUFF -> {
//                    player.getPawn(targetIndex).updateStatsMax(stat.statIncrease, true);
//                    player.getPawn(targetIndex).updateStats(stat.statIncrease, true);
//                }
//            }
//        }
//        actionReturn.playerPawnStates.get(0).addFlag(ActionFlag.EFFECTED);
//        return actionReturn;
        return null;
    }


    PotionCard(int level, List<PotionStats> stats) {
        this.level = level;
        this.stats = stats;
    }


    // Helper Methods for bot
    public boolean isPotionClass(PotionClass pClass) {
        for (var stat : stats) {
            if (stat.potionClass == pClass) return true;
        }
        return false;
    }

    public boolean hasStatBuff(StatType statType) {

        for (var stat : stats) {
            if (stat.statIncrease == null) continue;
            if (stat.statIncrease.get(statType) > 0) return true;
        }
        return false;
    }

    public Integer getStatAmount(StatType statType) {
        for (var stat : stats) {
            if (stat.statIncrease == null) continue;
            if (stat.statIncrease.get(statType) > 0) {
                return stat.statIncrease.get(statType);
            }
        }
        return 0;
    }

    public boolean hasCure(EffectType effect) {
        for (var stat : stats) {
            if (stat.effectType == null) continue;
            if (stat.potionClass == PotionClass.CURE && stat.effectType == effect) {
                return true;
            }
        }
        return false;
    }

    public double cureAmount(EffectType effect) {
        for (var stat : stats) {
            if (stat.effectType == null) continue;
            if(stat.potionClass == PotionClass.CURE && stat.effectType == effect) {
                return stat.amount;
            }
        }
        return 0;
    }

    public enum PotionClass {
        CURE_ANY,
        CURE,
        STAT_BUFF,
        POS_EFFECT
    }
}
