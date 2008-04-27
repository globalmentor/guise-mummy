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

import java.net.URI;
import java.util.Queue;

import javax.mail.internet.ContentType;

import static com.globalmentor.io.ContentTypes.*;
import static com.globalmentor.text.xml.XML.*;

import com.globalmentor.net.URIPath;
import com.guiseframework.GuiseApplication;
import com.guiseframework.platform.*;

/**The web platform for Guise.
@author Garret Wilson
*/
public interface WebPlatform extends Platform
{

	/**The namespace of the Guise markup language to be used with XHTML.*/
	public final static URI GUISE_ML_NAMESPACE_URI=URI.create("http://guiseframework.com/id/ml#");
	/**The standard prefix to use with the Guise markup language namespace.*/
	public final static String GUISE_ML_NAMESPACE_PREFIX="guise";

	/**The public ID of the Guise XHTML DTD.*/
	public final static String GUISE_XHTML_DTD_PUBLIC_ID="-//Guise//DTD XHTML Guise 1.0//EN";	

	/**The URI query parameter used to specify a Guise session by UUID, overriding the Guise session based upon the HTTP session identified by the request.*/ 
	public final static String GUISE_SESSION_UUID_URI_QUERY_PARAMETER="guiseSessionUUID";
	/**The URI query parameter used to indicate the Guise version to prevent caching between versions.*/ 
	public final static String GUISE_VERSION_URI_QUERY_PARAMETER="guiseVersion";

	/**The content type of a Guise AJAX request, <code>application/x-guise-ajax-request</code>.*/
	public final static ContentType GUISE_AJAX_REQUEST_CONTENT_TYPE=getContentTypeInstance(APPLICATION_PRIMARY_TYPE, SUBTYPE_EXTENSION_PREFIX+"guise-ajax-request"+SUBTYPE_SUFFIX_DELIMITER_CHAR+XML_SUBTYPE_SUFFIX);

	/**The content type of a Guise AJAX response, <code>application/x-guise-ajax-response</code>.*/
	public final static ContentType GUISE_AJAX_RESPONSE_CONTENT_TYPE=getContentTypeInstance(APPLICATION_PRIMARY_TYPE, SUBTYPE_EXTENSION_PREFIX+"guise-ajax-response"+SUBTYPE_SUFFIX_DELIMITER_CHAR+XML_SUBTYPE_SUFFIX);

		//Guise-specific element attributes
			//img
	/**The Guise image attribute indicating the original source location of the image.*/
	public final static String ELEMENT_IMG_ATTRIBUTE_ORIGINAL_SRC="originalSrc";
	/**The Guise image attribute indicating the source location of the image to be used for rollovers.*/
	public final static String ELEMENT_IMG_ATTRIBUTE_ROLLOVER_SRC="rolloverSrc";
			//textarea
	/**The Guise textarea attribute indicating whether the user should be allowed to enter multiple physical lines; value is "true" or "false".*/
	public final static String ELEMENT_TEXTAREA_ATTRIBUTE_MULTILINE="multiline";
			//all elements 
	/**The Guise attribute containing the hash of the element attributes.*/
	public final static String ATTRIBUTE_ATTRIBUTE_HASH="attributeHash";
	/**The Guise attribute containing the hash of the element content.*/
	public final static String ATTRIBUTE_CONTENT_HASH="contentHash";
	/**The Guise attribute indicating the content type of an element's contents.*/
	public final static String ATTRIBUTE_CONTENT_TYPE="contentType";
	/**The Guise attribute indicating the type of patching that should occur.*/
	public final static String ATTRIBUTE_PATCH_TYPE="patchType";
		/**The Guise attribute value indicating that no patching should occur on the value.*/
		public final static String ATTRIBUTE_PATCH_TYPE_NO_VALUE="novalue";
		/**The Guise attribute value indicating that no patching should occur.*/
		public final static String ATTRIBUTE_PATCH_TYPE_NONE="none";
		/**The Guise attribute value indicating that the element is a temporary element that will be replaced later; functions just like "none" for patching except that this element will be removed from the original hierarchy.*/
		public final static String ATTRIBUTE_PATCH_TYPE_TEMP="temp";

	/**The path of the blank MP3 file, relative to the application.*/
	public final static URIPath BLANK_MP3_PATH=GuiseApplication.GUISE_ASSETS_AUDIO_PATH.resolve("blank.mp3");	

	/**The path of the empty HTML document, relative to the application.*/
	public final static URIPath GUISE_EMPTY_HTML_DOCUMENT_PATH=GuiseApplication.GUISE_ASSETS_DOCUMENTS_PATH.resolve("empty.html");

	/**The path of the AJAX JavaScript file, relative to the application.*/
	public final static URIPath AJAX_JAVASCRIPT_PATH=GuiseApplication.GUISE_ASSETS_JAVASCRIPT_PATH.resolve("ajax.js");
	/**The path of the DOM JavaScript file, relative to the application.*/
	public final static URIPath DOM_JAVASCRIPT_PATH=GuiseApplication.GUISE_ASSETS_JAVASCRIPT_PATH.resolve("dom.js");
	/**The path of the Guise JavaScript file, relative to the application.*/
	public final static URIPath GUISE_JAVASCRIPT_PATH=GuiseApplication.GUISE_ASSETS_JAVASCRIPT_PATH.resolve("guise.js");
	/**The path of the JavaScript JavaScript file, relative to the application.*/
	public final static URIPath JAVASCRIPT_JAVASCRIPT_PATH=GuiseApplication.GUISE_ASSETS_JAVASCRIPT_PATH.resolve("javascript.js");

	/**The path of the Guise DTD, relative to the application.*/
	public final static URIPath GUISE_DTD_PATH=GuiseApplication.GUISE_ASSETS_DTD_PATH.resolve("guise.dtd");
	
	/**The path of the Guise Flash file, relative to the application.*/
	public final static URIPath GUISE_FLASH_PATH=GuiseApplication.GUISE_ASSETS_FLASH_PATH.resolve("guise.swf");

	/**The web commands for controlling polling.*/
	public enum PollCommand implements WebPlatformCommand
	{
		/**The command to set the polling interval.
		parameters: <code>{{@value #INTERVAL}:"<var>interval</var>"}</code>
		*/
		POLL_INTERVAL;

		/**The property for specifying the poll interval in milliseconds.*/
		public final static String INTERVAL_PROPERTY="interval";
	}

	/**Generates an ID for the given depicted object appropriate for using on the platform.
	@param depictID The depict ID to be converted to a platform ID.
	@return The form of the depict ID appropriate for using on the platform.
	*/
	public String getDepictIDString(final long depictID);

	/**Returns the depicted object ID represented by the given platform-specific ID string.
	@param depictIDString The platform-specific form of the depict ID.
	@param depictID The depict ID to be converted to a platform ID.
	@return The depict ID the platform-specific form represents.
	@exception NullPointerException if the given string is <code>null</code>.
	@exception IllegalArgumentException if the given string does not represent the correct string form of a depict ID on this platform.
	*/
	public long getDepictID(final String depictIDString);

	/**@return The user agent client, such as a browser, used to access Guise on this platform.*/
	public WebUserAgentProduct getClientProduct();

	/**Retrieves information and functionality related to the current depiction.
	@return A context for the current depiction.
	@exception IllegalStateException if no depict context can be returned in the current depiction state.
	*/
	public WebDepictContext getDepictContext();

	/**@return The thread-safe queue of messages to be delivered to the platform.*/
	public Queue<WebPlatformMessage> getSendMessageQueue();

	/**@return The current polling interval in milleseconds.*/
	public int getPollInterval();

	/**Sets the polling interval in millseconds.
	@param newPollInterval The polling interval in millseconds.
	@exception IllegalArgumentException if the given polling interval is less than zero.
	*/
	public void setPollInterval(final int newPollInterval);

	/**Requests a polling interval for a given depicted object.
	The actual polling interval will be updated if the given polling interval is smaller than the current actual polling interval.
	@param depictedObject The depicted object requesting a polling interval.
	@param pollInterval The polling interval in milleseconds.
	@return <code>true</code> if the polling interval changed as a result of this request.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	@exception IllegalArgumentException if the value is less than zero.
	@see #discontinuePollInterval(DepictedObject)
	@see #getPollInterval()
	@see #setPollInterval()
	*/ 
	public boolean requestPollInterval(final DepictedObject depictedObject, final int pollInterval);

	/**Indicates that a depicted object no longer requests a particular polling interval.
	The actual polling interval will be updated if the relinquished poll interval is less than or equal to the current poll interval.
	@param depictedObject The depicted object that is relinquishing a polling interval.
	@return <code>true</code> if the polling interval changed as a result of this relinquishment.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	@see #requestPollInterval(DepictedObject, int)
	@see #getPollInterval()
	@see #setPollInterval()
	*/ 
	public boolean discontinuePollInterval(final DepictedObject depictedObject);

}
