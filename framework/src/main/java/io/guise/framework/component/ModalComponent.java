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

import static com.globalmentor.java.Classes.*;

/**
 * A component that supports different modes of interaction, such as an editable label or a modal frame.
 * @param <M> The type of mode this modal component supports.
 * @author Garret Wilson
 */
public interface ModalComponent<M extends Mode> extends Component {

	/** The bound property of the mode. */
	public static final String MODE_PROPERTY = getPropertyName(ModalComponent.class, "mode");

	/** @return The current mode of interaction, or <code>null</code> if the component is in a modeless state. */
	public M getMode();

	/**
	 * Sets the mode of interaction. This is a bound property.
	 * @param newMode The new mode of component interaction.
	 * @see #MODE_PROPERTY
	 */
	public void setMode(final M newMode);

}
