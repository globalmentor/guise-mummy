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

package com.guiseframework.event;

import java.net.URI;

/**
 * A object that listens for action events and in response modally changes the navigation. This class if declared final because it encapsulates a set of known,
 * bounded functionality that may be deferred to the client if possible.
 * @author Garret Wilson
 */
public final class NavigateModalActionListener extends AbstractNavigateModalActionListener {

	/**
	 * Constructs a listener to navigate modally to the provided path.
	 * @param navigationPath A path that is either relative to the application context path or is absolute.
	 * @param modalListener The listener to respond to the end of modal interaction.
	 * @throws NullPointerException if the given path and/or modal listener is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case
	 *           {@link #NavigateModalActionListener(URI, ModalNavigationListener)}</code> should be used instead).
	 */
	public NavigateModalActionListener(final String navigationPath, final ModalNavigationListener modalListener) {
		super(navigationPath, modalListener); //construct the parent class
	}

	/**
	 * Constructs a listener to navigate modally to the provided URI.
	 * @param navigationURI The URI for navigation when the action occurs.
	 * @param modalListener The listener to respond to the end of modal interaction.
	 * @throws NullPointerException if the given navigation URI and/or modal listener is null.
	 */
	public NavigateModalActionListener(final URI navigationURI, final ModalNavigationListener modalListener) {
		super(navigationURI, modalListener); //construct the parent class
	}

	/**
	 * Called when an action is initiated. This implementation requests modal navigation from the session.
	 * @param actionEvent The event indicating the source of the action.
	 */
	public void actionPerformed(final ActionEvent actionEvent) {
		actionEvent.getSession().navigateModal(getNavigationURI(), getModelListener()); //request that the session navigate modally to the configured URI
	}

}
