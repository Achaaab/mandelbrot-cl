package com.github.achaaab.mandelbrot;

public abstract class Fractal {

	private double centerX;
	private double centerY;
	private double halfWidth;
	private double halfHeight;

	public Fractal(double minX, double maxX, double minY, double maxY) {

		centerX = (minX + maxX) / 2;
		centerY = (minY + maxY) / 2;
		halfWidth = centerX - minX;
		halfHeight = centerY - minY;
	}

	public void zoom(double x, double y, double factor) {

		halfWidth /= factor;
		halfHeight /= factor;
		centerX = x - (x - centerX) / factor;
		centerY = y - (y - centerY) / factor;
	}

	public void translate(double dx, double dy) {

		centerX += dx;
		centerY += dy;
	}

	public double getMinX() {
		return centerX - halfWidth;
	}

	public double getMaxX() {
		return centerX + halfWidth;
	}

	public double getMinY() {
		return centerY - halfHeight;
	}

	public double getMaxY() {
		return centerY + halfHeight;
	}

	public double getWidth() {
		return halfWidth * 2;
	}

	public double getHeight() {
		return halfHeight * 2;
	}
}
