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

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static com.globalmentor.html.HtmlDom.*;
import static com.globalmentor.html.def.HTML.*;
import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.java.OperatingSystem.*;
import static com.globalmentor.xml.XmlDom.*;
import static io.guise.mummy.mummify.page.MarkdownPageMummifier.*;
import static java.nio.charset.StandardCharsets.*;
import static java.util.stream.Collectors.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.*;
import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;

import javax.annotation.*;

import org.junit.jupiter.api.*;
import org.w3c.dom.*;

import io.confound.config.Configuration;
import io.guise.mummy.*;
import io.urf.URF.Handle;

/**
 * Tests of {@link MarkdownPageMummifier}.
 * @author Garret Wilson
 */
public class MarkdownPageMummifierTest {

	public static final String CODE_JAVASCRIPT_MARKDOWN_RESOURCE_NAME = "code-javascript.md";
	public static final String SIMPLE_MARKDOWN_RESOURCE_NAME = "simple.md";
	public static final String SIMPLE_TITLE_MARKDOWN_RESOURCE_NAME = "simple-title.md";
	public static final String SIMPLE_METADATA_MARKDOWN_RESOURCE_NAME = "simple-metadata.md";

	private MummyContext mummyContext;

	@BeforeEach
	protected void setupContext() {
		final GuiseProject project = new DefaultGuiseProject(getWorkingDirectory(), Configuration.empty());
		mummyContext = new FakeMummyContext(project);
	}

	/**
	 * Retrieves the text content of the first paragraph in the given document.
	 * @param document The document from which to retrieve paragraph text.
	 * @return The text of the first paragraph, which will not be present if there is no body element or paragraph element.
	 */
	protected Optional<String> findFirstParagraphText(@Nonnull final Document document) {
		return findHtmlBodyElement(document).flatMap(bodyElement -> childElementsByNameNS(bodyElement, XHTML_NAMESPACE_URI_STRING, ELEMENT_P).findFirst())
				.map(Element::getTextContent);
	}

	/** @see MarkdownPageMummifier#MARKDOWN_WITH_YAML_PATTERN */
	@Test
	public void testMarkdownWithYamlPatternNoYaml() {
		final Matcher matcher = MARKDOWN_WITH_YAML_PATTERN.matcher("# Heading\n\nBody text.");
		assertThat(matcher.matches(), is(true));
		assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP), is(nullValue()));
		assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP), is("# Heading\n\nBody text."));
	}

	/** @see MarkdownPageMummifier#MARKDOWN_WITH_YAML_PATTERN */
	@Test
	public void testMarkdownWithYamlPatternEmptyYaml() {
		final Matcher matcher = MARKDOWN_WITH_YAML_PATTERN.matcher("---\n---\n# Heading\n\nBody text.");
		assertThat(matcher.matches(), is(true));
		assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP), is(""));
		assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP), is("# Heading\n\nBody text."));
	}

	/** @see MarkdownPageMummifier#MARKDOWN_WITH_YAML_PATTERN */
	@Test
	public void testMarkdownWithYamlPatternSingleLineYamlNotRecognized() {
		final Matcher matcher = MARKDOWN_WITH_YAML_PATTERN.matcher("---foo:bar---\n# Heading\n\nBody text.");
		assertThat(matcher.matches(), is(true));
		assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP), is(nullValue()));
		assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP), is("---foo:bar---\n# Heading\n\nBody text."));
	}

	/** @see MarkdownPageMummifier#MARKDOWN_WITH_YAML_PATTERN */
	@Test
	public void testMarkdownWithYamlPattern() {
		final Matcher matcher = MARKDOWN_WITH_YAML_PATTERN.matcher("---\nfoo:bar\nexample:test---\n# Heading\n\nBody text.");
		assertThat(matcher.matches(), is(true));
		assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP), is("foo:bar\nexample:test"));
		assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP), is("# Heading\n\nBody text."));
	}

	/** @see MarkdownPageMummifier#MARKDOWN_WITH_YAML_PATTERN */
	@Test
	public void testMarkdownWithYamlPatternNoMarkdown() {
		final Matcher matcher = MARKDOWN_WITH_YAML_PATTERN.matcher("---\nfoo:bar\nexample:test---");
		assertThat(matcher.matches(), is(true));
		assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP), is("foo:bar\nexample:test"));
		assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP), is(""));
	}

	/** @see MarkdownPageMummifier#MARKDOWN_WITH_YAML_PATTERN */
	@Test
	public void testMarkdownWithYamlPatternEmptyLineMarkdown() {
		final Matcher matcher = MARKDOWN_WITH_YAML_PATTERN.matcher("---\nfoo:bar\nexample:test---\n");
		assertThat(matcher.matches(), is(true));
		assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP), is("foo:bar\nexample:test"));
		assertThat(matcher.group(MARKDOWN_WITH_YAML_PATTERN_MARKDOWN_GROUP), is(""));
	}

	/**
	 * Asserts that the body of the given document matches that expected for the "simple-" test files.
	 * @param document The document to test.
	 */
	protected void assertSimpleBody(@Nonnull final Document document) {
		final Node body = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final List<Element> bodyElements = getChildElements(body);
		assertThat(bodyElements, hasSize(2));
		final Element h1 = bodyElements.get(0);
		assertThat(h1.getLocalName(), is(ELEMENT_H1));
		assertThat(h1.getNamespaceURI(), is(XHTML_NAMESPACE_URI_STRING));
		assertThat(h1.getTextContent(), is("Heading"));
		final Element p = bodyElements.get(1);
		assertThat(p.getLocalName(), is(ELEMENT_P));
		assertThat(p.getNamespaceURI(), is(XHTML_NAMESPACE_URI_STRING));
		assertThat(p.getTextContent(), is("Body text."));
	}

	/**
	 * @see MarkdownPageMummifier#loadSourceDocument(MummyContext, InputStream)
	 * @see #CODE_JAVASCRIPT_MARKDOWN_RESOURCE_NAME
	 */
	@Test
	public void testCodeMarkdownLanguage() throws IOException {
		final MarkdownPageMummifier mummifier = new MarkdownPageMummifier();
		final Document document;
		try (final InputStream inputStream = getClass().getResourceAsStream(CODE_JAVASCRIPT_MARKDOWN_RESOURCE_NAME)) {
			document = mummifier.loadSourceDocument(mummyContext, inputStream, CODE_JAVASCRIPT_MARKDOWN_RESOURCE_NAME);
		}
		final Node body = findHtmlBodyElement(document).orElseThrow(AssertionError::new);
		final List<Element> bodyElements = getChildElements(body);
		assertThat(bodyElements, hasSize(1));
		final Element pre = bodyElements.get(0);
		assertThat(pre.getLocalName(), is(ELEMENT_PRE));
		assertThat(pre.getNamespaceURI(), is(XHTML_NAMESPACE_URI_STRING));
		final List<Element> preElements = getChildElements(pre);
		assertThat(preElements, hasSize(1));
		final Element code = preElements.get(0);
		assertThat(code.getLocalName(), is(ELEMENT_CODE));
		assertThat(code.getNamespaceURI(), is(XHTML_NAMESPACE_URI_STRING));
		assertThat(code.getAttributeNS(null, ATTRIBUTE_CLASS), is("language-javascript"));
	}

	/**
	 * @see MarkdownPageMummifier#loadSourceDocument(MummyContext, InputStream)
	 * @see #SIMPLE_MARKDOWN_RESOURCE_NAME
	 * @see #assertSimpleBody(Document)
	 */
	@Test
	public void testSimpleMarkdown() throws IOException {
		final MarkdownPageMummifier mummifier = new MarkdownPageMummifier();
		final Document document;
		try (final InputStream inputStream = getClass().getResourceAsStream(SIMPLE_MARKDOWN_RESOURCE_NAME)) {
			document = mummifier.loadSourceDocument(mummyContext, inputStream, SIMPLE_MARKDOWN_RESOURCE_NAME);
		}
		assertThat(findTitle(document), isPresentAndIs("simple"));
		assertSimpleBody(document);
	}

	/**
	 * @see MarkdownPageMummifier#loadSourceDocument(MummyContext, InputStream)
	 * @see #SIMPLE_TITLE_MARKDOWN_RESOURCE_NAME
	 * @see #assertSimpleBody(Document)
	 */
	@Test
	public void testSimpleMarkdownDocumentTitle() throws IOException {
		final MarkdownPageMummifier mummifier = new MarkdownPageMummifier();
		final Document document;
		try (final InputStream inputStream = getClass().getResourceAsStream(SIMPLE_TITLE_MARKDOWN_RESOURCE_NAME)) {
			document = mummifier.loadSourceDocument(mummyContext, inputStream, SIMPLE_TITLE_MARKDOWN_RESOURCE_NAME);
		}
		assertThat(findTitle(document), isPresentAndIs(removeExtension(SIMPLE_TITLE_MARKDOWN_RESOURCE_NAME)));
		assertSimpleBody(document);
	}

	/**
	 * @see MarkdownPageMummifier#loadSourceDocument(MummyContext, InputStream)
	 * @see #SIMPLE_METADATA_MARKDOWN_RESOURCE_NAME
	 * @see #assertSimpleBody(Document)
	 */
	@Test
	public void testSimpleMarkdownDocumentMetadata() throws IOException {
		final MarkdownPageMummifier mummifier = new MarkdownPageMummifier();
		final Document document;
		try (final InputStream inputStream = getClass().getResourceAsStream(SIMPLE_METADATA_MARKDOWN_RESOURCE_NAME)) {
			document = mummifier.loadSourceDocument(mummyContext, inputStream, SIMPLE_METADATA_MARKDOWN_RESOURCE_NAME);
		}
		assertThat(findTitle(document), isPresentAndIs(removeExtension(SIMPLE_METADATA_MARKDOWN_RESOURCE_NAME)));
		//we no longer include metadata when loading the source XHTML document; it is loaded separately as part of the description
		assertThat(htmlHeadMetaElements(document).collect(toList()), empty());
		assertSimpleBody(document);
	}

	/**
	 * @see MarkdownPageMummifier#loadSourceMetadata(MummyContext, InputStream, String)
	 * @see #SIMPLE_METADATA_MARKDOWN_RESOURCE_NAME
	 */
	@Test
	public void testSimpleMarkdownMetadata() throws IOException {
		final MarkdownPageMummifier mummifier = new MarkdownPageMummifier();
		try (final InputStream inputStream = getClass().getResourceAsStream(SIMPLE_METADATA_MARKDOWN_RESOURCE_NAME)) {
			assertThat(mummifier.loadSourceMetadata(mummyContext, inputStream, SIMPLE_METADATA_MARKDOWN_RESOURCE_NAME),
					containsInAnyOrder(Map.entry(Handle.toTag("title"), "Simple Page with Other Metadata"), Map.entry(Handle.toTag("label"), "Simplicity"),
							Map.entry(Handle.toTag("fooBar"), "This is a test."),
							//string property inferred to be a local date from its name pattern
							Map.entry(Handle.toTag(Artifact.PROPERTY_HANDLE_PUBLISHED_ON), LocalDate.of(2001, 2, 3)),
							//Guise Mummy namespace with integer value
							Map.entry(Artifact.PROPERTY_TAG_MUMMY_ORDER, 3),
							//Open Graph namespace
							Map.entry(URI.create("http://ogp.me/ns#type"), "website")));
		}
	}

	/**
	 * Tests whether typographic conversions are appropriately being made, such as converting "---" to em dash "—".
	 * @implSpec Currently typographic conversions are disabled altogether because the Flexmark Typographic Extension seems to be dropping characters altogether
	 *           (e.g. the apostrophe). These tests will be updated and expanded if the Typographic Extension or something like it is re-enabled, but for now it
	 *           verifies that certain characters previously modified by the TYpographic Extension are not being dropped.
	 * @see MarkdownPageMummifier#loadSourceDocument(MummyContext, InputStream)
	 */
	@Test
	public void testTypographicConversions() throws IOException {
		final String markdown = "it's working --- or not";
		final MarkdownPageMummifier mummifier = new MarkdownPageMummifier();
		final Document document;
		try (final InputStream inputStream = new ByteArrayInputStream(markdown.getBytes(UTF_8))) {
			document = mummifier.loadSourceDocument(mummyContext, inputStream, "test");
		}
		assertThat(findFirstParagraphText(document), isPresentAndIs("it's working --- or not"));
	}

}
