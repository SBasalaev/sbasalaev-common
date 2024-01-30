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

import java.util.Comparator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 * Tests of Traversable methods.
 *
 * @author Sergey Basalaev
 */
public class TraversableTest {

    public TraversableTest() { }

    @Test
    public void testSortedBy() {
        var list = List.of("B", "C", "A");
        assertEquals(List.of("A", "B", "C"), list.sortedBy(Comparator.naturalOrder()));
    }

    @Test
    public void testGroupedBy() {
        var list = List.of("AA", "B", "C", "DDD", "EE", "DDD");
        assertEquals(
            SetMultimap.<Integer, String>build()
                .add(1, "B").add(1, "C")
                .add(2, "AA").add(2, "EE")
                .add(3, "DDD")
                .toSetMultimap(),
            list.groupedIntoSets(String::length)
        );
    }

    @Test
    public void testCountByPredicate() {
        var list = List.of("AA", "B", "C", "DDD", "EE", "DDD");
        assertEquals(2, list.count(s -> s.length() == 2));
    }
}
