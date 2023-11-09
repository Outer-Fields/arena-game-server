package io.mindspice.okragameserver.schema.websocket.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;


public abstract class NetMsg {
    @JsonProperty("is_player") public final boolean isPlayer;
    @JsonProperty("msg_type")  public final OutMsgType msgType;

    public NetMsg(OutMsgType msgType, boolean isPlayer) {
        this.msgType = msgType;
        this.isPlayer = isPlayer;
    }
}
