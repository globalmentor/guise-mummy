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

import static com.globalmentor.java.Classes.*;

import io.guise.framework.component.layout.MenuLayout;
import io.guise.framework.prototype.MenuPrototype;

/**
 * A group of components arranged as a menu. This component uses a {@link MenuPrototype} and a {@link MenuLayout}.
 * @author Garret Wilson
 * @see MenuLayout
 * @see MenuPrototype
 */
public interface Menu extends ContainerControl, ActionControl, LabelDisplayableComponent {

	/** The open bound property. */
	public static final String OPEN_PROPERTY = getPropertyName(Menu.class, "open");
	/** The bound property of whether children will be displayed upon rollover. */
	public static final String ROLLOVER_OPEN_ENABLED_PROPERTY = getPropertyName(Menu.class, "rolloverOpenEnabled");

	/** @return The layout definition for the menu. */
	public MenuLayout getLayout();

	/** @return Whether the menu is open. */
	public boolean isOpen();

	/**
	 * Sets whether the menu is open. This is a bound property of type <code>Boolean</code>.
	 * @param newOpen <code>true</code> if the menu should be open.
	 * @see #OPEN_PROPERTY
	 */
	public void setOpen(final boolean newOpen);

	/** @return Whether the menu children will be shown during rollover. */
	public boolean isRolloverOpenEnabled();

	/**
	 * Sets whether the menu children will be shown during rollover. If rollover open is enabled, the open state will not actually be changed during rollover.
	 * This is a bound property of type <code>Boolean</code>.
	 * @param newRolloverOpenEnabled <code>true</code> if the component should allow display during rollover, else <code>false</code>.
	 * @see #ROLLOVER_OPEN_ENABLED_PROPERTY
	 */
	public void setRolloverOpenEnabled(final boolean newRolloverOpenEnabled);
}
