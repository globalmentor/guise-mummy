package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.net.URI;
import java.util.MissingResourceException;

import com.garretwilson.lang.ObjectUtilities;

/**A component representing a Macromedia Flash object.
@author Garret Wilson
*/
public class Flash extends AbstractComponent<Flash>  
{

	/**The flash URI bound property.*/
	public final static String FLASH_URI_PROPERTY=getPropertyName(Flash.class, "flashURI");
	/**The flash URI resource key bound property.*/
	public final static String FLASH_URI_RESOURCE_KEY_PROPERTY=getPropertyName(Flash.class, "flashURIResourceKey");

	/**The flast URI, or <code>null</code> if there is no flash URI.*/
	private URI flashURI=null;

		/**Determines the URI of the flash.
		If a flash URI is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
		@return The flash URI, or <code>null</code> if there is no flash URI.
		@exception MissingResourceException if there was an error loading the value from the resources.
		@see #getFlashURIResourceKey()
		*/
		public URI getFlashURI() throws MissingResourceException
		{
			return getSession().determineURI(flashURI, getFlashURIResourceKey());	//get the value or the resource, if available
		}

		/**Sets the URI of the flash.
		This is a bound property of type <code>URI</code>.
		@param newFlashURI The new URI of the flash.
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

	/**The flash URI resource key, or <code>null</code> if there is no flash URI resource specified.*/
	private String flashURIResourceKey=null;

		/**@return The flash URI resource key, or <code>null</code> if there is no flash URI resource specified.*/
		public String getFlashURIResourceKey() {return flashURIResourceKey;}

		/**Sets the key identifying the URI of the flash in the resources.
		This is a bound property.
		@param newFlashResourceKey The new flash URI resource key.
		@see #IMAGE_RESOURCE_KEY_PROPERTY
		*/
		public void setFlashURIResourceKey(final String newFlashResourceKey)
		{
			if(!ObjectUtilities.equals(flashURIResourceKey, newFlashResourceKey))	//if the value is really changing
			{
				final String oldFlashURIResourceKey=flashURIResourceKey;	//get the old value
				flashURIResourceKey=newFlashResourceKey;	//actually change the value
				firePropertyChange(FLASH_URI_RESOURCE_KEY_PROPERTY, oldFlashURIResourceKey, newFlashResourceKey);	//indicate that the value changed
			}
		}

}
