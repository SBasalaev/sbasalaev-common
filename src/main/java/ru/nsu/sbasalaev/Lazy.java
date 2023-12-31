/*
 * The MIT License
 *
 * Copyright 2017 Sergey Basalaev.
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

import static java.util.Objects.requireNonNull;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import ru.nsu.sbasalaev.annotation.Out;

/**
 * Lazily evaluated value.
 *
 * @since 1.1
 *
 * @author Sergey Basalaev
 */
public final class Lazy<@Out T extends @NonNull Object> implements Supplier<T> {

    private @MonotonicNonNull T value;
    private final Supplier<T> supplier;

    /** Creates new instance that lazily evaluates given supplier. */
    public Lazy(Supplier<T> supplier) {
        this.supplier = requireNonNull(supplier);
    }

    /**
     * Evaluates and returns value of this Lazy.
     * If value is already evaluated, just returns it.
     */
    @Override
    public T get() {
        T v = value;
        if (v == null) {
            v = supplier.get();
            value = v;
        }
        return v;
    }

    /** True iff value is already evaluated. */
    public boolean isEvaluated() {
        return value != null;
    }

    @Override
    public String toString() {
        if (isEvaluated()) {
            return "Lazy{" + value + "}";
        } else {
            return "Lazy";
        }
    }
}
