package io.mindspice.okragameserver.game.gameroom;

import io.mindspice.okragameserver.core.Settings;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.gameroom.action.ActionReturn;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;
import io.mindspice.okragameserver.schema.websocket.outgoing.NetMsg;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.*;
import io.mindspice.okragameserver.util.Log;
import io.mindspice.okragameserver.util.gamelogger.GameLogger;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class NetCombatManager {

    protected final PlayerGameState player;
    protected final PlayerGameState enemy;
    protected final boolean doGameLog;
    protected final UUID roomId;

    public NetCombatManager(PlayerGameState player, PlayerGameState enemy) {
        this.player = player;
        this.enemy = enemy;
        doGameLog = Settings.GET().gameLogging;
        roomId = player.getPlayer().getGameRoom().getRoomId();

    }

    private String getInfo(List<PawnInterimState> pis, PlayerGameState tPlayer) {
        return pis.stream()
                .map(p -> tPlayer.getPawn(p.getPawnIndex()).getStatsLog())
                .collect(Collectors.joining("\n"));
    }

    public void doAction(ActionReturn actionToPlay) {
        assert (actionToPlay.playerPawnStates.get(0) != null);

        actionToPlay.doAction();
        sendTurnResponse(actionToPlay);

        if (actionToPlay.hasInsight) {
            send(new NetInsight(actionToPlay.insight), true);
        }
    }

//    public void doPotion(Pawn pawn) {
//        sendStatUpdate(pawn, true, true);
//        sendStatUpdate(pawn, false, false);
//        sendEffectUpdate(pawn, true);
//    }

    protected void sendTurnResponse(ActionReturn actionReturn) {
        var playerResponse = new NetTurnResponse(true);
        var enemyResponse = new NetTurnResponse(false);
        playerResponse.card_slot = actionReturn.action;
        enemyResponse.card_slot = actionReturn.action;

        if (actionReturn.isInvalid) {
            playerResponse.is_invalid = true;
            playerResponse.invalid_msg = actionReturn.invalidMsg.msg;
            playerResponse.action_pawn = actionReturn.playerPawnStates.get(0).getPawnIndex();
            send(playerResponse, true);
            return;
        }

        playerResponse.cost = actionReturn.cost;

        if (actionReturn.isFailed) {
            playerResponse.is_failed = true;
            enemyResponse.is_failed = true;
            playerResponse.action_pawn = actionReturn.playerPawnStates.get(0).getPawnIndex();
            enemyResponse.action_pawn = actionReturn.playerPawnStates.get(0).getPawnIndex();
            send(playerResponse, true);
            send(enemyResponse, false);
            return;
        }

        // Send originating pawn to both for attack animations
        playerResponse.action_pawn = actionReturn.playerPawnStates.get(0).getPawnIndex();
        enemyResponse.action_pawn = actionReturn.playerPawnStates.get(0).getPawnIndex();
        playerResponse.animation = actionReturn.animation;
        enemyResponse.animation = actionReturn.animation;

        // Players own pawns to self (full stats)
        var pPlayerAffected = actionReturn.playerPawnStates.stream()
                .filter(p -> !p.getActionFlags().isEmpty())
                .map(p -> new PawnResponse(
                                p.getPawnIndex(),
                                p.getActionFlags(),
                                p.getPawn().getNetStats(),
                                p.getPawn().getNetEffects(),
                                p.getPawn().getAndFlagIfDead()

                        )
                ).toList();
        // Players stats to enemy (only HP)
        var pEnemyAffected = actionReturn.targetPawnStates.stream()
                .filter(p -> !p.getActionFlags().isEmpty())
                .map(p -> new PawnResponse(
                                p.getPawnIndex(),
                                p.getActionFlags(),
                                p.getPawn().getHp(),
                                p.getPawn().hasStatusInsight() ? p.getPawn().getNetEffects() : List.of(),
                                p.getPawn().getAndFlagIfDead()
                        )
                ).toList();

        // If there are no affected(flagged) pawns then action failed due to luck
        if (pPlayerAffected.isEmpty() && pEnemyAffected.isEmpty()) {
            playerResponse.is_failed = true;
            enemyResponse.is_failed = true;
            playerResponse.action_pawn = actionReturn.playerPawnStates.get(0).getPawnIndex();
            enemyResponse.action_pawn = actionReturn.playerPawnStates.get(0).getPawnIndex();
            send(playerResponse, true);
            send(enemyResponse, false);
            return;
        }

        playerResponse.affected_pawns_player = pPlayerAffected;
        playerResponse.affected_pawns_enemy = pEnemyAffected;

        // Enemies own pawns sent to them (full stats)
        var ePlayerAffected = actionReturn.targetPawnStates.stream()
                .filter(p -> !p.getActionFlags().isEmpty())
                .map(p -> new PawnResponse(
                                p.getPawnIndex(),
                                p.getActionFlags(),
                                p.getPawn().getNetStats(),
                                p.getPawn().getNetEffects(),
                                p.getPawn().getAndFlagIfDead()

                        )
                ).toList();

        // Enemies pawns set to player (only hp)
        var eEnemyAffected = actionReturn.playerPawnStates.stream()
                .filter(p -> !p.getActionFlags().isEmpty())
                .map(p -> new PawnResponse(
                                p.getPawnIndex(),
                                p.getActionFlags(),
                                p.getPawn().getHp(),
                                p.getPawn().hasStatusInsight() ? p.getPawn().getNetEffects() : List.of(),
                                p.getPawn().getAndFlagIfDead()
                        )
                ).toList();
        enemyResponse.affected_pawns_player = ePlayerAffected;
        enemyResponse.affected_pawns_enemy = eEnemyAffected;

        // Send dead if occurred and not yet sent
        sendDead();
        send(playerResponse, true);
        send(enemyResponse, false);
        //  sendPlayableCards();
    }

    public void sendCardUpdate(boolean isFirst) {
        var cardUpdateSelf = new NetCardUpdate(true);

        // Get card hand for living pawns; Full card hand if first
        player.getLivingPawns().forEach(p -> cardUpdateSelf.setHand(p.getIndex(), p.getCardHand(isFirst)));

        // Send the visible enemy cards to the player if it is first round
        if (isFirst) {
            var cardUpdateEnemy = new NetCardUpdate(false);
            enemy.getLivingPawns().forEach(p -> cardUpdateEnemy.setHand(p.getIndex(), p.getEnemyCardHand()));
            send(cardUpdateEnemy, true);
        }
        send(cardUpdateSelf, true);
        // sendPlayableCards();
    }

    public void sendEffects() {
        var playerPawns = player.getLivingPawns();
        var enemyPawns = player.getLivingPawns();

        var playerEffects = new NetEffect(true);
        var enemyEffects = new NetEffect(false);
        playerPawns.forEach(p -> playerEffects.setEffects(p.getIndex(), p.getNetEffects()));
        enemyPawns.forEach(e -> {
            if (e.hasStatusInsight()) { enemyEffects.setEffects(e.getIndex(), e.getNetEffects()); }
        });

        if (!playerEffects.isEmpty()) { send(playerEffects, true); }
        if (!enemyEffects.isEmpty()) { send(enemyEffects, true); }

    }

    public void sendStats(List<Pawn> playerPawns) {
        var toPlayer = new NetStat(true);
        var toEnemy = new NetStat(false);

        for (var pawn : playerPawns) {
            toPlayer.setStats(pawn.getIndex(), pawn.getNetStats());
            toEnemy.setStats(pawn.getIndex(), pawn.getHp());
        }
        send(toPlayer, true);
        send(toEnemy, false);
    }

    // Called only on first round init called once independently on each player
    public void sendFirstRoundInfo(List<Pawn> playerPawns, List<Pawn> enemyPawns) {
        var playerStats = new NetStat(true);
        var enemyStats = new NetStat(false);
        var playerEffects = new NetEffect(true);

        // Send the full starting stats/effects and the enemies hp to the player
        for (int i = 0; i < 3; ++i) {
            switch (playerPawns.get(i).getIndex()) {
                case PAWN1 -> {
                    playerStats.pawn_1 = playerPawns.get(i).getNetStats();
                    playerEffects.pawn_1 = playerPawns.get(i).getNetEffects();
                    enemyStats.pawn_1 = enemyPawns.get(i).getHp();
                }
                case PAWN2 -> {
                    playerStats.pawn_2 = playerPawns.get(i).getNetStats();
                    playerEffects.pawn_2 = playerPawns.get(i).getNetEffects();
                    enemyStats.pawn_2 = enemyPawns.get(i).getHp();
                }
                case PAWN3 -> {
                    playerStats.pawn_3 = playerPawns.get(i).getNetStats();
                    playerEffects.pawn_3 = playerPawns.get(i).getNetEffects();
                    enemyStats.pawn_3 = enemyPawns.get(i).getHp();
                }
            }
        }
        send(playerStats, true);
        send(enemyStats, true);
        send(playerEffects, true);
    }

    // This is called on the start of a new turn on both players independently
    // Gives active player their active pawnLoadouts, inactive players are alerted to the state change
    // Also used to send stat and effects updates to both players, as they can/do change at turn start
    public void sendTurnUpdate(List<PawnIndex> activePawns, boolean isPlayersTurn) {
        var turnUpdate = new NetTurnUpdate(isPlayersTurn);

        if (isPlayersTurn) {
            activePawns.forEach(p -> turnUpdate.setActive(p, true));
        }

        sendDead();
        sendEffects();
        sendStats(player.getPawns());
        send(turnUpdate, true);
    }

    protected void sendDead() {
        var playerPawns = player.getPawns();
        var enemyPawns = enemy.getPawns();

        playerPawns.stream().filter(p -> p.isDead() && !p.haveSentDead()).forEach(p -> {
            p.setSentDead();
            send(new NetDead(p.getIndex(), true), true);
            send(new NetDead(p.getIndex(), false), false);
        });

        enemyPawns.stream().filter(p -> p.isDead() && !p.haveSentDead()).forEach(p -> {
            p.setSentDead();
            send(new NetDead(p.getIndex(), true), false);
            send(new NetDead(p.getIndex(), false), true);
        });
    }

    public void sendGameRestoreInfo(List<PawnIndex> activePawns, boolean isPlayerTurn) {
        sendFirstRoundInfo(player.getPawns(), enemy.getPawns());
        sendCardUpdate(true);
        sendTurnUpdate(activePawns, isPlayerTurn);
    }

    public void sendRoundUpdate(int round) {
        send(new NetRound(round), true);
    }

    public void sendGameRestore() {
        send(new NetRestoreGame(player.getRoomId().toString()), true);
    }

    // Se can send the object directly to the send method where it is serialized
    // to json and sent as TextMessage<msg> to the client
    protected void send(NetMsg msg, boolean toPlayer) {
        try {
            if (toPlayer) {
                player.send(msg);
                if (doGameLog) { GameLogger.GET().addMsgOut(roomId, player.getId(), msg); }
            } else {
                enemy.send(msg);
                if (doGameLog) { GameLogger.GET().addMsgOut(roomId, enemy.getId(), msg); }
            }
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "GameRoom: " + player.getPlayer().getGameRoom().getRoomId() + " | PlayerId: "
                    + (toPlayer ? player.getId() : enemy.getId()) + " | Connected: "
                    + (toPlayer ? player.getPlayer().isConnected() : enemy.getPlayer().isConnected()), e);
        }
    }

    // Se can send the object directly to the send method where it is serialized
    // to json and send as TextMessage<msg> to the client

}
