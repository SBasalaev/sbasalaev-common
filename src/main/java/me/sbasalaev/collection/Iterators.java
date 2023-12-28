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
package me.sbasalaev.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import me.sbasalaev.Opt;
import me.sbasalaev.Require;

/**
 * Factory methods for iterators.
 * All iterators produced by this class are read only and do not support remove().
 *
 * @author Sergey Basalaev
 */
public final class Iterators {

    private Iterators() { }

    private static final Iterator<?> EMPTY = new Iterator<@NonNull Void>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public @NonNull Void next() {
            throw new NoSuchElementException();
        }
    };

    /* CONSTRUCTORS */

    /** Empty iterator. */
    @SuppressWarnings("unchecked")
    public static <T extends @Nullable Object> Iterator<T> empty() {
        return (Iterator<T>) EMPTY;
    }

    /** Iterator over given elements in given order. */
    @SafeVarargs
    public static <T extends @Nullable Object> Iterator<T> of(T... elements) {
        return ofRange(elements, 0, elements.length);
    }

    /**
     * Iterator over range of given array.
     * 
     * @since 3.2
     */
    public static <T extends @Nullable Object> Iterator<T> ofRange(T[] elements, int offset, int size) {
        Objects.checkFromIndexSize(offset, size, elements.length);
        if (size == 0) return empty();
        return new Iterator<T>() {
            private int nextIndex = offset;

            @Override
            public boolean hasNext() {
                return nextIndex < size;
            }

            @Override
            public T next() {
                int n = nextIndex;
                if (n >= size) {
                    throw new NoSuchElementException();
                }
                nextIndex = n + 1;
                return elements[n];
            }
        };
    }

    /** Infinite iterator counting integers starting from given one. */
    public static Iterator<Integer> counting(int start) {
        return new Iterator<Integer>() {
            int next = start;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                int n = next;
                next = n + 1;
                return n;
            }
        };
    }

    /* TRANSFORMERS */

    /**
     * Wraps elements produced by given iterator into {@code Opt} values.
     * @since 3.2
     */
    public static <T extends @NonNull Object>
            Iterator<Opt<T>> wrapped(Iterator<@Nullable T> iterator) {
        return new Iterator<Opt<T>>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Opt<T> next() {
                return Opt.ofNullable(iterator.next());
            }
        };
    }

    /** Filters out iterator elements that do not satisfy {@code condition}. */
    public static <T extends @Nullable Object>
            Iterator<T> filter(Iterator<T> iterator, Predicate<? super T> condition) {
        Objects.requireNonNull(iterator, "iterator");
        Objects.requireNonNull(condition, "condition");
        return new Iterator<T>() {
            private @Nullable T nextItem = null;
            private boolean hasNextItem = false;

            private boolean fetchNext() {
                if (hasNextItem) return true;
                while (iterator.hasNext()) {
                    T item = iterator.next();
                    if (condition.test(item)) {
                        nextItem = item;
                        return hasNextItem = true;
                    }
                }
                return false;
            }

            @Override
            public boolean hasNext() {
                return fetchNext();
            }

            // works correctly, but the checker cannot cast
            // from @Nullable T to just T (which still may be nullable).
            @SuppressWarnings("nullness")
            @Override
            public T next() {
                if (!fetchNext()) {
                    throw new NoSuchElementException();
                }
                hasNextItem = false;
                return nextItem;
            }
        };
    }

    /** Applies {@code mapping} to all elements of {@code iterator}. */
    public static <T extends @Nullable Object, R extends @Nullable Object>
            Iterator<R> map(Iterator<T> iterator, Function<? super T, ? extends R> mapping) {
        Objects.requireNonNull(iterator, "iterator");
        Objects.requireNonNull(mapping, "mapping");
        return new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public R next() {
                return mapping.apply(iterator.next());
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    /**
     * Concatenates two iterators together.
     * The result first yields elements of the {@code first} iterator,
     * and once it is exhausted, elements of the {@code second}.
     */
    public static <T extends @Nullable Object>
            Iterator<T> chain(Iterator<? extends T> first, Iterator<? extends T> second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return first.hasNext() || second.hasNext();
            }

            @Override
            public T next() {
                if (first.hasNext()) {
                    return first.next();
                } else {
                    return second.next();
                }
            }
        };
    }

    /** Combines values produced by iterators using {@code combiner}. */
    public static <T extends @Nullable Object, U extends @Nullable Object, R extends @Nullable Object>
            Iterator<R> zipBy(Iterator<T> first, Iterator<U> second,
            BiFunction<? super T, ? super U, ? extends R> combiner) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        Objects.requireNonNull(combiner, "combiner");
        return new Iterator<R>() {
            @Override
            public boolean hasNext() {
                return first.hasNext() && second.hasNext();
            }

            @Override
            public R next() {
                return combiner.apply(first.next(), second.next());
            }
        };
    }

    /** Limit number of elements produced by {@code iterator} by {@code cap}. */
    public static <T extends @Nullable Object>
            Iterator<T> limit(Iterator<T> iterator, int cap) {
        Objects.requireNonNull(iterator, "iterator");
        Require.nonNegative(cap, "cap");
        return new Iterator<T>() {
            private int n = 0;

            @Override
            public boolean hasNext() {
                return n < cap && iterator.hasNext();
            }

            @Override
            public T next() {
                if (n >= cap) {
                    throw new NoSuchElementException();
                }
                n++;
                return iterator.next();
            }
        };
    }

    public static <T extends @Nullable Object>
            Iterator<T> takeWhile(Iterator<T> iterator, Predicate<? super T> condition) {
        Objects.requireNonNull(iterator, "iterator");
        Objects.requireNonNull(condition, "condition");
        return new Iterator<T>() {
            private @Nullable T nextItem = null;
            private boolean hasNextItem = false;
            private boolean conditionHolds = true;

            private boolean fetchNext() {
                if (hasNextItem) return true;
                if (!conditionHolds) return false;
                if (!iterator.hasNext()) return false;
                T item = iterator.next();
                if (condition.test(item)) {
                    hasNextItem = true;
                    nextItem = item;
                    return true;
                } else {
                    conditionHolds = false;
                    return false;
                }
            }

            @Override
            public boolean hasNext() {
                return fetchNext();
            }

            // works correctly, but the checker cannot cast
            // from @Nullable T to just T (which still may be nullable).
            @SuppressWarnings("nullness")
            @Override
            public T next() {
                if (!fetchNext()) {
                    throw new NoSuchElementException();
                }
                hasNextItem = false;
                return nextItem;
            }
        };
    }
}
