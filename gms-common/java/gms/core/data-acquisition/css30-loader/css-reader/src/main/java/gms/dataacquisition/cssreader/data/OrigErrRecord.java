package gms.dataacquisition.cssreader.data;

import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the 'OrigErr' table in CSS. Uses the FFPojo library annotations to make this class
 * parsable from a flat file representation.
 */
public class OrigErrRecord {

  private static final Logger logger = LoggerFactory.getLogger(OrigErrRecord.class);

  protected static int recordLength;

  protected int originId;
  protected double sxx;
  protected double syy;
  protected double szz;
  protected double stt;
  protected double sxy;
  protected double sxz;
  protected double syz;
  protected double stx;
  protected double sty;
  protected double stz;
  protected double sdobs;
  protected double smajax;
  protected double sminax;
  protected double strike;
  protected double sdepth;
  protected double stime;
  protected double conf;
  protected int commentId;
  protected Instant lddate;

  public static Logger getLogger() {
    return logger;
  }

  public static int getRecordLength() {
    return recordLength;
  }

  public static void setRecordLength(int recordLength) {
    OrigErrRecord.recordLength = recordLength;
  }

  public int getOriginId() {
    return originId;
  }

  public double getSxx() {
    return sxx;
  }

  public double getSyy() {
    return syy;
  }

  public double getSzz() {
    return szz;
  }

  public double getStt() {
    return stt;
  }

  public double getSxy() {
    return sxy;
  }

  public double getSxz() {
    return sxz;
  }

  public double getSyz() {
    return syz;
  }

  public double getStx() {
    return stx;
  }

  public double getSty() {
    return sty;
  }

  public double getStz() {
    return stz;
  }

  public double getSdobs() {
    return sdobs;
  }

  public double getSmajax() {
    return smajax;
  }

  public double getSminax() {
    return sminax;
  }

  public double getStrike() {
    return strike;
  }

  public double getSdepth() {
    return sdepth;
  }

  public double getStime() {
    return stime;
  }

  public double getConf() {
    return conf;
  }

  public int getCommentId() {
    return commentId;
  }

  public Instant getLddate() {
    return lddate;
  }

  @Override
  public String toString() {
    return "OrigErrRecord{" +
        "originId=" + originId +
        ", sxx=" + sxx +
        ", syy=" + syy +
        ", szz=" + szz +
        ", stt=" + stt +
        ", sxy=" + sxy +
        ", sxz=" + sxz +
        ", syz=" + syz +
        ", stx=" + stx +
        ", sty=" + sty +
        ", stz=" + stz +
        ", sdobs=" + sdobs +
        ", smajax=" + smajax +
        ", sminax=" + sminax +
        ", strike=" + strike +
        ", sdepth=" + sdepth +
        ", stime=" + stime +
        ", conf=" + conf +
        ", commentId=" + commentId +
        ", lddate='" + lddate + '\'' +
        '}';
  }

  public void validate() {
    Validate.notNaN(getOriginId(), "originId is NaN");
    Validate.notNaN(getSxx(), "sxx location uncertainty is NaN");
    Validate.notNaN(getSyy(), "syy location uncertainty is NaN");
    Validate.notNaN(getSzz(), "szz location uncertainty is NaN");
    Validate.notNaN(getStt(), "stt location uncertainty is NaN");
    Validate.notNaN(getSxy(), "sxy location uncertainty is NaN");
    Validate.notNaN(getSxz(), "sxz location uncertainty is NaN");
    Validate.notNaN(getSyz(), "syz location uncertainty is NaN");
    Validate.notNaN(getStx(), "stx location uncertainty is NaN");
    Validate.notNaN(getSty(), "sty location uncertainty is NaN");
    Validate.notNaN(getStz(), "stz location uncertainty is NaN");

    Validate.notNaN(getSdobs(), "sdobs std error of observations of location uncertainty is NaN");
    Validate.notNaN(getSmajax(), "smajax semi-major axis of ellipse error is NaN");
    Validate.notNaN(getSminax(), "sminax semi-minor axis of ellipse error is NaN");
    Validate.notNaN(getStrike(), "strike of the semi-major axis of ellipse is NaN");
    Validate.notNaN(getSdepth(), "sdepth ellipse depth uncertainty is NaN");
    Validate.notNull(getStime(), "stime ellipse origin time error is null");
    Validate.notNaN(getConf(), "conf ellipse confidence level is NaN");
    Validate.notNaN(getCommentId(), "commentId is NaN");
    Validate.notNull(getLddate(), "Load date is null");
  }
}
