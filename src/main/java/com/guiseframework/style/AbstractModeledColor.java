/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.style;

import java.util.Arrays;

import com.globalmentor.text.ArgumentSyntaxException;

/**
 * Abstract representation of a color value in a color space.
 * @param <C> The type of color component for this color space.
 * @author Garret Wilson
 * @see <a href="http://www.neuro.sfc.keio.ac.jp/~aly/polygon/info/color-space-faq.html">Color Space FAQ</a>
 * @see <a href="http://www.color.org/">International Color Consortium</a>
 */
public abstract class AbstractModeledColor<C extends Enum<C> & ModeledColor.Component> implements ModeledColor<C> {

	/** The color component values, each within the range (0.0-1.0). */
	private final double[] values;

	/** @return The color component values, each within the range (0.0-1.0). */
	public double[] getValues() {
		return values.clone();
	}

	/** The precalculated hash code of the color. */
	private final int hashCode;

	/**
	 * Constructs a color with the given components.
	 * @param values The values of components of the color in the correct color space, each within the range (0.0-1.0), in the order of the component ordinals.
	 * @throws NullPointerException if the components is <code>null</code>. TODO fix @throws IllegalArgumentException if the number of component values do not
	 *           equal the number of components.
	 */
	public AbstractModeledColor(final double... values) {
		this.values = new double[values.length]; //create a new array of values
		for(int i = values.length - 1; i >= 0; --i) { //for each value
			this.values[i] = checkComponentValue(values[i]); //check and store this color component
		}
		this.hashCode = Arrays.hashCode(this.values); //precalculate the hash code
	}

	/**
	 * Checks the range of a given color component.
	 * @param value The value to check.
	 * @return The checked value.
	 * @throws IllegalArgumentException if the given component is outside the range (0.0-1.0).
	 */
	protected static double checkComponentValue(final double value) {
		if(value < 0.0f || value > 1.0f) { //if this value is outside the color component range
			throw new IllegalArgumentException("Invalid color component value: " + value);
		}
		return value; //return the value, as it passed the test
	}

	@Override
	public double getComponent(final C component) {
		return values[component.ordinal()]; //look up the color component in the array
	}

	@Override
	public long getAbsoluteComponent(final C component, final int bitDepth) {
		return Math.round(getComponent(component) * ((1 << bitDepth) - 1)); //multiply the component value by the range of values at the given bit depth
	}

	@Override
	public int hashCode() {
		return hashCode; //return the precalculated hash code
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns whether the objects are of the same class with identical color component values.
	 * </p>
	 */
	@Override
	public boolean equals(final Object object) {
		return object != null && getClass().equals(object.getClass()) && Arrays.equals(values, ((AbstractModeledColor<?>)object).values); //see if the classes and the component values are the same
	}

	/**
	 * Creates a color from a string representation. This representation can be in one of the following forms:
	 * <ul>
	 * <li><code><var>colorname</var></code>, one of the <a href="http://www.w3schools.com/html/html_colornames.asp">HTML color names</a>, which must be in all
	 * lowercase without delimiters, such as "aliceblue".</li>
	 * <li><code>#<var>rgb</var></code>, with hexadecimal representation of RGB color components without regard to case.</li>
	 * <li><code>#<var>rrggbb</var></code>, with hexadecimal representation of RGB color components without regard to case.</li>
	 * <li><code>rgb(<var>red</var>,<var>green</var>,<var>blue</var>)</code>, with decimal representation with a depth of eight bits (0-255).</li>
	 * <li><code>rgb(<var>red</var>%,<var>green</var>%,<var>blue</var>%)</code>, with decimal component values multiplied by 100 (0.0-100.0%).</li>
	 * <li><code>rgba(<var>red</var>,<var>green</var>,<var>blue</var>,<var>alpha</var>)</code>, with decimal representation with a depth of eight bits
	 * (0-255).</li>
	 * <li><code>rgba(<var>red</var>%,<var>green</var>%,<var>blue</var>%,<var>alpha</var>%)</code>, with decimal component values multiplied by 100 (0.0%-100.0%).
	 * </li>
	 * <li><code>hsl(<var>hue</var>,<var>saturation</var>,<var>lightness</var>)</code>, with decimal representation with a depth of eight bits (0-255).</li>
	 * <li><code>hsl(<var>hue</var>%,<var>saturation</var>%,<var>lightness</var>%)</code>, with decimal component values multiplied by 100 (0.0-100.0%).</li>
	 * </ul>
	 * This method also recognizes the <code>transparent</code> color name as equivalent to <code>rgba(0, 0, 0, 0)</code>, or black with zero alpha.
	 * @param charSequence The character sequence representation of a color.
	 * @return A color object representing the color represented by the given string.
	 * @throws NullPointerException if the given string is <code>null</code>.
	 * @throws IllegalArgumentException if a color cannot be determined from the given string.
	 * @see HSLColor#valueOf(CharSequence)
	 * @see RGBColor#valueOf(CharSequence)
	 */
	public static Color valueOf(final CharSequence charSequence) {
		try {
			return RGBColor.valueOf(charSequence); //try to return an RGB color (perhaps the most common representation)
		} catch(final IllegalArgumentException illegalArgumentException) { //if we couldn't determine an RGB color
			try {
				return HSLColor.valueOf(charSequence); //try to return an HSL color
			} catch(final IllegalArgumentException illegalArgumentException2) { //if we couldn't determine an HSL color
				throw new ArgumentSyntaxException("Character sequence " + charSequence + " does not represent a known color.");
			}
		}
	}
}
