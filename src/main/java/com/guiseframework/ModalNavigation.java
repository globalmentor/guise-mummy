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

package com.guiseframework;

import java.net.URI;

import com.guiseframework.event.ModalNavigationListener;

import static com.globalmentor.java.Objects.*;

/**
 * The encapsulation of a point of modal navigation.
 * @author Garret Wilson
 */
public class ModalNavigation extends Navigation //TODO del class; if kept, update API to allow relative navigation paths
{

	/** The listener to respond to the end of modal interaction. */
	private final ModalNavigationListener modalListener;

	/** @return The listener to respond to the end of modal interaction. */
	public ModalNavigationListener getModalListener() {
		return modalListener;
	}

	/**
	 * Creates an object encapsulating a point of modal navigation.
	 * @param oldNavigationURI The old point of navigation, with an absolute path.
	 * @param newNavigationURI The new point of navigation, with an absolute path.
	 * @param modalListener The listener to respond to the end of modal interaction.
	 * @throws NullPointerException if one of the navigation URIs is <code>null</code>, or does not contain a path.
	 * @throws IllegalArgumentException if one of the given navigation URIs contains a relative path.
	 */
	public ModalNavigation(final URI oldNavigationURI, final URI newNavigationURI, final ModalNavigationListener modalListener) {
		super(oldNavigationURI, newNavigationURI); //construct the parent class
		this.modalListener = checkInstance(modalListener, "Modal listener cannot be null.");
	}
}
