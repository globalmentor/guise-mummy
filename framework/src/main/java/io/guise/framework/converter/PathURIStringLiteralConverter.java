/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.converter;

import java.net.URI;

import com.globalmentor.net.URIs;
import static com.globalmentor.net.URIs.*;

/**
 * A URI converter that interprets relative URIs as path URIs with the {@value URIs#PATH_SCHEME} scheme.
 * @author Garret Wilson
 * @see URI
 */
public class PathURIStringLiteralConverter extends AbstractURIStringLiteralConverter {

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the URI is relative, this implementation creates a URI with the {@value URIs#PATH_SCHEME} scheme.
	 * </p>
	 */
	@Override
	protected URI resolveURI(URI uri) {
		return uri.isAbsolute() ? uri : createURI(PATH_SCHEME, uri.toString()); //if the URI is relative, create a URI with the path: scheme
	}
}
