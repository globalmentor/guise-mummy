package com.guiseframework.platform.web;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.GuiseApplication;
import com.guiseframework.platform.AbstractPlatform;
import com.guiseframework.platform.DefaultEnvironment;
import com.guiseframework.platform.Environment;

/**An abstract implementation of a web platform for Guise.
This class registers no depictors.
@author Garret Wilson
*/
public abstract class AbstractWebPlatform extends AbstractPlatform implements WebPlatform
{

	/**The prefix used for creating depict ID strings on this platform.*/
	protected final static String DEPICT_ID_STRING_PREFIX="id";

	/**The user local environment.*/
	private final Environment environment;

		/**@return The user local environment.*/
		public Environment getEnvironment() {return environment;}

	/**Application.
	This version copies the current application environment to a new environment for this platform.
	@param application The Guise application running on this platform.
	@exception NullPointerException if the given application is <code>null</code>.
	*/
	public AbstractWebPlatform(final GuiseApplication application)
	{
		super(application);	//construct the parent class
		this.environment=new DefaultEnvironment();	//create a new environment
//TODO del; don't copy application environment properties; currently this exposes application-level passwords; this information needs to be removed from the application environment, though		environment.setProperties(application.getEnvironment().getProperties());	//copy the application environment to the platform environment
		//TODO create some sort of configuration that gets loaded on this platform
	}

	/**Generates an ID for the given depicted object appropriate for using on the platform.
	@param depictID The depict ID to be converted to a platform ID.
	@return The form of the depict ID appropriate for using on the platform.
	*/
	public String getDepictIDString(final long depictID)	//TODO change to Base64 with safe encoding
	{
		return "id"+Long.toHexString(depictID);	//create an ID string from the depict ID
	}

	/**Returns the depicted object ID represented by the given platform-specific ID string.
	@param depictIDString The platform-specific form of the depict ID.
	@param depictID The depict ID to be converted to a platform ID.
	@return The depict ID the platform-specific form represents.
	@exception NullPointerException if the given string is <code>null</code>.
	@exception IllegalArgumentException if the given string does not represent the correct string form of a depict ID on this platform.
	*/
	public long getDepictID(final String depictIDString)
	{
		if(!checkInstance(depictIDString, "Depict ID string cannot be null.").startsWith(DEPICT_ID_STRING_PREFIX))	//if the string does not start with the correct prefix
		{
			throw new IllegalArgumentException("Depict ID string "+depictIDString+" is not in the correct format for this platform.");
		}
		return Long.parseLong(depictIDString.substring(DEPICT_ID_STRING_PREFIX.length()), 16);	//parse out the actual ID, throwing a NumberFormatException if the ID is not in the correct lexical format
	}

}
