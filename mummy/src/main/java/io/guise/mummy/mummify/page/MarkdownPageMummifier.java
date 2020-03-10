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

import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.io.InputStreams.*;
import static com.globalmentor.io.Readers.*;
import static com.globalmentor.java.Conditions.*;
import static java.nio.charset.StandardCharsets.*;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.regex.*;

import javax.xml.parsers.*;

import org.snakeyaml.engine.v2.api.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.globalmentor.io.*;
import com.globalmentor.text.StringTemplate;
import com.globalmentor.vocab.Curie;
import com.globalmentor.vocab.VocabularyTerm;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.emoji.EmojiImageType;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.superscript.SuperscriptExtension;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;

import io.guise.mummy.MummyContext;

/**
 * Mummifier for Markdown documents.
 * @implSpec This implementation defaults to UTF-8, producing an error if the encoding isn't valid. Other encodings are supported with the appropriate BOM.
 * @author Garret Wilson
 */
public class MarkdownPageMummifier extends AbstractPageMummifier {

	/** Pattern for a Markdown document with YAML front matter. */
	static final Pattern MARKDOWN_WITH_YAML_PATTERN = Pattern.compile("(?:---[\\r\\n]+(.*)---(?:[\\r\\n]+|$))?(.*)", Pattern.DOTALL);
	/** The matching group for YAML content in a Markdown document with YAML. The value of the group will be <code>null</code> if no YAML is present. */
	static final int MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP = 1;
	/**
	 * The matching group for Markdown content in a Markdown document with YAML. The value of the group may be the empty string but will never be
	 * <code>null</code>.
	 */
	static final int MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP = 2;

	/**
	 * The template for wrapping an XHTML document around the generated HTML. It has the following parameters:
	 * <ol>
	 * <li>Page {@code <title>} content.</li>
	 * <li>Page {@code <body>} content.</li>
	 * </ol>
	 */
	private static final StringTemplate XHTML_TEMPLATE = StringTemplate
			.builder() //@formatter:off
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
	 * @implSpec This version uses the filename as a title. It will be replaced later by any title indicated in the metadata during mummification.
	 */
	@Override
	public Document loadSourceDocument(final MummyContext context, final InputStream inputStream, final String name) throws IOException, DOMException {
		final String content = readString(new BOMInputStreamReader(toMarkSupportedInputStream(inputStream))); //detect the BOM and to throw errors if the encoding is invalid
		final Matcher matcher = MARKDOWN_WITH_YAML_PATTERN.matcher(content);
		if(!matcher.matches()) {
			throw new IOException(String.format("Invalid markdown in `%s`.", name));
		}

		//parse Markdown
		final String markdown = matcher.group(MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP);
		com.vladsch.flexmark.util.ast.Document markdownDocument = getParser().parse(markdown);

		//generate XHTML
		final Document document;
		final String htmlBodyContent = getHtmlRenderer().render(markdownDocument);
		final String xhtmlDocumentString = XHTML_TEMPLATE.apply(removeExtension(name), htmlBodyContent);
		final DocumentBuilder documentBuilder = context.newPageDocumentBuilder();
		try {
			document = documentBuilder.parse(new ByteArrayInputStream(xhtmlDocumentString.getBytes(UTF_8)));
		} catch(final SAXException saxException) { //we don't expect this error, so checking for the locations using SAXParseException isn't that useful
			throw new IOException(String.format("Error parsing generated XHTML for `%s`: %s.", name, saxException.getLocalizedMessage()), saxException); //TODO i18n
		}

		return document;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation reads the entire document and then parses any YAML front matter using SnakeYAML Engine.
	 * @implSpec Only YAML mappings (name-value pairs) are supported. Names should be in <code>camelCase</code>. Namespace prefixes of predefined vocabularies in
	 *           {@link AbstractPageMummifier#PREDEFINED_VOCABULARIES} are supported for names in the form <code>eg:name</code>.
	 * @see AbstractPageMummifier#PREDEFINED_VOCABULARIES
	 */
	@Override
	protected List<Map.Entry<URI, Object>> loadSourceMetadata(final MummyContext context, final InputStream inputStream, final String name) throws IOException {
		final String content = readString(new BOMInputStreamReader(toMarkSupportedInputStream(inputStream))); //detect the BOM and to throw errors if the encoding is invalid
		final Matcher matcher = MARKDOWN_WITH_YAML_PATTERN.matcher(content);
		if(!matcher.matches()) {
			throw new IOException(String.format("Invalid markdown in `%s`.", name));
		}
		final String yaml = matcher.group(MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP);
		if(yaml == null) { //no YAML front matter present
			return emptyList();
		}
		final Object object = new Load(LoadSettings.builder().build()).loadFromString(yaml);
		if(!(object instanceof Map)) {
			return emptyList();
		}
		final Map<?, ?> yamlMap = (Map<?, ?>)object;
		try {
			return yamlMap.entrySet().stream().map(mapping -> {
				final Object key = mapping.getKey();
				final Object value = mapping.getValue();
				checkArgument(key instanceof CharSequence, "YAML mapping key `%s` of type `%s` not supported.", key, key.getClass().getName());
				checkArgument(key != null, "YAML key `%s` cannot be mapped to `null`.", key);
				final Curie curie = Curie.parse((CharSequence)mapping.getKey(), false);
				final URI tag = PREDEFINED_VOCABULARIES.findVocabularyTerm(curie).map(VocabularyTerm::toURI)
						.orElseThrow(() -> new IllegalArgumentException(String.format("YAML key `%s` uses unrecognized namespace prefix.", key)));
				return Map.entry(tag, value);
			}).collect(toList());
		} catch(final IllegalArgumentException illegalArgumentException) {
			throw new IOException(String.format("Invalid YAML in `%s`: %s", name, illegalArgumentException.getLocalizedMessage()), illegalArgumentException); //TODO i18n
		}
	}

}
