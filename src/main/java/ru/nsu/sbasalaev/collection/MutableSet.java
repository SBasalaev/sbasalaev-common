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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Set that can be mutated.
 *
 * @author Sergey Basalaev
 */
public abstract class MutableSet<T> extends Set<T> implements MutableCollection<T> {

    /* CONSTRUCTORS */

    /** Constructor for subclasses. */
    public MutableSet() { }

    /** Returns new mutable set that is initially empty. */
    public static <T> MutableSet<T> empty() {
        return new DefaultImpl<>();
    }

    /** Returns new mutable set that initially contains given elements. */
    @SafeVarargs
    public static <T> MutableSet<T> of(T... elements) {
        return of(List.of(elements));
    }

    /** Returns new mutable set that initially contains given elements. */
    public static <T> MutableSet<T> of(Collection<? extends T> elements) {
        MutableSet<T> set = empty();
        set.addAll(elements);
        return set;
    }

    /* IMPLEMENTATION */

    private static final class DefaultImpl<T> extends MutableSet<T> {

        private final HashSet<T> impl;

        private DefaultImpl() {
            impl = new HashSet<>();
        }

        @Override
        public boolean contains(Object element) {
            return impl.contains(element);
        }

        @Override
        public int size() {
            return impl.size();
        }

        @Override
        public Iterator<T> iterator() {
            return impl.iterator();
        }

        @Override
        public boolean add(T element) {
            return impl.add(Objects.requireNonNull(element));
        }

        @Override
        public void clear() {
            impl.clear();
        }

        @Override
        public boolean remove(T item) {
            return impl.remove(item);
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
