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

import static com.globalmentor.html.HtmlDom.*;
import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.lex.CompoundTokenization.*;
import static com.globalmentor.xml.XmlDom.*;
import static io.guise.mummy.Artifact.*;
import static java.nio.charset.StandardCharsets.*;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
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

import io.urf.URF.Handle;

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

		//TODO delegate to sourceMetadata() to retrieve metadata; forgo storing metadata in the XHTML document; manually add metadata to target document 

		String title = name != null ? Filenames.removeExtension(name) : ""; //default to a title of the filename with no extension
		com.vladsch.flexmark.util.ast.Document markdownDocument = getParser().parse(stringBuilder.toString());
		final AbstractYamlFrontMatterVisitor yamlVisitor = new AbstractYamlFrontMatterVisitor();
		yamlVisitor.visit(markdownDocument);

		//find the title if present in the front matter
		final List<String> frontMatterTitles = yamlVisitor.getData().get(PROPERTY_HANDLE_TITLE);
		if(frontMatterTitles != null && !frontMatterTitles.isEmpty()) {
			final Iterator<String> frontMatterTitlesIterator = frontMatterTitles.iterator();
			final String frontMatterTitle = frontMatterTitlesIterator.next(); //use the first title given
			if(frontMatterTitle != null) {
				title = frontMatterTitle;
			}
			while(frontMatterTitlesIterator.hasNext()) {
				getLogger().warn("Ignoring additional title {} in source Markdown document {}.", frontMatterTitlesIterator.next(), name);
			}
		}

		//generate XHTML
		final Document document;
		final String htmlBodyContent = getHtmlRenderer().render(markdownDocument);
		final String xhtmlDocumentString = XHTML_TEMPLATE.apply(title, htmlBodyContent);
		final DocumentBuilder documentBuilder = context.newPageDocumentBuilder();
		try {
			document = documentBuilder.parse(new ByteArrayInputStream(xhtmlDocumentString.getBytes(UTF_8)));
		} catch(final SAXException saxException) {
			throw new IOException(saxException);
		}

		//add additional metadata from the front matter
		final Element headElement = findHtmlHeadElement(document).orElseThrow(IllegalStateException::new); //our template has a <head>
		yamlVisitor.getData().forEach((metaName, metaValues) -> {
			if(PROPERTY_HANDLE_TITLE.equals(metaName)) { //ignore the title; we already processed it
				return;
			}
			//multiple front matter values for a name result in multiple <meta> elements as a side effect which may be useful
			metaValues.forEach(metaValue -> { //TODO add an HtmlDom.addNamedMetata() method
				final Element metaElement = addLast(headElement, document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_META));
				metaElement.setAttributeNS(null, ELEMENT_META_ATTRIBUTE_NAME, CAMEL_CASE.toKebabCase(metaName));
				metaElement.setAttributeNS(null, ELEMENT_META_ATTRIBUTE_CONTENT, metaValue);
			});
		});

		return document;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation loads the source Markdown document and then delegates to
	 *           {@link #sourceMetadata(MummyContext, com.vladsch.flexmark.util.ast.Document)} to extract the metadata.
	 */
	@Override
	protected Stream<Entry<URI, String>> sourceMetadata(final MummyContext context, final InputStream inputStream, final String name) throws IOException {
		//create a Reader to detect the BOM and to throw errors if the encoding is invalid
		@SuppressWarnings("resource") //we don't manage the underlying input stream
		final BOMInputStreamReader bomInputStreamReader = new BOMInputStreamReader(new BufferedInputStream(inputStream)); //TODO create utility to ensure mark supported
		final StringBuilder stringBuilder = new StringBuilder();
		final char[] buffer = new char[64 * 1024];
		int charsRead;
		while((charsRead = bomInputStreamReader.read(buffer)) != -1) { //TODO create utility to read all from a reader
			stringBuilder.append(buffer, 0, charsRead);
		}
		com.vladsch.flexmark.util.ast.Document markdownDocument = getParser().parse(stringBuilder.toString());
		return sourceMetadata(context, markdownDocument);
	}

	//TODO create map of predefined namespaces, eventually in RDFa class but for the short term in the base class

	/**
	 * Extracts metadata stored in the source document itself.
	 * @implSpec The XHTML document {@code <head><title>} will be returned as metadata, using {@value Artifact#PROPERTY_HANDLE_TITLE} as a handle; followed by
	 *           values in any {@code <head><meta>} elements.
	 * @param context The context of static site generation.
	 * @param sourceDocument The source XHTML document being mummified, from which metadata should be extracted.
	 * @return Metadata stored in the source document being mummified, consisting of resolved URI tag names and values. The name-value pairs may have duplicate
	 *         names.
	 * @throws DOMException if there is a problem retrieving metadata.
	 */
	protected Stream<Map.Entry<URI, String>> sourceMetadata(@Nonnull MummyContext context, @Nonnull final com.vladsch.flexmark.util.ast.Document sourceDocument)
			throws DOMException {
		final AbstractYamlFrontMatterVisitor yamlVisitor = new AbstractYamlFrontMatterVisitor();
		yamlVisitor.visit(sourceDocument);
		return yamlVisitor.getData().entrySet().stream().flatMap(frontDataEntry -> {
			final String name = frontDataEntry.getKey();
			return frontDataEntry.getValue().stream().map(value -> {
				final int prefixDelimiterIndex = name.indexOf(':'); //TODO use constant
				if(prefixDelimiterIndex >= 0) { //TODO create single character string splitting utility method
					throw new UnsupportedOperationException(); //TODO implement with known namespaces
				} else {
					return Map.entry(Handle.toTag(name), value);
				}
			});
		});
	}

}
