package com.luna.sanguine_networks.util;

import java.util.Objects;

//A port of com.ibm.icu.impl.Pair class, to make sure it's available on the server as well

public class Pair<F, S> {
    public final F first;
    public final S second;

    protected Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Pair<F, S> of(F first, S second) {
        if (first != null && second != null) {
            return new Pair<>(first, second);
        } else {
            throw new IllegalArgumentException("Pair.of requires non null values.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}