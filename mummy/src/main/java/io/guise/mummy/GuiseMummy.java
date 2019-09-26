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

import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.io.Paths.*;
import static java.nio.file.Files.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.annotation.*;

import io.clogr.Clogged;
import io.confound.config.*;
import io.confound.config.file.*;
import io.guise.mummy.deploy.*;
import io.guise.mummy.deploy.aws.*;
import io.urf.model.UrfResourceDescription;
import io.urf.turf.TurfSerializer;

/**
 * Guise static site generator.
 * @author Garret Wilson
 */
public class GuiseMummy implements Clogged {

	/** The official name of Guise Mummy. */
	public static final String NAME;

	/** The current Guise Mummy version. */
	public static final String VERSION;

	/** The configuration key containing the name. */
	private static final String CLASS_CONFIG_KEY_NAME = "name";

	/** The configuration key containing the version. */
	private static final String CLASS_CONFIG_KEY_VERSION = "version";

	static {
		try {
			final Configuration configuration = ResourcesConfigurationManager.loadConfigurationForClass(GuiseMummy.class)
					.orElseThrow(ResourcesConfigurationManager::createConfigurationNotFoundException);
			NAME = configuration.getString(CLASS_CONFIG_KEY_NAME);
			VERSION = configuration.getString(CLASS_CONFIG_KEY_VERSION);
		} catch(final IOException ioException) {
			throw new ConfigurationException(ioException);
		}
	}

	/** The string form or namespace of Guise Mummy elements, such as in an XHTML document or as the leading IRI segment of RDFa metadata. */
	public static final String NAMESPACE_STRING = "https://guise.io/name/mummy/";

	/** The namespace of Guise Mummy elements, such as in an XHTML document or as the leading IRI segment of RDFa metadata. */
	public static final URI NAMESPACE = URI.create(NAMESPACE_STRING);

	/** The phases of the default Guise Mummy life cycle, in order. */
	public enum LifeCyclePhase {
		INITIALIZE, VALIDATE, PLAN, MUMMIFY, PREPARE_DEPLOY, DEPLOY
	};

	//# configuration

	//## site configuration

	/** The configuration for the canonical domain name of the site, such as <code>example.com</code> or <code>www.example.com</code>. */
	public static final String CONFIG_KEY_SITE_DOMAIN = "site.domain";

	/**
	 * The configuration for the list of alias domain names of the site, such as <code>www.example.com</code> (as an alias for <code>example.com</code>) or
	 * <code>example.com</code> (if the canonical domain name is <code>www.example.com</code>).
	 */
	public static final String CONFIG_KEY_SITE_ALIASES = "site.aliases";

	//## mummy configuration

	/** The base filename for the Mummy configuration within the source tree. */
	public static final String MUMMY_CONFIG_BASE_FILENAME = ".guise-mummy";

	/** The configuration indicating <code>true</code> if extensions should be removed from page names (i.e. clean URLs) during mummification. */
	public static final String CONFIG_KEY_MUMMY_PAGE_NAMES_BARE = "mummy.pageNamesBare";

	//## deploy configuration

	/** The configuration indicating the DNS to use, if any, for deployment. Must be a {@link Section} indicating a {@link Dns}. */
	public static final String CONFIG_KEY_DEPLOY_DNS = "deploy.dns";

	//mummifier settings

	/** The default mummifier for normal files. */
	private final SourcePathMummifier defaultFileMummifier = new OpaqueFileMummifier();

	/** The default mummifier for normal directories. */
	private final SourcePathMummifier defaultDirectoryMummifier = new DirectoryMummifier();

	/** The registered mummifiers by supported extensions. */
	private final Map<String, SourcePathMummifier> fileMummifiersByExtension = new HashMap<>();

	//state

	private final List<URI> deployUrls = new ArrayList<>();

	/** @return The URLs of the sites that were successfully deployed. */
	public List<URI> getDeployUrls() {
		return unmodifiableList(deployUrls);
	}

	/**
	 * Registers a mummify for all its supported filename extensions.
	 * @param mummifier The mummifier to register.
	 * @see Mummifier#getSupportedFilenameExtensions()
	 */
	public void registerFileMummifier(@Nonnull final SourcePathMummifier mummifier) {
		mummifier.getSupportedFilenameExtensions().forEach(ext -> fileMummifiersByExtension.put(ext, mummifier));
	}

	/** No-args constructor. */
	public GuiseMummy() {
		//register default resource types
		registerFileMummifier(new MarkdownPageMummifier());
		registerFileMummifier(new XhtmlPageMummifier());
	}

	/**
	 * Performs static site generation on a source directory into a target directory.
	 * @param project The Guise project governing mummification.
	 * @param phase The life cycle phase to execute (including all those before it.
	 * @throws IllegalArgumentException if the configured source directory does not exist or is not a directory.
	 * @throws IllegalArgumentException if the configured source and target directories overlap.
	 * @throws IOException if there is an I/O error generating the static site.
	 */
	public void mummify(@Nonnull final GuiseProject project, @Nonnull final LifeCyclePhase phase) throws IOException {

		//#initialize phase
		final Context context = initialize(project); //the initialize phase must always occur

		//#validate phase
		if(phase.compareTo(LifeCyclePhase.VALIDATE) >= 0) {
			validate(context);
		}

		//#plan phase
		if(phase.compareTo(LifeCyclePhase.PLAN) >= 0) {
			final Artifact rootArtifact = new DirectoryMummifier().plan(context, context.getSiteSourceDirectory()); //TODO create special SiteMummifier extending DirectoryMummifier
			context.updatePlan(rootArtifact);

			printArtifactDescription(context, rootArtifact);

			//#mummify phase
			if(phase.compareTo(LifeCyclePhase.MUMMIFY) >= 0) {
				rootArtifact.getMummifier().mummify(context, rootArtifact);
				generateSiteDescription(context, rootArtifact);
			}

			//#deploy phase
			if(phase.compareTo(LifeCyclePhase.PREPARE_DEPLOY) >= 0) {

				//configure DNS
				context.getConfiguration().findSection(CONFIG_KEY_DEPLOY_DNS).map(dnsConfiguration -> {
					final String dnsType = dnsConfiguration.getSectionType().orElseThrow(() -> new ConfigurationException("No DNS type configured."));
					if(!dnsType.equals(Route53.class.getSimpleName())) {
						throw new ConfigurationException(String.format("Currently only Route 53 DNS is supported; unknown type `%s`.", dnsType));
					}
					return new Route53(context, dnsConfiguration);
				}).ifPresent(throwingConsumer(dns -> {
					dns.prepare(context); //prepare the DNS
					context.setDeployDns(dns); //store the DNS in the context for later
				}));

				//TODO fix for multiple targets
				final DeployTarget deployer = new S3(context);
				deployer.prepare(context);
				if(phase.compareTo(LifeCyclePhase.DEPLOY) >= 0) {
					final Optional<URI> deployUrl = deployer.deploy(context, rootArtifact);
					deployUrl.ifPresent(deployUrls::add);
					getLogger().info("Successfully deployed site to {}.", deployUrl.map(url -> "<" + url + ">").orElse("target"));
				}
			}
		}
	}

	/**
	 * Initialize phase; loads the site configuration, if any, and sets up the mummy context.
	 * @param project The project governing site mummification.
	 * @return A context to use during mummification.
	 * @throws IOException if there is an I/O error during initialization, such as when loading the site configuration.
	 */
	public Context initialize(@Nonnull final GuiseProject project) throws IOException {
		final Path siteSourceDirectory = project.getDirectory().resolve(project.getConfiguration().getPath(GuiseMummy.PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY));

		final Configuration mummyConfiguration;
		if(isDirectory(siteSourceDirectory)) { //leave error generation to validate phase TODO improve Confound not to throw errors if directory doesn't exist?
			//load Mummy configuration as if it all the keys started with "mummy.", falling back to the project configuration;
			//or just use the project configuration if there is no Mummy configuration
			mummyConfiguration = FileSystemConfigurationManager.loadConfigurationForBaseFilename(siteSourceDirectory, MUMMY_CONFIG_BASE_FILENAME)
					//TODO use constant and document prefix for super configuration
					.map(config -> config.superConfiguration("mummy")).map(config -> config.withFallback(project.getConfiguration())).orElse(project.getConfiguration());
		} else {
			mummyConfiguration = project.getConfiguration();
		}

		final Context context = new Context(project, mummyConfiguration);

		getLogger().debug("page names bare: {}", context.getConfiguration().findBoolean(CONFIG_KEY_MUMMY_PAGE_NAMES_BARE));

		return context;
	}

	/**
	 * Validate phase; checks directories and other settings.
	 * @param context The context of static site generation.
	 * @throws IllegalArgumentException if the configured source directory does not exist or is not a directory.
	 * @throws IllegalArgumentException if the configured source and target directories overlap.
	 * @throws IOException if there is an I/O error during validation.
	 */
	public void validate(@Nonnull final MummyContext context) throws IOException {
		checkArgumentDirectory(context.getSiteSourceDirectory());
		checkArgumentDisjoint(context.getSiteSourceDirectory(), context.getSiteTargetDirectory());
	}

	/**
	 * Recursively generates a description file for the indicated artifact and all its comprised artifacts if any.
	 * @param context The context of static site generation.
	 * @param artifact The artifact the description of which is being generated.
	 * @throws IOException if there is an I/O error generating the description.
	 * @see CompositeArtifact#comprisedArtifacts()
	 */
	private void generateSiteDescription(@Nonnull final MummyContext context, @Nonnull final Artifact artifact) throws IOException {
		final UrfResourceDescription description = artifact.getResourceDescription();
		if(description.hasProperties()) { //skip empty descriptions
			final Path targetPath = artifact.getTargetPath();
			if(!(artifact instanceof DirectoryArtifact)) { //skip directories TODO delegate to mummifier for description generation
				final Path descriptionTargetPath = addExtension(changeBase(targetPath, context.getSiteTargetDirectory(), context.getSiteDescriptionTargetDirectory()),
						"@.turf"); //TODO use constant

				//create parent directory as needed
				final Path descriptionTargetParentPath = descriptionTargetPath.getParent();
				if(descriptionTargetParentPath != null) {
					createDirectories(descriptionTargetParentPath);
				}

				//save description
				final TurfSerializer turfSerializer = new TurfSerializer();
				turfSerializer.setFormatted(true);
				try (final OutputStream outputStream = new BufferedOutputStream(newOutputStream(descriptionTargetPath))) {
					turfSerializer.serializeDocument(outputStream, description);
				}
			}
		}

		if(artifact instanceof CompositeArtifact) {
			for(final Artifact comprisedArtifact : (Iterable<Artifact>)((CompositeArtifact)artifact).comprisedArtifacts()::iterator) {
				generateSiteDescription(context, comprisedArtifact);
			}
		}
	}

	//TODO document
	private void printArtifactDescription(@Nonnull final MummyContext context, @Nonnull final Artifact artifact) { //TODO transfer to CLI
		final TurfSerializer turfSerializer = new TurfSerializer();

		//TODO remove debug code
		getLogger().debug("{} ({})", artifact.getTargetPath(), artifact.getTargetPath().toUri());
		if(artifact.getResourceDescription().hasProperties()) {
			try {
				getLogger().debug("    {}", turfSerializer.serializeDescription(new StringBuilder(), artifact.getResourceDescription()));
			} catch(final IOException ioException) {
				getLogger().error("Error debugging resource description.", ioException);
			}
		}

		context.findParentArtifact(artifact).ifPresent(parent -> getLogger().debug("  parent: {}", parent.getTargetPath()));
		final Collection<Artifact> siblings = context.siblingArtifacts(artifact).collect(toList()); //TODO make debugging calls more efficient, or transfer to describe functionality  
		if(!siblings.isEmpty()) {
			getLogger().debug("  siblings: {}", siblings);
		}
		final Collection<Artifact> children = context.childArtifacts(artifact).collect(toList());
		if(!children.isEmpty()) {
			getLogger().debug("  children: {}", children);
		}

		if(artifact instanceof CollectionArtifact) {
			for(final Artifact childArtifact : ((CollectionArtifact)artifact).getChildArtifacts()) {
				printArtifactDescription(context, childArtifact);
			}
		}
	}

	//project configuration

	/** The base filename for the project configuration. */
	public static final String PROJECT_CONFIG_BASE_FILENAME = "guise-project";

	/** The default relative path of the project source directory. Analogous to Maven's <code>${project.basedir}/src</code> property. */
	public final static Path DEFAULT_PROJECT_SOURCE_RELATIVE_DIR = Paths.get("src");

	/**
	 * The default relative path of the site source directory. Similar to the default value of Maven's <code>siteDirectory</code> property, which is
	 * <code>${project.basedir}/src/site</code>..
	 * @see <a href="https://maven.apache.org/plugins/maven-site-plugin/site-mojo.html#siteDirectory">Apache Maven Site Plugin: site:site
	 *      &lt;siteDirectory&gt;</a>.
	 */
	public final static Path DEFAULT_PROJECT_SITE_SOURCE_RELATIVE_DIR = DEFAULT_PROJECT_SOURCE_RELATIVE_DIR.resolve("site");

	/**
	 * The default relative path of the build directory. Analogous to the default value of Maven's <code>project.build.directory</code> property, which is
	 * <code>${project.basedir}/target</code>.
	 */
	public final static Path DEFAULT_PROJECT_BUILD_RELATIVE_DIR = Paths.get("target");

	/**
	 * The default relative path of the site target directory. Similar to the default value of Maven's <code>generatedSiteDirectory</code> property, which is
	 * <code>${project.build.directory}/generated-site</code>.
	 * @see <a href="https://maven.apache.org/plugins/maven-site-plugin/site-mojo.html#generatedSiteDirectory">Apache Maven Site Plugin: site:site
	 *      &lt;generatedSiteDirectory&gt;</a>.
	 * @see #DEFAULT_PROJECT_SITE_SOURCE_RELATIVE_DIR
	 */
	public final static Path DEFAULT_PROJECT_SITE_TARGET_RELATIVE_DIR = DEFAULT_PROJECT_BUILD_RELATIVE_DIR.resolve("site");

	/**
	 * The default relative path of the site description target directory.
	 * @see #DEFAULT_PROJECT_SITE_TARGET_RELATIVE_DIR
	 */
	public final static Path DEFAULT_PROJECT_SITE_DESCRIPTION_TARGET_RELATIVE_DIR = DEFAULT_PROJECT_BUILD_RELATIVE_DIR.resolve("site-description");

	public static final String PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY = "siteSourceDirectory";
	public static final String PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY = "siteTargetDirectory";
	public static final String PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY = "siteDescriptionTargetDirectory";

	/**
	 * Creates a Guise Mummy project based upon a project directory and optional explicit directory overrides.
	 * @apiNote Because the paths other than the project directory may be relative, as may those in the configuration files, when retrieving paths they must be
	 *          resolved against the project directory.
	 * @param projectDirectory The required absolute base directory for the project, in which the project file, if any, would be found.
	 * @param siteSourceDirectory The site source directory, or if <code>null</code> falling back to that specified in the project configuration as
	 *          {@value #PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY}, defaulting to <code>src/site</code> relative to the project directory.
	 * @param siteTargetDirectory The site target directory, or if <code>null</code> falling back to that specified in the project configuration as
	 *          {@value #PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY}, defaulting to <code>target/site</code> relative to the project directory.
	 * @param siteDescriptionTargetDirectory The site description target directory, or if <code>null</code> falling back to that specified in the project
	 *          configuration as {@value #PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY}, defaulting to <code>target/site-description</code> relative to
	 *          the project directory.
	 * @return The new project.
	 * @throws IllegalArgumentException if the project directory is not absolute.
	 * @throws IOException if there is an error determining or loading the project.
	 */
	public static GuiseProject createProject(@Nonnull Path projectDirectory, @Nullable final Path siteSourceDirectory, @Nullable final Path siteTargetDirectory,
			@Nullable final Path siteDescriptionTargetDirectory) throws IOException {
		requireNonNull(projectDirectory);
		projectDirectory = projectDirectory.normalize();

		//default settings (ultimate fallback)
		final Map<String, Object> defaultSettings = new HashMap<>();
		defaultSettings.put(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY, projectDirectory.resolve(DEFAULT_PROJECT_SITE_SOURCE_RELATIVE_DIR)); //siteDirectory=${project.basedir}/src/site
		defaultSettings.put(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY, projectDirectory.resolve(DEFAULT_PROJECT_SITE_TARGET_RELATIVE_DIR)); //siteTargetDirectory=${project.basedir}/target/site
		defaultSettings.put(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY, projectDirectory.resolve(DEFAULT_PROJECT_SITE_DESCRIPTION_TARGET_RELATIVE_DIR)); //siteDescriptionTargetDirectory=${project.basedir}/target/site-description
		final Configuration defaultConfiguration = new ObjectMapConfiguration(defaultSettings);

		//configuration file settings (`guise-project.*`); fall back to the default; if no config file present, just use the default
		final Configuration fileConfiguration = FileSystemConfigurationManager.loadConfigurationForBaseFilename(projectDirectory, PROJECT_CONFIG_BASE_FILENAME)
				.map(projectFileConfiguration -> projectFileConfiguration.withFallback(defaultConfiguration)).orElse(defaultConfiguration);

		//user settings (e.g. supplied on the command line); fall back to the file/default
		final Map<String, Object> userSettings = new HashMap<>();
		if(siteSourceDirectory != null) {
			userSettings.put(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY, siteSourceDirectory);
		}
		if(siteTargetDirectory != null) {
			defaultSettings.put(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY, siteTargetDirectory);
		}
		if(siteDescriptionTargetDirectory != null) {
			defaultSettings.put(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY, siteDescriptionTargetDirectory);
		}
		final Configuration projectConfiguration = new ObjectMapConfiguration(userSettings).withFallback(fileConfiguration); //the user settings override even the project file
		return new DefaultGuiseProject(projectDirectory, projectConfiguration);
	}

	/**
	 * Mutable mummification context controlled by Guise Mummy.
	 * @author Garret Wilson
	 */
	protected class Context extends BaseMummyContext {

		private final Configuration siteConfiguration;

		@Override
		public Configuration getConfiguration() {
			return siteConfiguration;
		}

		//## deploy

		private Dns deployDns = null;

		/**
		 * Sets the DNS configured for deployment.
		 * @param deployDns The DNS to use for deployment.
		 */
		protected void setDeployDns(@Nonnull final Dns deployDns) {
			this.deployDns = requireNonNull(deployDns);
		}

		@Override
		public Optional<Dns> getDeployDns() {
			return Optional.ofNullable(deployDns);
		}

		/**
		 * Site source directory constructor.
		 * @apiNote No validation is performed to ensure directories are valid.
		 * @param project The Guise project.
		 * @param siteConfiguration The configuration for the site.
		 */
		public Context(@Nonnull final GuiseProject project, @Nonnull final Configuration siteConfiguration) {
			super(project);
			this.siteConfiguration = requireNonNull(siteConfiguration);
		}

		@Override
		public SourcePathMummifier getDefaultSourceFileMummifier() {
			return defaultFileMummifier;
		}

		@Override
		public SourcePathMummifier getDefaultSourceDirectoryMummifier() {
			return defaultDirectoryMummifier;
		}

		@Override
		public Optional<SourcePathMummifier> findRegisteredMummifierForSourceFile(@Nonnull final Path sourceFile) {
			return extensions(sourceFile.getFileName().toString()).map(fileMummifiersByExtension::get).filter(Objects::nonNull).findFirst();
		}

		/**
		 * {@inheritDoc}
		 * @implSpec This implementation doesn't support registered source directory mummifiers, and will always return {@link Optional#empty()}.
		 */
		@Override
		public Optional<SourcePathMummifier> findRegisteredMummifierForSourceDirectory(Path sourceDirectory) {
			return Optional.empty();
		}

	}

}
