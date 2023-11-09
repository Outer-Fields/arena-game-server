package io.mindspice.okragameserver.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.mindspice.okragameserver.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class Settings {
    private static Settings INSTANCE;

    /* Server Settings */
    public volatile boolean isPaused;
    public volatile int botExecThreads;
    public volatile int lobbyExecThreads;
    public volatile int gameExecThreads;
    public volatile String pausedMsg;
    public volatile int queueSetLevelDifferential = 35;
    public volatile boolean advancedDebug;
    public volatile boolean gameLogging;
    public volatile String gameLogPath;

    /* DataBase */
    public volatile String dbURI;
    public volatile String dbUser;
    public volatile String dbPass;

    /* Admin*/
    public volatile int adminPort;
    public volatile String adminPass;
    public volatile String adminCert;
    public volatile String adminCertPass;

    /* Internal Services */

    // Auth
    public volatile String authServiceUri;
    public volatile int authServicePort;
    public volatile String authServiceUser;
    public volatile String authServicePass;

    // Item
    public volatile String itemServiceUri;
    public volatile int itemServicePort;
    public volatile String itemServiceUser;
    public volatile String itemServicePass;

    // limits
    public volatile long largeReqFreq = 10;
    public volatile long queueReqFreq = 30;
    public volatile long wsMsgWindow = 30;
    public volatile long wsMsgLimit = 20;
    public volatile long restMsgWindow = 30;
    public volatile long restMsgLimit = 10;

    // Desk Limits
    public volatile int maxActionDeckLevel;
    public volatile int[] actionDeckBounds;
    public volatile int maxAbilityDeckLevel;
    public volatile int[] abilityDeckBounds;
    public volatile int maxPowerDeckLevel;
    public volatile int[] powerDeckBounds;

    // Logging
    public volatile boolean logBotActions = false;
    public volatile int botQueueWait = 60;
    public volatile long queueCoolDown = 60;

    // Free games
    public volatile int maxFreeGames;
    public volatile boolean freeGameRewards;
    public volatile boolean freeGameBotOnly;

    // Notification
    public volatile List<List<String>> projectMessages;
    public volatile List<List<String>> projectAds;
    public volatile List<List<String>> otherAds;

    static {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();

        File file = new File("config.yaml");

        try {
            INSTANCE = mapper.readValue(file, Settings.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file.", e);
        }


    }

    public static void reload() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();

        File file = new File("config.yaml");

        try {
            INSTANCE = mapper.readValue(file, Settings.class);
        } catch (IOException e) {
            Log.SERVER.error(Settings.class, " Error reloading config: " , e);
        }
    }



    private Settings() {

//        botExecThreads = 2;
//        lobbyExecThreads = 2;
//        gameRoomExecThreads = 4;
//        adminCert = "/mnt/WinDrive/_JavaWorkSpace/_Projects/Okra_Server_Maven/src/main/resources/certs/okra_keystore.p12";
//        adminCertPass = "testpass";
//        adminPort = 20751;
//        adminPass = "testpass";
    }

    public static Settings GET() {
        return INSTANCE;
    }

    public static void writeBlank() throws IOException {
        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        File yamlFile = new File("defaults.yaml");
        mapper.writeValue(yamlFile, new Settings());
    }


}
