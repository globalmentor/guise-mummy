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

package io.guise.framework.component;

import io.guise.framework.model.*;

/**
 * A heading component. This component installs a default export strategy supporting export of the following content types:
 * <ul>
 * <li>The label content type.</li>
 * </ul>
 * @author Garret Wilson
 */
public class Heading extends AbstractLabel implements HeadingComponent {

	/** The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified. */
	private int level;

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public void setLevel(final int newLevel) {
		if(level != newLevel) { //if the value is really changing
			final int oldLevel = level; //get the old value
			level = newLevel; //actually change the value
			firePropertyChange(LEVEL_PROPERTY, oldLevel, newLevel); //indicate that the value changed
		}
	}

	/** Default constructor with a default info model. */
	public Heading() {
		this(NO_HEADING_LEVEL); //construct the class with a default model with no heading level
	}

	/**
	 * Heading level constructor with a default info model.
	 * @param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	 */
	public Heading(final int level) {
		this(new DefaultInfoModel(), level); //construct the class with a default model
	}

	/**
	 * Label constructor.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 */
	public Heading(final String label) {
		this(new DefaultInfoModel(label)); //construct the heading with a default info model and the given label text
	}

	/**
	 * Info model constructor.
	 * @param infoModel The component info model.
	 * @throws NullPointerException if the given info model is <code>null</code>.
	 */
	public Heading(final InfoModel infoModel) {
		this(infoModel, NO_HEADING_LEVEL); //construct the class with no heading level
	}

	/**
	 * Label and level constructor.
	 * @param label The text of the label, or <code>null</code> if there should be no label.
	 * @param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	 */
	public Heading(final String label, final int level) {
		this(new DefaultInfoModel(label), level); //construct the heading with a default info model and the given label text
	}

	/**
	 * Info model and level constructor.
	 * @param infoModel The component info model.
	 * @param level The zero-based level of the heading, or {@link HeadingComponent#NO_HEADING_LEVEL} if no level is specified.
	 * @throws NullPointerException if the given session info model is <code>null</code>.
	 */
	public Heading(final InfoModel infoModel, final int level) {
		super(infoModel); //construct the parent class
		this.level = level; //save the level
	}
}
