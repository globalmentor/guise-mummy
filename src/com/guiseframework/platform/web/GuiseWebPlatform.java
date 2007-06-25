package com.guiseframework.platform.web;

import java.net.URI;

import com.guiseframework.GuiseApplication;
import com.guiseframework.platform.GuisePlatform;

/**The web platform for Guise.
@author Garret Wilson
*/
public interface GuiseWebPlatform extends GuisePlatform
{

	/**The namespace of the Guise markup language to be used with XHTML.*/
	public final static URI GUISE_ML_NAMESPACE_URI=URI.create("http://guiseframework.com/id/ml#");
	/**The standard prefix to use with the Guise markup language namespace.*/
	public final static String GUISE_ML_NAMESPACE_PREFIX="guise";

		//Guise-specific elements
			//img
	/**The Guise image attribute indicating the original source location of the image.*/
	public final static String ELEMENT_IMG_ATTRIBUTE_ORIGINAL_SRC="originalSrc";
	/**The Guise image attribute indicating the source location of the image to be used for rollovers.*/
	public final static String ELEMENT_IMG_ATTRIBUTE_ROLLOVER_SRC="rolloverSrc";
			//all elements 
	/**The Guise attribute containing the hash of the element attributes.*/
	public final static String ATTRIBUTE_ATTRIBUTE_HASH="attributeHash";
	/**The Guise attribute containing the hash of the element content.*/
	public final static String ATTRIBUTE_CONTENT_HASH="contentHash";
	/**The Guise attribute containing commands related to a specific element.*/
	public final static String ATTRIBUTE_COMMANDS="commands";

	/**The path of the blank MP3 file, relative to the application.*/
	public final static String BLANK_MP3_PATH=GuiseApplication.GUISE_PUBLIC_AUDIO_PATH+"blank.mp3";	

	/**The path of the empty HTML document, relative to the application.*/
	public final static String GUISE_EMPTY_HTML_DOCUMENT_PATH=GuiseApplication.GUISE_PUBLIC_DOCUMENTS_PATH+"empty.html";

	/**The path of the AJAX JavaScript file, relative to the application.*/
	public final static String AJAX_JAVASCRIPT_PATH=GuiseApplication.GUISE_PUBLIC_JAVASCRIPT_PATH+"ajax.js";	
	/**The path of the DOM JavaScript file, relative to the application.*/
	public final static String DOM_JAVASCRIPT_PATH=GuiseApplication.GUISE_PUBLIC_JAVASCRIPT_PATH+"dom.js";	
	/**The path of the Guise JavaScript file, relative to the application.*/
	public final static String GUISE_JAVASCRIPT_PATH=GuiseApplication.GUISE_PUBLIC_JAVASCRIPT_PATH+"guise.js";	
	/**The path of the JavaScript JavaScript file, relative to the application.*/
	public final static String JAVASCRIPT_JAVASCRIPT_PATH=GuiseApplication.GUISE_PUBLIC_JAVASCRIPT_PATH+"javascript.js";	
	/**The path of the SoundManager JavaScript file, relative to the application.*/
	public final static String SOUNDMANAGER_JAVASCRIPT_PATH=GuiseApplication.GUISE_PUBLIC_JAVASCRIPT_PATH+"soundmanager2.js";	

	/**The path of the SoundManager Flash file, relative to the application.*/
	public final static String SOUNDMANAGER_FLASH_PATH=GuiseApplication.GUISE_PUBLIC_FLASH_PATH+"soundmanager2.swf";	

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

}