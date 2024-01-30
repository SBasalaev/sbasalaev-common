/*
 * The MIT License
 *
 * Copyright 2024 Sergey Basalaev
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

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Spliterators for our collections.
 *
 * @author Sergey Basalaev
 */
final class Spliterators {

    public static final Spliterator<@NonNull Void> EMPTY = new Empty();

    public static <T, U> Spliterator<U> mapped(Spliterator<T> split, Function<? super T, ? extends U> mapping) {
        return new Mapped<>(split, mapping);
    }

    private static final class Empty implements Spliterator<@NonNull Void> {

        private Empty() { }

        @Override
        public int characteristics() {
            return Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL
                    | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED;
        }

        @Override
        public long estimateSize() {
            return 0;
        }

        @Override
        public void forEachRemaining(Consumer<? super @NonNull Void> action) {
            Objects.requireNonNull(action);
        }

        @Override
        public boolean tryAdvance(Consumer<? super @NonNull Void> action) {
            Objects.requireNonNull(action);
            return false;
        }

        @Override
        public Spliterator<@NonNull Void> trySplit() {
            return null;
        }
    }

    private static final class Mapped<T, U> implements Spliterator<U> {

        private final Spliterator<T> split;
        private final Function<? super T, ? extends U> mapping;

        private Mapped(Spliterator<T> split, Function<? super T, ? extends U> mapping) {
            this.split = split;
            this.mapping = mapping;
        }

        @Override
        public int characteristics() {
            return split.characteristics() & ~(Spliterator.DISTINCT | Spliterator.SORTED);
        }

        @Override
        public long estimateSize() {
            return split.estimateSize();
        }

        @Override
        public boolean tryAdvance(Consumer<? super U> action) {
            return split.tryAdvance(t -> action.accept(mapping.apply(t)));
        }

        @Override
        public @Nullable Spliterator<U> trySplit() {
            var prefixSplit = split.trySplit();
            return prefixSplit == null ? null : new Mapped<>(prefixSplit, mapping);
        }
    }
}
