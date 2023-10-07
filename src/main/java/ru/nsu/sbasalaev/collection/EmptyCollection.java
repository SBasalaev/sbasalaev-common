/*
 * The MIT License
 *
 * Copyright 2015 Sergey Basalaev.
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
package ru.nsu.sbasalaev.collection;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import ru.nsu.sbasalaev.Require;

/**
 * Shared implementation of empty collections.
 *
 * @author Sergey Basalaev
 */
interface EmptyCollection<T> extends Traversable<T> {

    @Override
    default Traversable<T> takeWhile(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "condition");
        return this;
    }

    @Override
    default Traversable<T> take(int limit) {
        Require.nonNegative(limit, "limit");
        return this;
    }

    @Override
    default Traversable<T> filter(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "condition");
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    default <U> Traversable<U> narrow(Class<U> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        return (Traversable<U>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    default <R> Traversable<R> map(Function<? super T, ? extends R> mapping) {
        Objects.requireNonNull(mapping, "mapping");
        return (Traversable<R>) this;
    }

    @Override
    default Traversable<?> chain(Traversable<?> other) {
        return Objects.requireNonNull(other, "other");
    }

    @Override
    @SuppressWarnings("unchecked")
    default <R> Traversable<R> chainMap(Function<? super T, ? extends Traversable<R>> mapping) {
        Objects.requireNonNull(mapping, "mapping");
        return (Traversable<R>) this;
    }

    @Override
    default Iterator<T> iterator() {
        return Iterators.empty();
    }

    @Override
    default String join(String separator) {
        return "";
    }

    @Override
    default String join(String start, String separator, String end) {
        return start + end;
    }

    @Override
    default <K> Map<K, ? extends List<T>> groupedBy(Function<? super T, ? extends K> classifier) {
        Objects.requireNonNull(classifier, "classifier");
        return Map.empty();
    }

    @Override
    default List<T> sortedBy(Comparator<? super T> comparing) {
        Objects.requireNonNull(comparing, "comparing");
        return List.empty();
    }
}
