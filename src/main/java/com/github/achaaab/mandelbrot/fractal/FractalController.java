package com.github.achaaab.mandelbrot.fractal;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static java.lang.Math.pow;
import static java.lang.String.format;

/**
 * @author Jonathan Guéhenneux
 * @param <F> fractal type
 * @since 0.0.0
 */
public abstract class FractalController<F extends Fractal>
		implements MouseMotionListener, MouseWheelListener, MouseListener {

	protected final F fractal;
	protected final FractalView view;

	private final Point previousMousePosition;

	public FractalController(F fractal, FractalView view) {

		this.fractal = fractal;
		this.view = view;

		previousMousePosition = new Point();

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

		update();
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

		update();
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

	protected void update() {

		view.setMessage(format("[%f; %f[ x [%f; %f[",
				fractal.getMinX(), fractal.getMaxX(), fractal.getMinY(), fractal.getMaxY()));

		view.repaint();
	}
}
