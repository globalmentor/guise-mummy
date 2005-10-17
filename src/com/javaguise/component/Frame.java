package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.javaguise.event.ModalListener;
import com.javaguise.model.LabelModel;

/**A root-level component such as a window or an HTML page.
<p>The title is specified by the frame model's label.</p>
<p>A frame like other components is by default visible, but is not actually shown until its {@link #open(boolean)} method is called.</p>
@author Garret Wilson
*/
public interface Frame<C extends Frame<C>> extends CompositeComponent<C>, ModalComponent<C>
{
	/**The content bound property.*/
	public final static String CONTENT_PROPERTY=getPropertyName(Frame.class, "content");
	/**The bound property of whether the frame is modal if and when it is open.*/
	public final static String MODAL_PROPERTY=getPropertyName(Frame.class, "modal");
	/**The bound property of whether the component is movable.*/
	public final static String MOVABLE_PROPERTY=getPropertyName(Frame.class, "movable");
	/**The bound property of whether the frame can be resized.*/
	public final static String RESIZABLE_PROPERTY=getPropertyName(Frame.class, "resizable");
	/**The bound state property.*/
	public final static String STATE_PROPERTY=getPropertyName(Frame.class, "state");

	/**The state of the frame.*/
	public enum State
	{
		/**The frame is closed.*/
		CLOSED,
		/**The frame is open.*/
		OPEN;		
	}

	/**@return The state of the frame.*/
	public State getState();

	/**@return Whether the frame is modal if and when it is open.*/
	public boolean isModal();

	/**Sets whether the frame is modal if and when it is open.
	This is a bound property of type <code>Boolean</code>.
	@param newModal <code>true</code> if the frame should be modal, else <code>false</code>.
	@see #MODAL_PROPERTY
	*/
	public void setModal(final boolean newModal);

	/**@return Whether the frame is movable.*/
	public boolean isMovable();

	/**Sets whether the frame is movable.
	This is a bound property of type <code>Boolean</code>.
	@param newMovable <code>true</code> if the frame should be movable, else <code>false</code>.
	@see #MOVABLE_PROPERTY
	*/
	public void setMovable(final boolean newMovable);

	/**@return Whether the frame can be resized.*/
	public boolean isResizable();

	/**Sets whether the frame can be resized.
	This is a bound property of type <code>Boolean</code>.
	@param newResizable <code>true</code> if the frame can be resized, else <code>false</code>.
	@see #RESIZABLE_PROPERTY
	*/
	public void setResizable(final boolean newResizable);

	/**@return The data model used by this component.*/
	public LabelModel getModel();

	/**@return The single child component, or <code>null</code> if this frame does not have a child component.*/
	public Component<?> getContent();

	/**Sets the single child component.
	This is a bound property
	@param newContent The single child component, or <code>null</code> if this frame does not have a child component.
	@see #CONTENT_PROPERTY
	*/
	public void setContent(final Component<?> newContent);

	/**Opens the frame with the currently set modality.
	Opening the frame registers the frame with the session.
	If the frame is already open, no action occurs.
	@see #getState() 
	@see #STATE_PROPERTY
	*/
	public void open();

	/**Opens the frame, specifying modality.
	Opening the frame registers the frame with the session.
	If the frame is already open, no action occurs.
	@param modal <code>true</code> if the frame should be opened as a modal frame, else <code>false</code>.
	@see #getState() 
	@see #STATE_PROPERTY
	*/
	public void open(final boolean modal);

	/**Determines whether the frame should be allowed to close.
	This method is called from {@link #close()}.
	@return <code>true</code> if the frame should be allowed to close.
	*/
	public boolean canClose();
	
	/**Closes the frame.
	Closing the frame unregisters the frame with the session.
	If the frame is already closed, no action occurs.
	@see #getState() 
	@see #STATE_PROPERTY
	*/
	public void close();

	/**Adds a modal listener.
	@param modalListener The modal listener to add.
	*/
	public void addModalListener(final ModalListener<C> modalListener);

	/**Removes a modal listener.
	@param modalListener The modal listener to remove.
	*/
	public void removeModalListener(final ModalListener<C> modalListener);

}
