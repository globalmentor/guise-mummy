package net.marmox.guise.component;

import javax.mail.internet.ContentType;

import static com.garretwilson.io.ContentTypes.*;

import com.guiseframework.component.Component;

/**A custom component that can be embedded in user content.
Widgets must provide a default constructor.
@author Garret Wilson
*/
public interface Widget extends Component
{

	/**The MIME type of a Marmox widget.*/
	public final static ContentType MARMOX_WIDGET_CONTENT_TYPE=new ContentType(APPLICATION_PRIMARY_TYPE, SUBTYPE_EXTENSION_PREFIX+"marmox-widget", null);

}
