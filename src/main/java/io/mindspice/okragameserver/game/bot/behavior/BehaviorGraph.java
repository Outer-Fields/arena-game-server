package io.mindspice.okragameserver.game.bot.behavior;

import io.mindspice.okragameserver.game.bot.behavior.core.ActionNode;
import io.mindspice.okragameserver.game.bot.behavior.core.DecisionNode;
import io.mindspice.okragameserver.game.bot.behavior.core.Node;
import io.mindspice.okragameserver.game.bot.behavior.core.RootNode;
import io.mindspice.okragameserver.game.bot.behavior.logic.Actions;
import io.mindspice.okragameserver.game.bot.behavior.logic.Decision;
import io.mindspice.okragameserver.game.bot.behavior.logic.Decisions;
import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.bot.state.TreeFocusState;
import io.mindspice.okragameserver.game.enums.PawnIndex;


public class BehaviorGraph {

    private static final BehaviorGraph behaviorGraph = new BehaviorGraph();
    private final Node root;

    private BehaviorGraph() {
        root = init();
    }

    public void printStructure() {
        root.printGraph(0);
    }

    public static BehaviorGraph getInstance() {
        return behaviorGraph;
    }

    public void playTurn(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
        root.travel(botPlayerState, focusState, selfIndex);
    }

    public void doRandom(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex) {
        random.travel(botPlayerState, focusState, selfIndex);
        doRandom.travel(botPlayerState, focusState, selfIndex);
    }

    // @formatter:off
    public RootNode init() {
        return new GraphBuilder()
                .addChild(pawnConfused)
                    .addChild(confusedRollsOffSoon)
                        .addLeaf(doNothing)
                    .addSibling(confusedOver67)
                        .addLeaf(doNothing)
                    .addSibling(confusedOver50)
                        .addLeaf(doCure)
                    .addSibling(playerHasMortalPawns)
                        .addChild(focusPlayerMortal)
                            .addLeaf(doCure)
                        .addSibling(focusEnemyHighestAP)
                            .addLeaf(doAttack)
                        .stepBack()
                    .addSibling(playerCountAndHpLower)
                        .addChild(canHealSetFocus)
                            .addLeaf(doCure)
                        .stepBack()
                    .addSibling(playerCountAndHpHigher)
                        .addChild(checkAndFocusEnemyMortal)
                            .addLeaf(doAttack)
                        .stepBack()
                    .stepBack()
                .addSibling(playerHasMortalPawns)
                    .addChild(focusPlayerMortal)
                        .addLeaf(doCure)
                    .addSibling(focusEnemyHighestAP)
                        .addLeaf(doAttack)
                    .stepBack()
                .addSibling(playerHasNegativeStatus)
                    .addChild(canCure)
                        .addLeaf(doCure)
                    .addSibling(focusEnemyHighestAP)
                        .addLeaf(doAttack)
                    .stepBack()
                .addSibling(checkAndFocusEnemyMortal)
                    .addLeaf(doAttack)
                .addSibling(player50PctLessHP)
                    .addChild(canHealSetFocus)
                        .addLeaf(doCure)
                    .addSibling(canDemise)
                        .addLeaf(doSetAction)
                    .addSibling(checkAndFocusEnemyMortal)
                        .addLeaf(doAttack)
                    .addSibling(focusEnemyHighestAP)
                        .addLeaf(doAttack)
                    .addSibling(canBuff50Pct)
                        .addLeaf(doBuff)
                    .addSibling(new DecisionNode("50% chance", new Decision[]{Decisions.chance50Pct}))
                        .addChild(canCurse)
                            .addLeaf(doCurse)
                        .stepBack()
                    .stepBack()
                .addSibling(player30PctLessHP)
                    .addChild(new DecisionNode("50% chance", new Decision[]{Decisions.chance50Pct}))
                        .addChild(canHealSetFocus)
                            .addLeaf(doCure)
                        .stepBack()
                    .addSibling(canBuff50Pct)
                        .addLeaf(doBuff)
                    .addSibling(new DecisionNode("50% chance", new Decision[]{Decisions.chance50Pct}))
                        .addChild(canCurse)
                            .addLeaf(doCurse)
                        .addSibling(canDoPlunder)
                            .addLeaf(doSetAction)
                        .addSibling(canCapitulate)
                            .addLeaf(doSetAction)
                        .stepBack()
                    .addSibling(focusEnemyHighestAP)
                        .addLeaf(doAttack)
                    .stepBack()
                .addSibling(playerEqualHP)
                    .addChild(canCurse)
                        .addLeaf(doCurse)
                    .addSibling(canBuff50Pct)
                        .addLeaf(doBuff)
                    .addSibling(focusEnemyHighestAP)
                        .addLeaf(doAttack)
                    .stepBack()
                .addSibling(player30PctMoreHP)
                    .addChild(new DecisionNode("50% chance", new Decision[]{Decisions.chance50Pct}))
                        .addChild(canCurse)
                            .addLeaf(doCurse)
                        .stepBack()
                    .addSibling(focusEnemyHighestAP)
                        .addLeaf(doAttack)
                    .stepBack()
                .addSibling(player50PctMoreHP)
                    .addChild(focusEnemyHighestAP)
                        .addLeaf(doAttack)
                    .stepBack()
                .addSibling(random)
                    .addLeaf(doRandom)
                .addLeaf(doNothing)
                .build();
    }
    // @formatter:on

    /* Entrance Nodes */
    public final Node pawnConfused = new DecisionNode("selfConfused", new Decision[]{
            Decisions.isSelfConfused
    });

    public final Node playerHasMortalPawns = new DecisionNode("PlayerMortalPawns", new Decision[]{
            Decisions.playerHasMortalPawns
    });

    public final Node playerHasNegativeStatus = new DecisionNode("PlayerHasNegStatus", new Decision[]{
            Decisions.playerNegStatusPawns,
    });

    public final Node player50PctLessHP = new DecisionNode("Player50%Less", new Decision[]{
            Decisions.player50PctLowerHP
    });

    public final Node player30PctLessHP = new DecisionNode("Player30%Less", new Decision[]{
            Decisions.player30PctLowerHP
    });

    public final Node playerEqualHP = new DecisionNode("playerEqual", new Decision[]{
            Decisions.playerEqualHP
    });

    public final Node player30PctMoreHP = new DecisionNode("Player30%More", new Decision[]{
            Decisions.player30PctHigherHP
    });

    public final Node player50PctMoreHP = new DecisionNode("Player50%More", new Decision[]{
            Decisions.player50PctHigherHP
    });

    public final Node random = new DecisionNode("DoRandom", new Decision[]{
            Decisions.getRandomEnemy
    });

    /* Interim Nodes */

    public final Node playerCountAndHpLower = new DecisionNode("playerCount&HpLower", new Decision[]{
            Decisions.playerCountLower,
            Decisions.playerTotalHpLower
    });

    public final Node playerCountAndHpHigher = new DecisionNode("PlayerCount&HpHigher ", new Decision[]{
            Decisions.playerCountHigher,
            Decisions.playerTotalHPHigher
    });

    public final Node focusPlayerMortal = new DecisionNode("FocusPlayerMortal", new Decision[]{
            Decisions.setFocusPlayerMortal
    });

    public final Node confusedOver50 = new DecisionNode("Confused<50%,CanCure,FocusSelf", new Decision[]{
            Decisions.confusionUnder50Pct,
            Decisions.canCureConfusion,
            Decisions.setFocusSelf
    });

    public final Node confusedOver67 = new DecisionNode("Confused>67%", new Decision[]{
            Decisions.confusionOver67Pct
    });

    public final Node canHealSetFocus = new DecisionNode("CanHeal,SetFocus", new Decision[]{
            Decisions.canHeal,
            Decisions.setLowPawnHighestAP
    });

    public final Node canCure = new DecisionNode("CanCureEffect", new Decision[]{
            Decisions.canCure
    });

    public final Node checkAndFocusEnemyMortal = new DecisionNode("CheckAndFocusMortal", new Decision[]{
            Decisions.checkAndSetEnemyMortal
    });

    public final Node focusEnemyHighestAP = new DecisionNode("FocusEnemyHighestAP", new Decision[]{
            Decisions.setEnemyHighestAP
    });

    public final Node canBuff50Pct = new DecisionNode("CanBuff50/50", new Decision[]{
            Decisions.chance50Pct,
            Decisions.canBuff
    });

    public final Node canCurse = new DecisionNode("CanCurse", new Decision[]{
            Decisions.canCurse,
            Decisions.setEnemyHighestAP
    });

    public final Node confusedRollsOffSoon = new DecisionNode("ConfusedRollsOffSoon", new Decision[]{
            Decisions.confusionRollOffSoon
    });

    public final Node canDoPlunder = new DecisionNode("CanDoPlunder", new Decision[]{
            Decisions.canPlunder
    });

    public final Node canDemise = new DecisionNode("CanDoDemise", new Decision[]{
            Decisions.currentPawnLow,
            Decisions.canDemise
    });

    public final Node canCapitulate = new DecisionNode("CanDoCapitulate", new Decision[]{
            Decisions.currentPawnLow,
            Decisions.canCapitulate
    });




    /* Action Nodes */

    public final Node doNothing = new ActionNode(Actions.doNothing, "DoNothing");
    public final Node doCure = new ActionNode(Actions.selectBestCure, "DoCure");
    public final Node doBuff = new ActionNode(Actions.selectBestBuff, "DoBuff");
    public final Node doCurse = new ActionNode(Actions.selectCurse, "DoCurse");
    public final Node doAttack = new ActionNode(Actions.selectBestAttack, "DoAttack");
    public final Node doRandom = new ActionNode(Actions.selectRandomAction, "DoRandom");
    public final Node doSetAction = new ActionNode(Actions.doSetAction, "DoSetAction");
}