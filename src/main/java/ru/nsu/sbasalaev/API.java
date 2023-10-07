/*
 * The MIT License
 *
 * Copyright 2015-2022 Sergey Basalaev.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ru.nsu.sbasalaev;

import java.util.function.Function;
import ru.nsu.sbasalaev.annotation.Nullable;
import ru.nsu.sbasalaev.collection.List;
import ru.nsu.sbasalaev.collection.Set;
import ru.nsu.sbasalaev.collection.Traversable;

/**
 * Useful utilities meant to be imported statically.
 *
 * @author Sergey Basalaev
 */
public final class API {

    private API() { }

    /* THE MOST USEFUL FUNCTION */

    /** Throws NotImplementedError when evaluated. */
    public static <T> T TODO() {
        throw new NotImplementedError();
    }

    /** Throws NotImplementedError when evaluated. */
    public static <T> T TODO(String message) {
        throw new NotImplementedError(message);
    }

    /* FUNCTIONS */

    /** Constant function. */
    public static <T, R> Function<T, R> constant(R result) {
        return t -> result;
    }

    /* COLLECTIONS */

    /** Empty optional. */
    public static <T> Opt<T> none() {
        return Opt.empty();
    }

    /** Non-empty optional. */
    public static <T> Opt<T> some(T value) {
        return Opt.of(value);
    }

    /** Wraps value into optional. */
    public static <T> Opt<T> maybe(@Nullable T value) {
        return Opt.ofNullable(value);
    }

    /** Empty list. */
    public static <T> List<T> list() {
        return List.empty();
    }

    /** List of given elements. */
    @SafeVarargs
    public static <T> List<T> list(T... elements) {
        return List.of(elements);
    }

    /** Empty set. */
    public static <T> Set<T> set() {
        return Set.empty();
    }

    /** Set of given elements. */
    @SafeVarargs
    public static <T> Set<T> set(T... elements) {
        return Set.of(elements);
    }

    /**
     * Chains several traversables together.
     * @see Traversable#chain(ru.nsu.sbasalaev.collection.Traversable) 
     */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T> Traversable<T> chain(Traversable<? extends T>... elements) {
        return (Traversable<T>) list(elements).<Traversable<?>>fold(list(), Traversable::chain);
    }

    /**
     * Concatenates several lists together.
     * The returned list is immutable and is not affected by changes to the original lists.
     */
    @SafeVarargs
    public static <T> List<T> concat(List<? extends T>... lists) {
        return List.concatenated(lists);
    }

    /**
     * Returns list with given element appended to the end of given list.
     * The returned list is immutable and is not affected by changes to the original list.
     * @since 3.1
     */
    public static <T> List<T> append(List<? extends T> list, T item) {
        return concat(list, list(item));
    }

    /* OTHERS */

    /** Returns sum of all numbers in a given traversable. */
    public static int sum(Traversable<Integer> numbers) {
        return numbers.fold(0, Integer::sum);
    }
}
