package io.mindspice.okragameserver.schema.websocket.incoming;

public enum InMsgType {
    JOIN_QUEUE,
    LEAVE_QUEUE,
    SAVE_PAWN_SET,
    DELETE_PAWN_SET,
    POTION_PURCHASE,
    PACK_PURCHASE,
    FETCH_PAWN_SETS,
    FETCH_CARDS,
    FETCH_UPDATE,
    FETCH_OFFERS,
}
