/*
 * The MIT License
 *
 * Copyright 2023 Sergey Basalaev
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import ru.nsu.sbasalaev.Opt;
import ru.nsu.sbasalaev.annotation.Out;

/**
 * Common superclass for maps and multimaps.
 * Multimaps associate keys with collections of values.
 * Map is a type of a multimap that associates key with at most one value,
 * so a multimap of {@link Opt } collections.
 * Every multimap has a notion of {@linkplain #equals(java.lang.Object) equality}
 * though that notion depends on the type of the collection of values.
 * Multimap may be {@linkplain #clone() cloned} to obtain a copy
 * of a collection of the same type.
 *
 * @param <K> type of the keys of this multimap.
 * @param <V> type of the values of this multimap.
 * @param <C> type of the collections of values corresponding to the single key.
 * 
 * @author Sergey Basalaev
 * @since 3.2
 */
public abstract class Multimap<K extends @NonNull Object, @Out V extends @NonNull Object, @Out C extends @NonNull Collection<V>> {

    /** Constructor for subclasses. */
    public Multimap() { }

    /** Collection of values associated with given key in this multimap. */
    public abstract C get(Object key);

    /** Whether given key is present in this map. */
    public boolean containsKey(Object key) {
        return get(key).nonEmpty();
    }

    /** Whether given key corresponds to given value in this map. */
    public boolean containsEntry(Entry<K, ?> entry) {
        return containsEntry(entry.key(), entry.value());
    }

    /** Whether given key corresponds to given value in this map. */
    public boolean containsEntry(Object key, Object value) {
        return get(key).exists(value::equals);
    }

    /**
     * All key-value associations of this multimap.
     * If this multimap allows multiple value associations to the
     * single key (like e. g. {@link ListMultimap }) then the same
     * entry is returned multiple times.
     */
    public abstract Traversable<Entry<K,V>> entries();

    /** 
     * Associations between keys and collections of values in this multimap.
     */
    public abstract Set<Entry<K, C>> collectionEntries();

    /** Set of keys that have at least one associated value in this multimap. */
    public Set<K> keys() {
        return new Set<K>() {
            @Override
            public boolean contains(Object element) {
                return containsKey(element);
            }

            @Override
            public int size() {
                return keySize();
            }

            @Override
            public Iterator<K> iterator() {
                return Iterators.map(collectionEntries().iterator(), Entry::key);
            }
        };
    }

    /** Number of keys in this multimap that have at least one associated value. */
    public abstract int keySize();

    /** Number of key-value pairs in this multimap. */
    public int size() {
        return collectionEntries().fold(0, (acc, e) -> acc + e.value().size());
    }

    /** Whether this multimap has no entries. */
    public boolean isEmpty() {
        return size() == 0;
    }

    /** Whether this multimap has at least one entry. */
    public boolean nonEmpty() {
        return size() != 0;
    }

    /** Traverses all the values of this multimap. */
    public Traversable<V> values() {
        return entries().map(Entry::value);
    }

    /**
     * Returns a shallow copy of this multimap.
     * The returned multimap is immutable, has the same type
     * and contains the same collections of elements. The multimap
     * is allowed to return itself if it is immutable.
     */
    @Override
    public abstract Multimap<K, V, C> clone();

    /**
     * Whether given object is equal to this multimap.
     * Multimaps are equal if they map keys to the same collection types
     * of values and the sets of {@link #collectionEntries() } are the same.
     * Multimaps of different collection types are not equal to each other.
     * In particular empty multimaps of different types are unequal
     * since they return unequal empty collections.
     */
    @Override
    public abstract boolean equals(@Nullable Object obj);

    /**
     * Hash code of the multimap.
     * The hash code of the multimap is defined as a sum of hash codes
     * of all its entries.
     */
    @Override
    public int hashCode() {
        return entries().fold(0, (acc, entry) -> acc + entry.hashCode());
    }

    /**
     * String representation of this map.
     * The returned string has the same format as the string for the
     * entries set.
     */
    @Override
    public String toString() {
        return entries().toString();
    }
}
