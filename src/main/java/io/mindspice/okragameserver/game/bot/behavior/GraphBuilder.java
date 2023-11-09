package io.mindspice.okragameserver.game.bot.behavior;

import io.mindspice.okragameserver.game.bot.behavior.core.Node;
import io.mindspice.okragameserver.game.bot.behavior.core.RootNode;

import java.util.Stack;

public class GraphBuilder {
    private Node rootNode;
    private Stack<Node> nodeStack = new Stack<>();

    // Init with root node that is returned when built
    public GraphBuilder() {
        rootNode = new RootNode();
        nodeStack.push(rootNode);
    }

    // Add a child node and step to that depth
    public GraphBuilder addChild(Node node) {
        Node current = nodeStack.peek();
        current.addAdjacent(node);
        nodeStack.push(node);
        return this;
    }

    // Add another child to the parent node, sibling to other children of the parent node
    public GraphBuilder addSibling(Node node) {
        stepBack(); // Step back to the parent node
        return addChild(node); // Add the node as a child to the parent
    }

    // Step back to the parent of the current depth
    public GraphBuilder stepBack() {
        if (nodeStack.size() > 1) {
            nodeStack.pop();
        }
        return this;
    }

    // Add a terminal leaf to the current node and step back to maintain depth
    // of the current node
    public GraphBuilder addLeaf(Node node) {
        addChild(node);
        stepBack(); // Step back to the parent of the leaf
        return this;
    }

    // Return the root node
    public RootNode build() {
        return (RootNode) rootNode;
    }
}

