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

import static com.github.npathai.hamcrestopt.OptionalMatchers.*;
import static com.globalmentor.html.HtmlDom.*;
import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.xml.XmlDom.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

import org.junit.jupiter.api.*;
import org.w3c.dom.*;

import io.confound.config.Configuration;

/**
 * Tests of {@link MarkdownPageMummifier}.
 * @author Garret Wilson
 */
public class MarkdownPageMummifierTest {

	public static final String SIMPLE_MARKDOWN_RESOURCE_NAME = "simple.md";
	public static final String SIMPLE_TITLE_MARKDOWN_RESOURCE_NAME = "simple-title.md";

	private MummyContext mummyContext;

	@BeforeEach
	protected void setupContext() {
		mummyContext = new BaseMummyContext() { //TODO move to separate mock context

			@Override
			public Path getSiteTargetDirectory() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Path getSiteSourceDirectory() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Path getSiteDescriptionTargetDirectory() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Configuration getSiteConfiguration() {
				return Configuration.empty();
			}

			@Override
			public SourcePathMummifier getDefaultSourceFileMummifier() {
				throw new UnsupportedOperationException();
			}

			@Override
			public SourcePathMummifier getDefaultSourceDirectoryMummifier() {
				throw new UnsupportedOperationException();
			}

			@Override
			public Optional<SourcePathMummifier> findRegisteredMummifierForSourceFile(Path sourceFile) {
				return Optional.empty();
			}

			@Override
			public Optional<SourcePathMummifier> findRegisteredMummifierForSourceDirectory(Path sourceDirectory) {
				return Optional.empty();
			}
		};
	}

	/**
	 * @see MarkdownPageMummifier#loadSourceDocument(MummyContext, InputStream)
	 * @see #SIMPLE_MARKDOWN_RESOURCE_NAME
	 */
	@Test
	public void testSimpleMarkdown() throws IOException {
		final MarkdownPageMummifier mummifier = new MarkdownPageMummifier();
		final Document document;
		try (final InputStream inputStream = getClass().getResourceAsStream(SIMPLE_MARKDOWN_RESOURCE_NAME)) {
			document = mummifier.loadSourceDocument(mummyContext, inputStream, SIMPLE_MARKDOWN_RESOURCE_NAME);
		}
		assertThat(findTitle(document), isPresentAndIs("simple"));
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
	 * @see #SIMPLE_TITLE_MARKDOWN_RESOURCE_NAME
	 */
	@Test
	public void testSimpleTitleMarkdown() throws IOException {
		final MarkdownPageMummifier mummifier = new MarkdownPageMummifier();
		final Document document;
		try (final InputStream inputStream = getClass().getResourceAsStream(SIMPLE_TITLE_MARKDOWN_RESOURCE_NAME)) {
			document = mummifier.loadSourceDocument(mummyContext, inputStream, SIMPLE_TITLE_MARKDOWN_RESOURCE_NAME);
		}
		assertThat(findTitle(document), isPresentAndIs("Simple Page"));
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

}
