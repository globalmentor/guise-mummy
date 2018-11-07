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

package io.guise.framework;

import java.util.regex.Pattern;

import static java.util.Objects.*;

import com.globalmentor.net.URIPath;

/**
 * Abstract implementation of a destination referencing another destination.
 * @author Garret Wilson
 */
public abstract class AbstractReferenceDestination extends AbstractDestination implements ReferenceDestination {

	/** The referenced destination. */
	private final Destination destination;

	@Override
	public Destination getDestination() {
		return destination;
	}

	/**
	 * Path and referenced destination constructor.
	 * @param path The application context-relative path within the Guise container context, which does not begin with '/'.
	 * @param destination The referenced destination.
	 * @throws NullPointerException if the path and/or destination is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path is absolute.
	 */
	public AbstractReferenceDestination(final URIPath path, final Destination destination) {
		super(path); //construct the parent class
		this.destination = requireNonNull(destination, "Destination cannot be null.");
	}

	/**
	 * Path pattern and referenced destination constructor.
	 * @param pathPattern The pattern to match an application context-relative path within the Guise container context, which does not begin with '/'.
	 * @param destination The referenced destination.
	 * @throws NullPointerException if the path pattern and/or destination is <code>null</code>.
	 */
	public AbstractReferenceDestination(final Pattern pathPattern, final Destination destination) {
		super(pathPattern); //construct the parent class
		this.destination = requireNonNull(destination, "Destination cannot be null.");
	}
}
