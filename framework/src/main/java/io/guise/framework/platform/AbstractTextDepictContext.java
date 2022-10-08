/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.platform;

import java.io.IOException;

import io.guise.framework.Destination;
import io.guise.framework.GuiseSession;

import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.StringBuilders.*;

/**
 * Abstract encapsulation of text information related to the current depiction.
 * @author Garret Wilson
 */
public abstract class AbstractTextDepictContext extends AbstractDepictContext implements TextDepictContext {

	/** The string builder that holds the current content being collected for depiction, though not necessarily all the content collected. */
	private final StringBuilder depictStringBuilder = new StringBuilder();

	@Override
	public StringBuilder getDepictStringBuilder() {
		return depictStringBuilder;
	}

	@Override
	public void clearDepictText() {
		clear(depictStringBuilder); //clear the string builder
	}

	@Override
	public String getDepictText() {
		return depictStringBuilder.toString(); //return a string version of the text collected so far
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version returns <code>true</code>.
	 * </p>
	 */
	@Override
	public boolean isFormatted() {
		return true;
	}

	/** The zero-based level of text indentation. */
	private int indentLevel = 0;

	@Override
	public int getIndentLevel() {
		return indentLevel;
	}

	@Override
	public void setIndentLevel(final int newIndentLevel) {
		indentLevel = newIndentLevel; //actually change the value
	}

	@Override
	public void indent(final int indentDelta) {
		setIndentLevel(getIndentLevel() + indentDelta); //change the indention amount by the given delta
	}

	@Override
	public void indent() {
		indent(1); //indent by 1
	}

	@Override
	public void unindent() {
		indent(-1); //indent by -1
	}

	/**
	 * Guise session constructor.
	 * @param session The Guise user session of which this context is a part.
	 * @param destination The destination with which this context is associated.
	 * @throws NullPointerException if the given session and/or destination is null.
	 * @throws IOException If there was an I/O error loading a needed resource.
	 */
	public AbstractTextDepictContext(final GuiseSession session, final Destination destination) throws IOException {
		super(session, destination); //construct the parent class
	}

	@Override
	public void writeLiteral(final String text) throws IOException {
		getDepictStringBuilder().append(text); //append the text directly to the string builder
	}

	@Override
	public void write(final char character) throws IOException {
		write(String.valueOf(character)); //convert the character to a string and write it
	}

	@Override
	public void write(final String text) throws IOException {
		writeLiteral(encode(text)); //encode and write the text
	}

	/**
	 * Encodes text information for writing. This method uses a string parameter so that no processing need occur if no characters need encoded. This version does
	 * no processing of the text.
	 * @param string The text information to encode.
	 * @return The encoded text.
	 */
	protected String encode(final String string) {
		return string;
	}

	@Override
	public void writeIndent() throws IOException {
		if(isFormatted()) { //if we should format the output
			append(getDepictStringBuilder(), CHARACTER_TABULATION_CHAR, getIndentLevel()); //write the correct number of tabs
		}
	}

}
