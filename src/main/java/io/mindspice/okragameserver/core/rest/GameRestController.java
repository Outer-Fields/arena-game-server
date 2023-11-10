package io.mindspice.okragameserver.core.rest;

import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.databaseservice.client.api.OkraGameAPI;
import io.mindspice.databaseservice.client.schema.PlayerFunds;
import io.mindspice.databaseservice.client.schema.PlayerInfo;
import io.mindspice.databaseservice.client.schema.Results;
import io.mindspice.jxch.rpc.util.bech32.AddressUtil;
import io.mindspice.jxch.rpc.util.bech32.Bech32;
import io.mindspice.jxch.rpc.util.bech32.Bytes32;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.core.GameServer;
import io.mindspice.okragameserver.core.HttpServiceClient;
import io.mindspice.okragameserver.core.Settings;
import io.mindspice.okragameserver.game.gameroom.GameRoom;
import io.mindspice.okragameserver.game.player.Player;
import io.mindspice.okragameserver.game.player.PlayerData;
import io.mindspice.okragameserver.schema.PawnSet;
import io.mindspice.okragameserver.schema.rest.MatchInfo;
import io.mindspice.okragameserver.schema.rest.SaveSetReq;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.NetQueueJoinResponse;
import io.mindspice.okragameserver.util.Log;
import io.mindspice.okragameserver.util.ProfanityChecker;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;


@CrossOrigin(origins = "*", allowedHeaders = {"token", "user-agent", "content-type"}, exposedHeaders = {"token"})
@RestController
@RequestMapping("/api")
public class GameRestController {
    public final HttpServiceClient serviceClient;
    private final Map<Integer, Player> playerTable;
    private final GameServer gameServer;
    private final OkraGameAPI gameAPI;

    public GameRestController(
            @Qualifier("httpServiceClient") HttpServiceClient httpServiceClient,
            @Qualifier("gameServer") GameServer gameServer,
            @Qualifier("playerTable") Map<Integer, Player> playerTable,
            @Qualifier("gameApi") OkraGameAPI gameAPI) {
        this.serviceClient = httpServiceClient;
        this.playerTable = playerTable;
        this.gameServer = gameServer;
        this.gameAPI = gameAPI;
    }

    @PostMapping("/update_pawn_set")
    public ResponseEntity<String> updatePawnSet(
            @RequestHeader("CF-Connecting-IP") String originIp,
            @RequestBody SaveSetReq setReq,
            @RequestHeader("token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                Log.ABUSE.info("Request with no token | OriginIP: " + originIp);

                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            int playerId = serviceClient.getPlayerId(token);
            if (playerId == -2) { return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); }
            if (playerId == -1) {
                Log.ABUSE.info("Request with invalid token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            System.out.println(setReq);

            Player player = playerTable.get(playerId);
            if (player == null) { return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); }
            if (player.restTimeout()) {
                Log.ABUSE.info("Too many requests | PlayerId:  + playerId +  | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
            }
            PawnSet pawnSet = new PawnSet(setReq.setNum(), setReq.setName(), setReq.pawnLoadOut());
            serviceClient.gameAPI().updatePawnSet(
                    playerId, setReq.setNum(), pawnSet.toJsonString());

            player.getPawnSets().put(setReq.setNum(), pawnSet);

            return new ResponseEntity<>(JsonUtils.writeString(
                    JsonUtils.newSingleNode("set_num", setReq.setNum())),
                    HttpStatus.OK
            );
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/update_pawn_set threw: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @PostMapping("/join_queue")
    public ResponseEntity<String> joinQueue(
            @RequestHeader("CF-Connecting-IP") String originIp,
            @RequestHeader("token") String token,
            @RequestBody String req) {
        System.out.println("join queue in");
        try {
            if (token == null || token.isEmpty()) {
                Log.ABUSE.info("Request with no token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            int playerId = serviceClient.getPlayerId(token);
            if (playerId == -2) { return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); }
            if (playerId == -1) {
                Log.ABUSE.info("Request with invalid token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            Player player = playerTable.get(playerId);
            if (player == null) {
                Log.ABUSE.info("Request without socket connection | PlayerId: " + playerId + " | OriginIP " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            if (player.restTimeout() || player.isQueueCoolDown()) {
                Log.ABUSE.info("Too many requests | PlayerId:  + playerId +  | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
            }

            if (gameServer.isInQueue(playerId)) {
                return new ResponseEntity<>(JsonUtils.writeString(NetQueueJoinResponse.alreadyQueued()), HttpStatus.CONFLICT);
            }

            int setNum = JsonUtils.readTree(req).get("set_num").asInt();

            if (setNum == -1) {
                var queueResp = gameServer.addRemoveQueued(player, setNum, true);
                return new ResponseEntity<>(JsonUtils.writeString(queueResp), HttpStatus.OK);
            }
            PawnSet pawnSet = player.getPawnSets().get(setNum);
            if (pawnSet == null) {
                return new ResponseEntity<>(
                        JsonUtils.writeString(NetQueueJoinResponse.invalidSet("Not set at index")), HttpStatus.OK
                );
            }

            Pair<Boolean, String> validation = pawnSet.validate(player.getValidCards());

            if (validation.first()) {
                var queueResp = gameServer.addRemoveQueued(player, setNum, true);
                Log.SERVER.debug(this.getClass(), "Queue Join: Success |  PlayerId" + playerId);
                return new ResponseEntity<>(JsonUtils.writeString(queueResp), HttpStatus.OK);
            } else {
                Log.SERVER.debug(this.getClass(), "Queue Join: Fail Set Validation |  PlayerId"
                        + playerId + " | Reason:" + validation.second());
                return new ResponseEntity<>(
                        JsonUtils.writeString(NetQueueJoinResponse.invalidSet(validation.second())), HttpStatus.NOT_ACCEPTABLE
                );
            }
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/join_queue threw: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/leave_queue")
    public ResponseEntity<String> leaveQueue(
            @RequestHeader("CF-Connecting-IP") String originIp,
            @RequestHeader("token") String token) {
        try {

            if (token == null || token.isEmpty()) {
                Log.ABUSE.info("Request with no token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            int playerId = serviceClient.getPlayerId(token);
            if (playerId == -2) { return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); }
            if (playerId == -1) {
                Log.ABUSE.info("Request with invalid token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            Player player = playerTable.get(playerId);
            if (player == null) {
                Log.ABUSE.info("Request without socket connection | PlayerId: " + playerId + " | OriginIP " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            if (player.restTimeout()) {
                Log.ABUSE.info("Too many requests | PlayerId:  + playerId +  | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
            }
            gameServer.addRemoveQueued(player, -1, false);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/leave_queue threw: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/confirm_queue")
    public ResponseEntity<String> confirmQueue(
            @RequestHeader("CF-Connecting-IP") String originIp,
            @RequestHeader("token") String token,
            @RequestBody String confirmReq) {
        try {
            if (token == null || token.isEmpty()) {
                Log.ABUSE.info("Request with no token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            int playerId = serviceClient.getPlayerId(token);
            if (playerId == -2) { return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); }
            if (playerId == -1) {
                Log.ABUSE.info("Request with invalid token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            Player player = playerTable.get(playerId);
            if (player == null) {
                Log.ABUSE.info("PlayerId: " + playerId + " | Request without socket connection");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            JsonNode reqJson = JsonUtils.readTree(confirmReq);
            if (gameServer.setQueueConfirm(reqJson.get("uuid").asText(), playerId)) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }

        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/confirm_queue threw: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/confirm_ready")
    public ResponseEntity<String> confirmMatch(
            @RequestHeader("CF-Connecting-IP") String originIp,
            @RequestHeader("token") String token) {
        try {
            System.out.println("confirm in");
            if (token == null || token.isEmpty()) {
                Log.ABUSE.info("Request with no token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            int playerId = serviceClient.getPlayerId(token);
            if (playerId == -2) { return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); }
            if (playerId == -1) {
                Log.ABUSE.info("Request with invalid token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            Player player = playerTable.get(playerId);
            if (player == null) {
                Log.ABUSE.info("Request without socket connection | PlayerId: " + playerId + " | OriginIP " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

            }

            if (player.getGameRoom() == null) {
                Log.ABUSE.info("PlayerId: " + playerId + " | Confirmed match with none active");
                Log.SERVER.info("PlayerId: " + playerId + " | Confirmed match with none active");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            player.getGameRoom().setReady(playerId);
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/confirm_ready threw: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get_full_info")
    public ResponseEntity<String> getFullInfo(
            @RequestHeader("CF-Connecting-IP") String originIp,
            @RequestHeader("token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                Log.ABUSE.info("Request with no token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            int playerId = serviceClient.getPlayerId(token);
            if (playerId == -2) { return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); }
            if (playerId == -1) {
                Log.ABUSE.info("Request with invalid token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            Player player = playerTable.get(playerId);
            // Spin a little if the players
            int i = 0;
            while ((player == null || !player.hasData()) && i < 20) {
                Thread.sleep(50);
                player = playerTable.get(playerId);
                i++;
            }

            if (player == null) {
                Log.ABUSE.info("Request without socket connection | PlayerId: " + playerId + " | OriginIP " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            if (player.restTimeout()) {
                Log.ABUSE.info("PlayerId: " + playerId + " | Too many requests| OriginIP:" + originIp);
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
            }

            if (player.allowFullUpdate()) {
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
                player.setIp(originIp);
            }
            if (player.getPlayerData() == null) {
                throw new IllegalStateException("Player data is null");
            }
            return new ResponseEntity<>(player.getFullInfo(), HttpStatus.OK);
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/get_full_info threw: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get_basic_info")
    public ResponseEntity<String> getBasicInfo(
            @RequestHeader("CF-Connecting-IP") String originIp,
            @RequestHeader("token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                Log.ABUSE.info("Request with no token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            int playerId = serviceClient.getPlayerId(token);
            if (playerId == -2) { return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); }
            if (playerId == -1) {
                Log.ABUSE.info("Request with invalid token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            Player player = playerTable.get(playerId);
            if (player == null) {
                Log.ABUSE.info("Request without socket connection | PlayerId: " + playerId + " | OriginIP " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            if (player.restTimeout()) {
                Log.ABUSE.info("PlayerId: " + playerId + " | Too many requests" + " | OriginIP " + originIp);
                return new ResponseEntity<>(HttpStatus.TOO_MANY_REQUESTS);
            }

            if (player.allowBasicUpdate()) {
                var playerFunds = serviceClient.gameAPI().getPlayerFunds(playerId).data().orElseThrow();
                var dailyResults = serviceClient.gameAPI().getPlayerDailyResults(playerId).data().orElseThrow();
                var historicalResults = serviceClient.gameAPI().getPlayerHistoricalResults(playerId).data().orElseThrow();
                List<String> ownedCards = null;
                if (player.getDid() != null) {
                    ownedCards = serviceClient.gameAPI().getPlayerCards(player.getDid()).data().orElse(List.of());
                }

                player.setBasicPlayerData(ownedCards == null ? List.of() : ownedCards, playerFunds, dailyResults, historicalResults);
            }

            return new ResponseEntity<>(player.getBasicInfo(), HttpStatus.OK);

        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/get_basic_info threw: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update_display_name")
    public ResponseEntity<String> updateDisplayName(
            @RequestHeader("CF-Connecting-IP") String originIp,
            @RequestHeader("token") String token,
            @RequestBody String req) {
        try {
            if (token == null || token.isEmpty()) {
                Log.ABUSE.info("Request with no token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            int playerId = serviceClient.getPlayerId(token);
            if (playerId == -2) { return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); }
            if (playerId == -1) {
                Log.ABUSE.info("Request with invalid token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            if (Instant.now().getEpochSecond() - gameAPI.getLastDisplayNameUpdate(playerId).data().orElseThrow() < 86400) {
                return new ResponseEntity<>("Can only change name once per day", HttpStatus.UNAUTHORIZED);
            }

            String newName = JsonUtils.readTree(req).get("name").asText();

            if (newName.length() > 15) {
                return new ResponseEntity<>("Name must be under 15 characters.", HttpStatus.OK);
            }

            var profanityCheck = ProfanityChecker.check(newName);
            if (!profanityCheck.first()) {
                Log.ABUSE.info(this.getClass(), "Profane name change | PlayerId:" + playerId + " | words: " + profanityCheck.second());
                return new ResponseEntity<>("Name cannot contain profanity", HttpStatus.UNPROCESSABLE_ENTITY);
            }

            boolean alreadyExists = gameAPI.doesNameExist(newName).success();

            if (alreadyExists) {
                return new ResponseEntity<>("Name already exists, not updated", HttpStatus.OK);
            }

            gameAPI.updateDisplayName(playerId, newName);
            return new ResponseEntity<>("Name Updated", HttpStatus.OK);

        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/update_display_name threw: ", e);

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update_avatar")
    public ResponseEntity<String> updateAvatar(
            @RequestHeader("CF-Connecting-IP") String originIp,
            @RequestHeader("token") String token,
            @RequestBody String req) {
        try {
            if (token == null || token.isEmpty()) {
                Log.ABUSE.info("Request with no token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            int playerId = serviceClient.getPlayerId(token);
            if (playerId == -2) { return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); }
            if (playerId == -1) {
                Log.ABUSE.info("Request with invalid token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            if (Instant.now().getEpochSecond() - gameAPI.getLastAvatarUpdate(playerId).data().orElseThrow() < 86400) {
                return new ResponseEntity<>("Can only change avatar once per day", HttpStatus.UNAUTHORIZED);
            }

            String newAvatar = JsonUtils.readTree(req).get("nft_id").asText();

            serviceClient.updateAvatar(playerId, newAvatar);

            return new ResponseEntity<>("Avatar update pending", HttpStatus.OK);

        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/update_avatar threw: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get_match_info")
    public ResponseEntity<String> updateDisplayName(
            @RequestHeader("CF-Connecting-IP") String originIp,
            @RequestHeader("token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                Log.ABUSE.info("Request with no token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            int playerId = serviceClient.getPlayerId(token);
            if (playerId == -2) { return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); }
            if (playerId == -1) {
                Log.ABUSE.info("Request with invalid token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            var player = playerTable.get(playerId);

            if (player == null) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }

            if (!player.isInGame()) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            GameRoom gameRoom = player.getGameRoom();
            var enemyPlayer = gameRoom.getPlayer1().getId() == playerId
                    ? gameRoom.getPlayer2().getPlayer()
                    : gameRoom.getPlayer1().getPlayer();

            MatchInfo matchInfo = new MatchInfo(
                    player.getPlayerData().getDisplayName(),
                    player.getPlayerData().getAvatar(),
                    player.getPlayerData().getHistoricalResults(),
                    enemyPlayer.getPlayerData().getDisplayName(),
                    enemyPlayer.getPlayerData().getAvatar(),
                    enemyPlayer.getPlayerData().getHistoricalResults()
            );

            return new ResponseEntity<>(JsonUtils.writeString(matchInfo), HttpStatus.OK);
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/get_match_info threw: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @GetMapping("/get_messages")
    public ResponseEntity<String> getMessages(
            @RequestHeader("CF-Connecting-IP") String originIp,
            @RequestHeader("token") String token) {
        try {
            if (token == null || token.isEmpty()) {
                Log.ABUSE.info("Request with no token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            int playerId = serviceClient.getPlayerId(token);
            if (playerId == -2) { return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR); }
            if (playerId == -1) {
                Log.ABUSE.info("Request with invalid token | OriginIP: " + originIp);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            var response = new JsonUtils.ObjectBuilder()
                    .put("messages", Settings.GET().projectMessages)
                    .put("p_banners", Settings.GET().projectAds)
                    .put("o_banners", Settings.GET().otherAds)
                    .buildString();

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Log.SERVER.error(this.getClass(), "/get_match_info threw: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }
}