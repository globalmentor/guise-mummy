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

import static com.globalmentor.io.Filenames.*;

import java.io.IOException;
import java.util.Set;

import org.jspecify.annotations.*;

import com.globalmentor.security.MessageDigests;

import io.clogr.Clogged;
import dev.guise.mummy.*;
import io.urf.format.turf.TURF;
import io.urf.vocab.content.Content;

/// Processes a resource for mummification.
///
/// During mummification, it is important to determine references such as relative links in relation to the _context artifact_. An artifact such as
/// `/foo/bar/index.html` may merely be the implementation for storing the content of the `/foo/bar/`, and other resources will refer to
/// `/foo/bar/`, not `/foo/bar/index.html`. In that case `/foo/bar/` will be the context artifact, and it should be used when
/// requesting e.g. child or sibling artifacts from the mummy context.
/// @author Garret Wilson
public interface Mummifier extends Clogged {

	/// The recommended algorithm for calculating a fingerprint of the generated target artifact.
	/// @apiNote This algorithm should be set to an algorithm known to be implemented on all supported Java versions.
	/// @see Content#FINGERPRINT_PROPERTY_TAG
	public static final MessageDigests.Algorithm FINGERPRINT_ALGORITHM = MessageDigests.SHA_256;

	/// The extension segment for identifying a description sidecar; the `-` in `filename.ext.-.tupr`.
	public static final String DESCRIPTION_FILE_SIDECAR_EXTENSION_SEGMENT = "-";

	/// The compound extension for identifying a description sidecar; the `-.tupr` in `filename.ext.-.tupr`.
	/// @apiNote This extension is used both for source metadata sidecar descriptions and for target description sidecar files.
	public static final String DESCRIPTION_FILE_SIDECAR_EXTENSION = addExtension(DESCRIPTION_FILE_SIDECAR_EXTENSION_SEGMENT, TURF.PROPERTIES_FILENAME_EXTENSION);

	/// Retrieves the extensions of files supported by this mummifier.
	///
	/// An extension may be a simple extension such as `foo`, or a compound extension such as `foo.bar`, the latter of which would match a
	/// file ending in `.foo.bar`, such as `example.foo.bar`.
	/// @return The extensions of filenames for file types supported by this mummifier.
	public @NonNull Set<String> getSupportedFilenameExtensions();

	/// Determines the preferred target filename for an artifact given the indicated source filename.
	/// @apiNote This is normally called by some other mummifier before an artifact is planned so that this mummifier may decide on exactly what filename it would
	///          prefer. Nevertheless there is no guarantee that this method will be called before this mummifier is requested to plan an artifact.
	/// @param context The context of static site generation.
	/// @param filename A suggested filename, such as from the path in the site source directory.
	/// @return The filename this mummifier would prefer to use for the generated target file.
	public String planArtifactTargetFilename(@NonNull MummyContext context, @NonNull String filename);

	/// Mummifies a resource in the presence of a context artifact, which may or may not be the same as the artifact itself.
	/// @param context The context of static site generation.
	/// @param artifact The artifact being generated
	/// @throws IOException if there is an I/O error during static site generation.
	public void mummify(@NonNull final MummyContext context, @NonNull Artifact artifact) throws IOException;

}
