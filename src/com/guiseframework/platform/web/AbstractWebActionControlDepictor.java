package com.guiseframework.platform.web;

import java.net.URI;
import java.util.Set;

import com.guiseframework.component.*;
import com.guiseframework.platform.DepictEvent;
import com.guiseframework.platform.PlatformEvent;

import static com.guiseframework.platform.web.GuiseCSSStyleConstants.*;

/**Abstract depictor for rendering simple action controls in XHTML.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public abstract class AbstractWebActionControlDepictor<C extends ActionControl> extends AbstractSimpleWebComponentDepictor<C>
{

	/**Default constructor with no element representation.*/
	public AbstractWebActionControlDepictor()
	{
		this(null, null);	//construct the strategy with no element representation
	}

	/**Element namespace and local name constructor that doesn't create an empty element, even if there is no content.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	*/
	public AbstractWebActionControlDepictor(final URI namespaceURI, final String localName)
	{
		this(namespaceURI, localName, false);	//don't allow an empty element
	}

	/**Element namespace and local name constructor.
	@param namespaceURI The URI of the XML namespace of the element, or <code>null</code> if there is no namespace.
	@param localName The local name of the element with no prefix, or <code>null</code> if this component should not be rendered as an element.
	@param isEmptyElementAllowed Whether an empty element can be created if there is no content.
	*/
	public AbstractWebActionControlDepictor(final URI namespaceURI, final String localName, final boolean isEmptyElementAllowed)
	{
		super(namespaceURI, localName, isEmptyElementAllowed);	//construct the parent class
	}

	/**Processes an event from the platform.
	@param event The event to be processed.
	@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	*/
	public void processEvent(final PlatformEvent event)
	{
		if(event instanceof WebActionEvent)	//if this is an action event
		{
			final WebActionEvent webActionEvent=(WebActionEvent)event;	//get the web action event
			final C component=getDepictedObject();	//get the depicted object
			if(webActionEvent.getDepictedObject()!=component)	//if the event was meant for another depicted object
			{
				throw new IllegalArgumentException("Depict event "+event+" meant for depicted object "+webActionEvent.getDepictedObject());
			}
			if(component.isEnabled())	//if the component is enabled
			{
				component.performAction();	//tell the component to perform its action TODO implement option and perhaps force
			}
		}
/*TODO fix for non-AJAX form posts
		if(event instanceof FormControlEvent)	//if this is a form submission
		{
			final FormControlEvent formEvent=(FormControlEvent)event;	//get the form submit event
			final String frameActionInputID=XHTMLApplicationFrameView.getActionInputID(component.getSession().getApplicationFrame());	//get the ID of the hidden action input field
			final String actionInputValue=asInstance(formEvent.getParameterListMap().getItem(frameActionInputID), String.class);	//get the action input value
			if(component.getID().equals(actionInputValue))	//if this action put its ID in the hidden input field
			final String componentID=getPlatform().getDepictIDString(component.getID());	//get this component's ID
			final String value=asInstance(formEvent.getParameterListMap().getItem(componentID), String.class);	//get the form value for this control
			if(componentID.equals(value))	//if this action put its ID as the value
			{
				if(component.isEnabled())	//if the component is enabled
				{
					component.performAction();	//tell the component to perform its action
				}
			}
		}
*/
		super.processEvent(event);	//do the default event processing
	}

	/**Retrieves the base style IDs for the given component.
	This version adds an attribute for selected status if the control is a select action control with a selected model.
	This version also adds an attribute if the component is disabled.
	@param prefix The prefix that needs to be added to each, or <code>null</code> if there is no prefix to add.
	@param suffix The suffix that needs to be added to each, or <code>null</code> if there is no suffix to add.
	@return The base style IDs for the component.
	@see SelectActionControl
	@see GuiseCSSStyleConstants#SELECTED_CLASS
	@see GuiseCSSStyleConstants#DISABLED_CLASS
	*/
	protected Set<String> getBaseStyleIDs(final String prefix, final String suffix)
	{
		final Set<String> baseStyleIDs=super.getBaseStyleIDs(prefix, suffix);	//get the default base style IDs
		if(!getDepictedObject().isEnabled())	//if this component is disabled	//TODO maybe add an Enableable interface
		{
			baseStyleIDs.add(DISABLED_CLASS);	//add the disabled class ID
		}
		return baseStyleIDs;	//return the new style IDs
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

}
