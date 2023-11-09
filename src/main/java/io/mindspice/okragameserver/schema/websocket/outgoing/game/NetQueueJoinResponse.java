package io.mindspice.okragameserver.schema.websocket.outgoing.game;

import io.mindspice.okragameserver.schema.websocket.outgoing.NetMsg;
import io.mindspice.okragameserver.schema.websocket.outgoing.OutMsgType;


public class NetQueueJoinResponse extends NetMsg {
    public boolean success;
    public String message = "";

    public NetQueueJoinResponse(boolean queueAccepted) {
        super(OutMsgType.QUEUE_JOIN_RESPONSE, true);
        success = queueAccepted;
    }

    public NetQueueJoinResponse(boolean queueAccepted, String msg) {
        super(OutMsgType.QUEUE_JOIN_RESPONSE, true);
        success = queueAccepted;
        message = msg;
    }

    public static NetQueueJoinResponse acceptedQueue() {
        return new NetQueueJoinResponse(true, "Joined Queue");
    }

    public static NetQueueJoinResponse leftQueue() {
        return new NetQueueJoinResponse(true, "Left Queue");
    }

    public static NetQueueJoinResponse freeGamesExhausted() {
        return new NetQueueJoinResponse(true, " Daily Free Games Exhausted");
    }

    public static NetQueueJoinResponse alreadyQueued() {
        return new NetQueueJoinResponse(true, "Already In Queue");
    }

    public static NetQueueJoinResponse invalidPawnSet (String reason) {
        return new NetQueueJoinResponse(false, "Invalid Pawn Set: " + reason);
    }

    public static NetQueueJoinResponse paused() {
        return new NetQueueJoinResponse(false, "Matchmaking Paused");
    }

    public static NetQueueJoinResponse invalidSet(String reason) {
        return new NetQueueJoinResponse(false, "Invalid Pawn Set: " + reason);
    }

    public static NetQueueJoinResponse error(String errorMsg) {
        return new NetQueueJoinResponse(false, "Error: " + errorMsg);
    }

    public static NetQueueJoinResponse coolDown() {
        return  new NetQueueJoinResponse(false, "On cool down from missed queue");
    }
}
