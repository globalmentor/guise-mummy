package com.guiseframework;

import java.net.URI;
import java.util.*;

import com.globalmentor.util.UUIDs;

/**The default implementation of a Guise application.
@author Garret Wilson
*/
public class DefaultGuiseApplication extends AbstractGuiseApplication
{

	/**Default constructor.
	This implementation generates a new UUID URI for the application identifier.
	*/
	public DefaultGuiseApplication()
	{
		this(UUIDs.toURI(UUID.randomUUID()));	//construct the class with the JVM default locale
	}

	/**URI constructor.
	@param uri The URI for the application, which may or may not be the URI at which the application can be accessed.
	@throws NullPointerException if the given URI is <code>null</code>.
	*/
	public DefaultGuiseApplication(final URI uri)
	{
		super(uri);	//construct the parent class
	}

}
