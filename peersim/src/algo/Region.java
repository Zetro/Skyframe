package algo;

import java.util.ArrayList;
import java.util.Arrays;
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

	private void addRegionIfValid(List<Region> regions, Range... dims) {
		for (int i=0; i<dims.length; i++) {
			if (dims[i].low < this.dims[i].low ||
				dims[i].high > this.dims[i].high) {
				return;
			}
		}
		Region r = new Region(dims);
		System.out.println(r + " " + r.isValid() + " " + regions.contains(r));
		System.out.println("O:"+regions);
		if (regions.size() == 1) {
			System.out.println(":"+regions.get(0).equals(r));
		}
		System.out.println();
		if (r.isValid() && !regions.contains(r)) {
			regions.add(r);
		}
	}

	public Region[] subtract(Region r2) {
		List<Region> regions = new ArrayList<>();

		for (int j=0; j<dims.length; j++) {
			Range[] dims_low = new Range[dims.length];
			Range[] dims_high = new Range[dims.length];

			for (int i=0; i<dims.length; i++) {
				Range dim_low, dim_high;
				if (i == j) {
					dim_low = new Range(dims[i].low, r2.dims[i].low);
					dim_high = new Range(r2.dims[i].high, dims[i].high);
				} else {
					dim_low = new Range(dims[i].low, dims[i].high);
					dim_high = new Range(dims[i].low, dims[i].high);
				}
				dims_low[i] = dim_low;
				dims_high[i] = dim_high;
			}

			addRegionIfValid(regions, dims_low);
			addRegionIfValid(regions, dims_high);
		}
		System.out.println("L:"+regions);
		System.out.println();

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
