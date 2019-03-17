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

import static com.globalmentor.java.Conditions.checkArgument;
import static java.nio.file.Files.*;
import static java.util.Collections.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.util.Set;

/**
 * Mummifier for directories.
 * @author Garret Wilson
 */
public class DirectoryMummifier implements ResourceMummifier { //TODO extend site mummifier from this class

	@Override
	public Set<String> getSupportedFilenameExtensions() {
		return emptySet();
	}

	@Override
	public void mummify(final MummifyContext context, final Artifact artifact) throws IOException {
		checkArgument(artifact instanceof DirectoryArtifact, "Artifact %s is not a directory artifact.");
		checkArgument(isDirectory(artifact.getSourceFile()), "Source path %s does not exist or is not a directory.");

		final DirectoryArtifact directoryArtifact = (DirectoryArtifact)artifact;

		//create the directory
		System.out.println("created directory:" + directoryArtifact);
		createDirectories(artifact.getOutputFile());

		//mummify the directory content artifact, if present
		directoryArtifact.getContentArtifact().ifPresent(throwingConsumer(contentArtifact -> contentArtifact.getMummifier().mummify(context, contentArtifact)));

		//mummify each child artifact
		for(final Artifact childArtifact : directoryArtifact.getChildArtifacts()) {
			childArtifact.getMummifier().mummify(context, childArtifact);
		}
	}

}
