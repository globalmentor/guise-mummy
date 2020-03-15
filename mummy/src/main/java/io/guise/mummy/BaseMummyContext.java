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
import static com.globalmentor.java.Conditions.*;
import static java.nio.file.Files.*;
import static java.util.Objects.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import javax.annotation.*;
import javax.xml.parsers.*;

import org.xml.sax.*;

import com.globalmentor.html.spec.HTML;
import com.globalmentor.xml.DefaultEntityResolver;

import io.confound.config.ConfigurationException;

/**
 * Abstract base implementation of a mummification context with common default functionality.
 * @author Garret Wilson
 */
public abstract class BaseMummyContext implements MummyContext {

	/** The segment prefix that indicates a veiled resource or resource parent. */
	public static final String VEILED_PATH_SEGMENT_PREFIX = "_";

	/**
	 * {@inheritDoc}
	 * @implSpec This version returns a string in the form <code>Guise Mummy <em>version</em></code>.
	 */
	@Override
	public String getMummifierIdentification() {
		return GuiseMummy.NAME + ' ' + GuiseMummy.VERSION;
	}

	private final GuiseProject project;

	@Override
	public GuiseProject getProject() {
		return project;
	}

	/** The shared page document builder factory. Use must be synchronized on the factory itself. */
	private final DocumentBuilderFactory pageDocumentBuilderFactory;

	/**
	 * Constructor.
	 * @param project The Guise project.
	 */
	public BaseMummyContext(@Nonnull final GuiseProject project) {
		this.project = requireNonNull(project);
		pageDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
		pageDocumentBuilderFactory.setNamespaceAware(true);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This specification currently ignores dotfiles, for example <code>.git</code> and <code>.gitignore</code>; as well as non-regular files.
	 */
	public boolean isIgnore(final Path sourcePath) {
		if(isDotfile(sourcePath)) { //ignore dotfiles
			return true;
		}
		if(!isRegularFile(sourcePath) && !isDirectory(sourcePath)) { //TODO add option to traverse symbolic links
			return true;
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation considers veiled any source path the source filename of which, or the filename of any parent source directory of which
	 *           (within the site), starts with {@value #VEILED_PATH_SEGMENT_PREFIX}. For example both <code>…/_foo/bar.txt</code> and <code>…/foo/_bar.txt</code>
	 *           would be considered veiled.
	 */
	@Override
	public boolean isVeiled(Path sourcePath) {
		final Path siteSourceDirectory = getSiteSourceDirectory();
		while(!sourcePath.equals(siteSourceDirectory)) {
			if(sourcePath.getFileName().toString().startsWith(VEILED_PATH_SEGMENT_PREFIX)) {
				return true;
			}
			sourcePath = sourcePath.getParent();
			assert sourcePath != null : "Source path is expected to be inside site source directory.";
		}
		return false;
	}

	private final Map<Artifact, Artifact> parentArtifactsByArtifact = new HashMap<>();

	@Override
	public Optional<Artifact> findParentArtifact(final Artifact artifact) {
		return Optional.ofNullable(parentArtifactsByArtifact.get(requireNonNull(artifact)));
	}

	private final Map<Path, Artifact> artifactsByReferenceSourcePath = new HashMap<>();

	@Override
	public Optional<Artifact> findArtifactBySourceReference(final Path referenceSourcePath) {
		return Optional.ofNullable(artifactsByReferenceSourcePath.get(checkArgumentAbsolute(referenceSourcePath)));
	}

	/**
	 * Recursively updates the mummification plan for the given artifact. Parent artifacts are updated in the map, for example.
	 * @param artifact The artifact the plan of which to update.
	 */
	protected void updatePlan(@Nonnull final Artifact artifact) {
		requireNonNull(artifact);
		artifact.getReferentSourcePaths().forEach(referenceSourcePath -> artifactsByReferenceSourcePath.put(referenceSourcePath, artifact));
		if(artifact instanceof CollectionArtifact) {
			for(final Artifact childArtifact : ((CollectionArtifact)artifact).getChildArtifacts()) {
				parentArtifactsByArtifact.put(childArtifact, artifact); //map the parent to the child
				updatePlan(childArtifact); //recursively update the plan for the children
			}
		}
	}

	//factory methods

	/**
	 * Special Guise Mummy entity resolver with additional capabilities.
	 * @implSpec This implementation uses preloaded versions of frequently-used XHTML-related DTDs and other entities instead of downloading them from external
	 *           sources by using {@link DefaultEntityResolver}.
	 * @implSpec If the {@value HTML#XHTML_1_1_PUBLIC_ID} DTD is requested, the {@value HTML#XHTML_1_0_STRICT_PUBLIC_ID} will be returned instead, which results
	 *           in faster parsing and does not produce unnecessary and incorrect default attributes. For most XHTML 1.1 documents there will be no effective
	 *           difference. See <a href="https://stackoverflow.com/q/60603441/421049">Java XML parser adding unnecessary xmlns and xml:space attributes</a> and
	 *           <a href="https://www.w3.org/TR/xhtml11/changes.html">XHTML 1.1 - Second Edition § A. Changes from XHTML 1.0 Strict</a>.
	 */
	private static final EntityResolver ENTITY_RESOLVER = new EntityResolver() {

		private final EntityResolver defaultEntityResolver = DefaultEntityResolver.getInstance();

		@Override
		public InputSource resolveEntity(final String publicID, final String systemID) throws SAXException, IOException {
			if(XHTML_1_1_PUBLIC_ID.equals(publicID)) { //parse XHTML 1.1 documents as XHTML 1.0 Strict
				final InputSource inputSource = resolveEntity(XHTML_1_0_STRICT_PUBLIC_ID, systemID);
				checkState(inputSource != null, "Default entity resolver should have known the XHTML 1.0 Strict DTD `%s`.", XHTML_1_0_STRICT_PUBLIC_ID);
				inputSource.setPublicId(publicID);
				return inputSource;
			}
			return defaultEntityResolver.resolveEntity(publicID, systemID);
		}

	};

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns a document builder that uses preloaded versions of frequently-used XHTML-related DTDs and other entities instead of
	 *           downloading them from external sources. In addition, for any documents using the XHTML 1.1 DTD, the document will actually be parsed using the
	 *           the XHTML 1.0 Strict DTD instead, which is faster and does not result in unnecessary and incorrect default attributes. For most XHTML 1.1
	 *           documents there will be no effective difference. See <a href="https://stackoverflow.com/q/60603441/421049">Java XML parser adding unnecessary
	 *           xmlns and xml:space attributes</a> and <a href="https://www.w3.org/TR/xhtml11/changes.html">XHTML 1.1 - Second Edition § A. Changes from XHTML
	 *           1.0 Strict</a>.
	 * @implSpec This implementation synchronizes on the internal document builder factory instance.
	 */
	@Override
	public DocumentBuilder newPageDocumentBuilder() {
		synchronized(pageDocumentBuilderFactory) {
			try {
				final DocumentBuilder documentBuilder = pageDocumentBuilderFactory.newDocumentBuilder();
				documentBuilder.setEntityResolver(ENTITY_RESOLVER); //install an entity resolver that knows about many XHTML-related entities
				return documentBuilder;
			} catch(final ParserConfigurationException parserConfigurationException) {
				throw new ConfigurationException(parserConfigurationException);
			}
		}
	}

}
