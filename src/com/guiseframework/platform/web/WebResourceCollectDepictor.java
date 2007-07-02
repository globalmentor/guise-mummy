package com.guiseframework.platform.web;

import java.io.IOException;
import java.util.Map;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;
import com.garretwilson.util.Debug;

import com.guiseframework.Bookmark;
import com.guiseframework.component.*;
import com.guiseframework.model.*;
import com.guiseframework.platform.*;
import static com.guiseframework.platform.web.WebPlatform.*;

/**Strategy for rendering a resource collect control as an XHTML <code>&lt;input&gt;</code> element with type="file".
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebResourceCollectDepictor<C extends ResourceCollectControl> extends AbstractDecoratedWebComponentDepictor<C>
{

	/**Default constructor using the XHTML <code>&lt;input&gt;</code> element.*/
	public WebResourceCollectDepictor()
	{
		super(XHTML_NAMESPACE_URI, ELEMENT_INPUT, true);	//represent <xhtml:input>, allowing an empty element if possible
		getIgnoredProperties().add(ResourceCollectControl.RESOURCE_PATHS_PROPERTY);	//TODO fix; temporary for development
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
			final String resourcePath=asInstance(properties.get("resourcePath"), String.class);	//get the added resource path TODO use a constant
			if(resourcePath!=null)	//if there is a resource path
			{
				if(component.isEnabled())	//if the component is enabled
				{
					component.addResourcePath(resourcePath);	//add this resource path to the control
				}
			}
		}
		else if(event instanceof WebProgressEvent)	//if this is a progress event
		{
			final WebProgressEvent webProgressEvent=(WebProgressEvent)event;	//get the progress event
			final C component=getDepictedObject();	//get the depicted object
			if(webProgressEvent.getDepictedObject()!=component)	//if the event was meant for another depicted object
			{
				throw new IllegalArgumentException("Depict event "+event+" meant for depicted object "+webProgressEvent.getDepictedObject());
			}
			if(component.isEnabled())	//if the component is enabled
			{
				final TaskState taskState=webProgressEvent.getTaskState();	//get the new task state
				component.fireProgressed(webProgressEvent.getTask(), taskState, webProgressEvent.getProgress(), webProgressEvent.getGoal());	//tell the control that progress has been made
				if(webProgressEvent.getTask()==null)	//if this is a progress indication for the entire transfer
				{
					component.setSendState(taskState);	//update the control with the new state
				}
			}
		}
		super.processEvent(event);	//do the default event processing
	}	

	/**Writes the beginning part of the outer decorator element.
	This version write additional patching attributes.
	@exception IOException if there is an error rendering the component.
	*/
	protected void writeDecoratorBegin() throws IOException
	{
		final WebDepictContext depictContext=getDepictContext();	//get the depict context
		final C component=getDepictedObject();	//get the component
		depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, "patchType", "none");	//guise:patchType="none" never patch this component TODO use constants
		final TaskState sendState=component.getSendState();	//see which send state the component is in
Debug.trace("updating resource collect control; current send state is", sendState);
		if(sendState!=null)	//if there is a send state
		{
			switch(sendState)	//see which send state the component is in
			{
				case INITIALIZE:	//if the component is preparing to send
					final String destinationPath=component.getDestinationPath();	//get the destination path
					assert destinationPath!=null : "Destination path not expected to be null when component is sending information.";
					final String resolvedPath=component.getSession().getApplication().resolvePath(destinationPath);	//resolve the path
					final Bookmark bookmark=component.getDestinationBookmark();	//get the bookmark to be used
					final String sendPath=bookmark!=null ? resolvedPath+bookmark.toString() : resolvedPath;	//append the bookmark if needed
					depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, ATTRIBUTE_COMMANDS, "sendResources("+sendPath+")");	//guise:command="sendResources(destinationPath)" TODO use a constant for the command
					break;
				case COMPLETE:	//if the component has finished sending
					depictContext.writeAttribute(GUISE_ML_NAMESPACE_URI, ATTRIBUTE_COMMANDS, "sendCompleted()");	//guise:command="complete()" TODO use a constant for the command
					break;
			}
		}
		super.writeDecoratorBegin();	//write the default decorator beginning
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
/*TODO fix
		if(!component.isEditable())	//if the component's model is not editable
		{
			context.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_READONLY, INPUT_READONLY_READONLY);	//readonly="readonly"			
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
				context.writeAttribute(null, ELEMENT_INPUT_ATTRIBUTE_ACCEPT, acceptStringBuilder.toString());	//accept="acceptedContentTypes"							
			}
		}
*/
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
