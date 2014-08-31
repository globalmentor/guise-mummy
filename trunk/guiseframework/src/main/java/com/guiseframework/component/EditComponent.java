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

import com.guiseframework.event.EditListenable;

/**
 * A component that has content that can be edited.
 * @author Garret Wilson
 */
public interface EditComponent extends Component, EditListenable {

	/** The editable bound property. */
	public static final String EDITABLE_PROPERTY = getPropertyName(EditComponent.class, "editable");

	/** @return Whether the value is editable and the component will allow the the user to change the value. */
	public boolean isEditable();

	/**
	 * Sets whether the value is editable and the component will allow the the user to change the value. This is a bound property of type {@link Boolean}.
	 * @param newEditable <code>true</code> if the component should allow the user to change the value.
	 * @see #EDITABLE_PROPERTY
	 */
	public void setEditable(final boolean newEditable);

}
