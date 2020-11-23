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

package io.guise.mummy;

import static io.guise.mummy.GuiseMummy.*;
import static java.nio.file.Files.*;

import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/**
 * A base integration test for providing end-to-end testing of Guise Mummy.
 * <p>
 * A subclass may call {@link #mummify(LifeCyclePhase)} to initiate testing, or testing may be done manually using the fixture project which has been set up for
 * each test.
 * </p>
 * @author Garret Wilson
 */
public abstract class BaseEndToEndIT {

	private GuiseProject fixtureProject;

	/** @return The project configured for testing. */
	protected GuiseProject getFixtureProject() {
		return fixtureProject;
	}

	@BeforeEach
	protected void setupFixture(@TempDir final Path tempDir) throws IOException {
		fixtureProject = new DefaultGuiseProject(tempDir, getDefaultConfiguration(tempDir));
		final Path siteSourceDirectory = fixtureProject.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY);
		createDirectories(siteSourceDirectory);
		populateSiteSourceDirectory(siteSourceDirectory);
	}

	/**
	 * Populates the site source directory with any files necessary for testing. This test runs once per test as part of setup.
	 * @implSpec This implementation does nothing.
	 * @param siteSourceDirectory The project site source directory.
	 * @see GuiseMummy#PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY
	 */
	protected void populateSiteSourceDirectory(@Nonnull final Path siteSourceDirectory) throws IOException {
	}

	/**
	 * A convenience method to mummify current fixture project.
	 * @param phase The life cycle phase to execute (including all those before it).
	 * @throws IOException if there is an I/O error generating the static site.
	 * @see #getFixtureProject()
	 * @see GuiseMummy#mummify(GuiseProject, GuiseMummy.LifeCyclePhase)
	 */
	protected void mummify(@Nonnull final GuiseMummy.LifeCyclePhase phase) throws IOException {
		final GuiseMummy mummy = new GuiseMummy();
		mummy.mummify(getFixtureProject(), phase);
	}

}
