/*
 * Copyright © 2019 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.mummy.mummify.page;

import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.io.InputStreams.*;
import static com.globalmentor.io.Readers.*;
import static com.globalmentor.xml.spec.XML.*;
import static java.nio.charset.StandardCharsets.*;

import java.io.*;
import java.nio.charset.CodingErrorAction;
import java.util.Set;

import org.w3c.dom.*;

import com.globalmentor.io.*;
import com.globalmentor.xml.spec.XML;

import io.guise.mummy.MummyContext;

/**
 * Mummifier for general HTML documents.
 * @implSpec This mummifier supports general HTML documents such as HTML5 using lenient HTML parsing rules. If the document begins with the XML declaration
 *           {@value XML#XML_DECL_START}, the document is parsed ax XHTML using the stricter XML parsing rules, delegating to {@link XhtmlPageMummifier} for
 *           parsing.
 * @implSpec General HTML mummification has not yet been implemented.
 * @author Garret Wilson
 */
public class HtmlPageMummifier extends XhtmlPageMummifier {

	@Override
	public Set<String> getSupportedFilenameExtensions() {
		return Set.of(HTML_FILENAME_EXTENSION, HTM_FILENAME_EXTENSION);
	}

	/**
	 * The number of bytes to read and test. This includes:
	 * <ul>
	 * <li>Enough room for the byte order mark.</li>
	 * <li>Enough room for the XML declaration in whatever encoding.</li>
	 * <li>Extra padding to keep from going past the end of the buffer.</li>
	 * </ul>
	 */
	private static final int TEST_BUFFER_SIZE = ByteOrderMark.UTF_32BE.getLength() + (XML_DECL_START.length() * ByteOrderMark.MAX_BYTE_COUNT) + 8;

	/**
	 * {@inheritDoc}
	 * @implSpec This version parses the document in HTML format unless it begins with the XML declaration {@value XML#XML_DECL_START}, in which case the document
	 *           is parsed as XML by delegating to {@link XhtmlPageMummifier#loadSourceDocument(MummyContext, InputStream, String)}.
	 */
	@Override
	public Document loadSourceDocument(final MummyContext context, final InputStream inputStream, final String name) throws IOException, DOMException {
		final InputStream markedInputStream = toMarkSupportedInputStream(inputStream); //make sure our input stream supports mark/reset
		final boolean hasXmlDeclaration;
		{
			//Temporarily create a test reader that detects the BOM but will substitute a replacement character for any errors (as we are just testing, not actually parsing).
			//Create a buffered reader so that we can reset after checking for the XML declaration, but only use a buffer large enough for the text characters.
			markedInputStream.mark(TEST_BUFFER_SIZE); //save enough room for the number of bytes to read
			final Reader testReader = new BufferedReader(new BOMInputStreamReader(toMarkSupportedInputStream(inputStream), UTF_8, CodingErrorAction.REPLACE),
					TEST_BUFFER_SIZE);
			hasXmlDeclaration = XML_DECL_START.equals(readString(testReader, XML_DECL_START.length())); //just read enough to test the XML declaration
			markedInputStream.reset(); //reset the original input stream back to the way it was
		}
		if(hasXmlDeclaration) {
			return super.loadSourceDocument(context, markedInputStream, name); //delegate to the XHTML parsing version _passing our stream we marked and reset_
		}
		throw new UnsupportedOperationException(String.format("General HTML parsing not yet supported for `%s`.", name));
	}
}
