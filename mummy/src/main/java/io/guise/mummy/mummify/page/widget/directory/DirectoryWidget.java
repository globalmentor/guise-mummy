/*
 * Copyright © 2020 GlobalMentor, Inc. <https://www.globalmentor.com/>
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
import java.text.Collator;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import org.w3c.dom.*;

import com.globalmentor.collections.comparators.SortOrder;
import com.globalmentor.io.IllegalDataException;
import com.globalmentor.xml.spec.NsName;

import io.guise.mummy.Artifact;
import io.guise.mummy.GuiseMummy;
import io.guise.mummy.MummyContext;
import io.guise.mummy.CorporealSourceArtifact;
import io.guise.mummy.mummify.page.PageMummifier;
import io.guise.mummy.mummify.page.widget.Widget;

/**
 * A widget representing a "directory" or "index" of artifacts.
 * <p>
 * Examples
 * </p>
 * <dl>
 * <dt>{@code <mummy:directory />}
 * <dd>A simple list if pages, sorted by publication date and then by title, both in ascending order.</dd>
 * <dt>{@code <mummy:directory group-by="-publication-year"/>}
 * <dd>A simple list of pages, grouped by year in reverse publication year, and sorted by publication date and title within each group.</dd>
 * <dt>{@code <mummy:directory archetype="blog" more-label="Read More"/>}
 * <dd>A series of blog entries summaries and excerpts, sorted in reverse-order by publication date and title, with trailing "Read More" link (in addition to
 * the title link).</dd>
 * </dl>
 * @apiNote This widget would typically be found in the content historically found in an <code>index.html</code> file. Rather than providing a reverse lookup
 *          based on content as a book index normally does, instead this widget provides a listing of artifacts by name or title, as the directory of a building
 *          might show or as a console <code>dir</code> DOS command might display.
 * @implSpec This implementation supports an <code>archetype</code> attribute of <code>blog</code>, which causes the items to be sorted in descending order of
 *           publication date, and then ascending by title. Each item will result in a title, date, extract, and other information. A <code>more-label</code>
 *           value can be specified for the text of the trailing link to the blog post.
 * @implSpec If no archetype is specified, the items are presented in a simple list, ordered by publication and then by title, both in ascending order. A
 *           <code>group-by</code> attribute may be specified with either of the values <code>publication-date</code> or <code>publication-year</code>,
 *           optionally prepended with <code>+</code> or <code>-</code> to indicate if the groupings should be sorted in ascending or descending order.
 * @author Garret Wilson
 */
public class DirectoryWidget implements Widget {

	/** The widget element name. */
	private static final NsName WIDGET_ELEMENT = NsName.of(GuiseMummy.NAMESPACE_STRING, "directory");

	/** The optional attribute indicating some general predefine type of layout. */
	private static final NsName ATTRIBUTE_ARCHETYPE = NsName.of("archetype");
	/** The archetype value for a blog layout. */
	private static final String ARCHETYPE_BLOG = "blog";

	/** The optional attribute indicating how the items should be grouped. */
	private static final NsName ATTRIBUTE_GROUP_BY = NsName.of("group-by");
	/** The group-by value for grouping by publication date. */
	private static final String GROUP_BY_PUBLICATION_DATE = "publication-date";
	/** The group-by value for grouping by publication year. */
	private static final String GROUP_BY_PUBLICATION_YEAR = "publication-year";

	/** The optional attribute indicating the label for the "more" link. */
	private static final NsName ATTRIBUTE_MORE_LABEL = NsName.of("more-label");

	/** The formatter for producing the published on date string. */
	private static final DateTimeFormatter PUBLISHED_ON_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL); //i18n; update to allow project-based locale from mummification context; probably request formatter from mummification context

	@Override
	public NsName getWidgetElementName() {
		return WIDGET_ELEMENT;
	}

	@Override
	public List<Element> processElement(final PageMummifier mummifier, final MummyContext context, final Artifact artifact, final Element widgetElement)
			throws IOException, DOMException {
		final Document document = widgetElement.getOwnerDocument();
		final Stream<Artifact> items = mummifier.childNavigationArtifacts(context, artifact);
		return findAttribute(widgetElement, ATTRIBUTE_GROUP_BY) //group-by
				.map(groupBy -> {
					final List<Element> groupedItemElements = new ArrayList<>();
					//determine the sort order, if any, by the prefix
					final Optional<SortOrder> foundSortOrder = !groupBy.isEmpty() ? SortOrder.findFromSign(groupBy.charAt(0)) : Optional.empty();
					//strip off the sort order if needed
					final String groupByValue = foundSortOrder.map(sortOrder -> groupBy.substring(1)).orElse(groupBy);
					switch(groupByValue) {
						case GROUP_BY_PUBLICATION_DATE:
							{
								final Map<Optional<LocalDate>, List<Artifact>> itemsByFoundPublicationDate = items.collect(groupingBy(
										item -> item.getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_PUBLISHED_ON).flatMap(asInstance(LocalDate.class))));
								final Stream<Map.Entry<Optional<LocalDate>, List<Artifact>>> groups = itemsByFoundPublicationDate.entrySet().stream();
								final Stream<Map.Entry<Optional<LocalDate>, List<Artifact>>> sortedGroups = foundSortOrder.map(sortOrder -> sortOrder //sort by date, nulls first, reversing if necessary
										.applyTo(Comparator.<Map.Entry<Optional<LocalDate>, List<Artifact>>, LocalDate>comparing(entry -> entry.getKey().orElse(null),
												nullsFirst(naturalOrder()))))
										.map(groups::sorted).orElse(groups);
								sortedGroups.forEach(throwingConsumer(itemsForFoundPublicationDate -> {
									final Optional<LocalDate> foundPublicationDate = itemsForFoundPublicationDate.getKey();
									final Element groupHeadingElement = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_H2); //<h2>
									final String publicationDateText = foundPublicationDate.map(PUBLISHED_ON_FORMATTER::format).orElse("?"); //TODO fix text
									appendText(groupHeadingElement, publicationDateText);
									groupedItemElements.add(groupHeadingElement);
									groupedItemElements
											.addAll(generateItemElements(mummifier, context, artifact, widgetElement, 3, itemsForFoundPublicationDate.getValue().stream()));
								}));
							}
							break;
						case GROUP_BY_PUBLICATION_YEAR:
							{
								final Map<Optional<Year>, List<Artifact>> itemsByFoundPublicationYear = items.collect(groupingBy(item -> item.getResourceDescription()
										.findPropertyValueByHandle(PROPERTY_HANDLE_PUBLISHED_ON).flatMap(asInstance(LocalDate.class)).map(Year::from)));
								final Stream<Map.Entry<Optional<Year>, List<Artifact>>> groups = itemsByFoundPublicationYear.entrySet().stream();
								final Stream<Map.Entry<Optional<Year>, List<Artifact>>> sortedGroups = foundSortOrder.map(sortOrder -> sortOrder //sort by year, nulls first, reversing if necessary
										.applyTo(Comparator.<Map.Entry<Optional<Year>, List<Artifact>>, Year>comparing(entry -> entry.getKey().orElse(null),
												nullsFirst(naturalOrder()))))
										.map(groups::sorted).orElse(groups);
								sortedGroups.forEach(throwingConsumer(itemsForFoundPublicationYear -> {
									final Optional<Year> foundPublicationYear = itemsForFoundPublicationYear.getKey();
									final Element groupHeadingElement = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_H2); //<h2>
									final String publicationYear = foundPublicationYear.map(Year::toString).orElse("?"); //TODO fix text
									appendText(groupHeadingElement, publicationYear);
									groupedItemElements.add(groupHeadingElement);
									groupedItemElements
											.addAll(generateItemElements(mummifier, context, artifact, widgetElement, 3, itemsForFoundPublicationYear.getValue().stream()));
								}));
							}
							break;
						default:
							throw new IllegalDataException(String.format("Unrecognized `%s` attribute value `%s`.", ATTRIBUTE_GROUP_BY, groupBy));
					}
					return groupedItemElements;
				})
				//no grouping
				.orElseGet(throwingSupplier(() -> generateItemElements(mummifier, context, artifact, widgetElement, 2, items)));
	}

	/**
	 * Generates elements to represent the items in the directory.
	 * @param mummifier The mummifier processing the page on which this widget appears.
	 * @param context The context of static site generation.
	 * @param artifact The artifact being generated.
	 * @param widgetElement The list element to regenerate.
	 * @param headingLevel The one-based heading level (with <code>&lt;h1&gt;</code> being the first heading level) with which to start when generating headings;
	 *          may not apply to all archetypes.
	 * @param items The directory items to process.
	 * @return The generated items to represent the items. There may not be a one-to-one correspondence between items and generated elements.
	 * @throws IOException if there is an I/O error processing the element.
	 * @throws IllegalDataException if the information in the widget element is not appropriate for the widget.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public List<Element> generateItemElements(@Nonnull final PageMummifier mummifier, @Nonnull final MummyContext context, @Nonnull final Artifact artifact,
			@Nonnull final Element widgetElement, @Nonnegative final int headingLevel, Stream<Artifact> items)
			throws IOException, IllegalDataException, DOMException {
		final Document document = widgetElement.getOwnerDocument();
		final Collator titleCollator = Collator.getInstance(); //TODO i18n: get locale for page, defaulting to site locale
		titleCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
		titleCollator.setStrength(Collator.PRIMARY); //ignore accents and case
		return findAttribute(widgetElement, ATTRIBUTE_ARCHETYPE) //archetype
				.map(archetype -> {
					switch(archetype) {
						case ARCHETYPE_BLOG:
							if(findAttribute(widgetElement, ATTRIBUTE_GROUP_BY).isPresent()) {
								throw new IllegalDataException(
										String.format("Attribute `%s` not allowed with attribute `%s` value `%s`.", ATTRIBUTE_GROUP_BY, ATTRIBUTE_ARCHETYPE, archetype));
							}
							return items.sorted( //sort the items in reverse order of (published-on date followed by undated artifacts), secondarily by determined title
									Comparator
											.<Artifact, LocalDate>comparing(item -> item.getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_PUBLISHED_ON)
													.flatMap(asInstance(LocalDate.class)).orElse(null), nullsFirst(naturalOrder()))
											.reversed().thenComparing(Artifact::determineTitle, titleCollator))
									.flatMap(item -> {
										//separator (will be ignored for the first item)
										final Element separatorElement = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_HR); //<hr/>
										//title
										final String postHref = context.getPlan().referenceInSource(artifact, item).toString();
										final Element titleElement = createElement(document, ELEMENT_H(headingLevel)); //<h1>
										final Element titleElementLink = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_A); //<h1><a>
										titleElementLink.setAttributeNS(null, ELEMENT_A_ATTRIBUTE_HREF, postHref);
										appendText(titleElementLink, item.determineTitle()); //<h1><a>title</a></h1>
										titleElement.appendChild(titleElementLink);
										//publication date
										final Optional<Element> publishedOnElement = item.getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_PUBLISHED_ON)
												.flatMap(asInstance(LocalDate.class)).map(publishedOn -> {
													final Element element = createElement(document, ELEMENT_H(headingLevel + 1)); //<h2>
													appendText(element, PUBLISHED_ON_FORMATTER.format(publishedOn));
													return element;
												});
										//excerpt; only included if the item is a corporeal source artifact
										final Optional<Element> excerptElement = asInstance(item, CorporealSourceArtifact.class)
												.flatMap(throwingFunction(sourceFileItem -> mummifier.loadSourceExcerpt(context, sourceFileItem))).map(excerpt -> {
													//Wrap the excerpt in a <div>. The other option would be to import the document fragment children directly into the document,
													//but wrapping the excerpt may be more semantically correct and more useful for styling in the future.
													final Element excerptWrapper = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_DIV); //<div>
													appendImportedChildNodes(excerptWrapper, excerpt); //import the excerpt into the <div>
													return excerptWrapper;
												});
										//more
										final String moreLabel = findAttribute(widgetElement, ATTRIBUTE_MORE_LABEL).orElse("…");
										final Element moreLink = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_A); //<a>
										moreLink.setAttributeNS(null, ELEMENT_A_ATTRIBUTE_HREF, postHref);
										appendText(moreLink, moreLabel); //<a>…</a>
										return concat(concat(Stream.of(separatorElement, titleElement), publishedOnElement.stream()),
												concat(excerptElement.stream(), Stream.of(moreLink)));
									}).skip(1) //skip the first separator so that separators will only appear between posts
									.collect(toList());
						default:
							throw new IllegalDataException(String.format("Unrecognized `%s` attribute value `%s`.", ATTRIBUTE_ARCHETYPE, archetype));
					}
				}).orElseGet(() -> { //no archetype
					final Element ulElement = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL); //<ul>
					items.sorted(Comparator //sort the items in order of published-on date followed by undated artifacts;, secondarily by determined title
							.<Artifact, LocalDate>comparing(item -> item.getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_PUBLISHED_ON)
									.flatMap(asInstance(LocalDate.class)).orElse(null), nullsLast(naturalOrder()))
							.thenComparing(Artifact::determineTitle, titleCollator)).map(item -> { //map each item to `<li><a>title</a></li>`
								final Element liElement = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI); //<li>
								final String postHref = context.getPlan().referenceInSource(artifact, item).toString();
								final Element liElementLink = document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_A); //<li><a>
								liElementLink.setAttributeNS(null, ELEMENT_A_ATTRIBUTE_HREF, postHref);
								appendText(liElementLink, item.determineTitle()); //<li><a>title</a></li>
								liElement.appendChild(liElementLink);
								return liElement;
							}).forEach(ulElement::appendChild);
					return List.of(ulElement);
				});
	}

}
