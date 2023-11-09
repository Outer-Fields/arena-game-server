package io.mindspice.okragameserver.schema.websocket.outgoing;

public enum OutMsgType {
    TURN_UPDATE,
    INSIGHT,
    DEAD,
    GAME_OVER,
    EFFECT,
    STAT_UPDATE,
    TURN_RESPONSE,
    REJOIN,
    CARD_UPDATE,
    PAWN_SET_UPDATE,
    QUEUE_RESPONSE,
    OWNED_CARDS,
    NET_MSG,
    PLAYER_FUNDS,
    POTIONS_UPDATE,
    NET_STATUS,
    PLAYABLE_CARDS,
    ROUND_UPDATE,
    QUEUE_JOIN_RESPONSE,
    RESTORE_GAME,
    KEEP_ALIVE
}
