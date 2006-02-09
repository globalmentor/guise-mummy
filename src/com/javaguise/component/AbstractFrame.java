package com.javaguise.component;

import java.beans.PropertyChangeListener;
import java.net.URI;
import java.util.Iterator;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.util.Debug;
import com.garretwilson.util.EmptyIterator;
import com.garretwilson.util.ObjectIterator;
import com.javaguise.GuiseSession;
import com.javaguise.component.effect.Effect;
import com.javaguise.event.ActionEvent;
import com.javaguise.event.ActionListener;
import com.javaguise.event.GuisePropertyChangeListener;
import com.javaguise.model.LabelModel;

/**Abstract implementation of a frame.
@author Garret Wilson
@see LayoutPanel
*/
public abstract class AbstractFrame<C extends Frame<C>> extends AbstractEnumCompositeComponent<AbstractFrame.FrameComponent, C> implements Frame<C>
{

	/**The resource bundle key for the close image.*/
	public final static String CLOSE_ICON_RESOURCE_KEY="frame.close.icon";

	/**The enumeration of frame components.*/
	private enum FrameComponent{CONTENT_COMPONENT, CLOSE_ACTION_CONTROL};

	/**The default mode of an open, modal frame.*/
	public final static Mode DEFAULT_MODAL_FRAME_MODE=new Mode(){};

	/**The state of the frame.*/
	private State state=State.CLOSED;

		/**@return The state of the frame.*/
		public State getState() {return state;}

		/**Sets the state of the frame.
		This is a bound property.
		@param newState The new state of the frame.
		@exception NullPointerException if the given state is <code>null</code>.
		@see Frame#STATE_PROPERTY 
		*/
		protected void setState(final State newState)
		{
			if(state!=checkNull(newState, "State cannot be null."))	//if the value is really changing
			{
				final State oldState=state;	//get the old value
				state=newState;	//actually change the value
				firePropertyChange(STATE_PROPERTY, oldState, newState);	//indicate that the value changed
				setMode(isModal() && newState!=State.CLOSED ? DEFAULT_MODAL_FRAME_MODE : null);	//set the modal mode if we are open and modal
			}			
		}

	/**Whether the frame is modal if and when it is open.*/
	private boolean modal=false;

		/**@return Whether the frame is modal if and when it is open.*/
		public boolean isModal() {return modal;}

		/**Sets whether the frame is modal if and when it is open.
		This is a bound property of type <code>Boolean</code>.
		@param newModal <code>true</code> if the frame should be modal, else <code>false</code>.
		@see Frame#MODAL_PROPERTY
		*/
		public void setModal(final boolean newModal)
		{
			if(modal!=newModal)	//if the value is really changing
			{
				final boolean oldModal=modal;	//get the current value
				modal=newModal;	//update the value
				firePropertyChange(MODAL_PROPERTY, Boolean.valueOf(oldModal), Boolean.valueOf(newModal));
				setMode(newModal && getState()!=State.CLOSED ? DEFAULT_MODAL_FRAME_MODE : null);	//set the modal mode if we are open and modal
			}
		}

	/**The current mode of interaction, or <code>null</code> if the component is in a modeless state.*/
	private Mode mode=null;

		/**@return The current mode of interaction, or <code>null</code> if the component is in a modeless state.*/
		public Mode getMode() {return mode;}

		/**Sets the mode of interaction.
		This is a bound property.
		@param newMode The new mode of component interaction.
		@see ModalComponent#MODE_PROPERTY 
		*/
		public void setMode(final Mode newMode)
		{
			if(mode!=newMode)	//if the value is really changing
			{
				final Mode oldMode=mode;	//get the old value
				mode=newMode;	//actually change the value
				firePropertyChange(MODE_PROPERTY, oldMode, newMode);	//indicate that the value changed
			}			
		}

	/**Whether the frame is movable.*/
	private boolean movable=true;

		/**@return Whether the frame is movable.*/
		public boolean isMovable() {return movable;}

		/**Sets whether the frame is movable.
		This is a bound property of type <code>Boolean</code>.
		@param newMovable <code>true</code> if the frame should be movable, else <code>false</code>.
		@see Frame#MOVABLE_PROPERTY
		*/
		public void setMovable(final boolean newMovable)
		{
			if(movable!=newMovable)	//if the value is really changing
			{
				final boolean oldMovable=movable;	//get the current value
				movable=newMovable;	//update the value
				firePropertyChange(MOVABLE_PROPERTY, Boolean.valueOf(oldMovable), Boolean.valueOf(newMovable));
			}
		}

	/**Whether the frame can be resized.*/
	private boolean resizable=true;

		/**@return Whether the frame can be resized.*/
		public boolean isResizable() {return resizable;}

		/**Sets whether the frame can be resized.
		This is a bound property of type <code>Boolean</code>.
		@param newResizable <code>true</code> if the frame can be resized, else <code>false</code>.
		@see Frame#RESIZABLE_PROPERTY
		*/
		public void setResizable(final boolean newResizable)
		{
			if(resizable!=newResizable)	//if the value is really changing
			{
				final boolean oldResizable=resizable;	//get the current value
				resizable=newResizable;	//update the value
				firePropertyChange(MOVABLE_PROPERTY, Boolean.valueOf(oldResizable), Boolean.valueOf(newResizable));
			}
		}

	/**The related component such as a popup source, or <code>null</code> if the frame is not related to another component.*/
	private Component<?> relatedComponent=null;

		/**@return The related component such as a popup source, or <code>null</code> if the frame is not related to another component.*/
		public Component<?> getRelatedComponent() {return relatedComponent;}

		/**Sets the related component
		This is a bound property.
		@param newRelatedComponent The new related component, or <code>null</code> if the frame is not related to another component.
		@see Frame#RELATED_COMPONENT_PROPERTY 
		*/
		public void setRelatedComponent(final Component<?> newRelatedComponent)
		{
			if(relatedComponent!=newRelatedComponent)	//if the value is really changing
			{
				final Component<?> oldRelatedComponent=relatedComponent;	//get the old value
				relatedComponent=newRelatedComponent;	//actually change the value
				firePropertyChange(RELATED_COMPONENT_PROPERTY, oldRelatedComponent, newRelatedComponent);	//indicate that the value changed
			}			
		}

	/**Whether the title bar is visible.*/
	private boolean titleVisible=true;

		/**@return Whether the title bar is visible.*/
		public boolean isTitleVisible() {return titleVisible;}

		/**Sets whether the title bar is visible.
		This is a bound property of type <code>Boolean</code>.
		@param newTitleVisible <code>true</code> if the title bar should be visible, else <code>false</code>.
		@see Frame#TITLE_VISIBLE_PROPERTY
		*/
		public void setTitleVisible(final boolean newTitleVisible)
		{
			if(titleVisible!=newTitleVisible)	//if the value is really changing
			{
				final boolean oldTitleVisible=titleVisible;	//get the current value
				titleVisible=newTitleVisible;	//update the value
				firePropertyChange(TITLE_VISIBLE_PROPERTY, Boolean.valueOf(oldTitleVisible), Boolean.valueOf(newTitleVisible));
			}
		}

	/**The effect used for opening the frame, or <code>null</code> if there is no open effect.*/
	private Effect openEffect=null;

		/**@return The effect used for opening the frame, or <code>null</code> if there is no open effect.*/
		public Effect getOpenEffect() {return openEffect;}

		/**Sets the effect used for opening the frame.
		This is a bound property.
		@param newEffect The new effect used for opening the frame, or <code>null</code> if there should be no open effect.
		@see Frame#OPEN_EFFECT_PROPERTY 
		*/
		public void setOpenEffect(final Effect newOpenEffect)
		{
			if(openEffect!=newOpenEffect)	//if the value is really changing
			{
				final Effect oldOpenEffect=openEffect;	//get the old value
				openEffect=newOpenEffect;	//actually change the value
				firePropertyChange(OPEN_EFFECT_PROPERTY, oldOpenEffect, newOpenEffect);	//indicate that the value changed
			}			
		}

	/**@return The data model used by this component.*/
	public LabelModel getModel() {return (LabelModel)super.getModel();}

	/**The single child component, or <code>null</code> if this frame does not have a child component.*/
//TODO del	private Component<?> content;

		/**@return The content child component, or <code>null</code> if this frame does not have a content child component.
		@see FrameComponent#CONTENT_COMPONENT
		*/
		public Component<?> getContent() {return getComponent(FrameComponent.CONTENT_COMPONENT);}

		/**Sets the content child component.
		This is a bound property.
		@param newContent The content child component, or <code>null</code> if this frame does not have a content child component.
		@see FrameComponent#CONTENT_COMPONENT
		@see Frame#CONTENT_PROPERTY
		*/
		public void setContent(final Component<?> newContent)
		{
			final Component<?> oldContent=setComponent(FrameComponent.CONTENT_COMPONENT, newContent);	//set the component
			if(oldContent!=newContent)	//if the component really changed
			{
				firePropertyChange(CONTENT_PROPERTY, oldContent, newContent);	//indicate that the value changed
			}
		}

	/**The action listener for closing the frame.*/
	private final ActionListener closeActionListener;
		
	/**@return The action control for closing the frame, or <code>null</code> if this frame does not have a close action control.
	@see FrameComponent#CLOSE_ACTION_CONTROL
	*/
	public ActionControl<?> getCloseActionControl() {return (ActionControl<?>)getComponent(FrameComponent.CLOSE_ACTION_CONTROL);}

	/**Sets the action control for closing the frame.
	This is a bound property.
	@param newCloseActionControl The action control for closing the frame, or <code>null</code> if this frame does not have a close action control.
	@see FrameComponent#CLOSE_ACTION_CONTROL
	@see Frame#CLOSE_ACTION_CONTROL_PROPERTY
	*/
	public void setCloseActionControl(final ActionControl<?> newCloseActionControl)
	{
		final ActionControl<?> oldCloseActionControl=(ActionControl<?>)setComponent(FrameComponent.CLOSE_ACTION_CONTROL, newCloseActionControl);	//set the component
		if(oldCloseActionControl!=newCloseActionControl)	//if the component really changed
		{
			if(oldCloseActionControl!=null)	//if we had an old close action
			{
				oldCloseActionControl.getModel().removeActionListener(closeActionListener);	//remove the close action listener from the old control
			}
			if(newCloseActionControl!=null)	//if we have a new close action
			{
				newCloseActionControl.getModel().addActionListener(closeActionListener);	//listen for the new action control and close the frame in response
			}
			firePropertyChange(CLOSE_ACTION_CONTROL_PROPERTY, oldCloseActionControl, newCloseActionControl);	//indicate that the value changed
		}
	}

	/**Session, ID, model, and component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractFrame(final GuiseSession session, final String id, final LabelModel model, final Component<?> component)
	{
		super(session, id, model, FrameComponent.values());	//construct the parent class
		closeActionListener=new ActionListener()	//create an action listener for closing
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the close action is initiated
					{
						close();	//close the frame
					}
				};
		setComponent(FrameComponent.CONTENT_COMPONENT, component);	//set the component directly, because child classes may prevent the setContent() method from changing the component 
		final Link closeButton=new Link(session);	//create a close action control
		closeButton.getModel().setIconResourceKey(CLOSE_ICON_RESOURCE_KEY);	//indicate to the close action control the resource key for its icon
		setCloseActionControl(closeButton);	//set the close action control
	}

	/**Opens the frame with the currently set modality.
	Opening the frame registers the frame with the session.
	If the frame is already open, no action occurs.
	@see #getState() 
	@see Frame#STATE_PROPERTY
	*/
	public void open()
	{
		if(getState()==State.CLOSED)	//if the state is closed
		{
			getSession().addFrame(this);	//add the frame to the session
			setState(State.OPEN);	//change the state
		}		
	}

	/**Opens the frame, specifying modality.
	Opening the frame registers the frame with the session.
	If the frame is already open, no action occurs.
	@param modal <code>true</code> if the frame should be opened as a modal frame, else <code>false</code>.
	@see #getState() 
	@see Frame#STATE_PROPERTY
	*/
	public void open(final boolean modal)
	{
		setModal(modal);	//update the modality
		open();	//open the frame normally
	}

	/**Opens the frame as modal and installs the given property change listener to listen for the mode changing.
	This is a convenience method that adds the mode change listener using {@link #addPropertyChangeListener(String, PropertyChangeListener)} and then calls {@link #open(boolean)} with a value of <code>true</code>.
	@param modeChangeListener The mode property change listener to add.
	@see ModalComponent#MODE_PROPERTY 
	*/
	public void open(final GuisePropertyChangeListener<Mode> modeChangeListener)
	{
		addPropertyChangeListener(MODE_PROPERTY, modeChangeListener);	//add the mode property change listener
		open(true);	//open modally
	}

	/**Determines whether the frame should be allowed to close.
	This implementation returns <code>true</code>.
	This method is called from {@link #close()}.
	@return <code>true</code> if the frame should be allowed to close.
	*/
	public boolean canClose()
	{
		return true;	//by default always allow the frame to be closed
	}

	/**Closes the frame.
	Closing the frame unregisters the frame with the session.
	If the frame is already closed, no action occurs.
	This method calls {@link #canClose()} and only performs closing functionality if that method returns <code>true</code>.
	This method delegates actual closing to {@link #closeImpl()}, and that method should be overridden rather than this one.
	@see #getState() 
	@see Frame#STATE_PROPERTY
	*/
	public final void close()
	{
		if(getState()!=State.CLOSED)	//if the frame is not already closed
		{
			if(canClose())	//if the frame can close
			{
				closeImpl();	//actually close the frame
			}
		}
	}

	/**Implementation of frame closing.*/
	protected void closeImpl()
	{
//TODO del Debug.trace("ready to remove frame");
		getSession().removeFrame(this);	//remove the frame from the session
		setState(State.CLOSED);	//change the state
	}
}
