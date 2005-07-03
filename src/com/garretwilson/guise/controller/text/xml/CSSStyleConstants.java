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

	
	//style IDs
	
	/**The CSS class for panels meant to visually group components.*/
	public final static String GROUP_PANEL_CLASS="groupPanel";
	
	//components
	
	/**The CSS class postfix for the decorator wrapper of a component.*/
	public final static String COMPONENT_DECORATOR_CLASS_POSTFIX="-decorator";
	/**The CSS class postfix for the label part of a component.*/
	public final static String COMPONENT_LABEL_CLASS_POSTFIX="-label";
	/**The CSS class postfix for the message part of a component.*/
	public final static String COMPONENT_MESSAGE_CLASS_POSTFIX="-message";
	/**The CSS class postfix for the component part of a component.*/
	public final static String COMPONENT_BODY_CLASS_POSTFIX="-body";
	/**The CSS class postfix for the error part of a component.*/
	public final static String COMPONENT_ERROR_CLASS_POSTFIX="-error";
	
	//layout
	
	/**The CSS class for any enclosing element needed for horizontal flow layout.*/
	public final static String LAYOUT_FLOW_X_CLASS="layout-flow-x";
	/**The CSS class for horizontal flow layout children.*/
	public final static String LAYOUT_FLOW_X_CHILD_CLASS="layout-flow-x-child";

	/**The CSS class for any enclosing element needed for vertical flow layout.*/
	public final static String LAYOUT_FLOW_Y_CLASS="layout-flow-y";
	/**The CSS class for vertical flow layout children.*/
	public final static String LAYOUT_FLOW_Y_CHILD_CLASS="layout-flow-y-child";
}
