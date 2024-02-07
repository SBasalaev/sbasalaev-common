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
package me.sbasalaev;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Assertions in code.
 * All methods throw {@code AssertionError} if the corresponding tests fail.
 * To check the preconditions of methods and constructors where the exceptional
 * situation is <i>the caller's fault</i> for not following method contract use
 * {@link Require} instead. The method names are chosen so that {@code Assert}
 * class itself is imported rather than individual methods, e.g.
 * {@snippet lang = java :
 * var list = List.of("A", "B", "C");
 * Assert.that(list.last().equals("C"));
 * }
 *
 * @author Sergey Basalaev
 * @see Require
 */
public final class Assert {

    private Assert() { }

    /**
     * Asserts given {@code expression} is true.
     * 
     * @param expression the expression to be checked.
     * @throws AssertionError if {@code expression} is false.
     */
    public static void that(boolean expression) {
        if (!expression) throw new AssertionError();
    }

    /**
     * Asserts given {@code expression} is false.
     *
     * @param expression the expression to be checked.
     * @throws AssertionError if {@code expression} is true.
     */
    public static void not(boolean expression) {
        if (expression) throw new AssertionError();
    }

    /**
     * Asserts given {@code value} is not {@code null}.
     *
     * @param <T> the type of the value.
     * @param value the value to be checked.
     * @return the value if it is not {@code null}.
     * @throws AssertionError if the {@code value} is {@code null}.
     * @see Require#nonNull(java.lang.Object, java.lang.String)
     * @see Objects#nonNull(java.lang.Object) 
     */
    public static <T> T nonNull(@Nullable T value) {
        if (value == null) throw new AssertionError();
        return value;
    }
}
