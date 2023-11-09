package io.mindspice.okragameserver;

import io.mindspice.okragameserver.core.Settings;
import io.mindspice.okragameserver.game.gameroom.PreGameQueue;
import io.mindspice.okragameserver.game.player.QueuedPlayer;
import io.mindspice.okragameserver.schema.websocket.outgoing.lobby.NetQueueResponse;
import io.mindspice.okragameserver.util.Log;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;


public class JankQueueTest {

    ScheduledExecutorService gameExec = new ScheduledThreadPoolExecutor(5);
    private final Set<QueuedPlayer> matchQueue = ConcurrentHashMap.newKeySet();
    private final Map<String, PreGameQueue> preGameQueue = new ConcurrentHashMap<>();

    @Test
    public void testQueue() throws InterruptedException {
        Runnable matchMaker = () -> {
            try {
                if (Settings.GET().isPaused) { return; }
                int setDiff = Settings.GET().queueSetLevelDifferential;
                Set<QueuedPlayer> queueRemovals = new HashSet<>();
                long now = Instant.now().getEpochSecond();

                for (var player1 : matchQueue) {
                    for (var player2 : matchQueue) {
                        if (player1 == player2 || queueRemovals.contains(player2) || queueRemovals.contains(player1)) {
                            continue;
                        }
                        if (Math.abs(player1.setLevel() - player2.setLevel()) < setDiff) {
                            System.out.println("queueing match:" + player1 + " | " + player2);
                            queueRemovals.add(player1);
                            queueRemovals.add(player2);
                            break; // Break as we don't need to loop the inner loop more with player1
                        }
                    }
                    if (now - player1.queuedTime() > Settings.GET().botQueueWait) {
                        System.out.println("queueing bot match:" + player1);
                        queueRemovals.add(player1);
                    }
                }
                queueRemovals.forEach(matchQueue::remove);

                var preGameRemovals = new ArrayList<String>();
                for (var e : preGameQueue.entrySet()) {
                    var player1 = e.getValue().player1();
                    var player2 = e.getValue().player2();
                    var isBotMatch = e.getValue().isBotMatch();
                    if (e.getValue().matchReady()) {
                        if (isBotMatch) {
                            // createMatchWithBot(player1);
                        } else {
                            //createMatch(player1, player2);
                        }
                        preGameRemovals.add(e.getKey());
                        continue;
                    }
                    if (e.getValue().expired(now)) {
                        player1.player().send(new NetQueueResponse(false));
                        if (e.getValue().isPlayer1Ready()) {
                            matchQueue.add(player1);
                        } else {
                            player1.player().setQueueCoolDown();
                        }
                        if (!e.getValue().isBotMatch()) {
                            player2.player().send(new NetQueueResponse(false));
                            if (e.getValue().isPlayer2Ready()) {
                                matchQueue.add(player2);
                            } else {
                                player2.player().setQueueCoolDown();
                            }
                        }
                        preGameRemovals.add(e.getKey());
                    }
                }
                preGameRemovals.forEach(preGameQueue::remove);
                System.out.println(matchQueue);
                Settings.reload();
            } catch (Exception e) {
                Log.SERVER.error(this.getClass(), "Error in match queue", e);
            }
        };
        gameExec.scheduleAtFixedRate(matchMaker, 0, 5, TimeUnit.SECONDS);

        for (int i = 0; i < 9; ++i) {
            matchQueue.add(new QueuedPlayer(null, ThreadLocalRandom.current().nextInt(110, 1000), null, Instant.now().getEpochSecond(), false));

        }
        System.out.println(matchQueue.size());
        Thread.sleep(999999999);
    }
}
