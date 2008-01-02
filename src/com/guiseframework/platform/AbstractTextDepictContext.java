package com.guiseframework.platform;

import java.io.IOException;

import com.guiseframework.Destination;
import com.guiseframework.GuiseSession;

import static com.garretwilson.text.Characters.*;
import static com.globalmentor.java.StringBuilders.*;

/**Abstract encapsulation of text information related to the current depiction.
@author Garret Wilson
*/
public abstract class AbstractTextDepictContext extends AbstractDepictContext implements TextDepictContext
{

	/**The string builder that holds the current content being collected for depiction, though not necessarily all the content collected.*/
	private final StringBuilder depictStringBuilder=new StringBuilder();

		/**The string builder that holds the current content being collected, though not necessarily all the content collected.
		The string builder returned is appropriate for adding content, but may not be a complete representation of all the text collected.
		@return The string builder that holds the current content being collected for depiction.
		*/
		public StringBuilder getDepictStringBuilder() {return depictStringBuilder;}

	/**Clears all data collected for depiction.*/
	public void clearDepictText()
	{
		clear(depictStringBuilder);	//clear the string builder
	}
	
	/**@return The string that holds the current content being collected for depiction.*/
	public String getDepictText()
	{
		return depictStringBuilder.toString();	//return a string version of the text collected so far
	}

	/**@return Whether output should be formatted.
	This version returns <code>true</code>.
	*/
	public boolean isFormatted() {return true;}
	
	/**The zero-based level of text indentation.*/
	private int indentLevel=0;

		/**@return The zero-based level of text indentation.*/
		public int getIndentLevel() {return indentLevel;}

		/**Sets the level of text indentation.
		@param newIndentLevel The new zero-based level of text indention.
		*/
		public void setIndentLevel(final int newIndentLevel)
		{
			indentLevel=newIndentLevel;	//actually change the value
		}

		/**Changes the indent level by the given amount.
		@param indentDelta The amount by which to increase or decrease the indent level.
		@see #getIndentLevel()
		@see #setIndentLevel(int)
		*/
		public void indent(final int indentDelta)
		{
			setIndentLevel(getIndentLevel()+indentDelta);	//change the indention amount by the given delta
		}

		/**Increments the indent level.
		@see #indent(int)
		*/
		public void indent()
		{
			indent(1);	//indent by 1
		}

		/**Decrements the indent level.
		@see #indent(int)
		*/
		public void unindent()
		{
			indent(-1);	//indent by -1
		}

	/**Guise session constructor.
	@param session The Guise user session of which this context is a part.
	@param destination The destination with which this context is associated.
	@exception NullPointerException if the given session and/or destination is null.
	@exception IOException If there was an I/O error loading a needed resource.
	*/
	public AbstractTextDepictContext(final GuiseSession session, final Destination destination) throws IOException
	{
		super(session, destination);	//construct the parent class
	}

	/**Writes literal text with no encoding.
	All writing by the controller should use this method.
	@param text The literal text to write.
	@exception IOException if there is an error writing the information.
	*/
	public void writeLiteral(final String text) throws IOException
	{
		getDepictStringBuilder().append(text);	//append the text directly to the string builder
	}

	/**Writes a character, encoding it as necessary.
	This method calls {@link #writeLiteral(String)}.
	@param character The character to write.
	@exception IOException if there is an error writing the information.
	@see #encode(StringBuilder)
	*/
	public void write(final char character) throws IOException
	{
		write(String.valueOf(character));	//convert the character to a string and write it
	}

	/**Writes text, encoding it as necessary.
	This method calls {@link #writeLiteral(String)}.
	@param text The text to write.
	@exception IOException if there is an error writing the information.
	@see #encode(StringBuilder)
	*/
	public void write(final String text) throws IOException
	{
		writeLiteral(encode(text));	//encode and write the text
	}

	/**Encodes text information for writing.
	This method uses a string parameter so that no processing need occur if no characters need encoded.
	This version does no processing of the text.
	@param string The text information to encode.
	@return The encoded text.
	*/
	protected String encode(final String string)
	{
		return string;
	}

	/**Writes an indention at the current indention level.
	If the context is not formatted, no action occurs.
	@exception IOException if there is an error writing the information.
	@see #isFormatted()
	@see #getIndentLevel()
	*/
	public void writeIndent() throws IOException
	{
		if(isFormatted())	//if we should format the output
		{
			append(getDepictStringBuilder(), HORIZONTAL_TABULATION_CHAR, getIndentLevel());	//write the correct number of tabs
		}
	}

}
