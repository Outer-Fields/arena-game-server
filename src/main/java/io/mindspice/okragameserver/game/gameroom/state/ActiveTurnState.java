package io.mindspice.okragameserver.game.gameroom.state;

import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.gameroom.action.ActionFactory;
import io.mindspice.okragameserver.game.gameroom.action.ActionReturn;
import io.mindspice.okragameserver.game.gameroom.effect.ActiveEffect;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.schema.websocket.incoming.NetGameAction;
import io.mindspice.okragameserver.util.Log;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ActiveTurnState {

    public final PlayerGameState activePlayer;
    public final PlayerGameState enemyPlayer;
    private final ActionFactory actionFactory;
    private volatile long turnInitTime;
    private final List<PawnTurnState> pawnTurnStates = new ArrayList<>();
    private volatile long lastActionTime;

    public ActiveTurnState(PlayerGameState activePlayer, PlayerGameState enemyPlayer, int round) {
        this.activePlayer = activePlayer;
        this.enemyPlayer = enemyPlayer;
        actionFactory = new ActionFactory(activePlayer, enemyPlayer); //TODO this can be instanced once for each player at game start

        Log.SERVER.debug(this.getClass(), "GameRoom: " + activePlayer.getPlayer().getGameRoom().getRoomId()
                + " | Player: " + activePlayer.getId() + " Starting Turn"
                + " | Round: " + activePlayer.getPlayer().getGameRoom().getRound());

        // Add active pawnLoadouts regenerate their stats
        enemyPlayer.getLivingPawns().forEach(p -> p.setActive(false));

        for (Pawn pawn : activePlayer.getPawns()) {
            if (!pawn.isDead()) {
                pawnTurnStates.add(new PawnTurnState(pawn));
                if (round != 1) pawn.regenerate();
            }
        }

        updatePawnStatusEffects();
        // send turn updates

        List<PawnIndex> activePawns = pawnTurnStates.stream()
                .filter(PawnTurnState::isActive)
                .map(PawnTurnState::getIndex)
                .toList();

        activePlayer.getCombatManager().sendTurnUpdate(activePawns, true);
        enemyPlayer.getCombatManager().sendTurnUpdate(null, false);

        turnInitTime = Instant.now().getEpochSecond();
        lastActionTime = Instant.now().getEpochSecond();
    }

    private void updatePawnStatusEffects() {
        for (PawnTurnState pTurnState : pawnTurnStates) {
            Iterator<ActiveEffect> effectItr = pTurnState.getPawn().getStatusEffects().iterator();

            while (effectItr.hasNext()) {
                ActiveEffect effect = effectItr.next();
                if (!effect.update()) {
                    effectItr.remove();
                    continue;
                }

                // "Chance" effects are applied here, they either roll off or are applied if their chance hits
                boolean effectState = effect.doEffect();
                switch (effect.getType()) {
                    case PARALYSIS -> { pTurnState.setParalyzed(effectState); }
                    case CONFUSION -> { pTurnState.setConfused(effectState); }
                    case SLEEP -> { if (effectState) { pTurnState.setActive(false); } }
                }
            }
        }
    }



    public List<PawnIndex> getActivePawns() {
        return pawnTurnStates.stream()
                .filter(PawnTurnState::isActive)
                .map(PawnTurnState::getIndex)
                .toList();
    }

    /*
     * These methods are called by the packet handler in game room to direct action inputs from the packet queue
     * The packet queue performs initial authoritative validation as to make sure the action packets are from the active player
     * Before an action is performed these methods first check if the pawn is active, then check if action is valid/allowed
     */
    public void doAction(NetGameAction nga) {
        if (!isValidTurn(nga.playerPawn())) {
            Log.ABUSE.info("PlayerId: " + getActivePlayerId() + " | Attempted to player action from non-active pawn");
            Log.SERVER.debug(this.getClass(), "PlayerId: " + getActivePlayerId() + " | Attempted to player action from non-active pawn");
            return;
        }

        if (isTurnOver()) {
            Log.SERVER.debug(this.getClass(), "PlayerId: " + getActivePlayerId() + " | Action sent after turn end");
            return;
        }

        lastActionTime = Instant.now().getEpochSecond();
        ActionReturn actionReturn = null;
        switch (nga.action()) {
            case WEAPON_CARD_1 -> actionReturn = actionFactory.attackWeapon1(nga.playerPawn(), nga.targetPawn());
            case WEAPON_CARD_2 -> actionReturn = actionFactory.attackWeapon2(nga.playerPawn(), nga.targetPawn());
            case ACTION_CARD_1 -> actionReturn = actionFactory.playActionCard1(nga.playerPawn(), nga.targetPawn());
            case ACTION_CARD_2 -> actionReturn = actionFactory.playActionCard2(nga.playerPawn(), nga.targetPawn());
            case ABILITY_CARD_1 -> actionReturn = actionFactory.playAbilityCard1(nga.playerPawn(), nga.targetPawn());
            case ABILITY_CARD_2 -> actionReturn = actionFactory.playAbilityCard2(nga.playerPawn(), nga.targetPawn());
            // case POTION -> actionReturn = actionFactory.consumePotion(nga.player_pawn, nga.potionCard);
            case SKIP_PAWN -> {
                disablePawn(nga.playerPawn());
                return;
            }
            case END_TURN -> {
                pawnTurnStates.forEach(p -> p.setActive(false));
                return;
            }
        }

        ;
        // actionReturn should never be null unless abuse was attempted, or a logic error
        if (actionReturn != null) {
            actionReturn.setAction(nga.action()); // PlayerAction sent to disable card slot in client
            // Confusion/Paralysis must come first as these mutate the ActionReturn state
            if (getTurnState(nga.playerPawn()).isConfused()) { actionReturn.doConfusion(); }
            if (getTurnState(nga.playerPawn()).isParalyzed()) { actionReturn.doParalysis(); }
            if (!actionReturn.isInvalid) { disablePawn(nga.playerPawn()); }
            activePlayer.getCombatManager().doAction(actionReturn);
        } else {
            Log.SERVER.error(this.getClass(), "GameRoom: " + activePlayer.getPlayer().getGameRoom().getRoomId()
                    + " | NULL action return for PlayerId: " + activePlayer.getId());
            Log.ABUSE.info(this.getClass(), "GameRoom: " + activePlayer.getPlayer().getGameRoom().getRoomId()
                    + " | NULL action return for PlayerId: " + activePlayer.getId());
        }
    }

    public boolean isTurnOver() {
        if (turnInitTime + 35 <= Instant.now().getEpochSecond()) {
            return true;
        }

        for (PawnTurnState pawn : pawnTurnStates) {
            if (pawn.isActive()) {
                return false;
            }
        }
        return true;
    }

    public void adjustRoundTime() {
        long delta = lastActionTime - turnInitTime;
        turnInitTime = Instant.now().getEpochSecond() - delta;
    }

    public int getTimeLeft() {
        return (int) ((turnInitTime + 35) - Instant.now().getEpochSecond());
    }

    public void sendActiveInfo() {

        // activePlayer.getCombatManager().sendPlayableCards();
    }

    public boolean isValidTurn(PawnIndex pawnIndex) {
        for (PawnTurnState pawn : pawnTurnStates) {
            if (pawn.getIndex() == pawnIndex) {
                return pawn.isActive();
            }
        }
        return false;
    }

    private void disablePawn(PawnIndex pawnIndex) {
        for (PawnTurnState pawn : pawnTurnStates) {
            if (pawn.getIndex() == pawnIndex) {
                pawn.setActive(false);
            }
        }
    }

    private PawnTurnState getTurnState(PawnIndex pawnIndex) {
        for (PawnTurnState pawn : pawnTurnStates) {
            if (pawn.getIndex() == pawnIndex) {
                return pawn;
            }
        }
        throw new IllegalStateException("Null Turn state, Error in game logic");
    }

    private Pawn getPawn(PawnIndex pawnIndex) {
        for (PawnTurnState pawn : pawnTurnStates) {
            if (pawn.getIndex() == pawnIndex) {
                return pawn.getPawn();
            }
        }
        throw new IllegalStateException("Null Turn state, Error in game logic");
    }

    public int getActivePlayerId() {
        return activePlayer.getId();
    }

    public long getLastActionTime() {
        return lastActionTime;
    }

    /* FOR TESTING */

    public PlayerGameState getActivePlayer() {
        return activePlayer;
    }

    public PlayerGameState getEnemyPlayer() {
        return enemyPlayer;
    }

    public ActionFactory getActionFactory() {
        return actionFactory;
    }

    // public long getTurnInitTime() {return turnInitTime;}
    public List<PawnTurnState> getPawnTurnStates() {
        return pawnTurnStates;
    }

    public PawnTurnState getPawnTurnState(PawnIndex index) {
        for (PawnTurnState p : pawnTurnStates) {
            if (p.getIndex() == index) return p;
        }
        return null;
    }


}


