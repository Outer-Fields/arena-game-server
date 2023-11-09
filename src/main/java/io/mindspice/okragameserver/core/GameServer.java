package io.mindspice.okragameserver.core;

import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.databaseservice.client.schema.Results;
import io.mindspice.databaseservice.client.schema.Reward;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.bot.BotFactory;
import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.gameroom.GameRoom;
import io.mindspice.okragameserver.game.gameroom.MatchResult;
import io.mindspice.okragameserver.game.gameroom.PreGameQueue;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;
import io.mindspice.okragameserver.schema.PawnSet;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.NetGameOver;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.NetKeepAlive;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.NetQueueJoinResponse;
import io.mindspice.okragameserver.schema.websocket.outgoing.lobby.NetQueueResponse;
import io.mindspice.okragameserver.util.Log;
import io.mindspice.okragameserver.util.RewardCalc;
import io.mindspice.okragameserver.game.player.Player;
import io.mindspice.okragameserver.game.player.QueuedPlayer;
import org.springframework.web.socket.CloseStatus;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;


public class GameServer {

    /* Executors */
    private final ScheduledExecutorService gameRoomExec;

    /* Lobby Structures */
    private final Map<UUID, ActiveGame> activeGames = new ConcurrentHashMap<>();
    private final Set<QueuedPlayer> matchQueue = ConcurrentHashMap.newKeySet();
    private final Map<String, PreGameQueue> preGameQueue = new ConcurrentHashMap<>();
    private final BotFactory botFactory;
    private final HttpServiceClient serviceClient;
    private final Map<Integer, Player> playerTable;

    /* util */

    public GameServer(ScheduledExecutorService gameRoomExec, BotFactory botFactory,
            HttpServiceClient serviceClient, Map<Integer, Player> playerTable) {
        this.gameRoomExec = gameRoomExec;
        this.botFactory = botFactory;
        this.serviceClient = serviceClient;
        this.playerTable = playerTable;
    }

    public void init() {
        initMatchMaking();
        initCleanup();
        Log.SERVER.info(this.getClass(), "Initialized GameServer");
    }

    public NetQueueJoinResponse addRemoveQueued(Player player, int setIndex, boolean isJoin) {
        Log.SERVER.debug(this.getClass(), "Queue update for player:" + player.getLoggable() + " isJoin: " + isJoin);

        if (Settings.GET().isPaused) { return NetQueueJoinResponse.paused(); }
        if (player.isInGame()) { return NetQueueJoinResponse.alreadyQueued(); }
        if (player.isQueueCoolDown()) { NetQueueJoinResponse.coolDown(); }

        if (!isJoin) {
            matchQueue.removeIf(p -> p.player().getId() == player.getId());
            return NetQueueJoinResponse.leftQueue();
        }

        if (setIndex == -1) {
            if (Settings.GET().maxFreeGames != -1) {
                try {
                    int gamesPlayed = serviceClient.gameAPI().getFreeGamesPlayed(player.getId()).data().orElseThrow();
                    if (gamesPlayed >= Settings.GET().maxFreeGames) {
                        return NetQueueJoinResponse.freeGamesExhausted();
                    }
                } catch (NoSuchElementException e) {
                    return NetQueueJoinResponse.error("Failed DB Lookup, please report error");
                }
            }
            if (Settings.GET().freeGameBotOnly) {
                queueMatchWithBot(QueuedPlayer.makeFreeQueue(player));
                return NetQueueJoinResponse.acceptedQueue();
            }
            matchQueue.add(QueuedPlayer.makeFreeQueue(player));
            return NetQueueJoinResponse.acceptedQueue();
        }

        if (!player.getPawnSets().containsKey(setIndex)) { return NetQueueJoinResponse.invalidSet("No set at index"); }
        matchQueue.add(new QueuedPlayer(player, setIndex));
        return NetQueueJoinResponse.acceptedQueue();
    }

    public boolean setQueueConfirm(String uuid, int playerId) {
        Log.SERVER.debug(this.getClass(), "Queue Confirm for playerId:" + "playerId:" + playerId + " uuid:" + uuid);
        if (!preGameQueue.containsKey(uuid)) {
            return false;
        }
        preGameQueue.get(uuid).setPlayerReady(playerId);
        return true;
    }

    public boolean isInQueue(int playerId) {
        for (var qp : matchQueue) {
            if (qp.player().getId() == playerId) {
                Log.SERVER.debug(this.getClass(), "Already in queue, playerId:" + playerId);
                return true;
            }
        }
        return false;
    }

    private void initCleanup() {
        Log.SERVER.info(this.getClass(), "Initialized Cleanup");

        Runnable cleanup = () -> {
            try {
                final long now = Instant.now().getEpochSecond();

                playerTable.entrySet().stream()
                        .filter(p -> !p.getValue().isConnected())
                        .filter(p -> (now - p.getValue().getLastMsgEpoch() > 1600))
                        .forEach(p -> {
                            playerTable.remove(p.getKey());
                            try {
                                p.getValue().getConnection().close(CloseStatus.NORMAL);
                            } catch (IOException e) {
                                // No need to catch, will throw if player is already disconnected.
                            }
                        });

                playerTable.forEach((key, value) -> value.send(new NetKeepAlive()));
            } catch (Exception e) {
                Log.SERVER.error(this.getClass(), "Error running player table clean up", e);
            }
        };
        gameRoomExec.scheduleAtFixedRate(cleanup, 60, 60, TimeUnit.SECONDS);
    }

    private void initMatchMaking() {
        Log.SERVER.info(this.getClass(), "Initialized MatchMaker");
        Runnable matchMaker = () -> {
            try {
                if (Settings.GET().isPaused) { return; }
                int setDiff = Settings.GET().queueSetLevelDifferential;
                Set<QueuedPlayer> queueRemovals = new HashSet<>();
                long now = Instant.now().getEpochSecond();

                for (var player1 : matchQueue) {
                    for (var player2 : matchQueue) {
                        if (player1 == player2 || queueRemovals.contains(player1) || queueRemovals.contains(player2)) {
                            continue;
                        }
                        if ((player1.setLevel() == -1 || player2.setLevel() == -1)
                                || Math.abs(player1.setLevel() - player2.setLevel()) < setDiff) {
                            queueMatch(player1, player2);
                            queueRemovals.add(player1);
                            queueRemovals.add(player2);
                            break; // Break,  don't need to loop the inner loop more with player1
                        }
                    }
                    if (now - player1.queuedTime() > Settings.GET().botQueueWait) {
                        queueMatchWithBot(player1);
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
                            createMatchWithBot(player1);
                        } else {
                            createMatch(player1, player2);
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

            } catch (Exception e) {
                Log.SERVER.error(this.getClass(), "Error in match queue", e);
            }
        };
        gameRoomExec.scheduleAtFixedRate(matchMaker, 0, 5, TimeUnit.SECONDS);
    }

    private void queueMatch(QueuedPlayer player1, QueuedPlayer player2) {
        String matchId = UUID.randomUUID().toString();
        Log.SERVER.debug(
                this.getClass(),
                "Pre Match:" + matchId + "PlayerIds:" + player1.player().getIp() + " | " + player2.player().getId()
        );
        preGameQueue.put(matchId, new PreGameQueue(player1, player2));
        player1.player().send(new NetQueueResponse(matchId));
        player2.player().send(new NetQueueResponse(matchId));
    }

    private void queueMatchWithBot(QueuedPlayer player1) {
        String matchId = UUID.randomUUID().toString();
        Log.SERVER.debug(this.getClass(), "Pre Match With Bot:" + matchId + "PlayerId:" + player1.player().getId());
        preGameQueue.put(matchId, new PreGameQueue(player1));
        player1.player().send(new NetQueueResponse(matchId));
    }

    private void createMatch(QueuedPlayer player1, QueuedPlayer player2) {
        if (Settings.GET().isPaused) {
            Log.SERVER.debug(this.getClass(), "Aborted creating game due to paused Server");
            return;
        }

        int freeLevel;
        if (player1.isFreeGame() && player2.isFreeGame()) {
            freeLevel = PawnSet.getRandomLevel();
            serviceClient.gameAPI().incFreeGamesPlayed(player1.player().getId());
            serviceClient.gameAPI().incFreeGamesPlayed(player2.player().getId());
        } else {
            freeLevel = player1.isFreeGame() ? player2.pawnSet().setLevel() : player1.setLevel();
            serviceClient.gameAPI().incFreeGamesPlayed(player1.isFreeGame()
                    ? player1.player().getId()
                    : player2.player().getId()
            );
        }

        PlayerGameState p1State;
        if (player1.isFreeGame()) {
            p1State = new PlayerGameState(player1.player(), PawnSet.getRandomPawnSet(freeLevel));
        } else {
            p1State = new PlayerGameState(player1.player(), player1.pawnSet());
        }

        PlayerGameState p2State;
        if (player2.isFreeGame()) {
            p2State = new PlayerGameState(player2.player(), PawnSet.getRandomPawnSet(freeLevel));
        } else {
            p2State = new PlayerGameState(player2.player(), player2.pawnSet());
        }

        PlayerGameState firstPlayer = ThreadLocalRandom.current()
                .nextInt(0, 100) < 49
                ? p1State : p2State;

        GameRoom gameRoom = new GameRoom(firstPlayer, firstPlayer == p1State ? p2State : p1State);
        if (player1.isFreeGame() || player2.isFreeGame()) {
            var freeIds = new ArrayList<Integer>(2);
            if (player1.isFreeGame()) { freeIds.add(player1.player().getId()); }
            if (player2.isFreeGame()) { freeIds.add(player2.player().getId()); }
            gameRoom.setFreeGame(freeIds);
        }

        ScheduledFuture<?> gameProc = gameRoomExec.scheduleWithFixedDelay(
                gameRoom,
                1000,
                200,
                TimeUnit.MILLISECONDS);

        player1.player().send(new NetQueueResponse(true));
        player2.player().send(new NetQueueResponse(true));

        Log.SERVER.debug(this.getClass(),
                "Initiated gameroom:" + gameRoom.getRoomId() + " for playerIds:"
                        + player1.player().getId() + " | " + player2.player().getId()
        );

        ActiveGame activeGame = new ActiveGame(gameRoom, gameProc);
        activeGames.put(gameRoom.getRoomId(), activeGame);

        gameRoom.getResultFuture().thenAccept(result -> {
            finalizeMatch(result, gameRoom, gameProc);
        }).exceptionally(ex -> {
            Log.SERVER.error(this.getClass(), "Error on finalize match callback |" +
                    "Info: " + gameRoom.getStatusJson());
            return null;
        });
    }

    private void createMatchWithBot(QueuedPlayer player1) {
        if (Settings.GET().isPaused) {
            Log.SERVER.debug(this.getClass(), "Aborted creating game due to paused Server");
            return;
        }
        PlayerGameState p1State;
        int player1Lvl = player1.isFreeGame() ? PawnSet.getRandomLevel() : player1.setLevel();
        if (player1.isFreeGame()) {
            serviceClient.gameAPI().incFreeGamesPlayed(player1.player().getId());
            p1State = new PlayerGameState(player1.player(), PawnSet.getRandomPawnSet(player1Lvl));
        } else {
            p1State = new PlayerGameState(player1.player(), player1.pawnSet());
        }
        BotPlayerState bState = botFactory.getBotPlayerState(p1State, player1Lvl);

        PlayerGameState firstPlayer = ThreadLocalRandom.current()
                .nextInt(0, 100) < 49
                ? p1State : bState;

        GameRoom gameRoom = new GameRoom(firstPlayer, firstPlayer == p1State ? bState : p1State);
        if (player1.isFreeGame()) { gameRoom.setFreeGame(List.of(player1.player().getId())); }

        ScheduledFuture<?> gameProc = gameRoomExec.scheduleWithFixedDelay(
                gameRoom,
                0,
                200,
                TimeUnit.MILLISECONDS);

        Log.SERVER.debug(this.getClass(), "Started GameRoom: " + gameRoom.getRoomId());

        gameRoom.setReady(bState.getId());

        player1.player().send(new NetQueueResponse(true));
        ActiveGame activeGame = new ActiveGame(gameRoom, gameProc);

        Log.SERVER.debug(this.getClass(), "Initiated bot gameroom:" + gameRoom.getRoomId()
                + " for playerIds:" + player1.player().getId());

        activeGames.put(gameRoom.getRoomId(), activeGame);
        gameRoom.getResultFuture().thenAccept(result -> {
            finalizeMatch(result, gameRoom, gameProc);
        }).exceptionally(ex -> {
            Log.SERVER.error(this.getClass(), "Error on finalize bot match callback");
            return null;
        });
    }

    private void finalizeMatch(MatchResult matchResult, GameRoom gameRoom, ScheduledFuture<?> gameProc) {
        try {

            gameProc.cancel(false);
            activeGames.remove(gameRoom.getRoomId());
            matchResult.player1().getPlayer().setInGame(false);
            matchResult.player1().getPlayer().setGameRoom(null);
            matchResult.player2().getPlayer().setInGame(false);
            matchResult.player2().getPlayer().setGameRoom(null);

            serviceClient.gameAPI().commitFullMatchResult(
                    matchResult.matchId().toString(),
                    matchResult.player1().getId(),
                    matchResult.player2().getId(),
                    matchResult.winners().contains(matchResult.player1()),
                    matchResult.winners().contains(matchResult.player2()),
                    matchResult.roundCount(),
                    matchResult.endFlag().name(),
                    new ArrayList<>(matchResult.player1().getPlayer().getIp()),
                    new ArrayList<>(matchResult.player2().getPlayer().getIp())
            );

            // Iterate over the winning players, there isn't tie logic atm but will be in the future,
            //  so it simpler to just iterate over the winners/loser and do the updates and rewards
            if (matchResult.endFlag() == MatchResult.EndFlag.UNREADIED) {
                matchResult.player1().send(new NetGameOver(false, matchResult.endFlag(), "Game Canceled, Both Player Didn't Ready"));
                matchResult.player2().send(new NetGameOver(false, matchResult.endFlag(), "Game Canceled, Both Player Didn't Ready"));
                return;
            }

            if(matchResult.endFlag() == MatchResult.EndFlag.DISCONNECT && matchResult.roundCount() < 5) {
                matchResult.player1().send(new NetGameOver(false, matchResult.endFlag(), "Player Disconnected"));
                matchResult.player2().send(new NetGameOver(false, matchResult.endFlag(), "Player Disconnected"));
            }

            for (var player : matchResult.winners()) {
                if (player.getPlayer().isBot()) { continue; }
                serviceClient.gameAPI().commitMatchResult(player.getId(), true);
                // Get the updated result count for rewards
                Results todayResults = serviceClient.gameAPI().getPlayerDailyResults(player.getId())
                        .data().orElseThrow(serviceClient.serviceError);

                if (matchResult.freeGameIds().contains(player.getId())) {
                    Pair<Reward, Integer> reward = RewardCalc.getBasicReward(todayResults.wins());
                    serviceClient.gameAPI().commitPlayerRewards(player.getId(), reward.first(), reward.second());
                    player.send(new NetGameOver(true, matchResult.endFlag(), reward.second() + " " + reward.first()));

                } else if (player.getId() != 12) { //12 is the bots id, rewards should be skipped for it
                    Pair<Reward, Integer> reward = RewardCalc.getReward(todayResults.wins());
                    serviceClient.gameAPI().commitPlayerRewards(player.getId(), reward.first(), reward.second());
                    player.send(new NetGameOver(true, matchResult.endFlag(), reward.second() + " " + reward.first()));
                }
            }

            for (var player : matchResult.losers()) {
                if (player.getPlayer().isBot()) { continue; }
                serviceClient.gameAPI().commitMatchResult(player.getId(), false);
                player.send(new NetGameOver(false, matchResult.endFlag(), ""));
            }

            Log.SERVER.debug(this.getClass(), "Finalized Match:" + matchResult.getJsonLog());
        } catch (Exception e) {
            Log.FAILED.error(this.getClass(), "Failed to update match and send results: " + matchResult.getJsonLog(), e);
        }
    }

    public JsonNode getGameRoomStatusJson(String uuid) {
        return activeGames.get(UUID.fromString(uuid)).gameRoom().getStatusJson();
    }

    public JsonNode getStatusJson() {
        return new JsonUtils.ObjectBuilder()
                .put("active_games_count", activeGames.size())
                .put("active_games", activeGames.values().stream().map(g -> g.gameRoom().getStatusJson()).toList())
                .put("match_queue_size", matchQueue.size())
                .put("match_queue", matchQueue.stream().map(qp -> qp.player().getStatusJson()).toList())
                .put("pregame_queue_size", preGameQueue.size())
                .put("pregame_queue", preGameQueue.values().stream().map(PreGameQueue::getStatusJson).toList())
                .buildNode();
    }
}
