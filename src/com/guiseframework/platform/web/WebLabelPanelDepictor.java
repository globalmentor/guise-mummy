package com.guiseframework.platform.web;

import java.io.IOException;

import static com.garretwilson.text.xml.xhtml.XHTML.*;

import com.guiseframework.component.LayoutComponent;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**Strategy for rendering a labeled panel as a series of XHTML elements.
This controller can be substituted for {@link WebFieldsetDepictor} for a different rendering of group panels.
@param <C> The type of component being depicted.
@author Garret Wilson
@see WebFieldsetDepictor
*/
public class WebLabelPanelDepictor<C extends LayoutComponent> extends AbstractWebLayoutComponentDepictor<C>
{

	/**Default constructor using the XHTML <code>&lt;div&gt;</code> element.*/
	public WebLabelPanelDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//represent <xhtml:div>
	}

	/**Begins the rendering process.
	This version wraps the component in a decorator element.
	@exception IOException if there is an error rendering the component.
	@exception IllegalArgumentException if the given value control represents a value type this controller doesn't support.
	*/
	protected void depictBegin() throws IOException
	{
		super.depictBegin();	//do the default beginning rendering
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		writeIDClassAttributes(null, null);	//write the ID and class attributes with no prefixes or suffixes
		writeDirectionAttribute();	//write the component direction, if this component specifies a direction
		writeLabel(decorateID(getPlatform().getDepictIDString(getDepictedObject().getDepictID()), null, COMPONENT_BODY_CLASS_SUFFIX));	//write the label for the body, if there is a label				
		depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div> (component-body)
		writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX);	//write the ID and class for the body element
	}

	/**Ends the rendering process.
	This version closes the decorator elements.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictEnd() throws IOException
	{
		getDepictContext().writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//</xhtml:div> (component-body)
		writeErrorMessage();	//write the error message, if any
		super.depictEnd();	//do the default ending rendering
	}

}
