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
package me.sbasalaev.collection;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests of immutable sets.
 *
 * @author Sergey Basalaev
 */
public class SetTest {

    public SetTest() { }

    @Test
    public void testEmpty() {
        assertSame(Set.empty(), Set.of());
    }

    @Test
    public void testOf() {
        assertEquals(Set.of(1, 2, 3), Set.of(3, 2, 1));
        assertEquals(Set.of("Aa", "BB"), Set.of("BB", "Aa")); // hashCode collision
        assertThrows(NullPointerException.class, () -> Set.of((Object) null));
        assertThrows(NullPointerException.class, () -> Set.of(1, null));
    }

    @Test
    public void testUnion() {
        assertEquals(
            Set.of(1,2,3,4),
            Set.union(Set.of(1,2,3), Set.of(), Set.of(1,2,4))
        );
    }

    @Test
    public void testFromJava() {
        assertEquals(Set.of(1, 2, 3), Set.fromJava(java.util.Set.of(1, 2, 3)));
    }

    @Test
    public void testContains() {
        Set<String> set = Set.of("Aa", "BB");
        assertTrue(set.contains("Aa"));
        assertTrue(set.contains("BB"));
        assertFalse(set.contains("Ba"));
        assertFalse(set.contains(1));
    }

    @Test
    public void testIsSubset() {
        var emptySet = Set.of();
        var set12 = Set.of(1, 2);
        var set13 = Set.of(1, 3);
        var set123 = Set.of(1, 2, 3);
        assertTrue(emptySet.isSubset(emptySet));
        assertTrue(emptySet.isSubset(set12));
        assertTrue(set13.isSubset(set123));
        assertTrue(set123.isSubset(set123));
        assertFalse(set12.isSubset(set13));
        assertFalse(set13.isSubset(set12));
    }

    @Test
    public void testIntersects() {
        var emptySet = Set.of();
        var set12 = Set.of(1, 2);
        var set13 = Set.of(1, 3);
        var set34 = Set.of(3, 4);
        assertTrue(set12.intersects(set13));
        assertFalse(set12.intersects(set34));
        assertFalse(set13.intersects(emptySet));
        assertFalse(emptySet.intersects(set12));
    }

    @Test
    public void testIntersect() {
        var emptySet = Set.of();
        var set12 = Set.of(1, 2);
        var set13 = Set.of(1, 3);
        var set34 = Set.of(3, 4);
        assertEquals(Set.of(1), set12.intersect(set13));
        assertEquals(emptySet, set12.intersect(set34));
        assertEquals(emptySet, set13.intersect(emptySet));
    }

    @Test
    public void testWithout() {
        var emptySet = Set.of();
        var set12 = Set.of(1, 2);
        var set13 = Set.of(1, 3);
        var set34 = Set.of(3, 4);
        assertEquals(Set.of(2), set12.without(set13));
        assertEquals(set12, set12.without(set34));
        assertEquals(set13, set13.without(emptySet));
        assertEquals(emptySet, set13.without(set13));
    }

    @Test
    public void testMapped() {
        assertEquals(Set.of(2, 1), Set.of("a", "b", "cc").mapped(String::length));
    }

    @Test
    public void testFiltered() {
        assertEquals(Set.of(5, 7), Set.of(2, 3, 5, 7).filtered(n -> n > 3));
    }

    @Test
    public void testToJava() {
        assertEquals(java.util.Set.of(2, 3, 1), Set.of(1, 2, 3).toJava());
    }

    @Test
    public void testEquals() {
        assertNotEquals(Set.of(1, 2), null);
        assertEquals(Set.of(1), Set.of(1));
        assertNotEquals(Set.of(1), List.of(1));
        assertEquals(Set.of(1, 2), Set.of(2, 1));
        assertNotEquals(Set.of(1), Set.of(2));
        assertNotEquals(Set.of(1), Set.of(2, 1));
    }

    @Test
    public void testHashCode() {
        assertEquals("A".hashCode() + "B".hashCode(), Set.of("A", "B").hashCode());
        assertEquals(0, Set.empty().hashCode());
    }

    @Test
    public void testIterator() {
        assertIterableEquals(List.of(3, 1, 2, 4), Set.of(3, 1, 2, 4));
    }
}
