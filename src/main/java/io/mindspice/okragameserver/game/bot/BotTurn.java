package io.mindspice.okragameserver.game.bot;

import io.mindspice.okragameserver.game.bot.behavior.BehaviorGraph;
import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.bot.state.TreeFocusState;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.util.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;

public class BotTurn implements Runnable {
    private final PawnIndex pawnIndex;
    private final BehaviorGraph behaviorGraph = BehaviorGraph.getInstance();
    private final BotPlayerState botPlayerState;


    public BotTurn(BotPlayerState botPlayerState, PawnIndex pawnIndex) {
        this.pawnIndex = pawnIndex;
        this.botPlayerState = botPlayerState;
    }

    @Override
    public void run() {
        try {
            var focusState = new TreeFocusState();
            behaviorGraph.playTurn(botPlayerState, focusState, pawnIndex);
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "Error Running Bot Turn On BotPlayerId "
                    + botPlayerState.getPlayerState().getId(), e
            );
        }
    }
}


