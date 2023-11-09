package io.mindspice.okragameserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.databaseservice.client.schema.Card;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.core.Settings;
import io.mindspice.okragameserver.game.bot.BotFactory;
import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.cards.AbilityCard;
import io.mindspice.okragameserver.game.cards.ActionCard;
import io.mindspice.okragameserver.game.cards.PowerCard;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.PlayerAction;
import io.mindspice.okragameserver.game.enums.StatType;
import io.mindspice.okragameserver.game.gameroom.GameRoom;
import io.mindspice.okragameserver.game.gameroom.action.ActivePower;
import io.mindspice.okragameserver.game.gameroom.effect.ActiveEffect;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;
import io.mindspice.okragameserver.game.player.Player;
import io.mindspice.okragameserver.game.player.PlayerData;
import io.mindspice.okragameserver.schema.websocket.incoming.NetGameAction;
import io.mindspice.okragameserver.util.Log;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;


public class GameTests {
    private ScheduledExecutorService botExec = Executors.newScheduledThreadPool(2);
    private BotFactory botFactory = new BotFactory(botExec);
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(8);

    // Plays a full bot game. Debug log inputs/outputs. Settings.advancedDebug can be set to true
    // to also enable the stats of all pawns after each action and various other detailed logging.
    // Debugging the full integration is challenging, running advance debug logging provides enough information
    // to manually follow the actions and game flow to hopefully identify bug
    // To run quickly chance the delay time in BotPlayerStat doTurn to 0
    @Test
    public void runTestGame() throws InterruptedException {
        var activeGames = Collections.synchronizedCollection(new ArrayList<>(20));
     //   while (true) {
//            if (activeGames.size() >= 2) {
//                Thread.sleep(10000);
//                continue;
//            }
            TestPlayer p1 = new TestPlayer(ThreadLocalRandom.current().nextInt(99999));
            Settings.GET();
            TestPlayer p2 = new TestPlayer(ThreadLocalRandom.current().nextInt(99999));
            p1.setFullPlayerData(new PlayerData("Player1"));
            p2.setFullPlayerData(new PlayerData("Player2"));
            int rnd = ThreadLocalRandom.current().nextInt(130, 200);
            int lvl1 = (int) ThreadLocalRandom.current().nextDouble(0.93, 1.07) * rnd;
            int lvl2 = (int) ThreadLocalRandom.current().nextDouble(0.93, 1.07) * rnd;
            BotPlayerState player1 = botFactory.getBotPlayerStateForBotVsBot(p1, lvl1);
            BotPlayerState player2 = botFactory.getBotPlayerStateForBotVsBot(p2, lvl2);
            player1.setEnemyPlayer(player2);
            player2.setEnemyPlayer(player1);
            p1.setPlayerGameState(player1);
            p2.setPlayerGameState(player2);
            GameRoom game = new GameRoom(player1, player2);


            ScheduledFuture<?> gameProc = exec.scheduleWithFixedDelay(
                    game,
                    0,
                    200,
                    TimeUnit.MILLISECONDS
            );
            activeGames.add(gameProc);
            game.setReady(p1.getId());
            game.setReady(p2.getId());
            game.getResultFuture().thenAccept(result -> {
                gameProc.cancel(true);
                activeGames.remove(gameProc);
                System.out.println("Finsihed on Round:" + result.roundCount());
            }).exceptionally(ex -> {
                Log.SERVER.error(this.getClass(), "Error on finalize bot match callback");
                return null;
            });
       // }

        Thread.sleep(10000000);
    }

    public static class TestPlayer extends Player {
        PlayerGameState pgs;

        public TestPlayer(int id) {
            super(id, null);
        }

        public void setPlayerGameState(PlayerGameState psg) {
            this.pgs = psg;
        }

        @Override
        public void send(Object obj) {
            System.out.println("Msg Out | Id:" + pgs.getId());
            System.out.println(writePretty(obj));
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public void onMessage(NetGameAction msg) {
            if (inGame) {
                System.out.println("Action In | Id:" + pgs.getId());
                System.out.println(writePretty(msg));
                gameRoom.addMsg(id, msg);
            } else {
                Log.SERVER.debug(this.getClass(), getLoggable() + " | Websocket message while not in game");
            }
            // if they are not in a game ignore their WS messages
        }
    }


    public record ActionIn(
            PlayerAction action,
            Card cardPlayed,
            PawnIndex playingPawn,
            PawnIndex targetPawn
    ) {

    }


    public record PawnStats(
            PawnIndex pawnIndex,
            Map<StatType, Integer> stats,
            List<ActiveEffect> effects,
            List<ActivePower> powers,
            int statHash,
            int effectHash,
            ActionCard actionCard1,
            ActionCard actionCard2,
            AbilityCard abilityCard1,
            AbilityCard abilityCard2,
            PowerCard powerCard
    ) {
        public PawnStats(Pawn pawn) {
            this(
                    pawn.getIndex(),
                    pawn.getStatsMap(),
                    pawn.getStatusEffects(),
                    pawn.getActivePowers(),
                    pawn.getStatsHash(),
                    pawn.getEffectHash(),
                    pawn.getActionCard(1),
                    pawn.getActionCard(2),
                    pawn.getAbilityCard(1),
                    pawn.getAbilityCard(2),
                    pawn.getPowerCard()
            );
        }
    }

    public static String writePretty(Object obj) {
        try {
            return JsonUtils.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}





