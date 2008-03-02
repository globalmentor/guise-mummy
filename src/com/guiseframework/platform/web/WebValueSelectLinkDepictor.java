package com.guiseframework.platform.web;

import java.io.IOException;
import java.net.URI;

import com.guiseframework.component.*;

import static com.globalmentor.text.xml.xhtml.XHTML.*;

/**Strategy for rendering an action model control as an XHTML <code>&lt;a&gt;</code> element that can be selected, with values represented by icons.
@param <V> The type of value represented by the component.
@param <C> The type of component being controlled.
@author Garret Wilson
*/
public class WebValueSelectLinkDepictor<V, C extends SelectActionControl & ActionValueControl<V>> extends WebSelectLinkDepictor<C>
{

	/**Writes the supplementary icons.
	If no supplementary icons are present, no action occurs.
	This version writes the value icon, if available.
	@exception IOException if there is an error writing the icon.
	*/
	protected void writeSupplementaryIcons() throws IOException
	{
		super.writeSupplementaryIcons();	//write the default suppementary icons
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final C component=getDepictedObject();	//get the component
		final V value=component.getValue();	//get the selected value
		final URI valueIcon=component.getValueGlyphURI(value);	//get the value icon, if any
		if(valueIcon!=null)	//if there is a selected icon
		{
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_IMG, true);	//<xhtml:img>
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_SRC, depictContext.getDepictionURI(valueIcon).toString());	//src="icon"
			//TODO fix to use description or something else, and always write an alt, even if there is no information
			depictContext.writeAttribute(null, ELEMENT_IMG_ATTRIBUTE_ALT, "value");	//alt="value" TODO i18n
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_IMG);	//</html:img>
		}		
	}
}
