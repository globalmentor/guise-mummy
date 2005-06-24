package com.garretwilson.guise.session;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.garretwilson.guise.Guise;
import com.garretwilson.guise.component.Frame;
import com.garretwilson.guise.context.GuiseContext;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.xml.XMLUtilities.*;

/**An abstract implementation that keeps track of the components of a user session.
@author Garret Wilson
*/
public class AbstractGuiseSession<GC extends GuiseContext> implements GuiseSession<GC>
{

	/**@return The instance of Guise to which this session belongs.*/
	private final Guise<GC> guise;

		/**@return The instance of Guise to which this session belongs.*/
		public Guise<GC> getGuise() {return guise;}

	/**The map binding frame types to context-relative paths.*/
	private final Map<String, Frame> pathFrameBindingMap=new HashMap<String, Frame>();

		/**Binds a frame to a particular context-relative path.
		Any existing binding for the given context-relative path is replaced.
		@param path The context-relative path to which the frame should be bound.
		@param frame The frame to render for this particular context-relative path.
		@return The frame previously bound to the given context-relative path, or <code>null</code> if no frame was previously bound to the path.
		@exception NullPointerException if the path and/or the frame is null.
		*/
		protected Frame bindFrame(final String path, final Frame frame)
		{
			return pathFrameBindingMap.put(checkNull(path, "Path cannot be null."), checkNull(frame, "Bound frame cannot be null."));	//store the binding
		}
		
	/**Guise constructor.
	@param guise The instance of Guise to which this session belongs.
	*/
	public AbstractGuiseSession(final Guise<GC> guise)
	{
		this.guise=guise;	//save the Guise instance
	}

	/**Retrieves the frame bound to the given path.
	If a frame has already been created and cached, it will be be returned; otherwise, one will be created and cached. 
	The frame will be given an ID of a modified form of the path.
	@param path The absolute path within the Guise container context.
	@return The frame bound to the given path, or <code>null</code> if no frame is bound to the given path
	@exception NoSuchMethodException if the frame bound to the path does not provide a single ID string constructor.
	@exception IllegalAccessException if the bound frame enforces Java language access control and the underlying constructor is inaccessible.
	@exception InstantiationException if the bound frame is an abstract class.
	@exception InvocationTargetException if the bound frame's underlying constructor throws an exception.
	*/
	public Frame getBoundFrame(final String path) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException
	{
		Frame frame=pathFrameBindingMap.get(path);	//get the bound frame type, if any
		if(frame==null)	//if no frame is cached
		{
			final Class<? extends Frame> frameClass=getGuise().getBoundFrameClass(path);	//see which frame we should show for this path
			if(frameClass!=null)	//if we found a frame class for this path
			{
				final String frameID=createName(path);	//convert the path to a valid ID TODO use a Guise-specific routine
				frame=frameClass.getConstructor(String.class).newInstance(frameID);	//find the ID constructor and create an instance of the class
				bindFrame(path, frame);	//bind the frame to the path, caching it for next time
			}
		}
		return frame;	//return the frame, or null if we couldn't find a frame
	}

}
