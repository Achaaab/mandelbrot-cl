package com.github.achaaab.mandelbrot;

import com.github.achaaab.mandelbrot.fractal.FractalView;
import com.github.achaaab.mandelbrot.jocl.MandelbrotFractalClDouble;
import com.github.achaaab.mandelbrot.jocl.MandelbrotFractalClFloat;

import javax.swing.JFrame;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 * @author Jonathan Guéhenneux
 * @since 0.0.0
 */
public class MandelbrotApplication {

	/**
	 * @param arguments not used
	 */
	public static void main(String... arguments) {

		invokeLater(() -> {

			var minX = -2.0;
			var maxX = 0.6;
			var minY = -1.3;
			var maxY = 1.3;
			var maxIterations = 512;
			var viewWidth = 1024;
			var viewHeight = 1024;
			var openCl = true;

			var view = new FractalView(viewWidth, viewHeight);

			var fractal = openCl ?
					new MandelbrotFractalClDouble(minX, maxX, minY, maxY, maxIterations) :
					new MandelbrotFractalCpu(minX, maxX, minY, maxY, maxIterations);

			var controller = new MandelbrotFractalController(fractal, view);

			controller.update();

			var frame = new JFrame("Mandelbrot set rendering");
			frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
			frame.setContentPane(view);
			frame.pack();
			frame.setVisible(true);
		});
	}
}
