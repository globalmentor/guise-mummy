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

import java.io.*;
import java.util.Set;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import io.guise.mummy.MummyContext;

/**
 * Mummifier for XHTML documents, such as HTML5 documents stored as XML.
 * @implNote This mummifier only works with XHTML documents.
 * @author Garret Wilson
 */
public class XhtmlPageMummifier extends AbstractPageMummifier {

	@Override
	public Set<String> getSupportedFilenameExtensions() {
		return Set.of(XHTML_FILENAME_EXTENSION);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version loads a document in XHTML format.
	 */
	@Override
	public Document loadSourceDocument(final MummyContext context, final InputStream inputStream, final String name) throws IOException, DOMException {
		final DocumentBuilder documentBuilder = context.newPageDocumentBuilder();
		try {
			return documentBuilder.parse(inputStream);
		} catch(final SAXException saxException) {
			final StringBuilder messageBuilder = new StringBuilder("XML error parsing `").append(name).append('`'); //TODO i18n
			if(saxException instanceof SAXParseException) { //get more parse state information if we can
				final SAXParseException saxParseException = (SAXParseException)saxException;
				final String publicId = saxParseException.getPublicId();
				final String systemId = saxParseException.getSystemId();
				final int line = saxParseException.getLineNumber();
				final int column = saxParseException.getColumnNumber();
				if(publicId != null || systemId != null || line >= 0 || column >= 0) { //if we have any informational values
					if(publicId != null) {
						messageBuilder.append(", public ID `").append(publicId).append('`'); //TODO i18n
					}
					if(systemId != null) {
						messageBuilder.append(", system ID `").append(systemId).append('`'); //TODO i18n
					}
					if(line >= 0) {
						messageBuilder.append(", line ").append(line); //TODO i18n
					}
					if(column >= 0) {
						messageBuilder.append(", column ").append(column); //TODO i18n
					}
				}
			}
			final String saxMessage = saxException.getLocalizedMessage();
			if(saxMessage != null) {
				messageBuilder.append(": ").append(saxMessage); //always tack on the original SAX exception message
			} else {
				messageBuilder.append('.'); //TODO i18n
			}
			throw new IOException(messageBuilder.toString(), saxException);
		}
	}

}
