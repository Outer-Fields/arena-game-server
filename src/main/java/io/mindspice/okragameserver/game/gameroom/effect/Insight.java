package io.mindspice.okragameserver.game.gameroom.effect;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.mindspice.okragameserver.game.cards.AbilityCard;
import io.mindspice.okragameserver.game.cards.ActionCard;
import io.mindspice.okragameserver.game.cards.PowerCard;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.CardHand;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.EffectStats;


import java.util.List;


public class Insight {
    @JsonProperty("pawn_index") public PawnIndex pawnIndex;
    @JsonProperty("insight_type") public EffectType insightType;
    @JsonProperty("card_hand") public CardHand cardHand;
    @JsonProperty("effects") public List<EffectStats> effects;
    @JsonProperty("action_deck")public List<ActionCard> actionDeck;
    @JsonProperty("ability_deck")public List<AbilityCard> abilityDeck;
    @JsonProperty("power_deck")public List<PowerCard> powerDeck;
}
