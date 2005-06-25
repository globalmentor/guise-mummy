package com.garretwilson.guise.session;

import java.lang.reflect.InvocationTargetException;

import com.garretwilson.guise.application.GuiseApplication;
import com.garretwilson.guise.component.Frame;
import com.garretwilson.guise.context.GuiseContext;

/**Represents a session with a user.
A client application may only have one session, while a web server application will likely have multiple sessions.
@author Garret Wilson
*/
public interface GuiseSession<GC extends GuiseContext>
{

	/**@return The Guise application to which this session belongs.*/
	public GuiseApplication<GC> getApplication();

	/**Retrieves the frame bound to the given appplication context-relative absolute path.
	If a frame has already been created and cached, it will be be returned; otherwise, one will be created and cached. 
	The frame will be given an ID of a modified form of the path.
	@param path The appplication context-relative absolute path within the Guise container context.
	@return The frame bound to the given path, or <code>null</code> if no frame is bound to the given path
	@exception NoSuchMethodException if the frame bound to the path does not provide a single ID string constructor or a default constructor.
	@exception IllegalAccessException if the bound frame enforces Java language access control and the underlying constructor is inaccessible.
	@exception InstantiationException if the bound frame is an abstract class.
	@exception InvocationTargetException if the bound frame's underlying constructor throws an exception.
	*/
	public Frame getBoundFrame(final String path) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException;

}
