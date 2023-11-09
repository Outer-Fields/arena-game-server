package io.mindspice.okragameserver.core;

import io.mindspice.okragameserver.game.gameroom.GameRoom;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

public record ActiveGame(
        GameRoom gameRoom,
        long initTime,
        ScheduledFuture<?> process
) {

    public ActiveGame(GameRoom gameRoom, ScheduledFuture<?> process) {
        this(gameRoom, Instant.now().getEpochSecond(), process);
    }

}
