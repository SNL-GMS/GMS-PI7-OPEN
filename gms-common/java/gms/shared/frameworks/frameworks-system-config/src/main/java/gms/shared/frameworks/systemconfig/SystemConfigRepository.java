package gms.shared.frameworks.systemconfig;

import static gms.shared.frameworks.systemconfig.SystemConfigConstants.SEPARATOR;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A SystemConfigRepository is a key-value store used for looking up system configuration values
 * based on key names.
 */
public interface SystemConfigRepository {

  /**
   * Repository-specific implementation of a value lookup for the given key.
   *
   * @param key key name tof return the value for from this repository
   * @return value of key if present, empty Optional if not found
   */
  Optional<String> get(String key);

  /**
   * Get a key from the repository, searching through the prefixed names to attempt to find
   * a match. The prefix will be prepended to the key name and if the value of the prefixed
   * key will be preferred over value of the non-prefix key name if present.
   *
   * @param key key name to return the value for
   * @param prefix search prefix to prepend to the key name 
   * @return value of key if present, empty Optional if not found.
   */
  default Optional<String> search(String key, String prefix) {
    for (String k : expandSearchKeys(key, prefix)) {
      Optional<String> value = get(k);
      if (value.isPresent()) {
        return value;
      }
    }
    return Optional.empty();
  }

  /**
   * Given a key, return an expanded list of search keys to look for given our internal prefix.
   *
   * <p>The prefix is typically set to the control name of a component.
   *
   * <p>The value of a key prefixed with the control name is preferred (if present) over the value
   * of a bare, non-prefixed key. For example, if the specified key is 'port' and our controlName is
   * 'spacemodulator', this will return ['spacemodulator.port', 'port'].
   *
   * <p>If the given key <i>already</i> includes a control name, then ignore our internal control
   * name and just decompose the given key into a list of search keys. For example, if the specified
   * key is 'timeinhibitor.port' and our control name is 'spacemodulator', this will return
   * ['timeinhibitor.port', 'port']. For this case, our internal control name is irrelevant since
   * they key already includes the control name the caller wants..
   *
   * <p>If the control name contains multiple terms separated by dots, it will return a list of
   * expanded keys from most-specific to least-specific. For example,
   * 'timeinhibitor.inverter.timeout' would expand to [ 'timeinhibitor.inverter.timeout',
   * 'timeinhibitor.timeout', 'timeout' ]
   *
   * @param key name of the key to expand into a list of search keys
   */
  private List<String> expandSearchKeys(String key, String prefix) {
    ArrayList<String> searchKeys = new ArrayList<>();

    String workingPrefix = prefix;
    String baseKey = key;

    /*
     * If the key already has a control name in it (as indicated by the presence of a '.'),
     * then decompose that into a control name and base key and ignore our internal control name.
     *
     * For example, if the key is "a.b.KEY", then we want to use "a.b" as our control name  and search
     * for the key "KEY". Our return value would be { 'a.b.KEY', 'a.KEY', 'KEY' }
     */
    if (key.contains(SEPARATOR)) {
      workingPrefix = key.substring(0, key.lastIndexOf(SEPARATOR));
      baseKey = key.substring(key.lastIndexOf(SEPARATOR) + 1); // +1 to skip past the SEPARATOR
    }

    /*
     * Build a series of keys based on the control name ending with our base key name.
     * So if our control name is "a.b" and our key is "KEY", then we want to return ["a.b.KEY", "a.KEY", "KEY" ]
     */
    if (null != workingPrefix) {
      searchKeys.add(workingPrefix + SEPARATOR + baseKey);
      // Keep chopping off the end of the workingPrefix after the last '.' until nothing is left
      while (workingPrefix.contains(SEPARATOR)) {
        workingPrefix = workingPrefix.substring(0, workingPrefix.lastIndexOf(
            SEPARATOR));
        searchKeys.add(workingPrefix + SEPARATOR + baseKey);
      }
    }
    searchKeys.add(baseKey);
    return searchKeys;
  }
}
