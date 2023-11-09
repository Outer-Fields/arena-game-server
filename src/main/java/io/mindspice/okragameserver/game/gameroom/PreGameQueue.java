package io.mindspice.okragameserver.game.gameroom;

import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.player.QueuedPlayer;

import java.sql.Time;
import java.time.Instant;


public class PreGameQueue {
    private final QueuedPlayer player1;
    private final QueuedPlayer player2;
    private boolean player1Ready;
    private boolean player2Ready;
    private boolean botMatch;
    private long initTime = Instant.now().getEpochSecond();

    public PreGameQueue(QueuedPlayer player1, QueuedPlayer player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.botMatch = false;
    }

    public PreGameQueue(QueuedPlayer player1) {
        this.player1 = player1;
        this.player2 = null;
        this.botMatch = true;
    }

    public boolean expired(long nowTime) {
        return nowTime - initTime > 32;
    }

    public void setPlayerReady(int playerId) {
        if (player1.player().getId() == playerId) {
            player1Ready = true;
        } else {
            player2Ready = true;
        }
    }

    public boolean isBotMatch() {
        return botMatch;
    }

    public boolean matchReady() {
        return player1Ready && (player2Ready || botMatch);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PreGameQueue: ");
        sb.append("\n  player1: ").append(player1);
        sb.append(",\n  player2: ").append(player2);
        sb.append(",\n  player1Ready: ").append(player1Ready);
        sb.append(",\n  player2Ready: ").append(player2Ready);
        sb.append(",\n  botMatch: ").append(botMatch);
        sb.append(",\n  initTime: ").append(initTime);
        sb.append("\n");
        return sb.toString();
    }

    public QueuedPlayer player1() {
        return player1;
    }

    public QueuedPlayer player2() {
        return player2;
    }

    public boolean isPlayer1Ready() {
        return player1Ready;
    }

    public boolean isPlayer2Ready() {
        return player2Ready;
    }

    public JsonNode getStatusJson() {
        return new JsonUtils.ObjectBuilder()
                .put("player1", player1.player().getId())
                .put("player1_ready", player1Ready)
                .put("player2", player2 != null ? player2.player().getId() : "Bot")
                .put("player2_ready", player2Ready)
                .put("init_time", Time.from(Instant.ofEpochSecond(initTime)))
                .put("match_ready", matchReady())
                .buildNode();
    }
}
