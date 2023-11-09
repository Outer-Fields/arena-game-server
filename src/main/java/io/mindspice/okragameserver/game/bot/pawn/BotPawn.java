package io.mindspice.okragameserver.game.bot.pawn;


/*  This class stores information regarding the state of enemy pawnLoadouts and their known state.
*   It records status effects and attack information that a normal player would be aware of through play.
*   Effects that a bot cast are assumed to be active for average amount of time that they would be in real play.
*   Not all of the stats recorded here are correct, but are to provide some intelligible data for the bot to
*   function off of.
*
*/

import io.mindspice.okragameserver.game.cards.AbilityCard;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;

import java.util.ArrayList;
import java.util.List;

public class BotPawn {
    private final Pawn pawn;
    private final List<BotEffect> effects = new ArrayList<>();
    //private final EnumMap<StatType, Integer> stats = new EnumMap<>(StatType.class);

    public BotPawn(Pawn pawn) {
        this.pawn = pawn;
    }

    public void mirrorAbilityCard(AbilityCard card) {
        //FIXME add coded o mimic ability
    }

    public int getCardCount() {
        return pawn.getCardCount();
    }

    public void update() {
        for (BotEffect be : effects) {
            be.update();
        }
    }

    public Pawn getPawn() {
        return pawn;
    }

    public List<BotEffect> getEffects() {
        return effects;
    }

    public void addEffect(Effect effect) {
        effects.add(new BotEffect(effect));
    }

}
