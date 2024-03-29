/*
 * The MIT License
 *
 * Copyright 2015, 2024 Sergey Basalaev.
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
package me.sbasalaev.collection;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;
import me.sbasalaev.Require;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Shared implementation of immutable empty collections.
 *
 * @author Sergey Basalaev
 */
interface EmptyCollection extends Traversable<@NonNull Void> {

    @Override
    default Traversable<@NonNull Void> takeWhile(Predicate<? super @NonNull Void> condition) {
        Require.nonNull(condition, "condition");
        return this;
    }

    @Override
    default Traversable<@NonNull Void> take(int limit) {
        Require.nonNegative(limit, "limit");
        return this;
    }

    @Override
    default Traversable<@NonNull Void> filter(Predicate<? super @NonNull Void> condition) {
        Require.nonNull(condition, "condition");
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    default <U extends Object> Traversable<U> narrow(Class<U> clazz) {
        Require.nonNull(clazz, "clazz");
        return (Traversable<U>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    default <R extends Object>
            Traversable<R> map(Function<? super @NonNull Void, ? extends R> mapping) {
        Require.nonNull(mapping, "mapping");
        return (Traversable<R>) this;
    }

    @Override
    default Traversable<?> chain(Traversable<?> other) {
        return Require.nonNull(other, "other");
    }

    @Override
    @SuppressWarnings("unchecked")
    default <R extends Object>
            Traversable<R> chainMap(Function<? super @NonNull Void, ? extends Traversable<R>> mapping) {
        Require.nonNull(mapping, "mapping");
        return (Traversable<R>) this;
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
    @Deprecated
    default <K extends Object>
            Map<K, ? extends List<@NonNull Void>> groupedBy(Function<? super @NonNull Void, ? extends K> classifier) {
        Require.nonNull(classifier, "classifier");
        return Map.empty();
    }

    @Override
    default <K extends Object>
            ListMultimap<K, @NonNull Void> groupedIntoLists(Function<? super @NonNull Void, ? extends K> classifier) {
        Require.nonNull(classifier, "classifier");
        return ListMultimap.empty();
    }

    @Override
    default <K extends Object>
            SetMultimap<K, @NonNull Void> groupedIntoSets(Function<? super @NonNull Void, ? extends K> classifier) {
        Require.nonNull(classifier, "classifier");
        return SetMultimap.empty();
    }

    @Override
    default List<@NonNull Void> sortedBy(Comparator<? super @NonNull Void> comparing) {
        Require.nonNull(comparing, "comparing");
        return List.empty();
    }
}
