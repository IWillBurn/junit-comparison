package org.atpfivt.comparison.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.InvocationComposition;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class RepeatedParameterizedTest {

	@RepeatedTest(2)
	@ParameterizedTest
	@ValueSource(ints = { 3, 1, 2 })
	@InvocationComposition(levels = ParameterizedTest.class)
	void allArgumentsWithinEachRepetition(int key, RepetitionInfo repetition) {
		SearchTree tree = new BinarySearchTree();
		assertTrue(tree.add(key));
		assertEquals(2, repetition.getTotalRepetitions());
	}

}
