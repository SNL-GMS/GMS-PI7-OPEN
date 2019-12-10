package gms.core.signalenhancement.fk.coi.client.config;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CoiClientConfiguration {

  public abstract String getPersistenceUrl();

  public static CoiClientConfiguration create(String persistenceUrl) {
    return new AutoValue_CoiClientConfiguration(persistenceUrl);
  }

}
