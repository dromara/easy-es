package org.dromara.easyes.common.utils;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 对JDK提供的Optional的自定义增强
 * 可在一定程度上减少代码中出现的if-else 提升代码优雅
 * <p>
 * Copyright © 2021 xpc1024 All Rights Reserved
 **/
public final class MyOptional<T> {
    private static final MyOptional<?> EMPTY = new MyOptional<>();

    private final T value;

    private MyOptional() {
        this.value = null;
    }

    public static <T> MyOptional<T> empty() {
        @SuppressWarnings("unchecked")
        MyOptional<T> t = (MyOptional<T>) EMPTY;
        return t;
    }

    private MyOptional(T value) {
        this.value = Objects.requireNonNull(value);
    }

    public static <T> MyOptional<T> of(T value) {
        return new MyOptional<>(value);
    }

    public static <T> MyOptional<T> ofNullable(T value) {
        return value == null ? empty() : of(value);
    }

    public boolean isPresent() {
        return value != null;
    }

    public void ifTrue(Consumer<? super T> consumer) {
        if (value != null && value instanceof Boolean) {
            boolean condition = (boolean) (Object) value;
            if (condition) {
                consumer.accept(value);
            }
        }
    }

    public void ifFalse(Consumer<? super T> consumer) {
        if (value != null && value instanceof Boolean) {
            boolean condition = (boolean) (Object) value;
            if (!condition) {
                consumer.accept(value);
            }
        }
    }

    public void ifPresent(Consumer<? super T> present, Supplier<?> other) {
        if (value != null) {
            present.accept(value);
        } else {
            other.get();
        }
    }

    public <U> MyOptional<U> ifPresent(Function<? super T, ? extends U> present, T otherValue) {
        Objects.requireNonNull(present);
        if (isPresent())
            return MyOptional.ofNullable(present.apply(value));
        else {
            return MyOptional.ofNullable(present.apply(otherValue));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MyOptional)) {
            return false;
        }

        MyOptional<?> other = (MyOptional<?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value != null
                ? String.format("MyOptional[%s]", value)
                : "MyOptional.empty";
    }
}
