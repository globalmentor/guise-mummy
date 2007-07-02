package com.guiseframework.platform;

import java.io.*;

import javax.mail.internet.ContentType;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.StringBuilderUtilities.*;

import com.garretwilson.text.CharacterEncoding;

/**Encapsulation of text information related to the current depiction.
Text is collected in a {@link StringBuilder} until it is ready to be depicted.
The text writing methods are preferred over retrieving the string builder, as these methods encode information as necessary.
@author Garret Wilson
*/
public interface TextDepictContext extends DepictContext
{

	/**The string builder that holds the current content being collected, though not necessarily all the content collected.
	The string builder returned is appropriate for adding content, but may not be a complete representation of all the text collected.
	@return The string builder that holds the current content being collected for depiction.
	*/
	public StringBuilder getDepictStringBuilder();

	/**Clears all data collected for depiction.*/
	public void clearDepictText();
	
	/**@return The string that holds the current content being collected for depiction.*/
	public String getDepictText();

	/**@return Whether output should be formatted.
	This version returns <code>true</code>.
	*/
	public boolean isFormatted();

	/**@return The zero-based level of text indentation.*/
	public int getIndentLevel();

	/**Sets the level of text indentation.
	@param newIndentLevel The new zero-based level of text indention.
	*/
	public void setIndentLevel(final int newIndentLevel);

	/**Changes the indent level by the given amount.
	@param indentDelta The amount by which to increase or decrease the indent level.
	@see #getIndentLevel()
	@see #setIndentLevel(int)
	*/
	public void indent(final int indentDelta);

	/**Increments the indent level.
	@see #indent(int)
	*/
	public void indent();

	/**Decrements the indent level.
	@see #indent(int)
	*/
	public void unindent();

	/**@return The character encoding currently used for the text output.*/
	public CharacterEncoding getOutputCharacterEncoding();

	/**@return The current content type of the text output.*/
	public ContentType getOutputContentType();

	/**Sets the content type of the text output.
	@param contentType The content type of the text output.
	*/
	public void setOutputContentType(final ContentType contentType);

	/**Writes literal text with no encoding.
	All writing by the controller should use this method.
	@param text The literal text to write.
	@exception IOException if there is an error writing the information.
	*/
	public void writeLiteral(final String text) throws IOException;

	/**Writes a character, encoding it as necessary.
	This method calls {@link #writeLiteral(String)}.
	@param character The character to write.
	@exception IOException if there is an error writing the information.
	*/
	public void write(final char character) throws IOException;

	/**Writes text, encoding it as necessary.
	This method calls {@link #writeLiteral(String)}.
	@param text The text to write.
	@exception IOException if there is an error writing the information.
	*/
	public void write(final String text) throws IOException;

	/**Writes an indention at the current indention level.
	If the context is not formatted, no action occurs.
	@exception IOException if there is an error writing the information.
	@see #isFormatted()
	@see #getIndentLevel()
	*/
	public void writeIndent() throws IOException;

}
