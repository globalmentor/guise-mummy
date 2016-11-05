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

package com.guiseframework.platform.web;

import java.util.*;

import static java.util.Objects.*;

import com.globalmentor.net.ContentType;

import static com.globalmentor.net.HTTP.*;

import com.guiseframework.platform.AbstractBrandedProduct;

/**
 * The default implementation of the identification of the user agent client, such as a browser, accessing Guise on the web platform.
 * @author Garret Wilson
 */
public class DefaultWebUserAgentProduct extends AbstractBrandedProduct<WebUserAgentProduct.Brand> implements WebUserAgentProduct {

	/** The content types accepted by the client. */
	private final Iterable<ContentType> acceptedContentTypes;

	@Override
	public Iterable<ContentType> getAcceptedContentTypes() {
		return acceptedContentTypes;
	}

	@Override
	public boolean isAcceptedContentType(final ContentType contentType) {
		return isAcceptedContentType(contentType, true); //check accepted content types, matching wildcards
	}

	@Override
	public boolean isAcceptedContentType(final ContentType contentType, final boolean matchWildcards) {
		final Iterator<ContentType> contentTypeIterator = getAcceptedContentTypes().iterator(); //get an iterator to the accepted content types
		if(!contentTypeIterator.hasNext()) { //if no content types are listed as being accepted, then everything is accepted
			return true; //this content type (and all other content types) is accepted
		}
		do { //for each content type
			final ContentType acceptedContentType = contentTypeIterator.next(); //get the next accepted content type
			if(matchWildcards || acceptedContentType.getBaseType().indexOf(WILDCARD_CHAR) < 0) { //only match wildcards if we were asked to
				if(contentType.hasBaseType(acceptedContentType)) { //if our content type matches an accepted content type (make sure we match to the accepted content type, which can have wildcards)
					return true; //show that we found a match
				}
				if("*/*".equals(acceptedContentType.getBaseType())) //if this is the wildcard content type TODO use a constant
				{
					return true;
				}
			}
		} while(contentTypeIterator.hasNext()); //keep looking until we run out of content types
		return false; //show that we didn't find an accepted content type
	}

	/** The languages accepted by the client. */
	private final Iterable<Locale> acceptedLanguages;

	@Override
	public Iterable<Locale> getClientAcceptedLanguages() {
		return acceptedLanguages;
	}

	/**
	 * ID, brand, name, and version constructor.
	 * @param id The identifying string of the product, or <code>null</code> if the ID is not known.
	 * @param brand The brand of the product, or <code>null</code> if the brand is not known.
	 * @param name The canonical name of the product, or <code>null</code> if the name is not known.
	 * @param version The version string provided by the product, or <code>null</code> if there is no string version of the product.
	 * @param versionNumber The version number provided by the product, or {@link Double#NaN} if there is no version number of the product.
	 * @param versionNumbers The version number components provided by the product, or <code>null</code> if there are no version number components of the product.
	 * @param acceptedContentTypes The content types accepted by the client.
	 * @param acceptedLanguages The languages accepted by the client.
	 * @throws NullPointerException if the given ID, name, accepted content types, and/or accepted languages is <code>null</code>.
	 */
	public DefaultWebUserAgentProduct(final String id, final WebUserAgentProduct.Brand brand, final String name, final String version,
			final double versionNumber, final int[] versionNumbers, final Iterable<ContentType> acceptedContentTypes, final Iterable<Locale> acceptedLanguages) {
		super(id, brand, name, version, versionNumber, versionNumbers); //construct the parent class
		this.acceptedContentTypes = requireNonNull(acceptedContentTypes, "Accepted content types cannot be null.");
		this.acceptedLanguages = requireNonNull(acceptedLanguages, "Accepted languages cannot be null.");
	}

}
