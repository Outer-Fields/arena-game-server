package io.mindspice.okragameserver.game.cards;

import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.gameroom.action.ActionReturn;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;

public interface Potion {
   ActionReturn consumePotion(PlayerGameState player, PawnIndex targetPawn);
}
