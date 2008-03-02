package com.guiseframework.model;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.text.TextUtilities.*;

import java.net.URI;

import javax.mail.internet.ContentType;

import com.globalmentor.java.Objects;

/**A default implementation of a model for an identifier such as text and/or an icon.
@author Garret Wilson
*/
public class DefaultLabelModel extends AbstractModel implements LabelModel
{

	/**The glyph URI, which may be a resource URI, or <code>null</code> if there is no glyph URI.*/
	private URI glyphURI;

		/**@return The glyph URI, which may be a resource URI, or <code>null</code> if there is no glyph URI.*/
		public URI getGlyphURI() {return glyphURI;}

		/**Sets the URI of the glyph.
		This is a bound property.
		@param newGlyphURI The new URI of the glyph, which may be a resource URI.
		@see #GLYPH_URI_PROPERTY
		*/
		public void setGlyphURI(final URI newGlyphURI)
		{
			if(!Objects.equals(glyphURI, newGlyphURI))	//if the value is really changing
			{
				final URI oldGlyphURI=glyphURI;	//get the old value
				glyphURI=newGlyphURI;	//actually change the value
				firePropertyChange(GLYPH_URI_PROPERTY, oldGlyphURI, newGlyphURI);	//indicate that the value changed
			}
		}

	/**The label text, which may include a resource reference, or <code>null</code> if there is no label text.*/
	private String label;

		/**@return The label text, which may include a resource reference, or <code>null</code> if there is no label text.*/
		public String getLabel() {return label;}

		/**Sets the text of the label.
		This is a bound property.
		@param newLabelText The new text of the label, which may include a resource reference.
		@see #LABEL_PROPERTY
		*/
		public void setLabel(final String newLabelText)
		{
			if(!Objects.equals(label, newLabelText))	//if the value is really changing
			{
				final String oldLabel=label;	//get the old value
				label=newLabelText;	//actually change the value
				firePropertyChange(LABEL_PROPERTY, oldLabel, newLabelText);	//indicate that the value changed
			}			
		}

	/**The content type of the label text.*/
	private ContentType labelContentType=PLAIN_TEXT_CONTENT_TYPE;

		/**@return The content type of the label text.*/
		public ContentType getLabelContentType() {return labelContentType;}

		/**Sets the content type of the label text.
		This is a bound property.
		@param newLabelTextContentType The new label text content type.
		@exception NullPointerException if the given content type is <code>null</code>.
		@exception IllegalArgumentException if the given content type is not a text content type.
		@see #LABEL_CONTENT_TYPE_PROPERTY
		*/
		public void setLabelContentType(final ContentType newLabelTextContentType)
		{
			checkInstance(newLabelTextContentType, "Content type cannot be null.");
			if(labelContentType!=newLabelTextContentType)	//if the value is really changing
			{
				final ContentType oldLabelTextContentType=labelContentType;	//get the old value
				if(!isText(newLabelTextContentType))	//if the new content type is not a text content type
				{
					throw new IllegalArgumentException("Content type "+newLabelTextContentType+" is not a text content type.");
				}
				labelContentType=newLabelTextContentType;	//actually change the value
				firePropertyChange(LABEL_CONTENT_TYPE_PROPERTY, oldLabelTextContentType, newLabelTextContentType);	//indicate that the value changed
			}			
		}

	/**Default constructor.*/
	public DefaultLabelModel()
	{
		this(null);	//construct the class with no label
	}

	/**Label constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	*/
	public DefaultLabelModel(final String label)
	{
		this(label, null);	//construct the label model with no glyph
	}

	/**Label and glyph URI constructor.
	@param label The text of the label, or <code>null</code> if there should be no label.
	@param glyphURI The glyph URI, which may be a resource URI, or <code>null</code> if there is no glyph URI.
	*/
	public DefaultLabelModel(final String label, final URI glyphURI)
	{
		this.label=label;	//save the label
		this.glyphURI=glyphURI;	//save the glyph URI
	}

	/**@return A string representation of this label model.*/
	public String toString()
	{
		final String label=getLabel();	//get the label, if any
		return label!=null ? getClass().getName()+": "+label : super.toString();	//return the class and label, or the default string if there is no label
	}
}
