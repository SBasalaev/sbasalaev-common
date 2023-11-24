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
package ru.nsu.sbasalaev.collection;

import java.util.function.Predicate;
import static java.util.function.Predicate.not;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Common methods of {@code MutableList} and {@code MutableSet}.
 *
 * @author Sergey Basalaev
 */
public interface MutableCollection<T extends @NonNull Object> extends Traversable<T> {

    /**
     * Adds element to this collection.
     * @return {@code true} if the collection has changed, false otherwise
     */
    boolean add(T element);

    /**
     * Adds elements to this collection.
     * @return {@code true} if the collection has changed, false otherwise
     */
    default boolean addAll(Collection<? extends T> elements) {
        var result = false;
        for (var e : elements) {
            result |= add(e);
        }
        return result;
    }

    /** Removes all elements from the collection. */
    default void clear() {
        var iter = iterator();
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
    }

    /**
     * Removes all elements matching given condition from the collection.
     * Returns true if the collection was modified.
     */
    default boolean removeAllMatching(Predicate<? super T> condition) {
        var iter = iterator();
        var result = false;
        while (iter.hasNext()) {
            if (condition.test(iter.next())) {
                iter.remove();
                result = true;
            }
        }
        return result;
    }

    /**
     * Retains only elements matching given condition in the collection.
     * Returns true if the collection was modified.
     */
    default boolean retainAllMatching(Predicate<? super T> condition) {
        return removeAllMatching(not(condition));
    }

    /**
     * Removes one occurence of given item from the collection.
     * Returns true if the collection was modified.
     */
    default boolean remove(T item) {
        var iter = iterator();
        while (iter.hasNext()) {
            if (item.equals(iter.next())) {
                iter.remove();
                return true;
            }
        }
        return false;
    }
}
