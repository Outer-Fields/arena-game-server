package io.mindspice.okragameserver.core.websocket;

import io.mindspice.databaseservice.client.schema.PlayerFunds;
import io.mindspice.databaseservice.client.schema.PlayerInfo;
import io.mindspice.databaseservice.client.schema.Results;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.core.HttpServiceClient;
import io.mindspice.okragameserver.game.player.PlayerData;
import io.mindspice.okragameserver.schema.websocket.incoming.NetGameAction;
import io.mindspice.okragameserver.game.player.Player;
import io.mindspice.okragameserver.util.Log;
import jakarta.annotation.PostConstruct;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Controller

public class GameServerSocketHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<Integer, Player> playerTable;
    private final HttpServiceClient serviceClient;

    public GameServerSocketHandler(ConcurrentHashMap<Integer, Player> playerTable, HttpServiceClient serviceClient) {
        this.playerTable = playerTable;
        this.serviceClient = serviceClient;
    }

    @PostConstruct
    public void init() {
        Log.SERVER.debug(this.getClass(), "GameServerSocketHandler Init");
        Log.SERVER.debug(this.getClass(), "playerTable:" + playerTable);
        Log.SERVER.debug(this.getClass(), "ServiceClient:" + serviceClient);

    }


    @Override
    public void handleTextMessage( @NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        // Look up the player from the connection attributes as set on afterConnectionEstablished
        // All incoming player messages must conform to NetGameAction, so if deserialization fails
        // the connection is dropped and logged due to malformed message likely being abuse.

        try {
            Player player = (Player) session.getAttributes().get("player");
            NetGameAction netGameAction = JsonUtils.readValue(message.getPayload(), NetGameAction.class);
            player.onMessage(netGameAction);
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "Error handling websocket message", e);
            session.close(CloseStatus.BAD_DATA);
            //TODO abuse log this
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        // No null check needed as handshakeHandlerIntercept guarantees any connection that makes it this far
        // has a playerId attribute
        try {
            Integer playerId = (Integer) session.getAttributes().get("playerId");
            // TODO debug log if null
            Player player = playerTable.get(playerId);

            if (player == null) {
                player = new Player(playerId, session);
                playerTable.put(playerId, player);
                session.getAttributes().put("player", player);
            } else {
                player.setConnection(session);
                session.getAttributes().put("player", player);
                return; // Return now as there is no need to calculate player data as it exists
            }

            PlayerInfo playerInfo = serviceClient.gameAPI().getPlayerInfo(playerId).data().orElseThrow();
            PlayerFunds playerFunds = serviceClient.gameAPI().getPlayerFunds(playerId).data().orElseThrow();
            Results dailyResults = serviceClient.gameAPI().getPlayerDailyResults(playerId).data().orElseThrow();
            Results historicalResults = serviceClient.gameAPI().getPlayerHistoricalResults(playerId).data().orElseThrow();
            String playerDid = serviceClient.gameAPI().getPlayerDid(playerId).data().orElse(null);
            List<String> ownedCards = null;
            if (playerDid != null) {
                ownedCards = serviceClient.gameAPI().getPlayerCards(playerDid).data().orElse(List.of());
            }
            Map<Integer, String> pawnSets = serviceClient.gameAPI().getPawnSets(playerId).data().orElse(new HashMap<>());

            player.setFullPlayerData(
                    new PlayerData(
                           playerInfo.displayName(),
                            playerDid,
                            playerFunds,
                            playerInfo.avatar(),
                            pawnSets,
                            dailyResults,
                            historicalResults,
                            ownedCards == null ? List.of() : ownedCards
                    )
            );

//            String displayName = gameAPI.getDisplayName(playerId).data().orElseThrow();
//            player.setName(displayName);

            Log.SERVER.info("Connection established for player: " + player.getLoggable());
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "Error in post connection logic", e);
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        //TODO add debug logging
    }
}
