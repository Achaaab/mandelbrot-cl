package com.github.achaaab.mandelbrot.fractal;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import static com.github.achaaab.mandelbrot.util.SwingUtility.getTextBounds;
import static com.github.achaaab.mandelbrot.util.SwingUtility.scale;
import static java.awt.Color.BLACK;
import static java.awt.Color.RED;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

/**
 * Default fractal view.
 *
 * @author Jonathan Guéhenneux
 * @since 0.0.0
 */
public class FractalView extends JComponent {

	private static final Point MESSAGE_POSITION = new Point(scale(2), scale(2));
	private static final float MESSAGE_FONT_SIZE = scale(12.0f);
	private static final Color MESSAGE_COLOR = RED;

	private final BufferedImage image;
	private boolean messageDisplayed;
	private String message;

	/**
	 * Creates a new fractal view.
	 *
	 * @param width view width in pixels
	 * @param height view height in pixels
	 * @since 0.0.0
	 */
	public FractalView(int width, int height) {

		image = new BufferedImage(width, height, TYPE_INT_RGB);
		messageDisplayed = false;

		var preferredSize = new Dimension(width, height);
		setPreferredSize(preferredSize);
	}

	@Override
	public void paintComponent(Graphics graphics) {

		var graphics2d = (Graphics2D) graphics;

		super.paintComponent(graphics2d);
		graphics2d.drawImage(image, 0, 0, this);

		if (messageDisplayed) {

			var font = graphics2d.getFont();
			var messageFont = font.deriveFont(MESSAGE_FONT_SIZE);
			graphics2d.setFont(messageFont);

			var messageBounds = getTextBounds(graphics2d, message,
					MESSAGE_POSITION.x, MESSAGE_POSITION.y);

			messageBounds.y += messageBounds.height;

			graphics2d.setColor(BLACK);
			graphics2d.fill(messageBounds);

			graphics.setColor(MESSAGE_COLOR);
			graphics.drawString(message, MESSAGE_POSITION.x, MESSAGE_POSITION.y + messageBounds.height);
		}
	}

	public BufferedImage getImage() {
		return image;
	}

	public boolean isMessageDisplayed() {
		return messageDisplayed;
	}

	public void setMessageDisplayed(boolean messageDisplayed) {
		this.messageDisplayed = messageDisplayed;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
