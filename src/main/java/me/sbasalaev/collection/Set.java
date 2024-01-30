/*
 * The MIT License
 *
 * Copyright 2015, 2022-2024 Sergey Basalaev
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
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import static java.util.function.Predicate.not;
import me.sbasalaev.API;
import me.sbasalaev.Opt;
import me.sbasalaev.annotation.Out;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A collection of distinct elements.
 *
 * @author Sergey Basalaev
 */
public abstract class Set<@Out T extends Object> extends Collection<T> {

    /* CONSTRUCTORS */

    /** Constructor for subclasses. */
    public Set() { }

    private static final Set<?> EMPTY = new EmptySet();

    /** Empty set. */
    @SuppressWarnings("unchecked")
    public static <T extends Object> Set<T> empty() {
        return (Set<T>) EMPTY;
    }

    /**
     * Set containing given elements.
     * When there are duplicate elements according to {@code equals()},
     * only the first of them is put in the resulting set.
     */
    @SafeVarargs
    public static <T extends Object> Set<T> of(T... elements) {
        return fromTrustedArray(elements.clone());
    }

    /**
     * Set containing given elements.
     * The array of elements is not cloned.
     */
    @SafeVarargs
    static <T extends Object> Set<T> fromTrustedArray(T... elements) {
        return switch (elements.length) {
            case 0 -> empty();
            case 1 -> new SingletonSet<>(Objects.requireNonNull(elements[0]));
            default-> new RegularSet(HashWheel.make(elements, Function.identity()));
        };
    }

    /**
     * Set containing elements of all given sets.
     * This method returns immutable set unaffected by changes to the original sets.
     */
    @SafeVarargs
    public static <T extends Object> Set<T> union(Set<? extends T>... sets) {
        return union(List.of(sets));
    }

    /**
     * Set containing elements of all given sets.
     * This method returns immutable set unaffected by the changes to the original sets.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Object> Set<T> union(Traversable<? extends Set<? extends T>> sets) {
        Opt<? extends Set<? extends T>> firstNonEmpty = Opt.empty();
        boolean multipleNonEmpty = false;
        for (var set : sets) {
            if (set.isEmpty()) {
                continue;
            }
            if (firstNonEmpty.nonEmpty()) {
                multipleNonEmpty = true;
                break;
            }
            firstNonEmpty = Opt.of(set);
        }
        if (multipleNonEmpty) {
            return buildUnion(sets);
        }
        return firstNonEmpty.match(set -> (Set<T>) set.clone(), Set::empty);
    }

    private static <T extends Object> Set<T> buildUnion(Traversable<? extends Set<? extends T>> sets) {
        int len = API.sum(sets.map(Set::size));
        @SuppressWarnings("unchecked")
        T[] array = (T[]) new Object[len];
        int offset = 0;
        for (var set : sets) {
            set.fillArray(array, offset);
            offset += set.size();
        }
        return fromTrustedArray(array);
    }

    /** Set view of given java set. */
    public static <T extends Object> Set<T> fromJava(java.util.Set<T> javaSet) {
        return new Set<>() {
            @Override
            public boolean contains(Object element) {
                return javaSet.contains(element);
            }

            @Override
            public int size() {
                return javaSet.size();
            }

            @Override
            public Iterator<T> iterator() {
                return javaSet.iterator();
            }

            @Override
            public java.util.Set<T> toJava() {
                return javaSet;
            }
        };
    }

    /* INTERFACE */

    /** Whether given element is in this set. */
    public abstract boolean contains(Object element);

    /** Whether all elements of given set are also in this set. */
    public boolean isSuperset(Set<?> other) {
        return other.forall(this::contains);
    }

    /** Whether all elements of this set are also in the given set. */
    public boolean isSubset(Set<?> other) {
        return forall(other::contains);
    }

    /** Whether any element of this set is also in the given set. */
    public boolean intersects(Set<?> other) {
        return exists(other::contains);
    }

    /**
     * Set containing elements of both this and the other set.
     * This method returns immutable set unaffected by changes to the original sets.
     */
    public Set<?> unite(Set<?> other) {
        return union(this, other);
    }

    /**
     * Set containing only elements of this set that are also in another set.
     * This method returns immutable set unaffected by changes to the original sets.
     */
    public Set<T> intersect(Set<?> other) {
        if (other.isEmpty()) return empty();
        return filtered(other::contains);
    }

    /**
     * Set containing only elements of this set that are not in another set.
     * This method returns immutable set unaffected by changes to the original sets.
     */
    public Set<T> without(Set<?> other) {
        if (other.isEmpty()) return clone();
        return filtered(not(other::contains));
    }

    /**
     * Returns set with given mapping applied to all elements of this set.
     * This method returns immutable set unaffected by changes to this set.
     */
    @Override
    public <R extends Object> Set<R> mapped(Function<? super T, ? extends R> mapping) {
        return this.<R>map(mapping).toSet();
    }

    /**
     * Returns set that contains only elements of this set satisfying given condition.
     * This method returns immutable set unaffected by changes to this set.
     */
    @Override
    public Set<T> filtered(Predicate<? super T> condition) {
        return filter(condition).toSet();
    }

    /** Returns view of this set as Java set. */
    @SuppressWarnings("variance") // java set is invariant but we only support covariant methods
    public java.util.Set<T> toJava() {
        return new java.util.AbstractSet<T>() {
            @Override
            public boolean contains(Object o) {
                return Set.this.contains(o);
            }

            @Override
            public Iterator<T> iterator() {
                return Set.this.iterator();
            }

            @Override
            public int size() {
                return Set.this.size();
            }
        };
    }

    /* OVERRIDES */

    /**
     * Returns shallow immutable copy of this set.
     * May return itself if this set is immutable.
     */
    @Override
    @SuppressWarnings("unchecked")
    public final Set<T> clone() {
        if (isEmpty()) return empty();
        if (this instanceof ImmutableSet<T>) return this;
        @SuppressWarnings("unchecked")
        T[] array = (T[]) new Object[size()];
        fillArray(array, 0);
        return fromTrustedArray(array);
    }

    /**
     * Whether given object is equal to this set.
     * Two sets are equal if they contain the same elements, i.e.
     * <pre>this.isSuperset(other) &amp;&amp; this.isSubset(other)</pre>
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Set<?> set)) return false;
        return size() == set.size() && isSubset(set) && isSuperset(set);
    }

    /**
     * Hash code of the set.
     * The hash code of the set is a sum of hash codes of its elements.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        for (T item : this) {
            hash += item.hashCode();
        }
        return hash;
    }

    /* IMMUTABLE IMPLEMENTATIONS */

    private static abstract class ImmutableSet<@Out T extends Object> extends Set<T> {

        @Override
        public Set<T> toSet() {
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<T> toList() {
            return List.fromTrustedArray((T[]) toArray());
        }
    }

    /** Set with no elements. */
    private static final class EmptySet
            extends ImmutableSet<@NonNull Void>
            implements EmptyCollection {

        private EmptySet() { }

        @Override
        public boolean contains(@Nullable Object element) {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isSuperset(Set<?> other) {
            return other.isEmpty();
        }

        @Override
        public boolean isSubset(Set<?> other) {
            Objects.requireNonNull(other);
            return true;
        }

        @Override
        public boolean intersects(Set<?> other) {
            Objects.requireNonNull(other);
            return false;
        }

        @Override
        public List<@NonNull Void> toList() {
            return List.empty();
        }

        @Override
        public Object[] toArray() {
            return new Object[] { };
        }

        @Override
        public @NonNull Void[] toArray(IntFunction<@NonNull Void[]> arraySupplier) {
            return arraySupplier.apply(0);
        }

        @Override
        public void fillArray(@Nullable Object[] array, int fromIndex) {
            Objects.checkFromIndexSize(fromIndex, 0, array.length);
        }
    }

    /** Set containing only one element. */
    private static final class SingletonSet<T extends Object> extends ImmutableSet<T> {

        private final T e1;

        private SingletonSet(T element) {
            this.e1 = element;
        }

        @Override
        public boolean contains(Object element) {
            return e1.equals(element);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public Iterator<T> iterator() {
            return Iterators.of(e1);
        }
    }

    /** Immutable set backed by a hash wheel. */
    private static final class RegularSet<T extends Object> extends ImmutableSet<T> {

        private final HashWheel<Object,T> wheel;

        private RegularSet(HashWheel<Object,T> wheel) {
            this.wheel = wheel;
        }

        @Override
        public boolean contains(Object element) {
            return wheel.get(element) != null;
        }

        @Override
        public int size() {
            return wheel.size();
        }

        @Override
        public Iterator<T> iterator() {
            return wheel.iterator();
        }

        @Override
        public Object[] toArray() {
            return wheel.toArray();
        }

        @Override
        public T[] toArray(IntFunction<T[]> arraySupplier) {
            return wheel.toArray(arraySupplier);
        }

        @Override
        public void fillArray(@Nullable Object[] array, int fromIndex) {
            wheel.fillArray(array, fromIndex);
        }
    }
}
