import java.awt.Color;

public class SeamCarver {
	private Picture picture;
	private double[][] energyArray;

	// create a seam carver object based on the given picture
	public SeamCarver(Picture picture) {
		reset(picture);
	}

	private void reset(Picture picture) {
		this.picture = picture;
		energyArray = new double[height()][width()];

		for (int h = 0; h < height(); h++) {
			for (int w = 0; w < width(); w++) {
				double energy = energy(w, h);
				energyArray[h][w] = energy;
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
		if (x < 0 || x >= width() || y < 0 || y >= height()) {
			throw new IndexOutOfBoundsException();
		}
		// border pixels have 3*255^2
		if (x == 0 || x == picture.width() - 1 || y == 0 || y == picture.height() - 1) {
			return 3 * (0xff * 0xff);
		} else {
			return deltaX(x, y) + deltaY(x, y);
		}
	}

	private double deltaX(int x, int y) {
		Color color1 = picture.get(x - 1, y);
		Color color2 = picture.get(x + 1, y);
		return delta(color1, color2);
	}

	private double deltaY(int x, int y) {
		Color color1 = picture.get(x, y - 1);
		Color color2 = picture.get(x, y + 1);
		return delta(color1, color2);
	}

	private double delta(Color color1, Color color2) {
		int redDiff = color1.getRed() - color2.getRed();
		int blueDiff = color1.getBlue() - color2.getBlue();
		int greenDiff = color1.getGreen() - color2.getGreen();
		return redDiff * redDiff + blueDiff * blueDiff + greenDiff * greenDiff;
	}

	// sequence of indices for horizontal seam
	public int[] findHorizontalSeam() {
		return findVerticalSP(true);
	}

	// sequence of indices for vertical seam
	public int[] findVerticalSeam() {
		return findVerticalSP(false);
	}

	// arg is row number
	private int[] findVerticalSP(boolean transpose) {
		int height = height(), width = width();
		if (transpose) {
			height = width();
			width = height();
		}

		double[][] distTo = new double[height][width];
		int[][] lastEdge = new int[height][width];
		// init distTo. the 1st row has distTo=0
		for (int c = 0; c < width; c++) {
			distTo[0][c] = 0d;
		}
		for (int r = 1; r < height; r++) {
			for (int c = 0; c < width; c++) {
				distTo[r][c] = Double.MAX_VALUE;
			}
		}

		int row = 0;
		while (++row != height) {
			// edge: [0]:from [1]:to
			// relax edges from previous layers that point to current layer
			for (int v = 0; v < width; v++) {
				double newDistUL = v > 0 ? distTo[row - 1][v - 1] + getEnergy(row, v, transpose) : Double.MAX_VALUE;
				if (newDistUL < distTo[row][v]) {
					distTo[row][v] = newDistUL;
					lastEdge[row][v] = v-1;
				}
				double newDistU = distTo[row - 1][v] + getEnergy(row, v, transpose);
				if (newDistU < distTo[row][v]) {
					distTo[row][v] = newDistU;
					lastEdge[row][v] = v;
				}
				double newDistUR = v < width - 1 ? distTo[row - 1][v + 1] + getEnergy(row, v, transpose)
						: Double.MAX_VALUE;
				if (newDistUR < distTo[row][v]) {
					distTo[row][v] = newDistUR;
					lastEdge[row][v] = v+1;
				}
			}
		}

		// return seam
		int selectedC = Integer.MAX_VALUE;
		double shortestDist = Double.MAX_VALUE;
		for (int c = 0; c < width; c++) {
			if (Double.compare(distTo[height - 1][c], shortestDist) < 1) {
				selectedC = c;
				shortestDist = distTo[height - 1][c];
			}
		}
		int prevCol = selectedC;
		int[] path = new int[height];
		for (int h = height - 1; h >= 0; h--) {
			path[h] = prevCol;
			prevCol = lastEdge[h][prevCol];
		}
		return path;
	}
	
	private double getEnergy(int x, int y, boolean transpose) {
		return transpose ? energyArray[y][x] : energyArray[x][y];
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
		reset(newPic);
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
		reset(newPic);
	}

	private void validateRemovalArg(int[] seam, boolean isVertical) {
		if (seam == null) {
			throw new NullPointerException("arg cannot be null");
		}
		if (!isVertical) {
			if (seam.length != width() || width() <= 1) {
				throw new IllegalArgumentException("arg array has invalid size");
			}
		} else {
			if (seam.length != height() || height() <= 1) {
				throw new IllegalArgumentException("arg array has invalid size");
			}
		}
		for (int i = 0; i < seam.length - 1; i++) {
			if (Math.abs(seam[i] - seam[i + 1]) > 1) {
				throw new IllegalArgumentException("adjacent entries differ by more than 1");
			}
		}
	}
}
