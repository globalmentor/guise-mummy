package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.getPropertyName;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

import javax.mail.internet.ContentType;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.ReferenceLayout;
import com.guiseframework.model.*;

/**A text component with an associated label.
This component may have child components, each bound to a specific ID in the text.
When the text is rendered, XML elements with IDs referencing child components will be replaced with representations of those child components.
Child element ID reference replacement can only occur if the text has an XML-based content type (such as XHTML).
<p>This component only supports text content types, including:</p>
<ul>
	<li><code>text/*</code></li>
	<li><code>application/xml</code></li>
	<li><code>application/*+xml</code></li>
</ul>
<p>The component defaults to a content type of <code>text/plain</code>.</p>
@author Garret Wilson
*/
public class Text extends AbstractContainer<Text>
{

	/**The content type bound property.*/
	public final static String TEXT_CONTENT_TYPE_PROPERTY=getPropertyName(Text.class, "contentType");
	/**The text bound property.*/
	public final static String TEXT_PROPERTY=getPropertyName(Text.class, "text");
	/**The text resource key bound property.*/
	public final static String TEXT_RESOURCE_KEY_PROPERTY=getPropertyName(Text.class, "textResourceKey");

	/**@return The layout definition for the text.*/
	public ReferenceLayout getLayout() {return (ReferenceLayout)super.getLayout();}	//a text component can only have a reference layout

	/**The text, or <code>null</code> if there is no text.*/
	private String text=null;

		/**@return The text, or <code>null</code> if there is no text.*/
		public String getText() {return text;}

		/**Sets the text.
		This is a bound property.
		@param newText The new text.
		@see #TEXT_PROPERTY
		*/
		public void setText(final String newText)
		{
			if(!ObjectUtilities.equals(text, newText))	//if the value is really changing
			{
				final String oldText=text;	//get the old value
				text=newText;	//actually change the value
				firePropertyChange(TEXT_PROPERTY, oldText, newText);	//indicate that the value changed
			}			
		}

	/**The content type of the text.*/
	private ContentType textContentType=PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the text.*/
		public ContentType getTextContentType() {return textContentType;}

		/**Sets the content type of the text.
		This is a bound property.
		@param newContentType The new text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see #TEXT_CONTENT_TYPE_PROPERTY
		*/
		public void setTextContentType(final ContentType newContentType)
		{
			checkNull(newContentType, "Content type cannot be null.");
			if(textContentType!=newContentType)	//if the value is really changing
			{
				final ContentType oldContentType=textContentType;	//get the old value
				if(!isText(newContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newContentType+" is not a text content type.");
				}
				textContentType=newContentType;	//actually change the value
				firePropertyChange(TEXT_CONTENT_TYPE_PROPERTY, oldContentType, newContentType);	//indicate that the value changed
			}			
		}

	/**The text resource key, or <code>null</code> if there is no text resource specified.*/
	private String textResourceKey=null;

		/**@return The text resource key, or <code>null</code> if there is no text resource specified.*/
		public String getTextResourceKey() {return textResourceKey;}

		/**Sets the key identifying the text in the resources.
		This is a bound property.
		@param newTextResourceKey The new text resource key.
		@see #TEXT_RESOURCE_KEY_PROPERTY
		*/
		public void setTextResourceKey(final String newTextResourceKey)
		{
			if(!ObjectUtilities.equals(textResourceKey, newTextResourceKey))	//if the value is really changing
			{
				final String oldTextResourceKey=textResourceKey;	//get the old value
				textResourceKey=newTextResourceKey;	//actually change the value
				firePropertyChange(TEXT_RESOURCE_KEY_PROPERTY, oldTextResourceKey, newTextResourceKey);	//indicate that the value changed
			}
		}

	/**Session constructor with a default model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Text(final GuiseSession session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Text(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultModel(session));	//construct the class with a default model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Text(final GuiseSession session, final String id, final Model model)
	{
		super(session, id, new ReferenceLayout(session)/*TODO add text model, model*/);	//construct the parent class
	}
}
