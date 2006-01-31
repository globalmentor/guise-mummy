package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.beans.PropertyChangeListener;

import com.javaguise.component.effect.Effect;
import com.javaguise.event.GuisePropertyChangeListener;
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
	/**The open effect bound property.*/
	public final static String OPEN_EFFECT_PROPERTY=getPropertyName(Frame.class, "openEffect");
	/**The bound property of the related component.*/
	public final static String RELATED_COMPONENT_PROPERTY=getPropertyName(Frame.class, "relatedComponent");
	/**The bound property of whether the frame can be resized.*/
	public final static String RESIZABLE_PROPERTY=getPropertyName(Frame.class, "resizable");
	/**The bound state property.*/
	public final static String STATE_PROPERTY=getPropertyName(Frame.class, "state");
	/**The bound property of whether the title bar is visible.*/
	public final static String TITLE_VISIBLE_PROPERTY=getPropertyName(Frame.class, "titleVisible");

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

	/**@return The related component such as a popup source, or <code>null</code> if the frame is not related to another component.*/
	public Component<?> getRelatedComponent();

	/**Sets the related component
	This is a bound property.
	@param newRelatedComponent The new related component, or <code>null</code> if the frame is not related to another component.
	@see Frame#RELATED_COMPONENT_PROPERTY 
	*/
	public void setRelatedComponent(final Component<?> newRelatedComponent);

	/**@return Whether the title bar is visible.*/
	public boolean isTitleVisible();

	/**Sets whether the title bar is visible.
	This is a bound property of type <code>Boolean</code>.
	@param newTitleVisible <code>true</code> if the title bar should be visible, else <code>false</code>.
	@see #TITLE_VISIBLE_PROPERTY
	*/
	public void setTitleVisible(final boolean newTitleVisible);

	/**@return The effect used for opening the frame, or <code>null</code> if there is no open effect.*/
	public Effect getOpenEffect();

	/**Sets the effect used for opening the frame.
	This is a bound property.
	@param newEffect The new effect used for opening the frame, or <code>null</code> if there should be no open effect.
	@see #OPEN_EFFECT_PROPERTY 
	*/
	public void setOpenEffect(final Effect newOpenEffect);

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

	/**Opens the frame as modal and installs the given property change listener to listen for the mode changing.
	This is a convenience method that adds the mode change listener using {@link #addPropertyChangeListener(String, PropertyChangeListener)} and then calls {@link #open(boolean)} with a value of <code>true</code>.
	@param modeChangeListener The mode property change listener to add.
	@see ModalComponent#MODE_PROPERTY 
	*/
	public void open(final GuisePropertyChangeListener<Mode> modeChangeListener);

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
}
