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

import java.util.Objects;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Precondition checks.
 * This class contains convenience static methods that help to check whether
 * the preconditions of a method or constructor are met. In contrast to
 * {@link Assert} these should only be used when the exceptional situation is
 * <i>the caller's fault</i>. The method names are chosen so that
 * {@code Require} class itself is imported rather than individual methods.
 * For instance, the start of the method repeating the string given number of
 * times could be
 * {@snippet language=java :
 * String repeat(String string, int times) {
 *     Require.nonNull(string, "string");
 *     Require.nonNegative(times, "times");
 *     ...
 * }
 * }
 *
 * @author Sergey Basalaev
 * @see Assert
 */
public final class Require {

    private Require() { }

    /**
     * Checks that the {@code value} is not {@code null}.
     *
     * @param <T> the type of the value.
     * @param value the value to be checked.
     * @param name the name of the parameter to report when there is {@code null}.
     * @return the value if it is not {@code null}.
     * @throws NullPointerException if the {@code value} is {@code null}.
     * @since 4.1
     * @see Assert#nonNull(java.lang.Object)
     * @see Objects#nonNull(java.lang.Object)
     */
    public static <T extends Object> T nonNull(@Nullable T value, String name) {
        if (value == null) throw new NullPointerException(name + " is null");
        return value;
    }

    /**
     * Checks that the {@code array} is not {@code null} and contains no {@code null} elements.
     *
     * @param <T> the type of the elements of the array.
     * @param array the array to check for {@code null} references.
     * @param name the name of the parameter to report when there is {@code null}.
     * @return the array if it is not {@code null} and contains no {@code null} elements.
     * @throws NullPointerException if either {@code array} or any of its elements is {@code null}.
     * @since 4.1
     */
    @SuppressWarnings("nullness") // the checker cannot prove the array has no nulls
    public static <T extends Object> T[] noNulls(@Nullable T @Nullable [] array, String name) {
        if (array == null) throw new NullPointerException(name + " is null");
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] == null) {
                throw new NullPointerException(name + "[" + i + "] is null");
            }
        }
        return array;
    }

    /**
     * Checks that the {@code array} is not {@code null} and contains no {@code null} elements.
     *
     * @param <T> the type of the elements of the array.
     * @param array the array to check for {@code null} references.
     * @return the array if it is not {@code null} and contains no {@code null} elements.
     * @throws NullPointerException if either {@code array} or any of its elements is {@code null}.
     */
    public static <T extends Object> T[] noNulls(@Nullable T @Nullable [] array) {
        return noNulls(array, "array");
    }

    /**
     * Ensures {@code condition} is {@code true}.
     *
     * @param condition a boolean expression to be checked.
     * @param message the message to be constructed and reported when {@code condition} is false.
     * @throws IllegalArgumentException if {@code condition} is {@code false}.
     */
    public static void argument(boolean condition, Supplier<String> message) {
        if (!condition) {
            throw new IllegalArgumentException(message.get());
        }
    }

    /**
     * Ensures {@code condition} is {@code true}.
     *
     * @param condition a boolean expression to be checked.
     * @param message the message to be reported when {@code condition} is false.
     * @throws IllegalArgumentException if {@code condition} is {@code false}.
     */
    public static void argument(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Ensures {@code condition} is {@code true}.
     *
     * @param condition a boolean expression to be checked.
     * @param message the message to be constructed and reported when {@code condition} is false.
     * @throws IllegalStateException if {@code condition} is {@code false}.
     */
    public static void state(boolean condition, Supplier<String> message) {
        if (!condition) {
            throw new IllegalStateException(message.get());
        }
    }

    /**
     * Ensures {@code condition} is {@code true}.
     *
     * @param condition a boolean expression to be checked.
     * @param message the message to be reported when {@code condition} is false.
     * @throws IllegalStateException if {@code condition} is {@code false}.
     */
    public static void state(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Checks that the {@code value} is non-negative and returns it.
     *
     * @param value the value to be checked.
     * @param name the name of the parameter to report when the value is negative.
     * @return the value if it is non-negative.
     * @throws IllegalArgumentException if the {@code value} is negative.
     */
    public static int nonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("expected non-negative value but " + name + "=" + value);
        }
        return value;
    }

    /**
     * Checks that the {@code value} is non-negative and returns it.
     *
     * @param value the value to be checked.
     * @param name the name of the parameter to report when the value is negative.
     * @return the value if it is non-negative.
     * @throws IllegalArgumentException if the {@code value} is negative.
     * @since 4.1
     */
    public static long nonNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("expected non-negative value but " + name + "=" + value);
        }
        return value;
    }

    /**
     * Checks that the {@code value} is positive and returns it.
     *
     * @param value the value to be checked.
     * @param name the name of the parameter to report when the value is not positive.
     * @return the value if it is positive.
     * @throws IllegalArgumentException if the {@code value} is zero or negative.
     * @since 4.1
     */
    public static int positive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException("expected positive value but " + name + "=" + value);
        }
        return value;
    }

    /**
     * Checks that the {@code value} is positive and returns it.
     *
     * @param value the value to be checked.
     * @param name the name of the parameter to report when the value is not positive.
     * @return the value if it is positive.
     * @throws IllegalArgumentException if the {@code value} is zero or negative.
     * @since 4.1
     */
    public static long positive(long value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException("expected positive value but " + name + "=" + value);
        }
        return value;
    }
}
