package com.github.achaaab.mandelbrot;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static java.lang.Math.max;
import static java.lang.Math.round;
import static java.lang.Math.toIntExact;

public abstract class MandelbrotFractal extends Fractal {

	/**
	 * @param interpolations
	 * @param colors
	 * @return
	 */
	private static int[] createPalette(int interpolations, Color... colors) {

		var colorCount = colors.length;
		var palette = new int[interpolations * colorCount];

		var paletteIndex = 0;

		for (var colorIndex = 0; colorIndex < colorCount - 1; colorIndex++) {

			var color0 = colors[colorIndex];
			var red0 = color0.getRed();
			var green0 = color0.getGreen();
			var blue0 = color0.getBlue();

			var color1 = colors[colorIndex + 1];
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

	protected final int[] palette;

	protected int maxIterations;

	/**
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 * @param maxIterations
	 */
	public MandelbrotFractal(double minX, double maxX, double minY, double maxY, int maxIterations) {

		super(minX, maxX, minY, maxY);

		palette = createPalette(32,
				new Color(0, 0, 96),
				new Color(255, 255, 255),
				new Color(255, 200, 0),
				new Color(0, 0, 96));

		this.maxIterations = maxIterations;
	}

	public void sharpen(int amount) {
		maxIterations = max(maxIterations + amount, 2);
	}

	public abstract void compute(BufferedImage image);
}
