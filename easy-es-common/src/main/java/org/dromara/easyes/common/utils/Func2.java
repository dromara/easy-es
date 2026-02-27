package org.dromara.easyes.common.utils;

@FunctionalInterface
public interface Func2<P1, P2, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param p1 the first function argument
     * @param p2 the second function argument
     * @return the function result
     */
    R apply(P1 p1, P2 p2);
}