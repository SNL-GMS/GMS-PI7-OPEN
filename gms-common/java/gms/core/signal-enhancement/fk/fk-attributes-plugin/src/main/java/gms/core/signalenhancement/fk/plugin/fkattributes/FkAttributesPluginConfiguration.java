package gms.core.signalenhancement.fk.plugin.fkattributes;

public class FkAttributesPluginConfiguration {

  private double DEFAULT_ERR_DB_DROP = 2.0;
  private double DEFAULT_FKQUAL_DB_DROP = 6.0;
  private double DEFAULT_DB_DOWN_RADIUS = 0.4;

  public FkAttributesPluginConfiguration() {
  }

  public FkAttributesPluginParameters createParameters() {
    return new FkAttributesPluginParameters(DEFAULT_ERR_DB_DROP, DEFAULT_FKQUAL_DB_DROP, DEFAULT_DB_DOWN_RADIUS);
  }
}
