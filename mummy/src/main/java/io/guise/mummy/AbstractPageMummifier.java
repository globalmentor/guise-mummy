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
import static com.globalmentor.io.Paths.*;
import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.lex.CompoundTokenization.*;
import static com.globalmentor.util.Optionals.*;
import static com.globalmentor.xml.XmlDom.*;
import static io.guise.mummy.Artifact.*;
import static io.guise.mummy.GuiseMummy.*;
import static io.urf.vocab.content.Content.*;
import static java.lang.System.*;
import static java.nio.file.Files.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.function.Predicate.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.text.Collator;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.*;

import org.w3c.dom.*;

import com.globalmentor.html.HtmlSerializer;
import com.globalmentor.html.spec.HTML;
import com.globalmentor.net.*;
import com.globalmentor.rdfa.spec.RDFa;
import com.globalmentor.vocab.Curie;
import com.globalmentor.vocab.*;
import com.globalmentor.xml.*;
import com.globalmentor.xml.spec.XML;

import io.urf.URF;
import io.urf.URF.Handle;
import io.urf.model.UrfObject;
import io.urf.model.UrfResourceDescription;
import io.urf.vocab.content.Content;

/**
 * Abstract base mummifier for generating HTML pages.
 * @author Garret Wilson
 * @see HTML#HTML_NAME_EXTENSION
 */
public abstract class AbstractPageMummifier extends AbstractSourcePathMummifier implements PageMummifier {

	/**
	 * A map of local names of HTML elements that can reference other resources (e.g. <code>"img"</code>), along with the attributes of each element that contains
	 * the actual resource reference path (e.g. (e.g. <code>"src"</code>) for {@code <img src="…">}).
	 */
	private static final Map<String, String> HTML_REFERENCE_ELEMENT_ATTRIBUTES = Map.ofEntries( //element local name -> attribute
			Map.entry(ELEMENT_A, ELEMENT_A_ATTRIBUTE_HREF), //<a href="…">
			Map.entry(ELEMENT_AREA, ELEMENT_AREA_ATTRIBUTE_HREF), //<area href="…">
			Map.entry(ELEMENT_AUDIO, ELEMENT_AUDIO_ATTRIBUTE_SRC), //<audio src="…">
			Map.entry(ELEMENT_EMBED, ELEMENT_EMBED_ATTRIBUTE_SRC), //<embed src="…">
			Map.entry(ELEMENT_IFRAME, ELEMENT_IFRAME_ATTRIBUTE_SRC), //<iframe src="…">
			Map.entry(ELEMENT_IMG, ELEMENT_IMG_ATTRIBUTE_SRC), //<img src="…">
			Map.entry(ELEMENT_LINK, ELEMENT_LINK_ATTRIBUTE_HREF), //<link href="…">
			Map.entry(ELEMENT_OBJECT, ELEMENT_OBJECT_ATTRIBUTE_DATA), //<object data="…">
			Map.entry(ELEMENT_SCRIPT, ELEMENT_SOURCE_ATTRIBUTE_SRC), //<script src="…">
			Map.entry(ELEMENT_SOURCE, ELEMENT_SOURCE_ATTRIBUTE_SRC), //<source src="…">
			Map.entry(ELEMENT_TRACK, ELEMENT_TRACK_ATTRIBUTE_SRC), //<track src="…">
			Map.entry(ELEMENT_VIDEO, ELEMENT_VIDEO_ATTRIBUTE_SRC) //<video src="…">
	);

	/**
	 * Vocabulary prefixes that will be recognized in metadata, such as in XHTML {@code <meta>} elements or in YAML, if they have not been associated with a
	 * different vocabulary.
	 * @implSpec These prefixes include those in {@link RDFa.InitialContext#VOCABULARIES}.
	 * @implSpec These prefixes include the prefix {@link GuiseMummy#NAMESPACE_PREFIX} for the namespace {@link GuiseMummy#NAMESPACE}.
	 */
	protected static final VocabularyRegistry PREDEFINED_VOCABULARIES = VocabularyRegistry.builder(RDFa.InitialContext.VOCABULARIES)
			.registerPrefix(GuiseMummy.NAMESPACE_PREFIX, GuiseMummy.NAMESPACE).build();

	/**
	 * {@inheritDoc}
	 * @implSpec This version changes the output file extension to {@value HTML#HTML_NAME_EXTENSION}.
	 */
	@Override
	public Path getArtifactTargetPath(final MummyContext context, final Path sourceFile) {
		final Path defaultArtifactTargetPath = super.getArtifactTargetPath(context, sourceFile);
		final boolean isNameBare = context.getConfiguration().findBoolean(CONFIG_KEY_MUMMY_PAGE_NAMES_BARE).orElse(false);
		if(isNameBare) { //so-called clean URLs
			return removeExtension(defaultArtifactTargetPath);
		} else {
			return changeExtension(defaultArtifactTargetPath, HTML_NAME_EXTENSION);
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
	 * Finds the artifact suitable to serve as parent level navigation for the artifacts at the current level. This will be the context artifact if the context
	 * artifact has child artifacts.
	 * @implSpec This method returns the context artifact itself if it is an instance of {@link CollectionArtifact}; otherwise the parent artifact, if any, is
	 *           returned by calling {@link MummyContext#findParentArtifact(Artifact)}.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @return The artifacts for navigation to the parent of the current navigation level.
	 * @see MummyContext#findParentArtifact(Artifact)
	 */
	protected Optional<Artifact> findParentNavigationArtifact(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact) {
		return contextArtifact instanceof CollectionArtifact ? Optional.of(contextArtifact) : context.findParentArtifact(contextArtifact);
	}

	/**
	 * Provides the artifacts suitable for direct subsequent navigation from this artifact, <em>excluding</em> the parent artifact. If sibling artifacts are
	 * returned, they will include the given resource.
	 * @apiNote The returned navigation artifacts are not necessarily children of the context artifact, but rather artifacts at the child level beneath some
	 *          parent.
	 * @implSpec This method retrieves candidate resources using {@link MummyContext#childArtifacts(Artifact)} if the artifact is a {@link CollectionArtifact};
	 *           otherwise it calls {@link MummyContext#siblingArtifacts(Artifact)}. Only artifacts that are not veiled are included.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @return The artifacts for subsequent navigation from this artifact.
	 * @see MummyContext#childArtifacts(Artifact)
	 * @see MummyContext#siblingArtifacts(Artifact)
	 * @see MummyContext#isVeiled(Artifact)
	 */
	protected Stream<Artifact> childNavigationArtifacts(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact) {
		final Stream<Artifact> candidateArtifacts = contextArtifact instanceof CollectionArtifact ? context.childArtifacts(contextArtifact)
				: context.siblingArtifacts(contextArtifact);
		return candidateArtifacts.filter(Artifact::isNavigable).filter(not(context::isVeiled));
	}

	@Override
	public PageArtifact plan(final MummyContext context, final Path sourceFile) throws IOException {
		final UrfResourceDescription description = loadDescription(context, sourceFile);
		return new PageArtifact(this, sourceFile, getArtifactTargetPath(context, sourceFile), description);
	}

	/**
	 * Converts a property name to a property tag URI.
	 * <ul>
	 * <li>If the property name is an RDFa <a href="https://www.w3.org/TR/rdfa-core/#s_curies">CURIE</a> such as the Open Graph <code>og:title</code> or even the
	 * Guise Mummy <code>mummy:order</code>, the CURIE is combined with its prefix by searching the hierarchy of the given element. The CURIE reference is
	 * converted from <code>kebab-case</code> to <code>camelCase</code>.</li>
	 * <li>Otherwise the meta name itself is used as a property handle, after converting from <code>kebab-case</code> to <code>camelCase</code>.</li>
	 * </ul>
	 * @implSpec The current implementation only finds prefixes if they are stored in <code>xmlns:</code> XML namespace prefix declarations, not in HTML5
	 *           <code>prefix</code> attributes.
	 * @implSpec This also recognizes all namespace prefixes included in {@link #PREDEFINED_VOCABULARIES} if they are not otherwise defined in the document.
	 * @param contextElement The context element that indicates the hierarchy for retrieving CURIE prefixes.
	 * @param metaName The name of a metadata property name, normally retrieved from HTML {@code <meta>} elements.
	 * @return The URI to be used as an URF property tag.
	 * @throws IllegalArgumentException if the property name is a CURIE but no prefix has been defined in the element hierarchy.
	 * @throws IllegalArgumentException if the property name is a CURIE but combined with the IRI leading segment does not result in a valid IRI.
	 * @throws IllegalArgumentException if the given property name is empty.
	 * @throws IllegalArgumentException if the given property name cannot be converted to <code>cameCase</code>, e.g. it has successive <code>'-'</code>
	 *           characters.
	 * @see <a href="https://www.w3.org/TR/rdfa-core/#s_curies">RDFa Core 1.1 - Third Edition § 6. CURIE Syntax Definition</a>.
	 * @see <a href="https://ogp.me/">The Open Graph protocol</a>
	 * @see Curie
	 */
	protected static URI htmlMetaNameToPropertyTag(@Nonnull final Element contextElement, @Nonnull final String metaName) {
		requireNonNull(contextElement);
		checkArgument(!metaName.isEmpty(), "Property name may not be empty.");
		final Curie curie = Curie.parse(metaName).mapReference(KEBAB_CASE::toCamelCase);
		final String reference = curie.getReference();
		return curie.getPrefix().map(prefix -> {
			String leadingSegment = null;
			Node currentNode = contextElement;
			do {
				leadingSegment = findAttributeNS((Element)currentNode, XML.XMLNS_NAMESPACE_URI_STRING, prefix).orElse(null);
			} while(leadingSegment == null && (currentNode = currentNode.getParentNode()) instanceof Element); //keep looking while we need to and while there are still parent elements
			if(leadingSegment == null) { //see if we have a predefined vocabulary for the prefix
				leadingSegment = PREDEFINED_VOCABULARIES.findVocabularyByPrefix(prefix).map(URI::toString).orElse(null);
			}
			checkArgument(leadingSegment != null, "No IRI leading segment defined for prefix %s of property %s.", prefix, metaName);
			return VocabularyTerm.toURI(URI.create(leadingSegment), reference);
		}).orElseGet(() -> Handle.toTag(curie.getReference()));
	}

	/**
	 * Determines the description for the given artifact based upon its source file and related files.
	 * @implSpec This default implementation loads description from metadata in the XHTML document obtained by calling
	 *           {@link #sourceMetadata(MummyContext, Path)}.
	 * @param context The context of static site generation.
	 * @param sourceFile The source file to be mummified.
	 * @return A description of the resource being mummified.
	 * @throws IOException if there is an I/O error retrieving the description, including if the metadata is invalid.
	 */
	protected UrfResourceDescription loadDescription(@Nonnull MummyContext context, @Nonnull final Path sourceFile) throws IOException {
		final UrfObject description = new UrfObject();

		//load source metadata
		try {
			sourceMetadata(context, sourceFile).forEach(meta -> {
				final URI propertyTag = meta.getKey();
				final String propertyValue = meta.getValue();
				if(!description.hasPropertyValue(propertyTag)) { //the first property wins
					description.setPropertyValue(propertyTag, propertyValue);
				}
			});
		} catch(final IllegalArgumentException illegalArgumentException) { //TODO include the filename and context
			throw new IOException(illegalArgumentException);
		}

		//TODO load any description sidecar

		//add the content type
		getArtifactMediaType(context, sourceFile).ifPresent(mediaType -> setContentType(description, mediaType));

		return description; //TODO add a way to make this immutable
	}

	/**
	 * Loads metadata stored in the source file itself.
	 * @apiNote This method ignores any metadata stored in related files such as sidecar files.
	 * @implSpec The default implementation opens an input stream to the given file and then extract the source metadata by calling
	 *           {@link #sourceMetadata(MummyContext, InputStream, String)}.
	 * @param context The context of static site generation.
	 * @param sourceFile The source file to be mummified.
	 * @return Metadata stored in the source file being mummified, consisting of resolved URI tag names and values. The name-value pairs may have duplicate names.
	 * @throws IOException if there is an I/O error retrieving the metadata.
	 */
	protected Stream<Map.Entry<URI, String>> sourceMetadata(@Nonnull MummyContext context, @Nonnull final Path sourceFile) throws IOException {
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(sourceFile))) {
			final Path filename = sourceFile.getFileName();
			return sourceMetadata(context, inputStream, filename != null ? filename.toString() : null);
		}
	}

	/**
	 * Loads metadata stored in the source file itself.
	 * @implSpec This implementation loads description from metadata in the XHTML document obtained by calling
	 *           {@link #loadSourceDocument(MummyContext, InputStream, String)} and then calls {@link #sourceMetadata(MummyContext, Document)} to extract the
	 *           metadata.
	 * @param context The context of static site generation.
	 * @param inputStream The input stream from which to to load the source metadata.
	 * @param name The optional name of the source, such as a filename, which may be missing or empty.
	 * @return Metadata stored in the source file being mummified, consisting of resolved URI tag names and values. The name-value pairs may have duplicate names.
	 * @throws IOException if there is an I/O error retrieving the metadata.
	 */
	protected Stream<Map.Entry<URI, String>> sourceMetadata(@Nonnull MummyContext context, @Nonnull InputStream inputStream, @Nullable final String name)
			throws IOException {
		final Document sourceDocument = loadSourceDocument(context, inputStream, name);
		try {
			return sourceMetadata(context, sourceDocument);
		} catch(final IllegalArgumentException | DOMException exception) { //TODO indicate the file and context
			throw new IOException(exception);
		}
	}

	/**
	 * Extracts metadata stored in the source document itself.
	 * @implSpec The XHTML document {@code <head><title>} will be returned as metadata, using {@value Artifact#PROPERTY_HANDLE_TITLE} as a handle; followed by
	 *           values in any {@code <head><meta>} elements, converted using {@link #htmlMetaNameToPropertyTag(Element, String)}.
	 * @param context The context of static site generation.
	 * @param sourceDocument The source XHTML document being mummified, from which metadata should be extracted.
	 * @return Metadata stored in the source document being mummified, consisting of resolved URI tag names and values. The name-value pairs may have duplicate
	 *         names.
	 * @throws IllegalArgumentException if any of the metadata is invalid.
	 * @throws DOMException if there is a problem retrieving metadata.
	 */
	protected Stream<Map.Entry<URI, String>> sourceMetadata(@Nonnull MummyContext context, @Nonnull final Document sourceDocument) throws DOMException {
		//TODO consider parsing out "keywords" in to multiple keyword+ properties for convenience
		return Stream.concat(
				//<title>; will override any <code>title</code> metadata property in this same document
				findTitle(sourceDocument).stream().map(title -> Map.entry(Handle.toTag(PROPERTY_HANDLE_TITLE), title)),
				//<meta> TODO detect and add warnings for invalid properties  
				namedMetadata(sourceDocument, AbstractPageMummifier::htmlMetaNameToPropertyTag, (element, value) -> value));
		//TODO consider parsing out "keywords" in to multiple keyword+ properties for convenience
	}

	@Override
	public void mummify(final MummyContext context, final Artifact contextArtifact, final Artifact artifact) throws IOException {

		try {

			//#load source document: get starting content to work with
			final Document sourceDocument = loadSourceDocument(context, (SourceFileArtifact)artifact); //this mummifier requires source file artifacts
			getLogger().debug("loaded source document: {}", artifact.getSourcePath());

			//#normalize: normalize the DOM and remove metadata
			final Document normalizedDocument = normalizeDocument(context, contextArtifact, artifact, sourceDocument);

			//#apply template
			final Document templatedDocument = applyTemplate(context, contextArtifact, artifact, normalizedDocument);

			//#process document: evaluate expressions and perform transformations
			final Document processedDocument = processDocument(context, contextArtifact, artifact, templatedDocument);

			//#relocate document from source to target: translate path references from the source to the target
			final Document relocatedDocument = relocateSourceDocumentToTarget(context, contextArtifact, artifact, processedDocument);

			//#cleanse document: remove all Guise Mummy related elements and attributes
			final Document cleansedDocument = cleanseDocument(context, contextArtifact, artifact, relocatedDocument);

			//#ascribe document: adds metadata not related to Guise Mummy directives
			final Document ascribedDocument = ascribeDocument(context, contextArtifact, artifact, cleansedDocument);

			//#save target document
			try (final OutputStream outputStream = new BufferedOutputStream(newOutputStream(artifact.getTargetPath()))) {
				final HtmlSerializer htmlSerializer = new HtmlSerializer(true);
				htmlSerializer.serialize(ascribedDocument, outputStream);
			}
			getLogger().debug("generated output document: {}", artifact.getTargetPath());

		} catch(final IllegalArgumentException | DOMException exception) { //convert input errors and XML errors to I/O errors TODO include filename?
			throw new IOException(exception);
		}

	}

	//#normalize

	/**
	 * Normalizes a document after it has been loaded, tidying the structure and removing any named metadata (which will be regenerated).
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
		document.normalizeDocument(); //normalize the XML DOM
		final List<Element> normalizedElements = normalizeElement(context, contextArtifact, artifact, document.getDocumentElement());
		if(normalizedElements.size() != 1 || normalizedElements.get(0) != document.getDocumentElement()) {
			throw new UnsupportedOperationException("Document element cannot be removed or replaced when normalizing a document.");
		}
		return document;
	}

	/**
	 * Normalizes a document element, removing any named metadata (that is, {@value HTML#ELEMENT_META} elements with a {@value HTML#ELEMENT_META_ATTRIBUTE_NAME}
	 * attribute).
	 * <p>
	 * The element is replaced with the returned elements. If only the same element is returned, no replacement is made. If no element is returned, the source
	 * element is removed.
	 * </p>
	 * @implSpec This implementation marks for removal any {@value HTML#ELEMENT_META} elements with a {@value HTML#ELEMENT_META_ATTRIBUTE_NAME} attribute. It also
	 *           removes all {@link RDFa#ATTRIBUTE_PREFIX} attributes.
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
				&& element.hasAttribute(ELEMENT_META_ATTRIBUTE_NAME)) { //<<meta name="…">
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
	 * @implNote This implementation does not yet allow returning different nodes than the one being normalized
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
			final int normalizedElementCount = normalizedElements.size();
			childNodeIndex += normalizedElementCount; //manually advance the index based upon the replacement nodes
			if(normalizedElementCount == 0) { //if we should remove the element
				childElement.getParentNode().removeChild(childElement);
				continue;
			} else if(normalizedElementCount == 1 && normalizedElements.get(0) == childElement) { //if no structural changes were requested
				continue;
			}
			throw new UnsupportedOperationException("Structural changes not yet fully supported when normalizing individual child elements.");
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
	 * @throws IOException if there is an error applying a template.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected Document applyTemplate(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Document sourceDocument) throws IOException, DOMException {
		return findTemplateSourceFile(context, contextArtifact, artifact, sourceDocument) //determine if there is a specified or appropriate template
				.flatMap(throwingFunction(templateSource -> { //try to apply the template
					final Path templateFile = templateSource.getKey();
					final PageMummifier templateMummifier = templateSource.getValue();
					getLogger().debug("  {*} found template: {}", templateFile);

					//#load and relocate the template document
					final Document templateDocument;
					{
						final Document sourceTemplateDocument = templateMummifier.loadSourceDocument(context, templateFile);
						//relocate the template links _within the source tree_ as if it were in the place of the artifact source
						templateDocument = relocateDocument(context, sourceTemplateDocument, templateFile, contextArtifact.getSourcePath(), Artifact::getSourcePath);
					}

					//1. validate structure
					findHtmlElement(templateDocument).orElseThrow(() -> new IOException(String.format("Template %s has no root <html> element.", templateFile)));

					// Do _not_ apply metadata. Metadata is now generated semantically from the actual description, which has already been loaded.

					//2. merge head links
					mergeLinks(templateDocument, sourceDocument);

					//3. apply content

					final Element templateContentElement = findContentElement(templateDocument)
							.orElseThrow(() -> new IOException(String.format("Template %s has no content insertion point.", templateFile)));
					findContentElement(sourceDocument).ifPresentOrElse(sourceContentElement -> {
						getLogger().debug("  {*} applying source content");
						removeChildren(templateContentElement);
						mergeAttributes(templateContentElement, sourceContentElement);
						appendImportedChildNodes(templateContentElement, sourceContentElement);
					}, () -> getLogger().warn("Source file for {} has no content to place in template.", artifact.getSourcePath()));
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
				throw new IOException(String.format("Source file %s specified invalid template %s: %s.", artifact.getSourcePath(), customTemplate,
						illegalArgumentException.getMessage()));
			}
		} else { //if no custom template was specified
			return context.findPageSourceFile(artifact.getSourceDirectory(), ".template", true); //look for a template TODO allow base filename to be configurable
		}
	}

	/**
	 * Merges all {@code <head>} links from the source document into the template document. If a link with the same reference already exists in the template, it
	 * will not be replaced by that in the source.
	 * <p>
	 * This method assumes that the template has been relocated to the perfect location; otherwise, duplicate links will not be detected.
	 * </p>
	 * @implSpec This implementation does not allow adding links already present in the template, even if the duplicate link is for a different type of content
	 *           (e.g. adding the same link as both a script and as a stylesheet).
	 * @implNote This implementation processes any reference element in {@link #HTML_REFERENCE_ELEMENT_ATTRIBUTES}, even though many of those elements would not
	 *           normally appear in a document {@code <head>} element.
	 * @param templateDocument The template into which the source document links are to be merged; must have a {@code <head>} element.
	 * @param sourceDocument The source document the links of which are being merged.
	 * @throws IllegalArgumentException if the template does not have a {@code <head>} element.
	 * @throws IOException if there is an error merging the links.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected void mergeLinks(@Nonnull final Document templateDocument, @Nonnull final Document sourceDocument) throws IOException, DOMException {
		final Set<URI> links = new HashSet<>(); //keep track of existing template links and added links
		final Element templateHeadElement = findHtmlHeadElement(templateDocument) //add a <html><head> if not present
				.orElseThrow(() -> new IllegalArgumentException("Template missing <head> element."));

		//collect existing template <head> links
		childElementsOf(templateHeadElement).filter(element -> XHTML_NAMESPACE_URI_STRING.equals(element.getNamespaceURI())).forEach(element -> {
			final String referenceAttributeName = HTML_REFERENCE_ELEMENT_ATTRIBUTES.get(element.getLocalName());
			if(referenceAttributeName != null) {
				findAttributeNS(element, null, referenceAttributeName).ifPresent(referenceString -> {
					try {
						final URI referenceURI = new URI(referenceString).normalize();
						links.add(referenceURI);
					} catch(final URISyntaxException uriSyntaxException) {
						getLogger().warn("Invalid template <head><{}> reference {}.", element.getLocalName(), referenceString, uriSyntaxException);
						return;
					}
				});
			}
		});

		//merge in source document <head> links
		findHtmlHeadElement(sourceDocument).stream().flatMap(XmlDom::childElementsOf)
				.filter(element -> XHTML_NAMESPACE_URI_STRING.equals(element.getNamespaceURI())).forEach(element -> {
					final String referenceAttributeName = HTML_REFERENCE_ELEMENT_ATTRIBUTES.get(element.getLocalName());
					if(referenceAttributeName != null) {
						findAttributeNS(element, null, referenceAttributeName).ifPresent(referenceString -> {
							try {
								final URI referenceURI = new URI(referenceString).normalize();
								if(!links.contains(referenceURI)) {
									templateHeadElement.appendChild(templateDocument.importNode(element, true));
									links.add(referenceURI); //prevent duplicate source links
								}
							} catch(final URISyntaxException uriSyntaxException) {
								getLogger().warn("Invalid template <head><{}> reference {}.", element.getLocalName(), referenceString, uriSyntaxException);
								return;
							}
						});
					}
				});
	}

	/**
	 * Attempts to determine which element in the source document contains the content of the document, to be inserted into a template, for example.
	 * @implSpec This implementation finds the content element in the following order:
	 *           <ol>
	 *           <li>The {@code <html><body><main>} element, if present.</li>
	 *           <li>The {@code <html><body>} element, if present.</li>
	 *           </ol>
	 * @param sourceDocument The document containing source content.
	 * @return The element containing content in the source document.
	 */
	protected Optional<Element> findContentElement(@Nonnull final Document sourceDocument) {
		final Optional<Element> htmlBodyElement = findHtmlBodyElement(sourceDocument);
		final Optional<Element> htmlBodyMainElement = htmlBodyElement
				.flatMap(htmlElement -> childElementsByNameNS(htmlElement, XHTML_NAMESPACE_URI_STRING, ELEMENT_MAIN).findFirst());
		//TODO add support for <article>
		//TODO add support for <mummy:content>
		return htmlBodyMainElement.or(() -> htmlBodyElement); //<main> gets priority over <body>
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
	 * Processes a source document element.
	 * <p>
	 * The element is replaced with the returned elements. If only the same element is returned, no replacement is made. If no element is returned, the source
	 * element is removed.
	 * </p>
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param sourceElement The source element to process.
	 * @return The processed element(s), if any, to replace the source element.
	 * @throws IOException if there is an error processing the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected List<Element> processElement(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Element sourceElement) throws IOException, DOMException {

		//TODO transfer to some system of pluggable element processing strategies

		if(ATTRIBUTE_REGENERATE.equals(findAttributeNS(sourceElement, NAMESPACE_STRING, ATTRIBUTE_REGENERATE).orElse(null))) { //TODO create utility Optional matcher

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
	 * @implNote This implementation does not yet allow returning different nodes than the one being processed.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param sourceElement The source element the children of which to process.
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
			final int processedElementCount = processedElements.size();
			childNodeIndex += processedElementCount; //manually advance the index based upon the replacement nodes
			if(processedElementCount == 0) { //if we should remove the element
				childElement.getParentNode().removeChild(childElement);
				continue;
			} else if(processedElementCount == 1 && processedElements.get(0) == childElement) { //if no structural changes were requested
				continue;
			}
			throw new UnsupportedOperationException("Structural changes not yet fully supported when processing individual child elements.");
		}
	}

	/**
	 * Returns the given object as a {@link Long}, converting if necessary.
	 * @implSpec This implementation only supports {@link Long} and {@link String} types, converting the latter using {@link Long#valueOf(String)}.
	 * @param object The object to return as a {@link Long}.
	 * @return The object as a {@link Long} instance.
	 * @throws IllegalArgumentException if the given object cannot be converted to a {@link Long}.
	 */
	private static Long toLong(@Nonnull final Object object) { //TODO switch to a general converter system, including number types, e.g. from Ploop
		if(object instanceof Long) {
			return (Long)object;
		} else if(object instanceof String) {
			return Long.valueOf((String)object);
		} else {
			throw new IllegalArgumentException(String.format("Cannot convert object of type %s to type %s.", object.getClass().getName(), Long.class.getName()));
		}
	}

	/**
	 * Regenerates a navigation list based upon the parent, sibling, and/or child navigation artifacts relative to the context artifact.
	 * <p>
	 * The element is replaced with the returned elements. If only the same element is returned, no replacement is made. If no element is returned, the source
	 * element is removed.
	 * </p>
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param navigationListElement The list element to regenerate.
	 * @return The processed element(s), if any, to replace the source element.
	 * @throws IOException if there is an error processing the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
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

		//decide how to sort the links
		final Collator navigationCollator = Collator.getInstance(); //TODO i18n: get locale for page, defaulting to site locale
		navigationCollator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
		navigationCollator.setStrength(Collator.PRIMARY); //ignore accents and case
		final Comparator<Artifact> navigationArtifactOrderComparator = Comparator
				//compare first by order (defaulting to zero)
				.<Artifact, Long>comparing(
						navigationArtifact -> toLong(navigationArtifact.getResourceDescription().findPropertyValue(PROPERTY_TAG_MUMMY_ORDER).orElse(MUMMY_ORDER_DEFAULT)))
				//then compare by alphabetical order
				.thenComparing(navigationArtifact -> navigationArtifact.determineLabel(), navigationCollator);

		//add new navigation links from templates
		Stream.concat(
				//put the parent navigation artifact (if any) first
				findParentNavigationArtifact(context, contextArtifact).stream(),
				//then include the sorted child navigation artifacts
				childNavigationArtifacts(context, contextArtifact).sorted(navigationArtifactOrderComparator))
				//generate navigation elements 
				.forEach(navigationArtifact -> {
					//if the navigation artifact is this artifact, use the template for an active link
					final Element liTemplate = navigationArtifact.equals(contextArtifact) ? activeLiTemplate : inactiveLiTemplate;
					final Element liElement = (Element)liTemplate.cloneNode(true);
					findFirst(liElement.getElementsByTagNameNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_A)).map(Element.class::cast).ifPresent(aElement -> { //find <li><a>
						aElement.setAttributeNS(null, ELEMENT_A_ATTRIBUTE_HREF, context.relativizeSourceReference(contextArtifact, navigationArtifact).toString());
						//remove all text and add the link label
						appendText(removeChildren(aElement), navigationArtifact.determineLabel());
					});
					navigationListElement.appendChild(liElement);
					appendText(navigationListElement, System.lineSeparator()); //TODO remove when HTML formatting is fixed
				});

		return List.of(navigationListElement);
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
		return relocateDocument(context, sourceDocument, contextArtifact.getSourcePath(), contextArtifact.getTargetPath(), Artifact::getTargetPath);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation does not allow the document element to be removed or replaced.
	 */
	@Override
	public Document relocateDocument(@Nonnull MummyContext context, @Nonnull final Document sourceDocument, @Nonnull final Path originalReferrerSourcePath,
			@Nonnull final Path relocatedReferrerPath, @Nonnull final Function<Artifact, Path> referentArtifactPath) throws IOException, DOMException {
		final List<Element> relocatedElements = relocateElement(context, sourceDocument.getDocumentElement(), originalReferrerSourcePath, relocatedReferrerPath,
				referentArtifactPath);
		if(relocatedElements.size() != 1 || relocatedElements.get(0) != sourceDocument.getDocumentElement()) {
			throw new UnsupportedOperationException("Document element cannot be removed or replaced when relocating a document.");
		}
		return sourceDocument;
	}

	/**
	 * Relocates a source document element by retargeting its references relative to a new referrer path location.
	 * <p>
	 * The element is replaced with the returned elements. If only the same element is returned, no replacement is made. If no element is returned, the source
	 * element is removed.
	 * </p>
	 * @implSpec This implementation relocates the {@link #HTML_REFERENCE_ELEMENT_ATTRIBUTES} elements and attributes.
	 * @param context The context of static site generation.
	 * @param sourceElement The source element to relocate.
	 * @param originalReferrerSourcePath The absolute original path of the referrer, e.g. <code>…/foo/page.xhtml</code>.
	 * @param relocatedReferrerPath The absolute relocated path of the referrer, e.g. <code>…/bar/page.xhtml</code>.
	 * @param referentArtifactPath The function for determining the path of the determined referent artifact. This function should return either the source path
	 *          or the destination path of the artifact concordant with the site tree of the relocated referrer.
	 * @return The relocated element(s), if any, to replace the source element.
	 * @throws IOException if there is an error relocating the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 * @see #HTML_REFERENCE_ELEMENT_ATTRIBUTES
	 */
	protected List<Element> relocateElement(@Nonnull MummyContext context, @Nonnull final Element sourceElement, @Nonnull final Path originalReferrerSourcePath,
			@Nonnull final Path relocatedReferrerPath, @Nonnull final Function<Artifact, Path> referentArtifactPath) throws IOException, DOMException {

		//TODO transfer to some system of pluggable element relocating strategies
		if(XHTML_NAMESPACE_URI_STRING.equals(sourceElement.getNamespaceURI())) {
			//see if this is a referrer element, and get the attribute doing the referencing
			final String referenceAttributeName = HTML_REFERENCE_ELEMENT_ATTRIBUTES.get(sourceElement.getLocalName());
			if(referenceAttributeName != null) {
				return relocateReferenceElement(context, sourceElement, referenceAttributeName, originalReferrerSourcePath, relocatedReferrerPath,
						referentArtifactPath);
			}
		}

		relocateChildElements(context, sourceElement, originalReferrerSourcePath, relocatedReferrerPath, referentArtifactPath);

		return List.of(sourceElement);
	}

	/**
	 * Relocates child elements of an existing element by retargeting references relative to a new referrer path location.
	 * @implNote This implementation does not yet allow returning different nodes than the one being relocated.
	 * @param context The context of static site generation.
	 * @param sourceElement The source element the children of which to relocate.
	 * @param originalReferrerSourcePath The absolute original path of the referrer, e.g. <code>…/foo/page.xhtml</code>.
	 * @param relocatedReferrerPath The absolute relocated path of the referrer, e.g. <code>…/bar/page.xhtml</code>.
	 * @param referentArtifactPath The function for determining the path of the determined referent artifact. This function should return either the source path
	 *          or the destination path of the artifact concordant with the site tree of the relocated referrer.
	 * @throws IOException if there is an error relocating the child elements.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected void relocateChildElements(@Nonnull MummyContext context, @Nonnull final Element sourceElement, @Nonnull final Path originalReferrerSourcePath,
			@Nonnull final Path relocatedReferrerPath, @Nonnull final Function<Artifact, Path> referentArtifactPath) throws IOException, DOMException {
		final NodeList childNodes = sourceElement.getChildNodes();
		for(int childNodeIndex = 0; childNodeIndex < childNodes.getLength();) { //advance the index manually as needed
			final Node childNode = childNodes.item(childNodeIndex);
			if(!(childNode instanceof Element)) { //skip non-elements
				childNodeIndex++;
				continue;
			}
			final Element childElement = (Element)childNode;
			final List<Element> relocatedElements = relocateElement(context, childElement, originalReferrerSourcePath, relocatedReferrerPath, referentArtifactPath);
			final int relocatedElementCount = relocatedElements.size();
			childNodeIndex += relocatedElementCount; //manually advance the index based upon the replacement nodes
			if(relocatedElementCount == 1 && relocatedElements.get(0) == childElement) { //if no structural changes were requested
				continue;
			}
			throw new UnsupportedOperationException("Structural changes not yet supported when relocating individual child elements.");
		}
	}

	/**
	 * Relocates a reference element by retargeting its reference attribute relative to a new referrer path location.
	 * <p>
	 * The element is replaced with the returned elements. If only the same element is returned, no replacement is made. If no element is returned, the source
	 * element is removed.
	 * </p>
	 * <p>
	 * A reference to <code>""</code> is considered to be a relative self reference as per RFC 3986, and is never modified during relocation, as it is always
	 * inherently "relocated" regardless of the resource location.
	 * </p>
	 * @param context The context of static site generation.
	 * @param referenceElement The reference element such a {@code <a>} to relocate.
	 * @param referenceAttributeName The name of the reference attribute such a {@code href} to relocate.
	 * @param originalReferrerSourcePath The absolute original path of the referrer, e.g. <code>…/foo/page.xhtml</code>.
	 * @param relocatedReferrerPath The absolute relocated path of the referrer, e.g. <code>…/bar/page.xhtml</code>.
	 * @param referentArtifactPath The function for determining the path of the determined referent artifact. This function should return either the source path
	 *          or the destination path of the artifact concordant with the site tree of the relocated referrer.
	 * @return The relocated element(s), if any, to replace the source element.
	 * @throws IOException if there is an error relocating the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 * @see #retargetResourceReference(MummyContext, URI, Path, Path, Function)
	 */
	protected List<Element> relocateReferenceElement(@Nonnull MummyContext context, @Nonnull final Element referenceElement,
			@Nonnull final String referenceAttributeName, @Nonnull final Path originalReferrerSourcePath, @Nonnull final Path relocatedReferrerPath,
			@Nonnull final Function<Artifact, Path> referentArtifactPath) throws IOException, DOMException {
		findAttributeNS(referenceElement, null, referenceAttributeName).ifPresent(referenceString -> {
			getLogger().debug("  - found reference <{} {}=\"{}\" …>", referenceElement.getNodeName(), referenceAttributeName, referenceString);
			//TODO check for the empty string and do something appropriate
			final URI referenceURI;
			try {
				referenceURI = new URI(referenceString);
				if(!referenceURI.isAbsolute()) { //only convert paths
					final String referencePath = referenceURI.getRawPath();
					if(referencePath != null && !referencePath.isEmpty() && !URIs.isPathAbsolute(referencePath)) { //only convert relative paths that are not self-references ("")
						retargetResourceReference(context, referenceURI, originalReferrerSourcePath, relocatedReferrerPath, referentArtifactPath)
								.ifPresentOrElse(retargetedResourceReference -> {
									getLogger().debug("  -> mapping to : {}", retargetedResourceReference);
									referenceElement.setAttributeNS(null, referenceAttributeName, retargetedResourceReference.toString());
								}, () -> getLogger().warn("No target artifact found for source relative reference {}.", referenceURI));
					}
				}
			} catch(final URISyntaxException uriSyntaxException) {
				getLogger().warn("Invalied reference <{} {}=\"{}\" …>\".", referenceElement.getNodeName(), referenceAttributeName, referenceString, uriSyntaxException);
				return;
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
	 * @param relocatedReferrerPath The absolute relocated path of the referrer, e.g. <code>…/bar/page.xhtml</code>.
	 * @param referentArtifactPath The function for determining the path of the determined referent artifact. This function should return either the source path
	 *          or the destination path of the artifact concordant with the site tree of the relocated referrer.
	 * @return The relative reference to the original artifact, now relativized to the relocated referrer path, e.g. <code>../bar/example/test.txt?foo#bar</code>.
	 * @throws IllegalArgumentException if the given reference has no path or its path is absolute.
	 * @throws IllegalArgumentException if the original referrer source path is not absolute and/or is not within the site source tree.
	 * @throws IllegalArgumentException if the relocated referred path is not in the site source or target tree.
	 * @throws IllegalArgumentException if the referent artifact path is not in the same source/target tree as the relocated referrer path.
	 */
	protected Optional<URI> retargetResourceReference(@Nonnull MummyContext context, @Nonnull URI resourceReference,
			@Nonnull final Path originalReferrerSourcePath, @Nonnull final Path relocatedReferrerPath, @Nonnull final Function<Artifact, Path> referentArtifactPath) {
		final URIPath resourceReferencePath = URIs.getPath(resourceReference);
		checkArgument(resourceReferencePath != null, "Resource reference %s has no path.");
		return retargetResourceReferencePath(context, resourceReferencePath, originalReferrerSourcePath, relocatedReferrerPath, referentArtifactPath) //retarget the path separately
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
	 * @param relocatedReferrerPath The absolute relocated path of the referrer, e.g. <code>…/bar/page.xhtml</code>.
	 * @param referentArtifactPath The function for determining the path of the determined referent artifact. This function should return either the source path
	 *          or the destination path of the artifact concordant with the site tree of the relocated referrer.
	 * @return The relative path to the original artifact, now relativized to the relocated referrer path, e.g. <code>../bar/example/test.txt</code>.
	 * @throws IllegalArgumentException if the given reference path is absolute.
	 * @throws IllegalArgumentException if the original referrer source path is not absolute and/or is not within the site source tree.
	 * @throws IllegalArgumentException if the relocated referred path is not in the site source or target tree.
	 * @throws IllegalArgumentException if the referent artifact path is not in the same source/target tree as the relocated referrer path.
	 */
	protected Optional<URIPath> retargetResourceReferencePath(@Nonnull MummyContext context, @Nonnull URIPath resourceReferencePath,
			@Nonnull final Path originalReferrerSourcePath, @Nonnull final Path relocatedReferrerPath, @Nonnull final Function<Artifact, Path> referentArtifactPath) {
		return context.findArtifactBySourceRelativeReference(originalReferrerSourcePath, resourceReferencePath)
				.map(referentArtifact -> context.relativizeResourceReference(relocatedReferrerPath, referentArtifactPath.apply(referentArtifact)));
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
	 * <p>
	 * The element is replaced with the returned elements. If only the same element is returned, no replacement is made. If no element is returned, the source
	 * element is removed.
	 * </p>
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
	 * @implNote This implementation does not yet allow returning different nodes than the one being cleansed.
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
			final int cleansedElementCount = cleansedElements.size();
			childNodeIndex += cleansedElementCount; //manually advance the index based upon the replacement nodes
			if(cleansedElementCount == 0) { //if we should remove the element
				childElement.getParentNode().removeChild(childElement);
				continue;
			} else if(cleansedElementCount == 1 && cleansedElements.get(0) == childElement) { //if no structural changes were requested
				continue;
			}
			throw new UnsupportedOperationException("Structural changes not yet fully supported when cleansing individual child elements.");
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
	 * <li>Creates appropriate metadata elements.</li>
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
			final Optional<URI> namespace = URF.Tag.getNamespace(tag);
			getLogger().debug("({}) Determining prefix for description tag {}.", artifact.getTargetPath(), tag);
			if(isPresentAndEquals(namespace, URF.AD_HOC_NAMESPACE)) { //skip tags that would have no namespace
				continue;
			}
			if(namespace.filter(UNASCRIBED_NAMESPACES::contains).isPresent()) { //skip tags in the unascribed namespaces
				continue;
			}
			vocabularyRegistrar.determinePrefixForTerm(tag); //make sure the namespace is registered with a prefix
		}

		//set the correct RDFa prefix associations in the <head> element
		headElement.setAttribute(RDFa.ATTRIBUTE_PREFIX, RDFa.toPrefixAttributeValue(vocabularyRegistrar));

		//set the title separately
		final URI titleTag = Handle.toTag(PROPERTY_HANDLE_TITLE);
		description.findPropertyValue(titleTag).map(Object::toString).ifPresent(title -> setText(titleElement, title));

		//set the other properties as <meta> elements
		for(final Map.Entry<URI, Object> property : description.getProperties()) {
			final URI tag = property.getKey();
			if(tag.equals(titleTag)) { //skip the title property; we already set it as the <title>
				continue;
			}
			if(URF.Tag.getNamespace(tag).filter(UNASCRIBED_NAMESPACES::contains).isPresent()) { //skip tags in the unascribed namespaces
				continue;
			}
			final Object value = property.getValue();
			final Curie curie;
			try { //convert the property to a kebab-case CURIE 
				curie = vocabularyRegistrar.determineCurieForTerm(tag).orElseThrow(IllegalArgumentException::new).mapReference(CAMEL_CASE::toKebabCase);
				getLogger().debug("({}) Ascribing metadata: `{}`=`{}` ", artifact.getTargetPath(), curie, value);
				appendText(headElement, CHARACTER_TABULATION_CHAR); //\t	//TODO remove formatting when HTML formatter is improved
				addNamedMetadata(headElement, curie.toString(), value.toString());
				appendText(headElement, lineSeparator()); //\n
			} catch(final IllegalArgumentException illegalArgumentException) {
				getLogger().warn("Cannot determine CURIE for metadata tag `{}` with value `{}`.", tag, value);
			}
		}

		//#identify Guise Mummy as the generator
		setNamedMetadata(document, META_NAME_GENERATOR, context.getMummifierIdentification());
		//#indicate the instant of generation
		setNamedMetadata(document, META_NAME_GENERATED_AT, Instant.now().toString());

		return document;
	}

}
