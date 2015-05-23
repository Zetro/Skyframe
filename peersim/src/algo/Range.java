package algo;

public class Range {
	
	public final double low;
	public final double high;

	public Range(double low, double high) {
		this.low = low;
		this.high = high;
	}

	public boolean isValid() {
		return low <= high;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 59 * hash + (int) (Double.doubleToLongBits(this.low) ^ (Double.doubleToLongBits(this.low) >>> 32));
		hash = 59 * hash + (int) (Double.doubleToLongBits(this.high) ^ (Double.doubleToLongBits(this.high) >>> 32));
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
		final Range other = (Range) obj;
		return true;
	}

	@Override
	public String toString() {
		return "["+low+","+high+")";
	}
}
