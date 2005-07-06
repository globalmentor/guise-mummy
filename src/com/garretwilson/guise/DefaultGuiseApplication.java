package com.garretwilson.guise;

import java.util.*;

import com.garretwilson.guise.context.GuiseContext;

/**The default implementation of a Guise application.
@author Garret Wilson
*/
public class DefaultGuiseApplication<GC extends GuiseContext> extends AbstractGuiseApplication<GC>
{

	/**Default constructor.
	This implemetation sets the locale to the JVM default.
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
