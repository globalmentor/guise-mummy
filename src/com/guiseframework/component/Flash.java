package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.net.URI;

import com.garretwilson.lang.ObjectUtilities;

/**A component representing a Macromedia Flash object.
@author Garret Wilson
*/
public class Flash extends AbstractComponent<Flash>  
{

	/**The flash URI bound property.*/
	public final static String FLASH_URI_PROPERTY=getPropertyName(Flash.class, "flashURI");

	/**The Flash URI, which may be a resource URI, or <code>null</code> if there is no Flash URI.*/
	private URI flashURI=null;

		/**@return The Flash URI, which may be a resource URI, or <code>null</code> if there is no Flash URI.*/
		public URI getFlashURI() {return flashURI;}

		/**Sets the URI of the Flash.
		This is a bound property of type <code>URI</code>.
		@param newFlashURI The new URI of the Flash, which may be a resource URI.
		@see #IMAGE_PROPERTY
		*/
		public void setFlashURI(final URI newFlashURI)
		{
			if(!ObjectUtilities.equals(flashURI, newFlashURI))	//if the value is really changing
			{
				final URI oldFlashURI=flashURI;	//get the old value
				flashURI=newFlashURI;	//actually change the value
				firePropertyChange(FLASH_URI_PROPERTY, oldFlashURI, newFlashURI);	//indicate that the value changed
			}			
		}

}
