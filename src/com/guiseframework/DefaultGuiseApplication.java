package com.guiseframework;

import java.util.*;

/**The default implementation of a Guise application.
@author Garret Wilson
*/
public class DefaultGuiseApplication extends AbstractGuiseApplication
{

	/**Default constructor.
	This implementation sets the locale to the JVM default.
	*/
	public DefaultGuiseApplication()
	{
		this(Locale.getDefault());	//construct the class with the JVM default locale
	}

	/**Locale constructor.
	@param locale The default application locale.
	*/
	public DefaultGuiseApplication(final Locale locale)
	{
		super(locale);	//construct the parent class with the provided locale
	}
}
