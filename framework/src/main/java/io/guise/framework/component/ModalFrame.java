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
 * A frame that supports modal interaction.
 * @param <R> The type of modal result this modal frame produces.
 * @author Garret Wilson
 */
public interface ModalFrame<R> extends Frame {

	/** The modal state bound property. */
	public static final String MODAL_PROPERTY = getPropertyName(ModalFrame.class, "modal");
	/** The result bound property. */
	public static final String RESULT_PROPERTY = getPropertyName(ModalFrame.class, "result");

	/** @return The result of this frame's modal interaction, or <code>null</code> if no result is given. */
	public R getResult();

	/**
	 * Sets the modal result. This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	 * @param newResult The new result of this frame's modal interaction, or <code>null</code> if no result is given.
	 * @see #RESULT_PROPERTY
	 */
	public void setResult(final R newResult);

}
