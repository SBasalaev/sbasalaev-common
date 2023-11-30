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
package ru.nsu.sbasalaev;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Assertions in code.
 * All methods throw AssertionError if the corresponding tests fail.
 *
 * @author Sergey Basalaev
 */
public final class Assert {

    private Assert() { }

    /** Asserts given expression is true. */
    public static void that(boolean expression) {
        if (!expression) throw new AssertionError();
    }

    /** Asserts given expression is false. */
    public static void not(boolean expression) {
        if (expression) throw new AssertionError();
    }

    /** Asserts given reference is not {@code null}. */
    public static <T> T nonNull(@Nullable T reference) {
        if (reference == null) throw new AssertionError();
        return reference;
    }
}
