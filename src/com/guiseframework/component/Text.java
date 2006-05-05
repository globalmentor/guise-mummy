package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import javax.mail.internet.ContentType;

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

	/**Default constructor with a default text model.*/
	public Text()
	{
		this(new DefaultTextModel());	//construct the class with a default text model
	}

	/**Text model constructor.
	@param textModel The component text model.
	@exception NullPointerException if the given text model is <code>null</code>.
	*/
	public Text(final TextModel textModel)
	{
		super(new ReferenceLayout());	//construct the parent class
		this.textModel=checkInstance(textModel, "Text model cannot be null.");	//save the text model
		this.textModel.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the text model
		this.textModel.addVetoableChangeListener(getRepeatVetoableChangeListener());	//listen and repeat all vetoable changes of the text model
	}

		//TextModel delegates 

	/**@return The text, which may include a resource reference, or <code>null</code> if there is no text.*/
	public String getText() {return getTextModel().getText();}

	/**Sets the text.
	This is a bound property.
	@param newText The new text, which may include a resource reference.
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

}
