package com.github.achaaab.mandelbrot;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import static java.util.stream.IntStream.range;
import static jdk.incubator.vector.DoubleVector.broadcast;
import static jdk.incubator.vector.DoubleVector.fromArray;
import static jdk.incubator.vector.DoubleVector.zero;
import static jdk.incubator.vector.VectorOperators.LE;

/**
 * Mandelbrot fractal computed with SIMD capable CPU.
 *
 * @author Jonathan Guéhenneux
 * @since 0.0.0
 */
public class MandelbrotFractalSimd extends MandelbrotFractal {

	private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
	private static final int LANE_COUNT = SPECIES.length();

	private int imageWidth;
	private int imageHeight;
	private double minX;
	private double minY;
	private double scaleX;
	private double scaleY;

	private int[] colorBuffer;

	private DoubleVector offset;

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
	public MandelbrotFractalSimd(double minX, double maxX, double minY, double maxY, int maxIterations) {
		super(minX, maxX, minY, maxY, maxIterations);
	}

	@Override
	public void compute(BufferedImage image) {

		var dataBuffer = (DataBufferInt) image.getRaster().getDataBuffer();

		colorBuffer = dataBuffer.getData();

		imageWidth = image.getWidth();
		imageHeight = image.getHeight();
		scaleX = getWidth() / imageWidth;
		scaleY = getHeight() / imageHeight;
		minX = getMinX();
		minY = getMinY();

		offset = fromArray(
				SPECIES,
				range(0, LANE_COUNT).mapToDouble(lane -> lane * scaleX).toArray(),
				0);

		range(0, imageHeight).parallel().forEach(this::computeRow);
	}

	/**
	 * Computes every pixel in the identified row.
	 *
	 * @param j row to compute
	 * @since 0.0.0
	 */
	private void computeRow(int j) {

		var y0 = broadcast(SPECIES, minY + j * scaleY);
		var iterationArray = new double[LANE_COUNT];

		var colorIndex = (imageHeight - j - 1) * imageWidth;

		for (var i = 0; i < imageWidth; i += LANE_COUNT) {

			var x = zero(SPECIES);
			var y = zero(SPECIES);
			var iteration = zero(SPECIES);
			var x0 = broadcast(SPECIES, minX + i * scaleX).add(offset);

			for (var n = 0; n < maxIterations; n++) {

				var xx = x.mul(x);
				var yy = y.mul(y);

				var active = xx.add(yy).compare(LE, 4.0);

				if (!active.anyTrue()) {
					break;
				}

				iteration = iteration.add(1, active);

				y = y.fma(x.add(x), y0);
				x = xx.sub(yy).add(x0);
			}

			iteration.intoArray(iterationArray, 0);

			for (var lane = 0; lane < LANE_COUNT; lane++) {

				if (iterationArray[lane] == maxIterations) {
					colorBuffer[colorIndex++] = 0x000000;
				} else {
					colorBuffer[colorIndex++] = palette[(int) iterationArray[lane] % palette.length];
				}
			}
		}
	}
}
