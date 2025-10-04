package com.github.achaaab.mandelbrot.jocl;

import com.github.achaaab.mandelbrot.MandelbrotFractal;
import org.jocl.CL;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;

import java.awt.image.BufferedImage;

import static com.github.achaaab.mandelbrot.jocl.JoclHelper.createBuffer;
import static com.github.achaaab.mandelbrot.jocl.JoclHelper.createCommandQueue;
import static com.github.achaaab.mandelbrot.jocl.JoclHelper.createContext;
import static com.github.achaaab.mandelbrot.jocl.JoclHelper.createKernel;
import static com.github.achaaab.mandelbrot.jocl.JoclHelper.createOutputBuffer;
import static com.github.achaaab.mandelbrot.jocl.JoclHelper.enqueue;
import static com.github.achaaab.mandelbrot.jocl.JoclHelper.getDevices;
import static com.github.achaaab.mandelbrot.jocl.JoclHelper.getPlatforms;
import static com.github.achaaab.mandelbrot.jocl.JoclHelper.loadImage;
import static com.github.achaaab.mandelbrot.jocl.JoclHelper.setKernelArgument;

/**
 * Mandelbrot fractal computed with OpenCL limited to simple precision floating point numbers.
 *
 * @author Jonathan Guéhenneux
 * @since 0.0.0
 */
public class MandelbrotFractalClFloat extends MandelbrotFractal {

	private final cl_context context;
	private final cl_command_queue commandQueue;
	private final cl_kernel kernel;

	private cl_mem rgbBuffer;
	private final cl_mem paletteBuffer;

	/**
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 * @param maxIterations
	 * @since 0.0.0
	 */
	public MandelbrotFractalClFloat(double minX, double maxX, double minY, double maxY, int maxIterations) {

		super(minX, maxX, minY, maxY, maxIterations);

		CL.setExceptionsEnabled(true);

		var platform = getPlatforms()[0];
		var device = getDevices(platform)[0];
		context = createContext(platform, device);

		commandQueue = createCommandQueue(context, device);
		kernel = createKernel(context, "kernels/mandelbrot_float.cl", "computeMandelbrot");
		paletteBuffer = createBuffer(context, commandQueue, palette);
	}

	@Override
	public void compute(BufferedImage image) {

		var minX = (float) getMinX();
		var minY = (float) getMinY();
		var width = getWidth();
		var height = getHeight();
		var imageWidth = image.getWidth();
		var imageHeight = image.getHeight();
		var scaleX = (float) width / imageWidth;
		var scaleY = (float) height / imageHeight;

		if (rgbBuffer == null) {
			rgbBuffer = createOutputBuffer(context, imageWidth, imageHeight);
		}

		var kernelArgumentIndex = 0;

		setKernelArgument(kernel, kernelArgumentIndex++, rgbBuffer);
		setKernelArgument(kernel, kernelArgumentIndex++, imageWidth);
		setKernelArgument(kernel, kernelArgumentIndex++, imageHeight);
		setKernelArgument(kernel, kernelArgumentIndex++, minX);
		setKernelArgument(kernel, kernelArgumentIndex++, minY);
		setKernelArgument(kernel, kernelArgumentIndex++, scaleX);
		setKernelArgument(kernel, kernelArgumentIndex++, scaleY);
		setKernelArgument(kernel, kernelArgumentIndex++, maxIterations);
		setKernelArgument(kernel, kernelArgumentIndex++, paletteBuffer);
		setKernelArgument(kernel, kernelArgumentIndex, palette.length);

		enqueue(commandQueue, kernel, imageWidth, imageHeight);
		loadImage(commandQueue, rgbBuffer, image);
	}
}
