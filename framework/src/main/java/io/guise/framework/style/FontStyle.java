/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.style;

import com.globalmentor.lex.Identifier;

/**
 * The style of a font.
 * @author Garret Wilson
 * @see <a href="http://www.w3.org/TR/REC-CSS2/fonts.html#font-styling">CSS 2 Fonts: Font Styling</a>
 */
public enum FontStyle implements Identifier {
	/** A font that is classified as "normal" in the font database. */
	NORMAL,
	/** A font that is classified as "oblique" in the font database, such as many fonts with names including the word "Oblique", "Slanted", or "Incline". */
	OBLIQUE,
	/** A font that is classified as "italic" in the font database, such as many fonts with names including the word "Italic", "Cursive", or "Kursiv". */
	ITALIC;
}
