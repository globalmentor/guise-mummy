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

import java.util.List;

/**
 * A frame for communication of an option. An option frame defaults to a single composite child panel with a row of options along the bottom. The contents of an
 * option dialog frame should be accessed by {@link #getOptionContent()} and {@link #setOptionContent(Component)}.
 * @param <O> The type of options available.
 * @author Garret Wilson
 */
public interface OptionDialogFrame<O> extends DialogFrame<O> {

	/** @return The component representing option contents, or <code>null</code> if this frame does not have an option contents component. */
	public Component getOptionContent();

	/**
	 * Sets the component representing option contents.
	 * @param newOptionContent The single option contents component, or <code>null</code> if this frame does not have an option contents component.
	 */
	public void setOptionContent(final Component newOptionContent);

	/** @return The container containing the options. */
	public Container getOptionContainer();

	/** @return The read-only list of available options in order. */
	public List<O> getOptions();

	/**
	 * Returns the component that represents the specified option.
	 * @param option The option for which a component should be returned.
	 * @return The component, such as a button, that represents the given option, or <code>null</code> if there is no component that represents the given option.
	 */
	public Component getOptionComponent(final O option);

}
