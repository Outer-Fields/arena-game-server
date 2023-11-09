package io.mindspice.okragameserver.core.configuration;

import io.mindspice.databaseservice.client.DBServiceClient;
import io.mindspice.databaseservice.client.api.OkraGameAPI;
import io.mindspice.okragameserver.core.GameServer;
import io.mindspice.okragameserver.core.HttpServiceClient;
import io.mindspice.okragameserver.core.Settings;
import io.mindspice.okragameserver.game.bot.BotFactory;
import io.mindspice.okragameserver.game.player.Player;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;


@Configuration
public class Initialization {

    /* Handle some initialization settings */

//    @Bean
//    public CommandLineRunner customInitialization() {
//        return args -> {
//            ObjectMapper mapper = JsonUtils.getMapper();
//            mapper.disable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
//        };
//    }
//

    /* Core Player Data */

    @Bean
    ConcurrentHashMap<Integer, Player> playerTable() {
        return new ConcurrentHashMap<>(100);
    }

    /* Core Game Services */

    @Bean
    ScheduledExecutorService gameExecutor() {
        return new ScheduledThreadPoolExecutor(Settings.GET().gameExecThreads);
    }

    @Bean
    ScheduledExecutorService botExecutor() {
        return new ScheduledThreadPoolExecutor(Settings.GET().botExecThreads);
    }

    @Bean
    BotFactory botFactory(@Qualifier("botExecutor") ScheduledExecutorService botExec) {
        return new BotFactory(botExec);
    }

    @Bean
    DBServiceClient dbServiceClient() throws Exception {
        return new DBServiceClient(Settings.GET().dbURI, Settings.GET().dbUser, Settings.GET().dbPass);
    }

    @Bean
    OkraGameAPI gameApi(@Qualifier("dbServiceClient") DBServiceClient dbServiceClient) {
        return new OkraGameAPI(dbServiceClient);
    }

    @Bean
    HttpServiceClient httpServiceClient(@Qualifier("gameApi") OkraGameAPI gameApi) {
        return new HttpServiceClient(gameApi);
    }

    @Bean
    GameServer gameServer(
            @Qualifier("gameExecutor") ScheduledExecutorService gameExec,
            @Qualifier("botFactory") BotFactory botFactory,
            @Qualifier("httpServiceClient") HttpServiceClient httpServiceClient,
            @Qualifier("playerTable") ConcurrentHashMap<Integer, Player> playerTable) {
        GameServer server = new GameServer(gameExec, botFactory, httpServiceClient, playerTable);
        server.init();
        return server;
    }

    /* Network Services */

//    @Bean
//    GameServerSocketHandler gameServerSocketHandler(
//            @Qualifier("playerTable") ConcurrentHashMap<Integer, Player> playerTable,
//            @Qualifier("gameApi") OkraGameAPI gameApi) {
//        return new GameServerSocketHandler(playerTable, gameApi);
//    }
//
//    @Bean
//    TokenHandshakeInterceptor tokenHandshakeInterceptor(
//            @Qualifier("httpServiceClient") HttpServiceClient httpServiceClient) {
//        return new TokenHandshakeInterceptor(httpServiceClient);
//    }

//    @Bean GameRestController gameRestController(
//            @Qualifier("httpServiceClient") HttpServiceClient httpServiceClient,
//            @Qualifier("gameServer") GameServer gameServer,
//            @Qualifier("playerTable") ConcurrentHashMap<Integer, Player> playerTable) {
//        return new GameRestController(httpServiceClient,gameServer, playerTable);
//    }


}
