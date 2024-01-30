/*
 * The MIT License
 *
 * Copyright 2015, 2024 Sergey Basalaev
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
package me.sbasalaev;

import java.util.function.Supplier;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Argument validation methods.
 *
 * @author Sergey Basalaev
 */
public final class Require {

    private Require() { }

    /**
     * Checks that array is not {@code null} and contains no {@code null} elements.
     *
     * @throws NullPointerException if either {@code array} or any of its elements is {@code null}.
     */
    @SuppressWarnings("nullness") // NPEs here are expected behavior
    public static <T extends Object> T[] noNulls(@Nullable T @Nullable [] array) {
        for (T item : array) { // implicit NPE
            if (item == null) throw new NullPointerException("array contains nulls");
        }
        return array;
    }

    /**
     * Throws IllegalArgumentException with given {@code message} if the {@code condition} fails.
     *
     * @throws IllegalArgumentException if the {@code condition} fails.
     */
    public static void argument(boolean condition, Supplier<String> message)
        throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(message.get());
        }
    }

    /**
     * Throws IllegalArgumentException with given {@code message} if the {@code condition} fails.
     *
     * @throws IllegalArgumentException if the {@code condition} fails.
     */
    public static void argument(boolean condition, String message)
        throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Throws IllegalStateException with given {@code message} if the {@code condition} fails.
     *
     * @throws IllegalStateException if the {@code condition} fails.
     */
    public static void state(boolean condition, Supplier<String> message)
        throws IllegalStateException {
        if (!condition) {
            throw new IllegalStateException(message.get());
        }
    }

    /**
     * Throws IllegalStateException with given {@code message} if the {@code condition} fails.
     *
     * @throws IllegalStateException if the {@code condition} fails.
     */
    public static void state(boolean condition, String message)
        throws IllegalStateException {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Checks that argument is non-negative and returns it.
     *
     * @throws IllegalArgumentException if {@code value} is negative.
     */
    public static @NonNegative int nonNegative(int value, String name) {
        if (value < 0) throw new IllegalArgumentException(name + " is negative");
        return value;
    }
}
