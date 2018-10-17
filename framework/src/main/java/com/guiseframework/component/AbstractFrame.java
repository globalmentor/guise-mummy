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

package com.guiseframework.component;

import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.*;

import static java.util.Objects.*;

import static com.guiseframework.Resources.*;
import static com.guiseframework.theme.Theme.*;

import com.globalmentor.beans.GenericPropertyChangeListener;
import com.globalmentor.java.Objects;
import com.guiseframework.component.effect.Effect;
import com.guiseframework.event.*;
import com.guiseframework.model.Notification;
import com.guiseframework.prototype.*;
import com.guiseframework.style.Color;

/**
 * Abstract implementation of a frame. This implementation notifies the user when the frame does not validate in {@link #validate()}.
 * @author Garret Wilson
 */
public abstract class AbstractFrame extends AbstractEnumCompositeComponent<AbstractFrame.FrameComponent> implements Frame {

	/** The enumeration of frame components. */
	protected enum FrameComponent {
		/** The component, if any, that comprises the content of the frame. */
		CONTENT_COMPONENT,
		/** The menu, if any, of the frame. */
		MENU_COMPONENT,
		/** The toolbar, if any, of the frame. */
		TOOLBAR_COMPONENT,
		/** The control that provides a way of closing the frame. */
		CLOSE_ACTION_CONTROL;
	};

	/** The state of the frame. */
	private State state = State.CLOSED;

	@Override
	public State getState() {
		return state;
	}

	/**
	 * Sets the state of the frame. This is a bound property.
	 * @param newState The new state of the frame.
	 * @throws NullPointerException if the given state is <code>null</code>.
	 * @see Frame#STATE_PROPERTY
	 */
	protected void setState(final State newState) {
		if(state != newState) { //if the value is really changing
			final State oldState = state; //get the old value
			state = requireNonNull(newState, "State cannot be null."); //actually change the value
			firePropertyChange(STATE_PROPERTY, oldState, newState); //indicate that the value changed
			setMode(isModal() && newState != State.CLOSED ? Mode.EXCLUSIVE : null); //set exclusive modal mode if we are open and modal
		}
	}

	/** Whether the frame is modal if and when it is open. */
	private boolean modal = false;

	@Override
	public boolean isModal() {
		return modal;
	}

	@Override
	public void setModal(final boolean newModal) {
		if(modal != newModal) { //if the value is really changing
			final boolean oldModal = modal; //get the current value
			modal = newModal; //update the value
			firePropertyChange(MODAL_PROPERTY, Boolean.valueOf(oldModal), Boolean.valueOf(newModal));
			setMode(newModal && getState() != State.CLOSED ? Mode.EXCLUSIVE : null); //set exclusive modal mode if we are open and modal
		}
	}

	/** The current mode of interaction, or <code>null</code> if the component is in a modeless state. */
	private Mode mode = null;

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public void setMode(final Mode newMode) {
		if(mode != newMode) { //if the value is really changing
			final Mode oldMode = mode; //get the old value
			mode = newMode; //actually change the value
			firePropertyChange(MODE_PROPERTY, oldMode, newMode); //indicate that the value changed
		}
	}

	/** Whether the frame is movable. */
	private boolean movable = true;

	@Override
	public boolean isMovable() {
		return movable;
	}

	@Override
	public void setMovable(final boolean newMovable) {
		if(movable != newMovable) { //if the value is really changing
			final boolean oldMovable = movable; //get the current value
			movable = newMovable; //update the value
			firePropertyChange(MOVABLE_PROPERTY, Boolean.valueOf(oldMovable), Boolean.valueOf(newMovable));
		}
	}

	/** Whether the frame can be resized. */
	private boolean resizable = true;

	@Override
	public boolean isResizable() {
		return resizable;
	}

	@Override
	public void setResizable(final boolean newResizable) {
		if(resizable != newResizable) { //if the value is really changing
			final boolean oldResizable = resizable; //get the current value
			resizable = newResizable; //update the value
			firePropertyChange(MOVABLE_PROPERTY, Boolean.valueOf(oldResizable), Boolean.valueOf(newResizable));
		}
	}

	/** The related component such as a popup source, or <code>null</code> if the frame is not related to another component. */
	private Component relatedComponent = null;

	@Override
	public Component getRelatedComponent() {
		return relatedComponent;
	}

	@Override
	public void setRelatedComponent(final Component newRelatedComponent) {
		if(relatedComponent != newRelatedComponent) { //if the value is really changing
			final Component oldRelatedComponent = relatedComponent; //get the old value
			relatedComponent = newRelatedComponent; //actually change the value
			firePropertyChange(RELATED_COMPONENT_PROPERTY, oldRelatedComponent, newRelatedComponent); //indicate that the value changed
		}
	}

	/** The background color of the title, or <code>null</code> if no background color is specified for the title. */
	private Color titleBackgroundColor = null;

	@Override
	public Color getTitleBackgroundColor() {
		return titleBackgroundColor;
	}

	@Override
	public void setTitleBackgroundColor(final Color newTitleBackgroundColor) {
		if(!Objects.equals(titleBackgroundColor, newTitleBackgroundColor)) { //if the value is really changing
			final Color oldTitleBackgroundColor = titleBackgroundColor; //get the old value
			titleBackgroundColor = newTitleBackgroundColor; //actually change the value
			firePropertyChange(TITLE_BACKGROUND_COLOR_PROPERTY, oldTitleBackgroundColor, newTitleBackgroundColor); //indicate that the value changed
		}
	}

	/** Whether the title bar is visible. */
	private boolean titleVisible = true;

	@Override
	public boolean isTitleVisible() {
		return titleVisible;
	}

	@Override
	public void setTitleVisible(final boolean newTitleVisible) {
		if(titleVisible != newTitleVisible) { //if the value is really changing
			final boolean oldTitleVisible = titleVisible; //get the current value
			titleVisible = newTitleVisible; //update the value
			firePropertyChange(TITLE_VISIBLE_PROPERTY, Boolean.valueOf(oldTitleVisible), Boolean.valueOf(newTitleVisible));
		}
	}

	/** The effect used for opening the frame, or <code>null</code> if there is no open effect. */
	private Effect openEffect = null;

	@Override
	public Effect getOpenEffect() {
		return openEffect;
	}

	@Override
	public void setOpenEffect(final Effect newOpenEffect) {
		if(openEffect != newOpenEffect) { //if the value is really changing
			final Effect oldOpenEffect = openEffect; //get the old value
			openEffect = newOpenEffect; //actually change the value
			firePropertyChange(OPEN_EFFECT_PROPERTY, oldOpenEffect, newOpenEffect); //indicate that the value changed
		}
	}

	@Override
	public Component getContent() {
		return getComponent(FrameComponent.CONTENT_COMPONENT);
	}

	@Override
	public void setContent(final Component newContent) {
		final Component oldContent = setComponent(FrameComponent.CONTENT_COMPONENT, newContent); //set the component
		if(oldContent != newContent) { //if the component really changed
			firePropertyChange(CONTENT_PROPERTY, oldContent, newContent); //indicate that the value changed
		}
	}

	@Override
	public Menu getMenu() {
		return (Menu)getComponent(FrameComponent.MENU_COMPONENT);
	}

	@Override
	public void setMenu(final Menu newMenu) {
		final Menu oldMenu = (Menu)setComponent(FrameComponent.MENU_COMPONENT, newMenu); //set the component
		if(oldMenu != newMenu) { //if the component really changed
			firePropertyChange(MENU_PROPERTY, oldMenu, newMenu); //indicate that the value changed
			prototypeProvisionStrategy.processPrototypeProvisions(); //process the prototype provisions again, now that we have a new menu
		}
	}

	@Override
	public Toolbar getToolbar() {
		return (Toolbar)getComponent(FrameComponent.TOOLBAR_COMPONENT);
	}

	@Override
	public void setToolbar(final Toolbar newToolbar) {
		final Toolbar oldToolbar = (Toolbar)setComponent(FrameComponent.TOOLBAR_COMPONENT, newToolbar); //set the component
		if(oldToolbar != newToolbar) { //if the component really changed
			firePropertyChange(TOOLBAR_PROPERTY, oldToolbar, newToolbar); //indicate that the value changed
			prototypeProvisionStrategy.processPrototypeProvisions(); //process the prototype provisions again, now that we have a new toolbar
		}
	}

	/** The internal prototype provider that provides the default prototypes provided by this frame. */
	private final DefaultPrototypeProvider defaultPrototypeProvider = new DefaultPrototypeProvider();

	/** The strategy for processing prototypes provisions from child prototype providers, along with this frame's prototype provisions. */
	private final FrameMenuToolPrototypeProvisionStrategy prototypeProvisionStrategy = new FrameMenuToolPrototypeProvisionStrategy(this,
			defaultPrototypeProvider);

	/** @return The strategy for processing prototypes provisions from child prototype providers, along with this frame's prototype provisions. */
	protected FrameMenuToolPrototypeProvisionStrategy getPrototypeProvisionStrategy() {
		return prototypeProvisionStrategy;
	}

	/** The action listener for closing the frame. */
	private final ActionListener closeActionListener;

	@Override
	public ActionControl getCloseActionControl() {
		return (ActionControl)getComponent(FrameComponent.CLOSE_ACTION_CONTROL);
	}

	@Override
	public void setCloseActionControl(final ActionControl newCloseActionControl) {
		final ActionControl oldCloseActionControl = (ActionControl)setComponent(FrameComponent.CLOSE_ACTION_CONTROL, newCloseActionControl); //set the component
		if(oldCloseActionControl != newCloseActionControl) { //if the component really changed
			if(oldCloseActionControl != null) { //if we had an old close action
				oldCloseActionControl.removeActionListener(closeActionListener); //remove the close action listener from the old control (this will have no effect if we are using our default control, which had a listener to the prototype rather than to the control itself)
			}
			if(newCloseActionControl != null) { //if we have a new close action
				newCloseActionControl.addActionListener(closeActionListener); //listen for the new action control and close the frame in response
			}
			firePropertyChange(CLOSE_ACTION_CONTROL_PROPERTY, oldCloseActionControl, newCloseActionControl); //indicate that the value changed
		}
	}

	/** The input focus strategy for this input focus group. */
	private InputFocusStrategy inputFocusStrategy = new DefaultInputFocusStrategy();

	@Override
	public InputFocusStrategy getInputFocusStrategy() {
		return inputFocusStrategy;
	}

	@Override
	public void setInputFocusStrategy(final InputFocusStrategy newInputFocusStrategy) {
		if(!inputFocusStrategy.equals(newInputFocusStrategy)) { //if the value is really changing
			final InputFocusStrategy oldInputFocusStrategy = inputFocusStrategy; //get the old value
			inputFocusStrategy = newInputFocusStrategy; //actually change the value
			firePropertyChange(INPUT_FOCUS_STRATEGY_PROPERTY, oldInputFocusStrategy, newInputFocusStrategy); //indicate that the value changed
		}
	}

	/** The component within this group that has the input focus, or <code>null</code> if no component currently has the input focus. */
	private InputFocusableComponent inputFocusedComponent = null;

	@Override
	public InputFocusableComponent getInputFocusedComponent() {
		return inputFocusedComponent;
	}

	@Override
	public void setInputFocusedComponent(final InputFocusableComponent newInputFocusedComponent) throws PropertyVetoException {
		if(!Objects.equals(inputFocusedComponent, newInputFocusedComponent)) { //if the value is really changing
			final InputFocusStrategy oldInputFocusedComponent = inputFocusStrategy; //get the old value
			inputFocusedComponent = newInputFocusedComponent; //actually change the value
			firePropertyChange(INPUT_FOCUSED_COMPONENT_PROPERTY, oldInputFocusedComponent, newInputFocusedComponent); //indicate that the value changed
		}
	}

	/** The action prototype for closing the frame. */
	private final ActionPrototype closeActionPrototype;

	@Override
	public ActionPrototype getCloseActionPrototype() {
		return closeActionPrototype;
	}

	/**
	 * Component constructor.
	 * @param component The single child component, or <code>null</code> if this frame should have no child component.
	 */
	public AbstractFrame(final Component component) {
		super(FrameComponent.values()); //construct the parent class
		closeActionListener = new ActionListener() { //create an action listener for closing

			@Override
			public void actionPerformed(final ActionEvent actionEvent) { //if the close action is initiated
				close(); //close the frame
			}

		};

		//close action prototype
		closeActionPrototype = new AbstractActionPrototype(LABEL_CLOSE, GLYPH_CLOSE) { //create the prototype for the close action

			@Override
			protected void action(final int force, final int option) { //TODO improve to call close action listener directly if wanted
			}
		};
		closeActionPrototype.addActionListener(closeActionListener); //close the frame when the close action is performed
		//default close action control
		final Link closeActionControl = new Link(closeActionPrototype); //create a close action control from the prototype
		closeActionControl.setLabelDisplayed(false); //don't display the label
		setComponent(FrameComponent.CLOSE_ACTION_CONTROL, closeActionControl); //set our default close action control; don't use setCloseActionControl(), as this will result in the action listener being installed twice TODO maybe just remove the listener altogether, and require the new control be created from the prototype
		setComponent(FrameComponent.CONTENT_COMPONENT, component); //set the component directly, because child classes may prevent the setContent() method from changing the component 
		updateDefaultPrototypeProvisions(); //update the prototype provisions
	}

	@Override
	public void open() {
		if(getState() == State.CLOSED) { //if the state is closed
			final ApplicationFrame applicationFrame = getSession().getApplicationFrame(); //get the application frame
			if(this != applicationFrame) { //if this is not the application frame
				getSession().getApplicationFrame().addChildFrame(this); //add the frame to the application frame
			}
			setState(State.OPEN); //change the state
		}
	}

	@Override
	public void open(final boolean modal) {
		setModal(modal); //update the modality
		open(); //open the frame normally
	}

	@Override
	public void open(final GenericPropertyChangeListener<Mode> modeChangeListener) {
		addPropertyChangeListener(MODE_PROPERTY, modeChangeListener); //add the mode property change listener
		open(true); //open modally
	}

	@Override
	public boolean canClose() {
		return true; //by default always allow the frame to be closed
	}

	@Override
	public final void close() {
		if(getState() != State.CLOSED) { //if the frame is not already closed
			if(canClose()) { //if the frame can close
				closeImpl(); //actually close the frame
			}
		}
	}

	/** Implementation of frame closing. */
	protected void closeImpl() {
		//TODO del Log.trace("ready to remove frame");
		final ApplicationFrame applicationFrame = getSession().getApplicationFrame(); //get the application frame
		if(this != applicationFrame) { //if this is not the application frame
			getSession().getApplicationFrame().removeChildFrame(this); //remove the frame from the application frame
		}
		setState(State.CLOSED); //change the state
	}

	@Override
	public boolean validate() {
		if(!super.validate()) { //validate the component normally; if the component does not validate
			Notification notification = getNotification(); //see if this panel has any notification
			if(notification == null) { //if we don't have a notification
				final Component contentComponent = getContent(); //get the content component
				if(contentComponent != null) { //if there is a content component
					final List<Notification> notifications = getNotifications(contentComponent); //get the notifications from the content component
					if(!notifications.isEmpty()) { //if there are notifications
						notification = notifications.get(0); //use the first notification
					}
				}
			}
			if(notification == null) { //if we didn't find a custom notification
				notification = new Notification(VALIDATION_FALSE_MESSAGE_RESOURCE_REFERENCE, Notification.Severity.ERROR); //use a general validation notification
			}
			getSession().notify(notification); //indicate that there was a validation error
		}
		return isValid(); //return the current valid state
	}

	/**
	 * Updates the default prototype provisions.
	 * @see #provideDefaultPrototypes()
	 */
	protected final void updateDefaultPrototypeProvisions() {
		defaultPrototypeProvider.updateDefaultPrototypeProvisions(); //update the default prototype provisions			
	}

	/**
	 * Provides default prototype provisions to be integrated into the menu and/or toolbar. The default prototype provisions are separate from those provided by
	 * any child component prototype producers. Subclasses may override this method to add or modify the default provided prototype provisions.
	 * @return A mutable set of default prototype provisions.
	 */
	protected Set<PrototypeProvision<?>> provideDefaultPrototypes() {
		return new HashSet<PrototypeProvision<?>>(); //this version provides no default prototypes
	}

	/**
	 * The default implementation of a prototype provider for a frame.
	 * @author Garret Wilson
	 */
	protected class DefaultPrototypeProvider extends AbstractPrototypeProvider {

		@Override
		protected Set<PrototypeProvision<?>> providePrototypes() {
			return provideDefaultPrototypes(); //return the frame's default prototypes
		}

		/**
		 * Updates the available prototype provisions. This method is provided to allow class access to the {@link #updatePrototypeProvisions()} method, to which
		 * this method delegates.
		 */
		protected final void updateDefaultPrototypeProvisions() {
			updatePrototypeProvisions(); //update the prototype previsions			
		}

	}

}
