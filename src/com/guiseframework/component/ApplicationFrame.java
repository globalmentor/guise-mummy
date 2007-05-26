package com.guiseframework.component;

/**The root frame of an application.
@author Garret Wilson
*/
public interface ApplicationFrame<C extends ApplicationFrame<C>> extends Frame<C>
{

	/**@return An iterable to all child frames.*/
	public Iterable<Frame<?>> getChildFrames();

	/**Adds a frame to the list of child frames.
	This method should usually only be called by the frames themselves.
	@param frame The frame to add.
	@exception NullPointerException if the given frame is <code>null</code>.
	@exception IllegalArgumentException if the given frame is this frame.
	*/
	public void addChildFrame(final Frame<?> frame);

	/**Removes a frame from the list of child frames.
	This method should usually only be called by the frames themselves.
	@param frame The frame to remove.
	@exception NullPointerException if the given frame is <code>null</code>.
	@exception IllegalArgumentException if the given frame is this frame.
	*/
	public void removeChildFrame(final Frame<?> frame);

}
