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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import io.confound.config.Configuration;
import io.confound.config.ObjectMapConfiguration;

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

	private Map<String, Object> settings;

	/**
	 * Provides access to the settings overriding the fixture project configuration.
	 * @apiNote This method allows individual tests to override the configuration dynamically on a test-by-test basis.
	 * @return The settings for fixture project, falling back to the defaults.
	 */
	protected Map<String, Object> getFixtureProjectSettings() {
		return settings;
	}

	/**
	 * @return Returns the fixture project site source directory.
	 * @apiNote This is a convenience method to retrieve the path from the project configuration.
	 * @see GuiseMummy#PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY
	 */
	protected Path getSiteSourceDirectory() {
		return getFixtureProject().getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY);
	}

	/**
	 * @return Returns the fixture project site target directory.
	 * @apiNote This is a convenience method to retrieve the path from the project configuration.
	 * @see GuiseMummy#PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY
	 */
	protected Path getSiteTargetDirectory() {
		return getFixtureProject().getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY);
	}

	/**
	 * Sets up the fixture project.
	 * @implSpec This method calls {@link #configure(Map)} to set test-specific configuration settings.
	 * @implSpec This method calls {@link #populateSiteSourceDirectory(Path)} to copy files into the test site source directory in preparation for testing.
	 * @param tempDir The temporary directory used for the integration test.
	 * @throws IOException If there was an I/O error during setup.
	 * @see #getFixtureProject()
	 */
	@BeforeEach
	protected void setupFixture(@TempDir final Path tempDir) throws IOException {
		//create a custom configuration falling back to the default configuration
		final Configuration defaultConfiguration = getDefaultConfiguration(tempDir);
		settings = new HashMap<>();
		configure(settings);
		final Configuration fixtureConfiguration = new ObjectMapConfiguration(settings).withFallback(defaultConfiguration);
		//create the fixture project
		fixtureProject = new DefaultGuiseProject(tempDir, fixtureConfiguration);
		//populate the site source directory
		final Path siteSourceDirectory = fixtureProject.getConfiguration().getPath(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY);
		createDirectories(siteSourceDirectory);
		populateSiteSourceDirectory(siteSourceDirectory);
	}

	/**
	 * Adds settings to the project configuration override.
	 * @apiNote These settings are later available via {@link #getFixtureProjectSettings()}.
	 * @implSpec This implementation does nothing.
	 * @param settings The settings to override the default project settings.
	 * @see #getFixtureProjectSettings()
	 */
	protected void configure(@Nonnull final Map<String, Object> settings) {
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
