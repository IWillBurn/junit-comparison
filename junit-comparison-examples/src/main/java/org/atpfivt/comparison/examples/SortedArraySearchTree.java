package org.atpfivt.comparison.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SortedArraySearchTree implements SearchTree {

	private final List<Integer> keys = new ArrayList<>();

	@Override
	public boolean add(int key) {
		int index = Collections.binarySearch(this.keys, key);
		if (index >= 0) {
			return false;
		}
		this.keys.add(-index - 1, key);
		return true;
	}

	@Override
	public boolean contains(int key) {
		return Collections.binarySearch(this.keys, key) >= 0;
	}

	@Override
	public int size() {
		return this.keys.size();
	}

	@Override
	public List<Integer> toSortedList() {
		return List.copyOf(this.keys);
	}

}
