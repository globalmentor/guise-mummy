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
import static java.util.Collections.*;
import static java.util.Objects.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.net.URIPath;

/**
 * Provides information about context of static site generation.
 * @author Garret Wilson
 */
public interface MummyContext {

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
	 * @apiNote This is analogous to Maven's <code>${project.basedir}/src/site</code> directory.
	 * @return The base directory of the site being mummified.
	 */
	public Path getSiteSourceDirectory();

	/**
	 * Returns the output directory of the entire site source, representing the root of the context.
	 * @apiNote This is analogous to Maven's <code>${project.build.directory}</code> directory.
	 * @return The base output directory of the site being mummified.
	 */
	public Path getSiteTargetDirectory();

	//TODO public UrfObject getResourceDescription(path)

	/**
	 * Determines whether a path should be ignored during discovery.
	 * @param sourcePath The path to check.
	 * @return <code>true</code> if the source path should be ignored and excluded from processing.
	 */
	public boolean isIgnore(@Nonnull final Path sourcePath);

	/**
	 * Determines the target path in the site target directory based upon the source path in the site source directory.
	 * @param sourcePath The path in the site source directory.
	 * @return The corresponding path in the site target directory.
	 * @throws IllegalArgumentException if the given source path is not in the site source tree.
	 * @see #getSiteSourceDirectory()
	 * @see #getSiteTargetDirectory()
	 */
	public default Path getTargetPath(@Nonnull final Path sourcePath) {
		return changeBase(sourcePath, getSiteSourceDirectory(), getSiteTargetDirectory());
	}

	/**
	 * Retrieves a mummifier for a particular source path, which may represent a file or a directory.
	 * @param sourcePath The path of the source to be mummified.
	 * @return The mummifier for the given source.
	 */
	public Optional<Mummifier> getMummifier(@Nonnull final Path sourcePath);

	/**
	 * Retrieves the artifact that should act as a referent in relation to this artifact.
	 * <p>
	 * For example, a <code>/foo/bar/index.html</code> artifact is merely the implementation for storing the content of the <code>/foo/bar/</code>, and other
	 * resources will refer to <code>/foo/bar/</code>, not <code>/foo/bar/index.html</code>. Therefore <code>/foo/bar/</code> will be the referent artifact of
	 * <code>/foo/bar/index.html</code>.
	 * </p>
	 * @param artifact The artifact for which a referent resource should be retrieved.
	 * @return The resource for acting as the target of links, which may be the given resource itself.
	 */
	//TODO delete public Artifact getReferentArtifact(@Nonnull final Artifact artifact);

	/**
	 * Determines the parent artifact of some artifact.
	 * @param artifact The artifact for which a parent is to be determined.
	 * @return The parent artifact, if any, of the given artifact.
	 */
	public Optional<Artifact> getParentArtifact(@Nonnull final Artifact artifact);

	/**
	 * Determines the child artifacts of some artifact.
	 * @param artifact The artifact for which children should be returned.
	 * @return The child artifacts, if any, of the given artifact.
	 */
	public default Collection<Artifact> getChildArtifacts(@Nonnull final Artifact artifact) {
		return requireNonNull(artifact) instanceof CollectionArtifact ? ((CollectionArtifact)artifact).getChildArtifacts() : emptySet();
	}

	/**
	 * Determines the artifacts that are siblings to the given artifact. An artifact will not have siblings if it has no parent. If any artifacts are returned,
	 * the returned artifacts <em>will</em> include the given artifact. This means that a single child artifact will return itself as the single sibling artifact.
	 * @param artifact The artifact for which siblings should be returned.
	 * @return The sibling artifacts, if any, of the given artifact, including the given artifact.
	 */
	public default Collection<Artifact> getSiblingArtifacts(@Nonnull final Artifact artifact) {
		return getParentArtifact(artifact).map(this::getChildArtifacts).orElse(emptySet());
	}

	/**
	 * Determines the artifacts suitable for direct subsequent navigation from this artifact, <em>excluding</em> the parent artifact. The sibling artifacts are
	 * returned, they will include the given resource.
	 * @apiNote This method is equivalent to calling {@link #getChildArtifacts(Artifact)} if the artifact is a {@link CollectionArtifact}, otherwise calling
	 *          {@link #getSiblingArtifacts(Artifact)}.
	 * @param artifact The artifact for which navigation artifacts should be returned.
	 * @return The artifacts for subsequent navigation from this artifact.
	 */
	public default Collection<Artifact> getNavigationArtifacts(@Nonnull final Artifact artifact) {
		return artifact instanceof CollectionArtifact ? getChildArtifacts(artifact) : getSiblingArtifacts(artifact);
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
	public Optional<Artifact> getArtifactBySourceReference(@Nonnull final Path referenceSourcePath);

	//TODO for looking up destinations from existing links	
	//	public Optional<Artifact> getArtifactBySourceReference(@Nonnull final Artifact contextArtifact, @Nonnull final URIPath referenceRelativeSourcePath) {
	//		Conditions.checkArgument(referenceRelativeSourcePath.isRelative(), "Absolute references not yet supported.");
	//	}

	/**
	 * Relativizes a reference from one artifact to another in the source file system relative to a context artifact. The returned reference will be a URI path
	 * relative to the given context artifact.
	 * @param contextArtifact The artifact the reference path should be relativized against.
	 * @param referentArtifact The artifact being referred to.
	 * @return The reference path to the referent artifact relative to the context artifact as a URI path.
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
	 */
	public default URIPath relativizeSourceReference(@Nonnull final Artifact contextArtifact, @Nonnull final Path referenceSourcePath) {
		return relativizeSourceReference(contextArtifact.getSourcePath(), referenceSourcePath);
	}

	/**
	 * Relativizes a reference to a source file in the file system against some base path. Both paths must be within the site. The returned reference will be a
	 * URI path (e.g. appropriate for web references) relative to the base path.
	 * @implNote This does not yet properly relativize paths that require backtracking, such as siblings; see
	 *           <a href="https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6226081">JDK-6226081</a>.
	 * @param baseSourcePath The absolute path against which the reference path with be relativized.
	 * @param referenceSourcePath The absolute reference source path to relativize.
	 * @return The reference path relative to the base path as a URI path.
	 * @throws IllegalArgumentException if the source path and or the base path is not absolute and/or is not within the site.
	 * @see #getSiteSourceDirectory()
	 */
	public default URIPath relativizeSourceReference(@Nonnull final Path baseSourcePath, @Nonnull final Path referenceSourcePath) {
		checkArgumentSubPath(getSiteSourceDirectory(), checkArgumentAbsolute(baseSourcePath));
		checkArgumentSubPath(getSiteSourceDirectory(), checkArgumentAbsolute(referenceSourcePath));
		//relativizing file system paths supports backtracking (`..`, e.g. siblings or parents); see e.g. https://stackoverflow.com/a/705963/421049
		//TODO fix; this creates a relative path, but toURI() converts it back to an absolute URI using the home path: this final URIPath relativeUriPath = new URIPath(baseSourcePath.relativize(referenceSourcePath).toUri());
		//TODO fix assert relativeUriPath.isRelative();
		System.out.println("URI relativization: " + URIPath.relativize(baseSourcePath.toUri(), referenceSourcePath.toUri()));
		return URIPath.relativize(baseSourcePath.toUri(), referenceSourcePath.toUri());
	}

}
