package io.mindspice.okragameserver;

import io.mindspice.okragameserver.game.bot.behavior.BehaviorGraph;
import org.junit.jupiter.api.Test;


public class BehaviorGraphTest {
    BehaviorGraph bg = BehaviorGraph.getInstance();

    @Test
    void printGraph() {
        bg.printStructure();

    }
}
