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

import static com.globalmentor.html.spec.HTML.*;
import static io.guise.mummy.GuiseMummy.*;
import static java.nio.charset.StandardCharsets.*;
import static java.nio.file.Files.*;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.annotation.*;

import org.w3c.dom.*;

import com.globalmentor.net.ContentType;
import com.globalmentor.xml.spec.NsName;

import io.guise.mummy.*;
import io.guise.mummy.mummify.Mummifier;

/**
 * Mummifier for generating HTML pages.
 * @implSpec This type of mummifier only works with {@link SourceFileArtifact}s. If some other type of artifact is used, the mummification methods may throw a
 *           {@link ClassNotFoundException}.
 * @author Garret Wilson
 */
public interface PageMummifier extends Mummifier {

	/** The standard Internet media types for generated pages: <code>text/html</code> in UTF-8. */
	public static final ContentType PAGE_MEDIA_TYPE = HTML_MEDIA_TYPE.withCharset(UTF_8);

	/** The extension to use for generated page filenames, unless bare names are enabled. */
	public static String PAGE_NAME_EXTENSION = HTML_NAME_EXTENSION;

	/** The HTML {@code <meta>} name for indicating the instant of artifact generation. */
	public static final String META_NAME_GENERATED_AT = "generated-at";

	/** The attribute for regenerating an element, such as a navigation list. */
	public static final NsName ATTRIBUTE_REGENERATE = NsName.of(NAMESPACE_STRING, "regenerate");

	/** The Guise Mesh context variable name for exposing the current artifact. */
	public static final String MESH_CONTEXT_VARIABLE_ARTIFACT = "artifact";

	/**
	 * The Guise Mesh context variable name for exposing the current page resource description.
	 * @see Artifact#getResourceDescription()
	 */
	public static final String MESH_CONTEXT_VARIABLE_PAGE = "page";

	/**
	 * Determines whether the given artifact is marked as an <dfn>asset</dfn>.
	 * @implSpec The default implementation considers an asset any artifact with a source path the source filename of which matches the pattern configured under
	 *           the {@value GuiseMummy#CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN} key. Asset parent directories are not considered, as an asset directory would
	 *           normally not be mummifying pages within that tree anyway. For example with a pattern of <code>/\$(.+)/</code> <code>…/foo/$bar.html</code> would
	 *           be considered an asset but <code>…/$foo/bar.html</code> would not.
	 * @param context The context of static site generation.
	 * @param artifact The artifact to check.
	 * @return <code>true</code> if the artifact should <em>not</em> be included in normal navigation.
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN
	 */
	public default boolean isAsset(@Nonnull MummyContext context, @Nonnull final Artifact artifact) {
		final Pattern assetNamePattern = context.getConfiguration().getObject(CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN, Pattern.class);
		final Path artifactPath = artifact.getSourcePath().getFileName();
		return artifactPath != null && assetNamePattern.matcher(artifactPath.toString()).matches();
	}

	/**
	 * Determines whether the given artifact is <dfn>veiled</dfn>; that is, hidden from navigation. The artifact will still be available for direct retrieval.
	 * @implSpec The default implementation considers veiled any artifact with a source path the source filename of which matches the pattern configured under the
	 *           {@value GuiseMummy#CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN} key. Veiled parent directories are not considered, as veiling only affects a single level.
	 *           For example with a pattern of <code>/_(.+)/</code> <code>…/foo/_bar.txt</code> would be considered veiled but <code>…/_foo/bar.txt</code> would
	 *           not.
	 * @param context The context of static site generation.
	 * @param artifact The artifact to check.
	 * @return <code>true</code> if the artifact should <em>not</em> be included in normal navigation.
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN
	 */
	public default boolean isVeiled(@Nonnull MummyContext context, @Nonnull final Artifact artifact) {
		final Pattern veilNamePattern = context.getConfiguration().getObject(CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN, Pattern.class);
		final Path artifactPath = artifact.getSourcePath().getFileName();
		return artifactPath != null && veilNamePattern.matcher(artifactPath.toString()).matches();
	}

	/**
	 * Finds the artifact suitable to serve as parent level navigation for the artifacts at the current level. This will be the context artifact if the context
	 * artifact has child artifacts.
	 * @apiNote This method finds the parent navigation artifact independent of any <code>.navigation.lst</code> file.
	 * @implSpec The default implementation returns the context artifact itself if it is an instance of {@link CollectionArtifact}; otherwise the parent artifact,
	 *           if any, is returned by calling {@link MummyContext#findParentArtifact(Artifact)}.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @return The artifacts for navigation to the parent of the current navigation level.
	 * @see #childNavigationArtifacts(MummyContext, Artifact)
	 * @see MummyContext#findParentArtifact(Artifact)
	 */
	public default Optional<Artifact> findParentNavigationArtifact(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact) {
		return contextArtifact instanceof CollectionArtifact ? Optional.of(contextArtifact) : context.findParentArtifact(contextArtifact);
	}

	/**
	 * Provides the artifacts suitable for direct subsequent navigation from this artifact, <em>excluding</em> the parent artifact. If sibling artifacts are
	 * returned, they will include the given resource.
	 * @apiNote The returned navigation artifacts are not necessarily children of the context artifact, but rather artifacts at the child level beneath some
	 *          parent.
	 * @apiNote This method allows access to child navigation artifacts independent of any explicit navigation list override defined by the user. It is thus
	 *          appropriate for access by a directory widget, for example, to provide custom navigation independent of any <code>.navigation.lst</code> file.
	 * @implSpec The default implementation retrieves candidate resources using {@link MummyContext#childArtifacts(Artifact)} if the artifact is a
	 *           {@link CollectionArtifact}; otherwise it calls {@link MummyContext#siblingArtifacts(Artifact)}. Only artifacts that are not assets and are not
	 *           veiled are included.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @return The artifacts for subsequent navigation from this artifact.
	 * @see #findParentNavigationArtifact(MummyContext, Artifact)
	 * @see MummyContext#childArtifacts(Artifact)
	 * @see MummyContext#siblingArtifacts(Artifact)
	 * @see #isAsset(MummyContext, Artifact)
	 * @see #isVeiled(MummyContext, Artifact)
	 */
	public default Stream<Artifact> childNavigationArtifacts(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact) {
		final Stream<Artifact> candidateArtifacts = contextArtifact instanceof CollectionArtifact ? context.childArtifacts(contextArtifact)
				: context.siblingArtifacts(contextArtifact);
		return candidateArtifacts.filter(Artifact::isNavigable).filter(artifact -> !isAsset(context, artifact)).filter(artifact -> !isVeiled(context, artifact));
	}

	/**
	 * Provides the tree of items suitable for direct navigation from this artifact. The may include the parent artifact, sibling artifacts, and/or the given
	 * resource itself. This official list may be overridden by the user through the use of a <code>.navigation.lst</code> file or other configured file.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @return The navigation items, in order, that constitute the official possible navigation destinations from this artifact. The stream must <em>not</em>
	 *         throw an {@link IOException} during iteration.
	 * @throws IllegalArgumentException if the information of the navigation artifacts prevent them from being ordered.
	 * @throws IOException If there is an I/O error determining the navigation.
	 * @see #findParentNavigationArtifact(MummyContext, Artifact)
	 * @see #childNavigationArtifacts(MummyContext, Artifact)
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_NAVIGATION_BASE_NAME
	 */
	public Stream<NavigationItem> navigation(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Artifact artifact)
			throws IOException;

	//# load

	/**
	 * Loads the source file and returns it as a document to be further refined before being used to generate the artifact.
	 * <p>
	 * The returned document will not yet have been processed. For example, no expressions will have been evaluated and links will still reference source paths.
	 * </p>
	 * <p>
	 * The document must be in XHTML using the HTML namespace.
	 * </p>
	 * @implSpec The default implementation opens an input stream to the given file and then loads the source document by calling
	 *           {@link #loadSourceDocument(MummyContext, InputStream, String)}.
	 * @param context The context of static site generation.
	 * @param sourceFile The file from which to load the document.
	 * @return A document describing the source content of the artifact to generate.
	 * @throws IOException if there is an error loading and/or converting the source file contents.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public default Document loadSourceDocument(@Nonnull MummyContext context, @Nonnull Path sourceFile) throws IOException, DOMException {
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(sourceFile))) {
			return loadSourceDocument(context, inputStream, sourceFile.toString());
		}
	}

	/**
	 * Loads the source of some artifact and returns it as a document to be further refined before being used to generate the artifact.
	 * <p>
	 * The returned document will not yet have been processed. For example, no expressions will have been evaluated and links will still reference source paths.
	 * </p>
	 * <p>
	 * The document must be in XHTML using the HTML namespace.
	 * </p>
	 * @apiNote The returned document need not include any named metadata (i.e. {@code <meta name="…"> elements}) unless this mummifier's implementation for
	 *          loading source metadata for the artifact description delegates to this method.
	 * @implSpec The default implementation opens an input stream using {@link SourceFileArtifact#openSource(MummyContext)} and then loads the source document by
	 *           calling {@link #loadSourceDocument(MummyContext, InputStream, String)}.
	 * @param context The context of static site generation.
	 * @param artifact The artifact for which to load the document.
	 * @return A document describing the source content of the artifact to generate.
	 * @throws IOException if there is an error loading and/or converting the source file contents.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public default Document loadSourceDocument(@Nonnull MummyContext context, @Nonnull SourceFileArtifact artifact) throws IOException, DOMException {
		try (final InputStream inputStream = new BufferedInputStream(artifact.openSource(context))) {
			return loadSourceDocument(context, inputStream, artifact.getSourcePath().toString());
		}
	}

	/**
	 * Loads a source document from the given input stream and returns it as a document to be further refined before being used to generate the artifact.
	 * <p>
	 * The returned document will not yet have been processed. For example, no expressions will have been evaluated and links will still reference source paths.
	 * </p>
	 * <p>
	 * The document must be in XHTML using the HTML namespace.
	 * </p>
	 * @apiNote The returned document need not include any named metadata (i.e. {@code <meta name="…"> elements}) unless this mummifier's implementation for
	 *          loading source metadata for the artifact description delegates to this method.
	 * @param context The context of static site generation.
	 * @param inputStream The input stream from which to to load the document.
	 * @param name The full source identifier of the document, such as a path or URL.
	 * @return A document describing the source content of the artifact to generate.
	 * @throws IOException if there is an error loading and/or converting the source file contents.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public Document loadSourceDocument(@Nonnull MummyContext context, @Nonnull InputStream inputStream, @Nonnull final String name)
			throws IOException, DOMException;

	//## load excerpt

	/**
	 * Loads an excerpt from the given source file and returns it as a document fragment.
	 * <p>
	 * The document must be in XHTML using the HTML namespace.
	 * </p>
	 * @implSpec The default implementation opens an input stream to the given file and then loads the source document by calling
	 *           {@link #loadSourceExcerpt(MummyContext, InputStream, String)}.
	 * @implNote The returned document fragment will not yet have been processed. For example, no expressions will have been evaluated and links will still
	 *           reference source paths. This will likely be changed or otherwise improved in the future.
	 * @param context The context of static site generation.
	 * @param sourceFile The file from which to load the excerpt.
	 * @return A document fragment providing an excerpt, if available, of the source content of the artifact to generate.
	 * @throws IOException if there is an error loading and/or converting the source file contents.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public default Optional<DocumentFragment> loadSourceExcerpt(@Nonnull MummyContext context, @Nonnull Path sourceFile) throws IOException, DOMException {
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(sourceFile))) {
			return loadSourceExcerpt(context, inputStream, sourceFile.toString());
		}
	}

	/**
	 * Loads an excerpt of some artifact and returns it as a document fragment.
	 * <p>
	 * The document fragment must be in XHTML using the HTML namespace.
	 * </p>
	 * @implSpec The default implementation opens an input stream using {@link SourceFileArtifact#openSource(MummyContext)} and then loads the source excerpt by
	 *           calling {@link #loadSourceExcerpt(MummyContext, InputStream, String)}.
	 * @implNote The returned document fragment will not yet have been processed. For example, no expressions will have been evaluated and links will still
	 *           reference source paths. This will likely be changed or otherwise improved in the future.
	 * @param context The context of static site generation.
	 * @param artifact The artifact for which to load the excerpt.
	 * @return A document fragment providing an excerpt, if available, of the source content of the artifact to generate.
	 * @throws IOException if there is an error loading and/or converting the source file contents.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public default Optional<DocumentFragment> loadSourceExcerpt(@Nonnull MummyContext context, @Nonnull SourceFileArtifact artifact)
			throws IOException, DOMException {
		try (final InputStream inputStream = new BufferedInputStream(artifact.openSource(context))) {
			return loadSourceExcerpt(context, inputStream, artifact.getSourcePath().toString());
		}
	}

	/**
	 * Loads an excerpt from the given source input stream and returns it as a document fragment.
	 * <p>
	 * The document fragment must be in XHTML using the HTML namespace.
	 * </p>
	 * @implNote The returned document fragment will not yet have been processed. For example, no expressions will have been evaluated and links will still
	 *           reference source paths. This will likely be changed or otherwise improved in the future.
	 * @param context The context of static site generation.
	 * @param inputStream The input stream from which to to load the excerpt.
	 * @param name The full source identifier of the document, such as a path or URL.
	 * @return A document fragment providing an excerpt, if available, of the source content of the artifact to generate.
	 * @throws IOException if there is an error loading and/or converting the source file contents.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public Optional<DocumentFragment> loadSourceExcerpt(@Nonnull MummyContext context, @Nonnull InputStream inputStream, @Nonnull final String name)
			throws IOException, DOMException;

	//# relocate

	/**
	 * Relocates a document by retargeting its references relative to a new referrer path location.
	 * @param context The context of static site generation.
	 * @param sourceDocument The source document to relocate.
	 * @param originalReferrerSourcePath The absolute original path of the referrer, e.g. <code>…/foo/page.xhtml</code>.
	 * @param relocatedReferrerPath The absolute relocated path of the referrer, e.g. <code>…/bar/page.xhtml</code>.
	 * @param referentArtifactPath The function for determining the path of the determined referent artifact. This function should return either the source path
	 *          or the destination path of the artifact concordant with the site tree of the relocated referrer.
	 * @return The relocated document, which may or may not be the same document supplied as input.
	 * @throws IOException if there is an error relocating the document.
	 * @throws DOMException if there is some error manipulating the XML document object model.
	 */
	public Document relocateDocument(@Nonnull MummyContext context, @Nonnull final Document sourceDocument, @Nonnull final Path originalReferrerSourcePath,
			@Nonnull final Path relocatedReferrerPath, @Nonnull final Function<Artifact, Path> referentArtifactPath) throws IOException, DOMException;

}
