package io.mindspice.okragameserver.schema.websocket.outgoing.lobby;

import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;


import java.util.List;


public record NetQueueResponse(
        OutMsgType msg_type,
        boolean is_staging,
        String match_id,
        boolean confirmed
) {
    public NetQueueResponse(String matchId) {
        this(OutMsgType.QUEUE_RESPONSE, true, matchId, false);
    }
    public NetQueueResponse(boolean confirmed) {
        this(OutMsgType.QUEUE_RESPONSE, false, "", confirmed);
    }
}
