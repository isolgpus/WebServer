package io.kiw.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Result<E, S> {

    public static <E, S> Result<E, S> success(S value) {
        return new Success<>(value);
    }

    public static <E, S> Result<E, S> error(E value) {
        return new Error<>(value);
    }

    public static <E, S> Result<Map<Integer, E>, List<S>> collapse(List<Result<E, S>> results) {

        Map<Integer, E> collectedErrors = new HashMap<>();
        List<S> successes = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            Result<E, S> result = results.get(i);

            if (result instanceof Result.Success<E, S>) {
                successes.add(((Success<E, S>) result).value);
            } else {
                collectedErrors.put(i, ((Error<E, S>) result).value);
            }
        }

        if (collectedErrors.isEmpty()) {
            return Result.success(successes);
        } else {
            return Result.error(collectedErrors);
        }
    }


    public abstract void consume(Consumer<E> errorConsumer, Consumer<S> successConsumer);

    public abstract <SR> Result<E, SR> flatMap(Function<S, Result<E, SR>> e);

    public abstract <ER> Result<ER, S> mapError(Function<E, ER> e);

    public abstract <R> R fold(Function<E, R> errorConsumer, Function<S, R> successConsumer);

    public abstract <SR> Result<E, SR> map(Function<S, SR> s);


    protected static class Success<E, S> extends Result<E, S> {

        private final S value;

        public Success(S value) {
            super();

            this.value = value;
        }


        @Override
        public void consume(Consumer<E> errorConsumer, Consumer<S> successConsumer) {
            successConsumer.accept(value);
        }

        @Override
        public <SR> Result<E, SR> flatMap(Function<S, Result<E, SR>> e) {
            return e.apply(this.value);
        }

        @Override
        public <ER> Result<ER, S> mapError(Function<E, ER> e) {
            return Result.success(value);
        }


        @Override
        public <R> R fold(Function<E, R> errorConsumer, Function<S, R> successConsumer) {
            return successConsumer.apply(value);
        }

        @Override
        public <SR> Result<E, SR> map(Function<S, SR> s) {
            return Result.success(s.apply(value));
        }

    }

    protected static class Error<E, S> extends Result<E, S> {

        private final E value;

        protected Error(E value) {
            super();

            this.value = value;
        }


        @Override
        public void consume(Consumer<E> errorConsumer, Consumer<S> successConsumer) {
            errorConsumer.accept(value);
        }

        @Override
        public <SR> Result<E, SR> flatMap(Function<S, Result<E, SR>> e) {
            return Result.error(this.value);
        }

        @Override
        public <ER> Result<ER, S> mapError(Function<E, ER> e) {
            return Result.error(e.apply(value));
        }

        @Override
        public <R> R fold(Function<E, R> errorConsumer, Function<S, R> successConsumer) {
            return errorConsumer.apply(value);
        }

        @Override
        public <SR> Result<E, SR> map(Function<S, SR> s) {
            return Result.error(value);
        }

    }
}
