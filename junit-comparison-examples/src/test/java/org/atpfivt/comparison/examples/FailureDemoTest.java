package org.atpfivt.comparison.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;

import org.atpfivt.comparison.CompareImplementations;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Tag("failure-demo")
@CompareImplementations(contract = SearchTree.class, value = { TreeSetSearchTree.class, BrokenSearchTree.class })
class FailureDemoTest {

	@ParameterizedTest
	@MethodSource("keySequences")
	void keepsKeysSortedAndDistinct(List<Integer> keys, SearchTree tree) {
		keys.forEach(tree::add);
		assertEquals(keys.stream().distinct().sorted().toList(), tree.toSortedList());
	}

	static Stream<List<Integer>> keySequences() {
		return Stream.of(List.of(1, 2, 3), List.of(3, 1, 2), List.of(2, 2, 1));
	}

}
