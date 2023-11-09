package io.mindspice.okragameserver.game.bot.behavior.core;

import io.mindspice.okragameserver.game.bot.behavior.logic.Decision;
import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.bot.state.TreeFocusState;
import io.mindspice.okragameserver.game.enums.PawnIndex;

import static io.mindspice.okragameserver.game.bot.behavior.core.Node.Type.DECISION;


public class DecisionNode extends Node {
    private final Decision[] decisions;

    public DecisionNode(String name, Decision[] decisions) {
        super(DECISION, name);
        this.decisions = decisions;
    }

    @Override
    public boolean travel(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
        for (Decision decision : decisions) {
            var rtn = decision.getDecision(botPlayerState, focusState, selfIndex);
            if (!rtn) { return false; }
        }

        focusState.decisions.add(name);

        for (Node node : adjacentNodes) {
            if (node.travel(botPlayerState, focusState, selfIndex)) { return true; }
        }
        return false;
    }
}
