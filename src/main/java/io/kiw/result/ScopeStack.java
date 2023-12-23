package io.kiw.result;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScopeStack {
    final Deque<String> stack = new ArrayDeque<>();

    public void addToStack(String name) {
        stack.add(name);
    }

    public void dropStack() {
        stack.removeLast();
    }

    public String resolveScope(String description) {
        return stack.size() > 0
            ? String.join(".", stack) + "." + description
            : description;
    }
}
