package algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Region {

	final Range[] dims;

	public Region(Range... dims) {
		this.dims = dims;
	}

	public boolean isValid() {
		for (Range r : dims) {
			if (!r.isValid()) {
				return false;
			}
		}
		return true;
	}
	
	public boolean hasIntersection(Region r2) {
		for (int i=0; i<dims.length; i++) {
			if (Math.min(dims[i].high, r2.dims[i].high) < Math.max(dims[i].low, r2.dims[i].low)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean contains(Region r2) {
		for (int i=0; i<dims.length; i++) {
			if(dims[i].low > r2.dims[i].low || dims[i].high < r2.dims[i].high) {
				return false;
			}
		}
		return true;
	}

	public Region intersect(Region r2) {
		Range[] dims = new Range[this.dims.length];

		for (int i=0; i<dims.length; i++) {
			dims[i] = new Range(Math.max(this.dims[i].low, r2.dims[i].low),
				Math.min(this.dims[i].high, r2.dims[i].high));

			if (dims[i].low > dims[i].high) {
				return null;
			}
		}
		return new Region(dims);
	}

	private void addRegionIfValid(HashSet<Region> regions, Range... dims) {
		for (int i=0; i<dims.length; i++) {
			if (dims[i].low < this.dims[i].low ||
				dims[i].high > this.dims[i].high) {
				return;
			}
		}
		Region r = new Region(dims);
		if (r.isValid() && !regions.contains(r)) {
			regions.add(r);
		}
	}

	public Region[] subtract(Region r2) {
		HashSet<Region> regions = new HashSet<>();
		
		if (r2.contains(this)) {
			return new Region[0];
		}

		for (int j=0; j<dims.length; j++) {
			Range[] highdims = new Range[dims.length];
			Range[] lowdims = new Range[dims.length];
			boolean skip = false;
			boolean hashigh = false;
			boolean haslow = false;
			for (int i=0; i<dims.length; i++) {
				Range highdim, lowdim;
				highdim = new Range(dims[i].low, dims[i].high);
				lowdim = new Range(dims[i].low, dims[i].high);
				if (i == j) {
					if(dims[i].low < r2.dims[i].low) {
						lowdim = new Range(dims[i].low, r2.dims[i].low);
						haslow = true;
					}
					if (r2.dims[i].high < dims[i].high) {
						highdim = new Range(r2.dims[i].high, dims[i].high);
						hashigh = true;
					}
					if (! (hashigh || haslow)) {
						skip = true;
						break;
					}
				}
				lowdims[i] = lowdim;
				highdims[i] = highdim;
			}
			if (skip) { continue; }

			if (hashigh)
				regions.add(new Region(highdims));
			if (haslow)
				regions.add(new Region(lowdims));
		}
		return regions.toArray(new Region[regions.size()]);
	}

	public boolean covers(Region r2) {
		for (int i=0; i<dims.length; i++) {
			if (dims[i].low > r2.dims[i].low ||
					dims[i].high < r2.dims[i].high)
				return false;
		}
		return true;
	}

	private static Region expand(Region region, int idx) {
		double epsilon = 0.000001;
		Range[] dims = new Range[region.dims.length];
		for (int i=0; i<region.dims.length; i++) {
			Range dim = region.dims[i];
			if (i==idx) {
				dim = new Range(dim.low-epsilon, dim.high+epsilon);
			}
			dims[i] = dim;
		}
		return new Region(dims);
	}

	public boolean borders(Region other) {
		boolean foundBorder = false;
		for (int i=0; i<dims.length; i++) {
			if(Math.min(dims[i].high, other.dims[i].high) == Math.max(dims[i].low, other.dims[i].low)){
				if (foundBorder) {
					return false;
				}
				foundBorder = true;
			}
			if(Math.min(dims[i].high, other.dims[i].high) < Math.max(dims[i].low, other.dims[i].low)) {
				return false;
			}
		}
		return foundBorder;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 13 * hash + Arrays.deepHashCode(this.dims);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Region other = (Region) obj;
		return Arrays.equals(dims, other.dims);
	}

	public String toString() {
		return Arrays.toString(dims);
	}
}
