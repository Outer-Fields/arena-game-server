package io.mindspice.okragameserver.game.bot.behavior.core;

import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.bot.state.TreeFocusState;
import io.mindspice.okragameserver.game.enums.PawnIndex;

import static io.mindspice.okragameserver.game.bot.behavior.core.Node.Type.ROOT;


public class RootNode extends Node {

    public RootNode() {
        super(ROOT, "Root");
    }

    @Override
    public boolean travel(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
        for (Node child : adjacentNodes) {
            if (child.travel(botPlayerState, focusState, selfIndex)) { return true; }
        }
        //TODO logic for if all decisions fail
        return false;
    }
}
