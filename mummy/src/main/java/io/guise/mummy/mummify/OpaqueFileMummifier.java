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

import static com.globalmentor.io.Files.*;
import static java.nio.file.StandardCopyOption.*;
import static java.util.Collections.*;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;

import com.globalmentor.net.MediaType;

import io.guise.mummy.*;

/**
 * Mummifier for files with unknown content.
 * @implSpec This implementation merely copies the file during mummification with no further action. Any existing target file will be replaced.
 * @author Garret Wilson
 */
public class OpaqueFileMummifier extends AbstractFileMummifier {

	@Override
	public Set<String> getSupportedFilenameExtensions() {
		return emptySet();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version returns no media type, because no media type is known for opaque files.
	 */
	@Override
	public Optional<MediaType> getArtifactMediaType(final MummyContext context, final Path sourcePath) throws IOException {
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns an empty list, as there is no way to know source metadata of an opaque file.
	 */
	@Override
	protected List<Entry<URI, Object>> loadSourceMetadata(MummyContext context, Path sourceFile) throws IOException {
		return emptyList();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation merely copies the file with no further action.
	 */
	@Override
	public void mummifyFile(final MummyContext context, final CorporealSourceArtifact artifact) throws IOException {
		final Path sourceFile = artifact.getSourcePath();
		checkArgumentRegularFile(sourceFile);
		Files.copy(sourceFile, artifact.getTargetPath(), REPLACE_EXISTING);
	}

}
