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

import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Mutable mapping of keys to sets of values.
 *
 * @author Sergey Basalaev
 * @since 3.2
 */
public abstract class MutableSetMultimap<K extends @NonNull Object, V extends @NonNull Object>
    extends SetMultimap<K, V>
    implements MultimapMutator<K, V, Set<V>> {

    /** Constructor for subclasses. */
    public MutableSetMultimap() { }

    /** Returns new mutable multimap that is initially empty. */
    public static <K extends @NonNull Object, V extends @NonNull Object> MutableSetMultimap<K, V> empty() {
        return new DefaultImpl<>();
    }

    private static final class DefaultImpl<K extends @NonNull Object, V extends @NonNull Object>
            extends MutableSetMultimap<K, V> {

        private final MutableMap<K, MutableSet<V>> impl = MutableMap.empty();
        private int size;

        private DefaultImpl() { }

        @Override
        public Set<V> get(Object key) {
            return impl.get(key)
                .mapped(Function.<Set<V>>identity())
                .orElseGet(Set::empty);
        }

        @Override
        public boolean add(K key, V value) {
            boolean result = impl.createIfMissing(key, MutableSet::empty).add(value);
            if (result) size++;
            return result;
        }

        @Override
        public int keySize() {
            return impl.keySize();
        }

        @Override
        public Set<Entry<K, Set<V>>> collectionEntries() {
            return (Set<Entry<K, Set<V>>>) (Set<?>) impl.entries();
        }

        @Override
        public Set<V> removeKey(K key) {
            for (var set : impl.removeKey(key)) {
                size -= set.size();
                return set;
            }
            return Set.empty();
        }

        @Override
        public boolean removeEntry(K key, V value) {
            for (var set : impl.get(key)) {
                boolean result = set.remove(value);
                if (result) size--;
                if (set.isEmpty()) impl.removeKey(key);
                return result;
            }
            return false;
        }

        @Override
        public void clear() {
            impl.clear();
            size = 0;
        }
    }
}
