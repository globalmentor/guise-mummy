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
import com.globalmentor.io.Filenames;
import com.globalmentor.xml.DefaultEntityResolver;

import io.confound.config.ConfigurationException;
import io.guise.mummy.mummify.Mummifier;
import io.guise.mummy.mummify.OpaqueFileMummifier;
import io.guise.mummy.mummify.SourcePathMummifier;
import io.guise.mummy.mummify.collection.DirectoryMummifier;
import io.guise.mummy.mummify.image.DefaultImageMummifier;
import io.guise.mummy.mummify.page.HtmlPageMummifier;
import io.guise.mummy.mummify.page.MarkdownPageMummifier;
import io.guise.mummy.mummify.page.XhtmlPageMummifier;

/**
 * Abstract base implementation of a mummification context with common default functionality.
 * @implSpec This implementation uses an {@link OpaqueFileMummifier} as the default file mummifier and a {@link DirectoryMummifier} as the default directory
 *           mummifier.
 * @implSpec This implementation registers common mummifiers by default; they can be overridden using {@link #registerFileMummifier(SourcePathMummifier)}.
 * @author Garret Wilson
 */
public abstract class BaseMummyContext implements MummyContext {

	/** The default mummifier for normal files. */
	private final SourcePathMummifier defaultFileMummifier = new OpaqueFileMummifier();

	/** The default mummifier for normal directories. */
	private final SourcePathMummifier defaultDirectoryMummifier = new DirectoryMummifier();

	/** The registered mummifiers by supported extensions. These extensions are in canonical (lowercase) form. */
	private final Map<String, SourcePathMummifier> fileMummifiersByExtension = new HashMap<>();

	/**
	 * Registers a mummify for all its supported filename extensions. If an extension is already registered with another mummifier, it will be overridden.
	 * @param mummifier The mummifier to register.
	 * @see Mummifier#getSupportedFilenameExtensions()
	 */
	protected void registerFileMummifier(@Nonnull final SourcePathMummifier mummifier) {
		mummifier.getSupportedFilenameExtensions().stream()
				//normalize extensions so we can look up without regard to case
				.map(Filenames.Extensions::normalize).forEach(ext -> fileMummifiersByExtension.put(ext, mummifier));
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version returns {@link GuiseMummy#LABEL}, usually a string in the form <code>Guise Mummy <var>version</var></code>.
	 */
	@Override
	public String getMummifierIdentification() {
		return GuiseMummy.LABEL;
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
		registerFileMummifier(new MarkdownPageMummifier());
		registerFileMummifier(new XhtmlPageMummifier());
		registerFileMummifier(new HtmlPageMummifier());
		registerFileMummifier(new DefaultImageMummifier());
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

	@Override
	public SourcePathMummifier getDefaultSourceFileMummifier() {
		return defaultFileMummifier;
	}

	@Override
	public SourcePathMummifier getDefaultSourceDirectoryMummifier() {
		return defaultDirectoryMummifier;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation finds a registered mummifier based upon the normalized filename extension (without regard to case).
	 */
	@Override
	public Optional<SourcePathMummifier> findRegisteredMummifierForSourceFile(@Nonnull final Path sourceFile) {
		return filenameExtensions(sourceFile).map(Filenames.Extensions::normalize).map(fileMummifiersByExtension::get).filter(Objects::nonNull).findFirst();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation doesn't support registered source directory mummifiers, and will always return {@link Optional#empty()}.
	 */
	@Override
	public Optional<SourcePathMummifier> findRegisteredMummifierForSourceDirectory(Path sourceDirectory) {
		return Optional.empty();
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
