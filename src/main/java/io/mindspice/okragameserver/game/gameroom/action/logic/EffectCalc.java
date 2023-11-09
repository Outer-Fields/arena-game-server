package io.mindspice.okragameserver.game.gameroom.action.logic;

import io.mindspice.okragameserver.game.cards.Card;
import io.mindspice.okragameserver.game.enums.ActionClass;
import io.mindspice.okragameserver.game.enums.Alignment;
import io.mindspice.okragameserver.game.enums.SpecialAction;
import io.mindspice.okragameserver.game.gameroom.effect.Effect;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;
import io.mindspice.okragameserver.game.gameroom.gameutil.LuckModifier;

import java.util.List;

import static io.mindspice.okragameserver.game.enums.StatType.LP;


public class EffectCalc {

    private EffectCalc() {
    }

    public static class BasicTarget {
        private static final BasicTarget instance = new BasicTarget();
        private static final IEffectCalc[] classRefs = new IEffectCalc[101];

        private BasicTarget() { }

        public static IEffectCalc GET(int i) {
            if (i < 1 || i > 100) { throw new IllegalStateException("Multi get must be 0-100"); }
            var multi = classRefs[i];
            if (multi == null) {
                multi = new MultiTargetBase(i);
                classRefs[i] = multi;
            }
            return multi;
        }
    }


    public static class BasicSelfNeg {
        private static final BasicSelfNeg instance = new BasicSelfNeg();
        private static final IEffectCalc[] classRefs = new IEffectCalc[101];

        private BasicSelfNeg() { }

        public static IEffectCalc GET(int i) {
            if (i < 1 || i > 100) { throw new IllegalStateException("Multi get must be 0-100"); }
            var multi = classRefs[i];
            if (multi == null) {
                multi = new MultiSelfNegBase(i);
                classRefs[i] = multi;
            }
            return multi;
        }
    }


    public static class BasicSelfPos {
        private static final BasicSelfPos instance = new BasicSelfPos();
        private static final IEffectCalc[] classRefs = new IEffectCalc[101];

        private BasicSelfPos() { }

        public static IEffectCalc GET(int i) {
            if (i < 1 || i > 100) { throw new IllegalStateException("Multi get must be 0-100"); }
            var multi = classRefs[i];
            if (multi == null) {
                multi = new MultiSelfPosBase(i);
                classRefs[i] = multi;
            }
            return multi;
        }
    }


    public static class BasicSelfPosTarget {
        private static final BasicSelfPosTarget instance = new BasicSelfPosTarget();
        private static final IEffectCalc[] classRefs = new IEffectCalc[101];

        private BasicSelfPosTarget() { }

        public static IEffectCalc GET(int i) {
            if (i < 1 || i > 100) { throw new IllegalStateException("Multi get must be 0-100"); }
            var multi = classRefs[i];
            if (multi == null) {
                multi = new MultiSelfPosTargetBase(i);
                classRefs[i] = multi;
            }
            return multi;
        }
    }


    private record MultiTargetBase(int multiAmount) implements IEffectCalc, AlignmentScale {
        @Override
        public void doEffect(IEffect effectLogic, List<PawnInterimState> playerPawnStates, List<PawnInterimState> targetPawnStates, Card card, SpecialAction special) {

            Alignment alignment = card.getStats().getAlignment();
            Effect[] effects = card.getStats().getTargetEffects();

            var playerPawn = playerPawnStates.get(0).getPawn();
            for (Effect effect : effects) {
                int itr = effect.actionClass == ActionClass.MULTI ? targetPawnStates.size() : 1;
                for (int i = 0; i < itr; ++i) {
                    var targetState = targetPawnStates.get(i);
                    for (int j = 0; j < multiAmount; ++j) {
                        double alignmentChance = alignmentChance(
                                targetState.getPawn(),
                                alignment,
                                targetState == targetPawnStates.get(0) ? effect.chance : effect.altChance
                        );
                        if (LuckModifier.chanceCalc(alignmentChance, playerPawn.getStat(LP))) {
                            double luckScalar;
                            if (targetState == targetPawnStates.get(0)) {
                                luckScalar = effect.scalar * LuckModifier.luckMod(playerPawn.getStat(LP));
                            } else {
                                luckScalar = effect.altScalar * LuckModifier.luckMod(playerPawn.getStat(LP));
                            }
                            luckScalar = alignmentScaleEnemy(playerPawn, targetState.getPawn(), luckScalar, alignment);
                            //effect cloned to use mutably
                            effectLogic.doEffect(playerPawnStates.get(0), targetState, effect.clone(), luckScalar);
                        }
                    }
                }
            }
        }

        @Override
        public boolean isSelf() {
            return false;
        }

        @Override
        public boolean isPos() {
            return false;
        }

        @Override
        public int getMulti() {
            return multiAmount;
        }
    }


    private record MultiSelfNegBase(int multiAmount) implements IEffectCalc, AlignmentScale {
        @Override
        public void doEffect(IEffect effectLogic, List<PawnInterimState> playerPawnStates,
                List<PawnInterimState> targetPawnStates, Card card, SpecialAction special) {
            Alignment alignment = card.getStats().getAlignment();
            Effect[] effects = card.getStats().getNegSelfEffects();

            var playerPawn = playerPawnStates.get(0).getPawn();
            for (Effect effect : effects) {
                int itr = effect.actionClass == ActionClass.MULTI ? playerPawnStates.size() : 1;
                for (int i = 0; i < itr; ++i) {
                    var playerState = playerPawnStates.get(i);
                    for (int j = 0; j < multiAmount; ++j) {
                        double alignmentChance =
                                alignmentChanceNegSelf(
                                        playerState.getPawn(),
                                        alignment,
                                        playerState == playerPawnStates.get(0) ? effect.chance : effect.altChance
                                );
                        if (LuckModifier.inverseChanceCalc(alignmentChance, playerPawn.getStat(LP))) {
                            double luckScalar;
                            if (playerState == playerPawnStates.get(0)) {
                                luckScalar = effect.scalar * LuckModifier.inverseLuckMod(playerPawn.getStat(LP));
                            } else {
                                luckScalar = effect.altScalar * LuckModifier.inverseLuckMod(playerPawn.getStat(LP));
                            }
                            luckScalar = alignmentScaleNegSelf(playerPawn, playerState.getPawn(), luckScalar, alignment);
                            //effect cloned to use mutably
                            effectLogic.doEffect(playerPawnStates.get(0), playerState, effect.clone(), luckScalar);
                        }
                    }
                }
            }
        }

        @Override
        public boolean isSelf() {
            return true;
        }

        @Override
        public boolean isPos() {
            return false;
        }

        @Override
        public int getMulti() {
            return multiAmount;
        }
    }


    private record MultiSelfPosBase(int multiAmount) implements IEffectCalc, AlignmentScale {
        @Override
        public void doEffect(IEffect effectLogic, List<PawnInterimState> playerPawnStates,
                List<PawnInterimState> targetPawnStates, Card card, SpecialAction special) {
            Alignment alignment = card.getStats().getAlignment();
            Effect[] effects = card.getStats().getPosSelfEffects();

            var playerPawn = playerPawnStates.get(0).getPawn();
            for (Effect effect : effects) {
                int itr = effect.actionClass == ActionClass.MULTI ? playerPawnStates.size() : 1;
                for (int i = 0; i < itr; ++i) {
                    var playerState = playerPawnStates.get(i);
                    for (int j = 0; j < multiAmount; ++j) {
                        double alignmentChance = alignmentChance(
                                playerState.getPawn(),
                                alignment,
                                playerState == playerPawnStates.get(0) ? effect.chance : effect.altChance
                        );
                        if (LuckModifier.chanceCalc(alignmentChance, playerPawn.getStat(LP))) {
                            double luckScalar;
                            if (playerState == playerPawnStates.get(0)) {
                                luckScalar = effect.scalar * LuckModifier.luckMod(playerPawn.getStat(LP));
                            } else {
                                luckScalar = effect.altScalar * LuckModifier.luckMod(playerPawn.getStat(LP));
                            }
                            luckScalar = alignmentScalePosSelf(playerPawn, playerState.getPawn(), luckScalar, alignment);
                            //effect cloned to use mutably
                            effectLogic.doEffect(playerPawnStates.get(0), playerState,
                                                 effect.clone(), luckScalar);
                        }
                    }
                }
            }
        }

        @Override
        public boolean isSelf() {
            return true;
        }

        @Override
        public boolean isPos() {
            return true;
        }

        @Override
        public int getMulti() {
            return multiAmount;
        }
    }


    // The target pawn state is the targeted player pawn when this method is called
    private record MultiSelfPosTargetBase(int multiAmount) implements IEffectCalc, AlignmentScale {
        @Override
        public void doEffect(IEffect effectLogic, List<PawnInterimState> playerPawnStates,
                List<PawnInterimState> targetPawnStates, Card card, SpecialAction special) {
            Alignment alignment = card.getStats().getAlignment();
            Effect[] effects = card.getStats().getPosSelfTargetEffects();

            var playerPawn = playerPawnStates.get(0).getPawn();
            var targetPawnState = targetPawnStates.get(0);
            for (Effect effect : effects) {
                for (int j = 0; j < multiAmount; ++j) {
                    double alignmentChance = alignmentChance(
                            playerPawn,
                            alignment,
                            effect.chance
                    );
                    if (LuckModifier.chanceCalc(alignmentChance, playerPawn.getStat(LP))) {
                        double luckScalar = effect.scalar * LuckModifier.luckMod(playerPawn.getStat(LP));
                        luckScalar = alignmentScalePosSelf(playerPawn, playerPawn, luckScalar, alignment);
                        effectLogic.doEffect(playerPawnStates.get(0), targetPawnState,
                                             effect.clone(), luckScalar);
                    }
                }
            }

        }

        @Override
        public boolean isSelf() {
            return true;
        }

        @Override
        public boolean isPos() {
            return true;
        }

        @Override
        public int getMulti() {
            return multiAmount;
        }
    }
}
