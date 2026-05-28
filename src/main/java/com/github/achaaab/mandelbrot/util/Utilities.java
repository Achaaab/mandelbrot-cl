package com.github.achaaab.mandelbrot.util;

import java.time.Duration;

import static java.time.Instant.now;

/**
 * Miscellaneous utility methods.
 *
 * @author Jonathan Guéhenneux
 * @since 0.0.1
 */
public final class Utilities {

	/**
	 * Runs a given runnable and measures its running time.
	 *
	 * @param runnable runnable to run
	 * @return measured running time
	 * @since 0.0.1
	 */
	public static Duration time(Runnable runnable) {

		var start = now();
		runnable.run();
		var end = now();

		return Duration.between(start, end);
	}

	/**
	 * Pads the specified string with specified padding character.
	 * If the alignment is {@link Alignment#CENTER} and there is an odd number of padding characters to add,
	 * there is 1 more padding character after the specified string than before.
	 *
	 * @param string string to pad
	 * @param length desired length
	 * @param paddingCharacter padding character
	 * @param alignment alignment of the specified string within the padded string, supported values are:
	 * <ul>
	 *   <li>{@link Alignment#LEFT}</li>
	 *   <li>{@link Alignment#CENTER}</li>
	 *   <li>{@link Alignment#RIGHT}</li>
	 * </ul>
	 * @return padded string
	 * @throws IllegalArgumentException if padded is needed to reach specified length,
	 * but specified alignment is not supported
	 * @since 0.0.1
	 */
	public static String pad(String string, int length, char paddingCharacter, Alignment alignment) {

		String paddedString;

		var stringLength = string.length();

		if (stringLength < length) {

			var paddingString = Character.toString(paddingCharacter);

			var leftPaddingLength = switch (alignment) {

				case LEFT -> 0;
				case CENTER -> (length - stringLength) / 2;
				case RIGHT -> length - stringLength;

				default -> throw new IllegalArgumentException("unsupported alignment: " + alignment);
			};

			var rightPaddingLength = length - stringLength - leftPaddingLength;

			paddedString = paddingString.repeat(leftPaddingLength) +
					string +
					paddingString.repeat(rightPaddingLength);

		} else if (stringLength == length) {

			paddedString = string;

		} else {

			paddedString = string.substring(0, length);
		}

		return paddedString;
	}
}
