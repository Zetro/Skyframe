package algo;

import java.util.Arrays;

public class Point {
	final double[] components;

	public Point(double... components) {
		this.components = components;
	}

	public double get(int idx) {
		return components[idx];
	}

	public String toString() {
		return Arrays.toString(components);
	}
}
