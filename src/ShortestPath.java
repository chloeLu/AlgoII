public class ShortestPath {
	private double minDist;
	int[] indices ;

	public ShortestPath(int[] indices, double minDist) {
		this.indices = indices;
		this.minDist = minDist;
	}

	public int[] getIndices() {
		return indices;
	}

	public double getMinDist() {
		return minDist;
	}
}