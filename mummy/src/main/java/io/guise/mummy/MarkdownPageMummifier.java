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

import static java.nio.charset.StandardCharsets.*;

import java.io.*;
import java.util.Set;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.globalmentor.io.BOMInputStreamReader;
import com.globalmentor.io.Filenames;
import com.globalmentor.text.StringTemplate;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;

/**
 * Mummifier for Markdown documents.
 * @implSpec This implementation defaults to UTF-8, producing an error if the encoding isn't valid. Other encodings are supported with the appropriate BOM.
 * @author Garret Wilson
 */
public class MarkdownPageMummifier extends AbstractPageMummifier {

	/**
	 * The template for wrapping an XHTML document around the generated HTML. It has the following parameters:
	 * <ol>
	 * <li>Page {@code <title>} content.</li>
	 * <li>Page {@code <body>} content.</li>
	 * </ol>
	 */
	private static final StringTemplate XHTML_TEMPLATE = StringTemplate.builder() //@formatter:off
					.text("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").newline()
					.text("<!DOCTYPE html>").newline()
					.text("<html xmlns=\"http://www.w3.org/1999/xhtml\">").newline() 
					.text("<head>").newline() 
					.text("<title>").parameter(StringTemplate.STRING_PARAMETER).text("</title>").newline() 
					.text("</head>").newline()
					.text("<body>").newline()
					.parameter(StringTemplate.STRING_PARAMETER)
					.text("</body>")
					.text("</html>").newline().build();	//@formatter:on

	/**
	 * {@inheritDoc}
	 * @implNote The supported extensions follow the two given in <a href="https://tools.ietf.org/html/rfc7763">RFC 7763</a> so as to promote consistency, namely
	 *           <code>md</code> and <code>markdown</code>, even though there are <a href="https://superuser.com/a/285878/954883">many variations</a> in the wild.
	 */
	@Override
	public Set<String> getSupportedFilenameExtensions() {
		return Set.of("md", "markdown");
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version loads a document in Markdown format.
	 */
	@Override
	public Document loadSourceDocument(final MummyContext context, final InputStream inputStream, final String name) throws IOException, DOMException {
		//create a Reader to detect the BOM and to throw errors if the encoding is invalid
		@SuppressWarnings("resource") //we don't manage the underlying input stream
		final BOMInputStreamReader bomInputStreamReader = new BOMInputStreamReader(new BufferedInputStream(inputStream)); //TODO create utility to ensure mark supported
		final StringBuilder stringBuilder = new StringBuilder();
		final char[] buffer = new char[64 * 1024];
		int charsRead;
		while((charsRead = bomInputStreamReader.read(buffer)) != -1) { //TODO create utility to read all from a reader
			stringBuilder.append(buffer, 0, charsRead);
		}
		final Parser parser = Parser.builder().build();
		com.vladsch.flexmark.util.ast.Document document = parser.parse(stringBuilder.toString());
		final HtmlRenderer renderer = HtmlRenderer.builder().build();
		final String htmlBodyContent = renderer.render(document);
		final String title = name != null ? Filenames.removeExtension(name) : "";
		final String xhtmlDocumentString = XHTML_TEMPLATE.apply(title, htmlBodyContent);
		final DocumentBuilder documentBuilder = context.newPageDocumentBuilder();
		try {
			return documentBuilder.parse(new ByteArrayInputStream(xhtmlDocumentString.getBytes(UTF_8)));
		} catch(final SAXException saxException) {
			throw new IOException(saxException);
		}
	}
}
