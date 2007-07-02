package com.guiseframework.platform.web;

import java.io.*;

import com.guiseframework.component.*;

import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;
import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**Strategy for rendering an action model control as an XHTML <code>&lt;button&gt;</code> element.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebButtonDepictor<C extends ActionControl> extends AbstractWebActionControlDepictor<C>
{

	/**Default constructor using the XHTML <code>&lt;button&gt;</code> element.*/
	public WebButtonDepictor()
	{
//TODO del; test for input button		super(XHTML_NAMESPACE_URI, ELEMENT_INPUT, true);	//represent <xhtml:input>
		super(XHTML_NAMESPACE_URI, ELEMENT_BUTTON);	//represent <xhtml:button>
	}

	/**Writes any action parameters as comments.
	This methods writes a comment containing the confirmation message, if any.
	@param context Guise context information.
	@param component The component being rendered.
	@exception IOException if there is an error rendering the component.
	*/
/*TODO del; transfer to another technique if needed
	protected void writeParameters(final GC context, final C component) throws IOException	//TODO replace with better parameters; probably remove confirmation altogether
	{
		final MessageModel confirmationMessage=component.getModel().getConfirmationMessage();	//get the action confirmation message, if there is one
		if(confirmationMessage!=null)	//if there is a confirmation message
		{
			final String message=confirmationMessage.getMessage();	//get the actual message
			if(message!=null)	//if a message is given
			{
				context.writeComment("confirm:"+AbstractModel.getPlainText(message, confirmationMessage.getMessageContentType()));	//confirm:confirmMessage TODO use constants; perhaps the confirm property, and a common routine for adding parameters
			}
		}
	}
*/

	/**Begins the rendering process.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBegin() throws IOException
	{
		super.depictBegin();	//do the default beginning rendering
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final C component=getDepictedObject();	//get the component
		depictContext.writeAttribute(null, ELEMENT_BUTTON_ATTRIBUTE_TYPE, BUTTON_TYPE_BUTTON);	//type="button"
		depictContext.writeAttribute(null, ATTRIBUTE_NAME, getPlatform().getDepictIDString(component.getDepictID()));	//write the component ID in the XHTML name attribute
		if(!component.isEnabled())	//if the component's model is not enabled
		{
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_DISABLED, INPUT_DISABLED_DISABLED);	//disabled="disabled"			
		}
	//TODO del	writeParameters(context, component);	//write any parameters for the JavaScript

		final boolean isIconDisplayed;
		final boolean isLabelDisplayed;
		if(component instanceof LabelDisplayableComponent)	//if this component specifies whether its label should be displayed
		{
			final LabelDisplayableComponent labelDisplayableComponent=(LabelDisplayableComponent)component;	//get the label displayable component
			isIconDisplayed=labelDisplayableComponent.isIconDisplayed();	//find out whether label and/or icon should be displayed
			isLabelDisplayed=labelDisplayableComponent.isLabelDisplayed();
		}
		else	//if this component doesn't specify whether label information should be displayed
		{
			isIconDisplayed=true;	//default to showing the information
			isLabelDisplayed=true;
		}
		if(hasLabelContent(isIconDisplayed, isLabelDisplayed))	//if there is label content
		{
/*TODO del; test for input button
			context.writeAttribute(null, ATTRIBUTE_VALUE, model.getLabel());	//TODO testing
*/
			depictContext.writeElementBegin(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//<xhtml:span>
			writeClassAttribute(getBaseStyleIDs(null, COMPONENT_LABEL_CLASS_SUFFIX));	//write the base style IDs with a "-label" suffix
			writeLabelContent(isIconDisplayed, isLabelDisplayed);	//write the content of the label
			depictContext.writeElementEnd(XHTML_NAMESPACE_URI, ELEMENT_SPAN);	//</xhtml:span>
		}	
	}
}
