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
import ru.nsu.sbasalaev.annotation.Out;

/**
 *
 * @author Sergey Basalaev
 */
public abstract class SetMultimap<K, @Out V>
    extends Multimap<K, V, Set<V>>
    implements Cloneable {

    /* CONSTRUCTORS */

    private static final SetMultimap<?, ?> EMPTY = new EmptyMultimap();

    /** Empty list multimap. */
    public static <K,V> SetMultimap<K, V> empty() {
        return (SetMultimap<K, V>) EMPTY;
    }

    /** Builder of immutable list multimaps. */
    public static <K,V> Builder<K,V> build() {
        return new Builder<>();
    }

    public static final class Builder<K, V> {

        private final MutableSetMultimap<K, V> collector = MutableSetMultimap.empty();

        private Builder() { }

        public Builder<K,V> add(K key, V value) {
            collector.add(key, value);
            return this;
        }

        public Builder<K,V> add(Entry<K,V> entry) {
            collector.add(entry.key(), entry.value());
            return this;
        }

        public SetMultimap<K,V> toSetMultimap() {
            if (collector.keySize() == 0) return empty();
            Entry<K, Set<V>>[] entries = new Entry[collector.keySize()];
            int index = 0;
            for (var entry : collector.collectionEntries()) {
                entries[index] = Entry.of(entry.key(), entry.value().clone());
                index++;
            }
            return fromTrustedArray(entries);
        }
    }

    private static <K,V> SetMultimap<K,V> fromTrustedArray(Entry<K, Set<V>>[] entries) {
        if (entries.length == 0) return empty();
        return new WheelMultimap<>(HashWheel.make(entries, Entry::key));
    }

    /* INTERFACE */

    /** Set of values associated with given key in this multimap. */
    @Override
    public abstract Set<V> get(K key);

    /* OVERRIDEN MEMBERS */

    @Override
    public Set<Entry<K, V>> entries() {
        return new Set<Entry<K, V>>() {
            @Override
            public boolean contains(Object element) {
                if (!(element instanceof Entry<?,?> entry)) return false;
                return SetMultimap.this.containsEntry((Entry<K,?>) entry);
            }

            @Override
            public int size() {
                return SetMultimap.this.size();
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                return collectionEntries()
                    .chainMap(e -> e.value().map(v -> Entry.of(e.key(), v)))
                    .iterator();
            }
        };
    }

    @Override
    public SetMultimap<K, V> clone() {
        if (isEmpty()) return empty();
        if (this instanceof ImmutableMultimap) return this;
        var array = new Entry<?,?>[size()];
        collectionEntries().fillArray(array, 0);
        return fromTrustedArray((Entry<K,Set<V>>[]) array);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SetMultimap<?,?> map)) return false;
        return collectionEntries().equals(map.collectionEntries());
    }

    /* IMMUTABLE IMPLEMENTATIONS */

    private static abstract class ImmutableMultimap<K, @Out V> extends SetMultimap<K, V> { }

    private static final class EmptyMultimap extends ImmutableMultimap<Object, Object> {

        @Override
        public Set<Object> get(Object key) {
            return Set.empty();
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
        public Set<Entry<Object, Set<Object>>> collectionEntries() {
            return Set.empty();
        }

        @Override
        public Set<Entry<Object, Object>> entries() {
            return Set.empty();
        }
    }

    /** Multimap backed by a hash wheel. */
    private static final class WheelMultimap<K, V> extends ImmutableMultimap<K, V> {

        private final HashWheel<K, Entry<K, Set<V>>> impl;
        private final int size;

        private WheelMultimap(HashWheel<K, Entry<K, Set<V>>> impl) {
            this.impl = impl;
            int count = 0;
            for (var i = impl.iterator(); i.hasNext(); ) {
                count += i.next().value().size();
            }
            this.size = count;
        }

        @Override
        public Set<V> get(K key) {
            var result = impl.get(key);
            return result != null ? result.value() : Set.empty();
        }

        @Override
        public int keySize() {
            return impl.size();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public Set<Entry<K, Set<V>>> collectionEntries() {
            return impl.toSet();
        }
    }
}
