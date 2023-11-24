/*
 * The MIT License
 *
 * Copyright 2018 Sergey Basalaev.
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
package ru.nsu.sbasalaev.staque;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.nsu.sbasalaev.annotation.Out;

/**
 * Common superinterface for stack and queue.
 * Feed can be iterated over but doing so changes its state, i.e.
 * every item returned by iterator is removed from the feed.
 *
 * @since 2.0
 *
 * @author Sergey Basalaev
 */
public interface Feed<@Out T extends @NonNull Object> extends Iterable<T> {

    /** Returns true if the feed is empty. */
    boolean isEmpty();

    /** Returns true if the feed is non-empty. */
    default boolean nonEmpty() {
        return !isEmpty();
    }

    /** Removes all elements from the feed. */
    void clear();

    /**
     * Removes top element from the feed and returns it.
     * @throws NoSuchElementException if the feed is empty.
     */
    T take() throws NoSuchElementException;

    /**
     * Returns top element from the feed without removing it.
     * @throws NoSuchElementException if the feed is empty.
     */
    T peek() throws NoSuchElementException;

    /**
     * State changing iterator of this feed.
     * Elements produced by the iterator are removed from this feed.
     * Specifically {@link Iterator#next() } has the same effect as
     * calling {@link #take() }. The iterator does not implement {@code remove()}
     * since element returned is already removed from the feed.
     */
    @Override
    public Iterator<T> iterator();
}
