package io.kiw.result;

import java.util.*;

public class CollectiveValidationErrors {
    final Map<String, List<String>> underlying = new LinkedHashMap<>();
    final ScopeStack scopeStack = new ScopeStack();

    public void addError(String description, String errorMessage) {
        final String scope = scopeStack.resolveScope(description);
        underlying.compute(scope, (key, value) -> {
            if (value == null) {
                value = new ArrayList<>();
            }
            value.add(errorMessage);
            return value;
        });
    }

    public boolean hasErrors() {
        return underlying.size() > 0;
    }

    @Override
    public String toString() {
        return "CollectiveValidationErrors{" +
            "underlying=" + underlying +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollectiveValidationErrors that = (CollectiveValidationErrors) o;
        return Objects.equals(underlying, that.underlying);
    }

    @Override
    public int hashCode() {
        return Objects.hash(underlying);
    }

    public void withinScope(String name, Runnable r) {
        scopeStack.addToStack(name);
        try {
            r.run();
        } finally {
            scopeStack.dropStack();
        }
    }

    public Map<String, List<String>> buildErrorMap() {
        return underlying;
    }
}
