package io.mindspice.okragameserver.schema.websocket.outgoing.lobby;

import io.mindspice.okragameserver.schema.PawnSet;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;

import java.util.HashMap;
import java.util.Map;


public record NetPawnSetUpdate(
        OutMsgType msg_type,
        Map<Integer, PawnSet> pawn_sets
) {
    public NetPawnSetUpdate(Map<Integer, PawnSet> pawnSets) {
        this(OutMsgType.PAWN_SET_UPDATE, pawnSets);
    }
}
