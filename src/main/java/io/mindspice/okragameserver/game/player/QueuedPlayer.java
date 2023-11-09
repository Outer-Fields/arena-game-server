package io.mindspice.okragameserver.game.player;

import io.mindspice.okragameserver.schema.PawnSet;

import java.time.Instant;


public record QueuedPlayer(
        Player player,
        int setLevel,
        PawnSet pawnSet,
        long queuedTime,
        boolean isFreeGame) {

    public QueuedPlayer(Player player, int setIndex) {
        this(
                player,
                player.getPawnSets().get(setIndex).setLevel(),
                player.getPawnSets().get(setIndex),
                Instant.now().getEpochSecond(),
                false
        );
    }

    public QueuedPlayer(Player player, boolean isFreeGame) {
        this(
                player,
                -1,
                null,
                Instant.now().getEpochSecond(),
                isFreeGame
        );
    }

    public QueuedPlayer(Player player) {
        this(
                player,
                -1,
                null,
                Instant.now().getEpochSecond(),
                false
        );
    }

    public static QueuedPlayer makeFreeQueue(Player player) {
        return new QueuedPlayer(
                player,
                -1,
                null,
                Instant.now().getEpochSecond(),
                true
        );
    }

    @Override
    public String toString() {
        return "QueuedPlayer{" +
            //    "player=" + player.getId() +
                ", setLevel=" + setLevel +
                ", queuedTime=" + queuedTime +
                '}';
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) { return true; }
//        if (o == null || getClass() != o.getClass()) { return false; }
//        QueuedPlayer that = (QueuedPlayer) o;
//        return (player.getId() == that.player.getId());
//    }

 //   @Override FIXME UNCOMMENT
//    public int hashCode() {
//        return player.getId();
//    }
}
