package com.guiseframework.component.layout;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.net.URI;

import javax.mail.internet.ContentType;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;

/**Constraints on an individual component in a card layout.
@author Garret Wilson
*/
public class CardConstraints extends AbstractConstraints implements LabelModel, Displayable, Enableable
{

	/**The label model used by this component.*/
	private final LabelModel labelModel;

		/**@return The label model used by this component.*/
		protected LabelModel getLabelModel() {return labelModel;}

	/**Whether the card is displayed or has no representation, taking up no space.*/
	private boolean displayed=true;

		/**@return Whether the card is displayed or has no representation, taking up no space.*/
		public boolean isDisplayed() {return displayed;}

		/**Sets whether the card is displayed or has no representation, taking up no space.
		This is a bound property of type <code>Boolean</code>.
		@param newDisplayed <code>true</code> if the card should be displayed, else <code>false</code> if the card should take up no space.
		@see #DISPLAYED_PROPERTY
		*/
		public void setDisplayed(final boolean newDisplayed)
		{
			if(displayed!=newDisplayed)	//if the value is really changing
			{
				final boolean oldDisplayed=displayed;	//get the current value
				displayed=newDisplayed;	//update the value
				firePropertyChange(DISPLAYED_PROPERTY, Boolean.valueOf(oldDisplayed), Boolean.valueOf(newDisplayed));
			}
		}

	/**Whether the card is enabled for selection.*/
	private boolean enabled=true;

		/**@return Whether the card is enabled for selection.*/
		public boolean isEnabled() {return enabled;}

		/**Sets whether the the card is enabled for selection.
		This is a bound property of type <code>Boolean</code>.
		@param newEnabled <code>true</code> if the corresponding card can be selected.
		@see #ENABLED_PROPERTY
		*/
		public void setEnabled(final boolean newEnabled)
		{
			if(enabled!=newEnabled)	//if the value is really changing
			{
				final boolean oldEnabled=enabled;	//get the old value
				enabled=newEnabled;	//actually change the value
				firePropertyChange(ENABLED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newEnabled));	//indicate that the value changed
			}			
		}

	/**Session constructor.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CardConstraints(final GuiseSession session)
	{
		this(session, true);	//construct the class with no label
	}

	/**Session and enabled constructor.
	@param session The Guise session that owns this model.
	@param enabled Whether the card is enabled.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CardConstraints(final GuiseSession session, final boolean enabled)
	{
		this(session, (String)null, enabled);	//construct the class with no label
	}

	/**Session and label constructor.
	@param session The Guise session that owns this model.
	@param label The text of the label.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CardConstraints(final GuiseSession session, final String label)
	{
		this(session, label, true);	//construct the class, defaulting to enabled
	}

	/**Session, label, and enabled constructor.
	@param session The Guise session that owns this model.
	@param label The text of the label.
	@param enabled Whether the card is enabled.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public CardConstraints(final GuiseSession session, final String label, final boolean enabled)
	{
		this(session, new DefaultLabelModel(session, label), enabled);	//construct the class with a default label model
	}

	/**Session and label model.
	@param session The Guise session that owns this model.
	@param labelModel The label model representing the card label.
	@exception NullPointerException if the given session and/or label model is <code>null</code>.
	*/
	public CardConstraints(final GuiseSession session, final LabelModel labelModel)
	{
		this(session, labelModel, true);	//construct the class, defaulting to enabled
	}

	/**Session, label model, and enabled constructor.
	@param session The Guise session that owns this model.
	@param labelModel The label model representing the card label.
	@param enabled Whether the card is enabled.
	@exception NullPointerException if the given session and/or label model is <code>null</code>.
	*/
	public CardConstraints(final GuiseSession session, final LabelModel labelModel, final boolean enabled)
	{
		super(session);	//construct the parent class 
		this.labelModel=checkNull(labelModel, "Label model cannot be null.");	//save the label model
		this.labelModel.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the label model
		this.enabled=enabled;	//save the enabled state
	}

		//LabelModel delegations

	/**@return The icon URI, or <code>null</code> if there is no icon URI.*/
	public URI getIcon() {return getLabelModel().getIcon();}

	/**Sets the URI of the icon.
	This is a bound property of type <code>URI</code>.
	@param newLabelIcon The new URI of the icon.
	@see #ICON_PROPERTY
	*/
	public void setIcon(final URI newLabelIcon) {getLabelModel().setIcon(newLabelIcon);}

	/**@return The icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
	public String getIconResourceKey() {return getLabelModel().getIconResourceKey();}

	/**Sets the key identifying the URI of the icon in the resources.
	This is a bound property.
	@param newIconResourceKey The new icon URI resource key.
	@see #ICON_RESOURCE_KEY_PROPERTY
	*/
	public void setIconResourceKey(final String newIconResourceKey) {getLabelModel().setIconResourceKey(newIconResourceKey);}

	/**@return The label text, or <code>null</code> if there is no label text.*/
	public String getLabel() {return getLabelModel().getLabel();}

	/**Sets the text of the label.
	This is a bound property.
	@param newLabelText The new text of the label.
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

	/**@return The label text resource key, or <code>null</code> if there is no label text resource specified.*/
	public String getLabelResourceKey() {return getLabelModel().getLabelResourceKey();}

	/**Sets the key identifying the text of the label in the resources.
	This is a bound property.
	@param newLabelTextResourceKey The new label text resource key.
	@see #LABEL_RESOURCE_KEY_PROPERTY
	*/
	public void setLabelResourceKey(final String newLabelTextResourceKey) {getLabelModel().setLabelResourceKey(newLabelTextResourceKey);}

}
