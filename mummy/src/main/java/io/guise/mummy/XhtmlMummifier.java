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

package io.guise.mummy;

import java.io.*;
import java.nio.file.*;
import java.util.Set;

import javax.xml.parsers.*;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Mummifier for XHTML documents, such as HTML5 documents stored as XML.
 * @author Garret Wilson
 */
public class XhtmlMummifier implements ResourceMummifier {

	@Override
	public Set<String> getSupportedFilenameExtensions() {
		return Set.of("xhtml"); //TODO use constant
	}

	@Override
	public void mummify(final GuiseMummyContext context, final Path resourcePath, final Path outputPath) throws IOException {
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance(); //TODO use shared factory?
		DocumentBuilder documentBuilder;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch(final ParserConfigurationException parserConfigurationException) {
			throw new IOException(parserConfigurationException);
		}
		final Document document;
		try (final InputStream inputStream = new BufferedInputStream(Files.newInputStream(resourcePath))) {
			try {
				document = documentBuilder.parse(inputStream);//TODO install appropriate entity resolvers as needed
			} catch(final SAXException saxException) {
				throw new IOException(saxException);
			}
		}
		System.out.println("parsed document: " + resourcePath);
	}

}
