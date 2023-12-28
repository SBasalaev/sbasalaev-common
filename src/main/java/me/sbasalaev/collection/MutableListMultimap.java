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
package me.sbasalaev.collection;

import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Mutable mapping of keys to lists of values.
 *
 * @author Sergey Basalaev
 * @since 3.2
 */
public abstract class MutableListMultimap<K extends @NonNull Object, V extends @NonNull Object>
    extends ListMultimap<K, V>
    implements MultimapMutator<K, V, List<V>> {

    /** Constructor for subclasses. */
    public MutableListMultimap() { }

    /** Returns new mutable multimap that is initially empty. */
    public static <K extends @NonNull Object, V extends @NonNull Object> MutableListMultimap<K, V> empty() {
        return new DefaultImpl<>();
    }

    private static final class DefaultImpl<K extends @NonNull Object, V extends @NonNull Object>
            extends MutableListMultimap<K, V> {

        private final MutableMap<K, MutableList<V>> impl = MutableMap.empty();
        private int size;

        private DefaultImpl() { }

        @Override
        public List<V> get(Object key) {
            return impl.get(key)
                .mapped(Function.<List<V>>identity())
                .orElseGet(List::empty);
        }

        @Override
        public boolean add(K key, V value) {
            size++;
            return impl.createIfMissing(key, MutableList::empty).add(value);
        }

        @Override
        public int keySize() {
            return impl.keySize();
        }

        @Override
        public Set<Entry<K, List<V>>> collectionEntries() {
            return (Set<Entry<K, List<V>>>) (Set<?>) impl.entries();
        }

        @Override
        public List<V> removeKey(K key) {
            for (var list : impl.removeKey(key)) {
                size -= list.size();
                return list;
            }
            return List.empty();
        }

        @Override
        public boolean removeEntry(K key, V value) {
            for (var list : impl.get(key)) {
                int index = list.findIndex(value::equals);
                if (index < 0) return false;
                if (list.size() == 1) {
                    impl.removeKey(key);
                } else {
                    list.removeAt(index);
                }
                size--;
                return true;
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
