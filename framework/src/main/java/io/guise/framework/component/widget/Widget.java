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

package io.guise.framework.component.widget;

import java.net.URI;
import java.util.Set;

import com.globalmentor.net.ContentType;

import io.guise.framework.component.Component;

/**
 * A component that can be embedded in certain content and can be edited within a widget editing environment. Widgets must provide a default constructor. A
 * widget may also provide a two-URI constructor that allows specification of the base URI where the widget is embedded, as well as the URI of the definition of
 * the widget, such as a template, either of which may be <code>null</code>.
 * @author Garret Wilson
 */
public interface Widget extends Component {

	/** The media type of a Guise widget. */
	public static final ContentType WIDGET_MEDIA_TYPE = ContentType.of(ContentType.APPLICATION_PRIMARY_TYPE,
			ContentType.SUBTYPE_EXTENSION_PREFIX + "guise-widget");

	/**
	 * Returns the base URI where the widget is embedded.
	 * @return The base URI where the widget is embedded, or <code>null</code> if the base URI is not known.
	 */
	public URI getBaseURI();

	/**
	 * Returns the base URI of the definition of the widget. This may be the URI of a template resource, allowing the true base URI of the widget to vary based
	 * upon the instance resource with which the resource is used.
	 * @return The base URI of the definition of the widget, or <code>null</code> if the definition base URI is not known.
	 */
	public URI getDefinitionBaseURI();

	/**
	 * Retrieves that names of parameters that are allowed to be accessed in a widget context.
	 * @return The set of names of widget parameters.
	 */
	public Set<String> getParameterNames();

}
