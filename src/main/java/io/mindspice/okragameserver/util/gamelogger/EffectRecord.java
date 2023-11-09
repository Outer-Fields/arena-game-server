package io.mindspice.okragameserver.util.gamelogger;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.okragameserver.game.enums.EffectType;

import java.util.UUID;


public record EffectRecord(
        @JsonProperty("uuid") UUID id,
        @JsonProperty("effect_type") EffectType effectType,
        @JsonProperty("amount") double amount,
        @JsonProperty("roll_off_rounds") int rollOffRounds,
        @JsonProperty("roll_off_chance") double rollOffChance,
        @JsonProperty("expected_roll_off_round") int expectedRollOffRound
) {
}
