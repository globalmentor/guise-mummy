/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy.mummify.page;

import static com.globalmentor.html.spec.HTML.*;

import java.io.*;
import java.util.Set;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import io.guise.mummy.MummyContext;

/**
 * Mummifier for XHTML documents, such as HTML5 documents stored as XML.
 * @author Garret Wilson
 */
public class XhtmlPageMummifier extends AbstractPageMummifier {

	@Override
	public Set<String> getSupportedFilenameExtensions() {
		return Set.of(XHTML_NAME_EXTENSION);
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
			throw new IOException(saxException);
		}
	}
}
