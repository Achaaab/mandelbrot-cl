package com.github.achaaab.mandelbrot.util;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
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
	 * @param graphics
	 * @param text
	 * @param x
	 * @param y
	 * @return
	 * @since 0.0.0
	 */
	public static Rectangle getTextBounds(Graphics2D graphics, String text, float x, float y) {

		var font = graphics.getFont();
		var renderContext = graphics.getFontRenderContext();
		var glyphVector = font.createGlyphVector(renderContext, text);
		return glyphVector.getPixelBounds(null, x, y);
	}

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 *
	 * @since 0.0.0
	 */
	private SwingUtility() {

	}
}
