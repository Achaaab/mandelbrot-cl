// An OpenCL kernel for computing the Mandelbrot set with float precision
//
// output : buffer with viewWidth * viewHeight elements, storing the colors as RGB integers
// imageWidth : buffer width in pixels
// imageHeight : buffer height in pixels
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
	int imageHeight,
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

	float x = 0.0;
	float y = 0.0;

	float xx = 0.0;
	float yy = 0.0;

	int iteration = 0;

	while (iteration < maxIterations && xx + yy < 4) {

		y = 2 * x * y + y0;
		x = xx - yy + x0;

		xx = x * x;
		yy = y * y;

		iteration++;
	}

	int pixelIndex = (imageHeight - j - 1) * imageWidth + i;

	if (iteration == maxIterations) {
		output[pixelIndex] = 0;
	} else {
		output[pixelIndex] = palette[iteration % paletteSize];
	}
}
