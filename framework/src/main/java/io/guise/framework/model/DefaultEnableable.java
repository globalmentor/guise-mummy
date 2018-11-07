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

package io.guise.framework.model;

import com.globalmentor.beans.BoundPropertyObject;

/**
 * A default implementation of an object that can be enabled or disabled.
 * @author Garret Wilson
 */
public class DefaultEnableable extends BoundPropertyObject implements Enableable {

	/** Whether the control is enabled and can receive user input. */
	private boolean enabled = true;

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(final boolean newEnabled) {
		if(enabled != newEnabled) { //if the value is really changing
			final boolean oldEnabled = enabled; //get the old value
			enabled = newEnabled; //actually change the value
			firePropertyChange(ENABLED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newEnabled)); //indicate that the value changed
		}
	}
}
