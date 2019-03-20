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
import java.nio.file.Path;
import java.util.List;

import javax.annotation.*;

import org.w3c.dom.*;

import com.globalmentor.html.HtmlSerializer;

/**
 * Abstract base mummifier for generating HTML pages.
 * @author Garret Wilson
 */
public abstract class AbstractPageMummifier extends AbstractSourcePathMummifier {

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

		final Document sourceDocument = loadSourceDocument(artifact.getSourcePath());
		System.out.println("loaded source document: " + artifact.getSourcePath());

		final Document processedDocument = processDocument(sourceDocument);

		try (final OutputStream outputStream = new BufferedOutputStream(newOutputStream(artifact.getTargetPath()))) {
			final HtmlSerializer htmlSerializer = new HtmlSerializer(true);
			htmlSerializer.serialize(processedDocument, outputStream);
		}
		System.out.println("generated output document: " + artifact.getTargetPath());

	}

	/**
	 * Loads the source file and returns it as a document to be further refined before being used to generate the artifact.
	 * <p>
	 * The returned document will not yet have been processed. For example, no expressions will have been evaluated and links will still reference source paths.
	 * </p>
	 * <p>
	 * The document must be in XHTML using the HTML namespace.
	 * </p>
	 * @param sourceFile The file from which to load the document.
	 * @return A document describing the source content of the artifact to generate.
	 * @throws IOException if there is an error loading and/or converting the source file contents.
	 */
	protected abstract Document loadSourceDocument(@Nonnull final Path sourceFile) throws IOException;

	/**
	 * Processes a source document before it is converted to an output document.
	 * @param sourceDocument The source document to process.
	 * @return The processed document ready, which may or may not be the same document supplied as input.
	 * @throws IOException if there is an error processing the document.
	 */
	protected Document processDocument(@Nonnull final Document sourceDocument) throws IOException {
		processChildElements(sourceDocument.getDocumentElement()); //process the root children, because the root can't be replaced
		return sourceDocument;
	}

	/**
	 * Processes a source document element.
	 * <p>
	 * The element is replaced with the returned elements. If only the same element is returned, no replacement is made. If no element is returned, the source
	 * element is removed.
	 * </p>
	 * @param sourceElement The source element to process.
	 * @return The processed element(s), if any, to replace the source element.
	 * @throws IOException if there is an error processing the element.
	 */
	protected List<Element> processElement(@Nonnull final Element sourceElement) throws IOException {

		//TODO transfer to some system of pluggable element processing strategies

		if(XHTML_NAMESPACE_URI.toString().equals(sourceElement.getNamespaceURI())) {
			if(ELEMENT_OL.equals(sourceElement.getLocalName()) || ELEMENT_UL.equals(sourceElement.getLocalName())) { //<ol> or <ul>

				boolean inMummyRegenerateNav = false; //TODO create XML utility method hasParent()
				Node parentNode = sourceElement.getParentNode();
				while(!inMummyRegenerateNav && parentNode instanceof Element) {
					final Element parentElement = (Element)parentNode;
					if(XHTML_NAMESPACE_URI.toString().equals(parentNode.getNamespaceURI()) && ELEMENT_NAV.equals(parentNode.getLocalName())) { //<nav>
						//TODO create an Optional findDefinedAttributeNS() method, with true URI variation
						if("regenerate".equals(getDefinedAttributeNS(parentElement, "https://guise.io/name/mummy/", "regenerate"))) { //TODO use constants
							inMummyRegenerateNav = true;
						}
					}
					parentNode = parentNode.getParentNode();
				}

				if(inMummyRegenerateNav) {
					return regenerateNavigationList(sourceElement);
				}
			}
		}

		processChildElements(sourceElement);

		return List.of(sourceElement);
	}

	/**
	 * Processes child elements of an existing element.
	 * @implNote This implementation does not yet allow returning different nodes than the one being processed.
	 * @param sourceElement The source element the children of which to process.
	 * @throws IOException if there is an error processing the child elements.
	 */
	protected void processChildElements(@Nonnull final Element sourceElement) throws IOException {
		final NodeList childNodes = sourceElement.getChildNodes();
		for(int childNodeIndex = 0; childNodeIndex < childNodes.getLength();) { //advance the index manually as needed
			final Node childNode = childNodes.item(childNodeIndex);
			if(!(childNode instanceof Element)) { //skip non-elements
				childNodeIndex++;
				continue;
			}
			final Element childElement = (Element)childNode;
			final List<Element> processedElements = processElement(childElement);
			final int processedElementCount = processedElements.size();
			childNodeIndex += processedElementCount; //manually advance the index based upon the replacement nodes
			if(processedElementCount == 1 && processedElements.get(0) == childElement) { //if no structural changes were requested
				continue;
			}
			throw new UnsupportedOperationException("Structural changes not yet supported when processing individual child elements.");
		}
	}

	/**
	 * Processes a source document element.
	 * <p>
	 * The element is replaced with the returned elements. If only the same element is returned, no replacement is made. If no element is returned, the source
	 * element is removed.
	 * </p>
	 * @param sourceElement The source element to process.
	 * @return The processed element(s), if any, to replace the source element.
	 * @throws IOException if there is an error processing the element.
	 */
	protected List<Element> regenerateNavigationList(@Nonnull final Element navigationListElement) throws IOException {
		System.out.println("-----> We should regenerate a navigation list!");
		return List.of(navigationListElement);
	}

}
