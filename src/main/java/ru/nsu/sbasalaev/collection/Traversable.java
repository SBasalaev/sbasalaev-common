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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.nsu.sbasalaev.API;
import static ru.nsu.sbasalaev.API.none;
import static ru.nsu.sbasalaev.API.some;
import ru.nsu.sbasalaev.Opt;
import ru.nsu.sbasalaev.Require;
import ru.nsu.sbasalaev.annotation.Out;

/**
 * Collection of elements that may be traversed in sequence.
 * This interface adds transformation and reduction methods
 * to the {@link Iterable}. The transformations return traversable
 * views of the collection indicating that their result should only
 * be used for reduction or iteration. Traversables can be chained
 * avoiding iteration until the reduction is performed.
 *
 * @author Sergey Basalaev
 */
public interface Traversable<@Out T extends @NonNull Object> extends Iterable<T> {

    /* TRANSFORMERS */

    /**
     * Iterates through elements of this traversable until given condition fails.
     * The returned traversable is a view of this object.
     *
     * @see Iterators#takeWhile(java.util.Iterator, java.util.function.Predicate) 
     */
    default Traversable<T> takeWhile(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "condition");
        return new AbstractView<T>() {
            @Override
            public Iterator<T> iterator() {
                return Iterators.takeWhile(Traversable.this.iterator(), condition);
            }
        };
    }

    /**
     * Traverses no more than {@code limit} elements of this traversable.
     * The returned traversable is a view of this object.
     *
     * @see Iterators#limit(java.util.Iterator, int) 
     */
    default Traversable<T> take(@NonNegative int limit) {
        Require.nonNegative(limit, "limit");
        return new AbstractView<T>() {
            @Override
            public Iterator<T> iterator() {
                return Iterators.limit(Traversable.this.iterator(), limit);
            }
        };
    }

    /**
     * Returns traversable that only traverses elements satisfying given {@code condition}.
     * The returned traversable is a view of this object.
     *
     * @see Iterators#filter(java.util.Iterator, java.util.function.Predicate) 
     */
    default Traversable<T> filter(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "condition");
        return new AbstractView<T>() {
            @Override
            public Iterator<T> iterator() {
                return Iterators.filter(Traversable.this.iterator(), condition);
            }
        };
    }

    /**
     * Returns traversable that only traverses subclasses of {@code clazz}.
     * The returned traversable is a view of this object.
     */
    @SuppressWarnings("unchecked")
    default <U extends @NonNull Object> Traversable<U> narrow(Class<U> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        return (Traversable<U>) filter(clazz::isInstance);
    }

    /**
     * Returns traversable with given {@code mapping} applied to all elements.
     * The returned traversable is a view of this traversable that lazily
     * applies {@code mapping} to elements returned by iterator.
     *
     * @see Iterators#map(java.util.Iterator, java.util.function.Function) 
     */
    default <R extends @NonNull Object> Traversable<R> map(Function<? super T, ? extends R> mapping) {
        Objects.requireNonNull(mapping, "mapping");
        return new AbstractView<R>() {
            @Override
            public Iterator<R> iterator() {
                return Iterators.<T,R>map(Traversable.this.iterator(), mapping);
            }

            @Override
            public int count() {
                return Traversable.this.count();
            }
        };
    }

    /**
     * Traversable that iterates over elements of both traversables.
     * The iterator of the result first returns elements of this traverable,
     * and once it is exhausted elements of the other traversable.
     *
     * @see API#chain(ru.nsu.sbasalaev.collection.Traversable...)
     * @see Iterators#chain(java.util.Iterator, java.util.Iterator)
     */
    default Traversable<?> chain(Traversable<?> other) {
        Objects.requireNonNull(other, "other");
        if (other instanceof EmptyCollection) {
            return this;
        }
        return new AbstractView<>() {
            @Override
            public Iterator<Object> iterator() {
                return Iterators.chain(Traversable.this.iterator(), other.iterator());
            }

            @Override
            public int count() {
                try {
                    return Math.addExact(Traversable.this.count(), other.count());
                } catch (ArithmeticException ae) {
                    return Integer.MAX_VALUE;
                }
            }
        };
    }

    /** Applies {@code mapping} to the elements and chains the resulting traversables together. */
    @SuppressWarnings("unchecked")
    default <R extends @NonNull Object> Traversable<R> chainMap(Function<? super T, ? extends Traversable<R>> mapping) {
        Objects.requireNonNull(mapping, "mapping");
        return (Traversable<R>) map(mapping).<Traversable<?>>fold(List.empty(), Traversable::chain);
    }

    /* REDUCTION */

    /**
     * Returns the first element produced by the iterator of this traversable.
     * May produce different results each time it is called if the traversable
     * has no defined order of its elements.
     *
     * @throws NoSuchElementException if the traversable is empty.
     */
    default T first() throws NoSuchElementException {
        return iterator().next();
    }

    /** Tests whether any element in this traversable matches given {@code condition}. */
    default boolean exists(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "condition");
        for (var item : this) {
            if (condition.test(item)) return true;
        }
        return false;
    }

    /** Tests whether all elements in this traversable match given {@code condition}. */
    default boolean forall(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "condition");
        for (var item : this) {
            if (!condition.test(item)) return false;
        }
        return true;
    }

    /**
     * Starting from the {@code first}, applies {@code combine} to elements of the collection returning result.
     * The order of elements is determined by the iterator.
     */
    default <R extends @NonNull Object> R fold(R first, BiFunction<? super R, ? super T, ? extends R> combine) {
        Objects.requireNonNull(combine, "combine");
        var result = first;
        for (var item : this) {
            result = combine.apply(result, item);
        }
        return result;
    }

    /** Returns element that satisfies {@code condition} or empty optional if there is no such element. */
    default Opt<T> find(Predicate<? super T> condition) {
        Objects.requireNonNull(condition, "condition");
        for (var item : this) {
            if (condition.test(item)) return some(item);
        }
        return none();
    }

    /** Joins elements of the traversable into a string. */
    default String join(String start, String separator, String end) {
        var builder = new StringBuilder(start);
        var prefix = "";
        for (T item : this) {
            builder.append(prefix).append(item);
            prefix = separator;
        }
        return builder.append(end).toString();
    }

    /** Joins elements of the traversable into a string. */
    default String join(String separator) {
        return join("", separator, "");
    }

    /**
     * Counts number of elements in this collection.
     * As opposed to {@link Collection#size()} this method
     * may need to perform computations to count the number of elements.
     * If the traversable has more than {@code Integer.MAX_VALUE}
     * elements, returns {@code Integer.MAX_VALUE}.
     */
    public default @NonNegative int count() {
        int count = 0;
        for (var __ : this) {
            count++;
            if (count == Integer.MAX_VALUE) break;
        }
        return count;
    }

    /** Whether this collection has no elements. */
    public default boolean isEmpty() {
        return !iterator().hasNext();
    }

    /** Whether this collection contains elements. */
    public default boolean nonEmpty() {
        return iterator().hasNext();
    }

    /**
     * Collects elements of this traversable into lists grouped by given classifier.
     * The resulting collection is immutable and is not affected by changes to this traversable.
     *
     * @param <K> type of the classifier key.
     * @param classifier function that assigns keys to elements of this traversable.
     */
    public default <K extends @NonNull Object>
            Map<K, ? extends List<T>> groupedBy(Function<? super T, ? extends K> classifier) {
        Objects.requireNonNull(classifier, "classifier");
        var map = MutableMap.<K, MutableList<T>>empty();
        for (var item : this) {
            map.createOrUpdate(classifier.apply(item),
                () -> MutableList.of(item),
                list -> { list.add(item); return list; }
            );
        }
        return map;
    }

    /**
     * Returns list of elements of this traversable sorted by given comparator.
     *
     * @param comparing comparator for elements of this traversable.
     */
    public default List<T> sortedBy(Comparator<? super T> comparing) {
        var list = MutableList.of(toList());
        list.sortBy(comparing);
        return list;
    }

    /** Collects elements of this traversable into an immutable list. */
    public default List<T> toList() {
        var list = MutableList.<T>empty();
        forEach(list::add);
        return list.clone();
    }

    /** Collects elements of this traversable into an immutable set. */
    public default Set<T> toSet() {
        var set = MutableSet.<T>empty();
        forEach(set::add);
        return set.clone();
    }
}
