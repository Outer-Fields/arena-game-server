package io.mindspice.okragameserver.game.bot.behavior.core;

import io.mindspice.okragameserver.game.bot.state.BotPlayerState;
import io.mindspice.okragameserver.game.bot.state.TreeFocusState;
import io.mindspice.okragameserver.game.enums.PawnIndex;

import java.util.ArrayList;
import java.util.List;


public abstract class Node {
    protected final String name;
    protected final Type type;
    protected final List<Node> adjacentNodes = new ArrayList<>(10);

    public Node(Type type, String name) {
        this.type = Type.ROOT;
        this.name = name;
    }

    public abstract boolean travel(BotPlayerState botPlayerState, TreeFocusState focusState, PawnIndex selfIndex);

    public Type getType() {
        return type;
    }

    public List<Node> getAdjacentNodes() {
        return adjacentNodes;
    }

    public String getName() {
        return name;
    }

    public void addAdjacent(Node node) {
        if (type == Type.ACTION) { throw new IllegalStateException("Can't add adjacent nodes to action nodes"); }
        if (adjacentNodes.contains(node)) { return; } // Keep from adding the same node twice
        adjacentNodes.add(node);
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean printGraph(int depth) {
        printNode(depth);
        if (this.type == Type.ACTION) { return false; }
        for (Node adjacentNode : getAdjacentNodes()) {
            if (!adjacentNode.printGraph(depth + 1)) { return false; }
        }
        return true;
    }

    protected void printNode(int depth) {
        for (int i = 0; i < depth; i++) { System.out.print("\t- "); }
        System.out.println(this); // Assuming a meaningful toString() implementation for the node
    }

    public enum Type {
        ROOT,
        DECISION,
        ACTION
    }
}
