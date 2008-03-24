package com.guiseframework.component.urf;

import java.beans.*;

import com.globalmentor.urf.*;

import com.guiseframework.component.*;

/**A component for specifying alteration to be performed on a resource.
Panels containing resource property information typically implement this interface, for example.
@author Garret Wilson
*/
public interface URFResourceAlterationComponent extends Component
{

	/**Retrieves the specification for modifying a resource based upon the contents of the component.
	@return The specification of alterations to be performed on a resource according to the properties edited in the component.
	*/
	public URFResourceAlteration getResourceAlteration();

	/**Sets the resource information displayed in the component.
	@param resource The resource containing the URI and properties to set in the component.
	@throws NullPointerException if the given resource is <code>null</code>.
	@throws PropertyVetoException if there was an error setting the information in the component.
	*/
	public void setResource(final URFResource resource) throws PropertyVetoException;
}
