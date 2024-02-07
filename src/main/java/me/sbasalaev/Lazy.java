/*
 * The MIT License
 *
 * Copyright 2017, 2024 Sergey Basalaev.
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
import me.sbasalaev.annotation.Out;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Lazily evaluated value.
 * This class is thread-safe, the evaluation of the value only happens once
 * and it is safe to call {@link #get() } simultaneously from different threads.
 *
 * @param <T> the type of the value, may be nullable.
 * @author Sergey Basalaev
 * @since 1.1
 */
public final class Lazy<@Out T> implements Supplier<T> {

    private volatile @Nullable T value;
    private volatile @Nullable Supplier<T> supplier;
    private final Object lock = new Object();

    /** Creates new instance that lazily evaluates the value from the {@code supplier}. */
    public Lazy(Supplier<T> supplier) {
        this.value = null;
        this.supplier = Require.nonNull(supplier, "supplier");
    }

    /** Creates new instance with the {@code value} that is already evaluated. */
    public Lazy(T value) {
        this.value = value;
        this.supplier = null;
    }

    /**
     * Evaluates and returns the value.
     * If value is already evaluated, just returns it.
     */
    @Override
    @SuppressWarnings("nullness")
    public T get() {
        if (supplier != null) {
            synchronized (lock) {
                if (supplier != null) {
                    value = supplier.get();
                    supplier = null;
                }
            }
        }
        return value;
    }

    /** True iff value is already evaluated. */
    public boolean isEvaluated() {
        return supplier == null;
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
