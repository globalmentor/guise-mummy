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

package com.guiseframework.component.layout;

import com.guiseframework.model.*;

/**
 * Constraints on a component in a container control. Each component can be specified as bing displayed and/or enabled.
 * @author Garret Wilson
 */
public class ControlConstraints extends AbstractConstraints implements Displayable, Enableable {

	/** Whether the component is displayed or has no representation, taking up no space. */
	private boolean displayed = true;

	/** @return Whether the component is displayed or has no representation, taking up no space. */
	public boolean isDisplayed() {
		return displayed;
	}

	/**
	 * Sets whether the component is displayed or has no representation, taking up no space. This is a bound property of type <code>Boolean</code>.
	 * @param newDisplayed <code>true</code> if the component should be displayed, else <code>false</code> if the component should take up no space.
	 * @see #DISPLAYED_PROPERTY
	 */
	public void setDisplayed(final boolean newDisplayed) {
		if(displayed != newDisplayed) { //if the value is really changing
			final boolean oldDisplayed = displayed; //get the current value
			displayed = newDisplayed; //update the value
			firePropertyChange(DISPLAYED_PROPERTY, Boolean.valueOf(oldDisplayed), Boolean.valueOf(newDisplayed));
		}
	}

	/** Whether the component is enabled. */
	private boolean enabled = true;

	/** @return Whether the component is enabled. */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets whether the the component is enabled. This is a bound property of type <code>Boolean</code>.
	 * @param newEnabled <code>true</code> if the corresponding component is enabled.
	 * @see #ENABLED_PROPERTY
	 */
	public void setEnabled(final boolean newEnabled) {
		if(enabled != newEnabled) { //if the value is really changing
			final boolean oldEnabled = enabled; //get the old value
			enabled = newEnabled; //actually change the value
			firePropertyChange(ENABLED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newEnabled)); //indicate that the value changed
		}
	}

	/** Default constructor. */
	public ControlConstraints() {
		this(true); //construct the class with no label
	}

	/**
	 * Enabled constructor.
	 * @param enabled Whether the component is enabled.
	 */
	public ControlConstraints(final boolean enabled) {
		this.enabled = enabled; //save the enabled state
	}

}
