package gms.shared.mechanisms.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.configuration.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements {@link ConfigurationRepository} by constructing {@link Configuration} from {@link
 * ConfigurationOption} files stored in files.
 *
 * Assumes the following structure: 1. All data is rooted in a single configuration root directory.
 * This directory is provided to {@link FileConfigurationRepository#create(Path)}
 *
 * 2. The configuration root directory has a collection of subdirectories but no files.  Each
 * subdirectory corresponds to a Configuration; the subdirectory name becomes the Configuration's
 * name.
 *
 * 3. Each subdirectory contains .yaml files but no nested subdirectories.  Each .yaml file contains
 * a single serialized ConfigurationOption.
 *
 * Only implements the {@link ConfigurationRepository#getKeyRange(String)} operation.
 */
public class FileConfigurationRepository implements ConfigurationRepository {

  private static final Logger logger = LoggerFactory.getLogger(FileConfigurationRepository.class);

  private final Map<String, Configuration> configurationByName;

  private FileConfigurationRepository(Map<String, Configuration> configurationByName) {
    this.configurationByName = configurationByName;
  }

  /**
   * Obtain a {@link FileConfigurationRepository} with the provided configurationRoot directory.
   * configurationRoot can be on an actual file system or packaged in a Jar (e.g. as occurs when the
   * files were originally in a src/resources directory).  If it is in a resources directory
   * configurationRoot must include the Jar's path.  If it is on an actual filesystem provide a path
   * to the configurationRoot directory.
   *
   * @param configurationRoot {@link Path} configuration root directory, not null
   * @return {@link FileConfigurationRepository}, not null
   * @throws NullPointerException if configurationRoot is null
   */
  public static FileConfigurationRepository create(Path configurationRoot) {
    Objects.requireNonNull(configurationRoot, "configurationRoot can't be null");

    return new FileConfigurationRepository(
        loadConfigurations(configurationRoot).stream()
            .collect(Collectors.toMap(Configuration::getName, Function.identity())));
  }

  @Override
  public Collection<Configuration> getKeyRange(String keyPrefix) {
    return configurationByName.entrySet().stream()
        .filter(e -> e.getKey().startsWith(keyPrefix))
        .map(Entry::getValue)
        .collect(Collectors.toList());
  }

  /**
   * Loads {@link Configuration}s from a base directory.  Constructs a Configuration for each
   * subdirectory in the baseDirectory.
   *
   * @param baseDirectory base directory containing the configurations
   * @return List of {@link Configuration} loaded from the baseDirectory
   */
  @SuppressWarnings("unchecked")
  private static List<Configuration> loadConfigurations(Path baseDirectory) {

    // Construct the correct type of DirectoryOperations for the provided path.  Wrap
    // the DirectoryOperations in a LoggingDirectoryOperations which logs which directories and
    // files get processed.
    final boolean isResources = baseDirectory.toString().contains("file:") &&
        baseDirectory.toString().contains("!");

    final DirectoryOperations directoryOperations = new LoggingDirectoryOperations(
        isResources ? new ResourcesDirectoryOperations() : new FilesystemDirectoryOperations());

    // YAML ObjectMapper
    final ObjectMapper objectMapper = CoiObjectMapperFactory.getYamlObjectMapper();

    List<Configuration> configurations = new ArrayList<>();

    try {
      // subDirectories become Configurations
      Collection<String> subDirectories = directoryOperations
          .getSubDirectories(baseDirectory.toFile());

      // Files in each subdirectory are the ConfigurationOptions
      for (String subDir : subDirectories) {
        logger.info("Loading configuration from subdirectory {}", subDir);

        final List<ConfigurationOption> configOptions = new ArrayList<>();
        for (String filename : directoryOperations.getFilesInDirectory(subDir)) {
          logger.info("Loading configuration from file {}", filename);

          configOptions.add(ObjectSerialization.fromFieldMap(
              objectMapper.readValue(directoryOperations.getUrl(filename), Map.class),
              ConfigurationOption.class));
        }

        // Construct the Configuration from all of the loaded ConfigurationOptions
        // TODO: is it correct that configuration name is between the last two File.separators?
        final String[] pathComponents = subDir.split(File.separator);
        final String configurationName = pathComponents[pathComponents.length - 1];
        configurations.add(Configuration.from(configurationName, configOptions));
      }
    } catch (IOException e) {
      logger.error("Could not load configuration from disk", e);
      throw new IllegalStateException("Could not load configuration", e);
    }

    logger.info("configurations: {}", configurations);

    return configurations;
  }

  /**
   * Defines the operations used when loading Configurations.  Different implementations load
   * Configurations from the filesystem or from a Jar file.
   */
  private interface DirectoryOperations {

    Collection<String> getSubDirectories(File configDirectory);

    Collection<String> getFilesInDirectory(String path);

    URL getUrl(String path);
  }

  /**
   * Implements {@link DirectoryOperations} by logging input parameters and operation results of
   * invoking a delegate {@link DirectoryOperations} implementation.
   */
  private static class LoggingDirectoryOperations implements DirectoryOperations {

    private final DirectoryOperations delegate;

    private LoggingDirectoryOperations(DirectoryOperations delegate) {
      this.delegate = delegate;
    }

    @Override
    public Collection<String> getSubDirectories(File configDirectory) {
      logger.info("Finding subdirectories of {}", configDirectory);

      final Collection<String> subdirectories = delegate.getSubDirectories(configDirectory);

      if (logger.isInfoEnabled()) {
        logger.info("Found subdirectories {}", Arrays.toString(subdirectories.toArray()));
      }

      return subdirectories;
    }

    @Override
    public Collection<String> getFilesInDirectory(String path) {
      logger.info("Loading files from directory {}", path);

      final Collection<String> files = delegate.getFilesInDirectory(path);

      if (logger.isInfoEnabled()) {
        logger.info("Found files to load {}", Arrays.toString(files.toArray()));
      }

      return files;
    }

    @Override
    public URL getUrl(String path) {
      logger.info("Getting URL for path {}", path);

      final URL url = delegate.getUrl(path);
      logger.info("URL is {} ", url);

      return url;
    }
  }

  /**
   * Implements {@link DirectoryOperations} using filesystem operations
   */
  private static class FilesystemDirectoryOperations implements DirectoryOperations {

    @Override
    public Collection<String> getSubDirectories(File configDirectory) {
      return Optional
          .ofNullable(
              configDirectory.list((current, name) -> new File(current, name).isDirectory()))
          .stream()
          .flatMap(dirs -> Arrays.stream(dirs).map(d -> configDirectory + File.separator + d))
          .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getFilesInDirectory(String path) {
      return Optional.ofNullable(new File(path).list())
          .map(listing ->
              Arrays.stream(listing)
                  .map(f -> path + File.separator + f)
                  .collect(Collectors.toList())
          ).orElse(List.of());
    }

    @Override
    public URL getUrl(String path) {
      try {
        return new File(path).toURI().toURL();
      } catch (MalformedURLException e) {
        final String message = "Could not create URL to file at path: " + path;
        throw new IllegalStateException(message, e);
      }
    }
  }

  /**
   * Implements {@link DirectoryOperations} for files in a jar file.
   */
  private static class ResourcesDirectoryOperations implements DirectoryOperations {

    @Override
    public Collection<String> getSubDirectories(File configDirectory) {
      final String path = relativeToJar(configDirectory.getAbsolutePath());
      logger.info("Loading all resources in directory {}", path);

      URL resource = getClassLoader().getResource(path);
      logger.info("Found resource for directory {}", resource);

      return list(path).stream()
          .filter(s -> s.endsWith(File.separator))
          .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getFilesInDirectory(String path) {
      return list(path).stream()
          .filter(s -> !s.endsWith(File.separator))
          .collect(Collectors.toList());
    }

    @Override
    public URL getUrl(String path) {
      logger.info("Loading file from resources path {}", path);
      return getResourceUrl(path);
    }

    /**
     * Find all of the children of the provided directory within a Jar file.  The provided
     * directoryPath and the returned paths are relative to the jar file.
     *
     * @param directoryPath path to a directory, relative to the jar file, not null
     * @return list of paths to children of the provided directoryPath, all paths are relative to
     * the jar file, not null
     */
    private List<String> list(String directoryPath) {

      // If necessary, append File.separator to end of directoryPath
      final String rootPath =
          directoryPath + (directoryPath.endsWith(File.separator) ? "" : File.separator);

      // True if the string is a direct child of the rootPath
      final Predicate<String> isChild = s -> s.startsWith(rootPath)
          && !rootPath.equals(s)
          && (!s.substring(rootPath.length(), s.length() - 1).contains(File.separator));

      // Find direct children of the root path
      return Optional.ofNullable(getResourceUrl(directoryPath))
          .map(URL::getPath)
          .map(p -> p.substring("file:".length(), p.indexOf('!')))
          .map(jarPath -> filterJarEntries(jarPath, isChild))
          .orElse(List.of());
    }

    /**
     * Finds names of all entries in the Jar file at the provided jarPath which satisfy the filter
     *
     * @param jarPath path to a jarFile, not null
     * @param filter {@link Predicate} deciding which jar entries are listed
     * @return List of String paths to jar entries passing the filter, not null
     */
    private static List<String> filterJarEntries(String jarPath, Predicate<String> filter) {
      List<String> filenames = new ArrayList<>();

      try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name()))) {
        jar.entries().asIterator().forEachRemaining(entry -> {
              final String entryName = entry.getName();
              if (filter.test(entryName)) {
                Optional.ofNullable(getResourceUrl(entryName))
                    .map(URL::toString)
                    .map(ResourcesDirectoryOperations::relativeToJar)
                    .ifPresent(filenames::add);
              }
            }
        );

      } catch (UnsupportedEncodingException e) {
        logger.error("Could not decode URL, ", e);
      } catch (IOException e) {
        logger.error("Could not access jar, ", e);
      }

      return filenames;
    }

    private static URL getResourceUrl(String path) {
      return getClassLoader().getResource(path);
    }

    private static ClassLoader getClassLoader() {
      return Thread.currentThread().getContextClassLoader();
    }

    private static String relativeToJar(String fullPath) {
      return fullPath.substring(fullPath.indexOf('!') + 2);
    }
  }
}
