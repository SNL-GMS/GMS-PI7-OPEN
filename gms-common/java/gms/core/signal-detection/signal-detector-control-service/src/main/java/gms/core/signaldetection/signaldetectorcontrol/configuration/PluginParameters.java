package gms.core.signaldetection.signaldetectorcontrol.configuration;

import com.google.common.collect.ImmutableMap;

public interface PluginParameters {

  String getPluginName();

  ImmutableMap<String, Object> getPluginParameters();

}
