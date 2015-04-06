import java.awt.Color;

public class SeamCarver {
	private Picture picture;
	private static int RGB_MAX = 0xFF;

	// create a seam carver object based on the given picture
	public SeamCarver(Picture picture) {
		this.picture = picture;
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
			return (sumSquare(x + 1, y) - sumSquare(x - 1, y)) 
					+ (sumSquare(x, y+1) - sumSquare(x, y-1));
		}
	}

	private double sumSquare(int x, int y) {
		Color color = picture.get(x, y);
		return Math.pow(color.getRed(), 2) + Math.pow(color.getGreen(), 2) + Math.pow(color.getBlue(), 2);
	}

	// sequence of indices for horizontal seam
	public int[] findHorizontalSeam() {

	}

	// sequence of indices for vertical seam
	public int[] findVerticalSeam() {

	}

	// remove horizontal seam from current picture
	public void removeHorizontalSeam(int[] seam) {

	}

	// remove vertical seam from current picture
	public void removeVerticalSeam(int[] seam) {

	}
}