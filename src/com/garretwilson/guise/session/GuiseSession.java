package com.garretwilson.guise.session;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Set;

import com.garretwilson.event.PostponedEvent;
import com.garretwilson.guise.application.GuiseApplication;
import com.garretwilson.guise.component.NavigationFrame;
import com.garretwilson.guise.context.GuiseContext;

/**Represents a session with a user.
A client application may only have one session, while a web server application will likely have multiple sessions.
@author Garret Wilson
*/
public interface GuiseSession<GC extends GuiseContext<GC>>
{

	/**@return The Guise application to which this session belongs.*/
	public GuiseApplication<GC> getApplication();

	/**@return The unmodifiable set of all states of available Guise contexts.*/
	public Set<GuiseContext.State> getContextStates();

	/**Queues a postponed model event to be fired after all contexts have finished updating the model.
	If a Guise context is currently updating the model, the event will be queued for later.
	If no Guise context is currently updating the model, the event will be fired immediately.
	@param postponedModelEvent The event to fire at a later time.
	@see GuiseContext.State#UPDATE_MODEL
	*/
	public void queueModelEvent(final PostponedEvent<?> postponedModelEvent);

	/**Retrieves the frame bound to the given appplication context-relateive path.
	If a frame has already been created and cached, it will be be returned; otherwise, one will be created and cached. 
	The frame will be given an ID of a modified form of the path.
	@param path The appplication context-relative path within the Guise container context.
	@return The frame bound to the given path, or <code>null</code> if no frame is bound to the given path
	@exception IllegalArgumentException if the provided path is absolute.
	@exception NoSuchMethodException if the frame bound to the path does not provide Guise session constructor; or a Guise session and ID string constructor.
	@exception IllegalAccessException if the bound frame enforces Java language access control and the underlying constructor is inaccessible.
	@exception InstantiationException if the bound frame is an abstract class.
	@exception InvocationTargetException if the bound frame's underlying constructor throws an exception.
	*/
	public NavigationFrame getBoundNavigationFrame(final String path) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException;

	/**Reports the navigation path relative to the application context path.
	@return The path representing the current navigation location of the Guise application.
	@exception IllegalStateException if this message has been called before the navigation path has been initialized.
	*/
	public String getNavigationPath();

	/**Requests navigation to the specified path.
	The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
	Later requested navigation before navigation occurs will override this request.
	@param path A path that is either relative to the application context path or is absolute.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority (in which case {@link #navigate(URI)} should be used instead).
	@see #navigate(URI)
	*/
	public void navigate(final String path);

	/**Requests navigation to the specified URI.
	The session need not perform navigation immediately or ever, and may postpone or deny navigation at some later point.
	Later requested navigation before navigation occurs will override this request.
	@param uri Either a relative or absolute path, or an absolute URI.
	@exception NullPointerException if the given URI is <code>null</code>.
	*/
	public void navigate(final URI uri);

}
