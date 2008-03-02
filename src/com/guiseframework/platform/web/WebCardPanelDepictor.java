package com.guiseframework.platform.web;

import java.io.IOException;


import com.guiseframework.component.AbstractCardPanel;

import static com.globalmentor.text.xml.xhtml.XHTML.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**Strategy for rendering a card panel as a series of XHTML elements.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebCardPanelDepictor<C extends AbstractCardPanel> extends AbstractWebLayoutComponentDepictor<C>
{
	/**Default constructor using the XHTML <code>&lt;div&gt;</code> element.*/
	public WebCardPanelDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//represent <xhtml:div>
	}

	/**Begins the rendering process.
	This version wraps the component in a decorator element and writes tabs.
	Each tab link is given an href of "?<var>tabbedPaneID</var>=<var>tabID</var>".
	@exception IOException if there is an error rendering the component.
	@exception IllegalArgumentException if the given value control represents a value type this controller doesn't support.
	*/
	protected void depictBegin() throws IOException
	{
		super.depictBegin();	//do the default beginning rendering
		writeIDClassAttributes(null, null);	//write the ID and class attributes with no prefixes or suffixes
		writeDirectionAttribute();	//write the component direction, if this component specifies a direction
		getDepictContext().writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_DIV);	//<xhtml:div> (component-body)
		writeBodyIDClassAttributes(null, COMPONENT_BODY_CLASS_SUFFIX);	//write the ID and class attributes for the body		
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
