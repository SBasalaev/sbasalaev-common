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
package ru.nsu.sbasalaev.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import ru.nsu.sbasalaev.API;
import ru.nsu.sbasalaev.Require;
import ru.nsu.sbasalaev.annotation.Nullable;

/**
 * Factory methods for iterators.
 *
 * @author Sergey Basalaev
 */
public final class Iterators {

    private Iterators() { }

    private static final Iterator<?> EMPTY = new Iterator<>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }
    };

    /* CONSTRUCTORS */

    /** Empty iterator. */
    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> empty() {
        return (Iterator<T>) EMPTY;
    }

    /** Iterator over given elements in given order. */
    @SafeVarargs
    public static <T> Iterator<T> of(T... elements) {
        if (elements.length == 0) return empty();
        return new Iterator<T>() {
            private int nextIndex = 0;

            @Override
            public boolean hasNext() {
                return nextIndex < elements.length;
            }

            @Override
            public T next() {
                int n = nextIndex;
                if (n >= elements.length) {
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

    /** Filters out iterator elements that do not satisfy {@code condition}. */
    public static <T> Iterator<T> filter(Iterator<T> iterator, Predicate<? super T> condition) {
        Objects.requireNonNull(iterator, "iterator");
        Objects.requireNonNull(condition, "condition");
        return new Iterator<T>() {
            private @Nullable T nextItem = null;
            private boolean hasNextItem = false;

            private boolean fetchNext() {
                if (hasNextItem) {
                    return true;
                }
                while (iterator.hasNext()) {
                    T item = iterator.next();
                    if (condition.test(item)) {
                        nextItem = item;
                        hasNextItem = true;
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean hasNext() {
                return fetchNext();
            }

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
    public static <T, R> Iterator<R> map(Iterator<T> iterator, Function<? super T, ? extends R> mapping) {
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
    public static <T> Iterator<T> chain(Iterator<? extends T> first, Iterator<? extends T> second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return first.hasNext() || second.hasNext();
            }

            @Override
            public T next() {
                return first.hasNext() ? first.next() : second.next();
            }
        };
    }

    /** Combines values produced by iterators using {@code combiner}. */
    public static <T,U,R> Iterator<R> zipBy(Iterator<T> first, Iterator<U> second,
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
    public static <T> Iterator<T> limit(Iterator<T> iterator, int cap) {
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

    public static <T> Iterator<T> takeWhile(Iterator<T> iterator, Predicate<? super T> condition) {
        Objects.requireNonNull(iterator, "iterator");
        Objects.requireNonNull(condition, "condition");
        return new Iterator<T>() {
            private T nextItem;
            private boolean holds;

            {
                fetchNext();
            }

            private void fetchNext() {
                if (iterator.hasNext()) {
                    T item = iterator.next();
                    if (condition.test(item)) {
                        holds = true;
                        nextItem = item;
                    } else {
                        holds = false;
                    }
                } else {
                    holds = false;
                }
            }

            @Override
            public boolean hasNext() {
                return holds;
            }

            @Override
            public T next() {
                if (!holds) {
                    throw new NoSuchElementException();
                }
                T item = nextItem;
                fetchNext();
                return item;
            }
        };
    }
}
