import java.awt.Color;
import java.util.LinkedList;

public class SeamCarver {
	private Picture picture;
	private static int RGB_MAX = 0xFF;
	private double[][] energyArray;

	// create a seam carver object based on the given picture
	public SeamCarver(Picture picture) {
		this.picture = picture;
		energyArray = new double[height()][width()];

		for (int h = 0; h < height(); h++) {
			for (int w = 0; w < width(); w++) {
				energyArray[h][w] = energy(w, h);
			}
		}
	}

	// current picture
	public Picture picture() {
		return picture;
	}

	// width of current picture
	public int width() {
		return picture.width();
	}

	// height of current picture
	public int height() {
		return picture.height();
	}

	// energy of pixel at column x and row y
	public double energy(int x, int y) {
		// border pixels have 3*255^2
		if (x == 0 || x == picture.width() - 1 || y == 0 || y == picture.height() - 1) {
			return 3 * Math.pow(RGB_MAX, 2);
		} else {
			return (sumSquare(x + 1, y) - sumSquare(x - 1, y)) + (sumSquare(x, y + 1) - sumSquare(x, y - 1));
		}
	}

	private double sumSquare(int x1, int x2, int y1, int y2) {
		Color color1 = picture.get(x1, y1);
		Color color2 = picture.get(x2, y2);
		return Math.pow(color1.getRed() - color2.getRed(), 2) + Math.pow(color1.getBlue() - color2.getBlue(), 2) + Math.pow(color1.getGreen() - color2.getGreen(), 2);
	}

	// sequence of indices for horizontal seam
	public int[] findHorizontalSeam() {
		int[] result = new int[height()];
		double minDist = Double.MAX_VALUE;
		for (int row = 1; row < height() - 1; row++) {
			ShortestPath sp = findHorizontalSP(row);
			if (sp.getMinDist() < minDist) {
				minDist = sp.getMinDist();
				result = sp.getIndices();
			}
		}
		return result;
	}

	// x is row number
	private ShortestPath findHorizontalSP(int x) {
		boolean[][] marked = new boolean[height()][width()];
		double[][] distTo = new double[height()][width()];
		int[][] lastEdge = new int[height()][width()];
		// init marked, lastEdge and distTo
		for (int r = 0; r < height(); r++) {
			for (int c = 0; c < height(); c++) {
				marked[r][c] = false;
				distTo[r][c] = Double.MAX_VALUE;
				lastEdge[r][c] = Integer.MAX_VALUE;
			}
		}
		int row = 0;
		distTo[row][x] = 0;
		LinkedList<Integer> currLayer = new LinkedList<Integer>();
		currLayer.add(x);
		LinkedList<int[]> edgesToNextLayer = new LinkedList<int[]>();
		int first, last;
		while (row != width()=1) {
			// relax edges from previous layers that point to current layer
			for (Integer i: nextLayerIndices){
				if (distTo)
			}
			
			// populate next layer indices
			if ((first = nextLayerIndices.getFirst()) > 0) {
				nextLayerIndices.addFirst(first - 1);
			}
			if ((last = nextLayerIndices.getLast()) < height()) {
				nextLayerIndices.addLast(last + 1);
			}
			
			row++;
		}
	}

	// sequence of indices for vertical seam
	public int[] findVerticalSeam() {
		int[] result = new int[height()];
		// double minDist = Double.MAX_VALUE;
		// for (int col = 1; col < width() - 1; col++) {
		// ShortestPath sp = findVerticalSP(col);
		// if (sp.getMinDist() < minDist) {
		// minDist = sp.getMinDist();
		// result = sp.getIndices();
		// }
		// }
		return result;
	}

	// remove horizontal seam from current picture
	public void removeHorizontalSeam(int[] seam) {

	}

	// remove vertical seam from current picture
	public void removeVerticalSeam(int[] seam) {

	}
}

class ShortestPath {
	int[] indices;
	double minDist;

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