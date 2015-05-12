package ch.obermuhlner.infinitespace.tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class Counter<T> {

	private Map<T, Integer> mapElementToCount = new HashMap<T, Integer>();
	
	public void add(T element) {
		Integer count = mapElementToCount.get(element);
		if (count == null) {
			count = 0;
		}
		count = count + 1;
		mapElementToCount.put(element, count);		
	}
	
	public int getCount(T element) {
		Integer count = mapElementToCount.get(element);
		if (count == null) {
			return 0;
		}
		return count;
	}
	
	public Set<T> getElements() {
		return mapElementToCount.keySet();
	}
	
	public Set<Entry<T, Integer>> getEntries() {
		return mapElementToCount.entrySet();
	}
	
	public Map<Integer, List<T>> getMapCountToElements() {
		Map<Integer, List<T>> mapCountToElements = new HashMap<Integer, List<T>>();
		
		for (Entry<T, Integer> entry : getEntries()) {
			T element = entry.getKey();
			Integer count = entry.getValue();
			
			List<T> elements = mapCountToElements.get(count);
			if (elements == null) {
				elements = new ArrayList<T>();
				mapCountToElements.put(count, elements);
			}
			elements.add(element);
		}
		
		return mapCountToElements;
	}
	
	public void printTop(int top) {
		Map<Integer, List<T>> mapCountToElements = getMapCountToElements();
		List<Integer> counts = new ArrayList<Integer>(mapCountToElements.keySet());
		Collections.sort(counts);
		Collections.reverse(counts);
		
		for (int i = 0; i < Math.min(top, counts.size()); i++) {
			Integer count = counts.get(i);
			System.out.println(count + " : " + mapCountToElements.get(count));
		}
	}
	
	@Override
	public String toString () {
		return mapElementToCount.toString();
	}
}
