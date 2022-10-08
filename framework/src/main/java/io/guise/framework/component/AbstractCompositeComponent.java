/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.component;

import java.beans.PropertyChangeListener;
import java.io.IOException;

import static java.util.Objects.*;

import com.globalmentor.beans.*;
import com.globalmentor.event.TargetedEvent;

import io.clogr.Clogged;
import io.guise.framework.event.*;
import io.guise.framework.input.*;
import io.guise.framework.model.InfoModel;

/**
 * An abstract implementation of a composite component. Every child component must be added or removed using
 * {@link AbstractLayoutComponent#addComponent(Component)} and {@link AbstractLayoutComponent#removeComponent(Component)}, although other actions may take
 * place. This version listens for the {@link Component#VALID_PROPERTY} of each child component and updates the valid status of this component in response. This
 * version listens for child notifications and fires a copy of the {@link NotificationEvent}, retaining the original event target. This version listens for
 * child components being added or removed and fires a copy of the {@link ComponentEvent}, retaining the original event target.
 * @author Garret Wilson
 */
public abstract class AbstractCompositeComponent extends AbstractComponent implements CompositeComponent, Clogged {

	/**
	 * The lazily-created property change listener to listen for changes in valid status and call
	 * {@link #childComponentValidPropertyChanged(Component, boolean, boolean)}.
	 */
	private PropertyChangeListener validChangeListener = null;

	/**
	 * The lazily-created property change listener to listen for changes in valid status and call
	 * {@link #childComponentValidPropertyChanged(Component, boolean, boolean)}.
	 */
	private PropertyChangeListener getValidChangeListener() {
		if(validChangeListener == null) { //if there is no valid change listener
			validChangeListener = new AbstractGenericPropertyChangeListener<Boolean>() { //create a new valid change listener

				@Override
				public void propertyChange(GenericPropertyChangeEvent<Boolean> propertyChangeEvent) { //if the child component's valid status changes
					//TODO do we want to update valid here?
					childComponentValidPropertyChanged((Component)propertyChangeEvent.getSource(), propertyChangeEvent.getOldValue().booleanValue(),
							propertyChangeEvent.getNewValue().booleanValue()); //notify this component that a child component's valid status changed
				}

			};
		}
		return validChangeListener; //return the valid change listener
	}

	/** The lazily-created property change listener to listen for changes in visible and display status and update the valid status in response. */
	private PropertyChangeListener displayVisibleChangeListener = null;

	/** The lazily-created property change listener to listen for changes in visible and display status and update the valid status in response. */
	private PropertyChangeListener getDisplayVisibleChangeListener() {
		if(displayVisibleChangeListener == null) { //if there is no display/visible change listener
			displayVisibleChangeListener = new AbstractGenericPropertyChangeListener<Boolean>() { //create a new display/visible change listener

				@Override
				public void propertyChange(GenericPropertyChangeEvent<Boolean> propertyChangeEvent) { //if the child component's display or visible status changes
					//TODO maybe add a flag to prevent infinite loops
					updateValid(); //update this composite component's valid state, because the validity of this component only depends on displayed, visible child components
				}

			};
		}
		return validChangeListener; //return the valid change listener
	}

	/**
	 * The lazily-created notification listener to listen for child notifications and fire a copy of the {@link NotificationEvent}, retaining the original event
	 * target.
	 */
	private NotificationListener repeatNotificationListener = null;

	/**
	 * Returns the notification listener to listen for child notifications and fire a copy of the {@link NotificationEvent}, retaining the original event target.
	 * This method is not thread-safe, and must be externally synchronized.
	 * @return The lazily-created notification listener to listen for child notifications and fire a copy of the {@link NotificationEvent}, retaining the original
	 *         event target.
	 */
	private NotificationListener getRepeatNotificationListener() {
		if(repeatNotificationListener == null) { //if there is no notification listener
			repeatNotificationListener = new NotificationListener() { //create a new notification listener

				@Override
				public void notified(final NotificationEvent notificationEvent) { //if the child component sends a notification
					fireNotified(new NotificationEvent(this, notificationEvent)); //fire a copy of the notification event to our listeners, keeping the original target						
				}

			};
		}
		return repeatNotificationListener; //return the notification listener
	}

	/**
	 * The lazily-created composite component listener to listen for child components being added or removed and fire a copy of the {@link ComponentEvent},
	 * retaining the original event target.
	 */
	private CompositeComponentListener repeatCompositeComponentListener = null;

	/**
	 * Returns the composite component listener to listen for child components being added or removed and fire a copy of the {@link ComponentEvent}, retaining the
	 * original event target. This method is not thread-safe, and must be externally synchronized.
	 * @return The lazily-created composite component listener to listen for child components being added or removed and fire a copy of the {@link ComponentEvent}
	 *         , retaining the original event target.
	 */
	private CompositeComponentListener getRepeatCompositeComponentListener() {
		if(repeatCompositeComponentListener == null) { //if there is no composite component listener
			repeatCompositeComponentListener = new CompositeComponentListener() { //create a new composite component listener

				@Override
				public void childComponentAdded(final ComponentEvent childComponentEvent) { //if a child component is added
					fireChildComponentAdded(new ComponentEvent(AbstractCompositeComponent.this, childComponentEvent)); //fire a copy of the component event to our listeners, keeping the original target
				}

				@Override
				public void childComponentRemoved(final ComponentEvent childComponentEvent) { //if a child component is removed
					fireChildComponentRemoved(new ComponentEvent(AbstractCompositeComponent.this, childComponentEvent)); //fire a copy of the component event to our listeners, keeping the original target
				}

			};
		}
		return repeatCompositeComponentListener; //return the notification listener
	}

	/**
	 * Initializes a component to be added as a child component of this composite component. This method should be called for every child component added to this
	 * composite component. This version installs a listener for the component's valid status. This version installs a listener to refire copies of notification
	 * events. This version installs a listener to refire copies of composite component events. This version loads the preferences of the child component, but not
	 * its descendants.
	 * @param childComponent The component to add to this component.
	 * @see Component#loadPreferences(boolean)
	 */
	protected void initializeChildComponent(final Component childComponent) {
		try {
			childComponent.loadPreferences(false); //load preferences for the child component only
		} catch(final IOException ioException) { //if there was an error loading preferences
			getLogger().warn("", ioException); //log a warning			
		}
		childComponent.addPropertyChangeListener(DISPLAYED_PROPERTY, getDisplayVisibleChangeListener()); //listen for changes in the component's displayed status and update this component's valid status in response
		childComponent.addPropertyChangeListener(VALID_PROPERTY, getValidChangeListener()); //listen for changes in the component's valid status and update this component's valid status in response
		childComponent.addPropertyChangeListener(VISIBLE_PROPERTY, getDisplayVisibleChangeListener()); //listen for changes in the component's visible status and update this component's valid status in response
		childComponent.addNotificationListener(getRepeatNotificationListener()); //listen for component notifications and refire notification events in response
		if(childComponent instanceof CompositeComponent) { //if the child component is a composite component
			((CompositeComponent)childComponent).addCompositeComponentListener(getRepeatCompositeComponentListener()); //listen for components being added or removed refire component events in response
		}
	}

	/**
	 * Uninitializes a comopnent to be removed as a child comopnent of this composite component. This method should be called for every child component removed
	 * from this composite component. This version uninstalls a listener for the component's valid status. This version uninstalls a listener to refire copies of
	 * notification events. This version uninstalls a listener to refire copies of composite component events. This version saves any preferences of the child
	 * component and any descendants.
	 * @param childComponent The component to remove from this component.
	 * @see Component#savePreferences(boolean)
	 */
	protected void uninitializeChildComponent(final Component childComponent) {
		try {
			childComponent.savePreferences(true); //save preferences for the entire child component tree
		} catch(final IOException ioException) { //if there was an error saving preferences
			getLogger().warn("", ioException); //log a warning			
		}
		childComponent.removePropertyChangeListener(DISPLAYED_PROPERTY, getDisplayVisibleChangeListener()); //stop listening for changes in the component's displayed status
		childComponent.removePropertyChangeListener(VALID_PROPERTY, getValidChangeListener()); //stop listening for changes in the component's valid status
		childComponent.removePropertyChangeListener(VISIBLE_PROPERTY, getDisplayVisibleChangeListener()); //stop listening for changes in the component's visible status
		childComponent.removeNotificationListener(getRepeatNotificationListener()); //stop listening for component notifications
		if(childComponent instanceof CompositeComponent) { //if the child component is a composite component
			((CompositeComponent)childComponent).removeCompositeComponentListener(getRepeatCompositeComponentListener()); //stop listening for components being added or removed
		}
	}

	/**
	 * Called when a descendant component is added to a descendant composite component. The target of the event indicates the descendant composite component to
	 * which the descendant component was added. The event is propogated to this component's parent, if any. This method is called by child components and should
	 * not be directly invoked by an application.
	 * @param childComponentEvent The event indicating the added child component and the target parent composite component.
	 */
	/*TODO del if not wanted
		public void descendantComponentAdded(final ComponentEvent childComponentEvent)
		{
			final CompositeComponent parentComponent=getParent();	//get the parent component, if any
			if(parentComponent!=null) {	//if there is a parent component
				parentComponent.descendantComponentAdded(new ComponentEvent(this, childComponentEvent));	//give the parent a copy of this event with the same target
			}
			
		}
	*/

	/**
	 * Called when a descendant component is removed from a descendant composite component. The target of the event indicates the descendant composite component
	 * from which the descendant component was removed. The event is propogated to this component's parent, if any. This method is called by child components and
	 * should not be directly invoked by an application.
	 * @param childComponentEvent The event indicating the removed child component and the target parent composite component.
	 */
	/*TODO del if not wanted
		public void descendantComponentRemoved(final ComponentEvent childComponentEvent)
		{
			
		}
	*/

	/**
	 * Info model constructor.
	 * @param infoModel The component info model.
	 * @throws NullPointerException if the given info model is <code>null</code>.
	 */
	public AbstractCompositeComponent(final InfoModel infoModel) {
		super(infoModel); //construct the parent class
	}

	/**
	 * Called when the {@link Component#VALID_PROPERTY} of a child component changes. Every child version should call this version. This version updates the
	 * composite component's valid state by calling {@link #updateValid()}.
	 * @param childComponent The child component the valid property of which changed.
	 * @param oldValid The old valid property.
	 * @param newValid The new valid property.
	 */
	protected void childComponentValidPropertyChanged(final Component childComponent, final boolean oldValid, final boolean newValid) {
		updateValid(); //update this composite component's valid state		
	}

	/**
	 * Called when a child fires a NotificationEvent {@link Component#VALID_PROPERTY} of a child component changes. Every child version should call this version.
	 * This version updates the composite component's valid state by calling {@link #updateValid()}.
	 * @param childComponent The child component the valid property of which changed.
	 * @param oldValid The old valid property.
	 * @param newValid The new valid property.
	 */
	/*TODO fix
		protected void childComponentValidPropertyChanged(final Component childComponent, final boolean oldValid, final boolean newValid)
		{
			updateValid();	//update this composite component's valid state		
		}
	*/

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version calls {@link #determineChildrenValid()}.
	 * </p>
	 */
	@Override
	protected boolean determineValid() {
		return super.determineValid() && determineChildrenValid(); //determine if the super class and children are valid
	}

	/**
	 * Checks the state of child components for validity. This version checks all child components for validity using the current {@link Component#isValid()};
	 * child component are not asked to update their valid state. Children that are not visible and/or not displayed are not taken into account.
	 * @return <code>true</code> if the relevant children pass all validity tests.
	 */
	protected boolean determineChildrenValid() {
		//TODO fix Log.trace("ready to determine children valid in", this);
		for(final Component childComponent : getChildComponents()) { //for each child component
			//TODO del Log.trace("in", this, "child", childComponent, "is valid", childComponent.isValid());			
			if(childComponent.isDisplayed() && childComponent.isVisible() && !childComponent.isValid()) { //if this child component is displayed and visible, but not valid
				//TODO del Log.trace("in", this, "found non-valid child", childComponent);
				return false; //the composite component should not be considered valid
			}
		}
		//	TODO fix Log.trace("in", this, "child", "all children are valid");
		return true; //all children are valid
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version first calls {@link #validateChildren()} so that all children will be validated before checks are performed on this component.
	 * </p>
	 */
	@Override
	public boolean validate() {
		validateChildren(); //validate all children
		super.validate(); //validate the component normally
		//TODO del Log.trace("in panel", this, "ready to return", isValid(), "for isValid()");
		return isValid(); //return the current valid state
	}

	/**
	 * Validates the user input of child components. Children that are not visible and/or not displayed are not taken into account.
	 * @return <code>true</code> if all child validations return <code>true</code>.
	 */
	protected boolean validateChildren() {
		boolean result = true; //start by assuming all child components will validate 
		for(final Component childComponent : getChildComponents()) { //for each child component
			if(childComponent.isDisplayed() && childComponent.isVisible() && !childComponent.validate()) { //if this child component is displayed and visible, but doesn't validate
				result = false; //the result will be false
			}
		}
		return result; //return whether all child components validated
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version recursively calls the {@link #resetTheme()} method of all child components before resetting the theme of this component.
	 * </p>
	 */
	@Override
	public void resetTheme() {
		for(final Component childComponent : getChildComponents()) { //for each child component
			childComponent.resetTheme(); //tell the child component to reset its theme
		}
		super.resetTheme(); //update the theme for this component normally
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version recursively calls the {@link #updateTheme()} method of all child components before updating the theme of this component.
	 * </p>
	 */
	@Override
	public void updateTheme() throws IOException {
		for(final Component childComponent : getChildComponents()) { //for each child component
			childComponent.updateTheme(); //tell the child component to update its theme
		}
		super.updateTheme(); //update the theme for this component normally
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version loads the preferences of child components if descendants should be included.
	 * </p>
	 */
	@Override
	public void loadPreferences(final boolean includeDescendants) throws IOException {
		if(includeDescendants) { //if descendants should be included
			for(final Component childComponent : getChildComponents()) { //for each child component
				childComponent.loadPreferences(includeDescendants); //tell the child component to load its preferences
			}
		}
		super.loadPreferences(includeDescendants); //load preferences normally
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version loads the preferences of child components if descendants should be included.
	 * </p>
	 */
	@Override
	public void savePreferences(final boolean includeDescendants) throws IOException {
		super.savePreferences(includeDescendants); //save preferences normally
		if(includeDescendants) { //if descendants should be included
			for(final Component childComponent : getChildComponents()) { //for each child component
				childComponent.savePreferences(includeDescendants); //tell the child component to save its preferences
			}
		}
	}

	@Override
	public void dispatchInputEvent(final InputEvent inputEvent) {
		//Log.trace("in composite component", this, "ready to do default dispatching of input event", inputEvent);
		if(!inputEvent.isConsumed()) { //if the input has not been consumed
			if(inputEvent instanceof FocusedInputEvent) { //if this is a focused input event, the target will be the focused child of the focus group ancestor of this component (or this component, if this component is a focused group)
				//Log.trace("this is a focused event");
				Component component = this; //start with this component
				do {
					if(component instanceof InputFocusGroupComponent) { //if we found a focus group
						//Log.trace("component", component, "is the focus group");
						final InputFocusGroupComponent focusGroup = (InputFocusGroupComponent)component; //get the focus group
						final InputFocusableComponent focusedComponent = focusGroup.getInputFocusedComponent(); //get the focused component
						if(focusedComponent != null) { //if there is a focused component
							//Log.trace("ready to dispatch to focused component", focusedComponent);
							dispatchInputEvent(inputEvent, focusedComponent); //dispatch the event to the focused component
							//Log.trace("done dispatching to focused component", focusedComponent);
						}
						break;
					} else { //if we didn't find a focus group
						component = component.getParent(); //walk up the tree
					}
				} while(component != null); //keep looking until we run out of components
			} else if(inputEvent instanceof TargetedEvent) { //if this is a targeted event
				//Log.trace("this is a targeted event");
				final Object targetObject = ((TargetedEvent)inputEvent).getTarget(); //get the event target
				if(targetObject instanceof Component) { //if the target is a component other than any focus target we might have used earlier
					dispatchInputEvent(inputEvent, (Component)targetObject); //dispatch the event to the event target						
				}
			} else { //if this wasn't a focused or targeted event, dispatch the event to all child components
				//Log.trace("this is an unconsumed non-targeted event; ready to send to children");
				for(final Component childComponent : getChildComponents()) { //for each child component
					if(inputEvent.isConsumed()) { //if the event has been consumed
						return; //stop further processing
					}
					childComponent.dispatchInputEvent(inputEvent); //dispatch the event to this child components
				}
				//Log.trace("finishing sending event to children");
			}
			if(!inputEvent.isConsumed()) { //if the event has not been consumed
				//Log.trace("event still not consumed, ready to do default dispatching");
				super.dispatchInputEvent(inputEvent); //do the default dispatching
			}
		}
	}

	/**
	 * Dispatches an input event to the specified target child hierarchy. If the given target is not a descendant of this component, or if the target is this
	 * component, no action occurs.
	 * @param inputEvent The input event to dispatch.
	 * @param target The target indicating the child hierarchy to which this event should be directed.
	 * @throws NullPointerException if the given event and/or target is <code>null</code>.
	 * @see #fireInputEvent(InputEvent)
	 * @see InputEvent#isConsumed()
	 * @see TargetedEvent
	 * @see MouseEvent
	 */
	protected void dispatchInputEvent(final InputEvent inputEvent, Component target) {
		requireNonNull(target, "Target cannot be null");
		while(target != null && target != this) { //keep going until we reach this component or run out of ancestors, the latter of which means that this component is not in the target branch at all
			final CompositeComponent parent = target.getParent(); //get the target ancestor's parent
			if(parent == this) { //if we are the target ancestor's immediate parent
				target.dispatchInputEvent(inputEvent); //dispatch the event to the target's ancestor, which is this component's child
			}
			target = parent; //walk up the chain
		}
	}

	@Override
	public void addCompositeComponentListener(final CompositeComponentListener compositeComponentListener) {
		getEventListenerManager().add(CompositeComponentListener.class, compositeComponentListener); //add the listener
	}

	@Override
	public void removeCompositeComponentListener(final CompositeComponentListener compositeComponentListener) {
		getEventListenerManager().remove(CompositeComponentListener.class, compositeComponentListener); //remove the listener
	}

	/**
	 * Fires a component added event to all registered composite component listeners. This method delegates to {@link #fireChildComponentAdded(ComponentEvent)}.
	 * @param childComponent The child component added.
	 * @see CompositeComponentListener
	 * @see ComponentEvent
	 */
	protected void fireChildComponentAdded(final Component childComponent) {
		if(getEventListenerManager().hasListeners(CompositeComponentListener.class)) { //if there are composite component listeners registered
			fireChildComponentAdded(new ComponentEvent(this, childComponent)); //create and fire a new component event
		}
	}

	/**
	 * Fires a given component added event to all registered composite component listeners.
	 * @param childComponentEvent The child component event to fire.
	 */
	protected void fireChildComponentAdded(final ComponentEvent childComponentEvent) {
		for(final CompositeComponentListener compositeComponentListener : getEventListenerManager().getListeners(CompositeComponentListener.class)) { //for each composite component listener
			compositeComponentListener.childComponentAdded(childComponentEvent); //dispatch the component event to the listener
		}
	}

	/**
	 * Fires a component removed event to all registered composite component listeners. This method delegates to
	 * {@link #fireChildComponentRemoved(ComponentEvent)}.
	 * @param childComponent The child component removed.
	 * @see CompositeComponentListener
	 * @see ComponentEvent
	 */
	protected void fireChildComponentRemoved(final Component childComponent) {
		if(getEventListenerManager().hasListeners(CompositeComponentListener.class)) { //if there are composite component listeners registered
			fireChildComponentRemoved(new ComponentEvent(this, childComponent)); //create and fire a new component event
		}
	}

	/**
	 * Fires a given component removed event to all registered composite component listeners.
	 * @param childComponentEvent The child component event to fire.
	 * @see CompositeComponentListener
	 */
	protected void fireChildComponentRemoved(final ComponentEvent childComponentEvent) {
		for(final CompositeComponentListener compositeComponentListener : getEventListenerManager().getListeners(CompositeComponentListener.class)) { //for each composite component listener
			compositeComponentListener.childComponentRemoved(childComponentEvent); //dispatch the component event to the listener
		}
	}

}
