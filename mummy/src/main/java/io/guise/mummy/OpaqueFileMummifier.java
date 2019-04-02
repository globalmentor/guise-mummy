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
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Collections.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Mummifier for files with unknown content.
 * @implSpec This implementation merely copies the file during mummification with no further action. Any existing target file will be replaced.
 * @author Garret Wilson
 */
public class OpaqueFileMummifier extends AbstractSourcePathMummifier {

	@Override
	public Set<String> getSupportedFilenameExtensions() {
		return emptySet();
	}

	@Override
	public Artifact plan(MummyContext context, Path sourcePath) throws IOException {
		checkArgumentRegularFile(sourcePath);
		return new OpaqueFileArtifact(this, sourcePath, getArtifactTargetPath(context, sourcePath));
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation merely copies the file with no further action.
	 */
	@Override
	public void mummify(final MummyContext context, final Artifact contextArtifact, final Artifact artifact) throws IOException {
		final Path sourceFile = artifact.getSourcePath();
		checkArgumentRegularFile(sourceFile);
		Files.copy(sourceFile, artifact.getTargetPath(), REPLACE_EXISTING);
	}

}
