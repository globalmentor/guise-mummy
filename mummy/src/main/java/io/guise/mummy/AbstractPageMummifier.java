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
import static com.globalmentor.java.StringBuilders.*;
import static com.globalmentor.xml.XmlDom.*;
import static java.nio.file.Files.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import javax.annotation.*;

import org.w3c.dom.*;

import com.globalmentor.html.HtmlSerializer;
import com.globalmentor.io.Filenames;
import com.globalmentor.net.URIPath;
import com.globalmentor.xml.spec.XML;

import io.urf.URF;
import io.urf.model.UrfObject;
import io.urf.model.UrfResourceDescription;

/**
 * Abstract base mummifier for generating HTML pages.
 * @author Garret Wilson
 */
public abstract class AbstractPageMummifier extends AbstractSourcePathMummifier implements PageMummifier {

	/**
	 * A map of local names of HTML elements that can reference other resources (e.g. <code>"img"</code>), along with the attributes of each element that contains
	 * the actual resource reference path (e.g. (e.g. <code>"src"</code>) for {@code <img src="…">}).
	 */
	private final static Map<String, String> HTML_REFERENCE_ELEMENT_ATTRIBUTES = Map.ofEntries( //element local name -> attribute
			Map.entry(ELEMENT_A, ELEMENT_A_ATTRIBUTE_HREF), //<a href="…">
			Map.entry(ELEMENT_AREA, ELEMENT_AREA_ATTRIBUTE_HREF), //<area href="…">
			Map.entry(ELEMENT_AUDIO, ELEMENT_AUDIO_ATTRIBUTE_SRC), //<audio src="…">
			Map.entry(ELEMENT_EMBED, ELEMENT_EMBED_ATTRIBUTE_SRC), //<embed src="…">
			Map.entry(ELEMENT_IFRAME, ELEMENT_IFRAME_ATTRIBUTE_SRC), //<iframe src="…">
			Map.entry(ELEMENT_IMG, ELEMENT_IMG_ATTRIBUTE_SRC), //<img src="…">
			Map.entry(ELEMENT_LINK, ELEMENT_LINK_ATTRIBUTE_HREF), //<link href="…">
			Map.entry(ELEMENT_OBJECT, ELEMENT_OBJECT_ATTRIBUTE_DATA), //<object data="…">
			Map.entry(ELEMENT_SOURCE, ELEMENT_SOURCE_ATTRIBUTE_SRC), //<source src="…">
			Map.entry(ELEMENT_TRACK, ELEMENT_TRACK_ATTRIBUTE_SRC), //<track src="…">
			Map.entry(ELEMENT_VIDEO, ELEMENT_VIDEO_ATTRIBUTE_SRC) //<video src="…">
	);

	/**
	 * {@inheritDoc}
	 * @implSpec This version changes the output file extension to <code>html</code>.
	 */
	@Override
	protected Path getArtifactTargetPath(final MummyContext context, final Path sourceFile) {
		return changeExtension(super.getArtifactTargetPath(context, sourceFile), "html"); //TODO use constant
	}

	@Override
	public PageArtifact plan(final MummyContext context, final Path sourceFile) throws IOException {
		final UrfResourceDescription description = loadDescription(context, sourceFile);
		return new PageArtifact(this, sourceFile, getArtifactTargetPath(context, sourceFile), description);
	}

	/** The character to use for replacing invalid metadata name characters. */
	private static final char META_NAME_REPLACMENT_CHAR = '_';

	/**
	 * Normalizes a metadata property name into an appropriate URF handle.
	 * <p>
	 * The following changes are made:
	 * </p>
	 * <ul>
	 * <li>Beginning and ending whitespace is trimmed.</li>
	 * <li>Any space <code>' '</code> is replaced with underscore <code>'_'</code>.</li>
	 * <li>The XML QName delimiter <code>':'</code> is replaced with the URF handle segment delimiter <code>'-'</code>.
	 * </ul>
	 * @param propertyName The name of a metadata property name, normally retrieved from HTML {@code <meta>} elements.
	 * @return The property name normalized appropriately to be used for an URF handle.
	 * @throws IllegalArgumentException if the given property name is empty.
	 * @throws IllegalArgumentException if the given property name cannot be normalized, e.g. it has successive <code>'-'</code> characters.
	 * @see URF.Handle#isValid(String)
	 * @see <a href="http://ogp.me/">The Open Graph protocol</a>
	 */
	protected static String normalizePropertyHandle(@Nonnull final String propertyName) {
		checkArgument(!propertyName.isEmpty(), "Property name may not be empty.");
		if(URF.Handle.isValid(propertyName)) { //if the property name is already valid, there's nothing to do
			return propertyName;
		}
		final StringBuilder handleBuilder = new StringBuilder(propertyName.trim()); //TODO use a more comprehensive trim method that recognizes Character.isWhiteSpace()
		replace(handleBuilder, SPACE_CHAR, META_NAME_REPLACMENT_CHAR); //TODO improve to catch all whitespace; consider converting to camelCase
		replace(handleBuilder, XML.NAMESPACE_DIVIDER, URF.Handle.SEGMENT_DELIMITER); //e.g. "og:type" from Open Graph
		//TODO convert other invalid characters
		final String normalizedHandle = handleBuilder.toString();
		checkArgument(URF.Handle.isValid(normalizedHandle), "Property name %s cannot be normalized.", propertyName);
		return normalizedHandle;
	}

	/**
	 * Determines the description for the given artifact based upon its source file and related files.
	 * @implSpec This default implementation loads description from metadata in the XHTML document obtained by calling
	 *           {@link #loadSourceDocument(MummyContext, Path)}.
	 * @param context The context of static site generation.
	 * @param sourceFile The source file to be mummified.
	 * @return An artifact describing the resource to be mummified.
	 * @throws IOException if there is an I/O error retrieving the description.
	 */
	protected UrfResourceDescription loadDescription(@Nonnull MummyContext context, @Nonnull final Path sourceFile) throws IOException {
		final UrfObject description = new UrfObject();
		final Document sourceDocument = loadSourceDocument(context, sourceFile);
		//<title>; will override any <code>title</code> metadata property in this same document
		findTitle(sourceDocument).ifPresent(title -> description.setPropertyValueByHandle(Artifact.PROPERTY_HANDLE_TITLE, title));
		//<meta>; empty and whitespace-only  
		namedMetadata(sourceDocument).filter(meta -> !meta.getKey().isBlank()).forEach(meta -> {
			final String propertyHandle;
			try {
				propertyHandle = normalizePropertyHandle(meta.getKey());
			} catch(final IllegalArgumentException illegalArgumentException) {
				getLogger().warn("Property name {} for artifact {} is invalid and will not be included in resource description.");
				return; //skip processing of this property
			}
			final String propertyValue = meta.getValue();
			if(!description.hasPropertyValueByHandle(propertyHandle)) { //the first property wins
				description.setPropertyValueByHandle(propertyHandle, propertyValue);
			}
			//TODO consider parsing out "keywords" in to multiple keyword+ properties for convenience
		});
		//TODO load any description sidecar
		return description; //TODO add a way to make this immutable
	}

	@Override
	public void mummify(final MummyContext context, final Artifact contextArtifact, final Artifact artifact) throws IOException {

		try {

			//#load source document: get starting content to work with
			final Document sourceDocument = loadSourceDocument(context, artifact.getSourcePath());
			getLogger().debug("loaded source document: {}", artifact.getSourcePath());

			//#apply template
			final Document templatedocument = applyTemplate(context, contextArtifact, artifact, sourceDocument);

			//#process document: evaluate expressions and perform transformations
			final Document processedDocument = processDocument(context, contextArtifact, artifact, templatedocument);

			//#relocate document from source to target: translate path references from the source to the target
			final Document relocatedDocument = relocateSourceDocumentToTarget(context, contextArtifact, artifact, processedDocument);

			//#save target document
			try (final OutputStream outputStream = new BufferedOutputStream(newOutputStream(artifact.getTargetPath()))) {
				final HtmlSerializer htmlSerializer = new HtmlSerializer(true);
				htmlSerializer.serialize(relocatedDocument, outputStream);
			}
			getLogger().debug("generated output document: {}", artifact.getTargetPath());

		} catch(final DOMException domException) { //convert XML errors to I/O errors
			throw new IOException(domException);
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
		return context.findPageSourceFile(artifact.getSourceDirectory(), ".template", true) //look for a template TODO allow base filename to be configurable
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

					//1. apply title

					//ensure that the template has the correct <html><head><title> structure
					final Element templateHtmlElement = findHtmlElement(templateDocument)
							.orElseThrow(() -> new IOException(String.format("Template %s has no root <html> element.", templateFile)));
					final Element templateHeadElement = findHtmlHeadElement(templateDocument) //add a <html><head> if not present
							.orElseGet(() -> addFirst(templateHtmlElement, templateDocument.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_HEAD)));
					final Element templateTitleElement = findHtmlHeadTitleElement(templateDocument) //add a <html><head><title> if not present
							.orElseGet(() -> addFirst(templateHeadElement, templateDocument.createElementNS(XHTML_NAMESPACE_URI_STRING, ELEMENT_TITLE)));

					findHtmlHeadTitleElement(sourceDocument).ifPresentOrElse(sourceTitleElement -> {
						//TODO get title from artifact description
						getLogger().debug("  {*} applying source title: {}", sourceTitleElement.getTextContent());
						removeChildren(templateTitleElement);
						appendImportedChildNodes(templateTitleElement, sourceTitleElement);
					}, () -> getLogger().warn("Source file for {} has no title to place in template.", artifact.getSourcePath()));

					//2. apply metadata

					namedMetadata(sourceDocument).filter(meta -> meta.getValue() != null) //ignore any source metadata without a value
							.forEachOrdered(meta -> { //update the template metadata with the source metadata
								getLogger().debug("  {*} applying source metadata: {}={} ", meta.getKey(), meta.getValue());
								setNamedMetata(templateDocument, meta);
							});

					//3. apply content

					final Element templateContentElement = findContentElement(templateDocument)
							.orElseThrow(() -> new IOException(String.format("Template %s has no content insertion point.", templateFile)));
					findContentElement(sourceDocument).ifPresentOrElse(sourceContentElement -> {
						getLogger().debug("  {*} applying source content");
						removeChildren(templateContentElement);
						appendImportedChildNodes(templateContentElement, sourceContentElement);
					}, () -> getLogger().warn("Source file for {} has no content to place in template.", artifact.getSourcePath()));
					return Optional.of(templateDocument);

				})).orElse(sourceDocument); //return the source document unchanged if we can't find a template
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
		processChildElements(context, contextArtifact, artifact, sourceDocument.getDocumentElement()); //process the root children, because the root can't be replaced
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

		if("regenerate".equals(findAttributeNS(sourceElement, "https://guise.io/name/mummy/", "regenerate").orElse(null))) { //TODO use constants; create utility Optional matcher

			//<nav><ol> or <nav><ul>
			if(XHTML_NAMESPACE_URI.toString().equals(sourceElement.getNamespaceURI())) {
				if(ELEMENT_OL.equals(sourceElement.getLocalName()) || ELEMENT_UL.equals(sourceElement.getLocalName())) { //<ol> or <ul>

					if(hasAncestorElementNS(sourceElement, XHTML_NAMESPACE_URI.toString(), ELEMENT_NAV)) { //if this is a navigation list
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
			if(processedElementCount == 1 && processedElements.get(0) == childElement) { //if no structural changes were requested
				continue;
			}
			throw new UnsupportedOperationException("Structural changes not yet supported when processing individual child elements.");
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
		Element discoveredLiTemplate = null;

		for(final Element liElement : (Iterable<Element>)streamOf(navigationListElement.getElementsByTagNameNS(XHTML_NAMESPACE_URI.toString(), ELEMENT_LI))
				.map(Element.class::cast)::iterator) {
			discoveredLiTemplate = liElement;
			break; //TODO do more work to determine styles of active/inactive items and parent/child items
		}

		final Element liTemplate;
		if(discoveredLiTemplate != null) {
			liTemplate = discoveredLiTemplate;
		} else {
			liTemplate = navigationListElement.getOwnerDocument().createElementNS(XHTML_NAMESPACE_URI.toString(), ELEMENT_LI); //<li>
			liTemplate.appendChild(navigationListElement.getOwnerDocument().createElementNS(XHTML_NAMESPACE_URI.toString(), ELEMENT_A)); //<a>
		}

		removeChildren(navigationListElement); //remove existing links

		//add new links from templates
		context.getNavigationArtifacts(contextArtifact).stream().filter(navArtifact -> {
			//TODO add facility for designating and skipping assets in navigation
			return !"css".equals(navArtifact.getSourcePath().getFileName().toString()) && !"js".equals(navArtifact.getSourcePath().getFileName().toString());
		}).forEach(navigationArtifact -> {
			final Element liElement = (Element)liTemplate.cloneNode(true);
			findFirst(liElement.getElementsByTagNameNS(XHTML_NAMESPACE_URI.toString(), ELEMENT_A)).map(Element.class::cast).ifPresent(aElement -> { //find <li><a>
				aElement.setAttributeNS(null, ELEMENT_A_ATTRIBUTE_HREF, context.relativizeSourceReference(contextArtifact, navigationArtifact).toString());
				//remove all text and add the link label
				appendText(removeChildren(aElement), Filenames.removeExtension(navigationArtifact.getSourcePath().getFileName().toString()));
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

	@Override
	public Document relocateDocument(@Nonnull MummyContext context, @Nonnull final Document sourceDocument, @Nonnull final Path originalReferrerSourcePath,
			@Nonnull final Path relocatedReferrerPath, @Nonnull final Function<Artifact, Path> referentArtifactPath) throws IOException, DOMException {
		relocateChildElements(context, sourceDocument.getDocumentElement(), originalReferrerSourcePath, relocatedReferrerPath, referentArtifactPath); //relocate the root children, because the root can't be replaced
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
		if(XHTML_NAMESPACE_URI.toString().equals(sourceElement.getNamespaceURI())) {
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
	 * @see #retargetResourceReference(MummyContext, URIPath, Path, Path, Function)
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
					final URIPath resourceReference = URIPath.fromURI(referenceURI);
					if(resourceReference.isRelative()) { //only convert relative paths TODO maybe later support "context paths", rooted at the site root
						retargetResourceReference(context, resourceReference, originalReferrerSourcePath, relocatedReferrerPath, referentArtifactPath)
								.ifPresentOrElse(retargetedResourceReference -> {
									getLogger().debug("  -> mapping to : {}", retargetedResourceReference);
									referenceElement.setAttributeNS(null, referenceAttributeName, retargetedResourceReference.toString());
								}, () -> getLogger().warn("No target artifact found for source relative reference {}.", resourceReference));
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
	 * Retargets a relative resource reference after relocating the referring source path to a new location, base upon the determined path of the referent
	 * artifact.
	 * <p>
	 * This method supports relocating within the source tree or from the source tree to the target tree.
	 * </p>
	 * @param context The context of static site generation.
	 * @param resourceReference The relative resource reference, e.g. <code>example/test.txt</code>.
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
	protected Optional<URIPath> retargetResourceReference(@Nonnull MummyContext context, @Nonnull URIPath resourceReference,
			@Nonnull final Path originalReferrerSourcePath, @Nonnull final Path relocatedReferrerPath, @Nonnull final Function<Artifact, Path> referentArtifactPath) {
		return context.findArtifactBySourceRelativeReference(originalReferrerSourcePath, resourceReference)
				.map(referentArtifact -> context.relativizeResourceReference(relocatedReferrerPath, referentArtifactPath.apply(referentArtifact)));
	}

}
