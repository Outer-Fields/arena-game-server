package io.mindspice.okragameserver.game.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.databaseservice.client.schema.PlayerFunds;
import io.mindspice.databaseservice.client.schema.Results;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.core.Settings;
import io.mindspice.okragameserver.game.enums.CardDomain;
import io.mindspice.okragameserver.schema.websocket.incoming.NetGameAction;
import io.mindspice.okragameserver.util.Log;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import io.mindspice.okragameserver.schema.PawnSet;
import io.mindspice.okragameserver.game.gameroom.GameRoom;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


public class Player {

    /* player Info */
    protected final int id;
    private final boolean isBot = false;
    protected volatile GameRoom gameRoom;
    private volatile WebSocketSession connection;
    private final Set<String> ips = new HashSet<>(1);
    protected volatile boolean inGame = false;
    private volatile boolean isPremium = false;
    private boolean fetchedPawnSets = false;
    /* Internal Info*/
    private volatile PlayerData playerData;
    /* Abuse filters */
    private volatile int wsMsgCount = 0;
    private volatile long wsMsgEpoch = 0;
    private volatile long lastSmallReq = 0;
    private volatile long lastLargeReq = 0;
    private volatile int restMsgCount = 0;
    private volatile long restMsgEpoch = 0;
    private volatile long queueCoolDownTime = 0;
    private volatile long lastMsgTime = Instant.now().getEpochSecond();

    public Player(int id, WebSocketSession connection) {
        this.id = id;
        this.connection = connection;
        wsMsgEpoch = Instant.now().getEpochSecond();
    }

    public Player(int id, WebSocketSession connection, String ip) {
        this.id = id;
        this.connection = connection;
        wsMsgEpoch = Instant.now().getEpochSecond();
    }

    public Player(int id, boolean isBot) {
        this.id = id;
        wsMsgEpoch = Instant.now().getEpochSecond();
    }

    private boolean wsTimeout() {
        long now = Instant.now().getEpochSecond();
        if ((now - wsMsgEpoch) >= Settings.GET().wsMsgWindow) {
            wsMsgEpoch = now;
            wsMsgCount = 0;
        }
        wsMsgCount++;
        return wsMsgCount > Settings.GET().wsMsgLimit;
    }

    public boolean restTimeout(long now) {
        System.out.println("here");
        if ((now - restMsgEpoch) >= Settings.GET().restMsgWindow) {
            restMsgEpoch = now;
            restMsgCount = 0;
        }
        restMsgCount++;
        return restMsgCount > Settings.GET().restMsgLimit;
    }

    public void setIp(String ip) {
        ips.add(ip);
        if (ips.size() > 1) {
            Log.ABUSE.info("Multiple ips detected | Player: " + id + " | ips: " + ips);
        }
    }

    public boolean restTimeout() {
        long now = Instant.now().getEpochSecond();
        if ((now - restMsgEpoch) >= Settings.GET().restMsgWindow) {
            restMsgEpoch = now;
            restMsgCount = 0;
        }
        restMsgCount++;
        return restMsgCount > Settings.GET().restMsgLimit;
    }

    public boolean hasData() {
        return playerData != null;
    }

    public long getLastMsgTime() {
        return lastMsgTime;
    }

    public void setFetchedPawnSets(){
        fetchedPawnSets = true;
    }

    public boolean haveFetchedPawnSets() {
        return fetchedPawnSets;
    }

    public void setLastMsgTime() {
        lastMsgTime = Instant.now().getEpochSecond();
    }

    public void setQueueCoolDown() {
        queueCoolDownTime = Instant.now().getEpochSecond();
    }

    public boolean isQueueCoolDown() {
        return Instant.now().getEpochSecond() - queueCoolDownTime < Settings.GET().queueCoolDown;
    }

    public String getDid() {
        return playerData.getDid();
    }

    public WebSocketSession getConnection() {
        return connection;
    }

    // Requests and messages
    public void onMessage(NetGameAction msg) {
        if (wsTimeout()) {
            try {
                connection.close();
                // TODO timeout player table?
                return;
            } catch (IOException e) {
                Log.ABUSE.info(getLoggable() + " | Too many websocket messages");
                Log.SERVER.error(this.getClass(), "Error closing connection", e);
            }
        }
        if (inGame) {
            gameRoom.addMsg(id, msg);
        } else {
            // if they are not in a game ignore their WS messages
            Log.SERVER.debug(this.getClass(), getLoggable() + " | Websocket message while not in game");
        }
        lastMsgTime = Instant.now().getEpochSecond();
    }

    public void send(Object obj) {
        try {
            if (!connection.isOpen()) { return; }
            var msg = JsonUtils.writeString(obj);
            connection.sendMessage(new TextMessage(msg));
        } catch (JsonProcessingException e) {
            Log.SERVER.error(this.getClass(), "", e);
        } catch (IOException e) {
            Log.SERVER.error(this.getClass(), "", e);
            //TODO handle issues if they disconnected
        }
    }

    public void setFullPlayerData(PlayerData playerData) {
        this.playerData = playerData;
        lastLargeReq = Instant.now().getEpochSecond();
    }

    public void setBasicPlayerData(List<String> ownedCards, PlayerFunds funds,
            Results dailyResults, Results historicalResults) {
        lastSmallReq = Instant.now().getEpochSecond();
        this.playerData = playerData.getUpdated(ownedCards, funds, dailyResults, historicalResults);
    }


    public PlayerData getPlayerData() {
        return playerData;
    }

    public String getBasicInfo() throws JsonProcessingException {
        return playerData.getBasicJson();
    }

    public String getFullInfo() throws JsonProcessingException {
        return playerData.getFullJson();
    }

    // Abuse Limits

    public boolean allowFullUpdate() {
        long now = Instant.now().getEpochSecond();
        if (now - lastLargeReq < 180) {
            Log.SERVER.debug(this.getClass(), "Full updated Disallowed");
            return false;
        }
        return true;
    }

    public boolean allowBasicUpdate() {
        long now = Instant.now().getEpochSecond();
        if (now - lastSmallReq < 60) {
            Log.SERVER.debug(this.getClass(), "Small updated Disallowed");
            return false;
        }
        return true;
    }

    public String getLoggable() {
        return "ID:" + id + " Name:" + playerData.getDisplayName() + " IP:" + ips;
    }

    // Getters/Setters

    public int getId() {
        return id;
    }

    public String getName() {
        return playerData.getDisplayName();
    }

    public boolean isConnected() {
        return connection.isOpen();
    }

    public boolean disconnect() {
        try {
            connection.close(CloseStatus.POLICY_VIOLATION);
        } catch (IOException e) {
            Log.SERVER.error(this.getClass(), "Error closing socket for playerId: " + id + " | reason:" + e.getMessage());
            return false;
        }
        return true;
    }

    public void setConnection(WebSocketSession connection) {
        this.connection = connection;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    public Map<Integer, PawnSet> getPawnSets() {
        return playerData.getPawnSets();
    }

    public long getLastMsgEpoch() {
        return wsMsgEpoch;
    }

    public boolean isBot() {
        return isBot;
    }

    public Map<CardDomain, List<String>> getOwnedCards() {
        return playerData.getOwnedCards();
    }

    // Map ownedCards uids are treated different internally and externally, as there are foil
    // ownedCards that are variants of internal ownedCards. The internal representation gets the
    // display type flag stripped

    // we need to copy as we mutate via removal to validate the set
    public Map<CardDomain, List<String>> getValidCards() {
        return playerData.getValidCards().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new ArrayList<>(entry.getValue())
                ));
    }

    public boolean isPremium() {
        return isPremium;
    }

    public Set<String> getIp() {
        return ips;
    }

    // TODO these could be made more null safe for if the player doesnt has data yet
    //  and they some how get called, but playerdata should be added soon after construction
    //  so its not too pressing
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Player: ");
        sb.append("\n  id: ").append(id);
        sb.append(",\n  name: \"").append(playerData.getDisplayName()).append('\"');
        sb.append(",\n  gameRoom: ").append(gameRoom != null ? gameRoom.getRoomId() : -1);
        sb.append(",\n  ip: \"").append(ips).append('\"');
        sb.append(",\n  inGame: ").append(inGame);
        sb.append("\n");
        return sb.toString();
    }

    public JsonNode getStatusJson() {
        return new JsonUtils.ObjectBuilder()
                .put("id", id)
                .put("name", playerData.getDisplayName())
                .put("remote_ip", ips)
                .put("premium", isPremium)
                .put("in_game", inGame)
                .put("game_room", gameRoom == null ? -1 : gameRoom.getRoomId())
                .put("owned_cards", playerData.getOwnedCards().size())
                .put("pawn_sets", playerData.getPawnSets().size())
                .buildNode();
    }
}
