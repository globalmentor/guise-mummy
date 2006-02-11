package com.javaguise.platform.web;

import java.net.URI;

/**Constant values for the Guise web platform.
@author Garret Wilson
*/
public class WebPlatformConstants
{

	/**The namespace of the Guise markup language to be used with XHTML.*/
	public final static URI GUISE_ML_NAMESPACE_URI=URI.create("http://guiseframework.com/id/ml#");
	/**The standard prefix to use with the Guise markup language namespace.*/
	public final static String GUISE_ML_NAMESPACE_PREFIX="guise";
	
	/**The base path for serving Guise resources.*/
	public final static String GUISE_PUBLIC_RESOURCE_BASE_PATH="/guise/";

		//CSS

	/**The path of the Guise CSS stylesheet, relative to the application.*/
	public final static String GUISE_CSS_STYLESHEET_PATH="guise/stylesheets/guise.css";

		//JavaScript
	
	/**The path of the Guise JavaScript file, relative to the application.*/
	public final static String GUISE_JAVASCRIPT_PATH="guise/javascript/guise.js";

		//images
	
	/**The path of the image for frame close.*/
//TODO del	public final static String FRAME_CLOSE_IMAGE_PATH="guise/images/frame-close.gif";

	/**The path of the horizontal slider thumb image.*/
//TODO del public final static String SLIDER_THUMB_X_LTR_IMAGE_PATH="guise/images/slider-thumb-x-ltr.gif";
	/**The path of the vertical slider thumb image.*/
//TODO del	public final static String SLIDER_THUMB_Y_IMAGE_PATH="guise/images/slider-thumb-y.gif";

}
