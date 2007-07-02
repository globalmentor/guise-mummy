package com.guiseframework.platform.web;

import com.guiseframework.platform.*;

/**A strategy for depicting objects on the web platform.
@param <O> The type of object being depicted.
@author Garret Wilson
*/
public interface WebDepictor<O extends DepictedObject> extends Depictor<O>
{

	/**@return The web platform on which this depictor is depicting ojects.*/
	public WebPlatform getPlatform();

	/**Retrieves information and functionality related to the current depiction on the web platform.
	This method delegates to {@link WebPlatform#getDepictContext()}.
	@return A context for the current depiction.
	@exception IllegalStateException if no depict context can be returned in the current depiction state.
	*/
	public WebDepictContext getDepictContext();
}
