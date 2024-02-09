package com.github.achaaab.mandelbrot;

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
			var maxIterations = 100;
			var viewWidth = 1500;
			var viewHeight = 1500;
			var openCl = true;

			var view = new FractalView(viewWidth, viewHeight);

			var fractal = openCl ?
					new MandelbrotFractalCl(minX, maxX, minY, maxY, maxIterations, viewWidth, viewHeight) :
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
