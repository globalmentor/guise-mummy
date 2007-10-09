package com.guiseframework.platform.web;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

import com.garretwilson.util.Debug;
import com.guiseframework.component.*;
import com.guiseframework.converter.*;
import com.guiseframework.model.Notification;
import com.guiseframework.platform.*;

import static com.guiseframework.platform.web.WebPlatform.*;

/**Strategy for rendering a text control as an XHTML <code>&lt;input&gt;</code> element or an XHTML <code>&lt;textarea&gt;</code> element.
This view will change the XHTML element rendered based upon the number of rows requested by the text control.
@param <V> The type of value represented in the control.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebTextControlDepictor<V, C extends TextControl<V>> extends AbstractDecoratedWebComponentDepictor<C>
{

	/**Default constructor using the XHTML <code>&lt;input&gt;</code> element.*/
	public WebTextControlDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_INPUT, true);	//represent <xhtml:input>, allowing an empty element if possible; this may be modified at render time based upon whether an <input> or or <textarea> element is being used
	}

	/**Determines the local name of the body XML element.
	This version returns the local name of the XHTML <code>&lt;input&gt;</code> element if exactly one row is specified, otherwise the local name of the XHTML <code>&lt;textarea&gt;</code>.
	@return The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	*/
	public String getBodyLocalName()
	{
		return getDepictedObject().getRowCount()==1 ? ELEMENT_INPUT : ELEMENT_TEXTAREA;	//if something besides one row is requested (such as no rows), use a text area
	}

	/**Determines whether an empty body element can be created if there is no content.
	This version returns <code>true</code> for <code>&lt;input&gt;</code> and <code>false</code> for <code>&lt;textarea&gt;</code>.
	@return Whether an empty body element can be created if there is no content.
	@see #getBodyLocalName()
	*/
	public boolean isEmptyBodyElementAllowed()
	{
		return ELEMENT_INPUT.equals(getBodyLocalName());	//only allow an empty element for <xhtml:input> (IE6 and IE7 breaks on an empty <textarea> element)
	}

	/**Determines the XHTML input type to use for getting input from the user.
	@return One of the XHTML input types.
	*/
	protected String getInputType()
	{
		return getDepictedObject().isMasked() ? INPUT_TYPE_PASSWORD : INPUT_TYPE_TEXT;	//if this is a masked component, use a password input 
	}

	/**Processes an event from the platform.
	@param event The event to be processed.
	@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	*/
	public void processEvent(final PlatformEvent event)
	{
		if(event instanceof WebChangeEvent)	//if a property changed
		{
			final WebChangeEvent webChangeEvent=(WebChangeEvent)event;	//get the web change event
			final C component=getDepictedObject();	//get the depicted object
			if(webChangeEvent.getDepictedObject()!=component)	//if the event was meant for another depicted object
			{
				throw new IllegalArgumentException("Depict event "+event+" meant for depicted object "+webChangeEvent.getDepictedObject());
			}
			final Map<String, Object> properties=webChangeEvent.getProperties();	//get the new properties
			String valueText=null;	//we'll get a new value to use if needed
			final String provisionalText=asInstance(properties.get("provisionalValue"), String.class);	//get the provisional value, if any; a provisional value will never be null TODO use a constant
			if(provisionalText!=null)	//if there is a provisional value
			{
				final Pattern autoCommitPattern=component.getAutoCommitPattern();	//get the auto-commit pattern, if any
				if(autoCommitPattern!=null && autoCommitPattern.matcher(provisionalText).matches())	//if there is an auto-commit pattern and the text patches that pattern
				{
					valueText=provisionalText;	//don't make this provisional; go ahead and commit the change
				}
				else	//if we have a new provisional value
				{
					component.setNotification(null);	//clear the component errors; this method may generate new errors if the change is not provisional TODO do we want this here?
					component.setProvisionalText(provisionalText);	//update the provisional literal text of the component
				}
			}
			final boolean valueSpecified=properties.containsKey("value");	//see if a value was specified TODO use a constant
			if(valueSpecified || valueText!=null)	//if a value was specified, or we have a provisional value to commit
			{
				if(valueSpecified)	//if a value was specified, it will always override any specified provisional value
				{
					valueText=asInstance(properties.get("value"), String.class);	//get the new value; this will incorrectly use a new value of null if the given value isn't a string TODO use a constant					
				}
				component.setNotification(null);	//clear the component errors; this method may generate new errors if the change is not provisional
				try
				{
					component.setTextValue(valueText);	//update the literal text of the component, which will in turn update the provisional text of the component, and then update the value
				}
				catch(final ConversionException conversionException)	//if there is a conversion error
				{
					component.setNotification(new Notification(conversionException));	//add this error to the component
				}
				catch(final PropertyVetoException propertyVetoException)	//if there is a veto
				{
					final Throwable cause=propertyVetoException.getCause();	//get the cause of the veto, if any
					component.setNotification(new Notification(cause!=null ? cause : propertyVetoException));	//add notification of the error to the component
				}
			}
		}
		else if(event instanceof WebFormEvent)	//if this is a form submission
		{
			final WebFormEvent formEvent=(WebFormEvent)event;	//get the form submit event
			final C component=getDepictedObject();	//get the component
			final String componentName=getDepictName();	//get the component's name
			if(componentName!=null)	//if there is a component name
			{
				final String text=asInstance(formEvent.getParameterListMap().getItem(componentName), String.class);	//get the form value for this control
				if(text!=null)	//if there was a parameter value for this component
				{
					component.setNotification(null);	//clear the component errors; this method may generate new errors if the change is not provisional
					try
					{
						component.setTextValue(text);	//update the literal text of the component, which will in turn update the provisional text of the component, and then update the value
					}
					catch(final ConversionException conversionException)	//if there is a conversion error
					{
						component.setNotification(new Notification(conversionException));	//add this error to the component
					}
					catch(final PropertyVetoException propertyVetoException)	//if there is a veto
					{
						final Throwable cause=propertyVetoException.getCause();	//get the cause of the veto, if any
						component.setNotification(new Notification(cause!=null ? cause : propertyVetoException));	//add notification of the error to the component
					}
				}
			}
		}
		super.processEvent(event);	//do the default event processing
	}

	/**Begins the rendering process.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBegin() throws IOException
	{
		super.depictBegin();	//do the default beginning rendering
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final C component=getDepictedObject();	//get the component
		depictContext.writeAttribute(null, ATTRIBUTE_NAME, getPlatform().getDepictIDString(component.getDepictID()));	//write the component ID in the XHTML name attribute
		if(!component.isEnabled())	//if the component's is not enabled
		{
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_DISABLED, INPUT_DISABLED_DISABLED);	//disabled="disabled"			
		}
		if(!component.isEditable())	//if the component's is not editable
		{
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_READONLY, INPUT_READONLY_READONLY);	//readonly="readonly"			
		}
		final int columnCount=component.getColumnCount();	//get the column count
		final String bodyLocalName=getBodyLocalName();	//see which type of element we are rendering
		if(ELEMENT_INPUT.equals(bodyLocalName))	//if we are rendering an <xhtml:input>
		{
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_TYPE, getInputType());	//type="inputType"
			final int maxLength=component.getMaximumLength();	//get the maximum length
			if(maxLength>=0)	//if a maximum length is given
			{
				depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_MAXLENGTH, Integer.toString(maxLength));	//maxlength="maxLength"							
			}
			if(columnCount>=0)	//if a valid column count is given
			{
				depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_SIZE, Integer.toString(columnCount));	//size="columnCount"							
			}
			final String text=component.getProvisionalText();	//see what string we should use for the XHTML value attribute (the provisional text represents the most recent text we know about)
			if(text!=null)	//if there is a value
			{
				depictContext.writeAttribute(null, ATTRIBUTE_VALUE, text);	//value="encodedValue"			
			}
				//check to see if this is just a provisional AJAX update, in which case we should tell the browser not to update the the value
				//this is a provisional AJAX update if the provisionalText property is modified but the text property is not, and the general update property is not set
			final Set<String> modifiedProperties=getModifiedProperties();	//get the modified properties
			if(!modifiedProperties.contains(GENERAL_PROPERTY)	//if a general modification did not occur
					&& modifiedProperties.contains(TextControl.PROVISIONAL_TEXT_PROPERTY)	//and if the text was provisionally modified
					&& !modifiedProperties.contains(TextControl.TEXT_PROPERTY))	//but the actual text was not modified, this was just a provisional value update; notify the browser not to patch in the new value
			{
				depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, "patchType", "novalue");	//guise:patchType="novalue" TODO use constants
			}
		}
		else if(ELEMENT_TEXTAREA.equals(bodyLocalName))	//if we are rendering an <xhtml:textarea>
		{
			final int rowCount=component.getRowCount();	//get the row count
			if(rowCount>=0)	//if a valid row count is given
			{
				depictContext.writeAttribute(null, ELEMENT_TEXTAREA_ATTRIBUTE_ROWS, Integer.toString(rowCount));	//rows="rowCount"							
			}
			if(columnCount>=0)	//if a valid column count is given
			{
				depictContext.writeAttribute(null, ELEMENT_TEXTAREA_ATTRIBUTE_COLS, Integer.toString(columnCount));	//cols="columnCount"							
			}
			final boolean lineWrap=component.isLineWrap();	//see if we should wrap lines
			depictContext.writeAttribute(null, ELEMENT_TEXTAREA_ATTRIBUTE_WRAP, lineWrap ? TEXTAREA_WRAP_SOFT : TEXTAREA_WRAP_OFF);	//wrap="soft|off"
		}
		else	//if we don't recognize the type of element we are rendering
		{
			throw new AssertionError("Unrecognized element local name: "+bodyLocalName);
		}
		
//TODO del Debug.trace("*****getting ready to update text input view; modified properties are:", getModifiedProperties());

	}

	/**Renders the body of the component.
	@exception IOException if there is an error rendering the component.
	*/
	protected void depictBody() throws IOException
	{
		super.depictBody();	//render the default main part of the component
		if(ELEMENT_TEXTAREA.equals(getBodyLocalName()))	//if we are rendering an <xhtml:textarea>
		{
			final String text=getDepictedObject().getText();	//see what literal text representation we are using
			if(text!=null)	//if there is a value
			{
				getDepictContext().write(text);	//write the value			
			}
		}
	}
}
