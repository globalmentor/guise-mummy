package com.guiseframework.platform.web;

import com.guiseframework.component.Component;
import com.guiseframework.platform.ComponentDepictor;

/**A strategy for depicting components on the web platform.
All component depictors on the web platform must implement this interface.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public interface WebComponentDepictor<C extends Component> extends ComponentDepictor<C>, WebDepictor<C>
{
	/**Determines the identifier to place in the name attribute of the component's XHTML element, if appropriate.
	This is usually the string version of the ID of the component, but some grouped components may use a common name.
	@return An identifier appropriate for the name attribute of the component's XHTML element.
	*/
	public String getDepictName();
}
