package com.github.achaaab.mandelbrot;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import static java.lang.Math.pow;

public abstract class FractalController<F extends Fractal>
		implements MouseMotionListener, MouseWheelListener {

	protected final F fractal;
	protected final FractalView view;

	private final Point previousMousePosition;

	public FractalController(F fractal, FractalView view) {

		this.fractal = fractal;
		this.view = view;

		previousMousePosition = new Point();

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

		fractal.translate(dx / scaleX, dy / scaleY);

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
		var y = fractal.getMinY() + mousePosition.y / scaleY;

		fractal.zoom(x, y, factor);

		update();
	}

	protected abstract void update();
}
