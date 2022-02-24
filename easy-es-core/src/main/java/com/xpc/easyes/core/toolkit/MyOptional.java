package com.xpc.easyes.core.toolkit;

import java.util.Objects;
import java.util.function.Function;

/**
 * 对JDK提供的Optional的自定义,个人认为其高频api ifPresent没有返回用起来不方便
 * 可避免写过多if-else 提升代码优雅
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
