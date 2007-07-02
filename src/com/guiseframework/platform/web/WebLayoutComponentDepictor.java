package com.guiseframework.platform.web;

import java.io.IOException;
import java.util.Map;

import com.guiseframework.component.*;

import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**Strategy for rendering a {@link LayoutComponent} as an XHTML <code>&lt;div&gt;</code> element.
Changes to {@link LayoutComponent#NOTIFICATION_PROPERTY} are ignored.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebLayoutComponentDepictor<C extends LayoutComponent> extends AbstractWebLayoutComponentDepictor<C>
{

	/**Default constructor using the XHTML <code>&lt;div&gt;</code> element.*/
	public WebLayoutComponentDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//represent <xhtml:div>
		getIgnoredProperties().add(LayoutComponent.NOTIFICATION_PROPERTY);	//ignore Panel.notification, because we don't want to mark the component invalid when it registers a notification as this is used to pass a notification up to an enclosing class
	}

	/**Retrieves the styles for the outer element of the component.
	This version combines the body styles with the outer styles.
	@return The styles for the outer element of the component, mapped to CSS property names.
	@see AbstractWebComponentDepictor#getBodyStyles(XMLGuiseContext, Component)
	*/
	protected Map<String, Object> getOuterStyles()	//TODO decide if this technique is the best for the container views
	{
		final Map<String, Object> outerStyles=super.getOuterStyles();	//get the default outer styles
		outerStyles.putAll(getBodyStyles());	//add the styles for the body
		return outerStyles;	//return the combined styles		
	}

	/**Begins the rendering process.
	@param context Guise context information.
	@param component The controlled component.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBegin() throws IOException
	{
		super.depictBegin();	//do the default beginning rendering
		writeBodyIDClassAttributes(null, null);	//write the ID and class attributes
		writeDirectionAttribute();	//write the component direction, if this component specifies a direction
	}
}
