package io.mindspice.okragameserver.game.gameroom.action.logic;

import io.mindspice.okragameserver.game.cards.Card;
import io.mindspice.okragameserver.game.enums.SpecialAction;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;

import java.util.List;

public interface IEffectCalc {
   void doEffect(IEffect effectLogic, List<PawnInterimState> playerStates, List<PawnInterimState> targetStates, Card card, SpecialAction special);

   boolean isSelf();
   boolean isPos();
   int getMulti();
}

