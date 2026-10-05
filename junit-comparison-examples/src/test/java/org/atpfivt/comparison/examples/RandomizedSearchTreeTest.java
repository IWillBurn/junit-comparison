package org.atpfivt.comparison.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import org.atpfivt.comparison.CompareImplementations;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ParameterizedClass
@ValueSource(longs = { 1, 2 })
@CompareImplementations(contract = SearchTree.class,
		value = { BinarySearchTree.class, SortedArraySearchTree.class, TreeSetSearchTree.class })
class RandomizedSearchTreeTest {

	@Parameter
	long seed;

	@ParameterizedTest
	@ValueSource(ints = { 10, 1_000 })
	void matchesTreeSet(int count, SearchTree tree) {
		Random random = new Random(this.seed);
		TreeSet<Integer> expected = new TreeSet<>();
		for (int i = 0; i < count; i++) {
			int key = random.nextInt(count);
			assertEquals(expected.add(key), tree.add(key));
		}
		assertEquals(expected.size(), tree.size());
		assertEquals(List.copyOf(expected), tree.toSortedList());
	}

}
