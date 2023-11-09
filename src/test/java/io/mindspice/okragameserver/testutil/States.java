package io.mindspice.okragameserver.testutil;

import io.mindspice.okragameserver.GameTests;
import io.mindspice.okragameserver.game.bot.BotFactory;
import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.gameroom.GameRoom;
import io.mindspice.okragameserver.game.player.PlayerData;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class States {

    public static GameRoom getReadiedGameRoom() {
        ScheduledExecutorService botExec = Executors.newScheduledThreadPool(2);
        BotFactory botFactory = new BotFactory(botExec);
        GameTests.TestPlayer p1 = new GameTests.TestPlayer(1);
        GameTests.TestPlayer p2 = new GameTests.TestPlayer(2);
        p1.setFullPlayerData(new PlayerData("Player1"));
        p2.setFullPlayerData(new PlayerData("Player2"));
        BotPlayerState player1 = botFactory.getBotPlayerStateForBotVsBot(p1, 150);
        BotPlayerState player2 = botFactory.getBotPlayerStateForBotVsBot(p2, 150);
        player1.setEnemyPlayer(player2);
        player2.setEnemyPlayer(player1);
        p1.setPlayerGameState(player1);
        p2.setPlayerGameState(player2);
        GameRoom game = new GameRoom(player1, player2);
        game.setReady(1);
        game.setReady(2);
        return game;
    }
}
