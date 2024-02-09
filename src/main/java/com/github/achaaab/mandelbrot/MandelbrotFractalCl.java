package com.github.achaaab.mandelbrot;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;
import org.jocl.cl_queue_properties;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static org.jocl.CL.CL_CONTEXT_PLATFORM;
import static org.jocl.CL.CL_DEVICE_TYPE_ALL;
import static org.jocl.CL.CL_MEM_READ_WRITE;
import static org.jocl.CL.CL_MEM_WRITE_ONLY;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clBuildProgram;
import static org.jocl.CL.clCreateBuffer;
import static org.jocl.CL.clCreateCommandQueueWithProperties;
import static org.jocl.CL.clCreateContext;
import static org.jocl.CL.clCreateKernel;
import static org.jocl.CL.clCreateProgramWithSource;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clEnqueueReadBuffer;
import static org.jocl.CL.clEnqueueWriteBuffer;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clSetKernelArg;
import static org.jocl.Sizeof.cl_int;
import static org.jocl.Sizeof.cl_uint;

public class MandelbrotFractalCl extends MandelbrotFractal {

	private final int viewWidth;
	private final int viewHeight;

	private cl_command_queue commandQueue;
	private cl_kernel kernel;
	private cl_mem pixelMem;
	private cl_mem colorMapMem;

	public MandelbrotFractalCl(double minX, double maxX, double minY, double maxY, int maxIterations,
			int viewWidth, int viewHeight) {

		super(minX, maxX, minY, maxY, maxIterations);

		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;

		var deviceType = CL_DEVICE_TYPE_ALL;

		// Enable exceptions and subsequently omit error checks in this sample
		CL.setExceptionsEnabled(true);

		// Obtain the number of platforms
		var platformCountArray = new int[1];
		clGetPlatformIDs(0, null, platformCountArray);
		var platformCount = platformCountArray[0];

		// Obtain a platform ID
		var platforms = new cl_platform_id[platformCount];
		clGetPlatformIDs(platforms.length, platforms, null);
		var platform = platforms[0];

		// Initialize the context properties
		var contextProperties = new cl_context_properties();
		contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

		// Obtain the number of devices for the platform
		var deviceCountArray = new int[1];
		clGetDeviceIDs(platform, deviceType, 0, null, deviceCountArray);
		var deviceCount = deviceCountArray[0];

		// Obtain a device ID
		var devicesIds = new cl_device_id[deviceCount];
		clGetDeviceIDs(platform, deviceType, deviceCount, devicesIds, null);
		var deviceId = devicesIds[0];
		var selectedDevices = new cl_device_id[] { deviceId };

		// Create a context for the selected device

		var context = clCreateContext(contextProperties, 1, selectedDevices, null, null, null);

		// Create a command-queue for the selected device
		var queueProperties = new cl_queue_properties();
		commandQueue = clCreateCommandQueueWithProperties(context, deviceId, queueProperties, null);

		try {

			var sourceFile = Path.of("src/main/resources/kernels/mandelbrot_double.cl");
			var source = readString(sourceFile, UTF_8);

			// Create the program
			var program = clCreateProgramWithSource(context, 1, new String[] { source }, null, null);

			// Build the program
			clBuildProgram(program, 0, null, "-cl-mad-enable", null, null);

			// Create the kernel
			kernel = clCreateKernel(program, "computeMandelbrot", null);

			// Create the memory object which will be filled with the pixel data
			pixelMem = clCreateBuffer(context, CL_MEM_WRITE_ONLY, (long) viewWidth * viewHeight * cl_uint, null, null);

			colorMapMem = clCreateBuffer(context, CL_MEM_READ_WRITE, (long) palette.length * cl_uint, null, null);

			clEnqueueWriteBuffer(commandQueue, colorMapMem, true, 0,
					(long) palette.length * cl_uint, Pointer.to(palette), 0, null, null);

		} catch (IOException cause) {

			throw new UncheckedIOException(cause);
		}
	}

	@Override
	public void compute(BufferedImage image) {

		// Set work size and execute the kernel
		var globalWorkSize = new long[] { viewWidth, viewHeight };

		var scaleX = getWidth() / viewWidth;
		var scaleY = getHeight() / viewHeight;

		clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(pixelMem));
		clSetKernelArg(kernel, 1, cl_uint, Pointer.to(new int[] { viewWidth }));
		clSetKernelArg(kernel, 2, Sizeof.cl_double, Pointer.to(new double[] { getMinX() }));
		clSetKernelArg(kernel, 3, Sizeof.cl_double, Pointer.to(new double[] { getMinY() }));
		clSetKernelArg(kernel, 4, Sizeof.cl_double, Pointer.to(new double[] { scaleX }));
		clSetKernelArg(kernel, 5, Sizeof.cl_double, Pointer.to(new double[] { scaleY }));
		clSetKernelArg(kernel, 6, cl_int, Pointer.to(new int[] { maxIterations }));
		clSetKernelArg(kernel, 7, Sizeof.cl_mem, Pointer.to(colorMapMem));
		clSetKernelArg(kernel, 8, cl_int, Pointer.to(new int[] { palette.length }));

		clEnqueueNDRangeKernel(commandQueue, kernel, 2, null, globalWorkSize, null, 0, null, null);

		// Read the pixel data into the BufferedImage
		var dataBuffer = (DataBufferInt) image.getRaster().getDataBuffer();
		var data = dataBuffer.getData();

		clEnqueueReadBuffer(commandQueue, pixelMem, CL_TRUE, 0,
				(long) cl_int * viewWidth * viewHeight, Pointer.to(data), 0, null, null);
	}
}
