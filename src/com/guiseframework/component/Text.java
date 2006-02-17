package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import javax.mail.internet.ContentType;

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
public class Text extends AbstractContainer<Text> implements TextModel
{

	/**The text model used by this component.*/
	private final TextModel textModel;

		/**@return The text model used by this component.*/
		protected TextModel getTextModel() {return textModel;}

	/**@return The layout definition for the text.*/
	public ReferenceLayout getLayout() {return (ReferenceLayout)super.getLayout();}	//a text component can only have a reference layout

	/**Session constructor with a default text model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Text(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default text model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Text(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultTextModel(session));	//construct the class with a default model
	}

	/**Session and text model constructor.
	@param session The Guise session that owns this component.
	@param textModel The component text model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public Text(final GuiseSession session, final TextModel textModel)
	{
		this(session, null, textModel);	//construct the component, indicating that a default ID should be used
	}

	/**Session, ID, and text model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param textModel The component text model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Text(final GuiseSession session, final String id, final TextModel textModel)
	{
		super(session, id, new ReferenceLayout(session));	//construct the parent class
		this.textModel=checkNull(textModel, "Text model cannot be null.");	//save the text model
		this.textModel.addPropertyChangeListener(getRepeaterPropertyChangeListener());	//listen and repeat all property changes of the text model
	}

		//TextModel delegates 

	/**@return The text, or <code>null</code> if there is no text.*/
	public String getText() {return getTextModel().getText();}

	/**Sets the text.
	This is a bound property.
	@param newText The new text.
	@see #TEXT_PROPERTY
	*/
	public void setText(final String newText) {getTextModel().setText(newText);}

	/**@return The content type of the text.*/
	public ContentType getTextContentType() {return getTextModel().getTextContentType();}

	/**Sets the content type of the text.
	This is a bound property.
	@param newTextContentType The new text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #TEXT_CONTENT_TYPE_PROPERTY
	*/
	public void setTextContentType(final ContentType newTextContentType) {getTextModel().setTextContentType(newTextContentType);}

	/**@return The text resource key, or <code>null</code> if there is no text resource specified.*/
	public String getTextResourceKey() {return getTextModel().getTextResourceKey();}

	/**Sets the key identifying the text in the resources.
	This is a bound property.
	@param newTextResourceKey The new text resource key.
	@see #TEXT_RESOURCE_KEY_PROPERTY
	*/
	public void setTextResourceKey(final String newTextResourceKey) {getTextModel().setTextResourceKey(newTextResourceKey);}

}
