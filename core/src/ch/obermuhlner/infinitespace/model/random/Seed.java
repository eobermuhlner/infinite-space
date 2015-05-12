package ch.obermuhlner.infinitespace.model.random;

import java.util.Arrays;

public class Seed {

	private final long[] seeds;
	
	public Seed(long... seeds) {
		if (seeds.length == 0) {
			throw new IllegalArgumentException("Empty seed is not allowed");
		}
		this.seeds = seeds;
	}
	
	public Seed(Seed parentSeed, long... childSeeds) {
		this(mergeSeeds(parentSeed.seeds, childSeeds));
	}
	
	private static long[] mergeSeeds(long[] parentSeeds, long... childSeeds) {
		long[] seeds = new long[parentSeeds.length + childSeeds.length];
		System.arraycopy(parentSeeds, 0, seeds, 0, parentSeeds.length);
		System.arraycopy(childSeeds, 0, seeds, parentSeeds.length, childSeeds.length);
		
		return seeds;
	}

	public Random getRandom() {
		return new Random(randomize(seeds));
	}
	
	private static long[] randomize (long[] seeds) {
		Random random = new Random(seeds);
		long[] result = new long[seeds.length];
		long accu = 0;
		for (int i = 0; i < seeds.length; i++) {
			accu += random.nextLong();
			result[i] += accu;
		}
		for (int i = 0; i < seeds.length; i++) {
			accu += random.nextLong();
			result[i] += accu;
		}
		
		return result;
	}
	
	public long getLastSeed() {
		return seeds[seeds.length - 1];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(seeds);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Seed other = (Seed) obj;
		if (!Arrays.equals(seeds, other.seeds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return Arrays.toString(seeds);
	}
}
