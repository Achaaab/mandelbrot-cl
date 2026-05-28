package com.github.achaaab.mandelbrot;

import com.github.achaaab.mandelbrot.fractal.Fractal;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static java.lang.Math.max;
import static java.lang.Math.round;
import static java.lang.Math.toIntExact;

/**
 * @author Jonathan Guéhenneux
 * @since 0.0.0
 */
public abstract class MandelbrotFractal extends Fractal {

	protected static final Color[] BASE_PALETTE_COLORS = {
			new Color(0, 0, 96),
			new Color(128, 192, 255),
			new Color(255, 255, 255),
			new Color(255, 192, 0),
			new Color(255, 96, 0) };

	/**
	 * @param interpolations
	 * @return
	 * @since 0.0.0
	 */
	protected static int[] createPalette(int interpolations) {
		return createPalette(interpolations, BASE_PALETTE_COLORS);
	}

	/**
	 * @param interpolations
	 * @param colors
	 * @return
	 * @since 0.0.0
	 */
	private static int[] createPalette(int interpolations, Color... colors) {

		var colorCount = colors.length;
		var palette = new int[interpolations * colorCount];

		var paletteIndex = 0;

		for (var colorIndex = 0; colorIndex < colorCount; colorIndex++) {

			var color0 = colors[colorIndex];
			var red0 = color0.getRed();
			var green0 = color0.getGreen();
			var blue0 = color0.getBlue();

			var color1 = colors[(colorIndex + 1) % colorCount];
			var red1 = color1.getRed();
			var green1 = color1.getGreen();
			var blue1 = color1.getBlue();

			var deltaRed = red1 - red0;
			var deltaGreen = green1 - green0;
			var deltaBlue = blue1 - blue0;

			for (var interpolation = 0; interpolation < interpolations; interpolation++) {

				var coefficient = (double) interpolation / (interpolations - 1);

				var red = toIntExact(round(red0 + coefficient * deltaRed));
				var green = toIntExact(round(green0 + coefficient * deltaGreen));
				var blue = toIntExact(round(blue0 + coefficient * deltaBlue));

				var rgb = red << 16 | green << 8 | blue;
				palette[paletteIndex++] = rgb;
			}
		}

		return palette;
	}

	protected int[] palette;
	protected int iterations;

	/**
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 * @param iterations
	 */
	public MandelbrotFractal(double minX, double maxX, double minY, double maxY, int iterations) {

		super(minX, maxX, minY, maxY);

		this.iterations = iterations;

		palette = createPalette(128);
	}

	/**
	 * @param delta
	 * @since 0.0.0
	 */
	public void adjustIterations(int delta) {
		iterations = max(iterations + delta, 2);
	}

	/**
	 * @return maximum number of iterations before giving up and considering the point is inside the set
	 * @since 0.0.1
	 */
	public int getIterations() {
		return iterations;
	}

	/**
	 * @param image
	 * @since 0.0.0
	 */
	public abstract void compute(BufferedImage image);
}
