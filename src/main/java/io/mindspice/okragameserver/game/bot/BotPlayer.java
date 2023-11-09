package io.mindspice.okragameserver.game.bot;

import io.mindspice.databaseservice.client.schema.PlayerFunds;
import io.mindspice.okragameserver.game.player.Player;
import io.mindspice.okragameserver.game.player.PlayerData;

import java.util.concurrent.ThreadLocalRandom;


public class BotPlayer extends Player {
    public BotPlayer() {
        super(12, true);
    }

    @Override
    public void send(Object obj) {
        // Not used for the bot player
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
