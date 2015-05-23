package algo;

import java.util.Arrays;
import java.util.List;

public class Query {

	public enum Component {
		Min, Max
	}

	public Component[] dims;

	public Query(Component... dims) {
		this.dims = dims;
	}

	public boolean isSkyLinePoint(Point point, List<Point> points) {
		for (Point p : points) {
			if (point != p && dominates(p, point)) {
				return false;
			}
		}
		return true;
	}

	public boolean dominates(Point p1, Point p2) {
		for (int i=0; i<dims.length; i++) {
			if (dims[i] == Component.Min && p1.get(i) > p2.get(i)) {
				return false;
			}
			if (dims[i] == Component.Max && p1.get(i) < p2.get(i)) {
				return false;
			}
		}
		return true;
	}

	public String toString() {
		return Arrays.toString(dims);
	}
}
