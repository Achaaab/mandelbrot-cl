package com.github.achaaab.mandelbrot.util;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.lang.Math.round;

/**
 * Swing utility methods
 *
 * @author Jonathan Guéhenneux
 * @since 0.0.0
 */
public class SwingUtility {

	private static final Toolkit TOOLKIT = getDefaultToolkit();
	private static final float BASE_RESOLUTION = 72.0f;
	private static final int RESOLUTION = TOOLKIT.getScreenResolution();

	/**
	 * @param size normalized size for 72 DPI resolution
	 * @return scaled and rounded size
	 * @since 0.0.0
	 */
	public static int scale(float size) {
		return round(scaleFloat(size));
	}

	/**
	 * @param size normalized size for 72 DPI resolution
	 * @return scaled size
	 * @since 0.0.0
	 */
	public static float scaleFloat(float size) {
		return size * RESOLUTION / BASE_RESOLUTION;
	}

	/**
	 * Measures and returns the given text bounds, using the given graphics context.
	 *
	 * @param graphics graphics in which the text will be drawn
	 * @param text text to measure
	 * @param x left position of the text to measure
	 * @param y bottom position of the text to measure
	 * @param margin bounds margin in pixels
	 * @return text bounds including margin
	 * @since 0.0.0
	 */
	public static Rectangle getTextBounds(
			Graphics2D graphics,
			String text,
			float x, float y, int margin) {

		var font = graphics.getFont();
		var renderContext = graphics.getFontRenderContext();
		var glyphVector = font.createGlyphVector(renderContext, text);
		var bounds = glyphVector.getPixelBounds(null, x, y);

		bounds.x -= margin;
		bounds.y -= margin;
		bounds.width += 2 * margin;
		bounds.height += 2 * margin;

		return bounds;
	}

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 *
	 * @since 0.0.0
	 */
	private SwingUtility() {

	}
}
