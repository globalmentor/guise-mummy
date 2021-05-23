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

package io.guise.cli;

import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.io.Files.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.java.OperatingSystem.*;
import static com.globalmentor.net.HTTP.*;
import static com.globalmentor.net.URIs.*;
import static io.guise.mummy.GuiseMummy.*;
import static java.nio.file.Files.*;
import static java.util.Collections.*;
import static org.fusesource.jansi.Ansi.*;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import javax.annotation.*;

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.slf4j.event.Level;

import com.github.dtmo.jfiglet.*;
import com.globalmentor.application.*;
import com.globalmentor.net.Host;
import com.globalmentor.net.URIs;

import io.confound.config.Configuration;
import io.confound.config.ConfigurationException;
import io.confound.config.file.ResourcesConfigurationManager;
import io.guise.catalina.webresources.SiteRoot;
import io.guise.mummy.*;
import io.guise.mummy.mummify.Mummifier;
import io.guise.mummy.mummify.page.PageMummifier;
import picocli.CommandLine.*;

/**
 * Command-line interface for Guise tasks.
 * @author Garret Wilson
 */
@Command(name = "guise", description = "Command-line interface for Guise tasks.")
public class GuiseCli extends BaseCliApplication {

	public static final String CONFIG_KEY_SERVER_DIRECTORY = "server.directory";
	public static final String CONFIG_KEY_SERVER_PORT = "server.port";

	/** The default server port used by the <code>serve</code> command. */
	private final static int DEFAULT_SERVER_PORT = 4040;

	/**
	 * Constructor.
	 * @param args The command line arguments.
	 */
	public GuiseCli(@Nonnull final String[] args) {
		super(args, Level.INFO);
		//bridge JUL to SLF4J for Tomcat logging
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	/**
	 * Main program entry method.
	 * @param args Program arguments.
	 */
	public static void main(@Nonnull final String[] args) {
		Application.start(new GuiseCli(args));
	}

	/**
	 * Prints startup app information, including application banner, name, and version.
	 * @throws ConfigurationException if some configuration information isn't present.
	 */
	protected void printAppInfo() {
		final FigletRenderer figletRenderer;
		final Configuration appConfiguration;
		try {
			appConfiguration = ResourcesConfigurationManager.loadConfigurationForClass(getClass())
					.orElseThrow(ResourcesConfigurationManager::createConfigurationNotFoundException);
			figletRenderer = new FigletRenderer(FigFontResources.loadFigFontResource(FigFontResources.BIG_FLF));
		} catch(final IOException ioException) {
			throw new ConfigurationException(ioException);
		}
		System.out.print(ansi().bold().fg(Ansi.Color.GREEN));
		System.out.print(figletRenderer.renderText("Guise"));
		System.out.println(ansi().reset());
		System.out.println(appConfiguration.getString(CONFIG_KEY_NAME) + " " + appConfiguration.getString(CONFIG_KEY_VERSION));
		System.out.println();
	}

	/**
	 * Logs information about the current Guise project.
	 * @param project The Guise project.
	 * @see #getLogger()
	 * @see Level#INFO
	 */
	protected void logProjectInfo(@Nonnull final GuiseProject project) {
		final Logger logger = getLogger();
		final Configuration projectConfiguration = project.getConfiguration();
		logger.info("Project directory: {}", project.getDirectory());
		logger.info("Site source directory: {}", projectConfiguration.getPath(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY));
		logger.info("Site target directory: {}", projectConfiguration.getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY));
		logger.info("Site description target directory: {}", projectConfiguration.getPath(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY));
	}

	@Command(description = "Validates a Guise project before mummification.", subcommands = {HelpCommand.class})
	public void validate(
			@Parameters(paramLabel = "<project>", description = "The base directory of the project to mummify.%nDefaults to the working directory, currently @|bold ${DEFAULT-VALUE}|@.", defaultValue = "${sys:user.dir}", arity = "0..1") @Nullable Path argProjectDirectory,
			@Option(names = "--site-source-dir", description = "The source root directory of the site to mummify.%nDefaults to @|bold src/site/|@ relative to the project base directory.") @Nullable Path argSiteSourceDirectory,
			@Option(names = "--site-target-dir", description = "The target root directory into which the site will be generated; will be created if needed.%nDefaults to @|bold target/site/|@ relative to the project base directory.") @Nullable Path argSiteTargetDirectory,
			@Option(names = "--site-description-target-dir", description = "The target root directory into which the site description will be generated; will be created if needed.%nDefaults to @|bold target/site-description/|@ relative to the project base directory.") @Nullable Path argSiteDescriptionTargetDirectory)
			throws IOException {

		printAppInfo();

		final Path projectDirectory = argProjectDirectory != null ? argProjectDirectory : getWorkingDirectory();

		final GuiseMummy mummifier = new GuiseMummy();
		final GuiseProject project = GuiseMummy.createProject(projectDirectory.toAbsolutePath(), argSiteSourceDirectory, argSiteTargetDirectory,
				argSiteDescriptionTargetDirectory);

		System.out.println(ansi().bold().fg(Ansi.Color.BLUE).a("Validate...").reset());
		logProjectInfo(project);

		mummifier.mummify(project, GuiseMummy.LifeCyclePhase.VALIDATE);

		System.out.println(ansi().bold().fg(Ansi.Color.BLUE).a("Done.").reset());
	}

	@Command(description = "Cleans a site by removing the site target directory.", subcommands = {HelpCommand.class})
	public void clean(
			@Parameters(paramLabel = "<project>", description = "The base directory of the project to mummify.%nDefaults to the working directory, currently @|bold ${DEFAULT-VALUE}|@.", defaultValue = "${sys:user.dir}", arity = "0..1") @Nullable Path argProjectDirectory,
			@Option(names = "--site-target-dir", description = "The target root directory of the site to be removed; will be created if needed.%nDefaults to @|bold target/site/|@ relative to the project base directory.") @Nullable Path argSiteTargetDirectory,
			@Option(names = "--site-description-target-dir", description = "The target root directory of the site description to be removed; will be created if needed.%nDefaults to @|bold target/site-description/|@ relative to the project base directory.") @Nullable Path argSiteDescriptionTargetDirectory)
			throws IOException {

		printAppInfo();

		final Path projectDirectory = argProjectDirectory != null ? argProjectDirectory : getWorkingDirectory();

		final GuiseProject project = GuiseMummy.createProject(projectDirectory.toAbsolutePath(), null, argSiteTargetDirectory, argSiteDescriptionTargetDirectory);
		final Configuration projectConfiguration = project.getConfiguration();
		final Path siteTargetDirectory = projectConfiguration.getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY);
		final Path siteDescriptionTargetDirectory = projectConfiguration.getPath(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY);

		System.out.println(ansi().bold().fg(Ansi.Color.BLUE).a("Clean...").reset());
		logProjectInfo(project);

		if(exists(siteTargetDirectory)) {
			deleteFileTree(siteTargetDirectory);
		}
		if(!siteTargetDirectory.equals(siteDescriptionTargetDirectory)) {
			if(exists(siteDescriptionTargetDirectory)) {
				deleteFileTree(siteDescriptionTargetDirectory);
			}
		}

		System.out.println(ansi().bold().fg(Ansi.Color.BLUE).a("Done.").reset());
	}

	@Command(description = "Mummifies a site by generating a static version.", subcommands = {HelpCommand.class})
	public void mummify(
			@Parameters(paramLabel = "<project>", description = "The base directory of the project to mummify.%nDefaults to the working directory, currently @|bold ${DEFAULT-VALUE}|@.", defaultValue = "${sys:user.dir}", arity = "0..1") @Nullable Path argProjectDirectory,
			@Option(names = "--site-source-dir", description = "The source root directory of the site to mummify.%nDefaults to @|bold src/site/|@ relative to the project base directory.") @Nullable Path argSiteSourceDirectory,
			@Option(names = "--site-target-dir", description = "The target root directory into which the site will be generated; will be created if needed.%nDefaults to @|bold target/site/|@ relative to the project base directory.") @Nullable Path argSiteTargetDirectory,
			@Option(names = "--site-description-target-dir", description = "The target root directory into which the site description will be generated; will be created if needed.%nDefaults to @|bold target/site-description/|@ relative to the project base directory.") @Nullable Path argSiteDescriptionTargetDirectory,
			@Option(names = {"--full",
					"-f"}, description = "Specifies full instead of incremental mummification.%nCached artifacts will be regenerated.", defaultValue = "false") final boolean full)
			throws IOException {

		printAppInfo();

		final Path projectDirectory = argProjectDirectory != null ? argProjectDirectory : getWorkingDirectory();

		final GuiseMummy mummifier = new GuiseMummy();
		final GuiseProject project = GuiseMummy.createProject(projectDirectory.toAbsolutePath(), argSiteSourceDirectory, argSiteTargetDirectory,
				argSiteDescriptionTargetDirectory);
		mummifier.setFull(full);

		System.out.println(ansi().bold().fg(Ansi.Color.BLUE).a("Mummify...").reset());
		logProjectInfo(project);

		mummifier.mummify(project, GuiseMummy.LifeCyclePhase.MUMMIFY);

		System.out.println(ansi().bold().fg(Ansi.Color.BLUE).a("Done.").reset());
	}

	@Command(name = "prepare-deploy", description = "Prepares to deploys a site after generating a static version, but does not actually deploy the site.", subcommands = {
			HelpCommand.class})
	public void prepareDeploy(
			@Parameters(paramLabel = "<project>", description = "The base directory of the project to deploy.%nDefaults to the working directory, currently @|bold ${DEFAULT-VALUE}|@.", defaultValue = "${sys:user.dir}", arity = "0..1") @Nullable Path argProjectDirectory,
			@Option(names = "--site-source-dir", description = "The source root directory of the site to mummify.%nDefaults to @|bold src/site/|@ relative to the project base directory.") @Nullable Path argSiteSourceDirectory,
			@Option(names = "--site-target-dir", description = "The target root directory into which the site will be generated; will be created if needed.%nDefaults to @|bold target/site/|@ relative to the project base directory.") @Nullable Path argSiteTargetDirectory,
			@Option(names = "--site-description-target-dir", description = "The target root directory into which the site description will be generated; will be created if needed.%nDefaults to @|bold target/site-description/|@ relative to the project base directory.") @Nullable Path argSiteDescriptionTargetDirectory,
			@Option(names = {"--full",
					"-f"}, description = "Specifies full instead of incremental mummification.%nCached artifacts will be regenerated.", defaultValue = "false") final boolean full)
			throws IOException {

		printAppInfo();

		final Path projectDirectory = argProjectDirectory != null ? argProjectDirectory : getWorkingDirectory();

		final GuiseMummy mummifier = new GuiseMummy();
		final GuiseProject project = GuiseMummy.createProject(projectDirectory.toAbsolutePath(), argSiteSourceDirectory, argSiteTargetDirectory,
				argSiteDescriptionTargetDirectory);
		mummifier.setFull(full);

		System.out.println(ansi().bold().fg(Ansi.Color.BLUE).a("Prepare Deploy...").reset());
		logProjectInfo(project);

		mummifier.mummify(project, GuiseMummy.LifeCyclePhase.PREPARE_DEPLOY);

		System.out.println(ansi().bold().fg(Ansi.Color.BLUE).a("Done.").reset());
	}

	@Command(description = "Deploys a site after generating a static version.", subcommands = {HelpCommand.class})
	public void deploy(
			@Parameters(paramLabel = "<project>", description = "The base directory of the project to deploy.%nDefaults to the working directory, currently @|bold ${DEFAULT-VALUE}|@.", defaultValue = "${sys:user.dir}", arity = "0..1") @Nullable Path argProjectDirectory,
			@Option(names = "--site-source-dir", description = "The source root directory of the site to mummify.%nDefaults to @|bold src/site/|@ relative to the project base directory.") @Nullable Path argSiteSourceDirectory,
			@Option(names = "--site-target-dir", description = "The target root directory into which the site will be generated; will be created if needed.%nDefaults to @|bold target/site/|@ relative to the project base directory.") @Nullable Path argSiteTargetDirectory,
			@Option(names = "--site-description-target-dir", description = "The target root directory into which the site description will be generated; will be created if needed.%nDefaults to @|bold target/site-description/|@ relative to the project base directory.") @Nullable Path argSiteDescriptionTargetDirectory,
			@Option(names = {"--browse", "-b"}, description = "Opens a browser to the site after starting the server.") final boolean browse,
			@Option(names = {"--full",
					"-f"}, description = "Specifies full instead of incremental mummification and deployment.%nCached artifacts will be regenerated and all artifacts will be redeployed.", defaultValue = "false") final boolean full)
			throws IOException {

		printAppInfo();

		final Path projectDirectory = argProjectDirectory != null ? argProjectDirectory : getWorkingDirectory();

		final GuiseMummy mummifier = new GuiseMummy();
		final GuiseProject project = GuiseMummy.createProject(projectDirectory.toAbsolutePath(), argSiteSourceDirectory, argSiteTargetDirectory,
				argSiteDescriptionTargetDirectory);
		mummifier.setFull(full);

		System.out.println(ansi().bold().fg(Ansi.Color.BLUE).a("Deploy...").reset());
		logProjectInfo(project);

		mummifier.mummify(project, GuiseMummy.LifeCyclePhase.DEPLOY);

		System.out.println(ansi().bold().fg(Ansi.Color.BLUE).a("Done.").reset());

		//launch the browser using the last deploy URL; see https://stackoverflow.com/a/5226244/421049
		if(browse && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			final List<URI> deployUrls = mummifier.getDeployUrls();
			if(!deployUrls.isEmpty()) {
				final URI lastDeployUrl = deployUrls.get(deployUrls.size() - 1);
				Desktop.getDesktop().browse(lastDeployUrl);
			}
		}
	}

	/** The relative path of the server base directory; meant to be used in conjunction with the temporary directory. */
	private static final Path SERVER_RELATIVE_BASE_DIRECTORY = Paths.get("guise", "mummy", "server"); //TODO use constants

	@Command(description = "Starts a web server for exploring the site in the target directory.", subcommands = {HelpCommand.class})
	public void serve(
			@Parameters(paramLabel = "<project>", description = "The base directory of the project being served.%nDefaults to the working directory, currently @|bold ${DEFAULT-VALUE}|@.", defaultValue = "${sys:user.dir}", arity = "0..1") @Nullable Path argProjectDirectory,
			@Option(names = "--site-target-dir", description = "The target root directory of the site to be served.%nDefaults to @|bold target/site/|@ relative to the project base directory.") @Nullable Path argSiteTargetDirectory,
			@Option(names = "--site-description-target-dir", description = "The target root directory of the description of the site to be served.%nDefaults to @|bold target/site-description/|@ relative to the project base directory.") @Nullable Path argSiteDescriptionTargetDirectory,
			@Option(names = {"--port", "-p"}, description = "Specifies the server port.%nDefaults to @|bold ${DEFAULT-VALUE}|@.", defaultValue = ""
					+ DEFAULT_SERVER_PORT) Integer argPort,
			@Option(names = {"--browse", "-b"}, description = "Opens a browser to the site after starting the server.") final boolean browse)
			throws IOException, LifecycleException {

		printAppInfo();

		final Path projectDirectory = argProjectDirectory != null ? argProjectDirectory : getWorkingDirectory();

		final Path siteTargetDirectory;
		final Path siteDescriptionTargetDirectory;
		final Path serverBaseDirectory;
		final int port;
		final GuiseProject project = GuiseMummy.createProject(projectDirectory.toAbsolutePath(), null, argSiteTargetDirectory, argSiteDescriptionTargetDirectory);
		final Configuration projectConfiguration = project.getConfiguration();
		siteTargetDirectory = projectConfiguration.getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY);
		siteDescriptionTargetDirectory = projectConfiguration.getPath(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY);

		//As per the servlet specification, Tomcat requires a base directory, but currently puts nothing inside it except for a `work` directory.
		//This implementation therefore uses a common server directory location relative to the system temporary directory.
		//In the future if Guise takes advantage of more server functionality and the server needs to store things,
		//perhaps by default a subdirectory within the temp folder could be made to parallel that of the project.
		serverBaseDirectory = getTempDirectory().resolve(SERVER_RELATIVE_BASE_DIRECTORY);
		port = argPort != null ? argPort : projectConfiguration.findInt(CONFIG_KEY_SERVER_PORT).orElse(DEFAULT_SERVER_PORT);

		checkArgument(isDirectory(siteTargetDirectory), "Site target directory %s does not exist.", siteTargetDirectory); //TODO improve error handling; see https://github.com/remkop/picocli/issues/672

		System.out.println(ansi().bold().fg(Ansi.Color.BLUE).a("Serve...").reset());
		logProjectInfo(project);
		getLogger().info("Server base directory: {}", serverBaseDirectory);
		getLogger().info("Server port: {}", port);

		final Tomcat tomcat = new Tomcat();
		tomcat.setPort(port);
		//set the base directory to the build directory (possibly needed in the future for e.g. JSP temporary directory; used now as the base for the context temp directory)
		tomcat.setBaseDir(serverBaseDirectory.toAbsolutePath().toString()); //base directory must be set before creating a connector
		tomcat.getConnector(); //create a default connector; required; see https://stackoverflow.com/a/49011424/421049

		final Context context = tomcat.addContext("", siteTargetDirectory.toAbsolutePath().toString());
		final SiteRoot siteRoot = new SiteRoot(siteDescriptionTargetDirectory.toAbsolutePath().toString());
		siteRoot.setDescriptionFileSidecarExtension(Mummifier.DESCRIPTION_FILE_SIDECAR_EXTENSION);
		context.setResources(siteRoot);

		final Wrapper defaultServlet = context.createWrapper(); //TODO use constants below
		defaultServlet.setName("default");
		defaultServlet.setServletClass("org.apache.catalina.servlets.DefaultServlet");
		defaultServlet.addInitParameter("debug", isDebug() ? "11" : "0"); //TODO use constant; see https://tomcat.apache.org/tomcat-9.0-doc/default-servlet.html
		defaultServlet.addInitParameter("listings", Boolean.FALSE.toString()); //TODO use constant
		defaultServlet.setLoadOnStartup(1);

		context.addChild(defaultServlet);
		context.addServletMappingDecoded(ROOT_PATH, "default"); //TODO use constant
		//TODO later add JSP servlet mappings
		DEFAULT_MIME_TYPES_BY_FILENAME_EXTENSION.forEach((extension, mimeType) -> context.addMimeMapping(extension, mimeType));

		//set up the collection content filenames (i.e "welcome files") such as `index`/`index.html`
		final boolean isPageNameBare = projectConfiguration.findBoolean(PageMummifier.CONFIG_KEY_MUMMY_PAGE_NAMES_BARE).orElse(false);
		projectConfiguration.getCollection(CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES, String.class).stream()
				.map(baseName -> isPageNameBare ? baseName : addExtension(baseName, PageMummifier.PAGE_FILENAME_EXTENSION)) //e.g. "index" or "index.html"
				.forEach(context::addWelcomeFile);

		tomcat.start(); //start the server

		final URI siteLocalUrl = URIs.createURI(HTTP_URI_SCHEME, null, Host.LOCALHOST.getName(), port, ROOT_PATH, null, null);
		getLogger().info("Serving site at <{}>. (Press Ctrl+C to stop.)", siteLocalUrl);

		//launch the browser; see https://stackoverflow.com/a/5226244/421049
		if(browse && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			Desktop.getDesktop().browse(siteLocalUrl);
		}

		tomcat.getServer().await();
	}

	/**
	 * Default MIME mapping for the server.
	 * @implNote Taken from the {@link Tomcat} source code.
	 */
	private static final Map<String, String> DEFAULT_MIME_TYPES_BY_FILENAME_EXTENSION;

	static { //TODO update; switch to use Tomcat map when it becomes public, or transfer to some common resource file
		final Map<String, String> mimeTypesByFilenameExtension = new HashMap<>();
		final String[] mimeMappings = {"abs", "audio/x-mpeg", "ai", "application/postscript", "aif", "audio/x-aiff", "aifc", "audio/x-aiff", "aiff", "audio/x-aiff",
				"aim", "application/x-aim", "art", "image/x-jg", "asf", "video/x-ms-asf", "asx", "video/x-ms-asf", "au", "audio/basic", "avi", "video/x-msvideo", "avx",
				"video/x-rad-screenplay", "bcpio", "application/x-bcpio", "bin", "application/octet-stream", "bmp", "image/bmp", "body", "text/html", "cdf",
				"application/x-cdf", "cer", "application/pkix-cert", "class", "application/java", "cpio", "application/x-cpio", "csh", "application/x-csh", "css",
				"text/css", "dib", "image/bmp", "doc", "application/msword", "dtd", "application/xml-dtd", "dv", "video/x-dv", "dvi", "application/x-dvi", "eps",
				"application/postscript", "etx", "text/x-setext", "exe", "application/octet-stream", "gif", "image/gif", "gtar", "application/x-gtar", "gz",
				"application/x-gzip", "hdf", "application/x-hdf", "hqx", "application/mac-binhex40", "htc", "text/x-component", "htm", "text/html", "html", "text/html",
				"ief", "image/ief", "jad", "text/vnd.sun.j2me.app-descriptor", "jar", "application/java-archive", "java", "text/x-java-source", "jnlp",
				"application/x-java-jnlp-file", "jpe", "image/jpeg", "jpeg", "image/jpeg", "jpg", "image/jpeg", "js", "application/javascript", "jsf", "text/plain",
				"jspf", "text/plain", "kar", "audio/midi", "latex", "application/x-latex", "m3u", "audio/x-mpegurl", "mac", "image/x-macpaint", "man", "text/troff",
				"mathml", "application/mathml+xml", "me", "text/troff", "mid", "audio/midi", "midi", "audio/midi", "mif", "application/x-mif", "mov", "video/quicktime",
				"movie", "video/x-sgi-movie", "mp1", "audio/mpeg", "mp2", "audio/mpeg", "mp3", "audio/mpeg", "mp4", "video/mp4", "mpa", "audio/mpeg", "mpe",
				"video/mpeg", "mpeg", "video/mpeg", "mpega", "audio/x-mpeg", "mpg", "video/mpeg", "mpv2", "video/mpeg2", "nc", "application/x-netcdf", "oda",
				"application/oda", "odb", "application/vnd.oasis.opendocument.database", "odc", "application/vnd.oasis.opendocument.chart", "odf",
				"application/vnd.oasis.opendocument.formula", "odg", "application/vnd.oasis.opendocument.graphics", "odi", "application/vnd.oasis.opendocument.image",
				"odm", "application/vnd.oasis.opendocument.text-master", "odp", "application/vnd.oasis.opendocument.presentation", "ods",
				"application/vnd.oasis.opendocument.spreadsheet", "odt", "application/vnd.oasis.opendocument.text", "otg",
				"application/vnd.oasis.opendocument.graphics-template", "oth", "application/vnd.oasis.opendocument.text-web", "otp",
				"application/vnd.oasis.opendocument.presentation-template", "ots", "application/vnd.oasis.opendocument.spreadsheet-template ", "ott",
				"application/vnd.oasis.opendocument.text-template", "ogx", "application/ogg", "ogv", "video/ogg", "oga", "audio/ogg", "ogg", "audio/ogg", "spx",
				"audio/ogg", "flac", "audio/flac", "anx", "application/annodex", "axa", "audio/annodex", "axv", "video/annodex", "xspf", "application/xspf+xml", "pbm",
				"image/x-portable-bitmap", "pct", "image/pict", "pdf", "application/pdf", "pgm", "image/x-portable-graymap", "pic", "image/pict", "pict", "image/pict",
				"pls", "audio/x-scpls", "png", "image/png", "pnm", "image/x-portable-anymap", "pnt", "image/x-macpaint", "ppm", "image/x-portable-pixmap", "ppt",
				"application/vnd.ms-powerpoint", "pps", "application/vnd.ms-powerpoint", "ps", "application/postscript", "psd", "image/vnd.adobe.photoshop", "qt",
				"video/quicktime", "qti", "image/x-quicktime", "qtif", "image/x-quicktime", "ras", "image/x-cmu-raster", "rdf", "application/rdf+xml", "rgb",
				"image/x-rgb", "rm", "application/vnd.rn-realmedia", "roff", "text/troff", "rtf", "application/rtf", "rtx", "text/richtext", "sh", "application/x-sh",
				"shar", "application/x-shar",
				/*"shtml", "text/x-server-parsed-html",*/
				"sit", "application/x-stuffit", "snd", "audio/basic", "src", "application/x-wais-source", "sv4cpio", "application/x-sv4cpio", "sv4crc",
				"application/x-sv4crc", "svg", "image/svg+xml", "svgz", "image/svg+xml", "swf", "application/x-shockwave-flash", "t", "text/troff", "tar",
				"application/x-tar", "tcl", "application/x-tcl", "tex", "application/x-tex", "texi", "application/x-texinfo", "texinfo", "application/x-texinfo", "tif",
				"image/tiff", "tiff", "image/tiff", "tr", "text/troff", "tsv", "text/tab-separated-values", "txt", "text/plain", "ulw", "audio/basic", "ustar",
				"application/x-ustar", "vxml", "application/voicexml+xml", "xbm", "image/x-xbitmap", "xht", "application/xhtml+xml", "xhtml", "application/xhtml+xml",
				"xls", "application/vnd.ms-excel", "xml", "application/xml", "xpm", "image/x-xpixmap", "xsl", "application/xml", "xslt", "application/xslt+xml", "xul",
				"application/vnd.mozilla.xul+xml", "xwd", "image/x-xwindowdump", "vsd", "application/vnd.visio", "wav", "audio/x-wav", "wbmp", "image/vnd.wap.wbmp",
				"wml", "text/vnd.wap.wml", "wmlc", "application/vnd.wap.wmlc", "wmls", "text/vnd.wap.wmlsc", "wmlscriptc", "application/vnd.wap.wmlscriptc", "wmv",
				"video/x-ms-wmv", "wrl", "model/vrml", "wspolicy", "application/wspolicy+xml", "Z", "application/x-compress", "z", "application/x-compress", "zip",
				"application/zip"};
		for(int i = 0; i < mimeMappings.length;) {
			mimeTypesByFilenameExtension.put(mimeMappings[i++], mimeMappings[i++]);
		}
		DEFAULT_MIME_TYPES_BY_FILENAME_EXTENSION = unmodifiableMap(mimeTypesByFilenameExtension);
	}

}
