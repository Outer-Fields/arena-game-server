package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import io.mindspice.okragameserver.game.gameroom.MatchResult;
import io.mindspice.okragameserver.schema.websocket.outgoing.NetMsg;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;


public class NetGameOver extends NetMsg {
    public final MatchResult.EndFlag endFlag;
    public final String reward;


    public NetGameOver(boolean isPlayer, MatchResult.EndFlag endFlag, String reward) {
        super(OutMsgType.GAME_OVER, isPlayer);
        this.endFlag = endFlag;
        this.reward = reward;
    }
}
