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

package com.guiseframework.converter;

import java.net.URI;

import static com.globalmentor.net.URIs.*;
import static com.globalmentor.java.Objects.*;

/**
 * A URI converter that resolves relative URIs to some base URI.
 * @author Garret Wilson
 * @see URI
 */
public class BaseURIStringLiteralConverter extends AbstractURIStringLiteralConverter {

	/** The base URI for resolving any relative URI. */
	private final URI baseURI;

	/** @return The base URI for resolving any relative URI. */
	public URI getBaseURI() {
		return baseURI;
	}

	/**
	 * Base URI constructor.
	 * @param baseURI The base URI for resolving any relative URI.
	 * @throws NullPointerException if the given base URI is <code>null</code>.
	 */
	public BaseURIStringLiteralConverter(final URI baseURI) {
		this.baseURI = checkInstance(baseURI, "Base URI cannot be null.");
	}

	/**
	 * Resolves a converted URI if needed. If the URI is already absolute, no action occurs. If the URI is relative, this implementation resolves the URI against
	 * the URI returned by {@link #getBaseURI()}.
	 * @param uri The URI to resolve.
	 * @return The URI resolved as needed and as appropriate.
	 * @see #getBaseURI()
	 */
	protected URI resolveURI(URI uri) {
		return uri.isAbsolute() ? uri : resolve(getBaseURI(), uri); //if the URI is relative, resolve it against the base URI
	}
}
