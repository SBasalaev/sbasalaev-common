/*
 * The MIT License
 *
 * Copyright 2015, 2024 Sergey Basalaev.
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

import java.util.Objects;
import me.sbasalaev.annotation.Out;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Map entry.
 * Associates key with value.
 *
 * @param <K> type of the key.
 * @param <V> type of the value.
 * @author Sergey Basalaev
 */
public abstract class Entry<@Out K extends Object, @Out V extends Object> {

    /**
     * Returns map entry with given key and value.
     * The entry is immutable and caches key hash.
     */
    public static <K extends Object, V extends Object> Entry<K,V> of(K key, V value) {
        int hash = key.hashCode();
        return new Entry<K, V>() {
            @Override
            public int keyHash() {
                return hash;
            }

            @Override
            public K key() {
                return key;
            }

            @Override
            public V value() {
                return value;
            }
        };
    }

    /** Constructor for subclasses. */
    public Entry() { }

    /** The key of this entry. */
    public abstract K key();

    /** The value associated with the key in this entry. */
    public abstract V value();

    /**
     * Returns the hash code of the key.
     * May be overriden if caching the hash is desired.
     */
    public int keyHash() {
        return key().hashCode();
    }

    /**
     * Whether given object is equal to this entry.
     * Two entries are equal if they contain the same keys and values.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Entry<?,?> entry)) return false;
        return key().equals(entry.key()) && value().equals(entry.value());
    }

    /** The hash code for this entry. */
    @Override
    public int hashCode() {
        return Objects.hash(key(), value());
    }

    /**
     * String representation of this entry.
     * Returns string of the form
     * <pre>key => value</pre>
     */
    @Override
    public String toString() {
        return key() + " => " + value();
    }
}
