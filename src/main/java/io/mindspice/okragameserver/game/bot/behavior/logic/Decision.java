package io.mindspice.okragameserver.game.bot.behavior.logic;

import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.bot.state.TreeFocusState;
import io.mindspice.okragameserver.game.enums.PawnIndex;


public abstract class Decision {
    private final String name;

    public Decision(String name) {
        this.name = name;
    }

    public abstract boolean getDecision(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex);

    public String getName() {
        return name;
    }

}
