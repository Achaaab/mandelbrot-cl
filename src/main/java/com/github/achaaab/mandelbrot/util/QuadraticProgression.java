package com.github.achaaab.mandelbrot.util;

import static java.lang.System.nanoTime;

/**
 * Quadratic progression allowing small increments and acceleration when updating frequently.
 *
 * @author Jonathan Guéhenneux
 * @since 0.0.1
 */
public class QuadraticProgression {

	private final double initialValue;
	private final double increment;
	private final long accelerationDelay;
	private final long resetDelay;

	private double value;

	private boolean increasing;
	private long lastUpdateTime;

	/**
	 * Creates a quadratic progression.
	 *
	 * @param initialValue initial value
	 * @param increment increment
	 * @param accelerationDelay delay in nanoseconds for increment
	 * @param resetDelay delay in nanoseconds before resetting to initial value
	 * @since 0.0.1
	 */
	public QuadraticProgression(double initialValue, double increment, long accelerationDelay, long resetDelay) {

		this.initialValue = initialValue;
		this.increment = increment;
		this.accelerationDelay = accelerationDelay;
		this.resetDelay = resetDelay;

		value = initialValue;
		increasing = false;
		lastUpdateTime = 0L;
	}

	/**
	 * Updates the value.
	 *
	 * @param increasing whether the value should increase or decrease
	 * @return new value
	 * @since 0.0.1
	 */
	public double update(boolean increasing) {

		var updateTime = nanoTime();
		var delay = updateTime - lastUpdateTime;

		if (delay > resetDelay || increasing != this.increasing) {
			value = initialValue;
		} else if (delay < accelerationDelay) {
			value += increment;
		}

		lastUpdateTime = updateTime;
		this.increasing = increasing;

		return value;
	}
}
