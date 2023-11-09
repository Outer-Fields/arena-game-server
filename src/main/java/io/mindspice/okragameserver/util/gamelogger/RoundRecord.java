package io.mindspice.okragameserver.util.gamelogger;

import com.fasterxml.jackson.annotation.JsonProperty;


public record RoundRecord(
        @JsonProperty("round_number") int roundNumber,
        @JsonProperty("player_1") PlayerRecord player1,
        @JsonProperty("player_2") PlayerRecord player2
) {
    PlayerRecord getPlayerById(int playerId) {
        return player1.playerId() == playerId ? player1 : player2;
    }

}
