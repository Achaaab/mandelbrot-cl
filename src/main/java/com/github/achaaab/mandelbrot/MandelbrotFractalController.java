package com.github.achaaab.mandelbrot;

import java.awt.event.MouseWheelEvent;

public class MandelbrotFractalController extends FractalController<MandelbrotFractal> {

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
		view.repaint();
	}
}
