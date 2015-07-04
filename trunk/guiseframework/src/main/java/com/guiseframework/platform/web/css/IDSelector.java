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

package com.guiseframework.platform.web.css;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.text.css.CSS.*;

/**
 * An ID simple selector.
 * @author Garret Wilson
 */
public class IDSelector implements SimpleSelector, Comparable<IDSelector> {

	/** The ID to be selected. */
	private final String id;

	/** @return The ID to be selected. */
	public String getID() {
		return id;
	}

	/**
	 * ID constructor.
	 * @param id The ID to be selected.
	 * @throws NullPointerException if the given ID is <code>null</code>.
	 */
	public IDSelector(final String id) {
		this.id = checkInstance(id, "ID cannot be null.");
	}

	/** @return A hash code for this object. */
	public int hashCode() {
		return getID().hashCode();
	}

	/**
	 * Determines whether this object is equivalent to another object.
	 * @param object The object to compare with this object.
	 * @return <code>true</code> if this object is equivalent to the given object.
	 */
	public boolean equals(final Object object) {
		return object instanceof IDSelector && getID().equals(((IDSelector)object).getID());
	}

	/** @return A string representation of this object. */
	public String toString() {
		return new StringBuilder().append(ID_SELECTOR_DELIMITER).append(getID()).toString();
	}

	/**
	 * Compares this object with the specified object for order. This implementation compares IDs. Returns a negative integer, zero, or a positive integer as this
	 * object is less than, equal to, or greater than the specified object.
	 * @param object The object to be compared.
	 * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	 */
	public int compareTo(final IDSelector object) {
		return getID().compareTo(object.getID()); //compare IDs
	}
}
