/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy.mummify.page.widget.directory;

import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.xml.XmlDom.*;
import static io.guise.mummy.Artifact.PROPERTY_HANDLE_PUBLISHED_ON;
import static java.util.Comparator.*;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.*;
import java.util.*;
import java.util.stream.Stream;

import org.w3c.dom.*;

import com.globalmentor.xml.spec.NsName;

import io.guise.mummy.Artifact;
import io.guise.mummy.GuiseMummy;
import io.guise.mummy.MummyContext;
import io.guise.mummy.mummify.page.PageMummifier;
import io.guise.mummy.mummify.page.PostArtifact;
import io.guise.mummy.mummify.page.widget.Widget;

/**
 * A widget representing a "directory" or "index" of artifacts.
 * @apiNote This widget would typically be found in the content historically found in an <code>index.html</code> file. Rather than providing a reverse lookup
 *          based on content as a book index normally does, instead this widget provides a listing of artifacts by name or title, as the directory of a building
 *          might show or as a console <code>dir</code> DOS command might display.
 * @author Garret Wilson
 */
public class DirectoryWidget implements Widget {

	/** The widget element name. */
	private static final NsName WIDGET_ELEMENT = NsName.of(GuiseMummy.NAMESPACE_STRING, "Directory");

	/** The optional attribute indicating the label for the "more" link. */
	private static final NsName ATTRIBUTE_MORE_LABEL = NsName.of("moreLabel");

	/** The formatter for producing the published on date string. */
	private static final DateTimeFormatter PUBLISHED_ON_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL); //i18n; update to allow project-based locale from mummification context; probably request formatter from mummification context

	@Override
	public NsName getWidgetElementName() {
		return WIDGET_ELEMENT;
	}

	@Override
	public List<Element> processElement(final PageMummifier mummifier, final MummyContext context, final Artifact contextArtifact, final Artifact artifact,
			final Element widgetElement) throws IOException, DOMException {
		final Document document = widgetElement.getOwnerDocument();
		return mummifier.childNavigationArtifacts(context, contextArtifact)
				//only include posts
				.flatMap(asInstances(PostArtifact.class))
				//sort the posts in reverse order of published-on date (with undated posts last, although there are not expected to be any), secondarily by determined title
				.sorted(nullsLast(Comparator.<PostArtifact, LocalDate>comparing(postArtifact -> postArtifact.getResourceDescription()
						.findPropertyValueByHandle(PROPERTY_HANDLE_PUBLISHED_ON).flatMap(asInstance(LocalDate.class)).orElse(null)).reversed())
								.thenComparing(PostArtifact::determineTitle))
				//generate content; the first element must be a separator element, which will be ignored for the first post
				.flatMap(throwingFunction(postArtifact -> {
					//separator
					final Element separatorElement = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_HR); //<hr/>
					//title
					final String postHref = context.relativizeSourceReference(contextArtifact, postArtifact).toString();
					final Element titleElement = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_H2); //<h2>
					final Element titleElementLink = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_A); //<h2><a>
					titleElementLink.setAttributeNS(null, ELEMENT_A_ATTRIBUTE_HREF, postHref);
					appendText(titleElementLink, postArtifact.determineTitle()); //<h2><a>title</a></h2>
					titleElement.appendChild(titleElementLink);
					//publication date
					final Optional<Element> publishedOnElement = postArtifact.getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_PUBLISHED_ON)
							.flatMap(asInstance(LocalDate.class)).map(publishedOn -> {
								final Element element = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_H3); //<h3>
								appendText(element, PUBLISHED_ON_FORMATTER.format(publishedOn));
								return element;
							});
					//excerpt
					final Optional<Element> excerptElement = mummifier.loadSourceExcerpt(context, postArtifact).map(excerpt -> {
						//Wrap the excerpt in a <div>. The other option would be to import the document fragment children directly into the document,
						//but wrapping the excerpt may be more semantically correct and more useful for styling in the future.
						final Element excerptWrapper = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_DIV); //<div>
						appendImportedChildNodes(excerptWrapper, excerpt); //import the excerpt into the <div>
						return excerptWrapper;
					});
					//more
					final String moreLabel = findAttributeNS(widgetElement, ATTRIBUTE_MORE_LABEL).orElse("…");
					final Element moreLink = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_A); //<a>
					moreLink.setAttributeNS(null, ELEMENT_A_ATTRIBUTE_HREF, postHref);
					appendText(moreLink, moreLabel); //<a>…</a>
					return concat(concat(Stream.of(separatorElement, titleElement), publishedOnElement.stream()), concat(excerptElement.stream(), Stream.of(moreLink)));
				})).skip(1) //skip the first separator so that separators will only appear between posts
				.collect(toList());
	}

}
