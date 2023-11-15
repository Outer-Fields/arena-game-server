package io.mindspice.okragameserver.game.bot;


import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;
import io.mindspice.okragameserver.game.player.Player;
import io.mindspice.okragameserver.game.player.PlayerData;
import io.mindspice.okragameserver.schema.PawnSet;

import java.util.concurrent.ScheduledExecutorService;


public class BotFactory {

    private final ScheduledExecutorService botExecutor;

    public BotFactory(ScheduledExecutorService botExecutor) {
        this.botExecutor = botExecutor;
    }

    public BotPlayerState getBotPlayerState(PlayerGameState enemyPlayerState, int enemyPawnSetLevel) {
        PawnSet botPawnSet = PawnSet.getRandomPawnSet(enemyPawnSetLevel);
        BotPlayer botPlayer = new BotPlayer();
        botPlayer.setFullPlayerData(new PlayerData("Bot"));
        return new BotPlayerState(botPlayer, enemyPlayerState, botPawnSet, botExecutor);
    }
    public BotPlayerState getHighLvlBotPlayerState(PlayerGameState enemyPlayerState) {
        PawnSet botPawnSet = PawnSet.getRandomPawnSet2();
        BotPlayer botPlayer = new BotPlayer();
        botPlayer.setFullPlayerData(new PlayerData("Bot"));
        return new BotPlayerState(botPlayer, enemyPlayerState, botPawnSet, botExecutor);
    }

    public BotPlayerState getBotPlayerStateForBotVsBot(Player botPlayer, int enemyPawnSetLevel) {
        PawnSet botPawnSet = PawnSet.getRandomPawnSet(enemyPawnSetLevel);
        return new BotPlayerState(botPlayer, botPawnSet, botExecutor);
    }



}