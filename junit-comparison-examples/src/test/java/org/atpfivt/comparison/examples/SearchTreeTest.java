package org.atpfivt.comparison.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.atpfivt.comparison.CompareImplementations;
import org.atpfivt.comparison.DisabledForImplementation;
import org.atpfivt.comparison.ImplementationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@CompareImplementations(contract = SearchTree.class,
		value = { BinarySearchTree.class, SortedArraySearchTree.class, TreeSetSearchTree.class })
class SearchTreeTest {

	@ImplementationTest
	void isEmptyInitially(SearchTree tree) {
		assertEquals(0, tree.size());
		assertFalse(tree.contains(0));
	}

	@ParameterizedTest
	@ValueSource(ints = { -7, 0, 42 })
	void containsAddedKey(int key, SearchTree tree) {
		assertTrue(tree.add(key));
		assertTrue(tree.contains(key));
	}

	@ParameterizedTest
	@MethodSource("keySequences")
	void keepsKeysSortedAndDistinct(List<Integer> keys, SearchTree tree) {
		keys.forEach(tree::add);
		assertEquals(keys.stream().distinct().sorted().toList(), tree.toSortedList());
	}

	static Stream<List<Integer>> keySequences() {
		return Stream.of(List.of(5, 3, 8, 1), List.of(1, 2, 3, 4), List.of(2, 2, 1));
	}

	@RepeatedTest(2)
	void matchesTreeSetOnRandomKeys(RepetitionInfo repetition, SearchTree tree) {
		Random random = new Random(repetition.getCurrentRepetition());
		TreeSet<Integer> expected = new TreeSet<>();
		for (int i = 0; i < 100; i++) {
			int key = random.nextInt(50);
			assertEquals(expected.add(key), tree.add(key));
		}
		assertEquals(List.copyOf(expected), tree.toSortedList());
	}

	@ImplementationTest
	@DisabledForImplementation(value = BinarySearchTree.class, reason = "unbalanced: degenerates into a list")
	void handlesLargeSortedInput(SearchTree tree) {
		IntStream.range(0, 100_000).forEach(tree::add);
		assertEquals(100_000, tree.size());
	}

	@Test
	void plainTestRunsOnce() {
		assertTrue(SearchTree.class.isInterface());
	}

	@Nested
	class Prefilled {

		@BeforeEach
		void addKeys(SearchTree tree) {
			List.of(4, 2, 6).forEach(tree::add);
		}

		@ParameterizedTest
		@ValueSource(ints = { 2, 4, 6 })
		void rejectsDuplicate(int key, SearchTree tree) {
			assertFalse(tree.add(key));
			assertEquals(3, tree.size());
		}
	}

}
