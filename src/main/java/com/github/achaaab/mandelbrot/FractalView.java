package com.github.achaaab.mandelbrot;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class FractalView extends JComponent {

	private final BufferedImage image;

	public FractalView(int width, int height) {

		image = new BufferedImage(width, height, TYPE_INT_RGB);

		var preferredSize = new Dimension(width, height);
		setPreferredSize(preferredSize);
	}

	@Override
	public void paintComponent(Graphics graphics) {

		super.paintComponent(graphics);
		graphics.drawImage(image, 0, 0, this);
	}

	public BufferedImage getImage() {
		return image;
	}
}
