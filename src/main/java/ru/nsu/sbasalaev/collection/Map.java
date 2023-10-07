/*
 * The MIT License
 *
 * Copyright 2015, 2022 Sergey Basalaev.
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
import java.util.Objects;
import java.util.function.Function;
import ru.nsu.sbasalaev.Opt;
import ru.nsu.sbasalaev.annotation.Nullable;
import ru.nsu.sbasalaev.annotation.Out;

/**
 * Mapping of keys to values.
 *
 * @author Sergey Basalaev
 */
public abstract class Map<K, @Out V> implements Cloneable {

    /* CONSTRUCTORS */

    private static final Map<?,?> EMPTY = new EmptyMap();

    /** Empty map. */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> empty() {
        return (Map<K, V>) EMPTY;
    }

    /** Map containing given entry. */
    public static <K, V> Map<K, V> of(K key, V value) {
        return new Map1<>(Objects.requireNonNull(key), Objects.requireNonNull(value));
    }

    /** Map containing given entries. */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2) {
        return fromTrustedArray(
            Entry.of(k1, v1),
            Entry.of(k2, v2)
        );
    }

    /** Map containing given entries. */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return fromTrustedArray(
            Entry.of(k1, v1),
            Entry.of(k2, v2),
            Entry.of(k3, v3)
        );
    }

    /** Map containing given entries. */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return fromTrustedArray(
            Entry.of(k1, v1),
            Entry.of(k2, v2),
            Entry.of(k3, v3),
            Entry.of(k4, v4)
        );
    }

    /** Map containing given entries. */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return fromTrustedArray(
            Entry.of(k1, v1),
            Entry.of(k2, v2),
            Entry.of(k3, v3),
            Entry.of(k4, v4),
            Entry.of(k5, v5)
        );
    }

    /** Map containing given entries. */
    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return fromTrustedArray(
            Entry.of(k1, v1),
            Entry.of(k2, v2),
            Entry.of(k3, v3),
            Entry.of(k4, v4),
            Entry.of(k5, v5),
            Entry.of(k6, v6)
        );
    }

    /**
     * Builder for maps with many entries.
     */
    public static final class Builder<K, V> {

        private final MutableList<Entry<K,V>> entries = MutableList.empty();

        private Builder() { }

        /** Adds entry to the map being built. */
        public Builder<K,V> add(K key, V value) {
            entries.add(Entry.of(key, value));
            return this;
        }

        /**
         * Creates new immutable map with entries added to this builder.
         * Can be used multiple times.
         */
        @SuppressWarnings("unchecked")
        public Map<K, V> toMap() {
            return fromTrustedArray(entries.toArray(Entry[]::new));
        }
    }

    /** Builds map from given entries. */
    public static <K, V> Map.Builder<K, V> build() {
        return new Builder<>();
    }

    /**
     * Set containing given elements.
     * The array of elements is not cloned.
     */
    @SafeVarargs
    static <K, V> Map<K, V> fromTrustedArray(Entry<K,V>... entries) {
        return switch (entries.length) {
            case 0 -> empty();
            case 1 -> {
                var e = entries[0];
                yield new Map1<>(Objects.requireNonNull(e.key()), Objects.requireNonNull(e.value()));
            }
            default-> {
                var init = Support.make(entries, (e1, e2) -> e1.key().equals(e2.key()), Entry::keyHash);
                if (init.origin().length == 1) {
                    var e = init.origin()[0];
                    yield new Map1<>(e.key(), e.value());
                } else {
                    yield new ArrayMap<>(init);
                }
            }
        };
    }

    /** Map view of given java map. */
    public static <K, V> Map<K,V> fromJava(java.util.Map<K,V> javaMap) {
        return new Map<K, V>() {
            @Override
            public Opt<V> get(K key) {
                return Opt.ofNullable(javaMap.get(key));
            }

            @Override
            public int size() {
                return javaMap.size();
            }

            @Override
            public Set<Entry<K, V>> entries() {
                return new Set<>() {
                    @Override
                    public boolean contains(Object element) {
                        if (element instanceof Entry<?,?> e) {
                            return Objects.equals(javaMap.get(e.key()), e.value());
                        }
                        return false;
                    }

                    @Override
                    public int size() {
                        return javaMap.size();
                    }

                    @Override
                    public Iterator<Entry<K, V>> iterator() {
                        return Iterators.map(
                            javaMap.entrySet().iterator(),
                            e -> Entry.of(e.getKey(), e.getValue())
                        );
                    }
                };
            }
        };
    }

    /* INTERFACE */

    /** Value associated with given key or empty optional if there is none. */
    public abstract Opt<V> get(K key);

    /** Whether given key is present in this map. */
    public boolean containsKey(K key) {
        return get(key).nonEmpty();
    }

    /** The number of entries in this map. */
    public abstract int size();

    /** Whether this map has no entries. */
    public boolean isEmpty() {
        return size() == 0;
    }

    /** Whether this map has entries. */
    public boolean nonEmpty() {
        return size() != 0;
    }

    /** The set of entries in this map. */
    public abstract Set<Entry<K,V>> entries();

    /** The set of keys present in this map. */
    public Set<K> keys() {
        return new Set<>() {
            @Override
            @SuppressWarnings("unchecked")
            public boolean contains(Object element) {
                return Map.this.containsKey((K) element);
            }

            @Override
            public int size() {
                return Map.this.size();
            }

            @Override
            public Iterator<K> iterator() {
                return Iterators.map(Map.this.entries().iterator(), Entry::key);
            }
        };
    }

    /** Traverses all the values of the map. */
    public Traversable<V> values() {
        return entries().map(Entry::value);
    }

    /** View of this map with given mapping applied to values. */
    public <W> Map<K, W> mapValues(Function<? super V, ? extends W> mapping) {
        Objects.requireNonNull(mapping);
        return new Map<K, W>() {
            @Override
            public Set<Entry<K, W>> entries() {
                return new Set<Entry<K, W>>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public boolean contains(Object element) {
                        if (element instanceof Entry<?,?> e) {
                            for (var value : Map.this.get((K) e.key())) {
                                if (mapping.apply(value).equals(e.value())) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }

                    @Override
                    public Iterator<Entry<K, W>> iterator() {
                        return Iterators.map(
                            Map.this.entries().iterator(),
                            e -> Entry.of(e.key(), mapping.apply(e.value()))
                        );
                    }

                    @Override
                    public int size() {
                        return Map.this.size();
                    }
                };
            }

            @Override
            public Opt<W> get(K key) {
                return Map.this.get(key).mapped(mapping);
            }

            @Override
            public int size() {
                return Map.this.size();
            }
        };
    }

    /** View of this map as java map. */
    public java.util.Map<K, V> toJava() {
        return new java.util.AbstractMap<K, V>() {
            final class JavaEntry<K, V> implements java.util.Map.Entry<K, V> {

                private final K key;
                private final V value;

                private JavaEntry(K key, V value) {
                    this.key = key;
                    this.value = value;
                }

                @Override public K getKey()   { return key; }
                @Override public V getValue() { return value; }
                @Override public V setValue(V value) { throw new UnsupportedOperationException(); }
            }

            @Override
            public java.util.Set<java.util.Map.Entry<K, V>> entrySet() {
                return new java.util.AbstractSet<java.util.Map.Entry<K, V>>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public boolean contains(Object o) {
                        if (o instanceof java.util.Map.Entry<?,?> javaEntry) {
                            for (var value : Map.this.get((K) javaEntry.getKey())) {
                                return Objects.equals(value, javaEntry.getValue());
                            }
                        }
                        return super.contains(o);
                    }

                    @Override
                    public Iterator<java.util.Map.Entry<K, V>> iterator() {
                        return Iterators.map(
                            Map.this.entries().iterator(),
                            entry -> new JavaEntry<>(entry.key(), entry.value())
                        );
                    }

                    @Override
                    public int size() {
                        return Map.this.size();
                    }
                };
            }

            @Override
            @SuppressWarnings("unchecked")
            public V get(Object key) {
                return Map.this.get((K) key).orElseNull();
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean containsKey(Object key) {
                return Map.this.containsKey((K) key);
            }

            @Override
            public int size() {
                return Map.this.size();
            }
        };
    }

    /* OVERRIDEN MEMBERS */

    /** Returns shallow immutable copy of this map. */
    @Override
    @SuppressWarnings("unchecked")
    public Map<K, V> clone() {
        if (isEmpty()) return empty();
        var array = new Entry<?,?>[size()];
        entries().fillArray(array, 0);
        return fromTrustedArray((Entry<K,V>[]) array);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Map<?,?> map)) return false;
        return this.entries().equals(map.entries());
    }

    @Override
    public int hashCode() {
        return entries().hashCode();
    }

    @Override
    public String toString() {
        return entries().toString();
    }

    /* IMMUTABLE IMPLEMENTATIONS */
    private static abstract class Immutable<K, @Out V> extends Map<K, V> {

        @Override
        public Map<K, V> clone() {
            return this;
        }
    }

    /** Singleton map with no elements. */
    private static final class EmptyMap
        extends Immutable<Object, Object>
        implements EmptyCollection<Entry<Object,Object>> {

        private EmptyMap() { }

        @Override
        public Opt<Object> get(Object key) {
            return Opt.empty();
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public Set<Entry<Object, Object>> entries() {
            return Set.empty();
        }

        @Override
        public <W> Map<Object, W> mapValues(Function<? super Object, ? extends W> mapping) {
            Objects.requireNonNull(mapping);
            return Map.empty();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    /** Map containing only one entry. */
    private static final class Map1<K, V> extends Immutable<K,V> {

        private final K key;
        private final V value;

        Map1(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Opt<V> get(K key) {
            return this.key.equals(key) ? Opt.of(value) : Opt.empty();
        }

        @Override
        public boolean containsKey(K key) {
            return this.key.equals(key);
        }

        @Override
        public Set<Entry<K, V>> entries() {
            return Set.of(Entry.of(key, value));
        }

        @Override
        public int size() {
            return 1;
        }
    }

    /** Map backed by an array. */
    private static final class ArrayMap<K, V> extends Immutable<K, V> {

        /** Elements of this set in the order given by constructor. */
        private final Entry<K,V>[] origin;
        /** Expanded array sorted by hashcode suitable for searching. */
        private final @Nullable Entry<K,V>[] searched;

        ArrayMap(Support.Initializer<Entry<K,V>> initializer) {
            this.origin = initializer.origin();
            this.searched = initializer.searched();
        }

        @Override
        public Opt<V> get(@Nullable K key) {
            if (key == null) return Opt.empty();
            int len = searched.length;
            int hash = key.hashCode();
            int index = Math.floorMod(hash, len);
            while (true) {
                var e = searched[index];
                if (e == null) return Opt.empty();
                if (e.keyHash() == hash && e.key().equals(key)) return Opt.of(e.value());
                index += 1;
                if (index == len) index = 0;
            }
        }

        @Override
        public Set<Entry<K, V>> entries() {
            return Set.fromTrustedArray(origin);
        }

        @Override
        public int size() {
            return origin.length;
        }
    }
}
