/*
 * The MIT License
 *
 * Copyright 2015, 2022, 2024 Sergey Basalaev.
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

import me.sbasalaev.annotation.Out;

/**
 * List element with its corresponding index.
 *
 * @author Sergey Basalaev
 */
public record IndexedElement<@Out T extends Object>(int index, T element) {

    /**
     * Creates an instance of indexed element.
     * This is a legacy compatibility method from before IndexedElement became a record.
     */
    public static <T extends Object> IndexedElement<T> of(int index, T element) {
        return new IndexedElement<>(index, element);
    }

    @Override
    public String toString() {
        return index + " => " + element;
    }
}
