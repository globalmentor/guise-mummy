package com.guiseframework.platform.web;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.*;

import javax.mail.internet.ContentType;

import com.guiseframework.component.*;
import com.guiseframework.model.*;
import com.guiseframework.platform.DepictEvent;
import com.guiseframework.platform.PlatformEvent;
import com.guiseframework.validator.*;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**Strategy for rendering a resource import control as an XHTML <code>&lt;input&gt;</code> element with type="file".
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebFileInputDepictor<C extends ValueControl<ResourceImport>> extends AbstractDecoratedWebComponentDepictor<C>
{

	/**Default constructor using the XHTML <code>&lt;input&gt;</code> element.*/
	public WebFileInputDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_INPUT, true);	//represent <xhtml:input>, allowing an empty element if possible
	}

	/**Processes an event from the platform.
	@param event The event to be processed.
	@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	*/
	public void processEvent(final PlatformEvent event)
	{
		if(event instanceof WebFormEvent)	//if this is a form submission
		{
			final WebFormEvent formEvent=(WebFormEvent)event;	//get the form submit event
			final C component=getDepictedObject();	//get the component
			final String componentName=getDepictName();	//get the component's name
			if(componentName!=null)	//if this component has a name
			{
				final ResourceImport resourceImport=asInstance(formEvent.getParameterListMap().getItem(componentName), ResourceImport.class);	//get the value reported for this component
				if(resourceImport!=null)	//if a resource import was given
				{
					try
					{
						component.setNotification(null);	//clear the component errors; this method may generate new errors
						component.setValue(resourceImport);	//store the value in the model
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
		depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_TYPE, INPUT_TYPE_FILE);	//type="file"
		if(!component.isEnabled())	//if the component's model is not enabled
		{
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_DISABLED, INPUT_DISABLED_DISABLED);	//disabled="disabled"			
		}
		if(!component.isEditable())	//if the component's model is not editable
		{
			depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_READONLY, INPUT_READONLY_READONLY);	//readonly="readonly"			
		}
		final Validator<ResourceImport> validator=component.getValidator();	//get the component's validator
		if(validator instanceof ResourceImportValidator)	//if the validator is a resource import validator
		{
			final Set<ContentType> acceptedContentTypes=((ResourceImportValidator)validator).getAcceptedContentTypes();	//get the accepted content types
			if(acceptedContentTypes!=null)	//if accepted content types are specified
			{
				final StringBuilder acceptStringBuilder=new StringBuilder();	//create a string builder for constructing the accept string
				for(final ContentType contentType:acceptedContentTypes)	//for each accepted content type
				{
					if(acceptStringBuilder.length()>0)	//if this is not the first accepted content type
					{
						acceptStringBuilder.append(COMMA_CHAR);	//separate the accepted content types
					}
					acceptStringBuilder.append(contentType.getBaseType());	//append the base accepted content type
				}
				depictContext.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_ACCEPT, acceptStringBuilder.toString());	//accept="acceptedContentTypes"							
			}
		}
/*TODO fix if we want to
		final ResourceImport encodedValue=getEncodedValue();	//get the encoded resource import, if there is one
		if(encodedValue!=null)	//if there is a value
		{
			final String name=encodedValue.getName();	//get the resource import name, if there is one; use the full name that was given originally, so we can match the original value round-trip
			if(name!=null)	//if we have a resource import name
			{
				context.writeAttribute(null, ATTRIBUTE_VALUE, name);	//value="name"
			}
		}
*/
	}

}
