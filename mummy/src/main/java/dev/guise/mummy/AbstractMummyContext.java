/*
 * Copyright © 2026 GlobalMentor, Inc. <https://www.globalmentor.com/>
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
import static java.nio.file.LinkOption.*;
import static java.util.Objects.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

import org.jspecify.annotations.*;

/// Abstract base implementation of [MummyContext] that caches site directory paths.
///
/// The site source, target, and description target directories are essential to the identity of the
/// context — the entire artifact tree is rooted in them. They are received as constructor parameters
/// and stored as immutable fields. The constructor validates that each path is in real-path form
/// via [com.globalmentor.io.Paths#checkArgumentRealPath(Path, LinkOption...)].
/// @implNote The constructor performs filesystem I/O to validate the real-path precondition.
/// @author Garret Wilson
public abstract class AbstractMummyContext implements MummyContext {

	private final GuiseProject project;
	private final URI root;
	private final Path siteSourceDirectory;
	private final Path siteTargetDirectory;
	private final Path siteDescriptionTargetDirectory;

	/// Constructor.
	/// @param project The Guise project.
	/// @param siteSourceDirectory The base directory of the site source, in real-path form.
	/// @param siteTargetDirectory The output directory of the site, in real-path form.
	/// @param siteDescriptionTargetDirectory The output directory of the site description, in real-path form.
	/// @throws IllegalArgumentException if any directory path is not in real-path form.
	/// @throws IOException if an I/O error occurs during real-path validation.
	protected AbstractMummyContext(@NonNull final GuiseProject project, @NonNull final Path siteSourceDirectory, @NonNull final Path siteTargetDirectory,
			@NonNull final Path siteDescriptionTargetDirectory) throws IOException {
		this.project = requireNonNull(project);
		this.siteSourceDirectory = checkArgumentRealPath(siteSourceDirectory, NOFOLLOW_LINKS);
		this.siteTargetDirectory = checkArgumentRealPath(siteTargetDirectory, NOFOLLOW_LINKS);
		this.siteDescriptionTargetDirectory = checkArgumentRealPath(siteDescriptionTargetDirectory, NOFOLLOW_LINKS);
		this.root = this.siteSourceDirectory.toUri();
	}

	@Override
	public GuiseProject getProject() {
		return project;
	}

	/// {@inheritDoc}
	/// @implSpec This implementation returns the URI form of the site source directory.
	@Override
	public URI getRoot() {
		return root;
	}

	@Override
	public Path getSiteSourceDirectory() {
		return siteSourceDirectory;
	}

	@Override
	public Path getSiteTargetDirectory() {
		return siteTargetDirectory;
	}

	@Override
	public Path getSiteDescriptionTargetDirectory() {
		return siteDescriptionTargetDirectory;
	}

}
