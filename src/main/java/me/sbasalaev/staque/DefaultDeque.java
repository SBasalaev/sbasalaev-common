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
package me.sbasalaev.staque;

import java.util.ArrayDeque;
import java.util.Iterator;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Deque implementation.
 *
 * @author Sergey Basalaev
 */
final class DefaultDeque<T extends @NonNull Object> implements Deque<T>, Iterator<T> {

    private final ArrayDeque<T> impl;

    DefaultDeque() {
        impl = new ArrayDeque<>();
    }

    @Override
    public void enqueue(T item) {
        impl.addLast(item);
    }

    @Override
    public void push(T item) {
        impl.addFirst(item);
    }

    @Override
    public T peek() {
        return impl.getFirst();
    }

    @Override
    public T take() {
        return impl.removeFirst();
    }

    @Override
    public void clear() {
        impl.clear();
    }

    @Override
    public boolean isEmpty() {
        return impl.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        return !impl.isEmpty();
    }

    @Override
    public T next() {
        return impl.removeFirst();
    }
}
