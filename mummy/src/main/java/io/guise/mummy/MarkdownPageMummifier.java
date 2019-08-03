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
import java.util.*;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.globalmentor.io.*;
import com.globalmentor.text.StringTemplate;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.emoji.EmojiImageType;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.superscript.SuperscriptExtension;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

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

	private final Parser parser;

	/**
	 * Returns the parser for parsing Markdown.
	 * @apiNote The Flexmark parser API indicates it can be used by multiple threads.
	 * @return The parser for parsing Markdown.
	 */
	protected Parser getParser() {
		return parser;
	}

	private final HtmlRenderer htmlRenderer;

	/** @return The formatter that serializes a Markdown tree as HTML body content. */
	protected HtmlRenderer getHtmlRenderer() {
		return htmlRenderer;
	}

	/** Constructor. */
	public MarkdownPageMummifier() {
		final MutableDataHolder parserOptions = new MutableDataSet()
				//emoji; see https://www.webfx.com/tools/emoji-cheat-sheet/
				.set(EmojiExtension.USE_IMAGE_TYPE, EmojiImageType.UNICODE_ONLY)
				//GFM tables
				.set(TablesExtension.COLUMN_SPANS, false).set(TablesExtension.APPEND_MISSING_COLUMNS, true).set(TablesExtension.DISCARD_EXTRA_COLUMNS, true)
				.set(TablesExtension.HEADER_SEPARATOR_COLUMN_MATCH, true)
				//extensions
				.set(Parser.EXTENSIONS, List.of(DefinitionExtension.create(), EmojiExtension.create(), SuperscriptExtension.create(), TablesExtension.create(),
						TypographicExtension.create(), YamlFrontMatterExtension.create()));
		parser = Parser.builder(parserOptions).build();
		htmlRenderer = HtmlRenderer.builder().build();
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
		String title = name != null ? Filenames.removeExtension(name) : ""; //default to a title of the filename with no extension
		com.vladsch.flexmark.util.ast.Document markdownDocument = getParser().parse(stringBuilder.toString());
		final AbstractYamlFrontMatterVisitor yamlVisitor = new AbstractYamlFrontMatterVisitor();
		yamlVisitor.visit(markdownDocument);
		final List<String> frontMatterTitles = yamlVisitor.getData().get("title"); //TODO use a constant from some URF list of standard properties
		if(frontMatterTitles != null && !frontMatterTitles.isEmpty()) {
			final String frontMatterTitle = frontMatterTitles.iterator().next();
			if(frontMatterTitle != null) {
				title = frontMatterTitle;
			}
		}
		final String htmlBodyContent = getHtmlRenderer().render(markdownDocument);
		final String xhtmlDocumentString = XHTML_TEMPLATE.apply(title, htmlBodyContent);
		final DocumentBuilder documentBuilder = context.newPageDocumentBuilder();
		try {
			return documentBuilder.parse(new ByteArrayInputStream(xhtmlDocumentString.getBytes(UTF_8)));
		} catch(final SAXException saxException) {
			throw new IOException(saxException);
		}
	}
}
