package algo;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SearchRegion {

	public Region[] regions;

	public SearchRegion(Region... regions) {
		this.regions = regions;
	}

	public SearchRegion union(SearchRegion sr2) {
		List<Region> regions = new ArrayList<>();
		for (Region region : this.regions)
			regions.add(region);
		for (Region region : sr2.regions)
			regions.add(region);
		return new SearchRegion(regions.toArray(new Region[regions.size()]));
	}
	
	public SearchRegion union(Region r2) {
		boolean add = true;
		List<Region> regions = new ArrayList<>();
		for (Region r: this.regions) {
			regions.add(r);
			if (r.contains(r2)) { add = false; }
		}
		if (add)
			regions.add(r2);
		return new SearchRegion(regions.toArray(new Region[regions.size()]));
	}

	public SearchRegion intersect(Region r) {
		List<Region> regions = new ArrayList<>();
		for (Region search_region : this.regions) {
			Region inter = search_region.intersect(r);
			if (inter != null)
				regions.add(inter);
		}
		return new SearchRegion(regions.toArray(new Region[regions.size()]));
	}

	public SearchRegion subtract(Region r) {
		HashSet<Region> regions = new HashSet<>();
		for (Region search_region : this.regions) {
			if (search_region.hasIntersection(r)) {
				Region[] sub = search_region.subtract(r);
				for (Region region : sub) {
						regions.add(region);
				}
			} else {
				regions.add(search_region);
			}
		}
		return new SearchRegion(regions.toArray(new Region[regions.size()]));
	}

	public boolean isCoveredBy(Region r) {
		for (Region search_region : regions) {
			if (!r.covers(search_region)) {
				return false;
			}
		}
		return true;
	}

	public String toString() {
		return Arrays.toString(regions);
	}
}
