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

package dev.guise.mummy;

import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.io.Files.*;
import static com.globalmentor.io.Paths.*;
import static com.globalmentor.java.Conditions.*;
import static java.nio.file.LinkOption.*;
import static java.nio.file.Files.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

import org.jspecify.annotations.*;

import com.globalmentor.net.DomainName;

import io.clogr.Clogged;
import io.confound.config.*;
import io.confound.config.file.*;
import dev.guise.mummy.deploy.*;
import dev.guise.mummy.deploy.aws.*;
import dev.guise.mummy.deploy.flange.FlangeWebSite;
import dev.guise.mummy.mummify.*;
import dev.guise.mummy.plan.PlanDescriber;
import dev.guise.mummy.mummify.collection.DirectoryMummifier;
import dev.guise.mummy.mummify.image.ImageMummifier;
import dev.guise.mummy.mummify.page.PageMummifier;
import io.urf.format.turf.TurfSerializer;

/// Guise static site generator.
/// @author Garret Wilson
public class GuiseMummy implements Clogged {

	/// The official name of Guise Mummy.
	public static final String NAME;

	/// The current Guise Mummy version.
	public static final String VERSION;

	/// The Guise Mummifier software identifier in human-readable format, including of the name and version.
	/// @implSpec the label is in the form `Guise Mummy version`.
	public static final String LABEL;

	/// The configuration key containing the name.
	private static final String CLASS_CONFIG_KEY_NAME = "name";

	/// The configuration key containing the version.
	private static final String CLASS_CONFIG_KEY_VERSION = "version";

	static {
		try {
			final Configuration configuration = ResourcesConfigurationManager.loadConfigurationForClass(GuiseMummy.class)
					.orElseThrow(ResourcesConfigurationManager::createConfigurationNotFoundException);
			NAME = configuration.getString(CLASS_CONFIG_KEY_NAME);
			VERSION = configuration.getString(CLASS_CONFIG_KEY_VERSION);
			LABEL = NAME + ' ' + VERSION;
		} catch(final IOException ioException) {
			throw new ConfigurationException(ioException);
		}
	}

	/// The string form of the namespace of Guise Mummy elements, such as in an XHTML document or as the leading IRI segment of RDFa metadata.
	public static final String NAMESPACE_STRING = "https://guise.dev/name/mummy/";

	/// The namespace of Guise Mummy elements, such as in an XHTML document or as the leading IRI segment of RDFa metadata.
	public static final URI NAMESPACE = URI.create(NAMESPACE_STRING);

	/// The typical prefix used for the namespace of Guise Mummy elements, such as in an XHTML document or in RDFa metadata.
	public static final String NAMESPACE_PREFIX = "mummy";

	/// The phases of the default Guise Mummy life cycle, in order.
	public enum LifeCyclePhase {
		/// Initialize.
		INITIALIZE,
		/// Validate.
		VALIDATE,
		/// Plan.
		PLAN,
		/// Mummify.
		MUMMIFY,
		/// Prepare deploy.
		PREPARE_DEPLOY,
		/// Deploy.
		DEPLOY
	};

	/// Optional executions that can be activated during mummification.
	/// @apiNote Analogous to Maven plugin executions bound to a lifecycle phase.
	public enum MummyExecution {
		/// Describe the site plan after the PLAN phase.
		DESCRIBE_PLAN
	}

	//# configuration

	/// The configuration for the domain name of the project (e.g. `example.com.`). May not be the same as the site domain (e.g.
	/// `www.example.com`). If present must be in absolute form; that is, ending with a dot `.` delimiter.
	public static final String CONFIG_KEY_DOMAIN = "domain";

	/// Retrieves the configured domain as a FQDN.
	/// @param configuration The configuration from which to retrieve values.
	/// @return The absolute domain, if present.
	/// @see #CONFIG_KEY_DOMAIN
	/// @throws ConfigurationException if the domain is not absolute.
	public static Optional<DomainName> findConfiguredDomain(@NonNull final Configuration configuration) throws ConfigurationException {
		return configuration.findString(CONFIG_KEY_DOMAIN).map(DomainName::of).map(domain -> {
			if(!domain.isAbsolute() || domain.isRoot()) {
				throw new ConfigurationException("The `%s` configuration `%s` must be a fully-qualified, non-root domain, ending in a dot `%s` character.".formatted(
						CONFIG_KEY_DOMAIN, domain, DomainName.DELIMITER)); //TODO i18n
			}
			return domain;
		});
	}

	//## site configuration

	/// The configuration for the canonical domain name of the site, such as `example.com` or `www.example.com`.
	public static final String CONFIG_KEY_SITE_DOMAIN = "site.domain";

	/// Retrieves the configured site domain as a FQDN. The value of `site.domain` if any is resolved against the value of
	/// `domain`, if any, defaulting to the value of [#CONFIG_KEY_DOMAIN].
	/// @param configuration The configuration from which to retrieve values.
	/// @return The absolute form of the site domain, if present.
	/// @see #CONFIG_KEY_SITE_DOMAIN
	/// @see #CONFIG_KEY_DOMAIN
	/// @throws ConfigurationException if the site domain cannot be resolved to absolute form.
	public static Optional<DomainName> findConfiguredSiteDomain(@NonNull final Configuration configuration) throws ConfigurationException {
		final Optional<DomainName> configuredDomain = findConfiguredDomain(configuration);
		final DomainName base = configuredDomain.orElse(DomainName.EMPTY);
		return configuration.findString(CONFIG_KEY_SITE_DOMAIN).map(DomainName::of).map(base::resolve).or(() -> configuredDomain).map(siteDomain -> {
			if(!siteDomain.isAbsolute() || siteDomain.isRoot()) {
				throw new ConfigurationException(
						("The `%s` configuration `%s` must be a fully-qualified, non-root domain name (FQDN), ending in a dot `%s` character; or resolve against a `%s` configuration that is a FQDN."
						).formatted(CONFIG_KEY_SITE_DOMAIN, siteDomain, DomainName.DELIMITER, CONFIG_KEY_DOMAIN));
			}
			return siteDomain;
		});
	}

	/// The configuration for the list of alternative domain names of the site, such as `www.example.com` (as an alternative for
	/// `example.com`) or `example.com` (if the canonical domain name is `www.example.com`).
	public static final String CONFIG_KEY_SITE_ALT_DOMAINS = "site.altDomains";

	/// Retrieves the configured alternative site domain as FQDNs.
	/// @param configuration The configuration from which to retrieve values.
	/// @return The absolute form of the site alternate domains, if present.
	/// @see #CONFIG_KEY_SITE_ALT_DOMAINS
	/// @throws ConfigurationException if one of the site alternate domains cannot be resolved to absolute form.
	public static Optional<Collection<DomainName>> findConfiguredSiteAltDomains(@NonNull final Configuration configuration) {
		final DomainName base = findConfiguredDomain(configuration).orElse(DomainName.EMPTY);
		return configuration.findCollection(CONFIG_KEY_SITE_ALT_DOMAINS, String.class)
				.map(names -> names.stream().map(DomainName::of).map(base::resolve).map(siteAltDomain -> {
					if(!siteAltDomain.isAbsolute() || siteAltDomain.isRoot()) {
						throw new ConfigurationException(
								("The `%s` configuration `%s` must be a fully-qualified, non-root domain name (FQDN), ending in a dot `%s` character; or resolve against a `%s` configuration that is a FQDN."
								).formatted(CONFIG_KEY_SITE_ALT_DOMAINS, siteAltDomain, DomainName.DELIMITER, CONFIG_KEY_DOMAIN)); //TODO i18n
					}
					return siteAltDomain;
				}).collect(toCollection(LinkedHashSet::new)));
	}

	//## mummy configuration

	/// The base filename for the Mummy configuration within the source tree.
	public static final String MUMMY_CONFIG_BASE_FILENAME = ".guise-mummy";

	/// The regular expression indicating if an artifact should be considered an *asset* if its name matches. The regular expression may have at most one
	/// matching group. If there is a matching group and it provides a match, the artifact will be renamed to the value of the matched group. If there is no
	/// matching group, the artifact will not have a separate asset name. In either case, the artifact may still be subject to other renaming rules, such as
	/// extension removal for bare names.
	/// @see PageMummifier#CONFIG_KEY_MUMMY_PAGE_NAMES_BARE
	public static final String CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN = "mummy.assetNamePattern";
	/// The configuration for the list of base filenames of files, in order of priority, that serve as content for a collection; defaults to
	/// `["index"]`. During mummification, any content file discovered will be normalized (renamed if needed) to the first of these base filenames.
	public static final String CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES = "mummy.collectionContentBaseNames";

	/// Determines the normalized collection content resource name from the mummification configuration.
	///
	/// During mummification, collection (directory) content files are normalized to use the first entry from
	/// [#CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES] as the base name, with the page filename extension
	/// [PageMummifier#PAGE_FILENAME_EXTENSION] appended unless bare names are enabled via
	/// [PageMummifier#CONFIG_KEY_MUMMY_PAGE_NAMES_BARE]. This method derives that normalized filename from
	/// the configuration without consulting the artifact tree.
	///
	/// @apiNote This is used by deploy targets and serving infrastructure to configure their "index document"
	///          or "welcome file" settings. The value reflects a contract between mummification (which produces
	///          collection content files with this name) and deployment (which must tell the serving
	///          infrastructure what filename to expect).
	/// @param configuration The mummification configuration.
	/// @return The collection content resource name (e.g. `"index.html"` or `"index"`), or empty if no
	///         collection content base names are configured.
	/// @see #CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES
	/// @see PageMummifier#CONFIG_KEY_MUMMY_PAGE_NAMES_BARE
	/// @see PageMummifier#PAGE_FILENAME_EXTENSION
	public static Optional<String> findCollectionContentResourceName(final Configuration configuration) {
		return configuration.getCollection(CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES, String.class).stream().findFirst().map(baseName -> {
			final boolean isNameBare = configuration.findBoolean(PageMummifier.CONFIG_KEY_MUMMY_PAGE_NAMES_BARE).orElse(false);
			return isNameBare ? baseName : addExtension(baseName, PageMummifier.PAGE_FILENAME_EXTENSION);
		});
	}

	/// The configuration for the base filename for navigation definition; defaults to `.navigation`.
	public static final String CONFIG_KEY_MUMMY_NAVIGATION_BASE_NAME = "mummy.navigationBaseName";
	/// The configuration for the base filename of a template; defaults to `.template`.
	public static final String CONFIG_KEY_MUMMY_TEMPLATE_BASE_NAME = "mummy.templateBaseName";
	/// The configuration specifying the newline character sequence to use. Defaults to `LF` (`U+000A`) in order to have repeatable builds
	/// across platforms.
	public static final String CONFIG_KEY_MUMMY_TEXT_OUTPUT_LINE_SEPARATOR = "mummy.textOutputLineSeparator";
	/// The regular expression indicating if an artifact should be considered *veiled* if its name matches. The regular expression may have at most one
	/// matching group. If there is a matching group and it provides a match, the artifact will be renamed to the value of the matched group. If there is no
	/// matching group, the artifact will not have a separate unveiled name. In either case, the artifact may still be subject to other renaming rules, such as
	/// extension removal for bare names.
	/// @see PageMummifier#CONFIG_KEY_MUMMY_PAGE_NAMES_BARE
	public static final String CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN = "mummy.veilNamePattern";

	//## deploy configuration

	/// The configuration indicating the DNS to use, if any, for deployment. Must be a [Section] indicating a [Dns].
	public static final String CONFIG_KEY_DEPLOY_DNS = "deploy.dns";
	/// The configuration indicating the deployment targets, if any. Must be a collection of [Section] each indicating a [DeployTarget].
	public static final String CONFIG_KEY_DEPLOY_TARGETS = "deploy.targets";

	private boolean full = false;

	/// Indicates whether full mummification is enabled, as opposed to incremental mummification.
	/// @return `true` if full mummification is enabled; `false` if mummification is incremental.
	public boolean isFull() {
		return full;
	}

	/// Enables or disables full mummification.
	/// @param full `true` if full mummification should occur; `false` if mummification should be incremental.
	public void setFull(final boolean full) {
		this.full = full;
	}

	private boolean verbose = false;

	/// Indicates whether verbose output is enabled.
	/// @return `true` if verbose output is enabled.
	public boolean isVerbose() {
		return verbose;
	}

	/// Enables or disables verbose output.
	/// @param verbose `true` if verbose output should be produced.
	public void setVerbose(final boolean verbose) {
		this.verbose = verbose;
	}

	//state

	private final List<URI> deployUrls = new ArrayList<>();

	/// Returns the URLs of the sites that were successfully deployed.
	/// @return The URLs of the sites that were successfully deployed.
	public List<URI> getDeployUrls() {
		return unmodifiableList(deployUrls);
	}

	//mummifier settings

	private Set<Class<? extends SourcePathMummifier>> fileMummifierTypes = new HashSet<>();

	/// Adds a mummifier type. If that type has already been added, no action occurs.
	/// @param mummifierClass The class representing the type of mummifier to add
	/// @throws IllegalArgumentException if the mummifier class does not have a no-args constructor.
	public void addFileMummifierType(@NonNull final Class<? extends SourcePathMummifier> mummifierClass) {
		try {
			mummifierClass.getDeclaredConstructor();
		} catch(final NoSuchMethodException noSuchMethodException) {
			throw new IllegalArgumentException("Mummifier type %s does not declare a no-args constructor.".formatted(mummifierClass.getName()));
		}
		fileMummifierTypes.add(mummifierClass);
	}

	/// No-args constructor.
	public GuiseMummy() {
	}

	/// Performs static site generation on a source directory into a target directory.
	/// @param project The Guise project governing mummification.
	/// @param phase The life cycle phase to execute (including all those before it).
	/// @param executions Optional executions to activate during mummification.
	/// @throws IllegalArgumentException if the configured source directory does not exist or is not a directory.
	/// @throws IllegalArgumentException if the configured source and target directories overlap.
	/// @throws IOException if there is an I/O error generating the static site.
	public void mummify(@NonNull final GuiseProject project, @NonNull final LifeCyclePhase phase,
			@NonNull final Set<MummyExecution> executions) throws IOException {

		//# initialize phase
		getLogger().info("Mummify phase: {}", LifeCyclePhase.INITIALIZE); //TODO i18n
		final Context context = initialize(project); //the initialize phase must always occur

		//# validate phase
		if(phase.compareTo(LifeCyclePhase.VALIDATE) >= 0) {
			getLogger().info("Mummify phase: {}", LifeCyclePhase.VALIDATE); //TODO i18n
			validate(context);
		}

		//# plan phase
		if(phase.compareTo(LifeCyclePhase.PLAN) >= 0) {
			getLogger().info("Mummify phase: {}", LifeCyclePhase.PLAN); //TODO i18n
			final Artifact rootArtifact = new DirectoryMummifier().plan(context, context.getSiteSourceDirectory(), context.getSiteTargetDirectory()); //TODO create special SiteMummifier extending DirectoryMummifier
			final MummyPlan plan = new DefaultMummyPlan(rootArtifact);
			context.setPlan(plan);

			if(executions.contains(MummyExecution.DESCRIBE_PLAN)) {
				new PlanDescriber(plan).describeTo(System.out, isVerbose());
			}

			printArtifactDescription(context, rootArtifact);

			//# mummify phase
			if(phase.compareTo(LifeCyclePhase.MUMMIFY) >= 0) {
				getLogger().info("Mummify phase: {}", LifeCyclePhase.MUMMIFY); //TODO i18n
				final Path siteTargetDirectory = context.getSiteTargetDirectory();
				createDirectories(siteTargetDirectory);
				checkArgumentRealPath(siteTargetDirectory, NOFOLLOW_LINKS); // checking after directory creation catches external creation with wrong case between PLAN and MUMMIFY
				rootArtifact.getMummifier().mummify(context, rootArtifact);
			}

			//# prepare-deploy phase
			if(phase.compareTo(LifeCyclePhase.PREPARE_DEPLOY) >= 0) {
				getLogger().info("Mummify phase: {}", LifeCyclePhase.PREPARE_DEPLOY); //TODO i18n

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
						} else if(targetType.equals(FlangeWebSite.class.getSimpleName())) {
							target = new FlangeWebSite(context, targetSection);
						} else {
							throw new ConfigurationException("Unknown deployment target type: `%s`.".formatted(targetType));
						}
						return target;
					}).toList();
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
					getLogger().info("Mummify phase: {}", LifeCyclePhase.DEPLOY); //TODO i18n
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

	/// Initialize phase; loads the site configuration, if any, and sets up the mummy context.
	/// @param project The project governing site mummification.
	/// @return A context to use during mummification.
	/// @throws IOException if there is an I/O error during initialization, such as when loading the site configuration.
	protected Context initialize(@NonNull final GuiseProject project) throws IOException {
		final Path projectDirectory = project.getDirectory();
		final Path siteSourceDirectory = deriveRealPath(
				projectDirectory.resolve(project.getConfiguration().getPath(GuiseMummy.PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY)), NOFOLLOW_LINKS);

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

		final Path siteTargetDirectory = deriveRealPath(
				projectDirectory.resolve(mummyConfiguration.getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY)), NOFOLLOW_LINKS);
		final Path siteDescriptionTargetDirectory = deriveRealPath(
				projectDirectory.resolve(mummyConfiguration.getPath(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY)), NOFOLLOW_LINKS);
		final Context context = new Context(project, mummyConfiguration, siteSourceDirectory, siteTargetDirectory, siteDescriptionTargetDirectory);
		for(final Class<? extends SourcePathMummifier> mummifierClass : fileMummifierTypes) { //register any additional mummifiers
			SourcePathMummifier fileMummifier;
			try {
				fileMummifier = mummifierClass.getDeclaredConstructor().newInstance();
			} catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
					| SecurityException instantiationException) {
				throw new RuntimeException("Error instantiating mummifier %s.".formatted(mummifierClass.getName()), instantiationException);
			}
			context.registerFileMummifier(fileMummifier);
		}

		getLogger().debug("Mummification: {}", context.isFull() ? "full" : "incremental"); //TODO i18n
		getLogger().debug("Configuration: page names bare = `{}`",
				context.getConfiguration().findBoolean(PageMummifier.CONFIG_KEY_MUMMY_PAGE_NAMES_BARE).orElse(false));

		return context;
	}

	/// The set of deprecated config keys which, if detected during validation, will generate a warning.
	/// @apiNote These will probably eventually be relocated the mummifier API or to a system of plugins.
	private static final Set<String> DEPRECATED_CONFIG_KEYS = Set.of();

	/// The set of obsolete config keys which, if detected during validation, will generate an error.
	/// @apiNote These will probably eventually be relocated the mummifier API or to a system of plugins.
	@SuppressWarnings("deprecation")
	private static final Set<String> OBSOLETE_CONFIG_KEYS = Set.of(PageMummifier.OBSOLETE_CONFIG_KEY_MUMMY_PAGE_NAMES_BARE);

	/// Validate phase; checks directories and other settings.
	/// @implSpec For each deprecated configuration key used, a warning will be generated. If an obsolete configuration key is used, a configuration exception will
	///           be thrown.
	/// @param context The context of static site generation.
	/// @throws IllegalArgumentException if the configured source directory does not exist or is not a directory.
	/// @throws IllegalArgumentException if the configured source and target directories overlap.
	/// @throws ConfigurationException if the configuration is invalid.
	/// @throws IOException if there is an I/O error during validation.
	public void validate(@NonNull final MummyContext context) throws IOException {
		checkArgumentDirectory(context.getSiteSourceDirectory());
		checkArgumentDisjoint(context.getSiteSourceDirectory(), context.getSiteTargetDirectory());
		final Configuration configuration = context.getConfiguration();
		DEPRECATED_CONFIG_KEYS.stream().filter(configuration::hasConfigurationValue)
				.forEach(deprecatedConfigKey -> getLogger().warn("The configuration key `{}` is deprecated and may be removed in the future.", deprecatedConfigKey));
		OBSOLETE_CONFIG_KEYS.stream().filter(configuration::hasConfigurationValue).findAny().ifPresent(obsoleteConfigKey -> {
			throw new ConfigurationException("The configuration key `%s` is obsolete and must not be used.".formatted(obsoleteConfigKey));
		});
		//make sure all the configured domains resolve to FQDNs
		findConfiguredDomain(configuration);
		findConfiguredSiteDomain(configuration);
		findConfiguredSiteAltDomains(configuration);
	}

	//TODO document
	private void printArtifactDescription(@NonNull final MummyContext context, @NonNull final Artifact artifact) { //TODO transfer to CLI
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

		context.getPlan().findParentArtifact(artifact).ifPresent(parent -> getLogger().trace("  parent: {}", parent.getTargetPath()));
		final Collection<Artifact> siblings = context.getPlan().siblingArtifacts(artifact).toList(); //TODO make debugging calls more efficient, or transfer to describe functionality  
		if(!siblings.isEmpty()) {
			getLogger().trace("  siblings: {}", siblings);
		}
		final Collection<Artifact> children = context.getPlan().childArtifacts(artifact).toList();
		if(!children.isEmpty()) {
			getLogger().trace("  children: {}", children);
		}

		if(artifact instanceof CollectionArtifact collectionArtifact) {
			for(final Artifact childArtifact : collectionArtifact.getChildArtifacts()) {
				printArtifactDescription(context, childArtifact);
			}
		}
	}

	//project configuration

	/// The base filename for the project configuration.
	public static final String PROJECT_CONFIG_BASE_FILENAME = "guise-project";

	/// The default relative path of the project source directory. Analogous to Maven's `${project.basedir}/src` property.
	public final static Path DEFAULT_PROJECT_SOURCE_RELATIVE_DIR = Paths.get("src");

	/// The default relative path of the site source directory. Similar to the default value of Maven's `siteDirectory` property, which is
	/// `${project.basedir}/src/site`.
	/// @see <a href="https://maven.apache.org/plugins/maven-site-plugin/site-mojo.html#siteDirectory">Apache Maven Site Plugin: site:site
	///      &lt;siteDirectory&gt;</a>.
	public final static Path DEFAULT_PROJECT_SITE_SOURCE_RELATIVE_DIR = DEFAULT_PROJECT_SOURCE_RELATIVE_DIR.resolve("site");

	/// The default relative path of the build directory. Analogous to the default value of Maven's `project.build.directory` property, which is
	/// `${project.basedir}/target`.
	public final static Path DEFAULT_PROJECT_BUILD_RELATIVE_DIR = Paths.get("target");

	/// The default relative path of the site target directory. Similar to the default value of Maven's `generatedSiteDirectory` property, which is
	/// `${project.build.directory}/generated-site`.
	/// @see <a href="https://maven.apache.org/plugins/maven-site-plugin/site-mojo.html#generatedSiteDirectory">Apache Maven Site Plugin: site:site
	///      &lt;generatedSiteDirectory&gt;</a>.
	/// @see #DEFAULT_PROJECT_SITE_SOURCE_RELATIVE_DIR
	public final static Path DEFAULT_PROJECT_SITE_TARGET_RELATIVE_DIR = DEFAULT_PROJECT_BUILD_RELATIVE_DIR.resolve("site");

	/// The default relative path of the site description target directory.
	/// @see #DEFAULT_PROJECT_SITE_TARGET_RELATIVE_DIR
	public final static Path DEFAULT_PROJECT_SITE_DESCRIPTION_TARGET_RELATIVE_DIR = DEFAULT_PROJECT_BUILD_RELATIVE_DIR.resolve("site-description");

	/// The configuration key for the site source directory.
	public static final String PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY = "siteSourceDirectory";
	/// The configuration key for the site target directory.
	public static final String PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY = "siteTargetDirectory";
	/// The configuration key for the site description target directory.
	public static final String PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY = "siteDescriptionTargetDirectory";

	/// Creates a Guise Mummy project based upon a project directory and optional explicit directory overrides.
	/// @apiNote Because the paths other than the project directory may be relative, as may those in the configuration files, when retrieving paths they must be
	///          resolved against the project directory.
	/// @implSpec This implementation ultimately falls back to the default configuration returned by [#getDefaultConfiguration(Path)].
	/// @param projectDirectory The required absolute base directory for the project, in which the project file, if any, would be found.
	/// @param siteSourceDirectory The site source directory, or if `null` falling back to that specified in the project configuration as
	///          `siteSourceDirectory`, defaulting to `src/site` relative to the project directory.
	/// @param siteTargetDirectory The site target directory, or if `null` falling back to that specified in the project configuration as
	///          `siteTargetDirectory`, defaulting to `target/site` relative to the project directory.
	/// @param siteDescriptionTargetDirectory The site description target directory, or if `null` falling back to that specified in the project
	///          configuration as `siteDescriptionTargetDirectory`, defaulting to `target/site-description` relative to
	///          the project directory.
	/// @return The new project.
	/// @throws IllegalArgumentException if one of the given directories is not absolute.
	/// @throws IOException if there is an error determining or loading the project.
	public static GuiseProject createProject(@NonNull Path projectDirectory, @Nullable final Path siteSourceDirectory, @Nullable final Path siteTargetDirectory,
			@Nullable final Path siteDescriptionTargetDirectory) throws IOException {
		projectDirectory = deriveRealPath(checkArgumentAbsolute(projectDirectory), NOFOLLOW_LINKS);

		//default settings (ultimate fallback)
		final Configuration defaultConfiguration = getDefaultConfiguration(projectDirectory);

		//configuration file settings (`guise-project.*`); fall back to the default; if no config file present, just use the default
		final Configuration fileConfiguration = FileSystemConfigurationManager.loadConfigurationForBaseFilename(projectDirectory, PROJECT_CONFIG_BASE_FILENAME)
				.map(projectFileConfiguration -> projectFileConfiguration.withFallback(defaultConfiguration)).orElse(defaultConfiguration);

		//user settings (e.g. supplied on the command line); fall back to the file/default
		final Map<String, Object> userSettings = new HashMap<>();
		if(siteSourceDirectory != null) {
			userSettings.put(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY, deriveRealPath(checkArgumentAbsolute(siteSourceDirectory), NOFOLLOW_LINKS));
		}
		if(siteTargetDirectory != null) {
			userSettings.put(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY, deriveRealPath(checkArgumentAbsolute(siteTargetDirectory), NOFOLLOW_LINKS));
		}
		if(siteDescriptionTargetDirectory != null) {
			userSettings.put(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY, deriveRealPath(checkArgumentAbsolute(siteDescriptionTargetDirectory), NOFOLLOW_LINKS));
		}
		final Configuration projectConfiguration = new ObjectMapConfiguration(unmodifiableMap(userSettings)).withFallback(fileConfiguration); //the user settings override even the project file
		return new DefaultGuiseProject(projectDirectory, projectConfiguration);
	}

	/// Returns the default fallback configuration values for Guise Mummy. The configuration may not be mutable.
	/// @param projectDirectory The required absolute base directory for the project.
	/// @return The default Guise Mummy settings configuration.
	/// @throws IllegalArgumentException if the project directory is not absolute.
	public static Configuration getDefaultConfiguration(@NonNull Path projectDirectory) {
		projectDirectory = checkArgumentAbsolute(projectDirectory).normalize();
		final Map<String, Object> defaultSettings = new HashMap<>();
		defaultSettings.put(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY, projectDirectory.resolve(DEFAULT_PROJECT_SITE_SOURCE_RELATIVE_DIR)); //siteDirectory=${project.basedir}/src/site
		defaultSettings.put(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY, projectDirectory.resolve(DEFAULT_PROJECT_SITE_TARGET_RELATIVE_DIR)); //siteTargetDirectory=${project.basedir}/target/site
		defaultSettings.put(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY, projectDirectory.resolve(DEFAULT_PROJECT_SITE_DESCRIPTION_TARGET_RELATIVE_DIR)); //siteDescriptionTargetDirectory=${project.basedir}/target/site-description
		defaultSettings.put(CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN, Pattern.compile("\\$(.*)"));
		defaultSettings.put(CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES, List.of("index"));
		defaultSettings.put(CONFIG_KEY_MUMMY_NAVIGATION_BASE_NAME, ".navigation");
		defaultSettings.put(CONFIG_KEY_MUMMY_TEMPLATE_BASE_NAME, ".template");
		defaultSettings.put(CONFIG_KEY_MUMMY_TEXT_OUTPUT_LINE_SEPARATOR, "\n");
		defaultSettings.put(CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN, Pattern.compile("_(.*)"));
		//TODO create facility for mummifiers (and later plugins) to contribute default settings
		defaultSettings.put(ImageMummifier.CONFIG_KEY_FORMAT_MUMMY_IMAGE_ASPECT___SCALE_MAX_LENGTH.formatted("preview"), 600);
		defaultSettings.put(ImageMummifier.CONFIG_KEY_FORMAT_MUMMY_IMAGE_ASPECT___COMPRESSION_QUALITY.formatted("preview"), 0.6);
		defaultSettings.put(ImageMummifier.CONFIG_KEY_FORMAT_MUMMY_IMAGE_ASPECT___SCALE_MAX_LENGTH.formatted("thumbnail"), 300);
		defaultSettings.put(ImageMummifier.CONFIG_KEY_FORMAT_MUMMY_IMAGE_ASPECT___COMPRESSION_QUALITY.formatted("thumbnail"), 0.6);
		return new ObjectMapConfiguration(unmodifiableMap(defaultSettings));
	}

	/// Mutable mummification context controlled by Guise Mummy itself.
	/// @author Garret Wilson
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

		private MummyPlan plan = null;

		@Override
		public MummyPlan getPlan() {
			checkState(plan != null, "Cannot retrieve plan before site has not yet been planned.");
			return plan;
		}

		/// Sets the site plan.
		/// @param plan The plan for the site.
		protected void setPlan(@NonNull final MummyPlan plan) {
			this.plan = requireNonNull(plan);
		}

		//## deploy

		private Dns deployDns = null;

		/// Sets the DNS configured for deployment.
		/// @param deployDns The DNS to use for deployment.
		protected void setDeployDns(@NonNull final Dns deployDns) {
			this.deployDns = requireNonNull(deployDns);
		}

		@Override
		public Optional<Dns> getDeployDns() {
			return Optional.ofNullable(deployDns);
		}

		private List<DeployTarget> deployTargets = null;

		/// Sets the targets configured for deployment.
		/// @param deployTargets The deployment targets.
		protected void setDeployTargets(@NonNull final List<DeployTarget> deployTargets) {
			this.deployTargets = requireNonNull(deployTargets);
		}

		@Override
		public Optional<List<DeployTarget>> getDeployTargets() {
			return Optional.ofNullable(deployTargets);
		}

		/// Context constructor.
		/// @param project The Guise project.
		/// @param siteConfiguration The configuration for the site.
		/// @param siteSourceDirectory The base directory of the site source, in real-path form.
		/// @param siteTargetDirectory The output directory of the site, in real-path form.
		/// @param siteDescriptionTargetDirectory The output directory of the site description, in real-path form.
		/// @throws IllegalArgumentException if any directory path is not in real-path form.
		/// @throws IOException if an I/O error occurs during real-path validation.
		public Context(@NonNull final GuiseProject project, @NonNull final Configuration siteConfiguration, @NonNull final Path siteSourceDirectory,
				@NonNull final Path siteTargetDirectory, @NonNull final Path siteDescriptionTargetDirectory) throws IOException {
			super(project, siteSourceDirectory, siteTargetDirectory, siteDescriptionTargetDirectory);
			this.siteConfiguration = requireNonNull(siteConfiguration);
		}

	}

}
