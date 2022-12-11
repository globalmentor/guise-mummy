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

import static com.globalmentor.html.HtmlDom.*;
import static com.globalmentor.html.def.HTML.*;
import static com.globalmentor.xml.XmlDom.*;
import static java.util.stream.Collectors.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import org.junit.jupiter.api.*;
import org.w3c.dom.*;

import com.globalmentor.html.def.HTML;
import com.globalmentor.xml.def.NsName;

/**
 * Tests of {@link AbstractPageMummifier}.
 * @author Garret Wilson
 */
public class AbstractPageMummifierTest {

	//names for identifying documents as link sources
	private static final String FROM_TEMPLATE = "template";
	private static final String FROM_SOURCE_DOCUMENT = "source-document";

	@Test
	public void testMergeHeadLinks() {
		//no links
		assertThat(mergeHeadLinks(Stream.of(), Stream.of()), empty());
		//one link
		assertThat(mergeHeadLinks(Stream.of(), Stream.of("bar")), contains(Map.entry("bar", FROM_SOURCE_DOCUMENT)));
		assertThat(mergeHeadLinks(Stream.of("foo"), Stream.of()), contains(Map.entry("foo", FROM_TEMPLATE)));
		assertThat(mergeHeadLinks(Stream.of("foo"), Stream.of("bar")), contains(Map.entry("foo", FROM_TEMPLATE), Map.entry("bar", FROM_SOURCE_DOCUMENT)));
		//three links each
		assertThat(mergeHeadLinks(Stream.of("a", "b", "c"), Stream.of("x", "y", "z")), contains(Map.entry("a", FROM_TEMPLATE), Map.entry("b", FROM_TEMPLATE),
				Map.entry("c", FROM_TEMPLATE), Map.entry("x", FROM_SOURCE_DOCUMENT), Map.entry("y", FROM_SOURCE_DOCUMENT), Map.entry("z", FROM_SOURCE_DOCUMENT)));
		//duplicate template links
		assertThat(mergeHeadLinks(Stream.of("a", "a", "b", "c"), Stream.of("x", "y", "z")), contains(Map.entry("a", FROM_TEMPLATE), Map.entry("b", FROM_TEMPLATE),
				Map.entry("c", FROM_TEMPLATE), Map.entry("x", FROM_SOURCE_DOCUMENT), Map.entry("y", FROM_SOURCE_DOCUMENT), Map.entry("z", FROM_SOURCE_DOCUMENT)));
		assertThat(mergeHeadLinks(Stream.of("a", "b", "c", "b"), Stream.of("x", "y", "z")), contains(Map.entry("a", FROM_TEMPLATE), Map.entry("b", FROM_TEMPLATE),
				Map.entry("c", FROM_TEMPLATE), Map.entry("x", FROM_SOURCE_DOCUMENT), Map.entry("y", FROM_SOURCE_DOCUMENT), Map.entry("z", FROM_SOURCE_DOCUMENT)));
		//duplicate source document links
		/*TODO improve handling of logging in tests
		assertThat(mergeHeadLinks(Stream.of("a", "b", "c"), Stream.of("x", "y", "x", "z")), contains(Map.entry("a", FROM_TEMPLATE), Map.entry("b", FROM_TEMPLATE),
				Map.entry("c", FROM_TEMPLATE), Map.entry("x", FROM_SOURCE_DOCUMENT), Map.entry("y", FROM_SOURCE_DOCUMENT), Map.entry("z", FROM_SOURCE_DOCUMENT)));
		assertThat(mergeHeadLinks(Stream.of("a", "b", "c"), Stream.of("x", "y", "z", "z")), contains(Map.entry("a", FROM_TEMPLATE), Map.entry("b", FROM_TEMPLATE),
				Map.entry("c", FROM_TEMPLATE), Map.entry("x", FROM_SOURCE_DOCUMENT), Map.entry("y", FROM_SOURCE_DOCUMENT), Map.entry("z", FROM_SOURCE_DOCUMENT)));
		*/
		//merging
		assertThat(mergeHeadLinks(Stream.of("a", "b", "c"), Stream.of("c", "y", "z")), contains(Map.entry("a", FROM_TEMPLATE), Map.entry("b", FROM_TEMPLATE),
				Map.entry("c", FROM_SOURCE_DOCUMENT), Map.entry("y", FROM_SOURCE_DOCUMENT), Map.entry("z", FROM_SOURCE_DOCUMENT)));
		assertThat(mergeHeadLinks(Stream.of("a", "b", "c"), Stream.of("x", "y", "b")), contains(Map.entry("a", FROM_TEMPLATE), Map.entry("x", FROM_SOURCE_DOCUMENT),
				Map.entry("y", FROM_SOURCE_DOCUMENT), Map.entry("b", FROM_SOURCE_DOCUMENT), Map.entry("c", FROM_TEMPLATE)));
		assertThat(mergeHeadLinks(Stream.of("a", "b", "c"), Stream.of("x", "b", "z")), contains(Map.entry("a", FROM_TEMPLATE), Map.entry("x", FROM_SOURCE_DOCUMENT),
				Map.entry("b", FROM_SOURCE_DOCUMENT), Map.entry("c", FROM_TEMPLATE), Map.entry("z", FROM_SOURCE_DOCUMENT)));
		assertThat(mergeHeadLinks(Stream.of("a", "b", "c"), Stream.of("x", "b", "a", "y")), contains(Map.entry("x", FROM_SOURCE_DOCUMENT),
				Map.entry("b", FROM_SOURCE_DOCUMENT), Map.entry("a", FROM_SOURCE_DOCUMENT), Map.entry("c", FROM_TEMPLATE), Map.entry("y", FROM_SOURCE_DOCUMENT)));
	}

	/** A convenience encapsulation of the LINK element namespace and local name. */
	private static final NsName LINK_ELEMENT = NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_LINK);

	/**
	 * Tests merging links by creating fake HTML documents with the given links, merging the links, and then gathering and returning the merged links for
	 * verification.
	 * @param templateLinks The links to use in the template document, in order.
	 * @param sourceDocumentLinks The links to use in the source document, in order.
	 * @return The resulting links after merging, associated with the name of the document they are from.
	 * @see AbstractPageMummifier#mergeHeadLinks(Document, Document)
	 */
	protected List<Map.Entry<String, String>> mergeHeadLinks(@Nonnull final Stream<String> templateLinks, @Nonnull final Stream<String> sourceDocumentLinks) {
		final Document templateDocument = createLinksDocument(FROM_TEMPLATE, templateLinks);
		final Document sourceDocument = createLinksDocument(FROM_SOURCE_DOCUMENT, sourceDocumentLinks);
		final AbstractPageMummifier mummifier = mock(AbstractPageMummifier.class, CALLS_REAL_METHODS);
		mummifier.mergeHeadLinks(templateDocument, sourceDocument);
		final Element templateHeadElement = findHtmlHeadElement(templateDocument).orElseThrow(IllegalStateException::new);
		return childElementsOf(templateHeadElement).filter(LINK_ELEMENT::matches)
				.map(linkElement -> Map.entry(linkElement.getAttributeNS(null, ELEMENT_LINK_ATTRIBUTE_HREF), linkElement.getAttributeNS(null, ATTRIBUTE_NAME)))
				.collect(toList());
	}

	/**
	 * Creates a fake HTML document with the given links. The given name will be used as the document title and also as each link {@value HTML#ATTRIBUTE_NAME}
	 * attribute value.
	 * @apiNote Adding a supplied value as the {@value HTML#ATTRIBUTE_NAME} attribute allows verifying to see the source of each result link after merging.
	 * @param name Some name to identify the document.
	 * @param links The links to use in the document, in order.
	 * @return A fake HTML document with the links.
	 * @see #LINK_ELEMENT
	 */
	protected Document createLinksDocument(@Nonnull final String name, @Nonnull final Stream<String> links) {
		final Document document = createXHTMLDocument(name);
		final Element headElement = findHtmlHeadElement(document).orElseThrow(IllegalStateException::new);
		links.map(link -> {
			final Element linkElement = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_LINK);
			linkElement.setAttributeNS(null, ELEMENT_LINK_ATTRIBUTE_HREF, link);
			linkElement.setAttributeNS(null, ATTRIBUTE_NAME, name);
			return linkElement;
		}).forEach(headElement::appendChild);
		return document;
	}

}
