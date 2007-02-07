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
	
	/**The path of the empty HTML document, relative to the application.*/
	public final static String GUISE_EMPTY_HTML_DOCUMENT_PATH=GuiseApplication.GUISE_PUBLIC_DOCUMENTS_PATH+"empty.html";

	/**The path of the Guise JavaScript file, relative to the application.*/
	public final static String GUISE_JAVASCRIPT_PATH=GuiseApplication.GUISE_PUBLIC_JAVASCRIPT_PATH+"guise.js";	

}
