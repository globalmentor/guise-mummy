/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.platform;

import java.beans.*;
import java.io.IOException;

import com.globalmentor.beans.PropertyBindable;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.Layout;
import com.guiseframework.component.transfer.Transferable;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.platform.AbstractDepictor.DepictedPropertyChangeListener;

/**An abstract implementation of a component depictor.
If the component has a model, this implementation will automatically register to listen to its properties being changed.
This implementation does not recognize that it needs to be updated if the associated component changes its registered listeners.
A view keeps track of component modified properties between updates.
This implementation ignores a change in {@link Component#VALID_PROPERTY} and {@link Component#INPUT_STRATEGY_PROPERTY}.
This implementation only dirties the depictors of containers, not composite components in general, when child components are added or removed.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public abstract class AbstractComponentDepictor<C extends Component> extends AbstractDepictor<C> implements ComponentDepictor<C>
{

	/**The listener that marks this depiction as dirty if direct children are added or removed, or <code>null</code> if the depicted component is not a container or is not installed.*/
	private DepictedCompositeComponentListener depictedCompositeComponentListener=null;

		/**@return The listener that marks this depiction as dirty if direct children are added or removed, or <code>null</code> if the depicted component is not a container or is not installed.*/
		protected DepictedCompositeComponentListener getDepictedCompositeComponentListener() {return depictedCompositeComponentListener;}

	/**The listener that listens for the change of a child's property, such as a value model's value, and marks the view as dirty.*/
	protected final PropertyChangeListener childPropertyChangeListener=new PropertyChangeListener()	//TODO only create this if needed
		{
			public void propertyChange(final PropertyChangeEvent propertyChangeEvent)	//if a property changes
			{
//TODO del Debug.trace("hey, a value property", propertyChangeEvent.getPropertyName(), "just changed!");
				setDepicted(false);	//show that we need general updates TODO improve to only indicate that the relevant property, such as VALUE_PROPERTY, changed			
			}
		};

	/**Default constructor.*/
	public AbstractComponentDepictor()
	{
		getIgnoredProperties().add(Component.INPUT_STRATEGY_PROPERTY);	//ignore Component.inputStrategy, because changes to the input strategy does not affect the component's view
		getIgnoredProperties().add(Component.VALID_PROPERTY);	//ignore Component.valid, because we don't want to mark composite components as dirty just because a child does not have valid input
	}
		
	/**Called when the depictor is installed in a component.
	If the component is a container, this version listens for container events and marks the view as needing updated.
	@param component The component into which this depictor is being installed.
	@exception NullPointerException if the given component is <code>null</code>.
	@exception IllegalStateException if this depictor is already installed in a component.
	@see #depictedPropertyChangeListener
	*/
	public void installed(final C component)
	{
		super.installed(component);	//perform the default installation
//TODO fix; dirtying the view of all composite components would make web pages reload when frames are added to the application frame; find a way to make this more consistent		if(component instanceof CompositeComponent)	//if the component is a composite component
		if(component instanceof Container)	//if the component is a container TODO why aren't we checking for a composite component?
		{
			depictedCompositeComponentListener=new DepictedCompositeComponentListener();	//create a change listener to listen for composite components changing
			final CompositeComponent compositeComponent=(CompositeComponent)component;	//cast the component to a composite component
			compositeComponent.addCompositeComponentListener(depictedCompositeComponentListener);	//listen for composite component events
		}
		if(component instanceof LayoutComponent)	//if the component is a layout component
		{
			final LayoutComponent layoutComponent=(LayoutComponent)component;	//cast the component to a layout component
			layoutComponent.getLayout().addPropertyChangeListener(getDepictedPropertyChangeListener());	//listen for layout property change events (including layout constraint property change events)
		}
		if(component instanceof ValueModel)	//if the component holds a value, listen for the value's properties changing
		{
			final Object value=((ValueModel<?>)component).getValue();	//get the current value
			if(value instanceof PropertyBindable)	//if there is a value that supports bound properties
			{
				((PropertyBindable)value).addPropertyChangeListener(getDepictedPropertyChangeListener());	//listen for changes in the properties of the value
			}
		}
	}

	/**Called when the depictor is uninstalled from a component.
	If the component is a container, this version stops listening for container events.
	@param component The component from which this depictor is being uninstalled.
	@exception NullPointerException if the given component is <code>null</code>.
	@exception IllegalStateException if this depictor is not installed in a component.
	@see #depictedPropertyChangeListener
	*/
	public void uninstalled(final C component)
	{
		super.uninstalled(component);	//perform the default uninstallation
//TODO fix; dirtying the view of all composite components would make web pages reload when frames are added to the application frame; find a way to make this more consistent		if(component instanceof CompositeComponent)	//if the component is a composite component
		if(component instanceof Container)	//if the component is a container
		{
			final CompositeComponent compositeComponent=(CompositeComponent)component;	//cast the component to a composite component
			compositeComponent.removeCompositeComponentListener(getDepictedCompositeComponentListener());	//stop listening for composite component events
			depictedCompositeComponentListener=null;	//release the comopsite component listener
		}
		if(component instanceof LayoutComponent)	//if the component is a layout component
		{
			final LayoutComponent layoutComponent=(LayoutComponent)component;	//cast the component to a layout component
			layoutComponent.getLayout().removePropertyChangeListener(getDepictedPropertyChangeListener());	//stop listening for layout property change events (including layout constraint property change events)
		}
		if(component instanceof ValueModel)	//if the component holds a value, stop listening for the value's properties changing
		{
			final Object value=((ValueModel<?>)component).getValue();	//get the current value
			if(value instanceof PropertyBindable)	//if there is a value that supports bound properties
			{
				((PropertyBindable)value).removePropertyChangeListener(getDepictedPropertyChangeListener());	//stop listening for changes in the properties of the value
			}
		}
	}

	/**Processes an event from the platform.
	This implementation handles {@link PlatformFocusEvent}.
	@param event The event to be processed.
	@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	*/
	public void processEvent(final PlatformEvent event)
	{
		if(event instanceof PlatformDropEvent)	//if this is a drop event
		{
			final PlatformDropEvent dropEvent=(PlatformDropEvent)event;	//get the drop event
			final C component=getDepictedObject();	//get the component
			if(dropEvent.getDepictedObject()!=component)	//if the event was meant for another depicted object
			{
				throw new IllegalArgumentException("Depict event "+event+" meant for depicted object "+dropEvent.getDepictedObject());
			}
			final Transferable<?> transferable=dropEvent.getDragSource().exportTransfer();	//export from the drag source
			if(transferable!=null)	//if we have data to transfer
			{
				component.importTransfer(transferable);	//import the transfer to the drop target
			}
		}
		if(event instanceof PlatformFocusEvent)	//if this is a focus event
		{
			final PlatformFocusEvent platformFocusEvent=(PlatformFocusEvent)event;	//get the focus event
			final C component=getDepictedObject();	//get the component
			if(platformFocusEvent.getDepictedObject()!=component)	//if the event was meant for another depicted object
			{
				throw new IllegalArgumentException("Depict event "+event+" meant for depicted object "+platformFocusEvent.getDepictedObject());
			}
			if(component instanceof InputFocusableComponent)	//if this is a focusable component, set all the focused components of the focus groups up the chain to form a path from application frame to focused component
			{
//Debug.trace("setting focus for component", component);
				final ApplicationFrame applicationFrame=component.getSession().getApplicationFrame();	//we'll work our way up until we get to the application frame
				InputFocusableComponent focusableComponent=(InputFocusableComponent)component;	//the component is focusable
				Component currentComponent=component;	//we'll start looking at the component receiving the focus
				do
				{
					final CompositeComponent parent=currentComponent.getParent();	//get the current component's parent
					if(parent instanceof InputFocusGroupComponent)	//if this parent is a focus group
					{
						final InputFocusGroupComponent focusGroup=(InputFocusGroupComponent)parent;	//get the parent as a focus group
						try
						{
							focusGroup.setInputFocusedComponent(focusableComponent);	//try to set the focused component in this group
							focusableComponent=focusGroup;	//now the focus group becomes the focusable component to receive focus in its parent focus group
						}
						catch(final PropertyVetoException propertyVetoException)	//if this component can't receive focus for some reason
						{
							break;	//stop setting the focus chain
						}
					}
					currentComponent=parent;	//go up a level and look at the parent
				}
				while(currentComponent!=null && currentComponent!=applicationFrame);	//when we reach the application frame (or run out of parents) there are no more focus groups for setting the focus
			}
		}
		super.processEvent(event);	//do the default processing of the event
	}

	/**Updates the depiction of the object.
	This implementation updates child components, if any.
	@exception IOException if there is an error updating the depiction.
	*/
	public void depict() throws IOException
	{
		depictChildren();	//depict the children
		super.depict();	//do the default depiction
	}

	/**Depicts any child components.
	@param component The depicted component.
	@exception IOException if there is an error updating the child depictions.
	*/
	protected void depictChildren() throws IOException
	{
		final C component=getDepictedObject();	//get the depicted object
		if(component instanceof CompositeComponent)	//if this is a composite component
		{
			for(final Component childComponent:((CompositeComponent)component).getChildComponents())	//for each child component
			{
				depictChild(childComponent);	//update this child
			}
		}
	}

	/**Depicts a single child.
	The child's depiction will be marked as updated if successful.
	@param childComponent The child component to depict.
	@exception IOException if there is an error updating the child depiction.
	@see Depictor#setDepicted(boolean)
	@see DepictedObject#depict()
	*/
	protected void depictChild(final Component childComponent) throws IOException
	{
		childComponent.getDepictor().setDepicted(false);	//mark the child component's view as generally not updated to prevent partial updates TODO improve this---in the future we may want child views to partially update, too, but not when the whole page is being rendered from scratch
			//TODO decide whether to mark the child view as not updated before updating; there is a slight chance it will be partially dirty and therefore only partially update 
		childComponent.depict();	//update the child view
	}

	/**Constructs an error message for all component errors.
	@return An error message constructed from all component errors, which may include resource references, or <code>null</code> if there are no errors.
	*/
	protected String getErrorMessage()
	{
			//TODO improve all this to work with the new notification framework
		final Notification notification=getDepictedObject().getNotification();	//get any notification of the component
		if(notification!=null)	//if the component has errors
		{
			final StringBuilder errorStringBuilder=new StringBuilder();	//we'll construct the error message
//TODO fix			for(final Throwable error:component.getErrors())	//for each error
			{
				final String message=notification.getMessage();	//get the error message
				if(errorStringBuilder.length()>0)	//if we've already included error messages
				{
					errorStringBuilder.append(';').append(' ');	//separate the messages
				}
/*TODO del if not wanted
				errorStringBuilder.append(ERROR_LABEL_RESOURCE_REFERENCE);	//"Error"
				errorStringBuilder.append(':').append(' ');	//separate the introduction from the actual error message
*/
				errorStringBuilder.append(message);	//add the error message, which may be a resource reference
			}
			return errorStringBuilder.toString();	//return the error message
		}
		else	//if there are no errors
		{
			return null;	//there is no error message
		}
	}

	/**Called when a depicted object bound property is changed.
	This method may also be called for objects related to the depicted object, so if specific properties are checked the event source should be verified to be the depicted object.
	This implementation marks the property as being modified if the property is not an ignored property.
	@param propertyChangeEvent An event object describing the event source and the property that has changed.
	@see #getIgnoredProperties()
	@see #setPropertyModified(String, boolean)
	*/ 
	protected void depictedObjectPropertyChange(final PropertyChangeEvent propertyChangeEvent)
	{
		super.depictedObjectPropertyChange(propertyChangeEvent);	//do the default property change functionality
		final C depictedObject=getDepictedObject();	//get the depicted object
		final Object source=propertyChangeEvent.getSource();	//get the source of the event
		final String propertyName=propertyChangeEvent.getPropertyName();	//get the name of the changing property
		final Object oldValue=propertyChangeEvent.getOldValue();	//get the old value
		final Object newValue=propertyChangeEvent.getOldValue();	//get the new value
//Debug.trace("property", propertyChangeEvent.getPropertyName(), "of source", source, "ID", source instanceof Component ? ((Component)source).getID() : "(non-component)", "change from", propertyChangeEvent.getOldValue(), "to", propertyChangeEvent.getNewValue());
		if(source==depictedObject)	//if this property change was on the depicted object
		{
			if(source instanceof Container && Container.LAYOUT_PROPERTY.equals(propertyName))	//if the source is a container and the container layout changed
			{
				if(oldValue instanceof Layout)	//if we know the old layout
				{
					((Layout<?>)oldValue).removePropertyChangeListener(getDepictedPropertyChangeListener());	//stop listening for layout property change events on the old layout (including layout constraint property change events)
				}
				if(newValue instanceof Layout)	//if we know the new layout
				{
					((Layout<?>)newValue).addPropertyChangeListener(getDepictedPropertyChangeListener());	//listen for layout property change events on the new layout (including layout constraint property change events)
				}
			}
			if(depictedObject instanceof ValueModel && ValueModel.VALUE_PROPERTY.equals(propertyName))	//if the component holds a value and its value is changing
			{
				if(oldValue instanceof PropertyBindable)	//if the old value supported bound properties
				{
					((PropertyBindable)oldValue).removePropertyChangeListener(childPropertyChangeListener);	//stop listening for changes in the properties of the old value
				}
				if(newValue instanceof PropertyBindable)	//if the new value supports bound properties
				{
					((PropertyBindable)newValue).addPropertyChangeListener(childPropertyChangeListener);	//listen for changes in the properties of the new value
				}
			}
		}
	}

	/**A listener that marks this depiction as dirty if direct children are added or deleted.
	@author Garret Wilson
	*/
	protected class DepictedCompositeComponentListener implements CompositeComponentListener
	{


		/**Called when a child component is added to a composite component.
		@param childComponentEvent The event indicating the added child component and the target parent composite component.
		*/
		public void childComponentAdded(final ComponentEvent childComponentEvent)
		{
			if(childComponentEvent.getTarget()==getDepictedObject())	//if the component as added as a direct child of this component
			{
				setDepicted(false);	//show that we need general updates
			}
		}

		/**Called when a child component is removed from a composite component.
		@param childComponentEvent The event indicating the removed child component and the target parent composite component.
		*/
		public void childComponentRemoved(final ComponentEvent childComponentEvent)
		{
			if(childComponentEvent.getTarget()==getDepictedObject())	//if the component as removed as a direct child of this component
			{
				setDepicted(false);	//show that we need general updates
			}
		}
	};

}
