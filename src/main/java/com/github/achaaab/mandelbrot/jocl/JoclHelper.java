package com.github.achaaab.mandelbrot.jocl;

import org.jocl.CL;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import static com.github.achaaab.mandelbrot.util.Alignment.LEFT;
import static com.github.achaaab.mandelbrot.util.StringUtilities.pad;
import static java.lang.Math.toIntExact;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
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
import static org.jocl.CL.clGetDeviceInfo;
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

	private static final List<Integer> KEY_DEVICE_PARAMETERS = List.of(
			CL.CL_DEVICE_NAME,
			CL.CL_DEVICE_VENDOR,
			CL.CL_DEVICE_OPENCL_C_VERSION,
			CL.CL_DRIVER_VERSION,
			CL.CL_DEVICE_EXTENSIONS);

	private static final Set<Integer> DEVICE_PARAMETERS = Set.of(
			CL.CL_DEVICE_TYPE,
			CL.CL_DEVICE_VENDOR_ID,
			CL.CL_DEVICE_MAX_COMPUTE_UNITS,
			CL.CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS,
			CL.CL_DEVICE_MAX_WORK_GROUP_SIZE,
			CL.CL_DEVICE_MAX_WORK_ITEM_SIZES,
			CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_CHAR,
			CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_SHORT,
			CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_INT,
			CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_LONG,
			CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_FLOAT,
			CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE,
			CL.CL_DEVICE_MAX_CLOCK_FREQUENCY,
			CL.CL_DEVICE_ADDRESS_BITS,
			CL.CL_DEVICE_MAX_READ_IMAGE_ARGS,
			CL.CL_DEVICE_MAX_WRITE_IMAGE_ARGS,
			CL.CL_DEVICE_MAX_MEM_ALLOC_SIZE,
			CL.CL_DEVICE_IMAGE2D_MAX_WIDTH,
			CL.CL_DEVICE_IMAGE2D_MAX_HEIGHT,
			CL.CL_DEVICE_IMAGE3D_MAX_WIDTH,
			CL.CL_DEVICE_IMAGE3D_MAX_HEIGHT,
			CL.CL_DEVICE_IMAGE3D_MAX_DEPTH,
			CL.CL_DEVICE_IMAGE_SUPPORT,
			CL.CL_DEVICE_MAX_PARAMETER_SIZE,
			CL.CL_DEVICE_MAX_SAMPLERS,
			CL.CL_DEVICE_MEM_BASE_ADDR_ALIGN,
			CL.CL_DEVICE_MIN_DATA_TYPE_ALIGN_SIZE,
			CL.CL_DEVICE_SINGLE_FP_CONFIG,
			CL.CL_DEVICE_DOUBLE_FP_CONFIG,
			CL.CL_DEVICE_HALF_FP_CONFIG,
			CL.CL_DEVICE_GLOBAL_MEM_CACHE_TYPE,
			CL.CL_DEVICE_GLOBAL_MEM_CACHELINE_SIZE,
			CL.CL_DEVICE_GLOBAL_MEM_CACHE_SIZE,
			CL.CL_DEVICE_GLOBAL_MEM_SIZE,
			CL.CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE,
			CL.CL_DEVICE_MAX_CONSTANT_ARGS,
			CL.CL_DEVICE_LOCAL_MEM_TYPE,
			CL.CL_DEVICE_LOCAL_MEM_SIZE,
			CL.CL_DEVICE_ERROR_CORRECTION_SUPPORT,
			CL.CL_DEVICE_PROFILING_TIMER_RESOLUTION,
			CL.CL_DEVICE_ENDIAN_LITTLE,
			CL.CL_DEVICE_AVAILABLE,
			CL.CL_DEVICE_COMPILER_AVAILABLE,
			CL.CL_DEVICE_EXECUTION_CAPABILITIES,
			CL.CL_DEVICE_NAME,
			CL.CL_DEVICE_VENDOR,
			CL.CL_DRIVER_VERSION,
			CL.CL_DEVICE_PROFILE,
			CL.CL_DEVICE_VERSION,
			CL.CL_DEVICE_EXTENSIONS,
			CL.CL_DEVICE_PLATFORM,
			CL.CL_DEVICE_PREFERRED_VECTOR_WIDTH_HALF,
			CL.CL_DEVICE_NATIVE_VECTOR_WIDTH_CHAR,
			CL.CL_DEVICE_NATIVE_VECTOR_WIDTH_SHORT,
			CL.CL_DEVICE_NATIVE_VECTOR_WIDTH_INT,
			CL.CL_DEVICE_NATIVE_VECTOR_WIDTH_LONG,
			CL.CL_DEVICE_NATIVE_VECTOR_WIDTH_FLOAT,
			CL.CL_DEVICE_NATIVE_VECTOR_WIDTH_DOUBLE,
			CL.CL_DEVICE_NATIVE_VECTOR_WIDTH_HALF,
			CL.CL_DEVICE_OPENCL_C_VERSION,
			CL.CL_DEVICE_LINKER_AVAILABLE,
			CL.CL_DEVICE_BUILT_IN_KERNELS,
			CL.CL_DEVICE_IMAGE_MAX_BUFFER_SIZE,
			CL.CL_DEVICE_IMAGE_MAX_ARRAY_SIZE,
			CL.CL_DEVICE_PARENT_DEVICE,
			CL.CL_DEVICE_PARTITION_MAX_SUB_DEVICES,
			CL.CL_DEVICE_PARTITION_PROPERTIES,
			CL.CL_DEVICE_PARTITION_AFFINITY_DOMAIN,
			CL.CL_DEVICE_PARTITION_TYPE,
			CL.CL_DEVICE_REFERENCE_COUNT,
			CL.CL_DEVICE_PREFERRED_INTEROP_USER_SYNC,
			CL.CL_DEVICE_PRINTF_BUFFER_SIZE,
			CL.CL_DEVICE_QUEUE_ON_HOST_PROPERTIES,
			CL.CL_DEVICE_IMAGE_PITCH_ALIGNMENT,
			CL.CL_DEVICE_IMAGE_BASE_ADDRESS_ALIGNMENT,
			CL.CL_DEVICE_MAX_READ_WRITE_IMAGE_ARGS,
			CL.CL_DEVICE_MAX_GLOBAL_VARIABLE_SIZE,
			CL.CL_DEVICE_QUEUE_ON_DEVICE_PROPERTIES,
			CL.CL_DEVICE_QUEUE_ON_DEVICE_PREFERRED_SIZE,
			CL.CL_DEVICE_QUEUE_ON_DEVICE_MAX_SIZE,
			CL.CL_DEVICE_MAX_ON_DEVICE_QUEUES,
			CL.CL_DEVICE_MAX_ON_DEVICE_EVENTS,
			CL.CL_DEVICE_SVM_CAPABILITIES,
			CL.CL_DEVICE_GLOBAL_VARIABLE_PREFERRED_TOTAL_SIZE,
			CL.CL_DEVICE_MAX_PIPE_ARGS,
			CL.CL_DEVICE_PIPE_MAX_ACTIVE_RESERVATIONS,
			CL.CL_DEVICE_PIPE_MAX_PACKET_SIZE,
			CL.CL_DEVICE_PREFERRED_PLATFORM_ATOMIC_ALIGNMENT,
			CL.CL_DEVICE_PREFERRED_GLOBAL_ATOMIC_ALIGNMENT,
			CL.CL_DEVICE_PREFERRED_LOCAL_ATOMIC_ALIGNMENT);


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
		return createBuffer(context, CL.CL_MEM_WRITE_ONLY, size);
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
		var buffer = createBuffer(context, CL.CL_MEM_READ_WRITE, size);
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
			var resourceStream = requireNonNull(classLoader.getResourceAsStream(resourceName));
			var resourceReader = new BufferedReader(new InputStreamReader(resourceStream, UTF_8));
			var source = resourceReader.readAllAsString();

			var sourcePointer = new String[] { source };
			var program = clCreateProgramWithSource(context, 1, sourcePointer, null, null);
			clBuildProgram(program, 0, null, null, null, null);

			return clCreateKernel(program, name, null);

		} catch (IOException cause) {

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
		contextProperties.addProperty(CL.CL_CONTEXT_PLATFORM, platform);
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
		clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, 0, null, deviceCountPointer);
		var deviceCount = deviceCountPointer[0];

		var devices = new cl_device_id[deviceCount];
		clGetDeviceIDs(platform, CL.CL_DEVICE_TYPE_ALL, deviceCount, devices, null);

		return devices;
	}

	/**
	 * Gets string information about an OpenCL device.
	 *
	 * @param device device ID
	 * @param parameter parameter
	 * @return device information
	 * @since 0.0.1
	 */
	public static String getDeviceStringInformation(cl_device_id device, int parameter) {

		var size = new long[1];
		clGetDeviceInfo(device, parameter, 0, null, size);

		var buffer = new byte[toIntExact(size[0])];
		clGetDeviceInfo(device, parameter, buffer.length, Pointer.to(buffer), null);

		return new String(buffer, UTF_8);
	}

	/**
	 * Gets long information about an OpenCL device.
	 *
	 * @param device device ID
	 * @param parameter parameter
	 * @return device information
	 * @since 0.0.1
	 */
	public static long getDeviceLongInformation(cl_device_id device, int parameter) {

		var value = new long[1];
		clGetDeviceInfo(device, parameter, Sizeof.cl_long, Pointer.to(value), null);

		return value[0];
	}

	/**
	 * Gets information about an OpenCL device.
	 *
	 * @param device device ID
	 * @since 0.0.1
	 */
	public static void dumpDeviceKeyInformation(cl_device_id device, OutputStream outputStream) {

		try (var writer = new PrintWriter(outputStream)) {

			for (var key : KEY_DEVICE_PARAMETERS) {

				writer.print(pad(CL.stringFor_cl_device_info(key), 48, ' ', LEFT));
				writer.println(" " + getDeviceStringInformation(device, key));
			}
		}
	}
	/**
	 * Returns whether the given device supports double precision floating point numbers.
	 *
	 * @param device device ID
	 * @since 0.0.1
	 */
	public static boolean hasDoublePrecisionSupport(cl_device_id device) {
		return getDeviceLongInformation(device, CL.CL_DEVICE_DOUBLE_FP_CONFIG) != 0;
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

		clEnqueueReadBuffer(commandQueue, buffer, CL.CL_TRUE, 0, size, arrayPointer, 0, null, null);
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
