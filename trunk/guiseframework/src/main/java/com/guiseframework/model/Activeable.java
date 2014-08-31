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

/**
 * An object that can be set active or inactive.
 * @author Garret Wilson
 */
public interface Activeable {

	/** The active bound property. */
	public static final String ACTIVE_PROPERTY = getPropertyName(Activeable.class, "active");

	/** @return Whether the object is active. */
	public boolean isActive();

	/**
	 * Sets whether the object is active. This is a bound property of type <code>Boolean</code>.
	 * @param newActive <code>true</code> if the object should be active.
	 * @see #ACTIVE_PROPERTY
	 */
	public void setActive(final boolean newActive);
}
