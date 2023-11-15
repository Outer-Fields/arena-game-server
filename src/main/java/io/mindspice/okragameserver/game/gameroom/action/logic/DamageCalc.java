package io.mindspice.okragameserver.game.gameroom.action.logic;

import io.mindspice.okragameserver.game.cards.Card;
import io.mindspice.okragameserver.game.enums.ActionClass;
import io.mindspice.okragameserver.game.enums.ActionType;
import io.mindspice.okragameserver.game.enums.Alignment;
import io.mindspice.okragameserver.game.enums.SpecialAction;
import io.mindspice.okragameserver.game.gameroom.action.StatMap;
import io.mindspice.okragameserver.game.gameroom.gameutil.LuckModifier;
import io.mindspice.okragameserver.game.gameroom.gameutil.Utils;
import io.mindspice.okragameserver.game.gameroom.state.PawnInterimState;

import java.util.ArrayList;
import java.util.List;

import static io.mindspice.okragameserver.game.enums.StatType.LP;


/* Attempted to build extend functionalities and not repeat. Things like vital chances calls its own base class that
 holds existing classes with that logic, or creates them if needed. Those class do the specific logic related to vital change
  and then call the basic target/basic self classes that hold references to the target/self base damage classes.
that themselves call
 */


public class DamageCalc {

    private DamageCalc() {
    }

    //////////////////////////////
    //     Utility Classes      //
    // Holds/Creates References //
    //     To Base Instances    //
    //////////////////////////////
    public static class BasicTarget {
        private static final BasicTarget instance = new BasicTarget();
        private static final IDamageCalc[] classRefs = new IDamageCalc[101];
        public final boolean isSelf = false;

        private BasicTarget() {
        }

        public static IDamageCalc GET(int i) {
            if (i < 1 || i > 100) {
                throw new IllegalStateException("Multi get must be 0-100");
            }
            var multi = classRefs[i];
            if (multi == null) {
                multi = new MultiTargetBase(i);
                classRefs[i] = multi;
            }
            return multi;
        }
    }


    public static class BasicSelf {
        private static final BasicSelf instance = new BasicSelf();
        private static final IDamageCalc[] classRefs = new IDamageCalc[10011];
        public final boolean isSelf = true;

        private BasicSelf() {
        }

        public static IDamageCalc GET(int i) {
            if (i < 1 || i > 100) {
                throw new IllegalStateException("Multi get must be 0-100");
            }
            var multi = classRefs[i];
            if (multi == null) {
                multi = new MultiSelfBase(i);
                classRefs[i] = multi;
            }
            return multi;
        }
    }


    public static class VitalChance {
        private static final VitalChance instance = new VitalChance();
        private static final List<VitalChanceBase> classRefs = new ArrayList<>();
        public final boolean isSelf = false;

        private VitalChance() {
        }

        public static IDamageCalc GET(double chance, int multi) {
            for (var ref : classRefs) {
                if (ref.equals(chance, multi)) {
                    return ref;
                }
            }
            var newRef = new VitalChanceBase(chance, multi);
            classRefs.add(newRef);
            return newRef;
        }
    }


    public static class VitalChanceSelf {
        private static final VitalChanceSelf instance = new VitalChanceSelf();
        private static final List<VitalChanceSelfBase> classRefs = new ArrayList<>();
        public final boolean isSelf = true;

        private VitalChanceSelf() {
        }

        public static IDamageCalc GET(double chance, int multi) {
            for (var ref : classRefs) {
                if (ref.equals(chance, multi)) {
                    return ref;
                }
            }
            var newRef = new VitalChanceSelfBase(chance, multi);
            classRefs.add(newRef);
            return newRef;
        }
    }


    public static class SeqTargetAndSelf implements IDamageCalc, AlignmentScale {

        public static final DamageCalc.SeqTargetAndSelf GET = new DamageCalc.SeqTargetAndSelf();

        private SeqTargetAndSelf() {
        }

        @Override
        public void doDamage(IDamage dmgLogic, List<PawnInterimState> playerPawnStates, List<PawnInterimState> targetPawnStates, Card card, SpecialAction special) {
            StatMap damage = card.getStats().getDamage();
            StatMap selfDamage = card.getStats().getSelfDamage();
            Alignment alignment = card.getStats().getAlignment();
            ActionType actionType = card.getStats().getActionType();

            var playerPawn = playerPawnStates.get(0).getPawn();
            // Calculate targets damage
            int targetItr = card.getStats().getDamageClass() == ActionClass.MULTI ? targetPawnStates.size() : 1;
            for (int i = 0; i < targetItr; ++i) {
                var targetState = targetPawnStates.get(i);
                // Calculate Chance
                var alignmentChance = alignmentChance(
                        targetState.getPawn(),
                        alignment,
                        targetState == targetPawnStates.get(0) ? damage.chance : damage.altChance
                );
                if (LuckModifier.chanceCalc(alignmentChance, targetState.getPawn().getStat(LP))) {
                    // Calculate Scale, checks if main target, if so scalar, else alt scalar
                    double scalar = 0.0;
                    if (targetState == targetPawnStates.get(0)) {
                        scalar = alignmentScaleEnemy(playerPawn, targetState.getPawn(), damage.scalar, alignment);
                    } else {
                        scalar = alignmentScaleEnemy(playerPawn, targetState.getPawn(), damage.altScalar, alignment);
                    }
                    var damageMap = damage.asMap();
                    Utils.scaleDamageMap(damageMap, (scalar * LuckModifier.luckMod(playerPawn.getStat(LP))));
                    dmgLogic.doDamage(playerPawnStates.get(0), targetState, actionType, special, damageMap, false);
                } else {
                    break;
                }
            }
            int selfItr = card.getStats().getSelfDamageClass() == ActionClass.MULTI ? playerPawnStates.size() : 1;
            for (int i = 0; i < selfItr; ++i) {
                var playerState = playerPawnStates.get(i);
                // Calculate Chance, inverse used for self damage
                double alignmentChance = alignmentChanceNegSelf(
                        playerState.getPawn(),
                        alignment,
                        playerState == playerPawnStates.get(0) ? selfDamage.chance : selfDamage.altChance
                );
                if (LuckModifier.inverseChanceCalc(alignmentChance, playerState.getPawn().getStat(LP))) {
                    // Calculate Scale, checks if main target, if so scalar, else alt scalar
                    var scalar = 0.0;
                    if (playerState == playerPawnStates.get(0)) {
                        scalar = alignmentScaleNegSelf(playerPawn, playerState.getPawn(), selfDamage.scalar, alignment);
                    } else {
                        scalar = alignmentScaleNegSelf(playerPawn, playerState.getPawn(), selfDamage.altScalar, alignment);
                    }
                    var damageMap = selfDamage.asMap();
                    Utils.scaleDamageMap(damageMap, (scalar * LuckModifier.luckMod(playerPawn.getStat(LP))));
                    dmgLogic.doDamage(playerPawnStates.get(0), playerState, actionType, special, damageMap, true);
                } else {
                    break;
                }
            }
        }

        // returns false, but does do self damage
        @Override
        public boolean isSelf() {
            return false;
        }

        @Override
        public int getMulti() {
            return 1;
        }

        @Override
        public boolean isVital() {
            return false;
        }
    }


    public static class SequentialTarget implements IDamageCalc, AlignmentScale {

        public static final SequentialTarget GET = new SequentialTarget();

        private SequentialTarget() {
        }

        @Override
        public void doDamage(IDamage dmgLogic, List<PawnInterimState> playerPawnStates, List<PawnInterimState> targetPawnStates, Card card, SpecialAction special) {
            StatMap damage = card.getStats().getDamage();
            Alignment alignment = card.getStats().getAlignment();
            ActionType actionType = card.getStats().getActionType();

            var playerPawn = playerPawnStates.get(0).getPawn();
            // Calculate targets damage
            int itr = card.getStats().getDamageClass() == ActionClass.MULTI ? targetPawnStates.size() : 1;
            for (int i = 0; i < itr; ++i) {
                var targetState = targetPawnStates.get(i);
                // Calculate Chance
                double alignmentChance = alignmentChance(
                        targetState.getPawn(),
                        alignment,
                        targetState == targetPawnStates.get(0) ? damage.chance : damage.altChance
                );
                if (LuckModifier.chanceCalc(alignmentChance, targetState.getPawn().getStat(LP))) {
                    // Calculate Scale, checks if main target, if so scalar, else alt scalar
                    double scalar = 0.0;
                    if (targetState == targetPawnStates.get(0)) {
                        scalar = alignmentScaleEnemy(playerPawn, targetState.getPawn(), damage.scalar, alignment);
                    } else {
                        scalar = alignmentScaleEnemy(playerPawn, targetState.getPawn(), damage.altScalar, alignment);
                    }
                    var damageMap = damage.asMap();
                    Utils.scaleDamageMap(damageMap, (scalar * LuckModifier.luckMod(playerPawn.getStat(LP))));
                    dmgLogic.doDamage(playerPawnStates.get(0), targetState, actionType, special, damageMap, false);
                } else {
                    return;
                }
            }
        }

        @Override
        public boolean isSelf() {
            return false;
        }

        @Override
        public int getMulti() {
            return 1;
        }

        @Override
        public boolean isVital() {
            return false;
        }
    }


    public static class SequentialSelf implements IDamageCalc, AlignmentScale {
        public static final SequentialSelf GET = new SequentialSelf();

        public SequentialSelf() {
        }

        @Override
        public void doDamage(IDamage dmgLogic, List<PawnInterimState> playerPawnStates, List<PawnInterimState> targetPawnStates, Card card, SpecialAction special) {
            StatMap selfDamage = card.getStats().getSelfDamage();
            Alignment alignment = card.getStats().getAlignment();
            ActionType actionType = card.getStats().getActionType();

            var playerPawn = playerPawnStates.get(0).getPawn();
            // Calculate targets damage
            int itr = card.getStats().getSelfDamageClass() == ActionClass.MULTI ? playerPawnStates.size() : 1;
            for (int i = 0; i < itr; ++i) {
                var playerState = playerPawnStates.get(i);
                // Calculate Chance, inverse used for self damage
                double alignmentChance = alignmentChanceNegSelf(
                        playerState.getPawn(),
                        alignment,
                        playerState == playerPawnStates.get(0) ? selfDamage.chance : selfDamage.altChance
                );
                if (LuckModifier.inverseChanceCalc(alignmentChance, playerState.getPawn().getStat(LP))) {
                    // Calculate Scale, checks if main target, if so scalar, else alt scalar
                    double scalar = 0.0;
                    if (playerState == playerPawnStates.get(0)) {
                        scalar = alignmentScaleNegSelf(playerPawn, playerState.getPawn(), selfDamage.scalar, alignment);
                    } else {
                        scalar = alignmentScaleNegSelf(playerPawn, playerState.getPawn(), selfDamage.altScalar, alignment);
                    }
                    var damageMap = selfDamage.asMap();
                    Utils.scaleDamageMap(damageMap, (scalar * LuckModifier.luckMod(playerPawn.getStat(LP))));
                    dmgLogic.doDamage(playerPawnStates.get(0), playerState, actionType, special, damageMap, true);
                } else {
                    return;
                }
            }
        }

        @Override
        public boolean isSelf() {
            return true;
        }

        @Override
        public int getMulti() {
            return 1;
        }

        @Override
        public boolean isVital() {
            return false;
        }
    }


    //////////////////
    // Base Classes //
    //////////////////
    private record VitalChanceBase(double chance, int multi) implements IDamageCalc, AlignmentScale {

        public boolean equals(double chance, int multi) {
            return this.chance == chance && this.multi == multi;
        }

        @Override
        public void doDamage(IDamage dmgLogic, List<PawnInterimState> playerPawnStates, List<PawnInterimState> targetPawnStates, Card card, SpecialAction special) {
            Alignment alignment = card.getStats().getAlignment();

            double vitalChance = alignmentChance(playerPawnStates.get(0).getPawn(), alignment, chance);
            if (LuckModifier.chanceCalc(vitalChance, playerPawnStates.get(0).getPawn().getStat(LP))) {
                special = SpecialAction.IGNORE_DP;
            }
            BasicTarget.GET(multi).doDamage(dmgLogic, playerPawnStates, targetPawnStates, card, special);
        }

        @Override
        public boolean isSelf() {
            return false;
        }

        @Override
        public int getMulti() {
            return multi;
        }

        @Override
        public boolean isVital() {
            return true;
        }
    }


    private record VitalChanceSelfBase(double chance, int multi) implements IDamageCalc, AlignmentScale {

        public boolean equals(double chance, int multi) {
            return this.chance == chance && this.multi == multi;
        }

        @Override
        public void doDamage(IDamage dmgLogic, List<PawnInterimState> playerPawnStates, List<PawnInterimState> targetPawnStates, Card card, SpecialAction special) {
            Alignment alignment = card.getStats().getAlignment();

            double vitalChance = alignmentChanceNegSelf(playerPawnStates.get(0).getPawn(), alignment, chance);
            if (LuckModifier.chanceCalc(vitalChance, playerPawnStates.get(0).getPawn().getStat(LP))) {
                special = SpecialAction.IGNORE_DP;
            }
            BasicSelf.GET(multi).doDamage(dmgLogic, playerPawnStates, targetPawnStates, card, special);
        }

        @Override
        public boolean isSelf() {
            return true;
        }

        @Override
        public int getMulti() {
            return multi;
        }

        @Override
        public boolean isVital() {
            return true;
        }
    }


    private record MultiTargetBase(int multiAmount) implements IDamageCalc, AlignmentScale {
        @Override
        public void doDamage(IDamage dmgLogic, List<PawnInterimState> playerPawnStates, List<PawnInterimState> targetPawnStates, Card card, SpecialAction special) {
            StatMap damage = card.getStats().getDamage();
            Alignment alignment = card.getStats().getAlignment();
            ActionType actionType = card.getStats().getActionType();

            var playerPawn = playerPawnStates.get(0).getPawn();
            // Calculate targets damage, itr calculates iterations if damage is on multiple pawns
            int itr = card.getStats().getDamageClass() == ActionClass.MULTI ? targetPawnStates.size() : 1;
            for (int i = 0; i < itr; i++) {
                var targetState = targetPawnStates.get(i);
                for (int j = 0; j < multiAmount; ++j) {
                    // Calculate Chance
                    double alignmentChance = alignmentChance(
                            targetState.getPawn(),
                            alignment,
                            targetState == targetPawnStates.get(0) ? damage.chance : damage.altChance
                    );
                    if (LuckModifier.chanceCalc(alignmentChance, targetState.getPawn().getStat(LP))) {
                        // Calculate Scale, checks if main target, if so scalar, else alt scalar
                        double scalar = 0.0;
                        if (targetState == targetPawnStates.get(0)) {
                            scalar = alignmentScaleEnemy(playerPawn, targetState.getPawn(), damage.scalar, alignment);
                        } else {
                            scalar = alignmentScaleEnemy(playerPawn, targetState.getPawn(), damage.altScalar, alignment);
                        }
                        var damageMap = damage.asMap();
                        Utils.scaleDamageMap(damageMap, (scalar * LuckModifier.luckMod(playerPawn.getStat(LP))));
                        dmgLogic.doDamage(playerPawnStates.get(0), targetState, actionType, special, damageMap, false);
                    }
                }
            }
        }

        @Override
        public boolean isSelf() {
            return false;
        }

        @Override
        public int getMulti() {
            return multiAmount;
        }

        @Override
        public boolean isVital() {
            return false;
        }
    }


    private record MultiSelfBase(int multiAmount) implements IDamageCalc, AlignmentScale {
        @Override
        public void doDamage(IDamage dmgLogic, List<PawnInterimState> playerPawnStates, List<PawnInterimState> targetPawnStates, Card card, SpecialAction special) {
            StatMap selfDamage = card.getStats().getSelfDamage();
            Alignment alignment = card.getStats().getAlignment();
            ActionType actionType = card.getStats().getActionType();

            // Calculate self damage, itr calculates iterations if damage is on multiple pawns
            var playerPawn = playerPawnStates.get(0).getPawn();
            int itr = card.getStats().getSelfDamageClass() == ActionClass.MULTI ? playerPawnStates.size() : 1;
            for (int i = 0; i < itr; ++i) {
                var playerState = playerPawnStates.get(i);
                for (int j = 0; j < multiAmount; ++j) {
                    // Calculate Chance, inverse used for self damage
                    double alignmentChance = alignmentChanceNegSelf(
                            playerState.getPawn(),
                            alignment,
                            playerState == playerPawnStates.get(0) ? selfDamage.chance : selfDamage.altChance
                    );
                    if (LuckModifier.inverseChanceCalc(alignmentChance, playerState.getPawn().getStat(LP))) {
                        // Calculate Scale, checks if main target, if so scalar, else alt scalar
                        double scalar = 0.0;
                        if (playerState == playerPawnStates.get(0)) {
                            scalar = alignmentScaleNegSelf(playerPawn, playerState.getPawn(), selfDamage.scalar, alignment);
                        } else {
                            scalar = alignmentScaleNegSelf(playerPawn, playerState.getPawn(), selfDamage.altScalar, alignment);
                        }
                        var damageMap = selfDamage.asMap();
                        Utils.scaleDamageMap(damageMap, (scalar * LuckModifier.luckMod(playerPawn.getStat(LP))));
                        dmgLogic.doDamage(playerPawnStates.get(0), playerState, actionType, special, damageMap, true);
                    }
                }
            }
        }

        @Override
        public boolean isSelf() {
            return true;
        }

        @Override
        public int getMulti() {
            return multiAmount;
        }

        @Override
        public boolean isVital() {
            return false;
        }
    }
}