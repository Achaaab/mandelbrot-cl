package com.github.achaaab.mandelbrot;

import com.github.achaaab.mandelbrot.fractal.FractalController;
import com.github.achaaab.mandelbrot.fractal.FractalView;
import com.github.achaaab.mandelbrot.util.QuadraticProgression;

import java.awt.event.MouseWheelEvent;
import java.time.Duration;

import static com.github.achaaab.mandelbrot.util.Utilities.time;
import static java.lang.Math.round;
import static java.lang.Math.toIntExact;

/**
 * This controller adds max iterations control with shift + mouse wheel rotation.
 *
 * @author Jonathan Guéhenneux
 * @since 0.0.0
 */
public class MandelbrotFractalController extends FractalController<MandelbrotFractal> {

	private final QuadraticProgression iterationProgression;

	/**
	 * Creates a new controller for the given Mandelbrot fractal and view.
	 *
	 * @param fractal Mandelbrot fractal
	 * @param view fractal view
	 * @since 0.0.0
	 */
	public MandelbrotFractalController(MandelbrotFractal fractal, FractalView view) {

		super(fractal, view);

		iterationProgression = new QuadraticProgression(1.0, 0.5, 200_000_000L, 500_000_000L);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {

		if (event.isShiftDown()) {

			var wheelRotation = -event.getWheelRotation();
			var factor = iterationProgression.update(wheelRotation > 0);
			fractal.adjustIterations(toIntExact(round(factor * wheelRotation)));
			requestUpdate();

		} else {

			super.mouseWheelMoved(event);
		}
	}

	@Override
	protected void update() {

		var image = view.getImage();
		var computeDuration = time(() -> fractal.compute(image));

		update(getMessage(computeDuration));
	}

	/**
	 * Builds custom message for Mandelbrot fractal.
	 *
	 * @param duration computing duration
	 * @return built custom message
	 * @since 0.0.1
	 */
	private String getMessage(Duration duration) {

		var seconds = duration.getNano() / 1_000_000_000.0;
		var baseMessage = getMessage();

		var maxIterations = fractal.getIterations();
		var iterationPluralized = maxIterations > 1 ? "iterations" : "iteration";

		var additionalMessage = String.format("    %d " + iterationPluralized + " (%.4fs)",
				maxIterations, seconds);

		return baseMessage + additionalMessage;
	}
}
