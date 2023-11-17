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

/**
 * Mutable mapping of keys to lists of values.
 *
 * @author Sergey Basalaev
 * @since 3.2
 */
public abstract class MutableListMultimap<K, V>
    extends ListMultimap<K, V>
    implements MultimapMutator<K, V, List<V>> {

    /** Return new mutable multimap that is initially empty. */
    public static <K, V> MutableListMultimap<K, V> empty() {
        return new DefaultImpl<>();
    }

    private static final class DefaultImpl<K,V> extends MutableListMultimap<K, V> {

        private final MutableMap<K, MutableList<V>> impl = MutableMap.empty();

        private DefaultImpl() { }

        @Override
        public List<V> get(K key) {
            return impl.get(key)
                .mapped(Function.<List<V>>identity())
                .orElseGet(List::empty);
        }

        @Override
        public boolean put(K key, V value) {
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
            return impl.removeKey(key)
                .mapped(Function.<List<V>>identity())
                .orElseGet(List::empty);
        }

        @Override
        public void clear() {
            impl.clear();
        }
    }
}
