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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Tests for immutable set multimaps.
 *
 * @author Sergey Basalaev
 */
public class SetMultimapTest {

    public SetMultimapTest() {
    }

    private static final SetMultimap<String, String> map = SetMultimap
            .<String,String>build()
            .add("key1", "AA")
            .add("key2", "BB")
            .add("key1", "AB")
            .add("key2", "BB")
            .toSetMultimap();

    @Test
    public void testEmpty() {
        assertSame(SetMultimap.empty(), SetMultimap.build().toSetMultimap());
    }

    @Test
    public void testGet() {
        assertEquals(Set.of("AA", "AB"), map.get("key1"));
        assertEquals(Set.of("BB"), map.get("key2"));
        assertEquals(Set.empty(), map.get("key3"));
    }

    @Test
    public void testEntries() {
        assertEquals(
            Set.of(Entry.of("key1", "AA"), Entry.of("key1", "AB"), Entry.of("key2", "BB")),
            map.entries()
        );
    }

    @Test
    public void testCollectionEntries() {
        assertEquals(
            Set.of(Entry.of("key1", Set.of("AA", "AB")), Entry.of("key2", Set.of("BB"))),
            map.collectionEntries()
        );
    }

    @Test
    public void testContainsEntry() {
        assertTrue(map.containsEntry(Entry.of("key1", "AB")));
        assertFalse(map.containsEntry(Entry.of("key1", "BB")));
    }
}
