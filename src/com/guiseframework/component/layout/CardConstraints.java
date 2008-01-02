package com.guiseframework.component.layout;

import static com.globalmentor.java.Objects.*;

import java.net.URI;

import javax.mail.internet.ContentType;

import com.guiseframework.model.*;

/**Constraints on an individual component in a card layout.
@author Garret Wilson
*/
public class CardConstraints extends ControlConstraints implements LabelModel, Displayable, Enableable
{

	/**The label model used by this component.*/
	private final LabelModel labelModel;

		/**@return The label model used by this component.*/
		protected LabelModel getLabelModel() {return labelModel;}

	/**Default constructor.*/
	public CardConstraints()
	{
		this(true);	//construct the class, defaulting to enabled
	}

	/**Enabled constructor.
	@param enabled Whether the card is enabled.
	*/
	public CardConstraints(final boolean enabled)
	{
		this((String)null, enabled);	//construct the class with no label
	}

	/**Label constructor.
	@param label The text of the label.
	*/
	public CardConstraints(final String label)
	{
		this(label, true);	//construct the class, defaulting to enabled
	}

	/**Label and enabled constructor.
	@param label The text of the label.
	@param enabled Whether the card is enabled.
	*/
	public CardConstraints(final String label, final boolean enabled)
	{
		this(new DefaultLabelModel(label), enabled);	//construct the class with a default label model
	}

	/**Label model constructor.
	@param labelModel The label model representing the card label.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public CardConstraints(final LabelModel labelModel)
	{
		this(labelModel, true);	//construct the class, defaulting to enabled
	}

	/**Label model, and enabled constructor.
	@param labelModel The label model representing the card label.
	@param enabled Whether the card is enabled.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public CardConstraints(final LabelModel labelModel, final boolean enabled)
	{
		super(enabled);	//construct the parent class 
		this.labelModel=checkInstance(labelModel, "Label model cannot be null.");	//save the label model
		this.labelModel.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the label model
		this.labelModel.addVetoableChangeListener(getRepeatVetoableChangeListener());	//listen and repeat all vetoable changes of the label model
	}

		//LabelModel delegations

	/**@return The icon URI, which may be a resource URI, or <code>null</code> if there is no icon URI.*/
	public URI getGlyphURI() {return getLabelModel().getGlyphURI();}

	/**Sets the URI of the icon.
	This is a bound property of type <code>URI</code>.
	@param newLabelIcon The new URI of the icon, which may be a resource URI.
	@see #GLYPH_URI_PROPERTY
	*/
	public void setGlyphURI(final URI newLabelIcon) {getLabelModel().setGlyphURI(newLabelIcon);}

	/**@return The label text, which may include a resource reference, or <code>null</code> if there is no label text.*/
	public String getLabel() {return getLabelModel().getLabel();}

	/**Sets the text of the label.
	This is a bound property.
	@param newLabelText The new text of the label, which may include a resource reference.
	@see #LABEL_PROPERTY
	*/
	public void setLabel(final String newLabelText) {getLabelModel().setLabel(newLabelText);}

	/**@return The content type of the label text.*/
	public ContentType getLabelContentType() {return getLabelModel().getLabelContentType();}

	/**Sets the content type of the label text.
	This is a bound property.
	@param newLabelTextContentType The new label text content type.
	@exception NullPointerException if the given content type is <code>null</code>.
	@exception IllegalArgumentException if the given content type is not a text content type.
	@see #LABEL_CONTENT_TYPE_PROPERTY
	*/
	public void setLabelContentType(final ContentType newLabelTextContentType) {getLabelModel().setLabelContentType(newLabelTextContentType);}

}
