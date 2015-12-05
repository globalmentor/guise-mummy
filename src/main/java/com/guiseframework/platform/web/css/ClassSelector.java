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
import static com.globalmentor.w3c.spec.CSS.*;

/**
 * A class simple selector.
 * @author Garret Wilson
 */
public class ClassSelector implements SimpleSelector, Comparable<ClassSelector> {

	/** The name of the class to be selected. */
	private final String className;

	/** @return The name of the class to be selected. */
	public String getClassName() {
		return className;
	}

	/**
	 * Class name constructor.
	 * @param className The name of the class to be selected.
	 * @throws NullPointerException if the given class name is <code>null</code>.
	 */
	public ClassSelector(final String className) {
		this.className = checkInstance(className, "Class name cannot be null.");
	}

	/** @return A hash code for this object. */
	public int hashCode() {
		return getClassName().hashCode();
	}

	/**
	 * Determines whether this object is equivalent to another object.
	 * @param object The object to compare with this object.
	 * @return <code>true</code> if this object is equivalent to the given object.
	 */
	public boolean equals(final Object object) {
		return object instanceof ClassSelector && getClassName().equals(((ClassSelector)object).getClassName());
	}

	/** @return A string representation of this object. */
	public String toString() {
		return new StringBuilder().append(CLASS_SELECTOR_DELIMITER).append(getClassName()).toString();
	}

	/**
	 * Compares this object with the specified object for order. This implementation compares class names. Returns a negative integer, zero, or a positive integer
	 * as this object is less than, equal to, or greater than the specified object.
	 * @param object The object to be compared.
	 * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	 */
	public int compareTo(final ClassSelector object) {
		return getClassName().compareTo(object.getClassName()); //compare class names
	}
}
