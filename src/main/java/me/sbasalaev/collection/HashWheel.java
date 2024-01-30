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

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntFunction;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Support implementation class for immutable maps and sets.
 * This is an immutable hashtable backed by a circular array.
 * For sets the entry is the key itself while for maps and
 * multimaps it is the corresponding {@link Entry}.
 *
 * @param <K> type of the key.
 * @param <E> type of the entry.
 *
 * @author Sergey Basalaev
 */
final class HashWheel<K extends Object, E extends Object>
        implements Iterable<E> {

    /** Elements in the order given by set or map constructor. */
    private final E[] elements;

    /** Expanded array sorted by hashcode suitable for searching. */
    private final @Nullable E[] table;

    /** Extracts hash code from the entry. */
    private final Function<E, K> extractKey;

    HashWheel(E[] elements, @Nullable E[] table, Function<E, K> extractKey) {
        this.elements = elements;
        this.table = table;
        this.extractKey = extractKey;
    }

    /** Returns entry corresponding to given key, or {@code null} if there is no such entry. */
    public @Nullable E get(Object key) {
        int len = table.length;
        int index = Math.floorMod(key.hashCode(), len);
        while (true) {
            E e = table[index];
            if (e == null) return null;
            if (extractKey.apply(e).equals(key)) {
                return e;
            }
            index += 1;
            if (index == len) index = 0;
        }
    }

    public int size() {
        return elements.length;
    }

    @Override
    public Iterator<E> iterator() {
        return Iterators.of(elements);
    }

    @Override
    public Spliterator<E> spliterator() {
        return java.util.Spliterators.spliterator(elements, Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.NONNULL);
    }

    public Set<E> toSet() {
        return Set.fromTrustedArray(elements);
    }

    public Object[] toArray() {
        return elements.clone();
    }

    public E[] toArray(IntFunction<E[]> arraySupplier) {
        E[] array = arraySupplier.apply(elements.length);
        System.arraycopy(elements, 0, array, 0, elements.length);
        return array;
    }

    public void fillArray(@Nullable Object[] array, int fromIndex) {
        Objects.checkFromIndexSize(fromIndex, 0, array.length);
        System.arraycopy(elements, 0, array, fromIndex, elements.length);
    }

    /**
     * Creates hash wheel for given entries.
     * If {@code entries} array contains only elements with unique
     * keys, then array is not cloned and is used for iteration.
     * If there are entries with duplicate keys, the duplicates are removed
     * and the array is shrunk down. During creation NPE is thrown if
     * {@code entries} is null or contains nulls.
     *
     * @param <K> type of the key.
     * @param <E> type of the entry.
     * @param entries map or set entries.
     * @param extractKey function to extract key part of the entry.
     *                   May be an identity function for sets.
     */
    @SuppressWarnings({"unchecked", "nullness"})
    // Entries array should have no nulls at the beginning, then we abuse it
    // and store nulls in place of duplicates. The function is only ever called
    // with trusted array.
    static <K extends Object, E extends Object>
            HashWheel<K, E> make(E[] entries, Function<E, K> extractKey) {
        int entriesLen = entries.length;
        int size = 0;
        // populating searched with elements
        // if elements contains duplicates they are replaced by nulls
        @Nullable E[] table = (@Nullable E[])
                Array.newInstance(entries.getClass().getComponentType(), entriesLen * 2);
        for (int i = 0; i < entriesLen; i++) {
            E e = entries[i];
            K key = extractKey.apply(e);
            int index = Math.floorMod(key.hashCode(), table.length);
            boolean foundCollision = false;
            while (table[index] != null) {
                if (key.equals(extractKey.apply(table[index]))) {
                    foundCollision = true;
                    break;
                }
                index++;
                if (index == table.length) index = 0;
            }
            if (foundCollision) {
                entries[i] = null;
            } else {
                table[index] = e;
                size++;
            }
        }
        // if there are no duplicates then elements are returned as origin
        // otherwise we copy elements to the new origin
        E[] origin;
        if (size == entriesLen) {
            origin = entries;
        } else {
            origin = (E[]) Array.newInstance(entries.getClass().getComponentType(), size);
            int insert = 0;
            for (E entry : entries) {
                if (entry != null) {
                    origin[insert] = entry;
                    insert++;
                }
            }
        }
        return new HashWheel<>(origin, table, extractKey);
    }
}
