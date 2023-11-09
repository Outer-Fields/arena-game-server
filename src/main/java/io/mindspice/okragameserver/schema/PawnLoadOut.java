package io.mindspice.okragameserver.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.okragameserver.game.cards.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public record PawnLoadOut(
        @JsonProperty("pawn_card") PawnCard pawnCard,
        @JsonProperty("talisman_card") TalismanCard talismanCard,
        @JsonProperty("weapon_card_1") WeaponCard weaponCard1,
        @JsonProperty("weapon_card_2") WeaponCard weaponCard2,
        @JsonProperty("action_deck") List<ActionCard> actionDeck,
        @JsonProperty("ability_deck") List<AbilityCard> abilityDeck,
        @JsonProperty("power_deck") List<PowerCard> powerDeck

) {

    public int sumLevel() {
        return Stream.of(
                weaponCard1.getLevel(),
                weaponCard2.getLevel(),
                actionDeck.stream().mapToInt(ActionCard::getLevel).sum(),
                abilityDeck.stream().mapToInt(AbilityCard::getLevel).sum(),
                powerDeck.stream().mapToInt(PowerCard::getLevel).sum()
        ).mapToInt(Integer::intValue).sum();
    }

    public boolean isValid() {
        if (pawnCard == null || talismanCard == null || weaponCard1 == null
                || weaponCard2 == null || actionDeck == null
                || abilityDeck == null || powerDeck == null) {
            return false;
        }
       return pawnCard.actionType == weaponCard2.getStats().getActionType()
               && weaponCard2.getStats().getActionType() == actionDeck.get(0).getStats().getActionType();
    }
}
