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
import static com.globalmentor.java.Conditions.*;
import static java.nio.file.Files.*;
import static java.util.function.Predicate.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

import javax.annotation.*;

/**
 * Guise static site generator.
 * @author Garret Wilson
 */
public class GuiseMummifier {

	/**
	 * Determines whether a path should be ignored during discovery.
	 * @implSpec This specification currently only ignores dotfiles, for example <code>.git</code> and <code>.gitignore</code>.
	 * @param path The path to check.
	 * @return <code>true</code> if the path should be ignored and excluded from processing.
	 */
	protected boolean isIgnore(@Nonnull final Path path) {
		if(isDotfile(path)) { //ignore dotfiles
			return true;
		}
		return false;
	}

	/**
	 * TODO document; The target directory is created if needed.
	 * @param sourceDirectory
	 * @param targetDirectory
	 * @throws IOException
	 */
	public void mummify(@Nonnull final Path sourceDirectory, @Nonnull final Path targetDirectory) throws IOException {
		checkArgument(isDirectory(sourceDirectory), "Source %s does not exist or is not a directory.");
		createDirectories(targetDirectory);
		System.out.println("target: " + targetDirectory);
		try (final Stream<Path> sourceDirectoryPaths = list(sourceDirectory).filter(not(this::isIgnore))) {
			sourceDirectoryPaths.forEach(System.out::println);
		}
	}

}
