package com.github.achaaab.mandelbrot.fractal;

import javax.swing.SwingUtilities;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.pow;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * @param <F> fractal type
 * @author Jonathan Guéhenneux
 * @since 0.0.0
 */
public abstract class FractalController<F extends Fractal>
		implements MouseMotionListener, MouseWheelListener, MouseListener {

	protected final F fractal;
	protected final FractalView view;

	private final Point previousMousePosition;

	private final ExecutorService executor;
	private final AtomicBoolean updatePending;
	private final AtomicBoolean updateRequested;

	/**
	 * @param fractal
	 * @param view
	 * @since 0.0.1
	 */
	public FractalController(F fractal, FractalView view) {

		this.fractal = fractal;
		this.view = view;

		previousMousePosition = new Point();

		executor = newSingleThreadExecutor();
		updatePending = new AtomicBoolean();
		updateRequested = new AtomicBoolean();

		view.addMouseListener(this);
		view.addMouseMotionListener(this);
		view.addMouseWheelListener(this);
	}

	@Override
	public void mouseDragged(MouseEvent event) {

		var mousePosition = event.getPoint();

		var dx = previousMousePosition.x - mousePosition.x;
		var dy = previousMousePosition.y - mousePosition.y;

		var fractalWidth = fractal.getWidth();
		var fractalHeight = fractal.getHeight();
		var viewWidth = view.getWidth();
		var viewHeight = view.getHeight();

		var scaleX = viewWidth / fractalWidth;
		var scaleY = viewHeight / fractalHeight;

		fractal.translate(dx / scaleX, -dy / scaleY);

		previousMousePosition.setLocation(mousePosition);

		requestUpdate();
	}

	@Override
	public void mouseMoved(MouseEvent event) {
		previousMousePosition.setLocation(event.getPoint());
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent event) {

		var factor = pow(2, -event.getWheelRotation() / 10.0);

		var mousePosition = event.getPoint();

		var fractalWidth = fractal.getWidth();
		var fractalHeight = fractal.getHeight();
		var viewWidth = view.getWidth();
		var viewHeight = view.getHeight();

		var scaleX = viewWidth / fractalWidth;
		var scaleY = viewHeight / fractalHeight;

		var x = fractal.getMinX() + mousePosition.x / scaleX;
		var y = fractal.getMinY() + (viewHeight - mousePosition.y - 1) / scaleY;

		fractal.zoom(x, y, factor);

		requestUpdate();
	}

	@Override
	public void mouseClicked(MouseEvent event) {

		view.setMessageDisplayed(!view.isMessageDisplayed());
		view.repaint();
	}

	@Override
	public void mousePressed(MouseEvent event) {

	}

	@Override
	public void mouseReleased(MouseEvent event) {

	}

	@Override
	public void mouseEntered(MouseEvent event) {

	}

	@Override
	public void mouseExited(MouseEvent event) {

	}

	/**
	 * @since 0.0.1
	 */
	public void requestUpdate() {

		if (updatePending.compareAndSet(false, true)) {
			executor.submit(() -> update());
		} else {
			updateRequested.set(true);
		}
	}

	/**
	 * @since 0.0.1
	 */
	protected void update() {
		update(getMessage());
	}

	/**
	 * @param message
	 * @since 0.0.1
	 */
	protected final void update(String message) {

		var interrupted = false;

		try {

			view.setMessage(message);
			SwingUtilities.invokeAndWait(view::paintImmediately);

		} catch (InterruptedException interruptedException) {

			interrupted = true;

		} catch (InvocationTargetException invocationTargetException) {

			System.err.printf("Repaint error: %s.%n", invocationTargetException.getMessage());

		} finally {

			if (!interrupted && updateRequested.compareAndSet(true, false)) {

				update();

			} else {

				updatePending.set(false);

				if (interrupted) {
					currentThread().interrupt();
				}
			}
		}
	}

	/**
	 * @return
	 * @since 0.0.1
	 */
	protected String getMessage() {

		return format("[%+.12f; %+.12f] x [%+.12f; %+.12f]",
				fractal.getMinX(), fractal.getMaxX(),
				fractal.getMinY(), fractal.getMaxY());
	}
}
