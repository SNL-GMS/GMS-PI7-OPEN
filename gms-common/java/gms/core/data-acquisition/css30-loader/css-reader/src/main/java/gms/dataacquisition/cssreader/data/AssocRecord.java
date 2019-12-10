package gms.dataacquisition.cssreader.data;

import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the 'Assoc' table in CSS. Uses the FFPojo library annotations to make this class
 * parsable from a flat file representation.
 */
public class AssocRecord {

  private static final Logger logger = LoggerFactory.getLogger(AssocRecord.class);

  protected static int recordLength;

  protected int arrivalId;
  protected int originId;
  protected String stationName;
  protected String phase;
  protected double belief;
  protected double delta;
  protected double seaz;
  protected double esaz;
  protected double timeres;
  protected boolean timedef;
  protected double azres;
  protected boolean azdef;
  protected double slores;
  protected boolean slodef;
  protected double emares;
  protected double wgt;
  protected String vmodel;
  protected int commentId;
  protected Instant lddate;

  public static Logger getLogger() {
    return logger;
  }

  public static int getRecordLength() {
    return recordLength;
  }

  public static void setRecordLength(int recordLength) {
    AssocRecord.recordLength = recordLength;
  }

  public int getArrivalId() {
    return arrivalId;
  }

  public int getOriginId() {
    return originId;
  }

  public String getStationName() {
    return stationName;
  }

  public String getPhase() {
    return phase;
  }

  public double getBelief() {
    return belief;
  }

  public double getDelta() {
    return delta;
  }

  public double getSeaz() {
    return seaz;
  }

  public double getEsaz() {
    return esaz;
  }

  public double getTimeres() {
    return timeres;
  }

  public boolean getTimedef() {
    return timedef;
  }

  public double getAzres() {
    return azres;
  }

  public boolean getAzdef() {
    return azdef;
  }

  public double getSlores() {
    return slores;
  }

  public boolean getSlodef() {
    return slodef;
  }

  public double getEmares() {
    return emares;
  }

  public double getWgt() {
    return wgt;
  }

  public String getVmodel() {
    return vmodel;
  }

  public int getCommentId() {
    return commentId;
  }

  public Instant getLddate() {
    return lddate;
  }

  public boolean getIsDefining() {
    return (this.timedef || this.azdef || this.slodef);
  }


  @Override
  public String toString() {
    return "AssocRecord{" +
        "arrivalId=" + arrivalId +
        ", originId=" + originId +
        ", stationName='" + stationName + '\'' +
        ", phase='" + phase + '\'' +
        ", belief=" + belief +
        ", delta=" + delta +
        ", seaz=" + seaz +
        ", esaz=" + esaz +
        ", timeres=" + timeres +
        ", timedef='" + timedef + '\'' +
        ", azres=" + azres +
        ", azdef='" + azdef + '\'' +
        ", slores=" + slores +
        ", slodef='" + slodef + '\'' +
        ", emares=" + emares +
        ", wgt=" + wgt +
        ", vmodel='" + vmodel + '\'' +
        ", commentId=" + commentId +
        ", lddate=" + lddate +
        '}';
  }

  public void validate() {
    Validate.notNaN(getArrivalId(), "arrivalId is NaN");
    Validate.notNaN(getOriginId(), "originId is NaN");
    Validate.notNull(getStationName(), "stationName is null");
    Validate.notNull(getPhase(), "phase is null");
    Validate.notNaN(getBelief(), "belief, phase confidence, is NaN");
    Validate.notNaN(getDelta(), "delta, station to event distance, is NaN");
    Validate.notNaN(getSeaz(), "seaz, station to event azimuth, is NaN");
    Validate.notNaN(getEsaz(), "esaz, event to station azimuth, is NaN");
    Validate.notNaN(getTimeres(), "timeres, time residual, is NaN");
    Validate.notNull(getTimedef(), "timedef, is time defining, is null");
    Validate.notNaN(getAzres(), "azres, azimuth residual, is NaN");
    Validate.notNull(getAzdef(), "azdef, is azimuth defining, is null");
    Validate.notNaN(getSlores(), "slores, slowness residual, is NaN");
    Validate.notNull(getSlodef(), "slodef, is slowness defining, is null");
    Validate.notNaN(getEmares(), "emares, incidence angle residual, is NaN");
    Validate.notNaN(getWgt(), "wgt, location weight, is NaN");
    Validate.notNull(getVmodel(), "vmodel, velocity model, is Null");
    Validate.notNaN(getCommentId(), "commentId is NaN");
    Validate.notNull(getLddate(), "Load date is null");
  }
}
