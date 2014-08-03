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

package com.guiseframework.component;

import static com.globalmentor.java.Classes.*;

/**
 * A component meant to be a heading. A heading provides a zero-based logical level of hierarchical nesting, with lower levels indicating headings of larger
 * groupings of information.
 * @author Garret Wilson
 */
public interface HeadingComponent extends Component {

	/** The heading level value indicating no heading level. */
	public final static int NO_HEADING_LEVEL = -1;

	/** The level bound property. */
	public final static String LEVEL_PROPERTY = getPropertyName(HeadingComponent.class, "level");

	/** @return The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified. */
	public int getLevel();

	/**
	 * Sets the level of the heading. This is a bound property of type <code>Integer</code>.
	 * @param newLevel The new zero-based heading level, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	 * @see HeadingComponent#LEVEL_PROPERTY
	 */
	public void setLevel(final int newLevel);

}
