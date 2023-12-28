/*
 * The MIT License
 *
 * Copyright 2023 Sergey Basalaev
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

import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests of immutable lists.
 *
 * @author Sergey Basalaev
 */
public class ListTest {

    public ListTest() { }

    @Test
    public void testEmpty() {
        assertSame(List.empty(), List.of());
    }

    @Test
    public void testOf() {
        assertEquals(List.of(1, 2, 4), List.of(1, 2, 4));
    }

    @Test
    public void testConcatenated() {
        assertEquals(
            List.of(2, 3, 5, 7, 11),
            List.concatenated(List.of(2, 3), List.empty(), List.of(5, 7), List.of(11))
        );
    }

    @Test
    public void testFromJava() {
        assertEquals(
            List.of(1, 2, 4),
            List.fromJava(java.util.List.of(1, 2, 4))
        );
    }

    @Test
    public void testRepeat() {
        assertSame(List.repeat("A", 0), List.empty());
        List<String> repeated = List.repeat("A", 3);
        assertEquals(List.of("A", "A", "A"), repeated);
        assertEquals(List.of("A", "A"), repeated.from(1));
        assertEquals(List.of("A", "A"), repeated.take(2));
        assertEquals("A", repeated.last());
    }

    @Test
    public void testFindIndex() {
        List<String> list = List.of("A", "B", "B", "C");
        assertEquals(1, list.findIndex("B"::equals));
        assertEquals(2, list.findIndex("B"::equals, 2));
        assertEquals(2, list.findLastIndex("B"::equals));
        assertEquals(-1, list.findIndex("D"::equals));
    }

    @Test
    public void testBinarySearch() {
        List<String> list = List.of("A", "B", "B", "D");
        assertEquals(2, list.binarySearch("B"::compareTo));
        assertEquals(-1, list.binarySearch("C"::compareTo));
    }

    @Test
    public void testIndexed() {
        var list = List.of("A", "B", "C");
        var indexedList = List.of(
            IndexedElement.of(0, "A"),
            IndexedElement.of(1, "B"),
            IndexedElement.of(2, "C")
        );
        assertEquals(indexedList, list.indexed());
        assertSame(List.empty(), List.empty().indexed());
    }

    @Test
    public void testFrom() {
        var list = List.of("A", "B", "C");
        assertEquals(List.of("B", "C"), list.from(1));
        assertSame(List.empty(), list.from(5));
        assertSame(list, list.from(0));
        assertThrows(IllegalArgumentException.class, () -> list.from(-1));
    }

    @Test
    public void testTake() {
        var list = List.of("A", "B", "C");
        assertEquals(List.of("A"), list.take(1));
        assertSame(List.empty(), list.take(0));
        assertSame(list, list.take(5));
        assertThrows(IllegalArgumentException.class, () -> list.take(-1));
    }

    @Test
    public void testReversed() {
        var list = List.of("A", "B", "C");
        var singleItem = List.of("C");
        assertEquals(List.of("C", "B", "A"), list.reversed());
        assertSame(list, list.reversed().reversed());
        assertSame(List.empty(), List.empty().reversed());
        assertSame(singleItem, singleItem.reversed());
        assertEquals(List.of("B", "A"), list.reversed().from(1));
        assertEquals(List.of("C", "B"), list.reversed().take(2));
    }

    @Test
    public void testZip() {
        assertEquals(List.of(4, 6), List.of(1, 2, 3).zip(List.of(3, 4), Integer::sum));
        assertSame(List.empty(), List.<Integer>empty().zip(List.of(3, 4), Integer::sum));
    }

    @Test
    public void testToJava() {
        assertEquals(java.util.List.of(1, 2), List.of(1, 2).toJava());
    }

    @Test
    public void testMap() {
        var list = List.of("A", "BB", "CCC");
        var lazyMapped = list.map(String::length);
        assertEquals(List.of(1, 2, 3), lazyMapped);
        assertEquals(List.of(1, 2, 3), lazyMapped.clone());
        assertNotSame(lazyMapped, lazyMapped.clone());
        assertSame(List.empty(), List.<String>empty().map(String::length));
    }

    @Test
    public void testMapped() {
        var list = List.of("A", "BB", "CCC");
        var eagerMapped = list.mapped(String::length);
        assertEquals(List.of(1, 2, 3), eagerMapped);
        assertEquals(List.of(1, 2, 3), eagerMapped.clone());
        assertSame(eagerMapped, eagerMapped.clone());
        assertSame(List.empty(), List.<String>empty().mapped(String::length));
    }

    @Test
    public void testFilter() {
        var list = List.of("A", "BB", "CCC");
        var lazyFiltered = list.filter(s -> s.length() <= 2);
        assertIterableEquals(List.of("A", "BB"), lazyFiltered);
        assertNotEquals(List.of("A", "BB"), lazyFiltered);
        assertSame(List.empty(), List.<String>empty().filter(s -> s.length() <= 2));
    }

    @Test
    public void testFiltered() {
        var list = List.of("A", "BB", "CCC");
        var eagerFiltered = list.filtered(s -> s.length() <= 2);
        assertEquals(List.of("A", "BB"), eagerFiltered);
        assertSame(eagerFiltered, eagerFiltered.clone());
        assertSame(List.empty(), List.<String>empty().filtered(s -> s.length() <= 2));
    }

    @Test
    public void testPairs() {
        var list = List.of("A", "B", "C");
        assertIterableEquals(
            List.of(Entry.of("A", "B"), Entry.of("A", "C"), Entry.of("B", "C")),
            list.pairs(Entry::of)
        );
    }

    @Test
    public void testHashCode() {
        assertEquals(Objects.hash("A", "B"), List.of("A", "B").hashCode());
    }
}
