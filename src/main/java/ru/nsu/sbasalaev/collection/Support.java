/*
 * The MIT License
 *
 * Copyright 2015, 2021 Sergey Basalaev.
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

import java.lang.reflect.Array;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import ru.nsu.sbasalaev.annotation.Nullable;

/**
 * Supporting methods for creation of immutable lists, sets and maps.
 *
 * @author Sergey Basalaev
 */
final class Support {

    private Support() { }

    /**
     * Initializer for immutable sets and maps backed by arrays.
     * Origin array contains distinct elements in order they are
     * passed to the set or map constructor. Searched array
     * contains the same elements interspersed by nulls and sorted
     * by hash code.
     */
    public record Initializer<T>(T[] origin, @Nullable T[] searched) { }

    /**
     * Creates initializer for given elements using supplied
     * {@code compare} and {@code hash} functions.
     * During creation NPE is thrown if {@code elements} is
     * null or contains nulls.
     */
    public static <T> Initializer<T> make(T[] elements, BiPredicate<T, T> equator, ToIntFunction<T> hasher) {
        int elementsLen = elements.length;
        int size = 0;
        // populating searched with elements
        // if elements contains duplicates they are replaced by nulls
        @SuppressWarnings("unchecked")
        T[] searched = (T[]) Array.newInstance(elements.getClass().getComponentType(), elementsLen * 2);
        for (int i = 0; i < elementsLen; i++) {
            var e = elements[i];
            int index = Math.floorMod(hasher.applyAsInt(e), searched.length); // implicit NPE here
            boolean found = false;
            while (searched[index] != null) {
                if (equator.test(searched[index], e)) {
                    found = true;
                    break;
                }
                index++;
                if (index == searched.length) index = 0;
            }
            if (found) {
                elements[i] = null;
            } else {
                searched[index] = e;
                size++;
            }
        }
        // if there are no duplicates then elements are returned as origin
        // otherwise we copy elements to the new origin
        if (size == elementsLen) {
            return new Initializer<>(elements, searched);
        }
        @SuppressWarnings("unchecked")
        T[] origin = (T[]) Array.newInstance(elements.getClass().getComponentType(), size);
        int insert = 0;
        for (T element : elements) {
            if (element != null) {
                origin[insert] = element;
                insert++;
            }
        }
        return new Initializer<>(origin, searched);
    }
}
