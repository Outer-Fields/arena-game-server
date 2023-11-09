package io.mindspice.okragameserver.game.bot.behavior.logic;

import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.bot.state.TreeFocusState;
import io.mindspice.okragameserver.game.enums.PawnIndex;


public abstract class Action {
    public abstract boolean doAction(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex);
}
