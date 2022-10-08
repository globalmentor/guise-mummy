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

package io.guise.framework.platform.web;

import java.io.IOException;
import java.net.URI;

import com.globalmentor.net.MediaType;
import com.globalmentor.net.HTTP;

import io.guise.framework.GuiseSession;
import io.guise.framework.component.Flash;

import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.net.URIs.*;

/**
 * Strategy for rendering a Flash component as an XHTML <code>&lt;object&gt;</code> element.
 * @param <C> The type of component being depicted.
 * @author Garret Wilson
 */
public class WebFlashDepictor<C extends Flash> extends AbstractSimpleWebComponentDepictor<C> { //TODO fix to work with new Microsoft activation process; see http://www.jeroenwijering.com/?item=embedding_flash and http://blog.deconcept.com/swfobject/

	/** The media type for Flash objects. */
	public static final MediaType FLASH_MEDIA_TYPE = MediaType.of(MediaType.APPLICATION_PRIMARY_TYPE, "x-shockwave-flash"); //TODO move to Flash class

	/**
	 * The "allowScriptAccess" parameter.
	 * @see <a href="http://www.adobe.com/devnet/flash/articles/fplayer8_security_09.html">Security Changes in Flash Player 8</a>
	 */
	public static final String ALLOW_SCRIPT_ACCESS_PARAMETER = "allowScriptAccess";
	/** The "allowScriptAccess" parameter "always" value. */
	public static final String ALLOW_SCRIPT_ACCESS_PARAMETER_ALWAYS = "always";
	/** The "allowScriptAccess" parameter "never" value. */
	public static final String ALLOW_SCRIPT_ACCESS_PARAMETER_NEVER = "never";
	/** The "allowScriptAccess" parameter "sameDomain" value. */
	public static final String ALLOW_SCRIPT_ACCESS_PARAMETER_SAME_DOMAIN = "sameDomain";
	/** The "movie" parameter. */
	public static final String MOVIE_PARAMETER = "movie";
	/** The "quality" parameter. */
	public static final String QUALITY_PARAMETER = "quality";
	/** The "quality" parameter "high" value. */
	public static final String QUALITY_PARAMETER_HIGH = "high";
	/** The "wmode" parameter. */
	public static final String WMODE_PARAMETER = "wmode";

	/** The Shockwave Flash player class ID. */
	public static final String FLASH_CLASS_ID = "clsid:D27CDB6E-AE6D-11cf-96B8-444553540000";

	/** The URI to the swflash.cab file. */
	public static final URI SWFLASH_CAB_URI = URI.create("http://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab");
	/** The HTTPS URI to the swflash.cab file. */
	public static final URI SWFLASH_CAB_SECURE_URI = URI.create("https://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab");
	/** The URI parameter specifying the version of the swflash.cab file to retrieve. */
	public static final String SWFLASH_CAB_URI_VERSION_PARAMETER = "version";

	/**
	 * Returns A URI appropriate for accessing the swflash.cab file at an optionally secure location with the given version.
	 * @param version The version of the file to retrieve.
	 * @param secure Whether a secure URI should be retrieved.
	 * @return A URI to the swflash.cab file.
	 */
	public static URI getSWFlashCabURI(final String version, final boolean secure) {
		return appendQueryParameter(secure ? SWFLASH_CAB_SECURE_URI : SWFLASH_CAB_URI, SWFLASH_CAB_URI_VERSION_PARAMETER, version);
	}

	/** Default constructor using the XHTML <code>&lt;object&gt;</code> element. */
	public WebFlashDepictor() {
		super(XHTML_NAMESPACE_URI, ELEMENT_OBJECT); //represent <xhtml:object>
	}

	@Override
	protected void depictBody() throws IOException {
		super.depictBody(); //render the default main part of the component
		final WebDepictContext depictContext = getDepictContext(); //get the depict context
		final GuiseSession session = getSession(); //get the session
		final C component = getDepictedObject(); //get the component
		final URI flashURI = component.getFlashURI(); //get the Flash URI
		final URI flashDepictURI = flashURI != null ? depictContext.getDepictionURI(flashURI) : null; //the Flash depict URI, if any
		if(getPlatform().getClientProduct().getBrand() == WebUserAgentProduct.Brand.INTERNET_EXPLORER) { //if the user agent is IE, use the special attributes
			depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_CLASSID, FLASH_CLASS_ID); //classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000"; only write the classid attributes for IE, because it will prevent the Flash from being loaded in Firefox
			//create a codebase URI in the form "http[s]://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0", making sure we use the same scheme so that IE6 won't complain if a secure page references a non-secure codebase
			depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_CODEBASE,
					getSWFlashCabURI("6,0,40,0", HTTP.HTTPS_URI_SCHEME.equals(depictContext.getDepictionURI().getScheme())).toString()); //codebase="http[s]://fpdownload.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,40,0" TODO allow version to be specified
		} else { //if the user agent is not IE, specify the object content type
			depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_TYPE, FLASH_MEDIA_TYPE.toString()); //type="application/x-shockwave-flash"; don't write the type Type attribute in IE, because this will prevent IE from loading the Flash movie 			
			if(flashDepictURI != null) { //if there is a flash URI
				depictContext.writeAttribute(null, ELEMENT_OBJECT_ATTRIBUTE_DATA, flashDepictURI.toString()); //data="flashURI"; don't write the data attribute in IE, because it will prevent the Flash movie from showing its preloader
			}
		}
		if(flashDepictURI != null) { //if there is a flash URI
			//param movie="flashURI" (necessary for IE)
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_PARAM); //<xhtml:param>
			depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_NAME, MOVIE_PARAMETER); //name="movie"
			depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_VALUE, flashDepictURI.toString()); //value="flashURI"
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_PARAM); //</xhtml:param>
		}
		//param quality="high" TODO allow customization
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_PARAM); //<xhtml:param>
		depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_NAME, QUALITY_PARAMETER); //name="quality"
		depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_VALUE, QUALITY_PARAMETER_HIGH); //value="high"
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_PARAM); //</xhtml:param>		
		//param wmode="opaque" TODO allow customization
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_PARAM); //<xhtml:param>
		depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_NAME, WMODE_PARAMETER); //name="wmode"
		depictContext.writeAttribute(null, ELEMENT_PARAM_ATTRIBUTE_VALUE, "opaque"); //value="opaque"
		depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_PARAM); //</xhtml:param>		
	}
}
