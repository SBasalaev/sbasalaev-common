/*
 * The MIT License
 *
 * Copyright 2023-2024 Sergey Basalaev
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

import java.util.Iterator;
import me.sbasalaev.annotation.Out;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Sergey Basalaev
 */
public abstract class SetMultimap<K extends Object, @Out V extends Object>
    extends Multimap<K, V, Set<V>>
    implements Cloneable {

    /* CONSTRUCTORS */

    /** Constructor for subclasses. */
    public SetMultimap() { }

    private static final SetMultimap<?, ?> EMPTY = new EmptyMultimap();

    /** Empty list multimap. */
    public static <K extends Object, V extends Object> SetMultimap<K, V> empty() {
        return (SetMultimap<K, V>) EMPTY;
    }

    /** Builder of immutable list multimaps. */
    public static <K extends Object, V extends Object> Builder<K,V> build() {
        return new Builder<>();
    }

    /** Builder of immutable list multimaps. */
    public static final class Builder<K extends Object, V extends Object> {

        private final MutableSetMultimap<K, V> collector = MutableSetMultimap.empty();

        private Builder() { }

        /** Adds given entry to this builder. */
        public Builder<K,V> add(K key, V value) {
            collector.add(key, value);
            return this;
        }

        /** Adds given entry to this builder. */
        public Builder<K,V> add(Entry<K,V> entry) {
            collector.add(entry.key(), entry.value());
            return this;
        }

        /**
         * Creates new immutable multimap with entries added to this builder.
         * May be called multiple times.
         */
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

    private static <K extends Object, V extends Object>
            SetMultimap<K,V> fromTrustedArray(Entry<K, Set<V>>[] entries) {
        if (entries.length == 0) return empty();
        return new WheelMultimap<>(HashWheel.make(entries, Entry::key));
    }

    /* INTERFACE */

    /** Set of values associated with given key in this multimap. */
    @Override
    public abstract Set<V> get(Object key);

    /* OVERRIDEN MEMBERS */

    @Override
    public boolean containsEntry(Object key, Object value) {
        return get(key).contains(value);
    }

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
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SetMultimap<?,?> map)) return false;
        return collectionEntries().equals(map.collectionEntries());
    }

    /* IMMUTABLE IMPLEMENTATIONS */

    private static abstract class ImmutableMultimap<K extends Object, @Out V extends Object>
            extends SetMultimap<K, V> { }

    private static final class EmptyMultimap extends ImmutableMultimap<Object, @NonNull Void> {

        @Override
        public Set<@NonNull Void> get(Object key) {
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
        public Set<Entry<Object, Set<@NonNull Void>>> collectionEntries() {
            return Set.empty();
        }

        @Override
        public Set<Entry<Object, @NonNull Void>> entries() {
            return Set.empty();
        }
    }

    /** Multimap backed by a hash wheel. */
    private static final class WheelMultimap<K extends Object, V extends Object>
            extends ImmutableMultimap<K, V> {

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
        public Set<V> get(Object key) {
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
