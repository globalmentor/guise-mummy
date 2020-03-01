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

package io.guise.mummy.mummify;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import javax.annotation.*;

import io.clogr.Clogged;
import io.guise.mummy.*;

/**
 * Processes a resource for mummification.
 * <p>
 * During mummification, it is important to determine references such as relative links in relation to the <dfn>context artifact</dfn>. An artifact such as
 * <code>/foo/bar/index.html</code> may merely be the implementation for storing the content of the <code>/foo/bar/</code>, and other resources will refer to
 * <code>/foo/bar/</code>, not <code>/foo/bar/index.html</code>. In that case <code>/foo/bar/</code> will be the context artifact, and it should be used when
 * requesting e.g. child or sibling artifacts from the mummy context.
 * </p>
 * @author Garret Wilson
 */
public interface Mummifier extends Clogged {

	/**
	 * Retrieves the extensions of files supported by this mummifier.
	 * <p>
	 * An extension may be a simple extension such as <code>foo</code>, or a compound extension such as <code>foo.bar</code>, the latter of which would match a
	 * file ending in <code>.foo.bar</code>, such as <code>example.foo.bar</code>.
	 * </p>
	 * @return The extensions of filenames for file types supported by this mummifier.
	 */
	public @Nonnull Set<String> getSupportedFilenameExtensions();

	/**
	 * Plans mummification of a source path supported by this mummifier.
	 * @param context The context of static site generation.
	 * @param sourcePath The source path to be mummified.
	 * @return An artifact describing the resource to be mummified.
	 * @throws IOException if there is an I/O error during planning.
	 */
	public Artifact plan(@Nonnull MummyContext context, @Nonnull final Path sourcePath) throws IOException;

	/**
	 * Mummifies a resource.
	 * @apiNote This method should normally not be overridden. Implementations should override {@link #mummify(MummyContext, Artifact, Artifact)} instead.
	 * @implSpec The default implementation delegates to {@link #mummify(MummyContext, Artifact, Artifact)}.
	 * @param context The context of static site generation.
	 * @param artifact The artifact being generated.
	 * @throws IOException if there is an I/O error during static site generation.
	 */
	public default void mummify(@Nonnull final MummyContext context, @Nonnull Artifact artifact) throws IOException {
		mummify(context, artifact, artifact);
	}

	/**
	 * Mummifies a resource in the presence of a context artifact, which may or may not be the same as the artifact itself.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param artifact The artifact being generated
	 * @throws IOException if there is an I/O error during static site generation.
	 */
	public void mummify(@Nonnull final MummyContext context, @Nonnull Artifact contextArtifact, @Nonnull Artifact artifact) throws IOException;

}
