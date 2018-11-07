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

/**
 * A composite component that holds a child content component. The component's content is specified using {@link #setContent(Component)}.
 * @author Garret Wilson
 */
public interface ContentComponent extends CompositeComponent {

	/** The content bound property. */
	public static final String CONTENT_PROPERTY = getPropertyName(ContentComponent.class, "content");

	/** @return The content child component, or <code>null</code> if this component does not have a content child component. */
	public Component getContent();

	/**
	 * Sets the content child component. This is a bound property
	 * @param newContent The content child component, or <code>null</code> if this component does not have a content child component.
	 * @see #CONTENT_PROPERTY
	 */
	public void setContent(final Component newContent);
}
