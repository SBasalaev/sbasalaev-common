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
package me.sbasalaev.collection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * List that can be mutated.
 *
 * @author Sergey Basalaev
 */
public abstract class MutableList<T extends @NonNull Object>
        extends List<T> implements MutableCollection<T> {

    /* CONSTRUCTORS */

    MutableList() { }

    /** New mutable list that is initially empty. */
    public static <T extends @NonNull Object> MutableList<T> empty() {
        return new DefaultImpl<>();
    }

    /** New mutable list that initially contains given elements. */
    @SafeVarargs
    public static <T extends @NonNull Object> MutableList<T> of(T... elements) {
        return of (List.of(elements));
    }

    /** New mutable list that initially contains given elements. */
    public static <T extends @NonNull Object> MutableList<T> of(Collection<? extends T> elements) {
        var list = new DefaultImpl<T>();
        list.addAll(elements);
        return list;
    }

    /* INTERFACE */

    /**
     * Assigns new element to the given index.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index >= size()}
     */
    public abstract T set(int elementIndex, T element);

    /**
     * Inserts element at the specified index of the list.
     *
     * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index > size()}
     */
    public abstract void insert(int insertIndex, T element);

    /**
     * Inserts elements at the specified position of the list.
     * Elements are inserted in the order returned by collection iterator.
     */
    public void insertAll(int insertIndex, Collection<? extends T> elements) {
        int i = insertIndex;
        for (var item : elements) {
            insert(i, item);
            i++;
        }
    }

    /**
     * Adds new element to the end of this list.
     * @return {@code true} because list always changes after this operation.
     */
    @Override
    public boolean add(T element) {
        insert(size(), element);
        return true;
    }

    /**
     * Adds all elements from given collection to the end of this list.
     * @return {@code true} if the supplied collection is non-empty.
     */
    @Override
    public boolean addAll(Collection<? extends T> elements) {
        insertAll(size(), elements);
        return elements.nonEmpty();
    }

    /**
     * Removes element at given index.
     */
    public abstract T removeAt(int elementIndex);

    /** Removes all elements in given range of this list. */
    public abstract void removeRange(int from, int to);

    /** Sorts this list using given comparator. */
    public abstract void sortBy(Comparator<? super T> comparing);

    /**
     * Transforms elements of this list using given transformation.
     * @since 3.1
     */
    public void transform(UnaryOperator<T> transformation) {
        for (int i = 0; i < size(); i++) {
            set(i, transformation.apply(get(i)));
        }
    }

    /**
     * Transforms elements of this list using given transformation.
     * @since 3.1
     */
    public void indexedTransform(BiFunction<Integer,T,T> transformation) {
        for (int i = 0; i < size(); i++) {
            set(i, transformation.apply(i, get(i)));
        }
    }

    /* DEFAULT IMPLEMENTATION */

    private static final class DefaultImpl<T extends @NonNull Object> extends MutableList<T> {

        private final ArrayList<T> impl;

        private DefaultImpl() {
            this.impl = new ArrayList<>();
        }

        @Override
        public T get(int index) {
            return impl.get(index);
        }

        @Override
        public T set(int index, T element) {
            return impl.set(index, element);
        }

        @Override
        public void insert(int index, T element) {
            impl.add(index, Objects.requireNonNull(element));
        }

        @Override
        public boolean add(T element) {
            return impl.add(Objects.requireNonNull(element));
        }

        @Override
        public int size() {
            return impl.size();
        }

        @Override
        public T removeAt(int index) {
            return impl.remove(index);
        }

        @Override
        public void removeRange(int from, int to) {
            impl.subList(from, to).clear();
        }

        @Override
        public void clear() {
            impl.clear();
        }

        @Override
        public void sortBy(Comparator<? super T> comparing) {
            impl.sort(comparing);
        }

        @Override
        public Iterator<T> iterator() {
            return impl.iterator();
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<T> toList() {
            return List.fromTrustedArray((T[]) impl.toArray());
        }

        @Override
        @SuppressWarnings("unchecked")
        public Set<T> toSet() {
            return Set.fromTrustedArray((T[]) impl.toArray());
        }

        @Override
        public Object[] toArray() {
            return impl.toArray();
        }

        @Override
        public T[] toArray(IntFunction<T[]> arraySupplier) {
            return impl.toArray(arraySupplier);
        }
    }
}
