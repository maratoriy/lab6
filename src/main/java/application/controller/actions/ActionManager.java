package application.controller.actions;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;

public class ActionManager {
    private final LinkedList<Action> actionQueue;

    {
        actionQueue = new LinkedList<>();
    }

    public void addFirst(Action action) {
        actionQueue.addFirst(action);
    }

    public void add(Action action) {
        actionQueue.addLast(action);
    }

    public int size() {
        return actionQueue.size();
    }

    public void clear() {
        actionQueue.clear();
    }

    public void poll() {
        actionQueue.poll();
    }

    public void act() {
        if (actionQueue.size() > 0)
            actionQueue.peek().act();
    }
}
