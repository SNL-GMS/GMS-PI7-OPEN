package gms.shared.frameworks.systemconfig;

import java.util.List;

public class SystemConfigRepositoryDefaultFactory {

  private SystemConfigRepositoryDefaultFactory() {
  }

  public static List<SystemConfigRepository> create() {
    return List.of(
        FileSystemConfigRepository.builder().build(), EtcdSystemConfigRepository.builder().build());
  }
}
