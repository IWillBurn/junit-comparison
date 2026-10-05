package org.atpfivt.comparison.examples;

import java.util.List;

public interface SearchTree {

	boolean add(int key);

	boolean contains(int key);

	int size();

	List<Integer> toSortedList();

}
