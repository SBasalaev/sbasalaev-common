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
package me.sbasalaev;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Stream;
import me.sbasalaev.annotation.Out;
import me.sbasalaev.collection.Collection;
import me.sbasalaev.collection.Iterators;
import me.sbasalaev.collection.Traversable;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Optional value.
 * {@code Opt} may contain a single value or no value. The value may be
 * fetched by a family of {@code orElse} methods providing the default
 * in case the value is absent, handled in a pattern maching style as
 * <pre>
 *   return optional.match(
 *     (value) -> ...,
 *     ()      -> ...
 *   );</pre>
 * or traversed in a {@code for} statement
 * <pre>
 *   for (var value : optional) {
 *     ...
 *   }</pre>
 * This class predates {@link java.util.Optional} introduced by Java 8 that
 * serves the same purpose but is not {@linkplain Traversable traversable}.
 * Values can be converted between {@link Opt} and {@link java.util.Optional}
 * with {@link #fromJava(java.util.Optional) fromJava()} and {@link #toJava() }.
 *
 * @author Sergey Basalaev
 */
public final class Opt<@Out T extends Object> extends Collection<T> {

    private final @Nullable T value;

    private Opt(@Nullable T value) {
        this.value = value;
    }

    /* CONSTRUCTORS */

    private static final Opt<?> NONE = new Opt<>(null);

    /** Empty optional. */
    @SuppressWarnings("unchecked")
    public static <T extends Object> Opt<T> empty() {
        return (Opt<T>) NONE;
    }

    /** Non-empty optional. */
    public static <T extends Object> Opt<T> of(T value) {
        return new Opt<>(Require.nonNull(value, "value"));
    }

    /** Optional of non-null value, empty optional for null. */
    public static <T extends Object> Opt<T> ofNullable(@Nullable T value) {
        return value != null ? new Opt<>(value) : empty();
    }

    /** Converts Java optional to Opt. */
    public static <T extends Object> Opt<T> fromJava(java.util.Optional<T> optional) {
        return optional.map(Opt::of).orElseGet(Opt::empty);
    }

    /* INTERFACE */

    /** Returns value in this optional or {@code null} if the optional is empty. */
    public @Nullable T orElseNull() {
        return value;
    }

    // If Java allowed union types the method could have the typesafe signature of
    //     <S> T|S orElse(S defaultValue)
    /** Returns value in this optional or {@code defaultValue} if the optional is empty. */
    public T orElse(@SuppressWarnings("variance") T defaultValue) {
        Require.nonNull(defaultValue, "defaultValue");
        return value != null ? value : defaultValue;
    }

    // If Java allowed union types the method could have the typesafe signature of
    //     <S> T|S orElse(Supplier<? extends S> defaultValue)
    /** Returns value in this optional or the value from {@code valueSupplier} if the optional is empty. */
    public T orElseGet(@SuppressWarnings("variance") Supplier<? extends T> valueSupplier) {
        Require.nonNull(valueSupplier, "valueSupplier");
        return value != null ? value : valueSupplier.get();
    }

    /** Returns value in this optional or throws {@code NoSuchElementException} with given {@code message}. */
    public T orElseThrow(String message) throws NoSuchElementException {
        if (value != null) {
            return value;
        }
        throw new NoSuchElementException(message);
    }

    /**
     * Returns value in this optional or throws the exception from given supplier.
     * @since 3.2
     */
    public <X extends Throwable> T orElseThrow(Supplier<X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        }
        throw exceptionSupplier.get();
    }

    /**
     * Either maps the value or produces one if the optional is empty.
     * Can be used in a pattern matching style:
     * <pre>
     *   return optional.match(
     *     (value) -> ...,
     *     ()      -> ...
     *   );
     * </pre>
     */
    public <R> R match(Function<T, R> onNonEmpty, Supplier<R> onEmpty) {
        Require.nonNull(onNonEmpty, "onNonEmpty");
        Require.nonNull(onEmpty, "onEmpty");
        return value != null ? onNonEmpty.apply(value) : onEmpty.get();
    }

    /**
     * Either runs {@code action} on the value or {@code emptyAction} if the optional is empty.
     * Can be used in a pattern matching style:
     * <pre>
     *   optional.matchDo(
     *     (value) -> { ... },
     *     ()      -> { ... }
     *   );
     * </pre>
     */
    public void matchDo(Consumer<T> action, Runnable emptyAction) {
        Require.nonNull(action, "action");
        Require.nonNull(emptyAction, "emptyAction");
        if (value != null) {
            action.accept(value);
        } else {
            emptyAction.run();
        }
    }

    /** Converts this value to Java optional. */
    public java.util.Optional<T> toJava() {
        return java.util.Optional.ofNullable(value);
    }

    /**
     * Maps value in this optional using given mapping.
     * This is eager operation that applies the mapping immediately.
     */
    @Override
    public <R extends Object> Opt<R> mapped(Function<? super T, ? extends R> mapping) {
        Require.nonNull(mapping, "mapping");
        return value != null ? Opt.of(mapping.apply(value)) : empty();
    }

    /**
     * Returns this optional if its value matches given condition, empty optional otherwise.
     * This is eager operation that tests the condition immediately.
     */
    @Override
    public Opt<T> filtered(Predicate<? super T> condition) {
        Require.nonNull(condition, "condition");
        return value != null && condition.test(value) ? this : empty();
    }

    /** Returns this optional if its value is of given class, empty optional otherwise. */
    @Override
    @SuppressWarnings("unchecked")
    public <U extends Object> Opt<U> narrow(Class<U> clazz) {
        return clazz.isInstance(value) ? (Opt<U>) this : empty();
    }

    /* OVERRIDEN METHODS */

    @Override
    public Spliterator<T> spliterator() {
        return java.util.Spliterators.spliterator(toArray(), Spliterator.NONNULL);
    }

    @Override
    public Stream<T> stream() {
        return value != null ? Stream.of(value) : Stream.empty();
    }

    /** Returns this optional. */
    @Override
    public Opt<T> clone() {
        return this;
    }

    @Override
    public <R extends Object> R fold(R first, BiFunction<? super R, ? super T, ? extends R> combine) {
        return value != null ? combine.apply(first, value) : first;
    }

    @Override
    public int size() {
        return value != null ? 1 : 0;
    }

    /* STANDARD STUFF */

    @Override
    public Iterator<T> iterator() {
        return value != null ? Iterators.of(value) : Iterators.empty();
    }

    /**
     * Whether given object is equal to this optional.
     *
     * The object is equal to this optional if it is an instance of Opt
     * and either both are empty or contain equal values.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Opt<?> opt)) return false;
        return Objects.equals(this.value, opt.value);
    }

    /** Hashcode of the value or 0 for empty optional. */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
