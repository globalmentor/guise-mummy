/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.platform;

import java.io.*;
import java.nio.charset.Charset;

import com.globalmentor.net.ContentType;

/**
 * Encapsulation of text information related to the current depiction. Text is collected in a {@link StringBuilder} until it is ready to be depicted. The text
 * writing methods are preferred over retrieving the string builder, as these methods encode information as necessary.
 * @author Garret Wilson
 */
public interface TextDepictContext extends DepictContext {

	/**
	 * The string builder that holds the current content being collected, though not necessarily all the content collected. The string builder returned is
	 * appropriate for adding content, but may not be a complete representation of all the text collected.
	 * @return The string builder that holds the current content being collected for depiction.
	 */
	public StringBuilder getDepictStringBuilder();

	/** Clears all data collected for depiction. */
	public void clearDepictText();

	/** @return The string that holds the current content being collected for depiction. */
	public String getDepictText();

	/**
	 * @return Whether output should be formatted. This version returns <code>true</code>.
	 */
	public boolean isFormatted();

	/** @return The zero-based level of text indentation. */
	public int getIndentLevel();

	/**
	 * Sets the level of text indentation.
	 * @param newIndentLevel The new zero-based level of text indention.
	 */
	public void setIndentLevel(final int newIndentLevel);

	/**
	 * Changes the indent level by the given amount.
	 * @param indentDelta The amount by which to increase or decrease the indent level.
	 * @see #getIndentLevel()
	 * @see #setIndentLevel(int)
	 */
	public void indent(final int indentDelta);

	/**
	 * Increments the indent level.
	 * @see #indent(int)
	 */
	public void indent();

	/**
	 * Decrements the indent level.
	 * @see #indent(int)
	 */
	public void unindent();

	/** @return The charset currently used for the text output. */
	public Charset getOutputCharset();

	/** @return The current content type of the text output. */
	public ContentType getOutputContentType();

	/**
	 * Sets the content type of the text output.
	 * @param contentType The content type of the text output.
	 */
	public void setOutputContentType(final ContentType contentType);

	/**
	 * Writes literal text with no encoding. All writing by the controller should use this method.
	 * @param text The literal text to write.
	 * @throws IOException if there is an error writing the information.
	 */
	public void writeLiteral(final String text) throws IOException;

	/**
	 * Writes a character, encoding it as necessary. This method calls {@link #writeLiteral(String)}.
	 * @param character The character to write.
	 * @throws IOException if there is an error writing the information.
	 */
	public void write(final char character) throws IOException;

	/**
	 * Writes text, encoding it as necessary. This method calls {@link #writeLiteral(String)}.
	 * @param text The text to write.
	 * @throws IOException if there is an error writing the information.
	 */
	public void write(final String text) throws IOException;

	/**
	 * Writes an indention at the current indention level. If the context is not formatted, no action occurs.
	 * @throws IOException if there is an error writing the information.
	 * @see #isFormatted()
	 * @see #getIndentLevel()
	 */
	public void writeIndent() throws IOException;

}
