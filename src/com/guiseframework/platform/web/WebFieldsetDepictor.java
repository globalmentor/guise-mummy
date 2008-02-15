package com.guiseframework.platform.web;

import java.io.IOException;

import com.guiseframework.component.LayoutComponent;
import static com.garretwilson.text.xml.xhtml.XHTML.*;

import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**Strategy for rendering a layout component as an XHTML <code>&lt;fieldset&gt;</code> element.
@param <C> The type of component being depicted.
@author Garret Wilson
@see WebLabelPanelDepictor
*/
public class WebFieldsetDepictor<C extends LayoutComponent> extends AbstractWebLayoutComponentDepictor<C>
{
	
	/**Default constructor using the XHTML <code>&lt;fieldset&gt;</code> element.*/
	public WebFieldsetDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_FIELDSET);	//represent <xhtml:fieldset>
	}

	/**Begins the rendering process.
	This implementation writes the fieldset <code>&lt;fieldset&gt;</code> element if the component has a label
	@exception IOException if there is an error rendering the component.
	@exception IllegalArgumentException if the given value control represents a value type this controller doesn't support.
	*/
	protected void depictBegin() throws IOException
	{
		super.depictBegin();	//do the default beginning rendering
		writeBodyIDClassAttributes(null, null);	//write the ID and class attributes
		writeStyleAttribute(getBodyStyles());	//write the component's body styles
		writeDirectionAttribute();	//write the component direction, if this component specifies a direction
		if(hasLabelContent())	//if there is label content
		{		
			final WebDepictContext depictContext=getDepictContext();	//get the depict context
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_LEGEND);	//<xhtml:legend>
			writeIDClassAttributes(null, COMPONENT_LABEL_CLASS_SUFFIX);	//write the ID and class for the label element
			writeDirectionAttribute();	//write the component direction, if this component specifies a direction
			writeLabelContent();	//write the content of the label
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_LEGEND);	//</xhtml:legend>
		}
	}
}
