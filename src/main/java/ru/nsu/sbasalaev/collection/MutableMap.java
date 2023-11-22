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
package ru.nsu.sbasalaev.collection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import static java.util.function.Predicate.not;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import static ru.nsu.sbasalaev.API.none;
import static ru.nsu.sbasalaev.API.some;
import ru.nsu.sbasalaev.Opt;

/**
 * Map that can be mutated.
 *
 * @author Sergey Basalaev
 */
public abstract class MutableMap<K, V>
    extends Map<K, V>
    implements MultimapMutator<K, V, Opt<V>> {

    /* CONSTRUCTORS */

    /** Constructor for subclasses. */
    public MutableMap() { }

    /** Returns new mutable map that is initially empty. */
    public static <K,V> MutableMap<K,V> empty() {
        return new DefaultImpl<>();
    }

    /** Returns new mutable map that initially contains given values. */
    public static <K,V> MutableMap<K,V> copyOf(Map<K,V> map) {
        MutableMap<K,V> result = empty();
        for (var entry : map.entries()) {
            result.set(entry.key(), entry.value());
        }
        return result;
    }

    /* INTERFACE */

    /**
     * Associates the {@code value} with the {@code key} in this map.
     *
     * @return
     *   value that was previously associated with the key
     *   or empty optional if there was none.
     */
    public abstract Opt<V> set(K key, V value);

    /**
     * Associates the {@code value} with the {@code key} in this map.
     *
     * @return
     *   {@code false} if the value was already associated with this
     *   key, {@code true} otherwise.
     */
    @Override
    public boolean add(K key, V value) {
        var prev = set(key, value);
        return !prev.exists(value::equals);
    }

    /**
     * Returns value associated with given key, adds it if no value was associated.
     * If a mapping already exists in this map, just returns the value.
     * Otherwise, given supplier is used to retrieve a value, it is put in this
     * map and then returned.
     */
    public V createIfMissing(K key, Supplier<? extends V> supplier) {
        for (var existing : get(key)) {
            return existing;
        }
        var value = supplier.get();
        set(key, value);
        return value;
    }

    /**
     * Creates or updates association with given key in this map.
     * If given key is not in this map, given supplier is used to retrieve a value,
     * it is put in this map and returned. If the map contains given key, the associated
     * value is transformed using supplied transformer, the new association is put in
     * this map and then new value is returned.
     */
    public V createOrUpdate(K key, Supplier<? extends V> supplier, UnaryOperator<V> transformer) {
        var value = get(key).mapped(transformer).orElseGet(supplier);
        set(key, value);
        return value;
    }

    /**
     * Updates association with given key if it is already present in this map.
     *
     * @return the new value associated with the key, or none() if the key was
     *         not present in this map.
     */
    public Opt<V> updateIfPresent(K key, UnaryOperator<V> transformer) {
        for (var value : get(key).mapped(transformer)) {
            set(key, value);
            return some(value);
        }
        return none();
    }

    /**
     * Removes key and associated value from this map.
     *
     * @return the value previously associated with this key, if any.
     */
    @Override
    public abstract Opt<V> removeKey(K key);

    /**
     * Removes all elements matching given condition from the collection.
     * Returns true if the collection was modified.
     */
    public boolean removeAllKeysMatching(Predicate<? super K> condition) {
        var iter = entries().iterator();
        var result = false;
        while (iter.hasNext()) {
            if (condition.test(iter.next().key())) {
                iter.remove();
                result = true;
            }
        }
        return result;
    }

    /**
     * Retains only entries with key matching given condition.
     * Returns true if the map was modified.
     */
    public boolean retainAllKeysMatching(Predicate<? super K> condition) {
        return removeAllKeysMatching(not(condition));
    }

    /* IMPLEMENTATION */

    private static final class DefaultImpl<K, V> extends MutableMap<K, V> {

        private final HashMap<K, V> impl;

        private DefaultImpl() {
            impl = new HashMap<>();
        }

        @Override
        public Opt<V> get(K key) {
            return Opt.ofNullable(impl.get(Objects.requireNonNull(key)));
        }

        @Override
        public Opt<V> set(K key, V value) {
            return Opt.ofNullable(impl.put(Objects.requireNonNull(key), Objects.requireNonNull(value)));
        }

        @Override
        public Opt<V> removeKey(K key) {
            return Opt.ofNullable(impl.remove(key));
        }

        @Override
        public boolean removeEntry(K key, V value) {
            return impl.remove(key, value);
        }

        @Override
        public void clear() {
            impl.clear();
        }

        @Override
        public int size() {
            return impl.size();
        }

        @Override
        public Set<Entry<K, V>> entries() {
            return new Set<Entry<K, V>>() {
                @Override
                public boolean contains(Object element) {
                    if (element instanceof Entry<?,?> entry) {
                        var value = impl.get((K) entry.key());
                        if (entry.value().equals(value)) {
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public int size() {
                    return impl.size();
                }

                @Override
                public Iterator<Entry<K, V>> iterator() {
                    return Iterators.map(impl.entrySet().iterator(), e -> Entry.of(e.getKey(), e.getValue()));
                }

                @Override
                public Object[] toArray() {
                    return impl.entrySet().toArray();
                }

                @Override
                public Entry<K, V>[] toArray(IntFunction<Entry<K, V>[]> arraySupplier) {
                    return impl.entrySet().toArray(arraySupplier);
                }
            };
        }
    }
}
