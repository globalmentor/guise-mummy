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

package com.guiseframework.model;

import static com.globalmentor.java.Classes.*;

import com.globalmentor.beans.PropertyBindable;

/**
 * An object that can be enabled or disabled.
 * @author Garret Wilson
 */
public interface Enableable extends PropertyBindable {

	/** The enabled bound property. */
	public static final String ENABLED_PROPERTY = getPropertyName(Enableable.class, "enabled");

	/** @return Whether the object is enabled and can receive user input. */
	public boolean isEnabled();

	/**
	 * Sets whether the object is enabled and can receive user input. This is a bound property of type <code>Boolean</code>.
	 * @param newEnabled <code>true</code> if the object should indicate and accept user input.
	 * @see #ENABLED_PROPERTY
	 */
	public void setEnabled(final boolean newEnabled);
}
