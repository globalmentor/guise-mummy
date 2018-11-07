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

package io.guise.framework.platform.web.css;

import static java.util.Objects.*;

import static com.globalmentor.w3c.spec.CSS.*;

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
		this.id = requireNonNull(id, "ID cannot be null.");
	}

	@Override
	public int hashCode() {
		return getID().hashCode();
	}

	@Override
	public boolean equals(final Object object) {
		return object instanceof IDSelector && getID().equals(((IDSelector)object).getID());
	}

	@Override
	public String toString() {
		return new StringBuilder().append(ID_SELECTOR_DELIMITER).append(getID()).toString();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation compares IDs. Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the
	 * specified object.
	 * </p>
	 */
	@Override
	public int compareTo(final IDSelector object) {
		return getID().compareTo(object.getID()); //compare IDs
	}
}
