package gms.core.signalenhancement.fk.plugin.fkattributes;

public class FkAttributesPluginParameters {
  private final double errorDbDrop;
  private final double fkQualDbDrop;
  private final double dbDownRadius;

  public FkAttributesPluginParameters(double errorDbDrop, double fkQualDbDrop,
      double dbDownRadius) {
    this.errorDbDrop = errorDbDrop;
    this.fkQualDbDrop = fkQualDbDrop;
    this.dbDownRadius = dbDownRadius;
  }


  public double getErrorDbDrop() {
    return errorDbDrop;
  }

  public double getFkQualDbDrop() {
    return fkQualDbDrop;
  }

  public double getDbDownRadius() {
    return dbDownRadius;
  }

}
