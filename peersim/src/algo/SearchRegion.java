package algo;

import java.util.ArrayList;
import java.util.List;

public class SearchRegion {

	Region[] regions;

	public SearchRegion(Region... regions) {
		this.regions = regions;
	}

	public SearchRegion union(SearchRegion sr2) {
		List<Region> regions = new ArrayList<>();
		for (Region region : regions)
			regions.add(region);
		for (Region region : sr2.regions)
			regions.add(region);
		return new SearchRegion(regions.toArray(new Region[regions.size()]));
	}

	public SearchRegion intersect(Region r) {
		List<Region> regions = new ArrayList<>();
		for (Region search_region : regions) {
			Region inter = search_region.intersect(r);
			if (inter != null)
				regions.add(inter);
		}
		return new SearchRegion(regions.toArray(new Region[regions.size()]));
	}

	public SearchRegion subtract(Region r) {
		List<Region> regions = new ArrayList<>();
		for (Region search_region : regions) {
			Region inter = search_region.intersect(r);
			if (inter != null) {
				Region[] sub = search_region.subtract(r);
				for (Region region : sub) {
					if (region != null) {
						regions.add(inter);
					}
				}
			} else if (!regions.contains(search_region)) {
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
}
