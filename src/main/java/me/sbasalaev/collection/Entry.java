/*
 * The MIT License
 *
 * Copyright 2015 Sergey Basalaev.
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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import me.sbasalaev.annotation.Out;

/**
 * Map entry.
 *
 * @author Sergey Basalaev
 */
public abstract class Entry<@Out K extends @NonNull Object, @Out V extends @NonNull Object> {

    /**
     * Returns map entry with given key and value.
     * The entry is immutable and caches key hash.
     */
    public static <K extends @NonNull Object, V extends @NonNull Object> Entry<K,V> of(K key, V value) {
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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Entry<?,?> entry)) return false;
        return key().equals(entry.key()) && value().equals(entry.value());
    }

    @Override
    public int hashCode() {
        return Objects.hash(key(), value());
    }

    @Override
    public String toString() {
        return key() + " => " + value();
    }
}
