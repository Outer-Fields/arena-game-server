package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.okragameserver.schema.websocket.outgoing.NetMsg;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;


public class NetStatus extends NetMsg {
    @JsonProperty("start_game") private boolean startGame;
    @JsonProperty("pause_game") private boolean pauseGame;
    @JsonProperty("pause_time") private int pauseTime;

    public NetStatus(boolean startGame, boolean pauseGame, int pauseTime) {
        super(OutMsgType.NET_STATUS, true);
        this.startGame = startGame;
        this.pauseGame = pauseGame;
        this.pauseTime = pauseTime;
    }
}
