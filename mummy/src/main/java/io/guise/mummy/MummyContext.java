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

import static com.globalmentor.io.Paths.*;
import static com.globalmentor.net.URIs.*;
import static java.nio.file.Files.*;
import static java.util.Objects.*;
import static java.util.function.Predicate.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;
import javax.xml.parsers.DocumentBuilder;

import com.globalmentor.net.URIPath;

import io.confound.config.*;
import io.guise.mummy.deploy.*;
import io.guise.mummy.mummify.SourcePathMummifier;
import io.guise.mummy.mummify.page.PageMummifier;

/**
 * Provides information about context of static site generation.
 * @author Garret Wilson
 */
public interface MummyContext {

	/**
	 * Returns a free-form string identifying the program generating the content.
	 * @return The generator identification string.
	 */
	public String getMummifierIdentification();

	/** @return The Guise project governing mummification. */
	public GuiseProject getProject();

	/** @return <code>true</code> if full mummification is enabled; <code>false</code> if mummification is incremental. */
	public boolean isFull();

	/**
	 * Indicates whether mummification should be incremental.
	 * @implSpec The default implementation delegates to {@link #isFull()} and returns the opposite value.
	 * @return <code>true</code> if incremental mummification is enabled.
	 */
	public default boolean isIncremental() {
		return !isFull();
	}

	/**
	 * Returns some URI indicating the root of the current context, that is, the site source directory. All resource context paths are interpreted relative to
	 * this root.
	 * @apiNote This method will typically but not necessarily return the URI form of the site source directory.
	 * @return The URI that represents the root of the current context.
	 */
	public default URI getRoot() { //TODO move to some abstract base class; if it is configurable, it shouldn't have a default implementation
		return getSiteSourceDirectory().toUri();
	}

	/**
	 * Returns the base directory of the entire site source, representing the root of the context.
	 * @implSpec The default implementation retrieves the value for key {@value GuiseMummy#PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY} from the configuration and
	 *           resolves it against the project directory.
	 * @apiNote This is analogous to Maven's <code>${project.basedir}/src/site</code> directory.
	 * @return The base directory of the site being mummified.
	 * @see GuiseProject#getDirectory()
	 */
	public default Path getSiteSourceDirectory() {
		return getProject().getDirectory().resolve(getConfiguration().getPath(GuiseMummy.PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY));
	}

	/**
	 * Returns the output directory of the entire site, representing the root of the context.
	 * @implSpec The default implementation retrieves the value for key {@value GuiseMummy#PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY} from the configuration and
	 *           resolves it against the project directory.
	 * @apiNote This is analogous to Maven's <code>${project.build.directory}</code> directory.
	 * @return The base output directory of the site being mummified.
	 * @see GuiseProject#getDirectory()
	 */
	public default Path getSiteTargetDirectory() {
		return getProject().getDirectory().resolve(getConfiguration().getPath(GuiseMummy.PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY));
	}

	/**
	 * Returns the output directory of the site description.
	 * @implSpec The default implementation retrieves the value for key {@value GuiseMummy#PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY} from the
	 *           configuration and resolves it against the project directory.
	 * @return The base output directory of the generated site description.
	 * @see GuiseProject#getDirectory()
	 */
	public default Path getSiteDescriptionTargetDirectory() {
		return getProject().getDirectory().resolve(getConfiguration().getPath(GuiseMummy.PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY));
	}

	//TODO public UrfObject getResourceDescription(path)

	/**
	 * Returns the configuration options for mummification. This configuration may contain local configuration overrides, but ultimately falls back to the project
	 * configuration.
	 * @return The configuration options for mummification.
	 * @see GuiseProject#getConfiguration()
	 */
	public Configuration getConfiguration();

	/**
	 * Determines whether a path should be ignored during discovery.
	 * @param sourcePath The source path to check.
	 * @return <code>true</code> if the source path should be ignored and excluded from processing.
	 */
	public boolean isIgnore(@Nonnull final Path sourcePath);

	/** @return The default mummifier for source files. */
	public SourcePathMummifier getDefaultSourceFileMummifier();

	/** @return The default mummifier for source directories. */
	public SourcePathMummifier getDefaultSourceDirectoryMummifier();

	/**
	 * Retrieves the default mummifier for a given source path, based upon whether it is a file or a directory.
	 * @apiNote This method should only be called in special cases. Normally it is desired to look up the <code>registered</code> mummifier for a source path
	 *          using {@link #findRegisteredMummifierForSourcePath(Path)}.
	 * @implSpec The default implementation delegates to {@link #getDefaultSourceFileMummifier()} or {@link #getDefaultSourceDirectoryMummifier()} based upon the
	 *           result of {@link Files#isDirectory(Path, LinkOption...)}.
	 * @param sourcePath The path of the source to be mummified.
	 * @return The default mummifier for the source path.
	 */
	public default SourcePathMummifier getDefaultSourcePathMummifier(@Nonnull final Path sourcePath) {
		return isDirectory(sourcePath) ? getDefaultSourceDirectoryMummifier() : getDefaultSourceFileMummifier();
	}

	/**
	 * Retrieves a registered mummifier for a particular source file.
	 * @param sourceFile The path of the source file to be mummified.
	 * @return The mummifier, if any, registered for the given source file.
	 */
	public Optional<SourcePathMummifier> findRegisteredMummifierForSourceFile(@Nonnull final Path sourceFile);

	/**
	 * Retrieves a registered mummifier for a particular source directory.
	 * @param sourceDirectory The path of the source directory to be mummified.
	 * @return The mummifier, if any, registered for the given source directory.
	 */
	public Optional<SourcePathMummifier> findRegisteredMummifierForSourceDirectory(@Nonnull final Path sourceDirectory);

	/**
	 * Retrieves a registered mummifier for a particular source path, which may represent a file or a directory.
	 * @implSpec The default implementation delegates to {@link #findRegisteredMummifierForSourceFile(Path)} or
	 *           {@link #findRegisteredMummifierForSourceDirectory(Path)} depending on whether the given source path is a file or a directory.
	 * @param sourcePath The path of the source to be mummified.
	 * @return The mummifier, if any, registered for the given source path.
	 */
	public default Optional<SourcePathMummifier> findRegisteredMummifierForSourcePath(@Nonnull final Path sourcePath) {
		return isDirectory(sourcePath) ? findRegisteredMummifierForSourceDirectory(sourcePath) : findRegisteredMummifierForSourceFile(sourcePath);
	}

	/**
	 * Retrieves a mummifier for a particular source path, which may represent a file or a directory. If no mummifier is registered, a default mummifier is
	 * returned.
	 * @implSpec The default implementation delegates to {@link #findRegisteredMummifierForSourcePath(Path)} and if none is present delegates to
	 *           {@link #getDefaultSourcePathMummifier(Path)}.
	 * @param sourcePath The path of the source to be mummified.
	 * @return The mummifier for the given source path.
	 */
	public default SourcePathMummifier getMummifierForSourcePath(@Nonnull final Path sourcePath) {
		return findRegisteredMummifierForSourcePath(sourcePath).orElseGet(() -> getDefaultSourcePathMummifier(sourcePath));
	}

	//hierarchy

	/**
	 * Returns the parent artifact of some artifact.
	 * @param artifact The artifact for which a parent is to be determined.
	 * @return The parent artifact, if any, of the given artifact.
	 */
	public Optional<Artifact> findParentArtifact(@Nonnull final Artifact artifact);

	/**
	 * Provides the child artifacts of some artifact.
	 * @param artifact The artifact for which children should be returned.
	 * @return The child artifacts, if any, of the given artifact.
	 */
	public default Stream<Artifact> childArtifacts(@Nonnull final Artifact artifact) {
		return requireNonNull(artifact) instanceof CollectionArtifact ? ((CollectionArtifact)artifact).getChildArtifacts().stream() : Stream.empty();
	}

	/**
	 * Provides the artifacts that are siblings to the given artifact. An artifact will not have siblings if it has no parent. If any artifacts are returned, the
	 * returned artifacts <em>will</em> include the given artifact. This means that a single child artifact will return itself as the single sibling artifact.
	 * @param artifact The artifact for which siblings should be returned.
	 * @return The sibling artifacts, if any, of the given artifact, including the given artifact.
	 */
	public default Stream<Artifact> siblingArtifacts(@Nonnull final Artifact artifact) {
		return findParentArtifact(artifact).map(this::childArtifacts).orElse(Stream.empty());
	}

	//source references

	/**
	 * Checks to ensure that a given path lies in the source directory.
	 * @param sourcePath The potential source path; must be absolute.
	 * @return The given source path.
	 * @throws IllegalArgumentException if the given source path is not absolute.
	 * @throws IllegalArgumentException if the given source path is not in the site source tree.
	 * @see #getSiteSourceDirectory()
	 */
	public default Path checkArgumentSourcePath(@Nonnull Path sourcePath) {
		checkArgumentSubPath(getSiteSourceDirectory(), checkArgumentAbsolute(sourcePath));
		return sourcePath;
	}

	/**
	 * Searches for a non-directory file in the given source directory that matches the given base filename and which can be mummified into a page. No files are
	 * ignored in the search.
	 * @apiNote Ancestors are <em>not</em> searched. To search ancestors, use the {@link #findPageSourceFile(Path, String, boolean)} variation.
	 * @apiNote This method would be useful for finding an <code>index.*</code> page source file ins a single directory, for example.
	 * @implSpec The default version delegates to {@link #findPageSourceFile(Path, String, boolean)}.
	 * @param sourceDirectory The directory in the source tree in which to search.
	 * @param baseFilename The base filename (i.e. with no extension) for which to search.
	 * @return The first encountered matching file, if any, along with its mummifier.
	 * @throws IllegalArgumentException if the given source directory is not absolute.
	 * @throws IllegalArgumentException if the given source directory is not in the site source tree.
	 * @throws IOException If there is an I/O error searching for a matching file.
	 */
	public default Optional<Map.Entry<Path, PageMummifier>> findPageSourceFile(@Nonnull Path sourceDirectory, @Nonnull final String baseFilename)
			throws IOException {
		return findPageSourceFile(sourceDirectory, baseFilename, false);
	}

	/**
	 * Searches for a non-directory file in the given source directory that matches the given base filename and which can be mummified into a page. No files are
	 * ignored in the search. Ancestors are never searched above the source root directory.
	 * @apiNote This method would be useful for finding a <code>.template.*</code> page source file up the hierarchy, for example.
	 * @param sourceDirectory The directory in the source tree in which to search.
	 * @param baseFilename The base filename (i.e. with no extension) for which to search.
	 * @param searchAncestors Whether parent directories should be recursively searched if the file cannot be found in the given source directory.
	 * @return The first encountered matching file, if any, along with its mummifier.
	 * @throws IllegalArgumentException if the given source directory is not absolute.
	 * @throws IllegalArgumentException if the given source directory is not in the site source tree.
	 * @throws IOException If there is an I/O error searching for a matching file.
	 */
	public default Optional<Map.Entry<Path, PageMummifier>> findPageSourceFile(@Nonnull Path sourceDirectory, @Nonnull final String baseFilename,
			boolean searchAncestors) throws IOException {
		checkArgumentSourcePath(sourceDirectory);
		final Path siteSourceDirectory = getSiteSourceDirectory();
		requireNonNull(baseFilename);
		try (final Stream<Path> sourceFiles = list(sourceDirectory)) {
			return sourceFiles.filter(not(Files::isDirectory)) //ignore directories
					.filter(byBaseFilename(baseFilename)) //filter by the base filename
					.flatMap(sourceFile -> findRegisteredMummifierForSourceFile(sourceFile).filter(PageMummifier.class::isInstance).map(PageMummifier.class::cast)
							.map(pageMummifier -> (Map.Entry<Path, PageMummifier>)new AbstractMap.SimpleImmutableEntry<>(sourceFile, pageMummifier)).stream()) //TODO use entry factory
					.findAny().or(throwingSupplier(() -> {
						if(searchAncestors && !sourceDirectory.equals(siteSourceDirectory)) {
							final Path parentDirectory = sourceDirectory.getParent(); //TODO if create utility Optional-returning parent utility method
							if(parentDirectory != null) {
								return findPageSourceFile(parentDirectory, baseFilename, searchAncestors);
							}
						}
						return Optional.empty();
					}));
		}
	}

	/**
	 * Retrieves an artifact referred to by a reference source path in the file system.
	 * <p>
	 * The source path of the returned artifact may not actually be equal to given source path. For example, a referent source path of
	 * <code>/foo/bar/index.xhtml</code> might return the artifact for the file that exists at the source path <code>/foo/bar/</code>, because any link to
	 * <code>/foo/bar/index.xhtml</code> is really referring to <code>/foo/bar/</code>; the <code>/foo/bar/index.xhtml</code> file is merely an implementation
	 * detail for storing the content of <code>/foo/bar/</code>.
	 * </p>
	 * @param referenceSourcePath The absolute source path being used as a reference to some artifact.
	 * @return The artifact referred to by a reference source path.
	 * @throws IllegalArgumentException if the given reference path is not absolute.
	 */
	public Optional<Artifact> findArtifactBySourceReference(@Nonnull final Path referenceSourcePath);

	/**
	 * Retrieves an artifact referred to by a URI path source reference relative to some context artifact.
	 * <p>
	 * The source path of the returned artifact may not actually be equal to given source path. For example, a referent source path of
	 * <code>/foo/bar/index.xhtml</code> might return the artifact for the file that exists at the source path <code>/foo/bar/</code>, because any link to
	 * <code>/foo/bar/index.xhtml</code> is really referring to <code>/foo/bar/</code>; the <code>/foo/bar/index.xhtml</code> file is merely an implementation
	 * detail for storing the content of <code>/foo/bar/</code>.
	 * </p>
	 * @param contextArtifact The artifact the relative reference should be resolved against when finding the referent artifact.
	 * @param sourceRelativeReference The relative URI path being used as a reference to some artifact.
	 * @return The artifact referred to by a relative path source reference.
	 * @throws IllegalArgumentException if the given reference path is absolute.
	 */
	public default Optional<Artifact> findArtifactBySourceRelativeReference(@Nonnull final Artifact contextArtifact,
			@Nonnull final URIPath sourceRelativeReference) {
		return findArtifactBySourceRelativeReference(contextArtifact.getSourcePath(), sourceRelativeReference);
	}

	/**
	 * Retrieves an artifact referred to by a URI path source reference relative to some context source path.
	 * <p>
	 * The source path of the returned artifact may not actually be equal to given source path. For example, a referent source path of
	 * <code>/foo/bar/index.xhtml</code> might return the artifact for the file that exists at the source path <code>/foo/bar/</code>, because any link to
	 * <code>/foo/bar/index.xhtml</code> is really referring to <code>/foo/bar/</code>; the <code>/foo/bar/index.xhtml</code> file is merely an implementation
	 * detail for storing the content of <code>/foo/bar/</code>.
	 * </p>
	 * <p>
	 * This method follows <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a> and resolves a relative reference of the empty string by using the source
	 * relative reference e.g. <code>/foo/bar</code>, not as the parent collection <code>/foo/</code>.
	 * </p>
	 * @param contextSourcePath The source path the relative reference should be resolved against when finding the referent artifact.
	 * @param sourceRelativeReference The relative URI path being used as a reference to some artifact.
	 * @return The artifact referred to by a relative path source reference.
	 * @throws IllegalArgumentException if the context source path is not absolute and/or is not within the site source tree.
	 * @throws IllegalArgumentException if the given reference path is absolute.
	 */
	public default Optional<Artifact> findArtifactBySourceRelativeReference(@Nonnull final Path contextSourcePath,
			@Nonnull final URIPath sourceRelativeReference) {
		checkArgumentSourcePath(contextSourcePath);
		sourceRelativeReference.checkRelative();
		//Resolve the relative path to the URI form of the context artifact path, and then convert that back to a file system path.
		//Follow RFC 3986 by interpreting resolution to "" as returning the context source path itself, not the collection/directory path.
		final URI sourceRelativeReferenceURI = sourceRelativeReference.toURI();
		final Path referenceSourcePath = sourceRelativeReferenceURI.equals(EMPTY_PATH_URI) ? contextSourcePath
				: Paths.get(contextSourcePath.toUri().resolve(sourceRelativeReference.toURI()));
		return findArtifactBySourceReference(referenceSourcePath);
	}

	/**
	 * Relativizes a reference from one artifact to another in the source file system tree relative to a context artifact. The returned reference will be a URI
	 * path relative to the given context artifact.
	 * @param contextArtifact The artifact the reference path should be relativized against.
	 * @param referentArtifact The artifact being referred to.
	 * @return The reference path to the referent artifact relative to the context artifact as a URI path.
	 * @see Artifact#getSourcePath()
	 */
	public default URIPath relativizeSourceReference(@Nonnull final Artifact contextArtifact, @Nonnull final Artifact referentArtifact) {
		return relativizeSourceReference(contextArtifact, referentArtifact.getSourcePath());
	}

	/**
	 * Relativizes an absolute reference to a source file in the file system relative to a context artifact. The reference must be to a path within the site. The
	 * returned reference will be a URI path relative to the given artifact.
	 * @param contextArtifact The artifact the reference path should be relativized against.
	 * @param referenceSourcePath The absolute reference source path to relativize.
	 * @return The reference path relative to the context artifact as a URI path.
	 * @see Artifact#getSourcePath()
	 */
	public default URIPath relativizeSourceReference(@Nonnull final Artifact contextArtifact, @Nonnull final Path referenceSourcePath) {
		return relativizeSourceReference(contextArtifact.getSourcePath(), referenceSourcePath);
	}

	/**
	 * Relativizes a reference to a source file in the file system against some base path. Both paths must be within the site. The returned reference will be a
	 * URI path (e.g. appropriate for web references) relative to the base path.
	 * @param baseSourcePath The absolute path against which the reference path with be relativized.
	 * @param referenceSourcePath The absolute reference source path to relativize.
	 * @return The reference path relative to the base path as a URI path.
	 * @throws IllegalArgumentException if the source path and or the base path is not absolute and/or is not within the site.
	 * @see #getSiteSourceDirectory()
	 */
	public default URIPath relativizeSourceReference(@Nonnull final Path baseSourcePath, @Nonnull final Path referenceSourcePath) {
		checkArgumentSourcePath(baseSourcePath);
		checkArgumentSourcePath(referenceSourcePath);
		return URIPath.relativize(baseSourcePath.toUri(), referenceSourcePath.toUri());
	}

	//target references

	/**
	 * Relativizes a reference from one artifact to another in the target file system tree relative to a context artifact. The returned reference will be a URI
	 * path relative to the given context artifact.
	 * @param contextArtifact The artifact the reference path should be relativized against.
	 * @param referentArtifact The artifact being referred to.
	 * @return The reference path to the referent artifact relative to the context artifact as a URI path.
	 * @see Artifact#getTargetPath()
	 */
	public default URIPath relativizeTargetReference(@Nonnull final Artifact contextArtifact, @Nonnull final Artifact referentArtifact) {
		return relativizeTargetReference(contextArtifact, referentArtifact.getTargetPath());
	}

	/**
	 * Relativizes an absolute reference to a target file in the file system relative to a context artifact. The reference must be to a path within the site. The
	 * returned reference will be a URI path relative to the given artifact.
	 * @param contextArtifact The artifact the reference path should be relativized against.
	 * @param referenceTargetPath The absolute reference target path to relativize.
	 * @return The reference path relative to the context artifact as a URI path.
	 * @see Artifact#getTargetPath()
	 */
	public default URIPath relativizeTargetReference(@Nonnull final Artifact contextArtifact, @Nonnull final Path referenceTargetPath) {
		return relativizeTargetReference(contextArtifact.getTargetPath(), referenceTargetPath);
	}

	/**
	 * Relativizes a reference to a target file in the file system against some base path. Both paths must be within the site. The returned reference will be a
	 * URI path (e.g. appropriate for web references) relative to the base path.
	 * @param baseTargetPath The absolute path against which the reference path with be relativized.
	 * @param referenceTargetPath The absolute reference target path to relativize.
	 * @return The reference path relative to the base path as a URI path.
	 * @throws IllegalArgumentException if the target path and or the base path is not absolute and/or is not within the site.
	 * @see #getSiteTargetDirectory()
	 */
	public default URIPath relativizeTargetReference(@Nonnull final Path baseTargetPath, @Nonnull final Path referenceTargetPath) {
		checkArgumentSubPath(getSiteTargetDirectory(), checkArgumentAbsolute(baseTargetPath));
		checkArgumentSubPath(getSiteTargetDirectory(), checkArgumentAbsolute(referenceTargetPath));
		return URIPath.relativize(baseTargetPath.toUri(), referenceTargetPath.toUri());
	}

	//resource references (in URI path form)

	/**
	 * Relativizes a reference to a target file in the file system against some base path. Both paths must be either in the site source directory or in the site
	 * destination directory. The returned reference will be a resource URI path (e.g. appropriate for web references) relative to the base path.
	 * @apiNote If the reference target path does not yet exist, this method may not produce a URI path in the correct collection form, as it is impossible to
	 *          know if the target represents a directory. Use {@link #relativizeResourceReference(Path, Path, boolean)} if it is possible to know whether the
	 *          target path is meant to be a collection.
	 * @implSpec This implementation delegates to {@link #relativizeResourceReference(Path, Path, boolean)} without forcing a collection.
	 * @param baseTargetPath The absolute path against which the reference path with be relativized.
	 * @param referenceTargetPath The absolute reference path to relativize.
	 * @return The reference path relative to the base path as a URI path.
	 * @throws IllegalArgumentException if the target path and or the base path is not absolute and/or is not within the same source/target tree.
	 * @see #getSiteSourceDirectory()
	 * @see #getSiteTargetDirectory()
	 */
	public default URIPath relativizeResourceReference(@Nonnull final Path baseTargetPath, @Nonnull final Path referenceTargetPath) {
		return relativizeResourceReference(baseTargetPath, referenceTargetPath, false);
	}

	/**
	 * Relativizes a reference to a target file in the file system against some base path. Both paths must be either in the site source directory or in the site
	 * destination directory. The returned reference will be a resource URI path (e.g. appropriate for web references) relative to the base path.
	 * @param baseTargetPath The absolute path against which the reference path with be relativized.
	 * @param referenceTargetPath The absolute reference path to relativize.
	 * @param forceCollection <code>true</code> if the returned path should be in collection form, ending with a slash, regardless of whether the reference target
	 *          path is a collection or even exists.
	 * @return The reference path relative to the base path as a URI path.
	 * @throws IllegalArgumentException if the target path and or the base path is not absolute and/or is not within the same source/target tree.
	 * @see #getSiteSourceDirectory()
	 * @see #getSiteTargetDirectory()
	 */
	public default URIPath relativizeResourceReference(@Nonnull final Path baseTargetPath, @Nonnull final Path referenceTargetPath,
			final boolean forceCollection) {
		final Path root = isSubPath(getSiteSourceDirectory(), baseTargetPath) ? getSiteSourceDirectory() : getSiteTargetDirectory();
		final URI baseTargetUri = checkArgumentSubPath(root, checkArgumentAbsolute(baseTargetPath)).toUri();
		final URI referenceTargetUri = checkArgumentSubPath(root, checkArgumentAbsolute(referenceTargetPath)).toUri();
		return URIPath.relativize(baseTargetUri, forceCollection ? toCollectionURI(referenceTargetUri) : referenceTargetUri);
	}

	//factory methods

	/**
	 * Creates a new instance of a {@link DocumentBuilder} appropriate for working with Guise Mummy pages.
	 * @implSpec The returned document builder will be namespace aware.
	 * @return A new instance of a page document builder.
	 * @throws ConfigurationException if there is a problem creating a document builder.
	 */
	public DocumentBuilder newPageDocumentBuilder();

	//## deploy

	/**
	 * Returns the DNS configured for deployment. Any configured DNS will not be available until preparation for deployment, but is guaranteed to be available, if
	 * configured, before targets are prepared for deployment.
	 * @return The DNS configured for deployment, if any; will not be present before deployment preparation.
	 */
	public Optional<Dns> getDeployDns();

	/**
	 * Returns the targets configured for deployment. The targets will not be available until preparation for deployment.
	 * @return The targets configured for deployment, if any; will not be present before deployment preparation.
	 */
	public Optional<List<DeployTarget>> getDeployTargets();

}
