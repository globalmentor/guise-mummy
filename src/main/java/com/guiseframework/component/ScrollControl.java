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

import static com.globalmentor.java.Objects.*;

import com.globalmentor.beans.AbstractGenericPropertyChangeListener;
import com.globalmentor.beans.GenericPropertyChangeEvent;
import com.guiseframework.model.*;

/**
 * A control that allows the user to scroll its contents The control's contents are specified using {@link #setContent(Component)}.
 * @author Garret Wilson
 */
public class ScrollControl extends AbstractEnumCompositeComponent<ScrollControl.ScrollComponent> implements ContentComponent, Control {

	/** The enumeration of frame components. */
	protected enum ScrollComponent {
		CONTENT_COMPONENT
	};

	/** The enableable object decorated by this component. */
	private final Enableable enableable;

	/** @return The enableable object decorated by this component. */
	protected Enableable getEnableable() {
		return enableable;
	}

	@Override
	public Component getContent() {
		return getComponent(ScrollComponent.CONTENT_COMPONENT);
	}

	@Override
	public void setContent(final Component newContent) {
		final Component oldContent = setComponent(ScrollComponent.CONTENT_COMPONENT, newContent); //set the component
		if(oldContent != newContent) { //if the component really changed
			firePropertyChange(CONTENT_PROPERTY, oldContent, newContent); //indicate that the value changed
		}
	}

	/** The status of the current user input, or <code>null</code> if there is no status to report. */
	private Status status = null;

	@Override
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the status of the current user input. This is a bound property.
	 * @param newStatus The new status of the current user input, or <code>null</code> if there is no status to report.
	 * @see #STATUS_PROPERTY
	 */
	protected void setStatus(final Status newStatus) {
		if(status != newStatus) { //if the value is really changing
			final Status oldStatus = status; //get the current value
			status = newStatus; //update the value
			firePropertyChange(STATUS_PROPERTY, oldStatus, newStatus);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version clears any notification.
	 * </p>
	 * @see #setNotification(Notification)
	 */
	@Override
	public void reset() {
		//TODO check; are we missing notification methods?		setNotification(null);	//clear any notification
	}

	/** Default constructor with no content component. */
	public ScrollControl() {
		this(null); //construct the class with no content child component
	}

	/**
	 * Component constructor.
	 * @param component The single child component, or <code>null</code> if this control should have no child component.
	 */
	public ScrollControl(final Component component) {
		super(ScrollComponent.values()); //construct the parent class
		setComponent(ScrollComponent.CONTENT_COMPONENT, component); //set the component directly, because child classes may prevent the setContent() method from changing the component 
		this.enableable = checkInstance(new DefaultEnableable(), "Enableable object cannot be null."); //save the enableable object TODO later allow this to be passed as an argument
		this.enableable.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the enableable object
		addPropertyChangeListener(ENABLED_PROPERTY, new AbstractGenericPropertyChangeListener<Boolean>() { //listen for the "enabled" property changing

			@Override
			public void propertyChange(GenericPropertyChangeEvent<Boolean> genericPropertyChangeEvent) { //if the "enabled" property changes
				setNotification(null); //clear any notification
				updateValid(); //update the valid status, which depends on the enabled status					
			}

		});
	}

	//Enableable delegations

	@Override
	public boolean isEnabled() {
		return enableable.isEnabled();
	}

	@Override
	public void setEnabled(final boolean newEnabled) {
		enableable.setEnabled(newEnabled);
	}

}
