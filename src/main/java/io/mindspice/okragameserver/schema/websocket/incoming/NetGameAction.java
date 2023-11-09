package io.mindspice.okragameserver.schema.websocket.incoming;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.PlayerAction;


public record NetGameAction(
        PlayerAction action,
        @JsonProperty("player_pawn") PawnIndex playerPawn,
        @JsonProperty("target_pawn") PawnIndex targetPawn
        // PotionCard potionCard,
) { }



