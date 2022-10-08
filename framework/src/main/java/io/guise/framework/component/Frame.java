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

package io.guise.framework.component;

import java.beans.PropertyChangeListener;

import static com.globalmentor.java.Classes.*;

import com.globalmentor.beans.GenericPropertyChangeListener;

import io.guise.framework.component.effect.Effect;
import io.guise.framework.model.ui.PresentationModel;
import io.guise.framework.prototype.*;
import io.guise.framework.style.Color;

/**
 * A root-level component such as a window or an HTML page. The frame's contents are specified using {@link #setContent(Component)}.
 * <p>
 * The title is specified by the frame model's label.
 * </p>
 * <p>
 * A frame like other components is by default visible, but is not actually shown until its {@link #open(boolean)} method is called.
 * </p>
 * <p>
 * A frame is a {@link InputFocusGroupComponent}, allowing descendant {@link InputFocusableComponent}s to have the focus.
 * </p>
 * @author Garret Wilson
 */
public interface Frame extends ContentComponent, ModalComponent<Frame.Mode>, InputFocusGroupComponent {

	/** The close action control bound property. */
	public static final String CLOSE_ACTION_CONTROL_PROPERTY = getPropertyName(Frame.class, "closeActionControl");
	/** The bound property of the frame menu. */
	public static final String MENU_PROPERTY = getPropertyName(Frame.class, "menu");
	/** The bound property of whether the frame is modal if and when it is open. */
	public static final String MODAL_PROPERTY = getPropertyName(Frame.class, "modal");
	/** The bound property of whether the component is movable. */
	public static final String MOVABLE_PROPERTY = getPropertyName(Frame.class, "movable");
	/** The open effect bound property. */
	public static final String OPEN_EFFECT_PROPERTY = getPropertyName(Frame.class, "openEffect");
	/** The bound property of the related component. */
	public static final String RELATED_COMPONENT_PROPERTY = getPropertyName(Frame.class, "relatedComponent");
	/** The bound property of whether the frame can be resized. */
	public static final String RESIZABLE_PROPERTY = getPropertyName(Frame.class, "resizable");
	/** The bound state property. */
	public static final String STATE_PROPERTY = getPropertyName(Frame.class, "state");
	/** The bound property of the title background color. */
	public static final String TITLE_BACKGROUND_COLOR_PROPERTY = getPropertyName(Frame.class, "titleBackgroundColor");
	/** The bound property of whether the title bar is visible. */
	public static final String TITLE_VISIBLE_PROPERTY = getPropertyName(Frame.class, "titleVisible");
	/** The bound property of the frame toolbar. */
	public static final String TOOLBAR_PROPERTY = getPropertyName(Frame.class, "toolbar");

	/** The mode of this component; whether the frame is in exclusive interaction with the user. */
	public enum Mode implements io.guise.framework.component.Mode {
		/** Exclusive mode. */
		EXCLUSIVE;
	}

	/** The state of the frame. */
	public enum State {
		/** The frame is closed. */
		CLOSED,
		/** The frame is open. */
		OPEN;
	}

	/** @return The state of the frame. */
	public State getState();

	/** @return The frame menu, or <code>null</code> if this frame does not have a menu. */
	public Menu getMenu();

	/**
	 * Sets the frame menu. This is a bound property.
	 * @param newMenu The frame menu, or <code>null</code> if this frame does not have a menu.
	 * @see #MENU_PROPERTY
	 */
	public void setMenu(final Menu newMenu);

	/** @return Whether the frame is modal if and when it is open. */
	public boolean isModal();

	/**
	 * Sets whether the frame is modal if and when it is open. This is a bound property of type <code>Boolean</code>.
	 * @param newModal <code>true</code> if the frame should be modal, else <code>false</code>.
	 * @see #MODAL_PROPERTY
	 */
	public void setModal(final boolean newModal);

	/** @return Whether the frame is movable. */
	public boolean isMovable();

	/**
	 * Sets whether the frame is movable. This is a bound property of type <code>Boolean</code>.
	 * @param newMovable <code>true</code> if the frame should be movable, else <code>false</code>.
	 * @see #MOVABLE_PROPERTY
	 */
	public void setMovable(final boolean newMovable);

	/** @return Whether the frame can be resized. */
	public boolean isResizable();

	/**
	 * Sets whether the frame can be resized. This is a bound property of type <code>Boolean</code>.
	 * @param newResizable <code>true</code> if the frame can be resized, else <code>false</code>.
	 * @see #RESIZABLE_PROPERTY
	 */
	public void setResizable(final boolean newResizable);

	/** @return The related component such as a popup source, or <code>null</code> if the frame is not related to another component. */
	public Component getRelatedComponent();

	/**
	 * Sets the related component This is a bound property.
	 * @param newRelatedComponent The new related component, or <code>null</code> if the frame is not related to another component.
	 * @see Frame#RELATED_COMPONENT_PROPERTY
	 */
	public void setRelatedComponent(final Component newRelatedComponent);

	/** @return The background color of the title, or <code>null</code> if no background color is specified for the title. */
	public Color getTitleBackgroundColor();

	/**
	 * Sets the background color of the title. This is a bound property.
	 * @param newTitleBackgroundColor The background color of the title, or <code>null</code> if the default background color should be used.
	 * @see #TITLE_BACKGROUND_COLOR_PROPERTY
	 */
	public void setTitleBackgroundColor(final Color newTitleBackgroundColor);

	/** @return Whether the title bar is visible. */
	public boolean isTitleVisible();

	/**
	 * Sets whether the title bar is visible. This is a bound property of type <code>Boolean</code>.
	 * @param newTitleVisible <code>true</code> if the title bar should be visible, else <code>false</code>.
	 * @see #TITLE_VISIBLE_PROPERTY
	 */
	public void setTitleVisible(final boolean newTitleVisible);

	/** @return The frame toolbar, or <code>null</code> if this frame does not have a toolbar. */
	public Toolbar getToolbar();

	/**
	 * Sets the frame toolbar. This is a bound property.
	 * @param newToolbar The frame toolbar, or <code>null</code> if this frame does not have a toolbar.
	 * @see #TOOLBAR_PROPERTY
	 */
	public void setToolbar(final Toolbar newToolbar);

	/** @return The effect used for opening the frame, or <code>null</code> if there is no open effect. */
	public Effect getOpenEffect();

	/**
	 * Sets the effect used for opening the frame. This is a bound property.
	 * @param newOpenEffect The new effect used for opening the frame, or <code>null</code> if there should be no open effect.
	 * @see #OPEN_EFFECT_PROPERTY
	 */
	public void setOpenEffect(final Effect newOpenEffect);

	/** @return The action prototype for closing the frame. */
	public ActionPrototype getCloseActionPrototype();

	/** @return The action control for closing the frame, or <code>null</code> if this frame does not have a close action control. */
	public ActionControl getCloseActionControl();

	/**
	 * Sets the action control for closing the frame. This is a bound property.
	 * @param newCloseActionControl The action control for closing the frame, or <code>null</code> if this frame does not have a close action control.
	 * @see #CLOSE_ACTION_CONTROL_PROPERTY
	 */
	public void setCloseActionControl(final ActionControl newCloseActionControl);

	/**
	 * Opens the frame with the currently set modality. Opening the frame registers the frame with the application frame. If the frame is already open, no action
	 * occurs.
	 * @see #getState()
	 * @see #STATE_PROPERTY
	 */
	public void open();

	/**
	 * Opens the frame as modal and installs the given property change listener to listen for the mode changing. This is a convenience method that adds the
	 * {@link ModalComponent#MODE_PROPERTY} change listener using {@link #addPropertyChangeListener(String, PropertyChangeListener)} and then calls
	 * {@link #open(boolean)} with a value of <code>true</code>.
	 * @param modeChangeListener The mode property change listener to add.
	 * @see ModalComponent#MODE_PROPERTY
	 */
	public void open(final GenericPropertyChangeListener<Mode> modeChangeListener);

	/**
	 * Opens the frame, specifying modality. Opening the frame registers the frame with the session. If the frame is already open, no action occurs.
	 * @param modal <code>true</code> if the frame should be opened as a modal frame, else <code>false</code>.
	 * @see #getState()
	 * @see #STATE_PROPERTY
	 */
	public void open(final boolean modal);

	/**
	 * Determines whether the frame should be allowed to close. This method is called from {@link #close()}.
	 * @return <code>true</code> if the frame should be allowed to close.
	 */
	public boolean canClose();

	/**
	 * Closes the frame. Closing the frame unregisters the frame with the session. If the frame is already closed, no action occurs.
	 * @see #getState()
	 * @see #STATE_PROPERTY
	 */
	public void close();
}
