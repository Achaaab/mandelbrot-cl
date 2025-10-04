package com.github.achaaab.mandelbrot;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import static java.lang.Math.fma;
import static java.util.stream.IntStream.range;

/**
 * Mandelbrot fractal computed with CPU.
 *
 * @author Jonathan Guéhenneux
 * @since 0.0.0
 */
public class MandelbrotFractalCpu extends MandelbrotFractal {

	private int imageWidth;
	private int imageHeight;
	private double minX;
	private double minY;
	private double scaleX;
	private double scaleY;
	private int[] data;

	/**
	 * Creates a new Mandelbrot fractal.
	 *
	 * @param minX minimum x
	 * @param maxX maximum x
	 * @param minY minimum y
	 * @param maxY maximum y
	 * @param maxIterations maximum iterations for each pixel
	 * @since 0.0.0
	 */
	public MandelbrotFractalCpu(double minX, double maxX, double minY, double maxY, int maxIterations) {
		super(minX, maxX, minY, maxY, maxIterations);
	}

	@Override
	public void compute(BufferedImage image) {

		var dataBuffer = (DataBufferInt) image.getRaster().getDataBuffer();

		data = dataBuffer.getData();
		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		scaleX = getWidth() / imageWidth;
		scaleY = getHeight() / imageHeight;
		minX = getMinX();
		minY = getMinY();

		range(0, imageHeight).parallel().forEach(this::computeRow);
	}

	/**
	 * Computes every pixel in the identified row.
	 *
	 * @param j row to compute
	 * @since 0.0.0
	 */
	private void computeRow(int j) {

		var pixelIndex = (imageHeight - j - 1) * imageWidth;

		for (var i = 0; i < imageWidth; i++) {

			var x0 = minX + i * scaleX;
			var y0 = minY + j * scaleY;

			var x = 0.0;
			var y = 0.0;
			var xx = 0.0;
			var yy = 0.0;

			var iteration = 0;

			while (iteration < maxIterations && xx + yy < 4) {

				// if fma is not intrinsic, replace with standard multiply and add
				// y = 2 * x * y + y0;
				y = fma(x + x, y, y0);
				x = xx - yy + x0;

				xx = x * x;
				yy = y * y;

				iteration++;
			}

			if (iteration == maxIterations) {
				data[pixelIndex++] = 0;
			} else {
				data[pixelIndex++] = palette[iteration % palette.length];
			}
		}
	}
}
