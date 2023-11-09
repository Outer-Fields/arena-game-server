package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.schema.websocket.outgoing.NetMsg;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;


public class NetTurnUpdate extends NetMsg {
    public boolean pawn_1 = false;
    public boolean pawn_2 = false;
    public boolean pawn_3 = false;

    public NetTurnUpdate(boolean isPlayer) {
        super(OutMsgType.TURN_UPDATE, isPlayer);
    }

    public void setActive(PawnIndex pawnIndex, boolean isActive) {
        switch (pawnIndex) {
            case PAWN1 -> pawn_1 = isActive;
            case PAWN2 -> pawn_2 = isActive;
            case PAWN3 -> pawn_3 = isActive;
        }
    }

}
