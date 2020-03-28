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
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

import javax.annotation.*;

import com.globalmentor.net.DomainName;

import io.clogr.Clogged;
import io.confound.config.*;
import io.confound.config.file.*;
import io.guise.mummy.deploy.*;
import io.guise.mummy.deploy.aws.*;
import io.guise.mummy.mummify.*;
import io.guise.mummy.mummify.collection.DirectoryMummifier;
import io.guise.mummy.mummify.page.HtmlPageMummifier;
import io.guise.mummy.mummify.page.MarkdownPageMummifier;
import io.guise.mummy.mummify.page.XhtmlPageMummifier;
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

	/** The typical prefix used for the namespace of Guise Mummy elements, such as in an XHTML document or in RDFa metadata. */
	public static final String NAMESPACE_PREFIX = "mummy";

	/** The phases of the default Guise Mummy life cycle, in order. */
	public enum LifeCyclePhase {
		INITIALIZE, VALIDATE, PLAN, MUMMIFY, PREPARE_DEPLOY, DEPLOY
	};

	//# configuration

	/**
	 * The configuration for the domain name of the project (e.g. <code>example.com.</code>). May not be the same as the site domain (e.g.
	 * <code>www.example.com</code>). If present must be in absolute form; that is, ending with a dot <code>.</code> delimiter.
	 */
	public static final String CONFIG_KEY_DOMAIN = "domain";

	/**
	 * Retrieves the configured domain as a FQDN.
	 * @param configuration The configuration from which to retrieve values.
	 * @return The absolute domain, if present.
	 * @see #CONFIG_KEY_DOMAIN
	 * @throws ConfigurationException if the domain is not absolute.
	 */
	public static Optional<DomainName> findConfiguredDomain(@Nonnull final Configuration configuration) throws ConfigurationException {
		return configuration.findString(CONFIG_KEY_DOMAIN).map(DomainName::of).map(domain -> {
			if(!domain.isAbsolute() || domain.isRoot()) {
				throw new ConfigurationException(
						String.format("The `%s` configuration `%s` must be a fully-qualified, non-root domain, ending in a dot `%s` character.", CONFIG_KEY_DOMAIN, domain,
								DomainName.DELIMITER)); //TODO i18n
			}
			return domain;
		});
	}

	//## site configuration

	/** The configuration for the canonical domain name of the site, such as <code>example.com</code> or <code>www.example.com</code>. */
	public static final String CONFIG_KEY_SITE_DOMAIN = "site.domain";

	/**
	 * Retrieves the configured site domain as a FQDN. The value of {@value #CONFIG_KEY_SITE_DOMAIN} if any is resolved against the value of
	 * {@value #CONFIG_KEY_DOMAIN}, if any, defaulting to the value of {@link #CONFIG_KEY_DOMAIN}.
	 * @param configuration The configuration from which to retrieve values.
	 * @return The absolute form of the site domain, if present.
	 * @see #CONFIG_KEY_SITE_DOMAIN
	 * @see #CONFIG_KEY_DOMAIN
	 * @throws ConfigurationException if the site domain cannot be resolved to absolute form.
	 */
	public static Optional<DomainName> findConfiguredSiteDomain(@Nonnull final Configuration configuration) throws ConfigurationException {
		final Optional<DomainName> configuredDomain = findConfiguredDomain(configuration);
		final DomainName base = configuredDomain.orElse(DomainName.EMPTY);
		return configuration.findString(CONFIG_KEY_SITE_DOMAIN).map(DomainName::of).map(base::resolve).or(() -> configuredDomain).map(siteDomain -> {
			if(!siteDomain.isAbsolute() || siteDomain.isRoot()) {
				throw new ConfigurationException(String.format(
						"The `%s` configuration `%s` must be a fully-qualified, non-root domain name (FQDN), ending in a dot `%s` character; or resolve against a `%s` configuration that is a FQDN.",
						CONFIG_KEY_SITE_DOMAIN, siteDomain, DomainName.DELIMITER, CONFIG_KEY_DOMAIN));
			}
			return siteDomain;
		});
	}

	/**
	 * The configuration for the list of alternative domain names of the site, such as <code>www.example.com</code> (as an alternative for
	 * <code>example.com</code>) or <code>example.com</code> (if the canonical domain name is <code>www.example.com</code>).
	 */
	public static final String CONFIG_KEY_SITE_ALT_DOMAINS = "site.altDomains";

	/**
	 * Retrieves the configured alternative site domain as FQDNs.
	 * @param configuration The configuration from which to retrieve values.
	 * @return The absolute form of the site alternate domains, if present.
	 * @see #CONFIG_KEY_SITE_ALT_DOMAINS
	 * @throws ConfigurationException if one of the site alternate domains cannot be resolved to absolute form.
	 */
	public static Optional<Collection<DomainName>> findConfiguredSiteAltDomains(@Nonnull final Configuration configuration) {
		final DomainName base = findConfiguredDomain(configuration).orElse(DomainName.EMPTY);
		return configuration.findCollection(CONFIG_KEY_SITE_ALT_DOMAINS, String.class)
				.map(names -> names.stream().map(DomainName::of).map(base::resolve).map(siteAltDomain -> {
					if(!siteAltDomain.isAbsolute() || siteAltDomain.isRoot()) {
						throw new ConfigurationException(String.format(
								"The `%s` configuration `%s` must be a fully-qualified, non-root domain name (FQDN), ending in a dot `%s` character; or resolve against a `%s` configuration that is a FQDN.",
								CONFIG_KEY_SITE_ALT_DOMAINS, siteAltDomain, DomainName.DELIMITER, CONFIG_KEY_DOMAIN)); //TODO i18n
					}
					return siteAltDomain;
				}).collect(toCollection(LinkedHashSet::new)));
	}

	//## mummy configuration

	/** The base filename for the Mummy configuration within the source tree. */
	public static final String MUMMY_CONFIG_BASE_FILENAME = ".guise-mummy";

	/**
	 * The configuration for the list of base filenames of files, in order of priority, that serve as content for a collection; defaults to <code>["index"]</code>
	 * @implNote Currently not all deployment targets support multiple collection content names; for the meantime only one collection content base name should be
	 *           used.
	 */
	public static final String CONFIG_KEY_COLLECTION_CONTENT_BASE_NAMES = "mummy.collectionContentBaseNames";
	/** The configuration indicating <code>true</code> if extensions should be removed from page names (i.e. clean URLs) during mummification. */
	public static final String CONFIG_KEY_MUMMY_PAGE_NAMES_BARE = "mummy.pageNamesBare";
	/**
	 * The regular expression indicating if an artifact should be considered <dfn>veiled</dfn> if its name matches. The regular expression may have at most one
	 * matching group. If there is a matching group and it provides a match, the artifact will be renamed to the value of the matched group. If there is no
	 * matching group, the artifact will not have a separate veiled name. In either case, the artifact may still be subject to other renaming rules, such as
	 * extension removal for bare names.
	 * @see #CONFIG_KEY_MUMMY_PAGE_NAMES_BARE
	 */
	public static final String CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN = "mummy.veilNamePattern";

	//## deploy configuration

	/** The configuration indicating the DNS to use, if any, for deployment. Must be a {@link Section} indicating a {@link Dns}. */
	public static final String CONFIG_KEY_DEPLOY_DNS = "deploy.dns";
	/** The configuration indicating the deployment targets, if any. Must be a collection of {@link Section} each indicating a {@link DeployTarget}. */
	public static final String CONFIG_KEY_DEPLOY_TARGETS = "deploy.targets";

	//mummifier settings

	/** The default mummifier for normal files. */
	private final SourcePathMummifier defaultFileMummifier = new OpaqueFileMummifier();

	/** The default mummifier for normal directories. */
	private final SourcePathMummifier defaultDirectoryMummifier = new DirectoryMummifier();

	/** The registered mummifiers by supported extensions. */
	private final Map<String, SourcePathMummifier> fileMummifiersByExtension = new HashMap<>();

	private boolean full = false;

	/** @return <code>true</code> if full mummification is enabled; <code>false</code> if mummification is incremental. */
	public boolean isFull() {
		return full;
	}

	/**
	 * Enables or disables full mummification.
	 * @param full <code>true</code> if full mummification should occur; <code>false</code> if mummification should be incremental.
	 */
	public void setFull(final boolean full) {
		this.full = full;
	}

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
		registerFileMummifier(new HtmlPageMummifier());
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

		//# initialize phase
		final Context context = initialize(project); //the initialize phase must always occur

		//# validate phase
		if(phase.compareTo(LifeCyclePhase.VALIDATE) >= 0) {
			validate(context);
		}

		//# plan phase
		if(phase.compareTo(LifeCyclePhase.PLAN) >= 0) {
			final Artifact rootArtifact = new DirectoryMummifier().plan(context, context.getSiteSourceDirectory(), context.getSiteTargetDirectory()); //TODO create special SiteMummifier extending DirectoryMummifier
			context.updatePlan(rootArtifact);

			printArtifactDescription(context, rootArtifact);

			//# mummify phase
			if(phase.compareTo(LifeCyclePhase.MUMMIFY) >= 0) {
				rootArtifact.getMummifier().mummify(context, rootArtifact);
			}

			//# prepare-deploy phase
			if(phase.compareTo(LifeCyclePhase.PREPARE_DEPLOY) >= 0) {

				//configured DNS
				final Optional<Dns> deployDns = context.getConfiguration().findSection(CONFIG_KEY_DEPLOY_DNS).map(dnsConfiguration -> {
					final String dnsType = dnsConfiguration.getSectionType().orElseThrow(() -> new ConfigurationException("No DNS type configured."));
					Configuration.check(dnsType.equals(Route53.class.getSimpleName()), "Currently only Route 53 DNS is supported; unknown type `%s`.", dnsType);
					return new Route53(context, dnsConfiguration);
				});
				deployDns.ifPresent(context::setDeployDns); //store the DNS in the context for later

				//configured targets
				final List<DeployTarget> deployTargets = context.getConfiguration().findCollection(CONFIG_KEY_DEPLOY_TARGETS, Section.class).map(targetSections -> {
					return targetSections.stream().map(targetSection -> {
						final String targetType = targetSection.getSectionType().orElseThrow(() -> new ConfigurationException("Target has no type configured."));
						final DeployTarget target;
						if(targetType.equals(CloudFront.class.getSimpleName())) {
							target = new CloudFront(context, targetSection);
						} else if(targetType.equals(S3.class.getSimpleName())) {
							target = new S3(context, targetSection);
						} else if(targetType.equals(S3Website.class.getSimpleName())) {
							target = new S3Website(context, targetSection);
						} else {
							throw new ConfigurationException(String.format("Unknown deployment target type: `%s`.", targetType));
						}
						return target;
					}).collect(toList());
				}).orElse(emptyList());
				context.setDeployTargets(deployTargets); //store the targets in the context for later

				//prepare the DNS
				deployDns.ifPresent(throwingConsumer(dns -> {
					dns.prepare(context);
				}));
				//prepare the targets
				deployTargets.forEach(throwingConsumer(target -> target.prepare(context))); //prepare the targets

				//# deploy phase
				if(phase.compareTo(LifeCyclePhase.DEPLOY) >= 0) {
					//deploy the DNS
					deployDns.ifPresent(throwingConsumer(dns -> {
						dns.deploy(context, rootArtifact);
					}));
					//deploy the targets
					for(final DeployTarget target : deployTargets) {
						final Optional<URI> deployUrl = target.deploy(context, rootArtifact);
						deployUrl.ifPresent(deployUrls::add);
						getLogger().info("({}) Successfully deployed site to {}.", target.getClass().getSimpleName(),
								deployUrl.map(url -> "<" + url + ">").orElse("target"));
					}
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

		getLogger().debug("Mummification: {}", context.isFull() ? "full" : "incremental"); //TODO i18n
		getLogger().debug("Configuration: page names bare = `{}`", context.getConfiguration().findBoolean(CONFIG_KEY_MUMMY_PAGE_NAMES_BARE).orElse(false));

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
		final Configuration configuration = context.getConfiguration();
		//make sure all the configured domains resolve to FQDNs
		findConfiguredDomain(configuration);
		findConfiguredSiteDomain(configuration);
		findConfiguredSiteAltDomains(configuration);
	}

	//TODO document
	private void printArtifactDescription(@Nonnull final MummyContext context, @Nonnull final Artifact artifact) { //TODO transfer to CLI
		final TurfSerializer turfSerializer = new TurfSerializer();

		//TODO remove debug code
		getLogger().trace("{} ({})", artifact.getTargetPath(), artifact.getTargetPath().toUri());
		if(artifact.getResourceDescription().hasProperties()) {
			try {
				getLogger().trace("    {}", turfSerializer.serializeDescription(new StringBuilder(), artifact.getResourceDescription()));
			} catch(final IOException ioException) {
				getLogger().error("Error debugging resource description.", ioException);
			}
		}

		context.findParentArtifact(artifact).ifPresent(parent -> getLogger().trace("  parent: {}", parent.getTargetPath()));
		final Collection<Artifact> siblings = context.siblingArtifacts(artifact).collect(toList()); //TODO make debugging calls more efficient, or transfer to describe functionality  
		if(!siblings.isEmpty()) {
			getLogger().trace("  siblings: {}", siblings);
		}
		final Collection<Artifact> children = context.childArtifacts(artifact).collect(toList());
		if(!children.isEmpty()) {
			getLogger().trace("  children: {}", children);
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
		defaultSettings.put(CONFIG_KEY_COLLECTION_CONTENT_BASE_NAMES, List.of("index"));
		defaultSettings.put(CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN, Pattern.compile("_(.*)"));
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

		@Override
		public boolean isFull() {
			return GuiseMummy.this.isFull();
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

		private List<DeployTarget> deployTargets = null;

		/**
		 * Sets the targets configured for deployment.
		 * @param deployTargets The deployment targets.
		 */
		protected void setDeployTargets(@Nonnull final List<DeployTarget> deployTargets) {
			this.deployTargets = requireNonNull(deployTargets);
		}

		@Override
		public Optional<List<DeployTarget>> getDeployTargets() {
			return Optional.ofNullable(deployTargets);
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
