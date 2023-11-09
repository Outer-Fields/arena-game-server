package io.mindspice.okragameserver.game.bot.behavior.core;

import io.mindspice.okragameserver.core.Settings;
import io.mindspice.okragameserver.game.bot.behavior.logic.Action;
import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.bot.state.TreeFocusState;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.util.gamelogger.GameLogger;

import static io.mindspice.okragameserver.game.bot.behavior.core.Node.Type.ACTION;


public class ActionNode extends Node {
    private final Action action;

    public ActionNode(Action action, String name) {
        super(ACTION, name);
        this.action = action;
    }

    @Override
    public boolean travel(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
        boolean rtn = action.doAction(botPlayerState, focusState, selfIndex);
        if (rtn) {
            focusState.action = name;
            focusState.decisions.add(name);
            if (Settings.GET().gameLogging) {
                GameLogger.GET().addBotDecision(
                        botPlayerState.getRoomId(), botPlayerState.getId(), selfIndex, focusState.decisions
                );
            }
        }
        return rtn;
        //return action.doAction(botPlayerState, focusState, selfIndex);
    }
}
