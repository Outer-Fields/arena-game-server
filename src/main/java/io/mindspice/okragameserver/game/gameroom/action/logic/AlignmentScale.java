package io.mindspice.okragameserver.game.gameroom.action.logic;

import io.mindspice.okragameserver.game.enums.Alignment;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;

public interface AlignmentScale {

    default double alignmentScaleEnemy(Pawn player, Pawn enemy, double scalar, Alignment cardAlignment) {
        var rtnScalar = scalar;
        if (player.getAlignment() == cardAlignment) {
            rtnScalar *= 1.05;
        }
        if (enemy.getAlignment() == Alignment.NEUTRAL) {
            return scalar;
        }
        if (enemy.getAlignment() != cardAlignment) {
            rtnScalar *= 1.05;
        } else {
            rtnScalar *= 0.95;
        }
        return rtnScalar;
    }

    default double alignmentScalePosSelf(Pawn player, Pawn target, double scalar, Alignment cardAlignment) {
        var rtnScalar = scalar;
        if (player.getAlignment() == cardAlignment) {
            rtnScalar *= 1.05;
        }
        if (target.getAlignment() == Alignment.NEUTRAL) {
            return scalar;
        }
        if (target.getAlignment() != cardAlignment) {
            rtnScalar *= 0.95;
        } else {
            rtnScalar *= 1.05;
        }
        return rtnScalar;
    }

    default double alignmentScaleNegSelf(Pawn player, Pawn target, double scalar, Alignment cardAlignment) {
        var rtnScalar = scalar;
        if (player.getAlignment() == cardAlignment) {
            rtnScalar *= 0.95;
        }
        if (target.getAlignment() == Alignment.NEUTRAL) {
            return scalar;
        }
        if (target.getAlignment() != cardAlignment) {
            rtnScalar *= 1.05;
        } else {
            rtnScalar *= 0.95;
        }
        return rtnScalar;
    }

    default double alignmentChance(Pawn player, Alignment cardAlignment, double chance) {
        if (player.getAlignment() == cardAlignment) {
            return chance * 1.05;
        } else {
            return chance;
        }
    }

    default double alignmentChanceNegSelf(Pawn player, Alignment cardAlignment, double chance) {
        if (player.getAlignment() == cardAlignment) {
            return chance * 0.95;
        } else {
            return chance;
        }
    }


}
