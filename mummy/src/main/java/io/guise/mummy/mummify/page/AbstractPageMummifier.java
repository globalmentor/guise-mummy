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

import static com.globalmentor.html.HtmlDom.*;
import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.java.CharSequences.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.lex.CompoundTokenization.*;
import static com.globalmentor.util.Optionals.*;
import static com.globalmentor.xml.XmlDom.*;
import static io.guise.mummy.Artifact.*;
import static io.guise.mummy.GuiseMummy.*;
import static java.nio.file.Files.*;
import static java.util.Collections.*;
import static java.util.function.Function.*;
import static java.util.function.Predicate.*;
import static java.util.stream.Collectors.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.text.Collator;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.*;

import org.w3c.dom.*;

import com.globalmentor.html.*;
import com.globalmentor.html.spec.HTML;
import com.globalmentor.io.IllegalDataException;
import com.globalmentor.net.*;
import com.globalmentor.rdfa.spec.RDFa;
import com.globalmentor.vocab.Curie;
import com.globalmentor.vocab.*;
import com.globalmentor.xml.*;
import com.globalmentor.xml.spec.*;

import io.guise.mesh.*;
import io.guise.mummy.*;
import io.guise.mummy.mummify.AbstractFileMummifier;
import io.guise.mummy.mummify.page.widget.Widget;
import io.guise.mummy.mummify.page.widget.directory.DirectoryWidget;
import io.urf.URF;
import io.urf.URF.Handle;
import io.urf.model.UrfResourceDescription;
import io.urf.vocab.content.Content;

/**
 * Abstract base mummifier for generating HTML pages.
 * @implSpec This mummifier generates pages using the string configured for {@value GuiseMummy#CONFIG_KEY_MUMMY_TEXT_OUTPUT_LINE_SEPARATOR} as the newline
 *           sequence in order to provide consistent, repeatable build across platforms.
 * @author Garret Wilson
 */
public abstract class AbstractPageMummifier extends AbstractFileMummifier implements PageMummifier {

	/** The supported widgets, mapped to their XHTML element names. */
	private static final Map<NsName, Widget> WIDGETS_BY_ELEMENT_NAME = Stream.of(new DirectoryWidget())
			.collect(toUnmodifiableMap(Widget::getWidgetElementName, identity()));

	/**
	 * A map of local names of HTML elements that can reference other resources (e.g. <code>"img"</code>), along with the attributes of each element that contains
	 * the actual resource reference path (e.g. (e.g. <code>"src"</code>) for {@code <img src="…">}).
	 */
	private static final Map<String, String> HTML_REFERENCE_ELEMENT_ATTRIBUTES = Map.ofEntries( //element local name -> attribute
			Map.entry(ELEMENT_A, ELEMENT_A_ATTRIBUTE_HREF), //<a href="…">
			Map.entry(ELEMENT_AREA, ELEMENT_AREA_ATTRIBUTE_HREF), //<area href="…">
			Map.entry(ELEMENT_AUDIO, ELEMENT_AUDIO_ATTRIBUTE_SRC), //<audio src="…">
			Map.entry(ELEMENT_EMBED, ELEMENT_EMBED_ATTRIBUTE_SRC), //<embed src="…">
			Map.entry(ELEMENT_FRAME, ELEMENT_FRAME_ATTRIBUTE_SRC), //<frame src="…">
			Map.entry(ELEMENT_IFRAME, ELEMENT_IFRAME_ATTRIBUTE_SRC), //<iframe src="…">
			Map.entry(ELEMENT_IMG, ELEMENT_IMG_ATTRIBUTE_SRC), //<img src="…">
			Map.entry(ELEMENT_LINK, ELEMENT_LINK_ATTRIBUTE_HREF), //<link href="…">
			Map.entry(ELEMENT_OBJECT, ELEMENT_OBJECT_ATTRIBUTE_DATA), //<object data="…">
			Map.entry(ELEMENT_SCRIPT, ELEMENT_SOURCE_ATTRIBUTE_SRC), //<script src="…">
			Map.entry(ELEMENT_SOURCE, ELEMENT_SOURCE_ATTRIBUTE_SRC), //<source src="…">
			Map.entry(ELEMENT_TRACK, ELEMENT_TRACK_ATTRIBUTE_SRC), //<track src="…">
			Map.entry(ELEMENT_VIDEO, ELEMENT_VIDEO_ATTRIBUTE_SRC) //<video src="…">
	);

	//some namespace-identified XHTML elements for easier matching; will switch to GlobalMentor HTML constants when available 
	private final static NsName XHTML_ELEMENT_FRAMESET = NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_FRAMESET);
	private final static NsName XHTML_ELEMENT_P = NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_P);
	private final static NsName XHTML_ELEMENT_SCRIPT = NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_SCRIPT);
	private final static NsName XHTML_ELEMENT_SCRIPT_ATTRIBUTE_SRC = NsName.of(ELEMENT_SOURCE_ATTRIBUTE_SRC);
	private final static NsName XHTML_ELEMENT_STYLE = NsName.of(XHTML_NAMESPACE_URI_STRING, ELEMENT_STYLE);

	/**
	 * Vocabulary prefixes that will be recognized in metadata, such as in XHTML {@code <meta>} elements or in YAML, if they have not been associated with a
	 * different vocabulary. The URF ad-hoc namespace {@link URF#AD_HOC_NAMESPACE} is used as the default so that a CURIE with no prefix can be correctly
	 * converted to a tag.
	 * @implSpec These prefixes include those in {@link RDFa.InitialContext#VOCABULARIES}.
	 * @implSpec These prefixes include the prefix {@link GuiseMummy#NAMESPACE_PREFIX} for the namespace {@link GuiseMummy#NAMESPACE}.
	 */
	protected static final VocabularyRegistry PREDEFINED_VOCABULARIES = VocabularyRegistry.builder(RDFa.InitialContext.VOCABULARIES)
			.setDefaultVocabulary(URF.AD_HOC_NAMESPACE).registerPrefix(GuiseMummy.NAMESPACE_PREFIX, GuiseMummy.NAMESPACE).build();

	/**
	 * The HTML formatting profile for pages.
	 * @implSpec Notably this profile manages the order of the RDFa {@value RDFa#ATTRIBUTE_PROPERTY} attribute.
	 * @author Garret Wilson
	 */
	protected static class PageFormatProfile extends DefaultHtmlFormatProfile {

		/** Shared singleton instance. */
		public final static PageFormatProfile INSTANCE = new PageFormatProfile();

		/** Constructor. */
		protected PageFormatProfile() {
		}

		/** The predefined attribute order this implementation uses for all elements. */
		private static final List<NsName> ATTRIBUTE_ORDER;

		static { //insert the `property` after the `name` attribute, or at the beginning if no `name`
			final List<NsName> attributeOrder = new ArrayList<>(DefaultHtmlFormatProfile.ATTRIBUTE_ORDER);
			final int nameIndex = attributeOrder.indexOf(NsName.of(ATTRIBUTE_NAME));
			assert nameIndex >= -1;
			attributeOrder.add(nameIndex + 1, NsName.of(RDFa.ATTRIBUTE_PROPERTY));
			ATTRIBUTE_ORDER = attributeOrder.stream().collect(toUnmodifiableList());
		}

		/**
		 * {@inheritDoc}
		 * @implSpec This implementation returns the custom {@link #ATTRIBUTE_ORDER} which includes the RDFa {@value RDFa#ATTRIBUTE_PROPERTY} attribute.
		 */
		@Override
		protected List<NsName> getAttributeOrder(final NsName element) {
			return ATTRIBUTE_ORDER;
		}

	};

	private final NavigationManager navigationManager = new NavigationManager();

	/** @return The strategy for loading and working with navigation. */
	protected NavigationManager getNavigationManager() {
		return navigationManager;
	}

	private final GuiseMesh guiseMesh = new GuiseMesh();

	/** @return The strategy for transformation a document based upon Mesh Expression Language (MEXL) expressions. */
	protected GuiseMesh getGuiseMesh() {
		return guiseMesh;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version changes the output file extension to {@value PageMummifier#PAGE_NAME_EXTENSION}, or leaves if off altogether if bare names were
	 *           requested.
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_PAGE_NAMES_BARE
	 */
	@Override
	public String planArtifactTargetFilename(final MummyContext context, final String filename) {
		final String targetFilename = super.planArtifactTargetFilename(context, filename);
		final boolean isNameBare = context.getConfiguration().findBoolean(CONFIG_KEY_MUMMY_PAGE_NAMES_BARE).orElse(false);
		if(isNameBare) { //so-called clean URLs
			return removeExtension(targetFilename);
		} else {
			return changeExtension(targetFilename, PAGE_NAME_EXTENSION);
		}
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version returns the media type <code>text/html</code> for all pages.
	 * @see #PAGE_MEDIA_TYPE
	 */
	@Override
	public Optional<ContentType> getArtifactMediaType(final MummyContext context, final Path sourcePath) throws IOException {
		return Optional.of(PAGE_MEDIA_TYPE);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation creates a {@link PostArtifact} for those artifacts with a source filename matching {@link PostArtifact#FILENAME_PATTERN}.
	 *           Otherwise it creates a {@link PageArtifact}.
	 */
	@Override
	protected Artifact createArtifact(final Path sourceFile, final Path outputFile, final UrfResourceDescription description) throws IOException {
		final Path sourceFilename = sourceFile.getFileName();
		if(sourceFilename != null && PostArtifact.FILENAME_PATTERN.matcher(sourceFilename.toString()).matches()) {
			return new PostArtifact(this, sourceFile, outputFile, description);
		}
		return new PageArtifact(this, sourceFile, outputFile, description);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation delegates to {@link NavigationManager#loadNavigation(MummyContext, Artifact)}. Otherwise if no navigation file is provided,
	 *           this implementation delegates to {@link #defaultNavigation(MummyContext, Artifact)}.
	 */
	@Override
	public Stream<NavigationItem> navigation(@Nonnull MummyContext context, final Artifact contextArtifact, final Artifact artifact) throws IOException {
		return getNavigationManager().loadNavigation(context, artifact).orElseGet(() -> defaultNavigation(context, contextArtifact));
	}

	/**
	 * Provides the default tree of items suitable for direct navigation from this artifact. The may include the parent artifact, sibling artifacts, and/or the
	 * given resource itself. This official list may be overridden by the user through the use of a <code>.navigation.list</code> file or other configured file.
	 * @apiNote These artifacts represent a default fallback. It is not usually appropriate to override this method.
	 * @implSpec This implementation calls {@link #defaultNavigationArtifacts(MummyContext, Artifact)}.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @return The navigation items, in order, that constitute the official possible navigation destinations from this artifact.
	 * @throws IllegalArgumentException if the information of the navigation artifacts prevent them from being ordered.
	 */
	protected Stream<NavigationItem> defaultNavigation(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact) {
		return defaultNavigationArtifacts(context, contextArtifact).map(navigationArtifact -> { //map navigation artifacts to their navigation items
			final String href = context.getPlan().referenceInSource(contextArtifact, navigationArtifact).toString();
			return DefaultNavigationItem.forArtifactReference(href, navigationArtifact);
		});
	}

	/**
	 * Provides the the default artifacts suitable for direct navigation from this artifact. These may include the parent artifact, sibling artifacts, and/or the
	 * given resource itself.
	 * @apiNote These artifacts represent a default fallback. It is not usually appropriate to override this method.
	 * @implSpec This implementation currently filters out post artifacts.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @return The artifacts, in order, that constitute the official possible navigation destinations from this artifact.
	 * @throws IllegalArgumentException if the information of the navigation artifacts prevent them from being ordered.
	 * @see #findParentNavigationArtifact(MummyContext, Artifact)
	 * @see #childNavigationArtifacts(MummyContext, Artifact)
	 */
	protected Stream<Artifact> defaultNavigationArtifacts(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact) {
		//decide how to sort the links
		final Collator navigationCollator = Collator.getInstance(); //TODO i18n: get locale for page, defaulting to site locale
		navigationCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
		navigationCollator.setStrength(Collator.PRIMARY); //ignore accents and case
		final Comparator<Artifact> navigationArtifactOrderComparator = Comparator
				//compare first by order (defaulting to zero)
				.<Artifact, Long>comparing(navigationArtifact -> {
					try {
						return toLong(navigationArtifact.getResourceDescription().findPropertyValue(PROPERTY_TAG_MUMMY_ORDER).orElse(MUMMY_ORDER_DEFAULT));
					} catch(final IllegalArgumentException illegalArgumentException) {
						throw new IllegalArgumentException(
								String.format("Invalid property <%s> value: %s", PROPERTY_TAG_MUMMY_ORDER, illegalArgumentException.getLocalizedMessage()),
								illegalArgumentException);
					}
				})
				//then compare by label alphabetical order
				.thenComparing(navigationArtifact -> navigationArtifact.determineLabel(), navigationCollator);
		return Stream.concat(
				//put the parent navigation artifact (if any) first
				findParentNavigationArtifact(context, contextArtifact).stream(),
				//then include the sorted child navigation artifacts
				childNavigationArtifacts(context, contextArtifact)
						//posts shouldn't appear in the normal navigation list TODO create a more semantic means of filtering posts
						.filter(not(PostArtifact.class::isInstance)).sorted(navigationArtifactOrderComparator));
	}

	/**
	 * Converts a metadata element to zero, or more property tag URI and value associations.
	 * <p>
	 * Property names are determined as follows:
	 * </p>
	 * <ul>
	 * <li>Property names in both the {@value HTML#ATTRIBUTE_NAME} and {@value RDFa#ATTRIBUTE_PROPERTY} attributes are recognized. Multiple property names are
	 * supported in the {@value RDFa#ATTRIBUTE_PROPERTY} attribute.</li>
	 * <li>If a property name is detected but no {@value HTML#ELEMENT_META_ATTRIBUTE_CONTENT} attribute is present, the empty string is used for the value as per
	 * <a href="https://www.w3.org/TR/html52/document-metadata.html#the-meta-element">HTML 5.2 § 4.2.5. The meta element</a>.
	 * <li>If the property name(s) in the {@value RDFa#ATTRIBUTE_PROPERTY} is an RDFa <a href="https://www.w3.org/TR/rdfa-core/#s_curies">CURIE</a> such as the
	 * Open Graph <code>og:title</code> or even the Guise Mummy <code>mummy:order</code>, the CURIE is combined with its prefix by searching the hierarchy of the
	 * given element. The CURIE reference is converted from <code>kebab-case</code> to <code>camelCase</code>. Otherwise the meta name itself is used as a
	 * property handle, after converting from <code>kebab-case</code> to <code>camelCase</code>.</li>
	 * <li>A property names in the {@value HTML#ATTRIBUTE_NAME} attribute is interpreted as a single non-prefixed name in the {@value RDFa#ATTRIBUTE_PROPERTY}
	 * attribute.
	 * </ul>
	 * <p>
	 * Property values types are inferred from the resulting property tag if possible.
	 * </p>
	 * @apiNote The indicated exceptions can also be thrown during iteration of the stream.
	 * @implSpec The current implementation only finds prefixes if they are stored in <code>xmlns:</code> XML namespace prefix declarations, not in HTML5
	 *           <code>prefix</code> attributes.
	 * @implSpec This implementation also recognizes all namespace prefixes included in {@link #PREDEFINED_VOCABULARIES} if they are not otherwise defined in the
	 *           document.
	 * @implSpec This implementation does not yet support a {@value RDFa#ATTRIBUTE_PROPERTY} attribute containing one or more absolute IRIs.
	 * @implSpec This implementation delegates to {@link #parseMetadataPropertyValue(URI, CharSequence)} to determine the final value, inferring the property type
	 *           based upon the property tag if possible.
	 * @param metaElement The {@code <meta>} element potentially representing a property.
	 * @return A potentially empty stream of property tag IRIs paired with values representing properties.
	 * @throws IllegalArgumentException if the property name is a CURIE but no prefix has been defined in the element hierarchy.
	 * @throws IllegalArgumentException if the property name is a CURIE but combined with the IRI leading segment does not result in a valid IRI.
	 * @throws IllegalArgumentException if the given property name is empty.
	 * @throws IllegalArgumentException if the given property name cannot be converted to <code>cameCase</code>, e.g. it has successive <code>'-'</code>
	 *           characters.
	 * @see <a href="https://www.w3.org/TR/rdfa-core/">RDFa Core 1.1</a>
	 * @see <a href="https://www.w3.org/TR/rdfa-core/#s_syntax">RDFa Core 1.1, § 5. Attributes and Syntax</a>
	 * @see <a href="https://www.w3.org/TR/html-rdfa/#extensions-to-the-html5-syntax">HTML+RDFa 1.1, § 4. Extensions to the HTML5 Syntax.</a>
	 * @see <a href="https://www.w3.org/TR/rdfa-core/#s_curies">RDFa Core 1.1 - Third Edition § 6. CURIE Syntax Definition</a>.
	 * @see <a href="https://ogp.me/">The Open Graph protocol</a>
	 * @see Curie
	 */
	protected static Stream<Map.Entry<URI, Object>> htmlMetaElementToProperties(@Nonnull final Element metaElement) {
		final Optional<URI> tagFromNameAttribute = findAttributeNS(metaElement, null, ELEMENT_META_ATTRIBUTE_NAME).map(name -> {
			checkArgument(!name.isEmpty(), "`<meta>` element `name` attribute must not be the empty string.");
			checkArgument(!contains(name, Curie.PREFIX_DELIMITER),
					"Property prefix not allowed `<meta>` element `name` attribute `%s`; consider using `property` attribute instead.", name);
			return Handle.toTag(KEBAB_CASE.toCamelCase(name));
		});
		final Stream<URI> tagsFromPropertyAttribute = findAttributeNS(metaElement, null, RDFa.ATTRIBUTE_PROPERTY).stream().flatMap(properties -> {
			final List<String> tokens = RDFa.WHITESPACE_CHARACTERS.split(properties);
			checkArgument(!tokens.isEmpty(), "`<meta>` element `property` attribute must contain at least one property.");
			return tokens.stream().map(property -> {
				final Curie curie = Curie.parse(property).mapReference(KEBAB_CASE::toCamelCase);
				final String reference = curie.getReference();
				return curie.getPrefix().map(prefix -> {
					String leadingSegment = null;
					Node currentNode = metaElement;
					do {
						leadingSegment = findAttributeNS((Element)currentNode, XML.XMLNS_NAMESPACE_URI_STRING, prefix).orElse(null);
					} while(leadingSegment == null && (currentNode = currentNode.getParentNode()) instanceof Element); //keep looking while we need to and while there are still parent elements
					if(leadingSegment == null) { //see if we have a predefined vocabulary for the prefix
						leadingSegment = PREDEFINED_VOCABULARIES.findVocabularyByPrefix(prefix).map(URI::toString).orElse(null);
					}
					checkArgument(leadingSegment != null, "No IRI leading segment defined for prefix `%s` of property `%s`.", prefix, property);
					return VocabularyTerm.toURI(URI.create(leadingSegment), reference);
				}).orElseGet(() -> Handle.toTag(curie.getReference()));
			});
		});
		//TODO add support for Microdata `itemprop`; see https://www.w3.org/TR/microdata/#names:-the-itemprop-attribute
		//if no content attribute, the value is the empty string as per _HTML 5.2 § 4.2.5. The meta element_
		final String lexicalValue = findAttributeNS(metaElement, null, ELEMENT_META_ATTRIBUTE_CONTENT).orElse("");
		return Stream.concat(tagFromNameAttribute.stream(), tagsFromPropertyAttribute).map(tag -> Map.entry(tag, parseMetadataPropertyValue(tag, lexicalValue)));
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation opens an input stream to the given file and then extract the source metadata by calling
	 *           {@link #loadSourceMetadata(MummyContext, InputStream, String)}.
	 */
	@Override
	protected List<Map.Entry<URI, Object>> loadSourceMetadata(@Nonnull MummyContext context, @Nonnull final Path sourceFile) throws IOException {
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(sourceFile))) {
			return loadSourceMetadata(context, inputStream, sourceFile.toString());
		}
	}

	/**
	 * Loads metadata stored in the source file itself.
	 * @implSpec This implementation loads description from metadata in the XHTML document obtained by calling
	 *           {@link #loadSourceDocument(MummyContext, InputStream, String)} and then calls {@link #extractMetadata(MummyContext, Document)} to extract the
	 *           metadata.
	 * @param context The context of static site generation.
	 * @param inputStream The input stream from which to to load the source metadata.
	 * @param name The full identifier of the source, such as a path or URL.
	 * @return Metadata stored in the source file being mummified, consisting of resolved URI tag names and values. The name-value pairs may have duplicate names.
	 * @throws IOException if there is an I/O error retrieving the metadata, including incorrectly formatted metadata.
	 */
	protected List<Map.Entry<URI, Object>> loadSourceMetadata(@Nonnull MummyContext context, @Nonnull InputStream inputStream, @Nonnull final String name)
			throws IOException {
		final Document sourceDocument = loadSourceDocument(context, inputStream, name);
		sourceDocument.normalize(); //**Do not call `document.normalizeDocument()`**; see note in `normalizeDocument()` below.
		try {
			return extractMetadata(context, sourceDocument);
		} catch(final IllegalArgumentException | IllegalDataException | DOMException exception) {
			throw new IOException(String.format("Error processing metadata in `%s`: %s", name, exception.getLocalizedMessage()), exception); //TODO i18n
		}
	}

	/**
	 * Extracts metadata stored in the source document itself.
	 * @implSpec The XHTML document {@code <head><title>} will be returned as metadata, using {@value Artifact#PROPERTY_HANDLE_TITLE} as a handle; followed by
	 *           values in any {@code <head><meta>} elements, converted using {@link #htmlMetaElementToProperties(Element)}.
	 * @param context The context of static site generation.
	 * @param sourceDocument The source XHTML document being mummified, from which metadata should be extracted.
	 * @return Metadata stored in the source document being mummified, consisting of resolved URI tag names and values. The name-value pairs may have duplicate
	 *         names.
	 * @throws IllegalArgumentException if any of the metadata is invalid.
	 * @throws DOMException if there is a problem retrieving metadata.
	 */
	protected List<Map.Entry<URI, Object>> extractMetadata(@Nonnull MummyContext context, @Nonnull final Document sourceDocument) throws DOMException {
		//TODO consider parsing out "keywords" in to multiple keyword+ properties for convenience
		return Stream.<Map.Entry<URI, Object>>concat(
				//<title>; will override any <code>title</code> metadata property in this same document
				findTitle(sourceDocument).stream().map(title -> Map.entry(Handle.toTag(PROPERTY_HANDLE_TITLE), title)),
				//<meta> TODO detect and add warnings for invalid properties
				htmlHeadMetaElements(sourceDocument).flatMap(AbstractPageMummifier::htmlMetaElementToProperties)).collect(toList());
		//TODO consider parsing out "keywords" in to multiple keyword+ properties for convenience
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation loads the source document using {@link #loadSourceDocument(MummyContext, InputStream, String)} and then extracts the first
	 *           paragraph.
	 * @implNote This implementation typically results in loading the source document N+1 times, that is, every time it is requested in addition to the time the
	 *           source document itself is mummified. Perhaps this won't be too much, though, as it is unlikely many targets would be requesting excerpts of the
	 *           same source.
	 */
	@Override
	public Optional<DocumentFragment> loadSourceExcerpt(final MummyContext context, final InputStream inputStream, final String name)
			throws IOException, DOMException {
		final Document sourceDocument = loadSourceDocument(context, inputStream, name);
		return findContentElement(sourceDocument).flatMap(this::getExcerpt);
	}

	/**
	 * Recursively finds and retrieves an excerpt from the given element.
	 * @implSpec This implementation uses the first non-empty paragraph encountered depth-first.
	 * @param element The element for which an excerpt should be returned.
	 * @return A document fragment containing an excerpt of the given element if one could be located.
	 * @see HTML#ELEMENT_P
	 */
	protected Optional<DocumentFragment> getExcerpt(final Element element) {
		if(XHTML_ELEMENT_P.matches(element)) { //XHTML `<p>`
			if(containsNonTrim(element.getTextContent())) { //if this paragraph isn't empty
				return Optional.of(extractNode(element)); //extract the paragraph
			}
		}
		return childElementsOf(element).map(this::getExcerpt).flatMap(Optional::stream).findFirst();
	}

	/**
	 * {@inheritDoc}
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_TEXT_OUTPUT_LINE_SEPARATOR
	 */
	@Override
	public void mummifyFile(final MummyContext context, final Artifact contextArtifact, final Artifact artifact) throws IOException {

		try {

			//#load source document: get starting content to work with
			final Document sourceDocument = loadSourceDocument(context, (SourceFileArtifact)artifact); //this mummifier requires source file artifacts
			getLogger().trace("Loaded page source document `{}`.", artifact.getSourcePath());

			//#normalize: normalize the DOM and remove metadata
			final Document normalizedDocument = normalizeDocument(context, contextArtifact, artifact, sourceDocument);

			//#apply template
			final Document templatedDocument = applyTemplate(context, contextArtifact, artifact, normalizedDocument);

			//#mesh document: evaluate MEXL expressions and perform transformations
			final MeshContext meshContext = new DefaultMeshContext();
			meshContext.setVariable(MESH_CONTEXT_VARIABLE_PLAN, context.getPlan());
			meshContext.setVariable(MESH_CONTEXT_VARIABLE_ARTIFACT, artifact);
			meshContext.setVariable(MESH_CONTEXT_VARIABLE_PAGE, artifact.getResourceDescription());
			final Document meshedDocument = getGuiseMesh().meshDocument(meshContext, templatedDocument);

			//#process document: evaluate Guise Mummy directives and widgets; and perform transformations
			final Document processedDocument = processDocument(context, contextArtifact, artifact, meshedDocument);

			//#relocate document from source to target: translate path references from the source to the target
			final Document relocatedDocument = relocateSourceDocumentToTarget(context, contextArtifact, artifact, processedDocument);

			//#cleanse document: remove all Guise Mummy related elements and attributes
			final Document cleansedDocument = cleanseDocument(context, contextArtifact, artifact, relocatedDocument);

			//#ascribe document: adds metadata not related to Guise Mummy directives
			final Document ascribedDocument = ascribeDocument(context, contextArtifact, artifact, cleansedDocument);

			//#save target document
			try (final OutputStream outputStream = new BufferedOutputStream(newOutputStream(artifact.getTargetPath()))) {
				final HtmlSerializer htmlSerializer = new HtmlSerializer(true, PageFormatProfile.INSTANCE);
				htmlSerializer.setLineSeparator(context.getConfiguration().getString(CONFIG_KEY_MUMMY_TEXT_OUTPUT_LINE_SEPARATOR));
				htmlSerializer.serialize(ascribedDocument, null, null, outputStream); //serialize using the HTML5 doctype (with no public or system ID)
			}
			getLogger().trace("Generated page output document `{}`.", artifact.getTargetPath());

		} catch(final IllegalArgumentException | IllegalDataException | MeshException | DOMException exception) { //convert input errors and XML errors to I/O errors
			throw new IOException(String.format("Error mummifying page `%s`: %s", artifact.getSourcePath(), exception.getLocalizedMessage()), exception); //TODO i18n
		}

	}

	//#normalize

	/**
	 * Normalizes a document after it has been loaded, which includes the following:
	 * <ul>
	 * <li>Tidies the structure.</li>
	 * <li>Removes any named metadata; they will be regenerated later during mummification.</li>
	 * </ul>
	 * @implSpec This implementation does not allow the document element to be removed or replaced.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param document The document to normalize.
	 * @return The normalized document, which may or may not be the same document supplied as input.
	 * @throws IOException if there is an error normalizing the document.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected Document normalizeDocument(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Document document) throws IOException, DOMException {
		//**Do not call `document.normalizeDocument()`**, as it apparently tries to look up entities without using the document factory entity resolver,
		//causing the method to pause and potentially print error messages if entities cannot be found.
		//See note about `resource-resolver` parameter in `DOMConfiguration` if this needs to be investigated further.
		document.normalize();
		final Element documentElement = document.getDocumentElement();
		final List<Element> normalizedElements = normalizeElement(context, contextArtifact, artifact, documentElement);
		if(normalizedElements.size() != 1 || normalizedElements.get(0) != documentElement) {
			throw new UnsupportedOperationException("Document element cannot be removed or replaced when normalizing a document.");
		}
		return document;
	}

	/**
	 * Normalizes a document element, removing any named metadata (that is, {@value HTML#ELEMENT_META} elements with a {@value HTML#ELEMENT_META_ATTRIBUTE_NAME}
	 * or a {@value RDFa#ATTRIBUTE_PROPERTY} attribute).
	 * @implSpec This implementation marks for removal any {@value HTML#ELEMENT_META} elements with a {@value HTML#ELEMENT_META_ATTRIBUTE_NAME} or a
	 *           {@value RDFa#ATTRIBUTE_PROPERTY} attribute. It also removes all {@link RDFa#ATTRIBUTE_PREFIX} attributes.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param element The element to normalized.
	 * @return The normalized element(s), if any, to replace the source element.
	 * @throws IOException if there is an error normalizing the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected List<Element> normalizeElement(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Element element) throws IOException, DOMException {

		//remove the element itself if it is named metadata
		if(HTML.XHTML_NAMESPACE_URI_STRING.equals(element.getNamespaceURI()) && ELEMENT_META.equals(element.getLocalName())
				&& (element.hasAttributeNS(null, ELEMENT_META_ATTRIBUTE_NAME) || element.hasAttributeNS(null, RDFa.ATTRIBUTE_PROPERTY))) { //`<meta name="…">` or `<meta property="…">`
			return emptyList();
		}

		//remove all RDFa `prefix` attributes
		final Iterator<Attr> attrIterator = attributesIterator(element);
		while(attrIterator.hasNext()) {
			final Attr attr = attrIterator.next();
			if(attr.getNamespaceURI() == null && RDFa.ATTRIBUTE_PREFIX.equals(element.getLocalName())) { //prefix=
				attrIterator.remove();
			}
		}

		normalizeChildElements(context, contextArtifact, artifact, element);

		return List.of(element);
	}

	/**
	 * Normalizes child elements of an existing element.
	 * @implSpec Each child element is replaced with the normalized elements returned from calling
	 *           {@link #normalizeElement(MummyContext, Artifact, Artifact, Element)}. If only the same element is returned, no replacement is made. If no element
	 *           is returned, the source element is removed.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param element The element the children of which to normalize.
	 * @throws IOException if there is an error processing the child elements.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected void normalizeChildElements(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Element element) throws IOException, DOMException {
		final NodeList childNodes = element.getChildNodes();
		for(int childNodeIndex = 0; childNodeIndex < childNodes.getLength();) { //advance the index manually as needed
			final Node childNode = childNodes.item(childNodeIndex);
			if(!(childNode instanceof Element)) { //skip non-elements
				childNodeIndex++;
				continue;
			}
			final Element childElement = (Element)childNode;
			final List<Element> normalizedElements = normalizeElement(context, contextArtifact, artifact, childElement);
			replaceChild(element, childElement, normalizedElements);
			childNodeIndex += normalizedElements.size(); //manually advance the index based upon the replacement nodes
		}
	}

	//#apply template

	/**
	 * Applies a template if appropriate to a source document before it is processed.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param sourceDocument The source document to process.
	 * @return The document after applying a template, which may or may not be the same document supplied as input.
	 * @throws IllegalDataException if there is an error in the template or the source document preventing the template from being applied.
	 * @throws IOException if there is an error applying a template.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected Document applyTemplate(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Document sourceDocument) throws IOException, IllegalDataException, DOMException {
		return findTemplateSourceFile(context, contextArtifact, artifact, sourceDocument) //determine if there is a specified or appropriate template
				.flatMap(throwingFunction(templateSource -> { //try to apply the template
					final Path templateFile = templateSource.getKey();
					final PageMummifier templateMummifier = templateSource.getValue();
					getLogger().trace("  {*} found template: {}", templateFile);

					//#load and relocate the template document
					final Document templateDocument;
					{
						final Document sourceTemplateDocument = templateMummifier.loadSourceDocument(context, templateFile);
						//relocate the template links _within the source tree_ as if it were in the place of the artifact source
						templateDocument = relocateDocument(context, sourceTemplateDocument, templateFile,
								referentArtifact -> context.getPlan().referenceInSource(artifact, referentArtifact));
					}

					//1. validate structure
					findHtmlElement(templateDocument).orElseThrow(() -> new IOException(String.format("Template `%s` has no root `<html>` element.", templateFile)));

					// Do _not_ apply metadata. Metadata is now generated semantically from the actual description, which has already been loaded.

					//2. import/merge head information
					mergeHeadLinks(templateDocument, sourceDocument);
					importHeadScripts(templateDocument, sourceDocument);
					importHeadStyles(templateDocument, sourceDocument);

					//3. apply content

					final Element templateContentElement;
					final Optional<Element> foundSourceContentElement = findContentElement(sourceDocument);
					//if the content element is <frameset>, convert the template <body> element to <frameset>; <frameset> support may be removed in the future
					if(foundSourceContentElement.map(XHTML_ELEMENT_FRAMESET::matches).orElse(false)) {
						getLogger().warn("Source file `{}` uses obsolete, non-conforming `<frameset>`; may not be supported in the future.", artifact.getSourcePath());
						final Element templateBodyElement = findHtmlBodyElement(templateDocument)
								.orElseThrow(() -> new IOException(String.format("Template `%s` requires `<body>` element for applying `<frameset>`.", templateFile)));
						templateContentElement = templateDocument.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_FRAMESET); //substitute a <frameset> element for <body> in the template
						mergeAttributes(templateContentElement, templateBodyElement); //copy over the original template <body> attributes to the new <frameset>
						templateBodyElement.getParentNode().replaceChild(templateContentElement, templateBodyElement); //replace the template <body> with <frameset>
					} else { //in normal (non-frameset) cases we just use the template content element
						templateContentElement = findContentElement(templateDocument)
								.orElseThrow(() -> new IOException(String.format("Template `%s` has no content insertion point.", templateFile)));
						if(XHTML_ELEMENT_FRAMESET.matches(templateContentElement)) {
							throw new IOException(String.format("Template `%s` does not support `<frameset>`.", templateFile));
						}
					}
					foundSourceContentElement.ifPresentOrElse(sourceContentElement -> {
						getLogger().trace("  {*} applying source content");
						removeChildren(templateContentElement);
						mergeAttributes(templateContentElement, sourceContentElement);
						appendImportedChildNodes(templateContentElement, sourceContentElement);
					}, () -> getLogger().warn("Source file `{}` has no content to place in template.", artifact.getSourcePath()));
					return Optional.of(templateDocument);

				})).orElse(sourceDocument); //return the source document unchanged if we can't find a template
	}

	/**
	 * Finds the source file for a template, if there is one, for the given artifact. The template may be specified in the description of the document itself
	 * using the <code>mummy:template</code> property ({@link Artifact#PROPERTY_TAG_MUMMY_TEMPLATE}). Otherwise a search is made for a template file in the given
	 * artifact directory and ancestor directories.
	 * @apiNote The template property is used from the description, because the original {@code <meta>} elements will have been removed during normalization.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param sourceDocument The source document to process.
	 * @return The template file, if any, along with its mummifier.
	 * @throws IOException If there is an I/O error searching for a matching file.
	 * @throws IOException if the document specifies an invalid template file.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 * @see MummyContext#findPageSourceFile(Path, String, boolean)
	 * @see Artifact#PROPERTY_TAG_MUMMY_TEMPLATE
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_TEMPLATE_BASE_NAME
	 */
	protected Optional<Map.Entry<Path, PageMummifier>> findTemplateSourceFile(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact,
			@Nonnull final Artifact artifact, @Nonnull final Document sourceDocument) throws IOException, DOMException {
		//determine if a custom template file was specified; throw an exception if not in the source tree
		final Optional<String> customTemplate = artifact.getResourceDescription().findPropertyValue(PROPERTY_TAG_MUMMY_TEMPLATE).map(Object::toString);
		if(customTemplate.isPresent()) { //if a custom template was specified, check it and map it to a mummifier
			final Path artifactSourcePath = artifact.getSourcePath();
			try {
				return customTemplate.map(artifactSourcePath::resolve)//.map(Path::toAbsolutePath).map(context::checkArgumentSourcePath).map(Paths::checkArgumentExists)
						.flatMap(templateFile -> {
							if(templateFile.equals(artifactSourcePath)) { //if the artifact specifies itself as a template, that effectively means "no template"
								return Optional.empty();
							}
							if(!isDirectory(templateFile)) {
								throw new IllegalArgumentException(String.format("Template path %s cannot be a directory.", templateFile));
							}
							final Optional<PageMummifier> customTemplateMummifier = (templateFile.equals(artifactSourcePath) ? Optional.of(artifact.getMummifier())
									: context.findRegisteredMummifierForSourceFile(templateFile)).filter(PageMummifier.class::isInstance).map(PageMummifier.class::cast);
							return customTemplateMummifier.map(templateMummifier -> Map.entry(templateFile, templateMummifier));
						});
			} catch(final IllegalArgumentException illegalArgumentException) {
				throw new IOException(String.format("Source file `%s` specified invalid template `%s`: %s", artifact.getSourcePath(), customTemplate,
						illegalArgumentException.getLocalizedMessage()), illegalArgumentException); //TODO i18n
			}
		} else { //if no custom template was specified
			final String templateBaseName = context.getConfiguration().getString(CONFIG_KEY_MUMMY_TEMPLATE_BASE_NAME);
			return context.findPageSourceFile(artifact.getSourceDirectory(), templateBaseName, true); //look for a template
		}
	}

	/**
	 * Merges all {@code <head>} links from the source document into the template document. If a link with the same reference already exists in the template, it
	 * will not be replaced by that in the source.
	 * <p>
	 * This method assumes that the template has been relocated to the perfect location; otherwise, duplicate links will not be detected.
	 * </p>
	 * @apiNote The <a href="https://www.w3.org/TR/html52/semantics-scripting.html#the-script-element">HTML 5 specification</a> precludes a script element from
	 *          having both a link and content.
	 * @implSpec This implementation does not allow adding links already present in the template, even if the duplicate link is for a different type of content
	 *           (e.g. adding the same link as both a script and as a stylesheet).
	 * @implNote This implementation processes any reference element in {@link #HTML_REFERENCE_ELEMENT_ATTRIBUTES}, even though many of those elements would not
	 *           normally appear in a document {@code <head>} element.
	 * @param templateDocument The template into which the source document links are to be merged; must have a {@code <head>} element.
	 * @param sourceDocument The source document the links of which are being merged.
	 * @throws IllegalArgumentException if the template does not have a {@code <head>} element.
	 * @throws IllegalDataException if there is an error with the links being merged.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected void mergeHeadLinks(@Nonnull final Document templateDocument, @Nonnull final Document sourceDocument) throws IllegalDataException, DOMException {
		//find all the source document head link elements and map them to the links, but still maintain order
		final Map<URI, Element> sourceDocumentHeadLinkElementsByLink = findHtmlHeadElement(sourceDocument).stream().flatMap(XmlDom::childElementsOf)
				.filter(element -> XHTML_NAMESPACE_URI_STRING.equals(element.getNamespaceURI())).flatMap(element -> { //only look at HTML elements
					return Optional.ofNullable(HTML_REFERENCE_ELEMENT_ATTRIBUTES.get(element.getLocalName())) //see if it is a reference element and get its reference attribute name
							.flatMap(referenceAttributeName -> findAttributeNS(element, null, referenceAttributeName).flatMap(referenceString -> {
								try { //convert any reference to a URI and normalize it
									final URI referenceURI = new URI(referenceString).normalize();
									return Optional.of(Map.entry(referenceURI, element));
								} catch(final URISyntaxException uriSyntaxException) {
									getLogger().warn("Invalid page `<head><{}>` reference {}: {}", element.getLocalName(), referenceString,
											uriSyntaxException.getLocalizedMessage());
									return Optional.empty();
								}
							})).stream();
				}).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (first, duplicate) -> {
					getLogger().warn("Ignoring duplicate page `<head>` reference `{}`.", duplicate);
					return first;
				}, LinkedHashMap::new));

		//consider the page links already "encountered" as they have already been deduped and we already know we will use them
		final Set<URI> encounteredLinks = new HashSet<>(sourceDocumentHeadLinkElementsByLink.keySet());

		//merge in our gathered source document links
		final Element templateHeadElement = findHtmlHeadElement(templateDocument)
				.orElseThrow(() -> new IllegalArgumentException("Template missing <head> element."));
		Node templateHeadChildNode = templateHeadElement.getFirstChild(); //this acts as a cursor through the children
		while(templateHeadChildNode != null) {
			if(templateHeadChildNode instanceof Element) {
				final Element templateHeadChildElement = (Element)templateHeadChildNode;
				if(XHTML_NAMESPACE_URI_STRING.equals(templateHeadChildElement.getNamespaceURI())) {
					final String referenceAttributeName = HTML_REFERENCE_ELEMENT_ATTRIBUTES.get(templateHeadChildElement.getLocalName());
					if(referenceAttributeName != null) {
						final String referenceString = findAttributeNS(templateHeadChildElement, null, referenceAttributeName).orElse(null);
						if(referenceString != null) {
							final URI referenceURI;
							try {
								referenceURI = new URI(referenceString).normalize();
							} catch(final URISyntaxException uriSyntaxException) {
								throw new IllegalDataException(String.format("Invalid template <head><%s> reference %s: %s", templateHeadChildElement.getLocalName(),
										referenceString, uriSyntaxException.getLocalizedMessage()));
							}
							//we'll eventually remove this element if it's a template duplicate or if it is present in the source document
							final boolean isToRemove = encounteredLinks.contains(referenceURI);
							encounteredLinks.add(referenceURI); //make note of the link for future deduping
							if(sourceDocumentHeadLinkElementsByLink.containsKey(referenceURI)) { //if the source document has this link
								//merge in all source links up to and including this link
								final Iterator<Map.Entry<URI, Element>> sourceLinkEntryIterator = sourceDocumentHeadLinkElementsByLink.entrySet().iterator();
								while(sourceLinkEntryIterator.hasNext()) {
									final Map.Entry<URI, Element> sourceLinkEntry = sourceLinkEntryIterator.next();
									final Element sourceLinkElement = sourceLinkEntry.getValue();
									//import an insert the link before this link
									templateHeadElement.insertBefore(templateDocument.importNode(sourceLinkElement, true), templateHeadChildElement);
									sourceLinkEntryIterator.remove(); //remove the record of the source link so we won't use it again
									if(sourceLinkEntry.getKey().equals(referenceURI)) { //stop when we get to the same source document link as this one
										break;
									}
								}
							}
							if(isToRemove) { //remove the template head element if marked for removal
								final Node remainingNode = templateHeadChildNode.getPreviousSibling();
								//if we are removing a node, it was duplicated in the template or we added in source document links before it
								assert remainingNode != null : "No remaining previous node when removing duplicate template link after merge.";
								templateHeadElement.removeChild(templateHeadChildNode);
								templateHeadChildNode = remainingNode; //place the "cursor" on a valid node
							}
						}
					}
				}
			}
			templateHeadChildNode = templateHeadChildNode.getNextSibling();
		}
		//add any remaining source document links that weren't merged in
		sourceDocumentHeadLinkElementsByLink.values().forEach(element -> templateHeadElement.appendChild(templateDocument.importNode(element, true)));
	}

	/**
	 * Imports all {@code <head><script>} elements that contain internal scripts (not links) from the source document into the template document. No checks are
	 * made for duplicate content.
	 * @apiNote The <a href="https://www.w3.org/TR/html52/semantics-scripting.html#the-script-element">HTML 5 specification</a> precludes a script element from
	 *          having both a link and content.
	 * @param templateDocument The template into which the source document scripts are to be merged; must have a {@code <head>} element.
	 * @param sourceDocument The source document from which the scripts are being imported.
	 * @throws IllegalArgumentException if the template does not have a {@code <head>} element.
	 * @throws IllegalDataException if there is an error importing the scripts.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 * @see <a href="https://www.w3.org/TR/html52/semantics-scripting.html#the-script-element">HTML 5.2 § 4.12.1. The script element</a>
	 */
	protected void importHeadScripts(@Nonnull final Document templateDocument, @Nonnull final Document sourceDocument) throws IllegalDataException, DOMException {
		final Element templateHeadElement = findHtmlHeadElement(templateDocument)
				.orElseThrow(() -> new IllegalArgumentException("Template missing <head> element."));
		findHtmlHeadElement(sourceDocument).stream().flatMap(XmlDom::childElementsOf).filter(XHTML_ELEMENT_SCRIPT::matches) //find all <script> elements
				.filter(element -> !XmlDom.hasAttribute(element, XHTML_ELEMENT_SCRIPT_ATTRIBUTE_SRC)) //don't include scripts with `src` attributes
				.forEach(element -> templateHeadElement.appendChild(templateDocument.importNode(element, true)));
	}

	/**
	 * Imports all {@code <head><style>} elements that contain internal styles from the source document into the template document. No checks are made for
	 * duplicate content.
	 * @param templateDocument The template into which the source document styles are to be merged; must have a {@code <head>} element.
	 * @param sourceDocument The source document from which the styles are being imported.
	 * @throws IllegalArgumentException if the template does not have a {@code <head>} element.
	 * @throws IllegalDataException if there is an error importing the styles.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 * @see <a href="https://www.w3.org/TR/html52/document-metadata.html#the-style-element">HTML 5.2 § 4.2.6. The style element</a>
	 */
	protected void importHeadStyles(@Nonnull final Document templateDocument, @Nonnull final Document sourceDocument) throws IllegalDataException, DOMException {
		final Element templateHeadElement = findHtmlHeadElement(templateDocument)
				.orElseThrow(() -> new IllegalArgumentException("Template missing <head> element."));
		findHtmlHeadElement(sourceDocument).stream().flatMap(XmlDom::childElementsOf).filter(XHTML_ELEMENT_STYLE::matches) //find all <style> elements
				.forEach(element -> templateHeadElement.appendChild(templateDocument.importNode(element, true)));
	}

	/**
	 * Attempts to determine which element in the source document contains the content of the document, to be inserted into a template, for example.
	 * @implSpec This implementation finds the content element in the following order:
	 *           <ol>
	 *           <li>The {@code <html><body><main>} element, if present.</li>
	 *           <li>The {@code <html><body><article>} element, if present.</li>
	 *           <li>The {@code <html><body>} element, if present.</li>
	 *           <li>The {@code <html><frameset>} element, if present.</li>
	 *           </ol>
	 * @implNote This method may return the {@code <frameset>} element, which is obsolete and should no longer be used in content. This element is not guaranteed
	 *           to work in all contexts for which a content element might be used; it is up to the caller to check and provide the appropriate warning or error
	 *           if it does not support {@code <frameset>}. See <a href="https://www.w3.org/TR/html52/obsolete.html#frames">HTML 5.2 § 11.3.3. Frames</a>.
	 * @param sourceDocument The document containing source content.
	 * @return The element containing content in the source document.
	 */
	protected Optional<Element> findContentElement(@Nonnull final Document sourceDocument) {
		final Optional<Element> htmlBodyElement = findHtmlBodyElement(sourceDocument);
		//TODO add support for <mummy:content>
		return htmlBodyElement.flatMap(htmlElement -> findFirstChildElementByNameNS(htmlElement, XHTML_NAMESPACE_URI_STRING, ELEMENT_MAIN)) //`<html><body><main>` gets priority
				//then `<html><body><article>`
				.or(() -> htmlBodyElement.flatMap(htmlElement -> findFirstChildElementByNameNS(htmlElement, XHTML_NAMESPACE_URI_STRING, ELEMENT_ARTICLE)))
				//then `<html><body>`
				.or(() -> htmlBodyElement)
				//then finally the obsolete `<html><frameset>`
				.or(() -> findHtmlElement(sourceDocument)
						.flatMap(htmlElement -> findFirstChildElementByNameNS(htmlElement, XHTML_NAMESPACE_URI_STRING, ELEMENT_FRAMESET)));
	}

	//#process

	/**
	 * Processes a source document before it is converted to an output document.
	 * @implSpec This implementation does not allow the document element to be removed or replaced.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param sourceDocument The source document to process.
	 * @return The processed document, which may or may not be the same document supplied as input.
	 * @throws IllegalArgumentException if the elements have some information that cannot be processed.
	 * @throws IOException if there is an error processing the document.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected Document processDocument(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Document sourceDocument) throws IOException, DOMException {
		final List<Element> processedElements = processElement(context, contextArtifact, artifact, sourceDocument.getDocumentElement());
		if(processedElements.size() != 1 || processedElements.get(0) != sourceDocument.getDocumentElement()) {
			throw new UnsupportedOperationException("Document element cannot be removed or replaced when processing a document.");
		}
		return sourceDocument;
	}

	/**
	 * Processes an element in the source document.
	 * @implSpec This implementation handles:
	 *           <ul>
	 *           <li>Processing of registered widgets.</li>
	 *           <li>Regeneration of navigation lists using {@link PageMummifier#ATTRIBUTE_REGENERATE} via
	 *           {@link #regenerateNavigationList(MummyContext, Artifact, Artifact, Element)}.</li>
	 *           </ul>
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param sourceElement The source element to process.
	 * @return The processed element(s), if any, to replace the source element.
	 * @throws IllegalArgumentException if the element has some information that cannot be processed.
	 * @throws IOException if there is an error processing the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected List<Element> processElement(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Element sourceElement) throws IOException, DOMException {

		//TODO transfer to some system of pluggable element processing strategies

		//widgets
		final Widget widget = WIDGETS_BY_ELEMENT_NAME.get(NsName.ofNode(sourceElement));
		if(widget != null) {
			try {
				return widget.processElement(this, context, contextArtifact, artifact, sourceElement);
			} catch(final IllegalDataException illegalDataException) { //make widget illegal argument errors more useful
				throw new IOException(String.format("Invalid data for widget `%s` in `%s`: %s", widget.getWidgetElementName(), artifact.getSourceDirectory(),
						illegalDataException.getLocalizedMessage()), illegalDataException); //TODO i18n
			}
		}

		if(GuiseMummy.NAMESPACE_STRING.equals(sourceElement.getNamespaceURI())) {
			getLogger().warn("Unrecognized Guise Mummy element `<{}>`.", sourceElement.getNodeName());
		}

		//navigation list regeneration
		if(isPresentAndEquals(findAttribute(sourceElement, ATTRIBUTE_REGENERATE), ATTRIBUTE_REGENERATE.getLocalName())) {

			//<nav><ol> or <nav><ul>
			if(XHTML_NAMESPACE_URI_STRING.equals(sourceElement.getNamespaceURI())) {
				if(ELEMENT_OL.equals(sourceElement.getLocalName()) || ELEMENT_UL.equals(sourceElement.getLocalName())) { //<ol> or <ul>

					if(hasAncestorElementNS(sourceElement, XHTML_NAMESPACE_URI_STRING, ELEMENT_NAV)) { //if this is a navigation list
						return regenerateNavigationList(context, contextArtifact, artifact, sourceElement);
					}
				}
			}

		}

		processChildElements(context, contextArtifact, artifact, sourceElement);

		return List.of(sourceElement);
	}

	/**
	 * Processes child elements of an existing element.
	 * @implSpec Each child element is replaced with the processed elements returned from calling
	 *           {@link #processElement(MummyContext, Artifact, Artifact, Element)}. If only the same element is returned, no replacement is made. If no element
	 *           is returned, the source element is removed.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param sourceElement The source element the children of which to process.
	 * @throws IllegalArgumentException if the elements have some information that cannot be processed.
	 * @throws IOException if there is an error processing the child elements.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected void processChildElements(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Element sourceElement) throws IOException, DOMException {
		final NodeList childNodes = sourceElement.getChildNodes();
		for(int childNodeIndex = 0; childNodeIndex < childNodes.getLength();) { //advance the index manually as needed
			final Node childNode = childNodes.item(childNodeIndex);
			if(!(childNode instanceof Element)) { //skip non-elements
				childNodeIndex++;
				continue;
			}
			final Element childElement = (Element)childNode;
			final List<Element> processedElements = processElement(context, contextArtifact, artifact, childElement);
			replaceChild(sourceElement, childElement, processedElements);
			childNodeIndex += processedElements.size(); //manually advance the index based upon the replacement nodes
		}
	}

	/**
	 * Returns the given object as a {@link Long}, converting if necessary.
	 * @apiNote This conversion is necessary because for Markdown+YAML may encode e.g. the {@link Artifact#PROPERTY_TAG_MUMMY_ORDER} property as an
	 *          {@link Integer} value, while parsing the same property from XHTML with knowledge of the Mummy ontology may result in a {@link Long} value.
	 * @implSpec This implementation only supports {@link Integer} and {@link Long} types.
	 * @param object The object to return as a {@link Long}.
	 * @return The object as a {@link Long} instance.
	 * @throws IllegalArgumentException if the given object cannot be converted to a {@link Long}.
	 */
	private static Long toLong(@Nonnull final Object object) { //TODO switch to a general converter system, including number types, e.g. from Ploop
		if(object instanceof Long) {
			return (Long)object;
		} else if(object instanceof Integer) {
			return Long.valueOf(((Integer)object).longValue());
		} else {
			throw new IllegalArgumentException(
					String.format("Cannot convert object %s of type %s to type %s.", object, object.getClass().getSimpleName(), Long.class.getSimpleName()));
		}
	}

	/**
	 * The set of <a href="https://fontawesome.com/">Font Awesome</a> icon groups.
	 * @see <a href="https://fontawesome.com/how-to-use">Font Awesome Basic Use</a>
	 */
	private final static Set<String> FONT_AWESOME_ICON_GROUPS = Set.of("fas", "far", "fal", "fad", "fab");

	/**
	 * Regenerates a navigation list based upon the parent, sibling, and/or child navigation artifacts relative to the context artifact.
	 * <p>
	 * This method discovers a link to serve as the template for the generated links. If the link has a reference to the template itself (preferably by simply
	 * using <code>href=""</code>), the link is used as the template for active links; that is, self-links. Any other link (which is not ever required to have an
	 * <code>href</code> attribute) is used as the template for all other links.
	 * </p>
	 * @implSpec This implementation calls {@link #generateNavigationList(MummyContext, Artifact, Artifact, Element, Element, Stream)} for each menu level. See
	 *           that method for a specification of how link templates work.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param navigationListElement The list element to regenerate.
	 * @return The processed element(s), if any, to replace the source element.
	 * @throws IllegalArgumentException if the information of the navigation artifacts prevent them from being ordered.
	 * @throws IOException if there is an error processing the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 * @see #navigation(MummyContext, Artifact, Artifact)
	 * @see #generateNavigationList(MummyContext, Artifact, Artifact, Element, Element, Stream)
	 */
	protected List<Element> regenerateNavigationList(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Element navigationListElement) throws IOException, DOMException {

		//determine the templates
		Element discoveredActiveLiTemplate = null;
		Element discoveredInactiveLiTemplate = null;

		for(final Element liElement : (Iterable<Element>)streamOf(navigationListElement.getElementsByTagNameNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI))
				.map(Element.class::cast)::iterator) {
			//find <li><a href>
			final Optional<String> href = findFirst(liElement.getElementsByTagNameNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_A)).map(Element.class::cast)
					.flatMap(aElement -> findAttributeNS(aElement, null, ELEMENT_A_ATTRIBUTE_HREF));
			if(href.isPresent() && href.get().isEmpty()) { //an explicit href="" indicates "this item" (a self link)
				if(discoveredActiveLiTemplate == null) { //the first example wins
					discoveredActiveLiTemplate = liElement;
				}
			} else {
				if(discoveredInactiveLiTemplate == null) { //the first example wins
					discoveredInactiveLiTemplate = liElement;
				}
			}
			if(discoveredActiveLiTemplate != null && discoveredInactiveLiTemplate != null) { //stop searching when we find both templates
				break;
			}
		}

		final Element inactiveLiTemplate;
		if(discoveredInactiveLiTemplate != null) { //use the discovered inactive template if we found one
			inactiveLiTemplate = discoveredInactiveLiTemplate;
		} else {
			if(discoveredActiveLiTemplate != null) { //use the active template if present
				inactiveLiTemplate = discoveredActiveLiTemplate;
			} else { //otherwise create a default inactive template
				inactiveLiTemplate = navigationListElement.getOwnerDocument().createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_LI); //<li>
				inactiveLiTemplate.appendChild(navigationListElement.getOwnerDocument().createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_A)); //<a>
			}
		}
		assert inactiveLiTemplate != null;

		//if there is no inactive example, use the inactive template for active links as well
		final Element activeLiTemplate = discoveredActiveLiTemplate != null ? discoveredActiveLiTemplate : inactiveLiTemplate;
		assert activeLiTemplate != null;

		removeChildren(navigationListElement); //remove existing links

		//regenerate the list and add the new list elements to the navigation list element
		generateNavigationList(context, contextArtifact, artifact, inactiveLiTemplate, activeLiTemplate, navigation(context, contextArtifact, artifact))
				.forEach(navigationListElement::appendChild);

		return List.of(navigationListElement);
	}

	/**
	 * Recursively regenerates a navigation list for the given navigation items based following the the given link templates.
	 * <ul>
	 * <li>Within each <code>&lt;li&gt;</code> element, the first {@code <i></i>} element is considered to be a placeholder for an icon. If the navigation
	 * artifact has an {@value Artifact#PROPERTY_HANDLE_ICON} property, it is replaced with a {@code <span></span>}; otherwise it is removed. The icon property
	 * value is expected to be in the form <code><var>group</var>/<var>name</var></code> form, and based upon the specific group the {@code <span>}
	 * <code>class</code> attribute and content will be updated appropriately. If the icon identification format isn't recognized, the literal value will be used
	 * as the text content of the {@code <span>}.</li>
	 * <li>Within each <code>&lt;li&gt;</code> element, the first {@code <a></a>} element is considered to be a placeholder for the link. All of its text is
	 * removed (leaving the icon, if any), and the result of {@link Artifact#determineLabel()} will be appended as text for the link label.</li>
	 * </ul>
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param inactiveLiTemplate The element to serve as a template for generating "normal", non-active items.
	 * @param activeLiTemplate The element to serve as a template for generating active items, those item that reference the context artifact.
	 * @param navigation The navigation items for which to generate list item elements.
	 * @return The elements generated for the list items.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected Stream<Element> generateNavigationList(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Element inactiveLiTemplate, @Nonnull final Element activeLiTemplate, @Nonnull final Stream<NavigationItem> navigation)
			throws DOMException {
		return navigation.map(navigationItem -> { //generate navigation elements 
			//see if the href is a relative link back to this artifact, and if so use the template for an active link;
			final boolean isSelfHref = navigationItem.findHref().map(URI::create).filter(not(URI::isAbsolute)) //assume absolute (external) URIs do not reference this artifact
					.filter(not(uri -> uri.getRawFragment() != null)) //ignore fragment references; the static page doesn't know when/if the browser includes the fragment
					.flatMap(URIs::findURIPath).flatMap(relativeReference -> context.getPlan().findArtifactBySourceRelativeReference(contextArtifact, relativeReference))
					.map(contextArtifact::equals).orElse(false);
			final Element liTemplate = isSelfHref ? activeLiTemplate : inactiveLiTemplate;
			final Element liElement = (Element)liTemplate.cloneNode(true);
			//update the icon: <li><i> (transform to <span></span>)
			findFirst(liElement.getElementsByTagNameNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_I)).map(Element.class::cast).ifPresent(iElement -> {
				//if the navigation element has an icon, replace the `<i></i>` with an icon `<span></span>` 
				navigationItem.findIconId().ifPresentOrElse(iconId -> {
					final String iconClass;
					final String iconContent;
					final String[] iconIdParts = iconId.split("/", -1); //TODO use constant
					if(iconIdParts.length == 2 && !iconIdParts[0].isBlank() && !iconIdParts[1].isBlank()) {
						final String iconGroup = iconIdParts[0];
						final String iconName = iconIdParts[1];
						if(FONT_AWESOME_ICON_GROUPS.contains(iconGroup)) {
							iconClass = iconGroup + ' ' + iconName; //e.g. `<span class="fas fa-home"></span>` (Font Awesome)
							iconContent = null;
						} else {
							iconClass = iconGroup; //e.g. `<span class="material-icons">home</span>` (Material Icons)
							iconContent = iconName;
						}
					} else { //if the icon name isn't in the format we expect, just use it as the content
						iconClass = null;
						iconContent = iconId;
					}
					final Element iconElement = iElement.getOwnerDocument().createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_SPAN);
					if(iconClass != null) {
						iconElement.setAttributeNS(null, ATTRIBUTE_CLASS, iconClass);
					}
					if(iconContent != null) {
						appendText(iconElement, iconContent);
					}
					iElement.getParentNode().replaceChild(iconElement, iElement);
				}, () -> iElement.getParentNode().removeChild(iElement)); //if the navigation element has no icon, remove the `<i></i>`
			});
			//update the link: <li><a>
			findFirst(liElement.getElementsByTagNameNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_A)).map(Element.class::cast).ifPresent(aElement -> {
				navigationItem.findHref().ifPresentOrElse(href -> aElement.setAttributeNS(null, ELEMENT_A_ATTRIBUTE_HREF, href),
						() -> aElement.removeAttributeNS(null, ELEMENT_A_ATTRIBUTE_HREF));
				//remove text nodes (leaving the icon or any other element)
				final Iterator<Node> childNodeIterator = XmlDom.childNodesIterator(aElement);
				while(childNodeIterator.hasNext()) {
					final Node childNode = childNodeIterator.next();
					if(childNode.getNodeType() == Node.TEXT_NODE) {
						childNodeIterator.remove();
					}
				}
				final String navigationLabel = navigationItem.getLabel();
				final String linkLabel = aElement.getChildNodes().getLength() > 0 ? " " + navigationLabel : navigationLabel; //add spacing if there are other elements (e.g. an icon)
				//append the link label
				appendText(aElement, linkLabel);
			});
			//recursively add child lists as needed: <li><ul>
			final List<NavigationItem> childNavigation = navigationItem.getNavigation();
			if(!childNavigation.isEmpty()) { //don't even add a sublist if there are no child navigation items
				final Element ulElement = liElement.getOwnerDocument().createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_UL);
				//generate navigation list elements and add them to the sublist element
				generateNavigationList(context, contextArtifact, artifact, inactiveLiTemplate, activeLiTemplate, childNavigation.stream())
						.forEach(ulElement::appendChild);
				liElement.appendChild(ulElement);
			}
			return liElement;
		});
	}

	//#relocate

	/**
	 * Relocates a document by retargeting its references from the artifact source path to the artifact target path.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param sourceDocument The source document to relocate.
	 * @return The relocated document, which may or may not be the same document supplied as input.
	 * @throws IOException if there is an error relocating the document.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected Document relocateSourceDocumentToTarget(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Document sourceDocument) throws IOException, DOMException {
		return relocateDocument(context, sourceDocument, contextArtifact.getSourcePath(),
				referentArtifact -> context.getPlan().referenceInTarget(artifact, referentArtifact));
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation does not allow the document element to be removed or replaced.
	 */
	@Override
	public Document relocateDocument(@Nonnull MummyContext context, @Nonnull final Document sourceDocument, @Nonnull final Path originalReferrerSourcePath,
			final Function<Artifact, URIPath> referenceGenerator) throws IOException, DOMException {
		final List<Element> relocatedElements = relocateElement(context, sourceDocument.getDocumentElement(), originalReferrerSourcePath, referenceGenerator);
		if(relocatedElements.size() != 1 || relocatedElements.get(0) != sourceDocument.getDocumentElement()) {
			throw new UnsupportedOperationException("Document element cannot be removed or replaced when relocating a document.");
		}
		return sourceDocument;
	}

	/**
	 * Relocates a source document element by retargeting its references relative to a new referrer path location.
	 * @implSpec This implementation relocates the {@link #HTML_REFERENCE_ELEMENT_ATTRIBUTES} elements and attributes.
	 * @param context The context of static site generation.
	 * @param sourceElement The source element to relocate.
	 * @param originalReferrerSourcePath The absolute original path of the referrer, e.g. <code>…/foo/page.xhtml</code>.
	 * @param referenceGenerator The function for generating a reference to the artifact indicated by the reference path resolved to the original path.
	 * @return The relocated element(s), if any, to replace the source element.
	 * @throws IOException if there is an error relocating the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 * @see #HTML_REFERENCE_ELEMENT_ATTRIBUTES
	 */
	protected List<Element> relocateElement(@Nonnull MummyContext context, @Nonnull final Element sourceElement, @Nonnull final Path originalReferrerSourcePath,
			final Function<Artifact, URIPath> referenceGenerator) throws IOException, DOMException {

		//TODO transfer to some system of pluggable element relocating strategies
		if(XHTML_NAMESPACE_URI_STRING.equals(sourceElement.getNamespaceURI())) {
			//see if this is a referrer element, and get the attribute doing the referencing
			final String referenceAttributeName = HTML_REFERENCE_ELEMENT_ATTRIBUTES.get(sourceElement.getLocalName());
			if(referenceAttributeName != null) {
				return relocateReferenceElement(context, sourceElement, referenceAttributeName, originalReferrerSourcePath, referenceGenerator);
			}
		}

		relocateChildElements(context, sourceElement, originalReferrerSourcePath, referenceGenerator);

		return List.of(sourceElement);
	}

	/**
	 * Relocates child elements of an existing element by retargeting references relative to a new referrer path location.
	 * @implSpec Each child element is replaced with the relocated elements returned from calling {@link #relocateElement(MummyContext, Element, Path, Function)}.
	 *           If only the same element is returned, no replacement is made. If no element is returned, the source element is removed.
	 * @param context The context of static site generation.
	 * @param sourceElement The source element the children of which to relocate.
	 * @param originalReferrerSourcePath The absolute original path of the referrer, e.g. <code>…/foo/page.xhtml</code>.
	 * @param referenceGenerator The function for generating a reference to the artifact indicated by the reference path resolved to the original path.
	 * @throws IOException if there is an error relocating the child elements.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected void relocateChildElements(@Nonnull MummyContext context, @Nonnull final Element sourceElement, @Nonnull final Path originalReferrerSourcePath,
			final Function<Artifact, URIPath> referenceGenerator) throws IOException, DOMException {
		final NodeList childNodes = sourceElement.getChildNodes();
		for(int childNodeIndex = 0; childNodeIndex < childNodes.getLength();) { //advance the index manually as needed
			final Node childNode = childNodes.item(childNodeIndex);
			if(!(childNode instanceof Element)) { //skip non-elements
				childNodeIndex++;
				continue;
			}
			final Element childElement = (Element)childNode;
			final List<Element> relocatedElements = relocateElement(context, childElement, originalReferrerSourcePath, referenceGenerator);
			replaceChild(sourceElement, childElement, relocatedElements);
			childNodeIndex += relocatedElements.size(); //manually advance the index based upon the replacement nodes
		}
	}

	/**
	 * Relocates a reference element by retargeting its reference attribute relative to a new referrer path location.
	 * <p>
	 * A reference to <code>""</code> is considered to be a relative self reference as per RFC 3986, and is never modified during relocation, as it is always
	 * inherently "relocated" regardless of the resource location.
	 * </p>
	 * @param context The context of static site generation.
	 * @param referenceElement The reference element such a {@code <a>} to relocate.
	 * @param referenceAttributeName The name of the reference attribute such a {@code href} to relocate.
	 * @param originalReferrerSourcePath The absolute original path of the referrer, e.g. <code>…/foo/page.xhtml</code>.
	 * @param referenceGenerator The function for generating a reference to the artifact indicated by the reference path resolved to the original path.
	 * @return The relocated element(s), if any, to replace the source element.
	 * @throws IOException if there is an error relocating the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 * @see #retargetResourceReference(MummyContext, URI, Path, Function)
	 */
	protected List<Element> relocateReferenceElement(@Nonnull MummyContext context, @Nonnull final Element referenceElement,
			@Nonnull final String referenceAttributeName, @Nonnull final Path originalReferrerSourcePath, final Function<Artifact, URIPath> referenceGenerator)
			throws IOException, DOMException {
		findAttributeNS(referenceElement, null, referenceAttributeName).ifPresent(referenceString -> {
			getLogger().trace("  - found reference <{} {}=\"{}\" ...>", referenceElement.getNodeName(), referenceAttributeName, referenceString);
			//TODO check for the empty string and do something appropriate
			final URI referenceURI;
			try {
				referenceURI = new URI(referenceString);
				if(!referenceURI.isAbsolute()) { //only convert paths
					final String referencePath = referenceURI.getRawPath();
					if(referencePath != null && !referencePath.isEmpty() && !URIs.isPathAbsolute(referencePath)) { //only convert relative paths that are not self-references ("")
						retargetResourceReference(context, referenceURI, originalReferrerSourcePath, referenceGenerator).ifPresentOrElse(retargetedResourceReference -> {
							getLogger().trace("  -> mapping to : {}", retargetedResourceReference);
							referenceElement.setAttributeNS(null, referenceAttributeName, retargetedResourceReference.toString());
						}, () -> getLogger().warn("No target artifact found for source relative reference `{}` in `{}`.", referenceURI, originalReferrerSourcePath));
					}
				}
			} catch(final URISyntaxException uriSyntaxException) {
				getLogger().warn("Invalid reference `<{} {}=\"{}\" ...>` in `{}`: {}", referenceElement.getNodeName(), referenceAttributeName, referenceString,
						originalReferrerSourcePath, uriSyntaxException.getLocalizedMessage()); //TODO i18n
			}
		});
		return List.of(referenceElement);
	}

	/**
	 * Retargets a relative resource reference after relocating the referring source path to a new location, based upon the determined path of the referent
	 * artifact.
	 * <p>
	 * This method supports relocating within the source tree or from the source tree to the target tree.
	 * </p>
	 * @apiNote This method supports non-path-only references, such as <code>example?foo</code>, <code>example#bar</code>, and <code>example?foo#bar</code>.
	 * @param context The context of static site generation.
	 * @param resourceReference The relative resource reference, e.g. <code>example/test.txt?foo#bar</code>.
	 * @param originalReferrerSourcePath The absolute original path of the referrer, e.g. <code>…/foo/page.xhtml</code>.
	 * @param referenceGenerator The function for generating a reference to the artifact indicated by the reference path resolved to the original path.
	 * @return The relative reference to the original artifact, now relativized to the relocated referrer path, e.g. <code>../bar/example/test.txt?foo#bar</code>.
	 * @throws IllegalArgumentException if the given reference has no path or its path is absolute.
	 * @throws IllegalArgumentException if the original referrer source path is not absolute and/or is not within the site source tree.
	 * @throws IllegalArgumentException if the relocated referred path is not in the site source or target tree.
	 * @throws IllegalArgumentException if the referent artifact path is not in the same source/target tree as the relocated referrer path.
	 */
	protected Optional<URI> retargetResourceReference(@Nonnull MummyContext context, @Nonnull URI resourceReference,
			@Nonnull final Path originalReferrerSourcePath, final Function<Artifact, URIPath> referenceGenerator) {
		final URIPath resourceReferencePath = URIs.findURIPath(resourceReference)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Resource reference %s has no path.", resourceReference)));
		return retargetResourceReferencePath(context, resourceReferencePath, originalReferrerSourcePath, referenceGenerator) //retarget the path separately
				.map(retargetedResourceReferencePath -> URIs.changePath(resourceReference, retargetedResourceReferencePath)); //switch the path of the original reference
	}

	/**
	 * Retargets a relative resource reference path after relocating the referring source path to a new location, based upon the determined path of the referent
	 * artifact.
	 * <p>
	 * This method supports relocating within the source tree or from the source tree to the target tree.
	 * </p>
	 * @apiNote This method does <em>not</em> support non-path-only references, such as <code>example?foo</code>, <code>example#bar</code>, and
	 *          <code>example?foo#bar</code>.
	 * @param context The context of static site generation.
	 * @param resourceReferencePath The relative resource reference path, e.g. <code>example/test.txt</code>.
	 * @param originalReferrerSourcePath The absolute original path of the referrer, e.g. <code>…/foo/page.xhtml</code>.
	 * @param referenceGenerator The function for generating a reference to the artifact indicated by the reference path resolved to the original path.
	 * @return The relative path to the original artifact, now relativized to the relocated referrer path, e.g. <code>../bar/example/test.txt</code>.
	 * @throws IllegalArgumentException if the given reference path is absolute.
	 * @throws IllegalArgumentException if the original referrer source path is not absolute and/or is not within the site source tree.
	 * @throws IllegalArgumentException if the relocated referred path is not in the site source or target tree.
	 * @throws IllegalArgumentException if the referent artifact path is not in the same source/target tree as the relocated referrer path.
	 */
	protected Optional<URIPath> retargetResourceReferencePath(@Nonnull MummyContext context, @Nonnull URIPath resourceReferencePath,
			@Nonnull final Path originalReferrerSourcePath, final Function<Artifact, URIPath> referenceGenerator) {
		return context.getPlan().findArtifactBySourceRelativeReference(context.checkArgumentSourcePath(originalReferrerSourcePath), resourceReferencePath)
				.map(referenceGenerator::apply);
	}

	//#cleanse

	/**
	 * Cleanses a document before it is saved, removing any Mummy-related directives.
	 * @implSpec This implementation does not allow the document element to be removed or replaced.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param document The document to cleanse.
	 * @return The cleansed document, which may or may not be the same document supplied as input.
	 * @throws IOException if there is an error cleansing the document.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected Document cleanseDocument(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Document document) throws IOException, DOMException {
		final List<Element> cleansedElements = cleanseElement(context, contextArtifact, artifact, document.getDocumentElement());
		if(cleansedElements.size() != 1 || cleansedElements.get(0) != document.getDocumentElement()) {
			throw new UnsupportedOperationException("Document element cannot be removed or replaced when cleansing a document.");
		}
		return document;
	}

	/**
	 * Cleanses a document element, removing any Mummy-related directives.
	 * @implSpec This implementation marks for removal any element in the {@link GuiseMummy#NAMESPACE} namespace, and for all other elements removes all
	 *           attributes in the {@link GuiseMummy#NAMESPACE} namespace.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param element The element to cleanse.
	 * @return The cleansed element(s), if any, to replace the source element.
	 * @throws IOException if there is an error cleansing the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 * @see GuiseMummy#NAMESPACE
	 */
	protected List<Element> cleanseElement(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Element element) throws IOException, DOMException {

		//remove the element itself if it is in the Guise Mummy namespace
		if(GuiseMummy.NAMESPACE_STRING.equals(element.getNamespaceURI())) { //<mummy:*>
			return emptyList();
		}

		//remove all attributes in the Guise Mummy namespace and Guise Mummy namespace declarations
		final Iterator<Attr> attrIterator = attributesIterator(element);
		while(attrIterator.hasNext()) {
			final Attr attr = attrIterator.next();

			if(XML.XMLNS_NAMESPACE_URI_STRING.equals(attr.getNamespaceURI())) { //xmlns:*
				if(GuiseMummy.NAMESPACE_STRING.equals(attr.getValue())) { //xmlns:mummy
					attrIterator.remove();
				}
			}

			if(GuiseMummy.NAMESPACE_STRING.equals(attr.getNamespaceURI())) { //mummy:*
				attrIterator.remove();
			}
		}

		cleanseChildElements(context, contextArtifact, artifact, element);

		return List.of(element);
	}

	/**
	 * Cleanses child elements of an existing element, removing any Mummy-related directives.
	 * @implSpec Each child element is replaced with the cleansed elements returned from calling
	 *           {@link #cleanseElement(MummyContext, Artifact, Artifact, Element)}. If only the same element is returned, no replacement is made. If no element
	 *           is returned, the source element is removed.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param element The element the children of which to cleanse.
	 * @throws IOException if there is an error processing the child elements.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected void cleanseChildElements(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Element element) throws IOException, DOMException {
		final NodeList childNodes = element.getChildNodes();
		for(int childNodeIndex = 0; childNodeIndex < childNodes.getLength();) { //advance the index manually as needed
			final Node childNode = childNodes.item(childNodeIndex);
			if(!(childNode instanceof Element)) { //skip non-elements
				childNodeIndex++;
				continue;
			}
			final Element childElement = (Element)childNode;
			final List<Element> cleansedElements = cleanseElement(context, contextArtifact, artifact, childElement);
			replaceChild(element, childElement, cleansedElements);
			childNodeIndex += cleansedElements.size(); //manually advance the index based upon the replacement nodes
		}
	}

	//#ascribe

	/** Namespaces of metadata that is not added to the document. */
	private static final Set<URI> UNASCRIBED_NAMESPACES = Set.of(GuiseMummy.NAMESPACE, Content.NAMESPACE);

	/**
	 * Generates metadata for a document.
	 * <ul>
	 * <li>Creates the {@code <html><head>} and {@code <html><head><title>} structure if necessary.</li>
	 * <li>Sets the title if one is present in the metadata.</li>
	 * <li>Creates appropriate metadata elements.
	 * <ul>
	 * <li>If the property is in the "default" URF ad-hoc namespace {@link URF#AD_HOC_NAMESPACE}, the property is stored as
	 * {@code <html><head><meta name="foo" content="bar"/>}.
	 * <li>If the property is in some other namespace, requiring a CURIE with a prefix, the property is stored as
	 * {@code <html><head><meta property="eg:foo" content="bar"/>}.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * <p>
	 * Metadata in the Guise Mummy namespace {@link GuiseMummy#NAMESPACE} and other internal namespaces are skipped.
	 * </p>
	 * @implSpec This implementation adds additional metadata to identify the generator and indicate the generation time.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param document The document to ascribe.
	 * @return The given document with metadata added.
	 * @throws IllegalArgumentException if the given document does not have an {@code <html>} element.
	 * @throws IOException if there is an error ascribing the document.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 * @see MummyContext#getMummifierIdentification()
	 */
	protected Document ascribeDocument(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Document document) throws IOException, DOMException {

		//ensure that the document has the correct <html><head><title> structure
		final Element htmlElement = findHtmlElement(document).orElseThrow(() -> new IllegalArgumentException("Document has no root <html> element."));
		final Element headElement = findHtmlHeadElement(document) //add a <html><head> if not present
				.orElseGet(() -> addFirst(htmlElement, document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_HEAD)));
		final Element titleElement = findHtmlHeadTitleElement(document) //add a <html><head><title> if not present
				.orElseGet(() -> addFirst(headElement, document.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_TITLE)));

		final UrfResourceDescription description = artifact.getResourceDescription();

		//discover the necessary vocabularies, using our predefined vocabularies for preferred prefixes 
		final VocabularyRegistrar vocabularyRegistrar = new VocabularyManager(XmlVocabularySpecification.INSTANCE, PREDEFINED_VOCABULARIES);
		vocabularyRegistrar.setDefaultVocabulary(URF.AD_HOC_NAMESPACE); //indicate which properties don't need a namespace
		for(final Map.Entry<URI, Object> property : description.getProperties()) {
			final URI tag = property.getKey();
			final Optional<URI> namespace = URF.Tag.findNamespace(tag);
			getLogger().trace("({}) Determining prefix for description tag {}.", artifact.getTargetPath(), tag);
			if(isPresentAndEquals(namespace, URF.AD_HOC_NAMESPACE)) { //skip tags that would have no namespace
				continue;
			}
			if(namespace.filter(UNASCRIBED_NAMESPACES::contains).isPresent()) { //skip tags in the unascribed namespaces
				continue;
			}
			vocabularyRegistrar.determinePrefixForTerm(tag); //make sure the namespace is registered with a prefix
		}

		//set the correct RDFa prefix associations in the <head> element, or remove it if no prefix definitions are required
		final String prefixAttributeValue = RDFa.toPrefixAttributeValue(vocabularyRegistrar);
		if(!prefixAttributeValue.isEmpty()) {
			headElement.setAttributeNS(null, RDFa.ATTRIBUTE_PREFIX, prefixAttributeValue);
		} else {
			headElement.removeAttributeNS(null, RDFa.ATTRIBUTE_PREFIX);
		}

		//set the title separately
		final URI titleTag = Handle.toTag(PROPERTY_HANDLE_TITLE);
		description.findPropertyValue(titleTag).map(Object::toString).ifPresent(title -> setText(titleElement, title));

		//set the other properties as `<meta>` elements, using either the `name` or `property` attribute depending on namespace
		for(final Map.Entry<URI, Object> property : description.getProperties()) {
			final URI tag = property.getKey();
			if(tag.equals(titleTag)) { //skip the title property; we already set it as the <title>
				continue;
			}
			if(URF.Tag.findNamespace(tag).filter(UNASCRIBED_NAMESPACES::contains).isPresent()) { //skip tags in the unascribed namespaces
				continue;
			}
			final Object value = property.getValue();
			try { //convert the property to a kebab-case CURIE 
				final Curie curie = vocabularyRegistrar.determineCurieForTerm(tag).orElseThrow(IllegalArgumentException::new).mapReference(CAMEL_CASE::toKebabCase);
				getLogger().trace("({}) Ascribing metadata: `{}`=`{}` ", artifact.getTargetPath(), curie, value);
				if(curie.getPrefix().isPresent()) { //use the `property` attribute for a CURIE with a prefix
					final Element metaElement = addLast(headElement, headElement.getOwnerDocument().createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_META));
					metaElement.setAttributeNS(null, RDFa.ATTRIBUTE_PROPERTY, curie.toString()); //TODO create RDFa utility
					metaElement.setAttributeNS(null, ELEMENT_META_ATTRIBUTE_CONTENT, value.toString());
				} else { //a non-prefixed CURIE is written to a normal `<meta>` `name` attribute
					addNamedMetadata(headElement, curie.toString(), value.toString());
				}
			} catch(final IllegalArgumentException illegalArgumentException) {
				getLogger().warn("Cannot determine CURIE for metadata tag `{}` with value `{}` in `{}`.", tag, value, artifact.getSourcePath());
			}
		}

		//#identify Guise Mummy as the generator
		setNamedMetadata(document, META_NAME_GENERATOR, context.getMummifierIdentification());
		//#indicate the instant of generation
		setNamedMetadata(document, META_NAME_GENERATED_AT, Instant.now().toString());

		return document;
	}

}
