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

/**
 * Representation of a color through use of a color color space model.
 * @param <C> The type of color component for this color space.
 * @author Garret Wilson
 * @see <a href="http://www.neuro.sfc.keio.ac.jp/~aly/polygon/info/color-space-faq.html">Color Space FAQ</a>
 * @see <a href="http://www.color.org/">International Color Consortium</a>
 */
public interface ModeledColor<C extends Enum<C> & ModeledColor.Component> extends Color {

	/** The color component used in the color space. */
	public interface Component {
	}

	/**
	 * Determines the value of the given color component.
	 * @param component The color component for which a value should be retrieved.
	 * @return The value of the requested color component.
	 */
	public double getComponent(final C component);

	/**
	 * Determines the absolute value of the given color component with the given bit depth. For example, retrieving a component with value 0.5 and a bit depth of
	 * 16 would produce 128 or 0x80.
	 * @param component The color component for which a value should be retrieved.
	 * @param bitDepth The number of bits to use for the given color component.
	 * @return The absolute value of the requested color component at the given bit depth.
	 * @see #getComponent(Enum)
	 */
	public long getAbsoluteComponent(final C component, final int bitDepth);
}
