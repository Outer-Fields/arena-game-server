package io.mindspice.okragameserver.util.gamelogger;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.mindspice.okragameserver.game.cards.*;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.enums.StatType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;


public record PawnRecord(
        @JsonProperty("pawn_index") PawnIndex pawnIndex,
        @JsonProperty("stats") Map<StatType, Integer> stats,
        @JsonProperty("stat_hash") int statHash,
        @JsonProperty("stats_max") Map<StatType, Integer> statsMax,
        @JsonProperty("effects") List<EffectRecord> effects,
        @JsonProperty("effects_hash") int effectHash,
        @JsonProperty("active_powers") List<PowerRecord> activePowers,
        @JsonProperty("pawn_card") PawnCard pawnCard,
        @JsonProperty("power_card") PowerCard powerCard,
        @JsonProperty("weapon_card_1") WeaponCard weaponCard1,
        @JsonProperty("weapon_card_2") WeaponCard weaponCard2,
        @JsonProperty("action_card_1") ActionCard actionCard1,
        @JsonProperty("action_card_2") ActionCard actionCard2,
        @JsonProperty("ability_card_1") AbilityCard abilityCard1,
        @JsonProperty("ability_card_2") AbilityCard abilityCard2,
        @JsonProperty("talisman_card") TalismanCard talismanCard
) {
}
