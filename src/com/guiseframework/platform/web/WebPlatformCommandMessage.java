package com.guiseframework.platform.web;

import java.net.URI;
import java.util.*;

import com.guiseframework.platform.DepictContext;
import com.guiseframework.platform.PlatformCommandMessage;

/**A command message to or from the web platform on which objects are being depicted.
All parameters with {@link URI} values will are considered to be application-relative;
before depiction they will be dereferenced and resolved using {@link DepictContext#getDepictURI(URI)}
@param <C> The type of command.
@author Garret Wilson
*/
public interface WebPlatformCommandMessage<C extends Enum<C> & WebPlatformCommand> extends PlatformCommandMessage<C>
{

	/**@return The read-only map of parameters.*/
	public Map<String, Object> getParameters();

}
