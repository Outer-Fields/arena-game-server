package io.mindspice.okragameserver.game.gameroom.action.logic;

import io.mindspice.okragameserver.game.enums.ActionFlag;
import io.mindspice.okragameserver.game.enums.PowerEnums;
import io.mindspice.okragameserver.game.gameroom.action.ActionReturn;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.effect.Insight;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;
import io.mindspice.okragameserver.schema.websocket.outgoing.game.CardHand;

import static io.mindspice.okragameserver.game.enums.EffectType.INSIGHT_ALL_DECKS;


public interface IEffect {

    void doEffect(PawnInterimState playerInterimState, PawnInterimState targetInterimState, Effect effect, double scalar);

    default void doInsight(Pawn targetPawn, Effect effect, ActionReturn actionReturn) {

        var defense = targetPawn.getPowerAbilityDefense(INSIGHT_ALL_DECKS);
        if (defense.containsKey(PowerEnums.PowerReturn.RESIST)) {
            actionReturn.targetPawnStates.get(0).addFlag(ActionFlag.RESISTED);
            return;
        }

        actionReturn.initInsight();
        var insight = new Insight();
        insight.insightType = effect.type;
        insight.pawnIndex = targetPawn.getIndex();
        actionReturn.initInsight();

        switch (effect.type) {
            case INSIGHT_STATUS -> {
                insight.effects = targetPawn.getNetEffects();
            }
            case INSIGHT_HAND -> {
                var cardHand = new CardHand();
                cardHand.ACTION_CARD_1 = targetPawn.getActionCard(1);
                cardHand.ACTION_CARD_2 = targetPawn.getActionCard(2);
                cardHand.ABILITY_CARD_1 = targetPawn.getAbilityCard(1);
                cardHand.ABILITY_CARD_2 = targetPawn.getAbilityCard(2);
                cardHand.POWER_CARD = targetPawn.getPowerCard();
                cardHand.TALISMAN_CARD = targetPawn.getTalisman();
                insight.cardHand = cardHand;
            }
            case INSIGHT_ACTION_DECK -> { insight.actionDeck = targetPawn.getActionDeck(); }
            case INSIGHT_ABILITY_DECK -> { insight.abilityDeck = targetPawn.getAbilityDeck(); }
            case INSIGHT_ALL_DECKS -> {
                insight.abilityDeck = targetPawn.getAbilityDeckStatic();
                insight.actionDeck = targetPawn.getActionDeckStatic();
                insight.powerDeck = targetPawn.getPowerDeckStatic();
            }
        }
        actionReturn.addInsight(insight);
    }
}
