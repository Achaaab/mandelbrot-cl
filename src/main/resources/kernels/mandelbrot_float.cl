// An OpenCL kernel for computing the Mandelbrot set with double precision
//
// output : buffer with viewWidth * viewHeight elements, storing the colors as RGB integers
// viewWidth : buffer width in pixels
// viewHeight : buffer height in pixels
// minX : min value of x
// maxX : max value of x
// minY : min value of y
// maxY : max value of y
// maxIterations : maximum number of iterations
// palette : buffer with paletteSize elements containing usable colors
// paletteSize : number of colors in the palette

__kernel void computeMandelbrot(
	__global uint *output,
	int imageWidth,
	float minX,
	float minY,
	float scaleX,
	float scaleY,
	int maxIterations,
	__global uint *palette,
	int paletteSize) {

	unsigned int i = get_global_id(0);
	unsigned int j = get_global_id(1);

	float x0 = minX + i * scaleX;
	float y0 = minY + j * scaleY;

	float x = 0;
	float y = 0;

	float magnitudeSquared = 0;
	int iteration = 0;

	while (iteration < maxIterations && magnitudeSquared < 4) {

		float xx = x * x;
		float yy = y * y;
		y = 2 * x * y + y0;
		x = xx - yy + x0;
		magnitudeSquared = xx + yy;
		iteration++;
	}

	int pixelIndex = j * imageWidth + i;

	if (iteration == maxIterations) {
		output[pixelIndex] = 0;
	} else {
		output[pixelIndex] = palette[iteration % paletteSize];
	}
}
