package com.guiseframework.platform.web;

import com.guiseframework.platform.XHTMLDepictContext;

/**Information related to the current depiction on the web platform.
@author Garret Wilson
*/
public interface WebDepictContext extends XHTMLDepictContext
{

	/**@return The web platform on which Guise objects are depicted.*/
	public WebPlatform getPlatform();

	/**@return Whether quirks mode is being used.*/
	public boolean isQuirksMode();

}
