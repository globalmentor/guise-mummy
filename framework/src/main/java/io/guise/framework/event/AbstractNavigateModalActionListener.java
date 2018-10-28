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

package io.guise.framework.event;

import java.net.URI;

import static java.util.Objects.*;

import static com.globalmentor.net.URIs.*;

/**
 * A abstract object that listens for action events and keeps information for modally navigating in response.
 * @author Garret Wilson
 */
public class AbstractNavigateModalActionListener extends AbstractNavigateActionListener {

	/** The listener to respond to the end of modal interaction. */
	private final ModalNavigationListener modalListener;

	/** @return The listener to respond to the end of modal interaction. */
	public final ModalNavigationListener getModelListener() {
		return getModelListener();
	}

	/**
	 * Constructs a listener to navigate modally to the provided path.
	 * @param navigationPath A path that is either relative to the application context path or is absolute.
	 * @param modalListener The listener to respond to the end of modal interaction.
	 * @throws NullPointerException if the given path and/or modal listener is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case
	 *           {@link #AbstractNavigateModalActionListener(URI, ModalNavigationListener)} should be used instead).
	 */
	public AbstractNavigateModalActionListener(final String navigationPath, final ModalNavigationListener modalListener) {
		this(createPathURI(navigationPath), modalListener); //create a URI from the path and construct the class
	}

	/**
	 * Constructs a listener to navigate modally to the provided URI.
	 * @param navigationURI The URI for navigation when the action occurs.
	 * @param modalListener The listener to respond to the end of modal interaction.
	 * @throws NullPointerException if the given navigation URI and/or modal listener is null.
	 */
	public AbstractNavigateModalActionListener(final URI navigationURI, final ModalNavigationListener modalListener) {
		super(navigationURI); //construct the parent class
		this.modalListener = requireNonNull(modalListener, "Modal listeners cannot be null");
	}

}
