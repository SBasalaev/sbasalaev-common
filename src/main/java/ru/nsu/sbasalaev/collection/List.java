/*
 * The MIT License
 *
 * Copyright 2015, 2023 Sergey Basalaev.
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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;
import org.checkerframework.checker.index.qual.GTENegativeOne;
import org.checkerframework.checker.index.qual.IndexFor;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.SameLen;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import ru.nsu.sbasalaev.API;
import ru.nsu.sbasalaev.Require;
import ru.nsu.sbasalaev.annotation.Out;

/**
 * Sequence of elements numbered by integer indices.
 * An implementation of the list must provide get() and size().
 * The default iterator implementation uses these methods.
 *
 * @author Sergey Basalaev
 */
public abstract class List<@Out T extends @NonNull Object> extends Collection<T> {

    /* CONSTRUCTORS */

    /** Constructor for subclasses. */
    public List() { }

    private static final List<?> EMPTY = new EmptyList();

    /** Empty list. */
    @SuppressWarnings("unchecked")
    public static <T extends @NonNull Object> List<T> empty() {
        return (List<T>) EMPTY;
    }

    /** List of given elements. */
    @SafeVarargs
    public static <T extends @NonNull Object> List<T> of(T... elements) {
        if (elements.length == 0) return empty();
        return new ArrayList<>(Require.noNulls(elements).clone());
    }

    /**
     * List of given elements.
     * Array is not cloned and is not checked for nulls.
     */
    @SafeVarargs
    static <T extends @NonNull Object> List<T> fromTrustedArray(T... elements) {
        return new ArrayList<>(elements);
    }

    /** Concatenates several lists together. */
    @SafeVarargs
    @SuppressWarnings("unchecked")
    public static <T extends @NonNull Object> List<T> concatenated(List<? extends T>... lists) {
        var listOfLists = List.of(lists);
        int firstNonEmpty = listOfLists.findIndex(List::nonEmpty);
        if (firstNonEmpty >= 0) {
            int secondNonEmpty = listOfLists.findIndex(List::nonEmpty, firstNonEmpty + 1);
            if (secondNonEmpty >= 0) {
                @SuppressWarnings("array.length.negative") // no it's not
                var array = (T[]) new Object[API.sum(fromTrustedArray(lists).map(List::size))];
                int offset = 0;
                for (var list : lists) {
                    list.fillArray(array, offset);
                    offset += list.size();
                }
                return fromTrustedArray(array);
            }
            return (List<T>) listOfLists.get(firstNonEmpty).clone();
        }
        return empty();
    }

    /** List view of given Java list. */
    public static <T extends @NonNull Object> List<T> fromJava(java.util.List<T> javaList) {
        return new List<>() {
            @Override
            public T get(@NonNegative int index) {
                return javaList.get(index);
            }

            @Override
            public @NonNegative int size() {
                return javaList.size();
            }

            @Override
            public Iterator<T> iterator() {
                return javaList.iterator();
            }

            @Override
            public java.util.List<T> toJava() {
                return javaList;
            }
        };
    }

    /** List that repeats the same element given number of times. */
    public static <T extends @NonNull Object> List<T> repeat(T element, @NonNegative int times) {
        Require.nonNegative(times, "times");
        if (times == 0) return empty();
        return new Repeat<>(element, times);
    }

    /* INTERFACE */

    /**
     * Element at the specified index of the list.
     *
     * @throws IndexOutOfBoundsException
     *   if {@code index} is negative or ≥ {@link #size() }.
     */
    public abstract T get(@NonNegative int index);

    /**
     * The last valid index in this list.
     * Returns {@code size()-1}.
     */
    public @GTENegativeOne int lastIndex() {
        return size() - 1;
    }

    /**
     * Returns the first element of this list.
     * @throws NoSuchElementException if the list is empty.
     * @see #last()
     */
    @Override
    public T first() throws NoSuchElementException {
        int idx = 0;
        if (idx < size()) {
            return get(idx);
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns the last element of this list.
     * @throws NoSuchElementException if the list is empty.
     * @see #first()
     */
    public T last() throws NoSuchElementException {
        int lastIndex = lastIndex();
        if (lastIndex >= 0) {
            return get(lastIndex);
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * Returns the index of the first element satisfying given condition.
     * Returns {@code -1} if there is no such element.
     */
    public @GTENegativeOne int findIndex(Predicate<? super T> condition) {
        return findIndex(condition, 0);
    }

    /**
     * Returns the index of the first element satisfying given condition.
     * Returns {@code -1} if there is no such element.
     */
    public @GTENegativeOne int findIndex(Predicate<? super T> condition, @NonNegative int fromIndex) {
        for (int i = fromIndex; i < size(); i++) {
            if (condition.test(get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last element satisfying given condition.
     * Returns {@code -1} if there is no such element.
     */
    public @GTENegativeOne int findLastIndex(Predicate<? super T> condition) {
        for (int i = size() - 1; i >= 0; i--) {
            if (condition.test(get(i))) {
                return i;
            }
        }
        return -1;
    }

    /** Finds item using binary search assuming elements are in increasing order. */
    public @GTENegativeOne int binarySearch(ToIntFunction<T> comparator) {
        int left = 0;
        int right = size();
        while (left < right) {
            int idx = (left + right) >>> 1;
            var result = comparator.applyAsInt(get(idx));
            if (result == 0) {
                return idx;
            } else if (result > 0) {
                right = idx;
            } else {
                left = idx + 1;
            }
        }
        return -1;
    }

    /**
     * A view of this list where each element is coupled with the corresponding index.
     * The returned list is a view that is affected immediately by the changes to this list.
     */
    public List<IndexedElement<T>> indexed() {
        return new List<IndexedElement<T>>() {
            @Override
            public IndexedElement<T> get(@NonNegative int index) {
                return IndexedElement.of(index, List.this.get(index));
            }

            @Override
            public @NonNegative int size() {
                return List.this.size();
            }

            @Override
            public Iterator<IndexedElement<T>> iterator() {
                return Iterators.zipBy(Iterators.counting(0), List.this.iterator(), IndexedElement::of);
            }
        };
    }

    /**
     * A view of this list that contains only elements starting from given offset.
     * If this list contains no more than {@code offset} elements the returned view is empty.
     * The returned list is a view that is affected immediately by the changes to this list.
     *
     * @param offset non-negative offset from the start of the list.
     * @throws IllegalArgumentException if the offset is negative.
     */
    public List<T> from(@NonNegative int offset) {
        Require.nonNegative(offset, "offset");
        if (offset == 0) return this;
        return new List<T>() {
            @Override
            public T get(@NonNegative int index) {
                Objects.checkIndex(index, size());
                return List.this.get(index + offset);
            }

            @Override
            public @NonNegative int size() {
                return Math.max(0, List.this.size() - offset);
            }
        };
    }

    /**
     * A view of this list that contains no more than {@code limit} elements.
     * The returned list is a view that is affected immediately by the changes to this list.
     *
     * @param limit maximum number of elements to take.
     * @throws IllegalArgumentException if given limit is negative.
     */
    @Override
    public List<T> take(@NonNegative int limit) {
        Require.nonNegative(limit, "limit");
        return new List<T>() {
            @Override
            public T get(@NonNegative int index) {
                Objects.checkIndex(index, size());
                return List.this.get(index);
            }

            @Override
            public @NonNegative int size() {
                return Math.min(limit, List.this.size());
            }
        };
    }

    /**
     * A view of this list that contains the same elements in the reversed order.
     * The returned list is a view that is affected immediately by the changes to this list.
     */
    public List<T> reversed() {
        return new List<T>() {
            @Override
            public T get(@NonNegative int index) {
                Objects.checkIndex(index, size());
                return List.this.get(size() - index - 1);
            }

            @Override
            public @NonNegative int size() {
                return List.this.size();
            }

            @Override
            public List<T> reversed() {
                return List.this;
            }
        };
    }

    /**
     * A combined view of {@code this} and {@code other} lists using given zipper.
     * The returned list is a view that is affected immediately by the changes to
     * {@code this} and {@code other} lists. Size of the view is the minimum of
     * sizes of two lists and the element at each index is a combination by {@code zipper}
     * of the elements of {@code this} and {@code other} at the same index.
     */
    public <U extends @NonNull Object, R extends @NonNull Object>
            List<R> zip(List<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return new List<R>() {
            @Override
            public R get(@NonNegative int index) {
                Objects.checkIndex(index, size());
                return zipper.apply(List.this.get(index), other.get(index));
            }

            @Override
            public @NonNegative int size() {
                return Math.min(List.this.size(), other.size());
            }
        };
    }

    /** A view of this list as Java list. */
    public java.util.List<T> toJava() {
        return new java.util.AbstractList<>() {
            @Override
            public T get(@NonNegative int index) {
                return List.this.get(index);
            }

            @Override
            public @NonNegative int size() {
                return List.this.size();
            }
        };
    }

    /* OVERRIDEN METHODS */

    /**
     * Iterator of this list.
     * The returned iterator returns elements of this list in order
     * starting at index 0.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private @NonNegative int next = 0;

            @Override
            public boolean hasNext() {
                return next < List.this.size();
            }

            @Override
            public T next() {
                int n = next;
                if (n >= List.this.size()) {
                    throw new NoSuchElementException();
                }
                next = n + 1;
                return List.this.get(n);
            }
        };
    }

    /** Returns shallow immutable copy of this list. */
    @Override
    public final List<T> clone() {
        if (isEmpty()) return empty();
        if (this instanceof ImmutableList) return this;
        @SuppressWarnings("unchecked")
        T[] array = (T[]) new Object[size()];
        fillArray(array, 0);
        return fromTrustedArray(array);
    }

    /**
     * Returns view of this list with given mapping applied to all elements.
     * The returned list is a view that is affected immediately by the changes to this list.
     */
    @Override
    public <R extends @NonNull Object>
            List<R> map(Function<? super T, ? extends R> mapping) {
        return new List<R>() {
            @Override
            public R get(@NonNegative int index) {
                return mapping.apply(List.this.get(index));
            }

            @Override
            public @NonNegative int size() {
                return List.this.size();
            }

            @Override
            public Iterator<R> iterator() {
                return Iterators.map(List.this.iterator(), mapping);
            }
        };
    }

    /**
     * Returns new list with given mapping applied to all elements.
     * The returned list is immutable and is unaffected by the changes to this list.
     */
    @Override
    public <R extends @NonNull Object>
            List<R> mapped(Function<? super T, ? extends R> mapping) {
        @SuppressWarnings("unchecked")
        R[] array = (R[]) new Object[size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = mapping.apply(get(i));
        }
        return fromTrustedArray(array);
    }

    /**
     * Returns new list that contains only elements of this list satisfying given condition.
     * The returned list is immutable and is unaffected by the changes to this list.
     */
    @Override
    public List<T> filtered(Predicate<? super T> condition) {
        return filter(condition).toList();
    }

    /**
     * Compares two objects for equality.
     * Two lists are equal if they have the same size
     * and the corresponding pairs of elements are equal.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof List<?> other)) {
            return false;
        }
        int len = this.size();
        if (other.size() != len) {
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (!this.get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns hash code for the list.
     * The hash code for the list is defined to be equal to
     * <pre>fold(1, (hash, item) -> hash*31 + item.hashCode());</pre>
     * This means that the hash code for the list is compatible with
     * Java definitions for {@link java.util.List#hashCode() },
     * {@link java.util.Arrays#hashCode(java.lang.Object[]) }
     * and {@link java.util.Objects#hash(java.lang.Object...) }.
     */
    @Override
    public int hashCode() {
        return fold(1, (hash, item) -> hash*31 + item.hashCode());
    }

    /* IMPLEMENTATIONS */

    private static abstract class ImmutableList<T extends @NonNull Object> extends List<T> {

        private ImmutableList() { }

        @Override
        public List<T> from(@NonNegative int offset) {
            if (offset >= size()) return empty();
            if (offset == 0) return this;
            return super.from(offset);
        }

        @Override
        public List<T> take(@NonNegative int limit) {
            if (limit >= size()) return this;
            if (limit == 0) return empty();
            return super.take(limit);
        }

        @Override
        public List<T> reversed() {
            return switch (size()) {
                case 0, 1 -> this;
                default   -> super.reversed();
            };
        }

        @Override
        public List<T> toList() {
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<T> toSet() {
            return Set.fromTrustedArray((T[]) toArray());
        }
    }

    /** Empty list. */
    private static final class EmptyList
            extends ImmutableList<@NonNull Void>
            implements EmptyCollection {

        private EmptyList() { }

        @Override
        public @NonNull Void get(@NonNegative int index) {
            throw new IndexOutOfBoundsException(index);
        }

        @Override
        public @NonNegative int size() {
            return 0;
        }

        @Override
        public Set<@NonNull Void> toSet() {
            return Set.empty();
        }

        @Override
        public <R extends @NonNull Object>
            List<R> map(Function<? super @NonNull Void, ? extends R> mapping) {
            return List.empty();
        }

        @Override
        public <R extends @NonNull Object>
            List<R> mapped(Function<? super @NonNull Void, ? extends R> mapping) {
            return List.empty();
        }

        @Override
        public List<@NonNull Void> filtered(Predicate<? super @NonNull Void> condition) {
            return this;
        }

        @Override
        public List<IndexedElement<@NonNull Void>> indexed() {
            return List.empty();
        }

        @Override
        public <U extends @NonNull Object, R extends @NonNull Object>
                List<R> zip(List<U> other, BiFunction<? super @NonNull Void, ? super U, ? extends R> zipper) {
            return List.empty();
        }

        @Override
        public Object[] toArray() {
            return new Object[] { };
        }

        @Override
        public @NonNull Void[] toArray(IntFunction<@NonNull Void[]> arraySupplier) {
            return arraySupplier.apply(0);
        }

        @Override
        public void fillArray(@Nullable Object[] array, int fromIndex) {
            Objects.checkFromIndexSize(fromIndex, 0, array.length);
        }
    }

    /** Immutable list backed by an array. */
    private static final class ArrayList<T extends @NonNull Object> extends ImmutableList<T> {

        private final T @SameLen("this") [] elements;

        private ArrayList(T @SameLen("this") [] elements) {
            this.elements = elements;
        }

        @Override
        public T get(@IndexFor("this") int index) {
            return elements[index];
        }

        @Override
        public @NonNegative int size() {
            return elements.length;
        }

        @Override
        public Iterator<T> iterator() {
            return Iterators.of(elements);
        }

        @Override
        public Object[] toArray() {
            return elements.clone();
        }

        @Override
        public T[] toArray(IntFunction<T[]> newArray) {
            T[] array = newArray.apply(elements.length);
            System.arraycopy(elements, 0, array, 0, elements.length);
            return array;
        }

        @Override
        public void fillArray(@Nullable Object[] array, @NonNegative int fromIndex) {
            System.arraycopy(elements, 0, array, fromIndex, elements.length);
        }
    }

    private static final class Repeat<T extends @NonNull Object> extends ImmutableList<T> {

        private final T element;
        private final @NonNegative int size;

        private Repeat(T element, @NonNegative int size) {
            this.element = element;
            this.size = size;
        }

        @Override
        public T get(@NonNegative int index) {
            Objects.checkIndex(index, size);
            return element;
        }

        @Override
        public @NonNegative int size() {
            return size;
        }

        @Override
        public List<T> from(@NonNegative int offset) {
            Require.nonNegative(offset, "offset");
            if (offset >= size) return empty();
            if (offset == 0) return this;
            return new Repeat<>(element, size - offset);
        }

        @Override
        public List<T> take(@NonNegative int limit) {
            Require.nonNegative(limit, "limit");
            if (limit >= size) return this;
            return new Repeat<>(element, limit);
        }

        @Override
        public List<T> reversed() {
            return this;
        }
    }
}
