package io.mindspice.okragameserver.game.bot.state;
import io.mindspice.okragameserver.game.enums.EffectType;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.gameroom.effect.ActiveEffect;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static io.mindspice.okragameserver.game.enums.StatType.HP;

public class BotEnemyState {

    //FIXME should extend idk why I did it this way......
    private final PlayerGameState gameState;

    public BotEnemyState(PlayerGameState gameState) {
        this.gameState = gameState;
    }

    public List<ActiveEffect> getNegativeStatus(PawnIndex pawnIndex) {
        return gameState.getPawn(pawnIndex).getStatusEffects()
                .stream()
                .filter(e -> e.getEffectType().isNegative)
                .collect(Collectors.toList());
    }

    public List<PawnIndex> getActivePawns(){
        return gameState.getLivingPawns()
                .stream()
                .map(Pawn::getIndex)
                .collect(Collectors.toList());
    }

    public List<ActiveEffect> getDisablingStatuses(PawnIndex pawnIndex) {
        var statuses = getNegativeStatus(pawnIndex);
        if (statuses.isEmpty()) return null;
        var disablingEffects = new ArrayList<ActiveEffect>();

        for(ActiveEffect e: statuses) {
            if (e.getType() == EffectType.SLEEP || e.getType() == EffectType.CONFUSION
                    || e.getType() == EffectType.PARALYSIS) {
                if (!e.getEffect().isRollOffChance && e.getRollOffRounds() > 1) {
                    disablingEffects.add(e);
                } else {
                    disablingEffects.add(e);
                }
            }
        }
        return disablingEffects;
    }

    public boolean hasMortallyLowPawn() {
        for (Pawn p : gameState.getLivingPawns()) {
            if (p.getStat(HP) > (p.getStatMax(HP) / 6)) return true;
        }
        return false;
    }

    public boolean isMortallyLowPawn(PawnIndex pawnIndex) {
        var pawn = gameState.getPawn(pawnIndex);
        return pawn.getStat(HP) < pawn.getStatMax(HP) / 6;
    }

    public Pawn getPawnHighestAP() {
        return gameState.getLivingPawns()
                .stream()
                .max(Comparator.comparingDouble(Pawn::getActionPotential)).get();
    }


    public PawnIndex mostImportantLowPawn(){
        var lowPawn = gameState.getLivingPawns()
                .stream()
                .filter(p -> p.getStat(HP) < (p.getStatMax(HP) / 1.5))
                .max(Comparator.comparingInt(Pawn::getCardCount)).orElse(null);

        if (lowPawn == null) {
            lowPawn = gameState.getLivingPawns()
                    .stream()
                    .sorted(Comparator.comparing(p -> p.getStat(HP)))
                    .max(Comparator.comparingInt(Pawn::getCardCount)).get();
        }
        return lowPawn.getIndex();
    }

    public int getTotalHP(){
        return gameState.getLivingPawns()
                .stream()
                .map(p -> p.getStat(HP))
                .reduce(0, Integer::sum);
    }

    public int activePawnCount() {
        return gameState.getLivingPawns().size();
    }

    public PlayerGameState getGameState() {
        return gameState;
    }
}



