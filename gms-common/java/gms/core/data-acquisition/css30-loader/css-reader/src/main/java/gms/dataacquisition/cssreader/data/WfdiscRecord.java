package gms.dataacquisition.cssreader.data;

import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the 'wfdisc' table in CSS. Uses the FFPojo library annotations to make this class
 * parsable from a flat file representation. Transforms some fields from their CSS representation to
 * something more useful, e.g. double that represents epoch seconds becomes a Java Instant. This
 * class is extend by classes implementing 32-bit and 64-bit IDs.  This class only contains getters
 * to support lambda filtering, and the setters are available in the 32-bit and 64-bit extended
 * classes. Created by trsault on 8/22/17.
 */
public abstract class WfdiscRecord {

  private static final Logger logger = LoggerFactory.getLogger(WfdiscRecord.class);

  protected String sta;
  protected String chan;
  protected Instant time;
  protected long wfid;
  protected long chanid;
  protected int jdate;
  protected Instant endtime;
  protected int nsamp;
  protected double samprate;
  protected double calib;
  protected double calper;
  protected String instype;
  protected String segtype;
  protected String datatype;
  protected boolean clip;
  protected String dir;
  protected String dfile;
  protected int foff;
  protected int commid;
  protected String lddate;

  public String getSta() {
    return this.sta;
  }

  public String getChan() {
    return this.chan;
  }

  public Instant getTime() {
    return this.time;
  }

  public long getWfid() {
    return wfid;
  }

  public long getChanid() {
    return chanid;
  }

  public int getJdate() {
    return jdate;
  }

  public Instant getEndtime() {
    return endtime;
  }

  public int getNsamp() {
    return nsamp;
  }

  public double getSamprate() {
    return samprate;
  }

  public double getCalib() {
    return calib;
  }

  public double getCalper() {
    return calper;
  }

  public String getInstype() {
    return instype;
  }

  public String getSegtype() {
    return segtype;
  }

  public String getDatatype() {
    return datatype;
  }

  public boolean getClip() {
    return clip;
  }

  public String getDir() {
    return dir;
  }

  public String getDfile() {
    return dfile;
  }

  // foff m8
  public int getFoff() {
    return foff;
  }

  public int getCommid() {
    return commid;
  }

  public String getLddate() {
    return lddate;
  }

  /**
   * Formats all fields into an easily readable string for logging and debugging.
   */
  @Override
  public String toString() {
    return "WfdiscRecord{" +
        "sta='" + sta + '\'' +
        ", chan='" + chan + '\'' +
        ", time=" + time +
        ", wfid=" + wfid +
        ", chanid=" + chanid +
        ", jdate=" + jdate +
        ", endtime=" + endtime +
        ", nsamp=" + nsamp +
        ", samprate=" + samprate +
        ", calib=" + calib +
        ", calper=" + calper +
        ", instype='" + instype + '\'' +
        ", segtype='" + segtype + '\'' +
        ", datatype='" + datatype + '\'' +
        ", clip='" + clip + '\'' +
        ", dir='" + dir + '\'' +
        ", dfile='" + dfile + '\'' +
        ", foff=" + foff +
        ", commid=" + commid +
        ", lddate='" + lddate + '\'' +
        '}';
  }

  /**
     * Perform validation on the CSS wfdisc row and exceptions will be thrown if invalid (such as
     * NullPointerException and IllegalArgumentException).
     */
    public void validate() {
        Validate.notEmpty(getSta(), "STA field is empty");
        Validate.notEmpty(getChan(), "CHAN field is empty");
        Validate.inclusiveBetween(1900001, 2500001, getJdate(), "Invalid JDATE value - must be between 1900 and 2500");
        Validate.isTrue(getNsamp() >= 0);
        Validate.isTrue(getSamprate() >= 0, "Invalid SAMPRATE value - must be >= 0");
        Validate.isTrue(getCalib() >= 0, "Invalid CALIB value - must be >= 0");
        Validate.isTrue(getCalper() >= 0, "Invalid CALPER value - must be >= 0");
        Validate.notEmpty(getInstype(), "INSTYPE field is empty");
        Validate.notEmpty(getSegtype(), "SEGTYPE field is empty.");
        Validate.notEmpty(getDatatype(), "DATATYPE field is empty.");
        Validate.notEmpty(getDfile(), "DFILE field is empty");
        Validate.notEmpty(getDir(), "DIR field is empty");
        Validate.isTrue(getFoff() >= 0, "Invalid FOFF value - must be >= 0");
        Validate.notEmpty(getLddate(), "LDDATE field is empty");

    if (getTime().getEpochSecond() > getEndtime().getEpochSecond()) {
      throw new IllegalArgumentException("TIME is greater than ENDTIME");
    }
  }
}
