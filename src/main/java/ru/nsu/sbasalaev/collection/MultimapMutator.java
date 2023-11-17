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

/**
 * Common mutating methods for all mutable multimaps.
 *
 * @author Sergey Basalaev
 */
public interface MultimapMutator<K, V, C extends Collection<V>> {

    /**
     * Puts key-value pair in this multimap.
     *
     * @return true iff the multimap was modified as a result of this operation.
     */
    public boolean put(K key, V value);

    /**
     * Puts key-value pair in this multimap.
     *
     * @return true iff the multimap was modified as a result of this operation.
     */
    public default boolean put(Entry<K, V> entry) {
        return put(entry.key(), entry.value());
    }

    /**
     * Removes the key and all associated values from this multimap.
     *
     * @return the collection of elements that were previously associated with given key.
     */
    public abstract C removeKey(K key);

    /** Removes all entries from this multimap. */
    public abstract void clear();
}
