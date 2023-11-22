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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for mutable set multimap.
 *
 * @author Sergey Basalaev
 */
public class MutableSetMultimapTest {

    public MutableSetMultimapTest() { }

    private MutableSetMultimap<String,String> map;

    @BeforeEach
    public void initMultimap() {
        map = MutableSetMultimap.<String,String>empty();
        map.add("key1", "AA");
        map.add("key2", "BB");
        map.add("key1", "AB");
    }

    @Test
    public void testGet() {
        assertEquals(Set.of("AA", "AB"), map.get("key1"));
        assertEquals(Set.of("BB"), map.get("key2"));
        assertEquals(Set.empty(), map.get("key3"));
    }

    @Test
    public void testAdd() {
        var multimap = MutableSetMultimap.<String,String>empty();
        assertEquals(0, multimap.size());
        assertEquals(0, multimap.keySize());
        assertEquals(true, multimap.add("key1", "AA"));
        assertEquals(1, multimap.size());
        assertEquals(1, multimap.keySize());
        assertEquals(true, multimap.add("key2", "BB"));
        assertEquals(2, multimap.size());
        assertEquals(2, multimap.keySize());
        assertEquals(true, multimap.add("key1", "AB"));
        assertEquals(3, multimap.size());
        assertEquals(2, multimap.keySize());
        assertEquals(false, multimap.add("key1", "AA"));
        assertEquals(3, multimap.size());
        assertEquals(2, multimap.keySize());
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

    @Test
    public void testRemoveKey() {
        assertEquals(Set.empty(), map.removeKey("key3"));
        assertEquals(3, map.size());
        assertEquals(2, map.keySize());
        assertEquals(Set.of("BB"), map.removeKey("key2"));
        assertEquals(true, map.containsKey("key1"));
        assertEquals(false, map.containsKey("key2"));
        assertEquals(2, map.size());
        assertEquals(1, map.keySize());
        assertEquals(Set.of("AA", "AB"), map.removeKey("key1"));
        assertEquals(false, map.containsKey("key1"));
        assertEquals(false, map.containsKey("key2"));
        assertEquals(0, map.size());
        assertEquals(0, map.keySize());
    }

    @Test
    public void testRemoveEntry() {
        assertEquals(false, map.removeEntry("key1", "BB"));
        assertEquals(3, map.size());
        assertEquals(2, map.keySize());
        assertEquals(true, map.removeEntry("key1", "AA"));
        assertEquals(2, map.size());
        assertEquals(2, map.keySize());
        assertEquals(true, map.removeEntry("key1", "AB"));
        assertEquals(1, map.size());
        assertEquals(1, map.keySize());
    }

    @Test
    public void testClear() {
        map.clear();
        assertEquals(0, map.keySize());
        assertEquals(0, map.size());
        assertEquals(Set.empty(), map.collectionEntries());
    }
}
