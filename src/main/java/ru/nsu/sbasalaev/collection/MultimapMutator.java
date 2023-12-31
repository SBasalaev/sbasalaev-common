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

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Common mutating methods for all mutable multimaps.
 *
 * @author Sergey Basalaev
 */
public interface MultimapMutator<K extends @NonNull Object, V extends @NonNull Object, C extends @NonNull Collection<V>> {

    /**
     * Puts key-value pair in this multimap.
     *
     * @return true iff the multimap was modified as a result of this operation.
     */
    public boolean add(K key, V value);

    /**
     * Puts key-value pair in this multimap.
     *
     * @return true iff the multimap was modified as a result of this operation.
     */
    public default boolean add(Entry<K, V> entry) {
        return add(entry.key(), entry.value());
    }

    /**
     * Removes the key and all associated values from this multimap.
     *
     * @return the collection of elements that was previously associated with given key.
     */
    public abstract C removeKey(K key);

    /**
     * Removes a single entry from this multimap.
     * 
     * @return {@code true} if the collection is changed as the result of this
     *   operation, {@code false} otherwise.
     */
    public abstract boolean removeEntry(K key, V value);

    /** Removes all entries from this multimap. */
    public abstract void clear();
}
