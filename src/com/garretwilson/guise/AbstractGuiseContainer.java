package com.garretwilson.guise;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.garretwilson.lang.ObjectUtilities.*;

/**An abstract base class for a Guise instance.
This implementation only works with Guise applications that descend from {@link AbstractGuiseApplication}.
@author Garret Wilson
*/
public abstract class AbstractGuiseContainer	implements GuiseContainer
{

	/**The thread-safe map of Guise applications keyed to application context paths.*/
	private final Map<String, AbstractGuiseApplication> applicationMap=new ConcurrentHashMap<String, AbstractGuiseApplication>();

	/**Installs the given application at the given context path.
	@param contextPath The context path at which the application is being installed.
	@exception NullPointerException if either the application or context path is <code>null</code>.
	@exception IllegalArgumentException if the context path is not absolute and does not end with a slash ('/') character.
	@exception IllegalStateException if the application is already installed in some container.
	@exception IllegalStateException if there is already an application installed in this container at the given context path.
	*/
	protected void installApplication(final AbstractGuiseApplication application, final String contextPath)
	{
		checkNull(application, "Application cannot be null");
		checkNull(contextPath, "Application context path cannot be null");
		synchronized(applicationMap)	//synchronize installations so that we can check the existence of the context path in the container
		{
			if(applicationMap.get(contextPath)!=null)	//if there is already an application installed at the given context path
			{
				throw new IllegalStateException("Application already installed at context path "+contextPath);
			}
			application.install(this, contextPath);	//tell the application it's being installed
			applicationMap.put(contextPath, application);	//install the application in the map
		}
	}

	/**Uninstalls the given application.
	@exception NullPointerException if the application is <code>null</code>.
	@exception IllegalStateException if the application is not installed in this container.
	*/
	protected void uninstallApplication(final AbstractGuiseApplication application)
	{
		checkNull(application, "Application cannot be null");
		final String contextPath=application.getContextPath();	//get the application's context path
		if(contextPath==null || application.getContainer()!=this)	//if the application has no context path or has a different container than this class
		{
			throw new IllegalStateException("Application installed in a different container.");
		}
		synchronized(applicationMap)	//synchronize uninstallations so that we can check the existence of the context path in the container
		{
			if(applicationMap.get(contextPath)!=application)	//if something (or nothing) other than the given application is installed at this context path
			{
				throw new IllegalStateException("Application not installed at context path "+contextPath);
			}
			applicationMap.remove(contextPath);	//remove the application in the map
			application.uninstall(this);	//tell the application it's being uninstalled
		}
	}
}
