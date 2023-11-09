package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.schema.websocket.outgoing.NetMsg;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;


public class NetDead extends NetMsg {
    public  PawnIndex pawn_index;

    public NetDead(PawnIndex pawn_index, boolean isOwnPawn) {
        super(OutMsgType.DEAD, isOwnPawn);
        this.pawn_index = pawn_index;
    }
}
