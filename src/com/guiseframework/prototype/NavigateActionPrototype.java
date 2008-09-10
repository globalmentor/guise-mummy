/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.prototype;

import java.net.URI;

import com.globalmentor.java.Objects;
import static com.globalmentor.java.Classes.*;
import com.globalmentor.net.URIPath;

import com.guiseframework.Guise;

/**Action prototype that knows how to navigate.
The navigation destination can be updated.
@author Garret Wilson
*/
public class NavigateActionPrototype extends AbstractActionPrototype
{

	/**The navigation URI bound property.*/
	public final static String NAVIGATION_URI_PROPERTY=getPropertyName(NavigateActionPrototype.class, "navigationURI");

	/**The requested navigation URI.*/
	private URI navigationURI;

		/**@return The requested navigation URI.*/
		public URI getNavigationURI() {return navigationURI;}

		/**Sets the URI for navigation.
		This is a bound property.
		@param newNavigationURI The new URI for navigation.
		@see #NAVIGATION_URI_PROPERTY
		*/
		public void setNavigationURI(final URI newNavigationURI)
		{
			final URI oldNavigationURI=getNavigationURI();
			if(!Objects.equals(oldNavigationURI, newNavigationURI))	//if the value is really changing
			{
				this.navigationURI=newNavigationURI;	//actually set the new navigation URI
				firePropertyChange(NAVIGATION_URI_PROPERTY, oldNavigationURI, newNavigationURI);	//indicate that the value changed
			}
		}

		/**Sets the URI path for navigation.
		This is a bound property.
		@param newNavigationURIPath The new URI path for navigation.
		@see #NAVIGATION_URI_PROPERTY
		*/
		public void setNavigationURIPath(final URIPath newNavigationURIPath)
		{
			setNavigationURI(newNavigationURIPath!=null ? newNavigationURIPath.toURI() : null);
		}

	/**Default constructor.*/
	public NavigateActionPrototype()
	{
		this((String)null);	//construct the class with no label
	}

	/**Label constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	*/
	public NavigateActionPrototype(final String label)
	{
		this(label, (URI)null);	//construct the class with no icon
	}

	/**Label and icon constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	*/
	public NavigateActionPrototype(final String label, final URI icon)
	{
		this(label, icon, (URI)null);	//construct the class with no navigation URI
	}
	
	/**Navigation URI constructor.
	@param navigationURI The URI for navigation when the action occurs, or <code>null</code> if no navigation should occur.
	*/
	public NavigateActionPrototype(final URI navigationURI)
	{
		this(null, null, navigationURI);	//construct the class with no label or icon, but with a navigation URI
	}

	/**Label, icon, and navigation URI constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	@param navigationURI The URI for navigation when the action occurs, or <code>null</code> if no navigation should occur.
	*/
	public NavigateActionPrototype(final String label, final URI icon, final URI navigationURI)
	{
		super(label, icon);	//construct the parent class
		this.navigationURI=navigationURI;	//set the navigation URI
	}
	
	/**Navigation URI path constructor.
	@param navigationURIPath The URI path for navigation when the action occurs, or <code>null</code> if no navigation should occur.
	*/
	public NavigateActionPrototype(final URIPath navigationURIPath)
	{
		this(null, navigationURIPath);	//construct the class with no label
	}

	/**Label and navigation URI path constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param navigationURIPath The URI path for navigation when the action occurs, or <code>null</code> if no navigation should occur.
	*/
	public NavigateActionPrototype(final String label, final URIPath navigationURIPath)
	{
		this(label, null, navigationURIPath);	//construct the class with no icon
	}

	/**Label, icon, and navigation URI path constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param icon The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.
	@param navigationURIPath The URI path for navigation when the action occurs, or <code>null</code> if no navigation should occur.
	*/
	public NavigateActionPrototype(final String label, final URI icon, final URIPath navigationURIPath)
	{
		this(label, icon, navigationURIPath!=null ? navigationURIPath.toURI() : null);	//construct the class with the navigation URI
	}

	@Override
	protected void action(final int force, final int option)
	{
		final URI navigationURI=getNavigationURI();	//get the configured navigation URI, if any
		if(navigationURI!=null)	//if we have a navigation URI
		{
			Guise.getInstance().getGuiseSession().navigate(getNavigationURI(), null);	//request that the session navigate to the configured URI
		}
	}

}
