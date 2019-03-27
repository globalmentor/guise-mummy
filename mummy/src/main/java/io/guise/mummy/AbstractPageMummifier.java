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

import static com.globalmentor.html.spec.HTML.*;
import static com.globalmentor.io.Paths.*;
import static com.globalmentor.xml.XML.*;
import static java.nio.file.Files.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.*;

import org.w3c.dom.*;

import com.globalmentor.html.HtmlSerializer;
import com.globalmentor.html.spec.HTML;
import com.globalmentor.io.Filenames;
import com.globalmentor.net.URIPath;

/**
 * Abstract base mummifier for generating HTML pages.
 * @author Garret Wilson
 */
public abstract class AbstractPageMummifier extends AbstractSourcePathMummifier implements PageMummifier {

	/**
	 * {@inheritDoc}
	 * @implSpec This version changes the output file extension to <code>html</code>.
	 */
	@Override
	protected Path getArtifactTargetPath(final MummyContext context, final Path sourceFile) {
		return changeExtension(super.getArtifactTargetPath(context, sourceFile), "html"); //TODO use constant
	}

	@Override
	public Artifact plan(final MummyContext context, final Path sourcePath) throws IOException {
		return new PageArtifact(this, sourcePath, getArtifactTargetPath(context, sourcePath));
	}

	@Override
	public void mummify(final MummyContext context, final Artifact contextArtifact, final Artifact artifact) throws IOException {

		try {

			//#load source document: get starting content to work with
			final Document sourceDocument = loadSourceDocument(context, contextArtifact, artifact, artifact.getSourcePath());
			getLogger().debug("loaded source document: {}", artifact.getSourcePath());

			final Document templatedocument = applyTemplate(context, contextArtifact, artifact, sourceDocument);

			//#process document: evaluate expressions and perform transformations
			final Document processedDocument = processDocument(context, contextArtifact, artifact, templatedocument);

			//#relocate document: translate path references from the source to the target
			final Document relocatedDocument = relocateDocument(context, contextArtifact, artifact, processedDocument);

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
	 * @throws IOException if there is an error applying a tmeplate.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected Document applyTemplate(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Document sourceDocument) throws IOException, DOMException {
		context.findPageSourceFile(artifact.getSourceDirectory(), ".template", true) //TODO allow base filename to be configurable
				.ifPresent(template -> getLogger().debug("  {} found template: " + template.getKey())); //TODO implement template application
		return sourceDocument;
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

		if("regenerate".equals(getDefinedAttributeNS(sourceElement, "https://guise.io/name/mummy/", "regenerate"))) { //TODO use constants

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
	 * Relocates a document by retargeting its references.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param sourceDocument The source document to relocate.
	 * @return The relocated document, which may or may not be the same document supplied as input.
	 * @throws IOException if there is an error relocating the document.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected Document relocateDocument(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Document sourceDocument) throws IOException, DOMException {
		relocateChildElements(context, contextArtifact, artifact, sourceDocument.getDocumentElement()); //relocate the root children, because the root can't be replaced
		return sourceDocument;
	}

	/**
	 * Relocates a source document element by retargeting its references.
	 * <p>
	 * The element is replaced with the returned elements. If only the same element is returned, no replacement is made. If no element is returned, the source
	 * element is removed.
	 * </p>
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param sourceElement The source element to relocate.
	 * @return The relocated element(s), if any, to replace the source element.
	 * @throws IOException if there is an error relocating the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected List<Element> relocateElement(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Element sourceElement) throws IOException, DOMException {

		//TODO transfer to some system of pluggable element relocating strategies

		//<a> TODO add support for other links, such as stylesheet links
		if(XHTML_NAMESPACE_URI.toString().equals(sourceElement.getNamespaceURI())) {
			if(ELEMENT_A.equals(sourceElement.getLocalName())) { //<a>
				return relocateLink(context, contextArtifact, artifact, sourceElement);
			}
		}

		relocateChildElements(context, contextArtifact, artifact, sourceElement);

		return List.of(sourceElement);
	}

	/**
	 * Relocates child elements of an existing element by retargeting references.
	 * @implNote This implementation does not yet allow returning different nodes than the one being relocated.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param sourceElement The source element the children of which to relocate.
	 * @throws IOException if there is an error relocating the child elements.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected void relocateChildElements(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Element sourceElement) throws IOException, DOMException {
		final NodeList childNodes = sourceElement.getChildNodes();
		for(int childNodeIndex = 0; childNodeIndex < childNodes.getLength();) { //advance the index manually as needed
			final Node childNode = childNodes.item(childNodeIndex);
			if(!(childNode instanceof Element)) { //skip non-elements
				childNodeIndex++;
				continue;
			}
			final Element childElement = (Element)childNode;
			final List<Element> relocatedElements = relocateElement(context, contextArtifact, artifact, childElement);
			final int relocatedElementCount = relocatedElements.size();
			childNodeIndex += relocatedElementCount; //manually advance the index based upon the replacement nodes
			if(relocatedElementCount == 1 && relocatedElements.get(0) == childElement) { //if no structural changes were requested
				continue;
			}
			throw new UnsupportedOperationException("Structural changes not yet supported when relocating individual child elements.");
		}
	}

	/**
	 * Relocates a link element by retargeting its {@value HTML#ELEMENT_A_ATTRIBUTE_HREF} attribute.
	 * <p>
	 * The element is replaced with the returned elements. If only the same element is returned, no replacement is made. If no element is returned, the source
	 * element is removed.
	 * </p>
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @param linkElement The list element such a {@code <a>} to relocate.
	 * @return The relocated element(s), if any, to replace the source element.
	 * @throws IOException if there is an error relocating the element.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	protected List<Element> relocateLink(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact,
			@Nonnull final Element linkElement) throws IOException, DOMException {
		findAttributeNS(linkElement, null, ELEMENT_A_ATTRIBUTE_HREF).ifPresent(href -> {
			getLogger().debug("  - found an href: {}", href);
			//TODO check for the empty string and do something appropriate
			final URI hrefURI = URI.create(href);
			if(!hrefURI.isAbsolute()) { //only convert paths
				final URIPath hrefPath = URIPath.fromURI(hrefURI);
				if(hrefPath.isRelative()) { //only convert relative paths TODO maybe later support "context paths", rooted at the site root
					context.findArtifactBySourceRelativeReference(contextArtifact, hrefPath).ifPresentOrElse(referentArtifact -> {
						getLogger().debug("  -> found referent artifact: {}", referentArtifact);
						final URIPath relativeTargetReference = context.relativizeTargetReference(contextArtifact, referentArtifact);
						getLogger().debug("  -> -> mapping to : {}", relativeTargetReference);
						linkElement.setAttributeNS(null, ELEMENT_A_ATTRIBUTE_HREF, relativeTargetReference.toString());
					}, () -> getLogger().warn("No target artifact found for source relative reference {}.", hrefPath));
				}
			}
		});

		return List.of(linkElement);

	}

}
