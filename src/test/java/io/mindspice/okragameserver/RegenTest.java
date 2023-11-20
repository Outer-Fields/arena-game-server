package io.mindspice.okragameserver;

import io.mindspice.okragameserver.game.cards.PawnCard;
import io.mindspice.okragameserver.game.cards.TalismanCard;
import io.mindspice.okragameserver.game.cards.WeaponCard;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import org.junit.jupiter.api.Test;

import java.util.List;


public class RegenTest {



    @Test
    void regenTest(){
        Pawn pawn = new Pawn(PawnIndex.PAWN1, PawnCard.DARK_SENTINEL, TalismanCard.BALANCE_BEAD,
                WeaponCard.ZWEIHANDER_OF_EXCELLENCE, WeaponCard.ZWEIHANDER_OF_EXCELLENCE, List.of(), List.of(), List.of());

        System.out.println(pawn.getStatsMap());
        pawn.regenerate();
        System.out.println(pawn.getStatsMap());
    }
}
