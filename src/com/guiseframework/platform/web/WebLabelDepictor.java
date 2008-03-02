package com.guiseframework.platform.web;

import java.io.IOException;

import com.guiseframework.component.LabelComponent;

import static com.globalmentor.text.xml.xhtml.XHTML.*;

/**Strategy for rendering a label as an XHTML <code>&lt;label&gt;</code> element.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebLabelDepictor<C extends LabelComponent> extends AbstractSimpleWebComponentDepictor<C>
{

	/**Default constructor using the XHTML <code>&lt;label&gt;</code> element.*/
	public WebLabelDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_LABEL, false);	//represent <xhtml:label>; don't allow an empty element to be created, which would confuse IE6 and corrupt the DOM tree
	}

	/**Renders the body of the component.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBody() throws IOException
	{
		super.depictBody();	//render the default main part of the component
		writeLabelContent();	//write the content of the label
	}

}
