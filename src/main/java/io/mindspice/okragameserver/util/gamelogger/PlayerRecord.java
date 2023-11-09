package io.mindspice.okragameserver.util.gamelogger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import io.mindspice.mindlib.util.FuncUtils;
import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.schema.websocket.incoming.NetGameAction;

import java.io.IOException;
import java.util.*;


public record PlayerRecord(
        @JsonProperty("player_id") int playerId,
        @JsonProperty("pawn_info") List<JsonNode> pawnInfo,
        @JsonProperty("action_in") List<NetGameAction> actionsIn,
        @JsonProperty("out_messages") List<Object> outMessages,
        @JsonProperty("bot_decisions") Map<PawnIndex, List<String>> botDecisions
) {
    PlayerRecord(int playerId, List<JsonNode> pawnInfo) {
        this(
                playerId,
                pawnInfo,
                new ArrayList<>(3),
                new ArrayList<>(),
                new EnumMap<>(PawnIndex.class)
        );
    }

    public List<PawnRecord> getPawnInfo() {
        return pawnInfo.stream()
                .map(p -> FuncUtils.nullOnExcept(() -> (PawnRecord) JsonUtils.readJson(p, PawnRecord.class)))
                .filter(Objects::nonNull)
                .toList();
    }

}
