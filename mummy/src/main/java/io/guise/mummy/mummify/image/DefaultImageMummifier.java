/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy.mummify.image;

import static com.globalmentor.io.Files.*;
import static com.globalmentor.io.Images.*;
import static java.nio.file.StandardCopyOption.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import io.guise.mummy.*;

/**
 * General image mummifier.
 * @implSpec This implementation supports GIF, JPEG, and PNG files.
 * @implSpec TODO This implementation uses <a href="https://docs.oracle.com/en/java/javase/11/docs/api/java.desktop/javax/imageio/package-summary.html">Java
 *           Image I/O</a>.
 * @implSpec This implementation merely copies the file during mummification with no further action. Any existing target file will be replaced.
 * @author Garret Wilson
 */
public class DefaultImageMummifier extends BaseImageMummifier {

	/** No-args constructor. */
	public DefaultImageMummifier() {
		super(Set.of(GIF_MEDIA_TYPE, JPEG_MEDIA_TYPE, PNG_MEDIA_TYPE));
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation merely copies the file with no further action.
	 */
	@Override
	public void mummifyFile(final MummyContext context, final Artifact contextArtifact, final Artifact artifact) throws IOException {
		final Path sourceFile = artifact.getSourcePath();
		checkArgumentRegularFile(sourceFile);
		Files.copy(sourceFile, artifact.getTargetPath(), REPLACE_EXISTING);
	}

}
