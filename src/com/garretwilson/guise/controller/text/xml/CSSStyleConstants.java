package com.garretwilson.guise.controller.text.xml;

import static com.garretwilson.io.FileConstants.*;
import static com.garretwilson.io.FileUtilities.*;

import java.net.URI;

/**Constants to be used with the Guise stylesheet.
@author Garret Wilson
*/
public class CSSStyleConstants
{

	/**The URI of the Guise XHTML CSS stylesheet.*/
	public final static URI GUISE_XHTML_CSS_STYLESHEET_URI=URI.create(addExtension("guise", CSS_EXTENSION));

	/**The CSS class for horizontal flow layout children.*/
	public final static String LAYOUT_FLOW_X_CHILD_CLASS="layout-flow-x-child";

	/**The CSS class for vertical flow layout children.*/
	public final static String LAYOUT_FLOW_Y_CHILD_CLASS="layout-flow-y-child";
}
