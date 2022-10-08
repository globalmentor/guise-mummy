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
 * The style of a line, such as used for a border or divider.
 * @author Garret Wilson
 * @see <a href="http://www.w3.org/TR/css3-background/#border-style">CSS3 Backgrounds and Borders Module: border-style</a>
 */
public enum LineStyle implements Identifier {
	/** A series of dots. */
	DOTTED,
	/** A series of dashes. */
	DASHED,
	/** A single line segment. */
	SOLID,
	/** Two parallel solid lines with some spaces between them, the lines together with the space equalling any line width specified. */
	DOUBLE,
	/** Alternating dots and dashes. */
	DOT_DASH,
	/** Two dots and a dash. */
	DOT_DOT_DASH,
	/** A wavy line. */
	WAVE,
	/** Effect emulating carving on a canvas; typically achieved by using two colors slightly lighter and darker than the line color. */
	GROOVE,
	/** Effect simulating a line coming out of the canvas. */
	RIDGE,
	/** Effect simulating a sunken canvas. */
	INSET,
	/** Effect simulating a raised canvas. */
}
