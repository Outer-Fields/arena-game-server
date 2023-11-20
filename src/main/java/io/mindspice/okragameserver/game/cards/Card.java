package io.mindspice.okragameserver.game.cards;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.mindspice.okragameserver.game.enums.InvalidMsg;
import io.mindspice.okragameserver.game.enums.PawnIndex;
import io.mindspice.okragameserver.game.gameroom.action.ActionReturn;
import io.mindspice.okragameserver.game.gameroom.pawn.Pawn;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;
import io.mindspice.okragameserver.game.gameroom.state.PlayerGameState;

import java.util.ArrayList;
import java.util.List;

import static io.mindspice.okragameserver.game.enums.ActionClass.MULTI;
import static io.mindspice.okragameserver.game.enums.PawnIndex.*;
import static io.mindspice.okragameserver.game.enums.PawnIndex.PAWN2;


public interface Card {

    ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx, PawnIndex targetIdx);

    default ActionReturn playCard(PlayerGameState player, PlayerGameState target, PawnIndex playerIdx,
            PawnIndex targetIdx, CardStats stats) {

        ActionReturn actionReturn = new ActionReturn(
                stats.isPlayerMulti() ? getMultiInterim(player, playerIdx) : getSingleInterim(player, playerIdx),
                stats.isTargetMulti() ? getMultiInterim(target, targetIdx) : getSingleInterim(target, targetIdx),
                stats.getAnimation(),
                this.getName()
        );

        // Do Cost, abort if player can't afford, shouldn't happen unless player is doing abuse
        if (!stats.getCostLogic().doCost(player.getPawn(playerIdx), stats.getCost().asMap(), actionReturn)) {
            return ActionReturn.getInvalid(player.getPawn(playerIdx), InvalidMsg._INVALID_COST, getName());
        }

        if (stats.hasDamage()) {
            if (stats.getDamageClass() == MULTI) {
                stats.getDamageCalc().doDamage(
                        stats.getDamageLogic(),
                        actionReturn.playerPawnStates,
                        actionReturn.targetPawnStates,
                        this, stats.getSpecial()
                );
            } else {
                stats.getDamageCalc().doDamage(
                        stats.getDamageLogic(),
                        actionReturn.playerPawnStates,
                        new ArrayList<>(actionReturn.targetPawnStates.subList(0, 1)),
                        this,
                        stats.getSpecial()
                );
            }
        }

        if (stats.hasSelfDamage()) {
            if (stats.getSelfDamageClass() == MULTI) {
                stats.getSelfDamageCalc().doDamage(
                        stats.getSelfDamageLogic(),
                        actionReturn.playerPawnStates,
                        actionReturn.targetPawnStates,
                        this,
                        stats.getSpecial()
                );
            } else {
                stats.getSelfDamageCalc().doDamage(
                        stats.getSelfDamageLogic(),
                        actionReturn.playerPawnStates.subList(0, 1),
                        actionReturn.targetPawnStates,
                        this,
                        stats.getSpecial()
                );
            }
        }

        if (stats.hasTargetEffects()) {
            if (stats.getTargetEffectsClass() == MULTI) {
                stats.getTargetEffectCalc().doEffect(
                        stats.getTargetEffectLogic(),
                        actionReturn.playerPawnStates,
                        actionReturn.targetPawnStates,
                        this,
                        stats.getSpecial()
                );
            } else {
                stats.getTargetEffectCalc().doEffect(
                        stats.getTargetEffectLogic(),
                        actionReturn.playerPawnStates,
                        new ArrayList<>(actionReturn.targetPawnStates.subList(0, 1)),
                        this,
                        stats.getSpecial()
                );
            }
        }

        if (stats.hasNegSelfEffects()) {
            if (stats.getNegSelfEffectsClass() == MULTI) {
                stats.getNegSelfEffectCalc().doEffect(
                        stats.getNegSelfEffectLogic(),
                        actionReturn.playerPawnStates,
                        actionReturn.targetPawnStates,
                        this,
                        stats.getSpecial()
                );
            } else {
                stats.getNegSelfEffectCalc().doEffect(
                        stats.getNegSelfEffectLogic(),
                        actionReturn.playerPawnStates.subList(0, 1),
                        actionReturn.targetPawnStates,
                        this,
                        stats.getSpecial()
                );
            }
        }

        if (stats.hasPosSelfEffects()) {
            if (stats.getPosSelfEffectsClass() == MULTI) {
                stats.getPosSelfEffectCalc().doEffect(
                        stats.getPosSelfEffectLogic(),
                        actionReturn.playerPawnStates,
                        actionReturn.targetPawnStates,
                        this,
                        stats.getSpecial()
                );
            } else {
                stats.getPosSelfEffectCalc().doEffect(
                        stats.getPosSelfEffectLogic(),
                        actionReturn.playerPawnStates.subList(0, 1),
                        actionReturn.targetPawnStates,
                        this,
                        stats.getSpecial()
                );
            }
        }

        if (stats.hasPosSelfTargetEffects()) {
            // add the targeted player pawn if needed
            boolean hasPTarget = actionReturn.playerPawnStates.stream().anyMatch(p -> p.getPawnIndex() == targetIdx);
            if (!hasPTarget) { actionReturn.playerPawnStates.add(new PawnInterimState(player.getPawn(targetIdx))); }
            stats.getPosSelfTargetEffectCalc().doEffect(
                    stats.getPosSelfTargetEffectLogic(),
                    actionReturn.playerPawnStates, // the self target effect uses the playerpawn as targetpawns
                    actionReturn.playerPawnStates.stream().filter(p -> p.getPawnIndex() == targetIdx).toList(),
                    this,
                    stats.getSpecial()
            );
        }
        return actionReturn;
    }

    default List<PawnInterimState> getSingleInterim(PlayerGameState playerState, PawnIndex targetPawn) {
        return new ArrayList<>(List.of(new PawnInterimState(playerState.getPawn(targetPawn))));
    }

    default List<PawnInterimState> getMultiInterim(PlayerGameState stateFocus, PawnIndex targetPawn) {
        List<PawnInterimState> interimStates = new ArrayList<>();
        interimStates.add(new PawnInterimState(stateFocus.getPawn(targetPawn)));

        switch (interimStates.get(0).getPawnIndex()) {
            case PAWN1 -> {
                interimStates.add(new PawnInterimState(stateFocus.getPawn(PAWN2)));
                interimStates.add(new PawnInterimState(stateFocus.getPawn(PAWN3)));
            }
            case PAWN2 -> {
                interimStates.add(new PawnInterimState(stateFocus.getPawn(PAWN1)));
                interimStates.add(new PawnInterimState(stateFocus.getPawn(PAWN3)));
            }
            case PAWN3 -> {
                interimStates.add(new PawnInterimState(stateFocus.getPawn(PAWN2)));
                interimStates.add(new PawnInterimState(stateFocus.getPawn(PAWN1)));
            }
        }
        interimStates.removeIf(pis -> pis.getPawn().isDead());
        return interimStates;
    }

    default List<PawnInterimState> getSequentialMultiInterim(PlayerGameState stateFocus, PawnIndex targetPawn) {
        var orgStates = getMultiInterim(stateFocus, targetPawn);
        var seqStates = new ArrayList<PawnInterimState>();
        seqStates.add(orgStates.get(0));
        for (int i = 1; i < orgStates.size(); ++i) {
            if (Math.abs(orgStates.get(i).getPawnIndex().idx - orgStates.get(i - 1).getPawnIndex().idx) == 1) {
                seqStates.add(orgStates.get(i));
            } else {
                break;
            }
        }
        return seqStates;
    }

    default ActionReturn getSequentialActionReturn(PlayerGameState playerGameState, PawnIndex playerIndex,
            PlayerGameState targetGameState, PawnIndex targetIndex, String animation) {
        return new ActionReturn(
                getSequentialMultiInterim(playerGameState, playerIndex),
                getSequentialMultiInterim(targetGameState, targetIndex),
                animation,
                getName()
        );
    }

    default ActionReturn getSingleActionReturn(Pawn player, Pawn target, String animation) {
        return new ActionReturn(
                new ArrayList<>(List.of(new PawnInterimState(player))),
                new ArrayList<>(List.of(new PawnInterimState(target))),
                animation,
                getName()
        );
    }

    default ActionReturn getMultiActionReturn(PlayerGameState player, PawnIndex playerIdx,
            PlayerGameState target, PawnIndex targetIdx, String animation) {
        return new ActionReturn(
                getMultiInterim(player, playerIdx),
                getMultiInterim(target, targetIdx),
                animation,
                getName()
        );
    }

    CardStats getStats();

    int getLevel();

    String getUid();

    ObjectNode toJson();

    String getName();
}
