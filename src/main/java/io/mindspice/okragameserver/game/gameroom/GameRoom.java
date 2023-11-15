package io.mindspice.okragameserver.game.gameroom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.core.Settings;
import io.mindspice.okragameserver.game.gameroom.state.ActiveRoundState;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.NetKeepAlive;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.NetStatus;

import io.mindspice.okragameserver.util.Log;
import io.mindspice.okragameserver.game.enums.PawnIndex;

import io.mindspice.okragameserver.game.enums.PlayerAction;
import io.mindspice.okragameserver.schema.websocket.incoming.NetGameAction;
import io.mindspice.okragameserver.util.gamelogger.GameLogger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;


public class GameRoom implements Runnable {
    /* Room Info */
    private final UUID roomId;
    private volatile int round = 0;
    private final long initTime;
    private volatile boolean gameOver = false;
    private volatile boolean isStarted = false;
    private volatile boolean isPaused = false;
    private final List<PlayerGameState> disconnPlayers = new CopyOnWriteArrayList<>();
    private final CompletableFuture<MatchResult> resultFuture = new CompletableFuture<>();
    private final boolean doGameLog;
    private volatile List<Integer> freeGameIds = List.of();
    private volatile long keepAliveTimer = Instant.now().getEpochSecond();
    private volatile long connTimeTimer = Instant.now().getEpochSecond();
    private final boolean isBotGame;

    /* States */
    private PlayerGameState player1; // Effectively final, though they do have a setter for testing
    private PlayerGameState player2;
    private volatile ActiveRoundState activeRound;

    /* Queues - Multiple used to avoid players attempting to starve others msgs */
    private final ArrayBlockingQueue<NetGameAction> player1MsgQueue = new ArrayBlockingQueue<>(20);
    private final ArrayBlockingQueue<NetGameAction> player2MsgQueue = new ArrayBlockingQueue<>(20);

    // init the player state and combat managers before join to room, this allows easy insertion of bots
    public GameRoom(PlayerGameState p1GameState, PlayerGameState p2GameState) {
        roomId = UUID.randomUUID();
        player1 = p1GameState;
        player2 = p2GameState;
        if (player1.getPlayer().isBot() || player2.getPlayer().isBot()) {
            isBotGame = true;
        } else {
            isBotGame = false;
        }

        player1.setRoomId(roomId);
        player2.setRoomId(roomId);

        player1.getPlayer().setGameRoom(this);
        player1.getPlayer().setInGame(true);
        player1.getPlayer().setLastMsgTime();
        player2.getPlayer().setGameRoom(this);
        player2.getPlayer().setInGame(true);
        player2.getPlayer().setLastMsgTime();

        player1.setCombatManager(new NetCombatManager(player1, player2));
        player2.setCombatManager(new NetCombatManager(player2, player1));

        initTime = Instant.now().getEpochSecond();

        if (Settings.GET().gameLogging) {
            doGameLog = true;
            GameLogger.GET().init(roomId);
            GameLogger.GET().addRoundRecord(player1, player2);
        } else {
            doGameLog = false;
        }
        Log.SERVER.info("Starting new game | RoomId: " + roomId + " | Player1: " + player1.getPlayer().getLoggable()
                + " | Player2: " + player2.getPlayer().getLoggable());
    }

    public void setReady(int playerId) {
        Log.SERVER.debug(this.getClass(), "GameRoom: " + roomId + " | Player Readied:" + playerId);
        getPlayer(playerId).setReady(true);
    }

    public void setFreeGame(List<Integer> freeGameIds) {
        this.freeGameIds = freeGameIds;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GameRoom: ");
        sb.append("\n  roomId: ").append(roomId);
        sb.append(",\n  round: ").append(round);
        sb.append(",\n  player1: ").append(player1.getId());
        sb.append(",\n  player2: ").append(player2.getId());
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public void run() {
        update();
    }

    public boolean isDoGameLog() {
        return doGameLog;
    }

    public void addMsg(int id, NetGameAction msg) {
        if (msg == null) {
            Log.ABUSE.info("GameRoom: " + roomId + " | Null packet from Player: " + id);
            Log.SERVER.debug(this.getClass(), "GameRoom: " + roomId + " | Null packet from Player: " + id);
            return;
        }
        if (!isStarted) {
            return;
        }
        if (player1.getId() == id) {
            player1MsgQueue.offer(msg);
        } else {
            player2MsgQueue.offer(msg);
        }
    }

    public UUID getRoomId() {
        return this.roomId;
    }

    private boolean readyCheck() {

        if (player1.isReady() && player2.isReady()) {
            player1.send(new NetStatus(true, false, 0));
            player2.send(new NetStatus(true, false, 0));
            round = 1;
            isStarted = true;
            activeRound = new ActiveRoundState(player1, player2, 1);
            Log.SERVER.debug(this.getClass(), "GameRoom: " + roomId + " | Starting game both players ready");
            return true;
        }
        if (round == 0 && Instant.now().getEpochSecond() - initTime > 60) { // This handles failing to read at start
            Log.SERVER.info("GameRoom: " + roomId + " closed, both players didn't ready. Player1 Ready: "
                    + player1.isReady() + " | Player2 Ready: " + player2.isReady());
            doPrematureEnd();
        }
        return false;
    }

    private boolean pauseCheck() {
        for (var player : disconnPlayers) {
            if (player.getPlayer().isConnected() && !player.isReady()) {
                player.getCombatManager().sendGameRestore();
            } else if (player.getPlayer().isConnected() && player.isReady()) {
                player.getCombatManager().sendGameRestoreInfo(
                        getActivePlayerId() == player.getId() ? activeRound.getActiveTurn().getActivePawns() : List.of(),
                        getActivePlayerId() == player.getId()
                );
                var enemy = player.getId() == player1.getId() ? player2 : player1;
                enemy.getCombatManager().sendTurnUpdate(
                        getActivePlayerId() == enemy.getId() ? activeRound.getActiveTurn().getActivePawns() : List.of(),
                        getActivePlayerId() == enemy.getId());
                Log.SERVER.info("GameRoom: " + roomId + " | Player reconnected: " + player);
                disconnPlayers.remove(player); //CoW Array so we can do this
            } else {
                player.timedOutAmount++;
                if (player.timedOutAmount > (isBotGame ? (14400 * 5) : (90 * 5))) { // 5 updates a sec
                    gameOver = true;
                    disconnLoss(player);
                }
            }
        }
        if (disconnPlayers.isEmpty()) {
            activeRound.getActiveTurn().adjustRoundTime();
            player1.send(new NetStatus(
                    true,
                    false,
                    activeRound != null ? activeRound.getTurnTimeLeft() : 0)
            );
            player2.send(new NetStatus(
                    true,
                    false,
                    activeRound != null ? activeRound.getTurnTimeLeft() : 0)
            );
            isPaused = false;
            return true;
        }

        return false;
    }

    private boolean connTest() {

        if (!player1.getPlayer().isConnected() && !disconnPlayers.contains(player1)) {
            disconnPlayers.add(player1);
            var time = disconnPlayers.stream().mapToInt(PlayerGameState::getTimedOutAmount).max().orElse(1);
            player2.send(new NetStatus(false, true, ((90 * 5) - time) / 5)); // 5 updates a sec
            player1.setReady(false);
            Log.SERVER.info("GameRoom: " + roomId + " | Player disconnected: " + player1.getPlayer().getLoggable());
        }
        if (!player2.getPlayer().isConnected() && !disconnPlayers.contains(player2)) {
            disconnPlayers.add(player2);
            var time = disconnPlayers.stream().mapToInt(PlayerGameState::getTimedOutAmount).max().orElse(1);
            player1.send(new NetStatus(false, true, ((90 * 5) - time) / 5));
            player2.setReady(false);
            Log.SERVER.info("GameRoom: " + roomId + " | Player disconnected: " + player2.getPlayer().getLoggable());
        }

        if (disconnPlayers.isEmpty()) {
            isPaused = false;
            return true;
        } else {
            isPaused = true;
            return false;
        }

    }

    // Checks to see if when the last message for both players was, as a failsafe against games that do not properly end
    // The shouldn't happen, but during beta phase it is good to have
//    public void checkOrphanedGame() {
//        long now = Instant.now().getEpochSecond();
//        if (now - player1.getPlayer().getLastMsgTime() > 120 || now - player2.getPlayer().getLastMsgTime() > 120) {
//            doPrematureEnd();
//            gameOver = true;
//            Log.SERVER.error(this.getClass(), "GameRoom: " + roomId + " | Closed Orphan Game" +
//                    " | Player1: " + player1.getId() + " Connected: " + player1.getPlayer().isConnected() +
//                    " | Player2: " + player2.getId() + " Connected: " + player2.getPlayer().isConnected());
//
//        }
//    }

    private void doPrematureEnd() {
        MatchResult result = MatchResult.unReadied(roomId, player1, player2);
        Log.SERVER.info("GameRoom: " + roomId + " | Game Over Players Didn't Ready " +
                "| Player1 Ready: " + player1.isReady() + " | Player2 Ready: " + player2.isReady());
        resultFuture.complete(result);
    }

    public void update() {
        try {
            long now = Instant.now().getEpochSecond();

            if (gameOver) { return; } // No reason to run, results will soon be gotten and room destroyed

            if (!isStarted) {
                if (!readyCheck()) { return; }
            }

            if (isPaused) {
                if (!pauseCheck()) { return; }
            }

            if (now - connTimeTimer > 3) {
                connTimeTimer = now;
                if (!connTest()) { return; }
            }

            switch (activeRound.update()) {
                case ACTIVE -> processActionQueue();
                case AWAITING_FINISH -> { return; }
                case FINISHED -> {
                    round++; // atomic doesn't matter, we just need it to be observable
                    activeRound = new ActiveRoundState(player1, player2, round);
                    Log.SERVER.debug(this.getClass(), "GameRoom: " + roomId + " | Finished round: " + round);
                }
                case GAME_OVER -> {
                    if (doGameLog) {
                        try {
                            GameLogger.GET().endLog(roomId);
                        } catch (Exception e) {
                            Log.SERVER.error(this.getClass(), "GameRoom: " + roomId + " | Error writing endgame log ", e);

                        }
                    }
                    Log.SERVER.info("GameRoom: " + roomId + " | Game over | Round: " + round + " | Total Time: "
                            + BigDecimal.valueOf((Instant.now().getEpochSecond() - initTime) / 60.0).setScale(3, RoundingMode.HALF_UP));
                    gameOver = true;
                    endGame();
                }
            }
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "GameRoom: " + roomId + " | Exception in update loop ", e);
        }
    }

    public void processActionQueue() {
        ArrayBlockingQueue<NetGameAction> packetQueue = getActiveQueue();
        if (packetQueue.isEmpty()) { return; }
        for (int i = 0; i < packetQueue.size(); ++i) {
            NetGameAction nga = packetQueue.poll();
            if (doGameLog) { GameLogger.GET().addActionMsgIn(roomId, getActivePlayerId(), nga); }
            if (!validate(getActivePlayerId(), nga)) { return; }
            activeRound.getActiveTurn().doAction(nga);
        }
    }

    private PlayerGameState getPlayer(int id) {
        if (player1.getId() == id) {
            return player1;
        } else {
            return player2;
        }
    }

    private boolean isActiveId(int id) {
        return activeRound.getActivePlayerId() == id;
    }

    private ArrayBlockingQueue<NetGameAction> getActiveQueue() {
        if (activeRound.getActivePlayerId() == player1.getId()) {
            if (!player2MsgQueue.isEmpty()) {
                Log.SERVER.debug(this.getClass(), "GameRoom:" + roomId + " | PlayerId: " + player1.getId()
                        + " | Non empty queue on round rollover, error or late message occurred"
                        + " | Queue Size:" + player1MsgQueue.size());
                player2MsgQueue.clear();
            }
            return player1MsgQueue;
        } else {
            if (!player1MsgQueue.isEmpty()) {
                Log.SERVER.debug(this.getClass(), "GameRoom:" + roomId + " | PlayerId: " + player1.getId()
                        + " | Non empty queue on round rollover, error or late message occurred"
                        + " | Queue Size:" + player1MsgQueue.size());
                player1MsgQueue.clear();
            }
            return player2MsgQueue;
        }
    }

    private int getActivePlayerId() {
        return activeRound.getActivePlayerId() == player1.getId() ? player1.getId() : player2.getId();
    }

    private PlayerGameState getGameStateById(int playerId) {
        if (player1.getId() == playerId) { return player1; }
        return player2;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    private boolean validate(int playerId, NetGameAction nga) {
        if (nga == null) {
            Log.ABUSE.info("GameRoom: " + roomId + " | Null packet from playerId: " + playerId);
            Log.SERVER.debug(this.getClass(), "GameRoom: " + roomId + " | Null packet from playerId: " + playerId);
            return false;
        }
        if (nga.action() == null) {
            Log.ABUSE.info("GameRoom: " + roomId + " | Null Action from playerId: " + playerId);
            Log.SERVER.debug(this.getClass(), "GameRoom: " + roomId + " | Null Action from playerId: " + playerId);
        }
        if (nga.playerPawn() == null) {
            Log.ABUSE.info("GameRoom: " + roomId + " | Null Player Pawn from playerId: " + playerId);
            Log.SERVER.debug(this.getClass(), "GameRoom: " + roomId + " | Null Player Pawn from playerId: " + playerId);
        }
        if (nga.action() == PlayerAction.SKIP_PAWN) {
            return true;
        }
        if (nga.action() != PlayerAction.POTION && nga.targetPawn() == null) {
            Log.ABUSE.info("GameRoom: " + roomId + " | Null Target Pawn from playerId: " + playerId);
            Log.SERVER.debug(this.getClass(), "GameRoom: " + roomId + " | Null Target Pawn from playerId: " + playerId);
        }
        return true;
    }

    private void endGame() {
        // NOTE Needed for an edge case where a player crashes right after they send their ready message, but not active round has been set
        // FIXME this is a hack and this edge case needs handled
        if (activeRound == null || activeRound.getWinningPlayer() == null) {
            resultFuture.complete(MatchResult.unReadied(this.roomId, player1, player2));
        }

        PlayerGameState winningPlayer = activeRound.getWinningPlayer();

        MatchResult result = MatchResult.singleWinner(
                roomId,
                MatchResult.EndFlag.WINNER,
                round,
                winningPlayer,
                player1,
                player2,
                freeGameIds
        );

        resultFuture.complete(result);
        Log.SERVER.info("GameRoom: " + roomId + " | Winner: " + winningPlayer.getId());
    }

    public CompletableFuture<MatchResult> getResultFuture() {
        return resultFuture;
    }

    private void disconnLoss(PlayerGameState losingPlayer) {
        PlayerGameState winningPlayer = (losingPlayer == player1 ? player2 : player1);
        if (winningPlayer.getPlayer().isConnected()) {
            MatchResult result = MatchResult.singleWinner(
                    roomId,
                    MatchResult.EndFlag.DISCONNECT,
                    round,
                    winningPlayer,
                    player1,
                    player2,
                    freeGameIds
            );
            Log.SERVER.info("GameRoom: " + roomId + " | Game Over | Disconnect loss");
            resultFuture.complete(result);
        } else {
            MatchResult result =
                    MatchResult.noWinners(
                            roomId,
                            MatchResult.EndFlag.DISCONNECT,
                            round,
                            player1,
                            player2
                    );
            Log.SERVER.info("GameRoom: " + roomId + " | Game Over | Disconnect loss");
            resultFuture.complete(result);
        }
    }

    public JsonNode getStatusJson() {
        ObjectNode node = new JsonUtils.ObjectBuilder()
                .put("curr_round", round)
                .put("init_time", initTime)
                .put("is_started", isStarted)
                .put("is_paused", isPaused)
                .put("disconn_players", disconnPlayers)
                .put("player1_stats", getPlayerJson(player1))
                .put("player_2_stats", getPlayerJson(player2))
                .buildNode();

        if (activeRound.getWinningPlayer() != null) {
            node.putPOJO("winning_player", activeRound.getWinningPlayer());
        }
        return node;
    }

    private JsonNode getPlayerJson(PlayerGameState player) {
        return new JsonUtils.ObjectBuilder()
                .put("id", player.getId())
                .put("name", player.getName())
                .put("remote_ip", player.getPlayer().getIp())
                .put("is_connected", player.getPlayer().isConnected())
                .put("pawn1_stats", player.getPawn(PawnIndex.PAWN1).getStatsMap())
                .put("pawn1_status", player.getPawn(PawnIndex.PAWN1).getStatusEnums())
                .put("pawn2_stats", player.getPawn(PawnIndex.PAWN2).getStatsMap())
                .put("pawn2_status", player.getPawn(PawnIndex.PAWN2).getStatusEnums())
                .put("pawn3_stats", player.getPawn(PawnIndex.PAWN3).getStatsMap())
                .put("pawn3_status", player.getPawn(PawnIndex.PAWN3).getStatusEnums())
                .buildNode();
    }


    /* FOR TESTING */

    public long getInitTime() {
        return initTime;
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void setStarted() {
        System.out.println("started");
        round = 1;
        isStarted = true;
        activeRound = new ActiveRoundState(player1, player2, 1);
        isStarted = true;
    }

    public PlayerGameState getPlayer1() {
        return player1;
    }

    public PlayerGameState getPlayer2() {
        return player2;
    }

    public void setPlayer1(PlayerGameState player1) {
        this.player1 = player1;
    }

    public void setPlayer2(PlayerGameState player2) {
        this.player2 = player2;
    }

    public ActiveRoundState getActiveRound() {
        return activeRound;
    }

    public int getRound() {
        return round;
    }

    public ArrayBlockingQueue<NetGameAction> getPlayer1MsgQueue() {
        return player1MsgQueue;
    }

    public ArrayBlockingQueue<NetGameAction> getPlayer2MsgQueue() {
        return player2MsgQueue;
    }

    public void setActiveRound(ActiveRoundState round) {
        this.activeRound = round;
    }

    public void setRound(int i) {
        this.round = i;
    }


}
