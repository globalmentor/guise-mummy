package com.guiseframework.component.widget;

import java.net.URI;
import java.util.Set;

import javax.mail.internet.ContentType;

import static com.globalmentor.io.ContentTypes.*;

import com.guiseframework.component.Component;

/**A component that can be embedded in certain content and can be edited within a widget editing environment.
Widgets must provide a default constructor.
A widget may also provide a URI constructor that allows specification of the base URI of the definition of the widget, such as a template.
@author Garret Wilson
*/
public interface Widget extends Component
{

	/**The MIME type of a Guise widget.*/
	public final static ContentType WIDGET_CONTENT_TYPE=getContentTypeInstance(APPLICATION_PRIMARY_TYPE, SUBTYPE_EXTENSION_PREFIX+"guise-widget");

	/**Returns the base URI of the definition of the widget.
	This may be the URI of a template resource, allowing the true base URI of the widget to vary based upon the instance resource with which the resource is used.
	@return The base URI of the definition of the widget, or <code>null</code> if the definition base URI is not known.
	*/
	public URI getDefinitionBaseURI();

	/**Retrieves that names of parameters that are allowed to be accessed in a widget context.
	@return The set of names of widget parameters.
	*/
	public Set<String> getParameterNames();

}
