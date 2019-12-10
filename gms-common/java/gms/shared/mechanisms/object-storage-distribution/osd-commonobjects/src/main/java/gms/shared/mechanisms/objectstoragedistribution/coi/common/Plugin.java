package gms.shared.mechanisms.objectstoragedistribution.coi.common;

import java.util.Map;

public interface Plugin {

  String getName();

  PluginVersion getVersion();

  void initialize(Map<String, Object> parameterFieldMap);

}
