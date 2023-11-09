package io.mindspice.okragameserver.game.bot.state;

import io.mindspice.okragameserver.game.bot.BotPlayer;
import io.mindspice.okragameserver.game.bot.BotTurn;
import io.mindspice.okragameserver.game.bot.behavior.BehaviorGraph;
import io.mindspice.okragameserver.game.player.Player;
import io.mindspice.okragameserver.schema.PawnSet;
import io.mindspice.okragameserver.schema.websocket.incoming.NetGameAction;
import io.mindspice.okragameserver.game.enums.*;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.NetTurnResponse;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.NetTurnUpdate;
import io.mindspice.okragameserver.util.Log;
import io.mindspice.okragameserver.game.gameroom.GameRoom;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;


// Must be added to the game room after the remote player has readied and submitted their PawnSet
public class BotPlayerState extends PlayerGameState {
    private PlayerGameState enemyState;
    private ScheduledExecutorService botExecutor;

    // Main constructor for bot vs player games
    public BotPlayerState(BotPlayer botPlayer, PlayerGameState enemyPlayer, PawnSet pawnSet,
            ScheduledExecutorService botExecutor) {
        super(botPlayer, pawnSet);
        this.enemyState = enemyPlayer;
        this.botExecutor = botExecutor;
    }

    // Used for bot vs bot games, where the enemyState is set manually after construction,
    // since they both need to reference each other
    public BotPlayerState(PawnSet pawnSet, ScheduledExecutorService botExecutor) {
        super(new BotPlayer(), pawnSet);
        this.botExecutor = botExecutor;
    }

    public BotPlayerState(Player botPlayer, PawnSet pawnSet, ScheduledExecutorService botExecutor) {
        super(botPlayer, pawnSet);
        this.botExecutor = botExecutor;
    }

    public void setEnemyPlayer(PlayerGameState enemyPlayer) {
        this.enemyState = enemyPlayer;
    }

    // Do the bots turns and schedule them. Delay is incremented, instead of reset so message don't overlap,
    // a random interval is used to make bots turn times seem more real and not be instant.
    // Bot turn traverses the behavior graph and then use the sendFauxMsg method the add message to the queue
    // Could use a callback, but it already needs a "this" reference to access the bots player state.
    public void doTurn(List<PawnIndex> activePawns) {
        var delay = ThreadLocalRandom.current().nextInt(4);
        for (PawnIndex pi : activePawns) {
            if (getPlayer().getGameRoom().isGameOver()) { return; }
            BotTurn turn = new BotTurn(this, pi);
            botExecutor.schedule(turn, delay, TimeUnit.SECONDS);
            delay += ThreadLocalRandom.current().nextInt(2,9);
        }
    }

    // This is where the bot intercepts the messages sent to it from the NetCombatManger
    // Nothing need to be done as all state is read directly from the bots state
    // Turn update message is used to trigger the bot to do its turn, which get added directly
    // to the message queue
    @Override
    public void send(Object obj) {
        if (obj instanceof NetTurnUpdate ntu) {
            if (ntu.isPlayer) {
                List<PawnIndex> activePawns = new ArrayList<>(3);
                if (ntu.pawn_1) { activePawns.add(PawnIndex.PAWN1); }
                if (ntu.pawn_2) { activePawns.add(PawnIndex.PAWN2); }
                if (ntu.pawn_3) { activePawns.add(PawnIndex.PAWN3); }
                doTurn(activePawns);
            }
        }

        if (obj instanceof NetTurnResponse ntr) {
            if (ntr.isPlayer && ntr.is_invalid) {
                BehaviorGraph.getInstance().doRandom(
                        this, new TreeFocusState(), ntr.action_pawn
                );
            }
        }
    }

    public void doAction(PlayerAction action, PawnIndex playerIndex, PawnIndex enemyIndex) {
        //    System.out.println("Doing Action: " + action + "\t| " + playerIndex + "->" +enemyIndex);
        NetGameAction nga = new NetGameAction(
                action,
                playerIndex,
                enemyIndex
        );
        sendFauxMsg(nga);
    }

    //FIXME potion logic needs removed, can just comment out from decision tree?
//    public void doPotion(PotionCard potionCard, PawnIndex playerIndex) {
//        NetGameAction nga = new NetGameAction();
//        nga.action = PlayerAction.POTION;
//        nga.player_pawn = playerIndex;
//        //nga.potionCard = potionCard;
//        sendFauxMsg(nga);
//    }

    // fake bot response, add message directly to gameroom queue
    public void sendFauxMsg(NetGameAction nga) {
        try {
            super.getPlayer().getGameRoom().addMsg(super.getId(), nga);
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "Error Adding Bot Msg TO Queue On BotPlayerId: " + getId(), e);
        }
    }

    public GameRoom getGameRoom() {
        return super.getPlayer().getGameRoom();
    }

    public PlayerGameState getEnemyState() {
        return enemyState;
    }


}