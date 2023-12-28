/*
 * The MIT License
 *
 * Copyright 2015, 2022, 2023 Sergey Basalaev.
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
import java.util.Objects;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import static me.sbasalaev.API.maybe;
import static me.sbasalaev.API.some;
import me.sbasalaev.Opt;
import me.sbasalaev.annotation.Out;

/**
 * Mapping of keys to values.
 *
 * @author Sergey Basalaev
 */
public abstract class Map<K extends @NonNull Object, @Out V extends @NonNull Object>
    extends Multimap<K, V, Opt<V>>
    implements Cloneable {

    /* CONSTRUCTORS */

    /** Constructor for subclasses. */
    public Map() { }

    private static final Map<?,?> EMPTY = new EmptyMap();

    /** Map with no entries. */
    @SuppressWarnings("unchecked")
    public static <K extends @NonNull Object, V extends @NonNull Object> Map<K, V> empty() {
        return (Map<K, V>) EMPTY;
    }

    /** Map containing given entry. */
    public static <K extends @NonNull Object, V extends @NonNull Object>
            Map<K, V> of(K key, V value) {
        return new SingletonMap<>(Objects.requireNonNull(key), Objects.requireNonNull(value));
    }

    /** Map containing given entries. */
    public static <K extends @NonNull Object, V extends @NonNull Object>
            Map<K, V> of(K k1, V v1, K k2, V v2) {
        return fromTrustedArray(
            Entry.of(k1, v1),
            Entry.of(k2, v2)
        );
    }

    /** Map containing given entries. */
    public static <K extends @NonNull Object, V extends @NonNull Object>
            Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return fromTrustedArray(
            Entry.of(k1, v1),
            Entry.of(k2, v2),
            Entry.of(k3, v3)
        );
    }

    /** Map containing given entries. */
    public static <K extends @NonNull Object, V extends @NonNull Object>
            Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return fromTrustedArray(
            Entry.of(k1, v1),
            Entry.of(k2, v2),
            Entry.of(k3, v3),
            Entry.of(k4, v4)
        );
    }

    /** Map containing given entries. */
    public static <K extends @NonNull Object, V extends @NonNull Object>
            Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return fromTrustedArray(
            Entry.of(k1, v1),
            Entry.of(k2, v2),
            Entry.of(k3, v3),
            Entry.of(k4, v4),
            Entry.of(k5, v5)
        );
    }

    /** Map containing given entries. */
    public static <K extends @NonNull Object, V extends @NonNull Object>
            Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return fromTrustedArray(
            Entry.of(k1, v1),
            Entry.of(k2, v2),
            Entry.of(k3, v3),
            Entry.of(k4, v4),
            Entry.of(k5, v5),
            Entry.of(k6, v6)
        );
    }

    /** Builder of immutable maps. */
    public static final class Builder<K extends @NonNull Object, V extends @NonNull Object> {

        private final MutableList<Entry<K,V>> entries = MutableList.empty();

        private Builder() { }

        /**
         * Adds entry to the map being built.
         * @return this builder.
         */
        public Builder<K,V> add(K key, V value) {
            entries.add(Entry.of(key, value));
            return this;
        }

        /**
         * Adds entry to the map being built.
         * @return this builder.
         */
        public Builder<K,V> add(Entry<K,V> entry) {
            entries.add(entry);
            return this;
        }

        /**
         * Creates new immutable map with entries added to this builder.
         * Can be used multiple times.
         */
        @SuppressWarnings("unchecked")
        public Map<K, V> toMap() {
            if (entries.size() == 0) return empty();
            return fromTrustedArray(entries.toArray(Entry[]::new));
        }
    }

    /** Builds map from given entries. */
    public static <K extends @NonNull Object, V extends @NonNull Object> Map.Builder<K, V> build() {
        return new Builder<>();
    }

    /**
     * Map containing given elements.
     * The array of elements is not cloned.
     */
    @SafeVarargs
    static <K extends @NonNull Object, V extends @NonNull Object>
            Map<K, V> fromTrustedArray(Entry<K,V>... entries) {
        return switch (entries.length) {
            case 0 -> empty();
            case 1 -> {
                var e = entries[0];
                yield new SingletonMap<>(Objects.requireNonNull(e.key()), Objects.requireNonNull(e.value()));
            }
            default-> new WheelMap<>(HashWheel.make(entries, Entry::key));
        };
    }

    /** Map view of given java map. */
    public static <K extends @NonNull Object, V extends @NonNull Object>
            Map<K,V> fromJava(java.util.Map<K,V> javaMap) {
        return new Map<K, V>() {
            @Override
            public Opt<V> get(Object key) {
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
    @Override
    public abstract Opt<V> get(Object key);

    /**
     * Number of keys in this map.
     * For the map this method returns the same value as {@code size()}.
     * @since 3.2
     */
    @Override
    public int keySize() {
        return size();
    }

    /** The set of key-value associations in this map. */
    @Override
    public abstract Set<Entry<K,V>> entries();

    @Override
    public Set<Entry<K, Opt<V>>> collectionEntries() {
        var entries = entries();
        return new Set<Entry<K, Opt<V>>>() {
            @Override
            public boolean contains(Object element) {
                if (element instanceof Entry<?,?> entry && entry.value() instanceof Opt<?> value && value.nonEmpty()) {
                    for (var valueOfThis : Map.this.get(entry.key())) {
                        return value.exists(valueOfThis::equals);
                    }
                }
                return false;
            }

            @Override
            public int size() {
                return entries.size();
            }

            @Override
            public Iterator<Entry<K, Opt<V>>> iterator() {
                return Iterators.map(entries.iterator(), e -> Entry.of(e.key(), some(e.value())));
            }
        };
    }

    /** The set of keys that have an associated value in this map. */
    @Override
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

    /** View of this map with given mapping applied to values. */
    public <W extends @NonNull Object> Map<K, W> mapValues(Function<? super V, ? extends W> mapping) {
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
            public Opt<W> get(Object key) {
                return Map.this.get(key).mapped(mapping);
            }

            @Override
            public int size() {
                return Map.this.size();
            }
        };
    }

    /** View of this map as java map. */
    @SuppressWarnings({"keyfor", "variance"}) // I couldn't manage to make keyfor work
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
            public @Nullable V get(Object key) {
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
    public final Map<K, V> clone() {
        if (isEmpty()) return empty();
        if (this instanceof ImmutableMap) return this;
        var array = new Entry<?,?>[size()];
        entries().fillArray(array, 0);
        return fromTrustedArray((Entry<K,V>[]) array);
    }

    /**
     * Whether some object is equal to this map.
     * Two maps are equal if they contain the same entries,
     * i.e. their {@link #entries() } methods return equal sets.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Map<?,?> map)) return false;
        return this.entries().equals(map.entries());
    }

    /* IMMUTABLE IMPLEMENTATIONS */
    private static abstract class ImmutableMap<K extends @NonNull Object, @Out V extends @NonNull Object> extends Map<K, V> { }

    /** Singleton map with no elements. */
    private static final class EmptyMap extends ImmutableMap<Object, @NonNull Void> {

        private EmptyMap() { }

        @Override
        public Opt<@NonNull Void> get(Object key) {
            return Opt.empty();
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public Set<Entry<Object, @NonNull Void>> entries() {
            return Set.empty();
        }

        @Override
        public <W extends @NonNull Object>
                Map<Object, W> mapValues(Function<? super @NonNull Void, ? extends W> mapping) {
            Objects.requireNonNull(mapping);
            return Map.empty();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    /** Map containing only one entry. */
    private static final class SingletonMap<K extends @NonNull Object, V extends @NonNull Object> extends ImmutableMap<K,V> {

        private final K key;
        private final V value;

        SingletonMap(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Opt<V> get(Object key) {
            return this.key.equals(key) ? Opt.of(value) : Opt.empty();
        }

        @Override
        public boolean containsKey(Object key) {
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

    /** Map backed by a hash wheel. */
    private static final class WheelMap<K extends @NonNull Object, V extends @NonNull Object> extends ImmutableMap<K, V> {

        private final HashWheel<K, Entry<K, V>> wheel;

        private WheelMap(HashWheel<K, Entry<K, V>> impl) {
            this.wheel = impl;
        }

        @Override
        public Opt<V> get(Object key) {
            return maybe(wheel.get(key)).mapped(Entry::value);
        }

        @Override
        public boolean containsKey(Object key) {
            return wheel.get(key) != null;
        }

        @Override
        public Set<Entry<K, V>> entries() {
            return wheel.toSet();
        }

        @Override
        public int size() {
            return wheel.size();
        }
    }
}
