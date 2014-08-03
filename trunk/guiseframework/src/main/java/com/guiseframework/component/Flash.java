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

import java.net.URI;

import com.globalmentor.java.Objects;

/**
 * A component representing a Macromedia Flash object.
 * @author Garret Wilson
 */
public class Flash extends AbstractComponent {

	/** The flash URI bound property. */
	public final static String FLASH_URI_PROPERTY = getPropertyName(Flash.class, "flashURI");

	/** The Flash URI, which may be a resource URI, or <code>null</code> if there is no Flash URI. */
	private URI flashURI = null;

	/** @return The Flash URI, which may be a resource URI, or <code>null</code> if there is no Flash URI. */
	public URI getFlashURI() {
		return flashURI;
	}

	/**
	 * Sets the URI of the Flash. This is a bound property of type <code>URI</code>.
	 * @param newFlashURI The new URI of the Flash, which may be a resource URI.
	 * @see #FLASH_URI_PROPERTY
	 */
	public void setFlashURI(final URI newFlashURI) {
		if(!Objects.equals(flashURI, newFlashURI)) { //if the value is really changing
			final URI oldFlashURI = flashURI; //get the old value
			flashURI = newFlashURI; //actually change the value
			firePropertyChange(FLASH_URI_PROPERTY, oldFlashURI, newFlashURI); //indicate that the value changed
		}
	}

}
