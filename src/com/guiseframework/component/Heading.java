package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

import java.net.URI;

import javax.mail.internet.ContentType;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;

/**A heading component.
@author Garret Wilson
*/
public class Heading extends AbstractComponent<Heading> implements LabeledComponent<Heading>
{

	/**The heading level value indicating no heading level.*/
	public final static int NO_HEADING_LEVEL=-1;
	
	/**The level bound property.*/
	public final static String LEVEL_PROPERTY=getPropertyName(Heading.class, "level");

	/**The label icon URI, or <code>null</code> if there is no icon URI.*/
	private URI labelIcon=null;

		/**@return The label icon URI, or <code>null</code> if there is no icon URI.*/
		public URI getLabelIcon() {return labelIcon;}

		/**Sets the URI of the label icon.
		This is a bound property of type <code>URI</code>.
		@param newLabelIcon The new URI of the label icon.
		@see #LABEL_ICON_PROPERTY
		*/
		public void setLabelIcon(final URI newLabelIcon)
		{
			if(!ObjectUtilities.equals(labelIcon, newLabelIcon))	//if the value is really changing
			{
				final URI oldLabelIcon=labelIcon;	//get the old value
				labelIcon=newLabelIcon;	//actually change the value
				firePropertyChange(LABEL_ICON_PROPERTY, oldLabelIcon, newLabelIcon);	//indicate that the value changed
			}			
		}

	/**The label icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
	private String labelIconResourceKey=null;

		/**@return The label icon URI resource key, or <code>null</code> if there is no icon URI resource specified.*/
		public String getLabelIconResourceKey() {return labelIconResourceKey;}

		/**Sets the key identifying the URI of the label icon in the resources.
		This is a bound property.
		@param newIconResourceKey The new label icon URI resource key.
		@see #LABEL_ICON_RESOURCE_KEY_PROPERTY
		*/
		public void setLabelIconResourceKey(final String newIconResourceKey)
		{
			if(!ObjectUtilities.equals(labelIconResourceKey, newIconResourceKey))	//if the value is really changing
			{
				final String oldIconResourceKey=labelIconResourceKey;	//get the old value
				labelIconResourceKey=newIconResourceKey;	//actually change the value
				firePropertyChange(LABEL_ICON_RESOURCE_KEY_PROPERTY, oldIconResourceKey, newIconResourceKey);	//indicate that the value changed
			}
		}

	/**The label text, or <code>null</code> if there is no label text.*/
	private String labelText=null;

		/**@return The label text, or <code>null</code> if there is no label text.*/
		public String getLabelText() {return labelText;}

		/**Sets the text of the label.
		This is a bound property.
		@param newLabelText The new text of the label.
		@see #LABEL_TEXT_PROPERTY
		*/
		public void setLabelText(final String newLabelText)
		{
			if(!ObjectUtilities.equals(labelText, newLabelText))	//if the value is really changing
			{
				final String oldLabel=labelText;	//get the old value
				labelText=newLabelText;	//actually change the value
				firePropertyChange(LABEL_TEXT_PROPERTY, oldLabel, newLabelText);	//indicate that the value changed
			}			
		}

	/**The content type of the label text.*/
	private ContentType labelTextContentType=Model.PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the label text.*/
		public ContentType getLabelTextContentType() {return labelTextContentType;}

		/**Sets the content type of the label text.
		This is a bound property.
		@param newLabelTextContentType The new label text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see #LABEL_TEXT_CONTENT_TYPE_PROPERTY
		*/
		public void setLabelTextContentType(final ContentType newLabelTextContentType)
		{
			checkNull(newLabelTextContentType, "Content type cannot be null.");
			if(labelTextContentType!=newLabelTextContentType)	//if the value is really changing
			{
				final ContentType oldLabelTextContentType=labelTextContentType;	//get the old value
				if(!isText(newLabelTextContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newLabelTextContentType+" is not a text content type.");
				}
				labelTextContentType=newLabelTextContentType;	//actually change the value
				firePropertyChange(LABEL_TEXT_CONTENT_TYPE_PROPERTY, oldLabelTextContentType, newLabelTextContentType);	//indicate that the value changed
			}			
		}

	/**The label text resource key, or <code>null</code> if there is no label text resource specified.*/
	private String labelTextResourceKey=null;
	
		/**@return The label text resource key, or <code>null</code> if there is no label text resource specified.*/
		public String getLabelTextResourceKey() {return labelTextResourceKey;}
	
		/**Sets the key identifying the text of the label in the resources.
		This is a bound property.
		@param newLabelTextResourceKey The new label text resource key.
		@see #LABEL_TEXT_RESOURCE_KEY_PROPERTY
		*/
		public void setLabelTextResourceKey(final String newLabelTextResourceKey)
		{
			if(!ObjectUtilities.equals(labelTextResourceKey, newLabelTextResourceKey))	//if the value is really changing
			{
				final String oldLabelTextResourceKey=labelTextResourceKey;	//get the old value
				labelTextResourceKey=newLabelTextResourceKey;	//actually change the value
				firePropertyChange(LABEL_TEXT_RESOURCE_KEY_PROPERTY, oldLabelTextResourceKey, newLabelTextResourceKey);	//indicate that the value changed
			}
		}

	/**The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.*/
	private int level;

		/**@return The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.*/
		public int getLevel() {return level;}

		/**Sets the level of the heading.
		This is a bound property of type <code>Integer</code>.
		@param newLevel The new zero-based heading level, or {@link #NO_HEADING_LEVEL} if no level is specified.
		@see #LEVEL_PROPERTY
		*/
		public void setLevel(final int newLevel)
		{
			if(level!=newLevel)	//if the value is really changing
			{
				final int oldLevel=level;	//get the old value
				level=newLevel;	//actually change the value
				firePropertyChange(LEVEL_PROPERTY, oldLevel, newLevel);	//indicate that the value changed
			}			
		}

	/**Session constructor with a default model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Heading(final GuiseSession session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session constructor with a default model with the given heading level.
	@param session The Guise session that owns this component.
	@param level The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Heading(final GuiseSession session, final int level)
	{
		this(session, null, level);	//construct the component, indicating that a default ID and the given heading level should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Heading(final GuiseSession session, final String id)
	{
		this(session, id, NO_HEADING_LEVEL);	//construct the class with a default model with no heading level
	}

	/**Session, ID, and heading level.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param level The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Heading(final GuiseSession session, final String id, final int level)
	{
		this(session, id, new DefaultModel(session), level);	//construct the class with a default model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Heading(final GuiseSession session, final String id, final Model model)
	{
		this(session, id, model, NO_HEADING_LEVEL);	//construct the class with no heading level
	}

	/**Session, ID, model, and level constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param level The zero-based level of the heading, or {@link #NO_HEADING_LEVEL} if no level is specified.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Heading(final GuiseSession session, final String id, final Model model, final int level)
	{
		super(session, id, model);	//construct the parent class
		this.level=level;	//save the level
	}
}
