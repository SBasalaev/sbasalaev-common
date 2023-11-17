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
import ru.nsu.sbasalaev.API;
import ru.nsu.sbasalaev.annotation.Out;

/**
 * Mapping of keys to lists of values.
 *
 * @author Sergey Basalaev
 * @since 3.2
 */
public abstract class ListMultimap<K, @Out V>
    extends Multimap<K, V, List<V>>
    implements Cloneable {

    /* CONSTRUCTORS */

    private static final ListMultimap<?, ?> EMPTY = new EmptyMultimap();

    /** Empty list multimap. */
    public static <K,V> ListMultimap<K, V> empty() {
        return (ListMultimap<K, V>) EMPTY;
    }

    /* INTERFACE */

    /**
     * Values associated with given key in this multimap.
     * Values are returned in the same order they are added to this multimap.
     */
    @Override
    public abstract List<V> get(K key);

    /* OVERRIDEN MEMBERS */

    @Override
    public Traversable<Entry<K, V>> entries() {
        return collectionEntries().chainMap(e -> e.value().map(v -> Entry.of(e.key(), v)));
    }

    @Override
    public Multimap<K, V, List<V>> clone() {
        if (isEmpty()) return empty();
        if (this instanceof Immutable) return this;
        return API.TODO();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ListMultimap map)) return false;
        return collectionEntries().equals(map.collectionEntries());
    }

    /* IMMUTABLE IMPLEMENTATIONS */

    private static abstract class Immutable<K, @Out V> extends ListMultimap<K, V> { }

    private static final class EmptyMultimap extends Immutable<Object, Object> {

        @Override
        public List<Object> get(Object key) {
            return List.empty();
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public int keySize() {
            return 0;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Set<Entry<Object, List<Object>>> collectionEntries() {
            return Set.empty();
        }

        @Override
        public Traversable<Entry<Object, Object>> entries() {
            return Set.empty();
        }
    }
}
