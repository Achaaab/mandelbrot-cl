package com.github.achaaab.mandelbrot.jocl;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.cl_platform_id;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readString;
import static java.util.Objects.requireNonNull;
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

/**
 * JOCL library does not fit very well to Java style programming.
 * This utility class aims to offer a more Java-ish way to access to JOCL.
 *
 * @author Jonathan Guéhenneux
 * @since 0.0.0
 */
public class JoclHelper {

	/**
	 * Creates a write-only buffer just large enough to contains {@code width x height} int values.
	 *
	 * @param context OpenCL context used to create the buffer object
	 * @param width image width
	 * @param height image height
	 * @return created buffer
	 * @since 0.0.0
	 */
	public static cl_mem createOutputBuffer(cl_context context, int width, int height) {

		var size = (long) width * height * Sizeof.cl_int;
		return createBuffer(context, CL_MEM_WRITE_ONLY, size);
	}

	/**
	 * Creates a memory buffer.
	 *
	 * @param context OpenCL context used to create the buffer object
	 * @param flags bit-field that is used to specify allocation and usage information
	 * @param size buffer size in bytes
	 * @return created buffer
	 * @since 0.0.0
	 */
	public static cl_mem createBuffer(cl_context context, long flags, long size) {
		return clCreateBuffer(context, flags, size, null, null);
	}

	/**
	 * Creates a memory buffer and writes the given array in it.
	 *
	 * @param context OpenCL context used to create the buffer object
	 * @param commandQueue queue in which to enqueue the write command
	 * @param array array to load into the created buffer
	 * @return created and filled buffer
	 * @since 0.0.0
	 */
	public static cl_mem createBuffer(cl_context context, cl_command_queue commandQueue, int[] array) {

		var size = (long) array.length * Sizeof.cl_int;
		var buffer = createBuffer(context, CL_MEM_READ_WRITE, size);
		writeBuffer(commandQueue, array, buffer);

		return buffer;
	}

	/**
	 * Creates a kernel for a given OpenCL context, loading the source code from the named resource.
	 *
	 * @param context OpenCL context
	 * @param resourceName name of the resource containing the kernel source code
	 * @param name name of the kernel (it must exist in the given program source code)
	 * @return created kernel from the given resource
	 * @since 0.0.0
	 */
	public static cl_kernel createKernel(cl_context context, String resourceName, String name) {

		try {

			var classLoader = JoclHelper.class.getClassLoader();
			var resourceUrl = classLoader.getResource(resourceName);
			var resourceUri = requireNonNull(resourceUrl).toURI();
			var resourcePath = Path.of(resourceUri);

			var source = readString(resourcePath, UTF_8);
			var sourcePointer = new String[] { source };
			var program = clCreateProgramWithSource(context, 1, sourcePointer, null, null);
			clBuildProgram(program, 0, null, null, null, null);

			return clCreateKernel(program, name, null);

		} catch (URISyntaxException | IOException cause) {

			throw new RuntimeException(cause);
		}
	}

	/**
	 * Creates a single device OpenCL context.
	 *
	 * @param platform platform
	 * @param device device
	 * @return created OpenCL context
	 * @since 0.0.0
	 */
	public static cl_context createContext(cl_platform_id platform, cl_device_id device) {

		var contextProperties = new cl_context_properties();
		contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
		var selectedDevices = new cl_device_id[] { device };

		return clCreateContext(contextProperties, 1, selectedDevices, null, null, null);
	}

	/**
	 * Create a command queue width default properties in the given context for the given device.
	 *
	 * @param context OpenCL context
	 * @param device OpenCL device
	 * @return created command queue
	 * @since 0.0.0
	 */
	public static cl_command_queue createCommandQueue(cl_context context, cl_device_id device) {
		return clCreateCommandQueueWithProperties(context, device, null, null);
	}

	/**
	 * Queries list of OpenCL platforms available.
	 *
	 * @return OpenCL platforms available
	 * @since 0.0.0
	 */
	public static cl_platform_id[] getPlatforms() {

		var platformCountPointer = new int[1];
		clGetPlatformIDs(0, null, platformCountPointer);
		var platformCount = platformCountPointer[0];

		var platforms = new cl_platform_id[platformCount];
		clGetPlatformIDs(platforms.length, platforms, null);

		return platforms;
	}

	/**
	 * Queries list of OpenCL devices on a given platform.
	 *
	 * @param platform platform ID
	 * @return OpenCL devices available on this platform
	 * @since 0.0.0
	 */
	public static cl_device_id[] getDevices(cl_platform_id platform) {

		var deviceCountPointer = new int[1];
		clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, 0, null, deviceCountPointer);
		var deviceCount = deviceCountPointer[0];

		var devices = new cl_device_id[deviceCount];
		clGetDeviceIDs(platform, CL_DEVICE_TYPE_ALL, deviceCount, devices, null);

		return devices;
	}

	/**
	 * Sets the value for a specific argument of a kernel.
	 *
	 * @param kernel kernel
	 * @param index argument index
	 * @param value argument value
	 * @since 0.0.0
	 */
	public static void setKernelArgument(cl_kernel kernel, int index, cl_mem value) {
		clSetKernelArg(kernel, index, Sizeof.cl_mem, Pointer.to(value));
	}

	/**
	 * Sets the value for a specific argument of a kernel.
	 *
	 * @param kernel kernel
	 * @param index argument index
	 * @param value argument value
	 * @since 0.0.0
	 */
	public static void setKernelArgument(cl_kernel kernel, int index, int value) {
		clSetKernelArg(kernel, index, Sizeof.cl_int, Pointer.to(new int[] { value }));
	}

	/**
	 * Sets the value for a specific argument of a kernel.
	 *
	 * @param kernel kernel
	 * @param index argument index
	 * @param value argument value
	 * @since 0.0.0
	 */
	public static void setKernelArgument(cl_kernel kernel, int index, float value) {
		clSetKernelArg(kernel, index, Sizeof.cl_float, Pointer.to(new float[] { value }));
	}

	/**
	 * Sets the value for a specific argument of a kernel.
	 *
	 * @param kernel kernel
	 * @param index argument index
	 * @param value argument value
	 * @since 0.0.0
	 */
	public static void setKernelArgument(cl_kernel kernel, int index, double value) {
		clSetKernelArg(kernel, index, Sizeof.cl_double, Pointer.to(new double[] { value }));
	}

	/**
	 * Enqueues a command to execute a kernel in the given command queue.
	 * This helper methods is dedicated to kernels working with global 2-dimensional work.
	 *
	 * @param queue command queue
	 * @param kernel kernel to execute
	 * @param width work width
	 * @param height work height
	 * @since 0.0.0
	 */
	public static void enqueue(cl_command_queue queue, cl_kernel kernel, int width, int height) {

		var globalWorkSize = new long[] { width, height };
		clEnqueueNDRangeKernel(queue, kernel, 2, null, globalWorkSize, null, 0, null, null);
	}

	/**
	 * Reads RGB values from the given RGB buffer and writes it into the given image.
	 *
	 * @param commandQueue queue in which the read command will be queued
	 * @param rgbBuffer RGB values
	 * @param image image to load
	 * @since 0.0.0
	 */
	public static void loadImage(cl_command_queue commandQueue, cl_mem rgbBuffer, BufferedImage image) {

		var raster = image.getRaster();
		var dataBuffer = (DataBufferInt) raster.getDataBuffer();
		var data = dataBuffer.getData();

		readBuffer(commandQueue, rgbBuffer, data);
	}

	/**
	 * Fills the given array reading from the given buffer.
	 *
	 * @param commandQueue queue in which the read command will be queued
	 * @param buffer buffer to read
	 * @param array array to write
	 * @since 0.0.0
	 */
	public static void readBuffer(cl_command_queue commandQueue, cl_mem buffer, int[] array) {

		var size = (long) array.length * Sizeof.cl_int;
		var arrayPointer = Pointer.to(array);

		clEnqueueReadBuffer(commandQueue, buffer, CL_TRUE, 0, size, arrayPointer, 0, null, null);
	}

	/**
	 * Fills the given buffer reading from the given array.
	 *
	 * @param commandQueue queue in which the write command will be queued
	 * @param array array to read
	 * @param buffer buffer to write
	 * @since 0.0.0
	 */
	public static void writeBuffer(cl_command_queue commandQueue, int[] array, cl_mem buffer) {

		var size = (long) array.length * Sizeof.cl_int;
		var arrayPointer = Pointer.to(array);

		clEnqueueWriteBuffer(commandQueue, buffer, true, 0, size, arrayPointer, 0, null, null);
	}
}
