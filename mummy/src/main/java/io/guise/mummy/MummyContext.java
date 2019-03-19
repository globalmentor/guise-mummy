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

import static com.globalmentor.java.Conditions.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

import javax.annotation.*;

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
		//TODO create Paths utility method for changing the base
		final Path relativePath = getSiteSourceDirectory().relativize(sourcePath);
		checkArgument(!relativePath.isAbsolute(), "Source path %s is not on in the source directory tree %s.", sourcePath, getSiteSourceDirectory());
		return getSiteTargetDirectory().resolve(relativePath);
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

}
