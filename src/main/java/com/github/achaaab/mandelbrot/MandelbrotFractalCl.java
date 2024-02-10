package com.github.achaaab.mandelbrot;

import org.jocl.CL;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;

import java.awt.image.BufferedImage;

import static com.github.achaaab.mandelbrot.JoclHelper.createBuffer;
import static com.github.achaaab.mandelbrot.JoclHelper.createCommandQueue;
import static com.github.achaaab.mandelbrot.JoclHelper.createContext;
import static com.github.achaaab.mandelbrot.JoclHelper.createKernel;
import static com.github.achaaab.mandelbrot.JoclHelper.createOutputBuffer;
import static com.github.achaaab.mandelbrot.JoclHelper.enqueue;
import static com.github.achaaab.mandelbrot.JoclHelper.getDevices;
import static com.github.achaaab.mandelbrot.JoclHelper.getPlatforms;
import static com.github.achaaab.mandelbrot.JoclHelper.loadImage;
import static com.github.achaaab.mandelbrot.JoclHelper.setKernelArgument;

public class MandelbrotFractalCl extends MandelbrotFractal {

	private final cl_context context;
	private final cl_command_queue commandQueue;
	private final cl_kernel kernel;

	private cl_mem rgbBuffer;
	private final cl_mem paletteBuffer;

	public MandelbrotFractalCl(double minX, double maxX, double minY, double maxY, int maxIterations) {

		super(minX, maxX, minY, maxY, maxIterations);

		CL.setExceptionsEnabled(true);

		var platform = getPlatforms()[0];
		var device = getDevices(platform)[0];
		context = createContext(platform, device);

		commandQueue = createCommandQueue(context, device);
		kernel = createKernel(context, "kernels/mandelbrot_double.cl", "computeMandelbrot");
		palette = createPalette(128);
		paletteBuffer = createBuffer(context, commandQueue, palette);
	}

	@Override
	public void compute(BufferedImage image) {

		var minX = getMinX();
		var minY = getMinY();
		var width = getWidth();
		var height = getHeight();
		var imageWidth = image.getWidth();
		var imageHeight = image.getHeight();
		var scaleX = width / imageWidth;
		var scaleY = height / imageHeight;

		if (rgbBuffer == null) {
			rgbBuffer = createOutputBuffer(context, imageWidth, imageHeight);
		}

		setKernelArgument(kernel, 0, rgbBuffer);
		setKernelArgument(kernel, 1, imageWidth);
		setKernelArgument(kernel, 2, minX);
		setKernelArgument(kernel, 3, minY);
		setKernelArgument(kernel, 4, scaleX);
		setKernelArgument(kernel, 5, scaleY);
		setKernelArgument(kernel, 6, maxIterations);
		setKernelArgument(kernel, 7, paletteBuffer);
		setKernelArgument(kernel, 8, palette.length);

		enqueue(commandQueue, kernel, imageWidth, imageHeight);
		loadImage(commandQueue, rgbBuffer, image);
	}
}
