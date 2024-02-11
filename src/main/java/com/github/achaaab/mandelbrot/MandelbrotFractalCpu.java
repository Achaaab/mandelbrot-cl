package com.github.achaaab.mandelbrot;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.Math.fma;
import static java.lang.Math.max;
import static java.lang.Runtime.getRuntime;
import static java.lang.Thread.currentThread;

public class MandelbrotFractalCpu extends MandelbrotFractal {

	private final int threadCount;

	private int imageWidth;
	private int imageHeight;
	private double minX;
	private double minY;
	private double scaleX;
	private double scaleY;
	private int[] data;
	private AtomicInteger columnProvider;

	/**
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 * @param maxIterations
	 */
	public MandelbrotFractalCpu(double minX, double maxX, double minY, double maxY, int maxIterations) {

		super(minX, maxX, minY, maxY, maxIterations);

		var availableProcessors = getRuntime().availableProcessors();
		threadCount = max(availableProcessors - 1, 1);
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
		columnProvider = new AtomicInteger(0);

		var threads = Stream.generate(this::columnComputingTask).
				map(Thread::new).limit(threadCount).
				toList();

		for (var thread : threads) {
			thread.start();
		}

		for (var thread : threads) {

			try {
				thread.join();
			} catch (InterruptedException interruptedException) {
				currentThread().interrupt();
			}
		}
	}

	private Runnable columnComputingTask() {

		return () -> {

			int i;

			while ((i = columnProvider.getAndIncrement()) < imageWidth) {
				computeColumn(i);
			}
		};
	}

	private void computeColumn(int i) {

		for (var j = 0; j < imageHeight; j++) {

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

			var pixelIndex = j * imageWidth + i;

			if (iteration == maxIterations) {
				data[pixelIndex] = 0;
			} else {
				data[pixelIndex] = palette[iteration % palette.length];
			}
		}
	}
}
