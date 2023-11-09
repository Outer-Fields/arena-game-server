package io.mindspice.okragameserver.game.gameroom.action.logic;

import io.mindspice.okragameserver.game.cards.Card;
import io.mindspice.okragameserver.game.enums.SpecialAction;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;

import java.util.List;

public interface IDamageCalc {
    void doDamage(IDamage dmgLogic, List<PawnInterimState> playerPawnStates, List<PawnInterimState> targetPawnStates, Card card, SpecialAction special);

    boolean isSelf();

    int getMulti();

    boolean isVital();
}

//    default  double alignmentSelfChance(Pawn player, Alignment cardAlignment, double chance) {
//        if (cardAlignment == null) return chance;
//        if (player.getAlignment() == cardAlignment) {
//            return chance * 0.95;
//        } else {
//            return chance;
//        }
//    }
//
//    default double alignmentChance(Pawn player, Alignment cardAlignment, double chance) {
//        if (cardAlignment == null) return chance;
//        if (player.getAlignment() == cardAlignment) {
//            return chance * 1.1;
//        } else {
//            return chance;
//        }
//    }
//
//
//
//    default double alignmentScale(Pawn player, Pawn enemy, double scalar, Alignment cardAlignment) {
//        var rtnScalar = scalar;
//        if (cardAlignment == null) return scalar;
//        if (cardAlignment == Alignment.NEUTRAL) return scalar;
//        if (player.getAlignment() == cardAlignment){
//            rtnScalar *= 1.0625;
//        }
//        if (player.getAlignment() != Alignment.NEUTRAL && player.getAlignment() == enemy.getAlignment()) {
//            rtnScalar *= 0.9;
//        }
//        if (enemy.getAlignment() != Alignment.NEUTRAL && enemy.getAlignment() != player.getAlignment()) {
//            rtnScalar *= 1.0625;
//        }
//        return rtnScalar;
//    }
//}
