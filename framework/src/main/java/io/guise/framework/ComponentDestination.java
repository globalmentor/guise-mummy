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

package io.guise.framework;

import java.net.URI;
import java.util.regex.Pattern;

import static java.util.Objects.*;

import static com.globalmentor.java.Classes.*;

import com.globalmentor.java.Objects;
import com.globalmentor.net.URIPath;

import io.guise.framework.component.Component;

/**
 * Default implementation of a navigation point based upon a component.
 * @author Garret Wilson
 */
public class ComponentDestination extends AbstractDestination {

	/** The style bound property. */
	public static final String STYLE_PROPERTY = getPropertyName(ComponentDestination.class, "style");

	/** The class of the component to represent this destination. */
	private final Class<? extends Component> componentClass;

	/** @return The class of the component to represent this destination. */
	public Class<? extends Component> getComponentClass() {
		return componentClass;
	}

	/** The style of this destination, or <code>null</code> if no destination-specific style is specified. */
	private URI style = null;

	/** @return The style of this destination, or <code>null</code> if no destination-specific style is specified. */
	public URI getStyle() {
		return style;
	}

	/**
	 * Sets the style of this destination. This is a bound property of type <code>URI</code>.
	 * @param newStyle The style of this destination, or <code>null</code> if no destination-specific style is specified.
	 * @see #STYLE_PROPERTY
	 */
	public void setStyle(final URI newStyle) {
		if(!Objects.equals(style, newStyle)) { //if the value is really changing
			final URI oldStyle = style; //get the old value
			style = newStyle; //actually change the value
			firePropertyChange(STYLE_PROPERTY, oldStyle, newStyle); //indicate that the value changed
		}
	}

	/**
	 * Path and component constructor with no style specified.
	 * @param path The application context-relative path within the Guise container context, which does not begin with '/'.
	 * @param componentClass The class of the component to represent this destination.
	 * @throws NullPointerException if the path and/or the component class is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path is absolute.
	 */
	public ComponentDestination(final URIPath path, final Class<? extends Component> componentClass) {
		this(path, componentClass, null); //construct the class with no style specified
	}

	/**
	 * Path, component, and style constructor.
	 * @param path The application context-relative path within the Guise container context, which does not begin with '/'.
	 * @param componentClass The class of the component to represent this destination.
	 * @param style The style of this destination, or <code>null</code> if no destination-specific style is specified.
	 * @throws NullPointerException if the path and/or the component class is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path is absolute.
	 */
	public ComponentDestination(final URIPath path, final Class<? extends Component> componentClass, final URI style) {
		super(path); //construct the parent class
		this.componentClass = requireNonNull(componentClass, "Component class cannot be null."); //store the associated class
		this.style = style;
	}

	/**
	 * Path pattern and component constructor with no style specified.
	 * @param pathPattern The pattern to match an application context-relative path within the Guise container context, which does not begin with '/'.
	 * @param componentClass The class of the component to represent this destination.
	 * @throws NullPointerException if the path pattern and/or the component class is <code>null</code>.
	 */
	public ComponentDestination(final Pattern pathPattern, final Class<? extends Component> componentClass) {
		this(pathPattern, componentClass, null); //construct the class with no style specified
	}

	/**
	 * Path pattern, component, and style constructor.
	 * @param pathPattern The pattern to match an application context-relative path within the Guise container context, which does not begin with '/'.
	 * @param componentClass The class of the component to represent this destination.
	 * @param style The style of this destination, or <code>null</code> if no destination-specific style is specified.
	 * @throws NullPointerException if the path pattern and/or the component class is <code>null</code>.
	 * @throws IllegalArgumentException if the provided path is absolute.
	 */
	public ComponentDestination(final Pattern pathPattern, final Class<? extends Component> componentClass, final URI style) {
		super(pathPattern); //construct the parent class
		this.componentClass = requireNonNull(componentClass, "Component class cannot be null."); //store the associated class
		this.style = style;
	}
}
