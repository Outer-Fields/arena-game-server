package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import io.mindspice.okragameserver.schema.websocket.outgoing.NetMsg;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;


public class NetRestoreGame extends NetMsg {
    public final String room_id;
    public NetRestoreGame(String roomId) {
        super(OutMsgType.RESTORE_GAME, true);
        room_id = roomId;
    }
}
