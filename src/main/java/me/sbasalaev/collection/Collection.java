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

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import me.sbasalaev.annotation.Out;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Collection of elements.
 * This is a base class for different types of collections
 * such as {@linkplain List lists} or {@linkplain Set sets}.
 * Collections have known {@linkplain #size() size} at any time.
 * Every collection has a notion
 * of {@linkplain #equals(java.lang.Object) equality} though that notion
 * depends on the type of the collection.
 * Collection may be {@linkplain #clone() cloned} to obtain a
 * shallow immutable copy of the collection of the same type.
 *
 * @author Sergey Basalaev
 */
public abstract class Collection<@Out T extends Object>
        implements Traversable<T>, Cloneable {

    /** Constructor for subclasses. */
    public Collection() { }

    /**
     * Number of elements in this collection.
     * Unlike {@link Traversable#count()} this method is guaranteed to be fast.
     */
    public abstract int size();

    /**
     * Number of elements in this collection.
     * Returns the same value as {@link #size() }.
     */
    @Override
    public int count() {
        return size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean nonEmpty() {
        return size() != 0;
    }

    /**
     * Returns collection of the same type with given mapping applied to all elements.
     * The collection is immutable and is unaffected by changes to this collection.
     */
    public abstract <R extends Object> Collection<R> mapped(Function<? super T, ? extends R> mapping);

    /**
     * Returns collection of the same type containing only elements matching given condition.
     * The collection is immutable and is unaffected by changes to this collection.
     */
    public abstract Collection<T> filtered(Predicate<? super T> condition);

    /** Allocates and returns array with the elements of this collection. */
    @SuppressWarnings("unchecked")
    public Object[] toArray() {
        return ((Collection<Object>) this).toArray(Object[]::new);
    }

    /** Allocates and returns array with the elements of this collection. */
    @SuppressWarnings("variance")
    public T[] toArray(IntFunction<T[]> arraySupplier) {
        int len = size();
        T[] array = arraySupplier.apply(len);
        int index = 0;
        for (T item : this) {
            array[index] = item;
            index++;
        }
        return array;
    }

    /**
     * Fills array with elements of this collection starting from given index in array.
     *
     * @throws IndexOutOfBoundsException if the collection does not fit into given array.
     */
    public void fillArray(@Nullable Object[] array, int fromIndex) {
        int len = size();
        Objects.checkFromIndexSize(fromIndex, len, array.length);
        int index = fromIndex;
        for (T item : this) {
            array[index] = item;
            index++;
        }
    }

    /**
     * Creates a {@link Spliterator} over elements of this collection.
     * The spliterator reports {@link Spliterator#NONNULL} and {@link Spliterator#SIZED}.
     */
    @Override
    public abstract Spliterator<T> spliterator();

    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     * 
     * @since 4.1
     */
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns a shallow copy of this collection.
     * The returned collection is immutable, has the same type
     * and contains the same elements. If the collection is ordered,
     * the elements in a copy are in the same order. The collection
     * is allowed to return itself if it is immutable.
     */
    @Override
    public abstract Collection<T> clone();

    @Override
    @SuppressWarnings("unchecked")
    public List<T> toList() {
        return List.of((T[]) toArray());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<T> toSet() {
        return Set.of((T[]) toArray());
    }

    /**
     * Whether given object is equal to this collection.
     * Different collections have different notions of equality.
     */
    @Override
    public abstract boolean equals(@Nullable Object obj);

    /**
     * Hash code of the collection.
     * Different collections have different ways to calculate the hash code.
     */
    @Override
    public abstract int hashCode();

    /**
     * String representation of this collection.
     * Returns string of the form
     * <pre>{e1, e2, ..., eN}</pre>
     * where elements are in the order returned by iterator.
     */
    @Override
    public String toString() {
        return join("{", ", ", "}");
    }
}
