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

package io.guise.framework.component;

import static com.globalmentor.java.Classes.*;

import io.guise.framework.model.ActionModel;

/**
 * A general control with an action model.
 * @author Garret Wilson
 */
public interface ActionControl extends Control, ActionModel {

	/** The bound property of the rollover state. */
	public static final String ROLLOVER_PROPERTY = getPropertyName(ActionControl.class, "rollover");

	/** @return Whether the component is in a rollover state. */
	public boolean isRollover();

	/**
	 * Sets whether the component is in a rollover state. This is a bound property of type <code>Boolean</code>.
	 * @param newRollover <code>true</code> if the component should be in a rollover state, else <code>false</code>.
	 * @see #ROLLOVER_PROPERTY
	 */
	public void setRollover(final boolean newRollover);

}
