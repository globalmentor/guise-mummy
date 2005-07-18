package com.javaguise.stylesheets;

import static com.garretwilson.io.FileConstants.*;
import static com.garretwilson.io.FileUtilities.*;

import java.net.URI;

/**Constants to be used with the Guise stylesheet.
@author Garret Wilson
*/
public class GuiseCSSStyleConstants
{

	/**The URI of the Guise XHTML CSS stylesheet.*/
	public final static URI GUISE_XHTML_CSS_STYLESHEET_URI=URI.create(addExtension("guise", CSS_EXTENSION));
	
	//style IDs
	
	/**The CSS class for panels meant to visually group components.*/
	public final static String GROUP_PANEL_CLASS="groupPanel";
	/**The CSS class for license information.*/
	public final static String LICENSE_CLASS="license";

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

	/**The CSS class postfix indicating the X axis.*/
	public final static String COMPONENT_X_AXIS_CLASS_POSTFIX="-x";
	/**The CSS class postfix indicating the Y axis.*/
	public final static String COMPONENT_Y_AXIS_CLASS_POSTFIX="-y";

	//layout

	/**The CSS class for any enclosing element needed for region layout.*/
	public final static String LAYOUT_REGION_CLASS="layout-region";
	/**The CSS class for the bottom region layout.*/
	public final static String LAYOUT_REGION_BOTTOM_CLASS="layout-region-bottom";
	/**The CSS class for the center region layout.*/
	public final static String LAYOUT_REGION_CENTER_CLASS="layout-region-center";
	/**The CSS class for the left region layout.*/
	public final static String LAYOUT_REGION_LEFT_CLASS="layout-region-left";
	/**The CSS class for the right region layout.*/
	public final static String LAYOUT_REGION_RIGHT_CLASS="layout-region-right";
	/**The CSS class for the top region layout.*/
	public final static String LAYOUT_REGION_TOP_CLASS="layout-region-top";

	/**The CSS class for any enclosing element needed for horizontal flow layout.*/
	public final static String LAYOUT_FLOW_X_CLASS="layout-flow-x";
	/**The CSS class for horizontal flow layout children.*/
	public final static String LAYOUT_FLOW_X_CHILD_CLASS="layout-flow-x-child";

	/**The CSS class for any enclosing element needed for vertical flow layout.*/
	public final static String LAYOUT_FLOW_Y_CLASS="layout-flow-y";
	/**The CSS class for vertical flow layout children.*/
	public final static String LAYOUT_FLOW_Y_CHILD_CLASS="layout-flow-y-child";
}
