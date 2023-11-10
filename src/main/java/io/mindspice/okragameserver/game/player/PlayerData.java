package io.mindspice.okragameserver.game.player;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.mindspice.databaseservice.client.schema.PlayerFunds;
import io.mindspice.databaseservice.client.schema.Results;
import io.mindspice.jxch.rpc.util.bech32.AddressUtil;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.CardDomain;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.schema.PawnSet;
import io.mindspice.okragameserver.util.CardUtil;
import io.mindspice.okragameserver.util.Log;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class PlayerData {
    private final String displayName;
    private final PlayerFunds playerFunds;
    private final String did;
    private final String avatar;
    private volatile Map<Integer, PawnSet> pawnSets;
    private final Results dailyResults;
    private final Results historicalResults;
    private final Map<CardDomain, List<String>> ownedCards; // these only get replace
    private final Map<CardDomain, List<String>> validCards;

    public PlayerData(String displayName, String did, PlayerFunds playerFunds, String avatar,
            Map<Integer, String> pawnSets, Results dailyResults, Results historicalResults, List<String> ownedCards) {
        this.displayName = displayName != null ? displayName : "";
        this.playerFunds = playerFunds;
        this.did = did == null ? null : did.startsWith("did") ? did : AddressUtil.encode("did:chia", did);
        this.avatar = avatar;
        this.dailyResults = dailyResults;
        this.historicalResults = historicalResults;
        Pair<Map<CardDomain, List<String>>, Map<CardDomain, List<String>>>
                mappedCards = CardUtil.playerMappedCards(ownedCards);
        this.ownedCards = mappedCards.first();
        this.validCards = mappedCards.second();
        this.pawnSets = new ConcurrentHashMap<>(5);
        pawnSets.forEach((k, v) -> {
            try {
                this.pawnSets.put(k, PawnSet.fromJsonEntry(k, v));
            } catch (IOException e) {
                Log.SERVER.error(PawnSet.class, "Error deserializing json", e);
            }
        });
    }

    public PlayerData(String displayName, String did, PlayerFunds playerFunds, String avatar, Results dailyResults,
            Results historicalResults, List<String> ownedCards) {
        this.displayName = displayName != null ? displayName : "";
        this.playerFunds = playerFunds;
        this.did = did == null ? null : did.startsWith("did") ? did : AddressUtil.encode("did:chia", did);
        this.avatar = avatar;
        this.dailyResults = dailyResults;
        this.historicalResults = historicalResults;
        Pair<Map<CardDomain, List<String>>, Map<CardDomain, List<String>>>
                mappedCards = CardUtil.playerMappedCards(ownedCards);
        this.ownedCards = mappedCards.first();
        this.validCards = mappedCards.second();
    }



    public PlayerData(String displayName, PlayerFunds playerFunds, String did, String avatar,
            Map<Integer, PawnSet> pawnSets, Results dailyResults, Results historicalResults, List<String> ownedCards) {
        this.displayName = displayName;
        this.playerFunds = playerFunds;
        this.did = did == null ? null : did.startsWith("did") ? did : AddressUtil.encode("did:chia", did);
        this.avatar = avatar;
        this.dailyResults = dailyResults;
        this.historicalResults = historicalResults;
        Pair<Map<CardDomain, List<String>>, Map<CardDomain, List<String>>>
                mappedCards = CardUtil.playerMappedCards(ownedCards);
        this.ownedCards = mappedCards.first();
        this.validCards = mappedCards.second();
        this.pawnSets = pawnSets;
    }

    public PlayerData(String displayName) {
        this.displayName = displayName;
        this.playerFunds = new PlayerFunds(0, 0, 0, 0);
        this.did = "Did:Bot";
        this.avatar = "bot";
        this.dailyResults = new Results(0, 0);
        this.historicalResults = new Results(0, 0);
        this.ownedCards = Map.of();
        this.validCards = Map.of();
        this.pawnSets = Map.of();
    }

    public PlayerData getUpdated(List<String> ownedCards, PlayerFunds funds,
            Results dailyResults, Results historicalResults) {
        return new PlayerData(
                this.displayName,
                this.playerFunds,
                this.did,
                this.avatar,
                this.pawnSets,
                dailyResults,
                historicalResults,
                ownedCards
        );
    }


    public String getDisplayName() {
        return displayName;
    }

    public PlayerFunds getPlayerFunds() {
        return playerFunds;
    }

    public String getDid() {
        return did;
    }

    public String getAvatar() {
        return avatar;
    }

    public Map<Integer, PawnSet> getPawnSets() {
        return pawnSets;
    }

    public Results getDailyResults() {
        return dailyResults;
    }

    public Results getHistoricalResults() {
        return historicalResults;
    }

    public Map<CardDomain, List<String>> getOwnedCards() {
        return ownedCards;
    }

    public Map<CardDomain, List<String>> getValidCards() {
        return validCards;
    }

    public String getFullJson() throws JsonProcessingException {
        return new JsonUtils.ObjectBuilder()
                .put("funds", playerFunds)
                .put("results", new JsonUtils.ObjectBuilder()
                        .put("daily", dailyResults)
                        .put("historical", historicalResults)
                        .buildNode())
                .put("pawn_sets", pawnSets)
                .put("display_name", displayName)
                .put("did", did != null ? did : "No DID, Transfer Account NFT to DID to Link")
                .put("cards", ownedCards)
                .put("avatar", avatar)
                .buildString();
    }

    public String getBasicJson() throws JsonProcessingException {
        return new JsonUtils.ObjectBuilder()
                .put("funds", playerFunds)
                .put("results", new JsonUtils.ObjectBuilder()
                        .put("daily", dailyResults)
                        .put("historical", historicalResults)
                        .buildNode())
                .put("cards", ownedCards)
                .buildString();
    }


}
