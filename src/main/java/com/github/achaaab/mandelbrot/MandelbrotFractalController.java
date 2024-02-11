package com.github.achaaab.mandelbrot;

import com.github.achaaab.mandelbrot.fractal.FractalController;
import com.github.achaaab.mandelbrot.fractal.FractalView;

import java.awt.event.MouseWheelEvent;

/**
 * This controller adds max iterations control with shift + mouse wheel rotation.
 *
 * @author Jonathan Guéhenneux
 * @since 0.0.0
 */
public class MandelbrotFractalController extends FractalController<MandelbrotFractal> {

	/**
	 * Creates a new controller for the given Mandelbrot fractal and view.
	 *
	 * @param fractal Mandelbrot fractal
	 * @param view fractal view
	 * @since 0.0.0
	 */
	public MandelbrotFractalController(MandelbrotFractal fractal, FractalView view) {
		super(fractal, view);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {

		if (event.isShiftDown()) {

			var amount = -5 * event.getWheelRotation();
			fractal.sharpen(amount);
			update();

		} else {

			super.mouseWheelMoved(event);
		}
	}

	@Override
	protected void update() {

		var image = view.getImage();
		fractal.compute(image);

		super.update();
	}
}
