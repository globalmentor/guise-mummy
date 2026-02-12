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

package dev.guise.mummy;

import static com.globalmentor.io.Paths.*;
import static java.nio.file.Files.*;
import static java.util.Objects.*;
import static java.util.function.Predicate.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;

import org.jspecify.annotations.*;

import io.confound.config.*;
import dev.guise.mummy.deploy.*;
import dev.guise.mummy.mummify.SourcePathMummifier;
import dev.guise.mummy.mummify.page.PageMummifier;

/// Provides information about context of static site generation.
/// @author Garret Wilson
public interface MummyContext {

	/// Returns a free-form string identifying the program generating the content.
	/// @return The generator identification string.
	public String getMummifierIdentification();

	/// Returns the Guise project governing mummification.
	/// @return The Guise project governing mummification.
	public GuiseProject getProject();

	/// Indicates whether full mummification is enabled, as opposed to incremental mummification.
	/// @return `true` if full mummification is enabled; `false` if mummification is incremental.
	public boolean isFull();

	/// Indicates whether mummification should be incremental.
	/// @implSpec The default implementation delegates to [#isFull()] and returns the opposite value.
	/// @return `true` if incremental mummification is enabled.
	public default boolean isIncremental() {
		return !isFull();
	}

	/// Returns some URI indicating the root of the current context, that is, the site source directory. All resource context paths are interpreted relative to
	/// this root.
	/// @apiNote This method will typically but not necessarily return the URI form of the site source directory.
	/// @return The URI that represents the root of the current context.
	public default URI getRoot() { //TODO move to some abstract base class; if it is configurable, it shouldn't have a default implementation
		return getSiteSourceDirectory().toUri();
	}

	/// Returns the base directory of the entire site source, representing the root of the context.
	/// @implSpec The default implementation retrieves the value for key `siteSourceDirectory` from the configuration and
	///           resolves it against the project directory.
	/// @apiNote This is analogous to Maven's `${project.basedir}/src/site` directory.
	/// @return The base directory of the site being mummified.
	/// @see GuiseProject#getDirectory()
	public default Path getSiteSourceDirectory() {
		return getProject().getDirectory().resolve(getConfiguration().getPath(GuiseMummy.PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY));
	}

	/// Returns the output directory of the entire site, representing the root of the context.
	/// @implSpec The default implementation retrieves the value for key `siteTargetDirectory` from the configuration and
	///           resolves it against the project directory.
	/// @apiNote This is analogous to Maven's `${project.build.directory}` directory.
	/// @return The base output directory of the site being mummified.
	/// @see GuiseProject#getDirectory()
	public default Path getSiteTargetDirectory() {
		return getProject().getDirectory().resolve(getConfiguration().getPath(GuiseMummy.PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY));
	}

	/// Returns the output directory of the site description.
	/// @implSpec The default implementation retrieves the value for key `siteDescriptionTargetDirectory` from the
	///           configuration and resolves it against the project directory.
	/// @return The base output directory of the generated site description.
	/// @see GuiseProject#getDirectory()
	public default Path getSiteDescriptionTargetDirectory() {
		return getProject().getDirectory().resolve(getConfiguration().getPath(GuiseMummy.PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY));
	}

	//TODO public UrfObject getResourceDescription(path)

	/// Returns the configuration options for mummification. This configuration may contain local configuration overrides, but ultimately falls back to the project
	/// configuration.
	/// @return The configuration options for mummification.
	/// @see GuiseProject#getConfiguration()
	public Configuration getConfiguration();

	/// Determines whether a path should be ignored during discovery.
	/// @param sourcePath The source path to check.
	/// @return `true` if the source path should be ignored and excluded from processing.
	public boolean isIgnore(@NonNull final Path sourcePath);

	/// Returns the default mummifier for source files.
	/// @return The default mummifier for source files.
	public SourcePathMummifier getDefaultSourceFileMummifier();

	/// Returns the default mummifier for source directories.
	/// @return The default mummifier for source directories.
	public SourcePathMummifier getDefaultSourceDirectoryMummifier();

	/// Retrieves the default mummifier for a given source path, based upon whether it is a file or a directory.
	/// @apiNote This method should only be called in special cases. Normally it is desired to look up the *registered* mummifier for a source path
	///          using [#findRegisteredMummifierForSourcePath(Path)].
	/// @implSpec The default implementation delegates to [#getDefaultSourceFileMummifier()] or [#getDefaultSourceDirectoryMummifier()] based upon the
	///           result of [Files#isDirectory(Path, LinkOption...)].
	/// @param sourcePath The path of the source to be mummified.
	/// @return The default mummifier for the source path.
	public default SourcePathMummifier getDefaultSourcePathMummifier(@NonNull final Path sourcePath) {
		return isDirectory(sourcePath) ? getDefaultSourceDirectoryMummifier() : getDefaultSourceFileMummifier();
	}

	/// Retrieves a registered mummifier for a particular source file.
	/// @param sourceFile The path of the source file to be mummified.
	/// @return The mummifier, if any, registered for the given source file.
	public Optional<SourcePathMummifier> findRegisteredMummifierForSourceFile(@NonNull final Path sourceFile);

	/// Retrieves a registered mummifier for a particular source directory.
	/// @param sourceDirectory The path of the source directory to be mummified.
	/// @return The mummifier, if any, registered for the given source directory.
	public Optional<SourcePathMummifier> findRegisteredMummifierForSourceDirectory(@NonNull final Path sourceDirectory);

	/// Retrieves a registered mummifier for a particular source path, which may represent a file or a directory.
	/// @implSpec The default implementation delegates to [#findRegisteredMummifierForSourceFile(Path)] or
	///           [#findRegisteredMummifierForSourceDirectory(Path)] depending on whether the given source path is a file or a directory.
	/// @param sourcePath The path of the source to be mummified.
	/// @return The mummifier, if any, registered for the given source path.
	public default Optional<SourcePathMummifier> findRegisteredMummifierForSourcePath(@NonNull final Path sourcePath) {
		return isDirectory(sourcePath) ? findRegisteredMummifierForSourceDirectory(sourcePath) : findRegisteredMummifierForSourceFile(sourcePath);
	}

	/// Retrieves a mummifier for a particular source path, which may represent a file or a directory. If no mummifier is registered, a default mummifier is
	/// returned.
	/// @implSpec The default implementation delegates to [#findRegisteredMummifierForSourcePath(Path)] and if none is present delegates to
	///           [#getDefaultSourcePathMummifier(Path)].
	/// @param sourcePath The path of the source to be mummified.
	/// @return The mummifier for the given source path.
	public default SourcePathMummifier getMummifierForSourcePath(@NonNull final Path sourcePath) {
		return findRegisteredMummifierForSourcePath(sourcePath).orElseGet(() -> getDefaultSourcePathMummifier(sourcePath));
	}

	//plan

	/// Returns the site plan. The plan will not be available until the [GuiseMummy.LifeCyclePhase] has completed the [GuiseMummy.LifeCyclePhase#PLAN]
	/// phase.
	/// @return The plan for the site
	/// @throws IllegalStateException if the site has not yet been planned.
	public MummyPlan getPlan();

	//source paths

	/// Checks to ensure that a given path lies in the source directory.
	/// @param sourcePath The potential source path; must be absolute.
	/// @return The given source path.
	/// @throws IllegalArgumentException if the given source path is not absolute.
	/// @throws IllegalArgumentException if the given source path is not in the site source tree.
	/// @see #getSiteSourceDirectory()
	public default Path checkArgumentSourcePath(@NonNull Path sourcePath) {
		checkArgumentSubPath(getSiteSourceDirectory(), checkArgumentAbsolute(sourcePath));
		return sourcePath;
	}

	/// Searches for a non-directory file in the given source directory that matches the given base filename and which can be mummified into a page. No files are
	/// ignored in the search.
	/// @apiNote Ancestors are *not* searched. To search ancestors, use the [#findPageSourceFile(Path, String, boolean)] variation.
	/// @apiNote This method would be useful for finding an `index.*` page source file ins a single directory, for example.
	/// @implSpec The default version delegates to [#findPageSourceFile(Path, String, boolean)].
	/// @param sourceDirectory The directory in the source tree in which to search.
	/// @param baseFilename The base filename (i.e. with no extension) for which to search.
	/// @return The first encountered matching file, if any, along with its mummifier.
	/// @throws IllegalArgumentException if the given source directory is not absolute.
	/// @throws IllegalArgumentException if the given source directory is not in the site source tree.
	/// @throws IOException If there is an I/O error searching for a matching file.
	public default Optional<Map.Entry<Path, PageMummifier>> findPageSourceFile(@NonNull Path sourceDirectory, @NonNull final String baseFilename)
			throws IOException {
		return findPageSourceFile(sourceDirectory, baseFilename, false);
	}

	/// Searches for a non-directory file in the given source directory that matches the given base filename and which can be mummified into a page. No files are
	/// ignored in the search. Ancestors are never searched above the source root directory.
	/// @apiNote This method would be useful for finding a `.template.*` page source file up the hierarchy, for example.
	/// @param sourceDirectory The directory in the source tree in which to search.
	/// @param baseFilename The base filename (i.e. with no extension) for which to search.
	/// @param searchAncestors Whether parent directories should be recursively searched if the file cannot be found in the given source directory.
	/// @return The first encountered matching file, if any, along with its mummifier.
	/// @throws IllegalArgumentException if the given source directory is not absolute.
	/// @throws IllegalArgumentException if the given source directory is not in the site source tree.
	/// @throws IOException If there is an I/O error searching for a matching file.
	public default Optional<Map.Entry<Path, PageMummifier>> findPageSourceFile(@NonNull Path sourceDirectory, @NonNull final String baseFilename,
			boolean searchAncestors) throws IOException {
		checkArgumentSourcePath(sourceDirectory);
		final Path siteSourceDirectory = getSiteSourceDirectory();
		requireNonNull(baseFilename);
		try (final Stream<Path> sourceFiles = list(sourceDirectory)) {
			return sourceFiles.filter(not(Files::isDirectory)) //ignore directories
					.filter(byBaseFilename(baseFilename)) //filter by the base filename
					.flatMap(sourceFile -> findRegisteredMummifierForSourceFile(sourceFile).filter(PageMummifier.class::isInstance).map(PageMummifier.class::cast)
							.map(pageMummifier -> Map.entry(sourceFile, pageMummifier)).stream()) //TODO use entry factory
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

	//factory methods

	/// Creates a new instance of a [DocumentBuilder] appropriate for working with Guise Mummy pages.
	/// @implSpec The returned document builder will be namespace aware.
	/// @return A new instance of a page document builder.
	/// @throws ConfigurationException if there is a problem creating a document builder.
	public DocumentBuilder newPageDocumentBuilder();

	//## deploy

	/// Returns the DNS configured for deployment. Any configured DNS will not be available until preparation for deployment, but is guaranteed to be available, if
	/// configured, before targets are prepared for deployment.
	/// @return The DNS configured for deployment, if any; will not be present before deployment preparation.
	public Optional<Dns> getDeployDns();

	/// Returns the targets configured for deployment. The targets will not be available until preparation for deployment.
	/// @return The targets configured for deployment, if any; will not be present before deployment preparation.
	public Optional<List<DeployTarget>> getDeployTargets();

}
