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

package dev.guise.mummy.mummify;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import org.jspecify.annotations.*;

import com.globalmentor.net.MediaType;

import dev.guise.mummy.*;

/// Mummifier for processing resources with a source path.
///
/// During mummification, it is important to determine references such as relative links in relation to the _context artifact_. An artifact such as
/// `/foo/bar/index.html` may merely be the implementation for storing the content of the `/foo/bar/`, and other resources will refer to
/// `/foo/bar/`, not `/foo/bar/index.html`. In that case `/foo/bar/` will be the context artifact, and it should be used when
/// requesting e.g. child or sibling artifacts from the mummy context.
/// @author Garret Wilson
public interface SourcePathMummifier extends Mummifier {

	/// Retrieves the extensions of files supported by this mummifier.
	///
	/// An extension may be a simple extension such as `foo`, or a compound extension such as `foo.bar`, the latter of which would match a
	/// file ending in `.foo.bar`, such as `example.foo.bar`.
	/// @return The extensions of filenames for file types supported by this mummifier.
	public @NonNull Set<String> getSupportedFilenameExtensions();

	/// Determines the media type for an artifact from the given source path.
	///
	/// A mummifier must not return an unsupported media type. If a mummifier only supports a single media type, it may assume that the given path is of the
	/// supported type and return that media type.
	/// @param context The context of static site generation.
	/// @param sourcePath The path in the site source directory; not guaranteed to exist;
	/// @return The target media type for the generated artifact, if known; will not be present if unknown or unsupported.
	/// @throws IOException if there is an I/O error determining the media type.
	public Optional<MediaType> getArtifactMediaType(@NonNull MummyContext context, @NonNull final Path sourcePath) throws IOException;

	/// Plans mummification of a source path supported by this mummifier.
	/// @param context The context of static site generation.
	/// @param sourcePath The source path to be mummified.
	/// @param targetPath The target path in the site target directory for the artifact.
	/// @return An artifact describing the resource to be mummified.
	/// @throws IOException if there is an I/O error during planning.
	public Artifact plan(@NonNull MummyContext context, @NonNull Path sourcePath, @NonNull Path targetPath) throws IOException;

}
