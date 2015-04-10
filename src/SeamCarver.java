import java.awt.Color;
import java.util.LinkedList;

public class SeamCarver {
	private Picture picture;
	private static int RGB_MAX = 0xFF;
	private double[][] energyArray;
	private double[][] energyArrayT;

	// create a seam carver object based on the given picture
	public SeamCarver(Picture picture) {
		this.picture = picture;
		energyArray = new double[height()][width()];
		energyArrayT = new double[width()][height()];

		for (int h = 0; h < height(); h++) {
			for (int w = 0; w < width(); w++) {
				double energy = energy(w, h);
				energyArray[h][w] = energy;
				energyArrayT[w][h] = energy;
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
			return deltaX(x, y) + deltaY(x, y);
		}
	}

	private double deltaX(int x, int y) {
		Color color1 = picture.get(x - 1, y);
		Color color2 = picture.get(x + 1, y);
		return Math.pow(color1.getRed() - color2.getRed(), 2) + Math.pow(color1.getBlue() - color2.getBlue(), 2)
				+ Math.pow(color1.getGreen() - color2.getGreen(), 2);
	}

	private double deltaY(int x, int y) {
		Color color1 = picture.get(x, y - 1);
		Color color2 = picture.get(x, y + 1);
		return Math.pow(color1.getRed() - color2.getRed(), 2) + Math.pow(color1.getBlue() - color2.getBlue(), 2)
				+ Math.pow(color1.getGreen() - color2.getGreen(), 2);
	}

	// sequence of indices for horizontal seam
	public int[] findHorizontalSeam() {
		Stack<Integer> stack = null;
		double minDist = Double.MAX_VALUE;
		for (int row = 1; row < height() - 1; row++) {
			ShortestPath sp = findVerticalSP(row, energyArrayT, width(), height());
			if (sp.getMinDist() < minDist) {
				minDist = sp.getMinDist();
				stack = sp.getIndices();
			}
		}
		int[] result = new int[width()];
		if (stack != null) {
			for (int i = 0; i < result.length; i++) {
				result[i] = stack.pop();
			}
		}
		return result;
	}

	// sequence of indices for vertical seam
	public int[] findVerticalSeam() {
		Stack<Integer> stack = null;
		double minDist = Double.MAX_VALUE;
		for (int col = 1; col < width() - 1; col++) {
			ShortestPath sp = findVerticalSP(col, energyArray, height(), width());
			if (sp.getMinDist() < minDist) {
				minDist = sp.getMinDist();
				stack = sp.getIndices();
			}
		}
		int[] result = new int[height()];
		if (stack != null) {
			for (int i = 0; i < result.length; i++) {
				result[i] = stack.pop();
			}
		}
		return result;
	}

	// arg is row number
	private ShortestPath findVerticalSP(int col, double[][] energyArr, int height, int width) {
		double[][] distTo = new double[height][width];
		int[][] lastEdge = new int[height][width];
		// init marked, lastEdge and distTo
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				distTo[r][c] = Double.MAX_VALUE;
				lastEdge[r][c] = Integer.MAX_VALUE;
			}
		}
		int row = 0;
		distTo[0][col] = 0;
		LinkedList<Integer> vOnPrevLayer = new LinkedList<Integer>();
		vOnPrevLayer.add(col);
		Queue<int[]> eToCurrLayer = new Queue<int[]>();
		updateEdgesToNextLayer(vOnPrevLayer, eToCurrLayer, width);
		while (++row != height) {
			// relax edges from previous layers that point to current layer
			while (!eToCurrLayer.isEmpty()) {
				int[] edge = eToCurrLayer.dequeue();
				double newDist = distTo[row - 1][edge[0]] + energyArr[row][edge[1]];
				if (newDist < distTo[row][edge[1]]) {
					distTo[row][edge[1]] = newDist;
					lastEdge[row][edge[1]] = edge[0];
				}
			}

			updateCurrLayerNodes(vOnPrevLayer, width);
			updateEdgesToNextLayer(vOnPrevLayer, eToCurrLayer, width);
		}

		// construct shortestPath
		int selectedC = Integer.MAX_VALUE;
		double shortestDist = Double.MAX_VALUE;
		for (int c = 0; c < width; c++) {
			if (Double.compare(distTo[height - 1][c], shortestDist) < 1) {
				selectedC = c;
				shortestDist = distTo[height - 1][c];
			}
		}
		int prevCol = selectedC;
		Stack<Integer> path = new Stack<Integer>();
		for (int h = height - 1; h >= 0; h--) {
			path.push(prevCol);
			prevCol = lastEdge[h][prevCol];
		}
		return new ShortestPath(path, shortestDist);
	}

	private void updateCurrLayerNodes(LinkedList<Integer> currLayer, int wid) {
		int first, last;
		if ((first = currLayer.getFirst()) > 0) {
			currLayer.addFirst(first - 1);
		}
		if ((last = currLayer.getLast()) < wid - 1) {
			currLayer.addLast(last + 1);
		}
	}

	private void updateEdgesToNextLayer(LinkedList<Integer> currLayer, Queue<int[]> layerQueue, int wid) {
		for (Integer i : currLayer) {
			layerQueue.enqueue(new int[] { i, i });
			if (i > 0) {
				layerQueue.enqueue(new int[] { i, i - 1 });
			}
			if (i < wid - 1) {
				layerQueue.enqueue(new int[] { i, i + 1 });
			}
		}
	}

	// remove horizontal seam from current picture
	public void removeHorizontalSeam(int[] seam) {
		validateRemovalArg(seam, false);
		Picture newPic = new Picture(width(), height() - 1);
		int oldRow;
		for (int col = 0; col < newPic.width(); col++) {
			oldRow = 0;
			for (int row = 0; row < newPic.height(); row++) {
				if (seam[col] == row) {
					oldRow++;
				}
				newPic.set(col, row, picture.get(col, oldRow));
				oldRow++;
			}
		}
		this.picture = newPic;
	}

	// remove vertical seam from current picture
	public void removeVerticalSeam(int[] seam) {
		validateRemovalArg(seam, true);
		Picture newPic = new Picture(width() - 1, height());
		int oldCol;
		for (int row = 0; row < newPic.height(); row++) {
			oldCol = 0;
			for (int col = 0; col < newPic.width(); col++) {
				if (seam[row] == col) {
					oldCol++;
				}
				newPic.set(col, row, picture.get(oldCol, row));
				oldCol++;
			}
		}
		this.picture = newPic;
	}

	private void validateRemovalArg(int[] seam, boolean isVertical) {
		if (seam == null) {
			throw new NullPointerException("arg cannot be null");
		}
		if (isVertical) {
			if (seam.length != width() - 1 || width() <= 1) {
				throw new IllegalArgumentException("arg array has invalid size");
			}
		} else {
			if (seam.length != height() - 1 || height() <= 1) {
				throw new IllegalArgumentException("arg array has invalid size");
			}
		}
	}
}

class ShortestPath {
	Stack<Integer> indices;
	double minDist;

	public ShortestPath(Stack<Integer> indices, double minDist) {
		this.indices = indices;
		this.minDist = minDist;
	}

	public Stack<Integer> getIndices() {
		return indices;
	}

	public double getMinDist() {
		return minDist;
	}
}