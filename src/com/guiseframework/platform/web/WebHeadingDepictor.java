package com.guiseframework.platform.web;

import static com.garretwilson.text.xml.xhtml.XHTML.*;

import com.guiseframework.component.*;

/**Strategy for rendering a label component as an XHTML <code>h1</code>, <code>h2</code>, etc. element.
If a heading level corresponds to one of the XHTML heading element names, that name will be used for the element; otherwise, the span element will be used.
If no style ID is provided, the default style ID will be used with the heading level, if given, appended.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebHeadingDepictor<C extends LabelComponent> extends WebLabelDepictor<C>
{

	/**The array of XHTML heading element local names.*/
	protected final static String[] HEADING_LOCAL_NAMES=new String[]{ELEMENT_H1, ELEMENT_H2, ELEMENT_H3, ELEMENT_H4, ELEMENT_H5, ELEMENT_H6};

	/**Determines the local name of the component.
	This version returns one of the XHTML heading element local names if a valid level is specified, otherwise the local name of the XHTML <code>&lt;span&gt;</code> element.
	@return The appropriate XHTML heading element name if a level is specified, otherwise the XHTML span element name.
	*/
	public String getLocalName()
	{
		final C component=getDepictedObject();	//get the component		
		final int level=component instanceof Heading ? ((Heading)component).getLevel() : -1;	//get the heading level, if this is a heading
		return level>=0 && level<HEADING_LOCAL_NAMES.length ? HEADING_LOCAL_NAMES[level] : super.getLocalName();	//if this is a valid level, retrieve the local name from the array; otherwise, use the default value
	}

}
