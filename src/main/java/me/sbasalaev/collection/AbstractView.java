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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Common superclass for traversables that only serve as chained views.
 *
 * @author Sergey Basalaev
 */
abstract class AbstractView<T extends Object> implements Traversable<T> {

    @Override
    public boolean equals(@Nullable Object obj) {
        throw new AssertionError("Not comparable");
    }

    @Override
    public int hashCode() {
        throw new AssertionError("Not hashable");
    }

    @Override
    public String toString() {
        return "{...}";
    }
}
