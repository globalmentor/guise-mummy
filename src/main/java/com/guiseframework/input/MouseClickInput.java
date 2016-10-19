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

package com.guiseframework.input;

import com.globalmentor.java.Objects;

import static com.globalmentor.java.Objects.*;

/**
 * An encapsulation of mouse click input.
 * @author Garret Wilson
 */
public class MouseClickInput extends AbstractMouseInput {

	/** The button that was clicked. */
	private final MouseButton button;

	/** @return The button that was clicked. */
	public MouseButton getButton() {
		return button;
	}

	/** The number of clicks that were input (e.g. 1 for a single click, 2 for a double click, etc.). */
	private final int count;

	/** @return The number of clicks that were input (e.g. 1 for a single click, 2 for a double click, etc.). */
	public int getCount() {
		return count;
	}

	/**
	 * Button and keys constructor.
	 * @param button The button that was clicked.
	 * @param count The number of clicks that were input (e.g. 1 for a single click, 2 for a double click, etc.).
	 * @param keys The keys that were pressed when this input occurred.
	 * @throws NullPointerException if the given button and/or keys is <code>null</code>.
	 * @throws IllegalArgumentException if the given count is zero or less.
	 */
	public MouseClickInput(final MouseButton button, final int count, final Key... keys) {
		super(keys); //construct the parent class
		if(count <= 0) { //if the count is not positive
			throw new IllegalArgumentException("Mouse click count must be positive.");
		}
		this.count = count; //store the count
		this.button = checkInstance(button, "Button cannot be null."); //save the button
	}

	/**
	 * Returns the hash code of this object. This version extends the hash code of the underlying objects with the the button and count.
	 * @return The hash code of this object.
	 */
	public int hashCode() {
		return Objects.getHashCode(super.hashCode(), button, count); //extend the hash code with the button and count
	}

	/**
	 * Determines if this object equals another object. Besides the default checks, this version ensures that the buttons are equal.
	 * @param object The object to compare with this object.
	 * @return <code>true</code> if the given object is considered equal to this object.
	 */
	public boolean equals(final Object object) {
		if(super.equals(object)) { //if the default checks pass, the object is of the correct type
			final MouseClickInput mouseClickInput = (MouseClickInput)object; //cast theh object to the correc type
			return getButton() == mouseClickInput.getButton() && getCount() == mouseClickInput.getCount(); //compare buttons and counts
		} else { //if the default checks don't pass
			return false; //the objects don't match
		}
	}

	/** @return A string representation of this object. */
	public String toString() {
		return getButton().toString() + ' ' + getCount() + ' ' + super.toString(); //add the button and count representations to the string
	}
}
