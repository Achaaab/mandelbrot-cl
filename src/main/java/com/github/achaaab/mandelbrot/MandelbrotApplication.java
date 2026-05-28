package com.github.achaaab.mandelbrot;

import com.github.achaaab.mandelbrot.fractal.FractalView;
import com.github.achaaab.mandelbrot.jocl.MandelbrotFractalClDouble;
import com.github.achaaab.mandelbrot.jocl.MandelbrotFractalClFloat;

import javax.swing.JFrame;
import java.lang.reflect.InvocationTargetException;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 * Simple desktop application drawing Mandelbrot fractal.
 *
 * @author Jonathan Guéhenneux
 * @since 0.0.0
 */
class MandelbrotApplication {

	/// Entry point of the application. Creates and show a Mandelbrot fractal GUI.
	///
	/// @param arguments 0. Computing method:
	///
	///     - OPEN_CL_64
	///     - OPEN_CL_32
	///     - CPU (default, but slowest)
	///     - CPU_SIMD (requires a CPU with SIMD instructions)
	/// @since 0.0.1
	///
	void main(String... arguments) {

		invokeLater(() -> {

			var minX = -2.0;
			var maxX = 0.6;
			var minY = -1.3;
			var maxY = 1.3;
			var maxIterations = 128;
			var viewWidth = 1024;
			var viewHeight = 1024;

			var view = new FractalView(viewWidth, viewHeight);
			view.setMessageDisplayed(true);

			var method = arguments.length > 0 ? arguments[0] : "CPU";

			var fractal = switch (method) {

				case "OPEN_CL_64" -> new MandelbrotFractalClDouble(minX, maxX, minY, maxY, maxIterations);
				case "OPEN_CL_32" -> new MandelbrotFractalClFloat(minX, maxX, minY, maxY, maxIterations);
				case "CPU_SIMD" -> createMandelbrotFactalSimd(minX, maxX, minY, maxY, maxIterations);
				default -> new MandelbrotFractalCpu(minX, maxX, minY, maxY, maxIterations);
			};

			var controller = new MandelbrotFractalController(fractal, view);
			controller.requestUpdate();

			var frame = new JFrame("Mandelbrot set rendering");
			frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			frame.setContentPane(view);
			frame.pack();
			frame.setResizable(false);
			frame.setVisible(true);
		});
	}

	/**
	 * @param minX
	 * @param maxX
	 * @param minY
	 * @param maxY
	 * @param maxIterations
	 * @return
	 */
	private static MandelbrotFractal createMandelbrotFactalSimd(
			double minX, double maxX, double minY, double maxY, int maxIterations) {

		try {

			var implementation = Class.forName("com.github.achaaab.mandelbrot.MandelbrotFractalSimd");

			var constructor = implementation.getConstructor(
					Double.TYPE,
					Double.TYPE,
					Double.TYPE,
					Double.TYPE,
					Integer.TYPE);

			return (MandelbrotFractal) constructor.newInstance(minX, maxX, minY, maxY, maxIterations);

		}catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
				InvocationTargetException cause) {

			throw new RuntimeException(cause);
		}
	}
}
