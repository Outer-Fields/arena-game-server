package io.mindspice.okragameserver.game.gameroom.state;


/*
 * Not much logic is needed for the update function
 * It first calls isTurnOver() to see if current active turn is complete
 * If it is still first turn, isTurnOver will initiate the next turn if first is over setting firstOver = true;
 * If the second turn is over it will set secondOver = true;
 *
 * Next isGameOver() checks both players to see if all of their pawnLoadouts are dead
 * If they are method sets winners to the winners PlayerGameState and returns GAMEOVER
 * If GAMEOVER is returned to gameRoom, it grabs the reference from the round state and initiates logic gameover();
 *
 *
 *
 *
 * After which method calls isRoundOver() which checks if both turns have been played
 * If isRoundOver() returns true, then the method returns FINISHED to gameRoom, so it can initiate the next round
 * If isRoundOver returns false, method return ACTIVE to game room to continue the same turn update next tick
 *
 * All the interactions that advance a turns internal state are directed through roundState to the active turn
 * These interactions are provided through the packet queue in GameRoom
 * The only logic update() is doing is checking for a finished state of the activeTurn either by time up or being fully played
 * As well as checking if both turns have been played
 *
 * All update() does is advance the games progression and check for winning conditions
 */

import io.mindspice.okragameserver.util.Log;
import io.mindspice.okragameserver.util.gamelogger.GameLogger;

import java.time.Instant;


public class ActiveRoundState {
    private final int round;
    public final PlayerGameState player1;
    public final PlayerGameState player2;
    private volatile ActiveTurnState activeTurn;
    private volatile boolean firstOver = false;
    private volatile boolean secondOver = false;
    private volatile PlayerGameState winningPlayer;


    public enum RoundState {
        ACTIVE,
        FINISHED,
        AWAITING_FINISH,
        GAME_OVER
    }

    public ActiveRoundState(PlayerGameState player1, PlayerGameState player2, int round) {
        this.player1 = player1;
        this.player2 = player2;
        this.round = round;
        newTurn(true);
        player1.getCombatManager().sendRoundUpdate(round);
        player2.getCombatManager().sendRoundUpdate(round);
    }

    // If it is 1st turn of the round, check if it is a re-deal round then init 1st turn, with player 1 active
    // If not (2nd turn of round) then init 2nd round with player 2 as active
    private void newTurn(boolean isFirstTurn) {

        if (round == 1) {
            player1.getCombatManager().sendFirstRoundInfo(player1.getPawns(), player2.getPawns());
            player2.getCombatManager().sendFirstRoundInfo(player2.getPawns(), player1.getPawns());
        }

        if (isFirstTurn) {
            activeTurn = new ActiveTurnState(player1, player2, round);
            if (round % 18 == 0) {
                activeTurn.getActivePlayer().resetHand();
                activeTurn.getEnemyPlayer().resetHand();
                Log.SERVER.debug(this.getClass(), "GameRoom: " + player1.getPlayer().getGameRoom().getRoomId()
                        + " | Reset Deck, and dealt new hands");
            }
            if (round == 1 || round % 6 == 0) {
                activeTurn.getActivePlayer().dealNewHands(round == 1);
                activeTurn.getEnemyPlayer().dealNewHands(round == 1);
                Log.SERVER.debug(this.getClass(), "GameRoom: " + player1.getPlayer().getGameRoom().getRoomId()
                        + " | Dealt new hand");
            }
            if (player1.getPlayer().getGameRoom().isDoGameLog()) { GameLogger.GET().addRoundRecord(player1, player2); }
        } else {
            activeTurn = new ActiveTurnState(player2, player1, round);
        }
    }

    public RoundState update() {
        if (isGameOver()) {
            return RoundState.GAME_OVER;
        }
        if (activeTurn.isTurnOver()) {
            if (activeTurn.activePlayer == player1) { // Wait 3 sec for animations to clear
                if (Instant.now().getEpochSecond() - activeTurn.getLastActionTime() > 3) {
                    firstOver = true;
                    newTurn(false);
                } else {
                    return RoundState.AWAITING_FINISH;
                }
            } else {
                secondOver = true;
            }
        }
        if (firstOver && secondOver) {
            // Wait 3 sec for animations to clear
            if (Instant.now().getEpochSecond() - activeTurn.getLastActionTime() > 3) {
                return RoundState.FINISHED;
            } else {
                return RoundState.AWAITING_FINISH;
            }

        } else {
            return RoundState.ACTIVE;
        }
    }

    public boolean isRoundOver() {
        return firstOver && secondOver;
    }

    // TODO might want to add a tie state
    private boolean isGameOver() {
        if (player1.lossCheck()) {
            winningPlayer = player2;
            return true;
        }
        if (player2.lossCheck()) {
            winningPlayer = player1;
            return true;
        }
        return false;
    }

    public boolean isFirstOver() { return firstOver; }

    public int getActivePlayerId() {
        return activeTurn.getActivePlayerId();
    }

    public ActiveTurnState getActiveTurn() {
        return activeTurn;
    }

    public int getTurnTimeLeft() {
        return activeTurn.getTimeLeft();
    }


    /* FOR TESTING */

    public int getRound() { return round; }

    public PlayerGameState getPlayer1() { return player1; }

    public PlayerGameState getPlayer2() { return player2; }



    public boolean isSecondOver() { return secondOver; }

    public PlayerGameState getWinningPlayer() { return winningPlayer; }
}
