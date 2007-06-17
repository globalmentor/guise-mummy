package com.guiseframework.platform.web;

import java.net.URI;

import com.guiseframework.GuiseApplication;

/**Constant values for the Guise web platform.
@author Garret Wilson
*/
public class WebPlatformConstants
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
	
	/**The path of the empty HTML document, relative to the application.*/
	public final static String GUISE_EMPTY_HTML_DOCUMENT_PATH=GuiseApplication.GUISE_PUBLIC_DOCUMENTS_PATH+"empty.html";

	/**The path of the JavaScript JavaScript file, relative to the application.*/
	public final static String JAVASCRIPT_JAVASCRIPT_PATH=GuiseApplication.GUISE_PUBLIC_JAVASCRIPT_PATH+"javascript.js";	
	/**The path of the DOM JavaScript file, relative to the application.*/
	public final static String DOM_JAVASCRIPT_PATH=GuiseApplication.GUISE_PUBLIC_JAVASCRIPT_PATH+"dom.js";	
	/**The path of the AJAX JavaScript file, relative to the application.*/
	public final static String AJAX_JAVASCRIPT_PATH=GuiseApplication.GUISE_PUBLIC_JAVASCRIPT_PATH+"ajax.js";	
/**The path of the Guise JavaScript file, relative to the application.*/
	public final static String GUISE_JAVASCRIPT_PATH=GuiseApplication.GUISE_PUBLIC_JAVASCRIPT_PATH+"guise.js";	

}
