package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.schema.websocket.outgoing.NetMsg;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;


public class NetCardUpdate extends NetMsg {
    public CardHand pawn_1;
    public CardHand pawn_2;
    public CardHand pawn_3;

    public NetCardUpdate(boolean isPlayer) {
        super(OutMsgType.CARD_UPDATE, isPlayer);
    }

    public void setHand(PawnIndex pawnIndex, CardHand cardHand) {
        switch (pawnIndex) {
            case PAWN1 -> pawn_1 = cardHand;
            case PAWN2 -> pawn_2 = cardHand;
            case PAWN3 -> pawn_3 = cardHand;
        }
    }

}
