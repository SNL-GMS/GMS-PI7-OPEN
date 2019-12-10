package gms.dataacquisition.cssreader.data;

import java.time.Duration;
import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AmplitudeRecord {

  private static final Logger logger = LoggerFactory.getLogger(ArrivalRecord.class);

  protected static int recordLength;

  protected int ampid;
  protected int arid;
  protected int parid;
  protected String chan;
  protected double amp;
  protected Duration per;
  protected double snr;
  protected Instant amptime;
  protected Instant time;
  protected double duration;
  protected double deltaf;
  protected String amptype;
  protected String units;
  protected String clip;
  protected String inarrival;
  protected String auth;
  protected Instant lddate;

  public static Logger getLogger() {
    return logger;
  }

  public static int getRecordLength() {
    return recordLength;
  }

  public int getAmpid() {
    return ampid;
  }

  public int getArid() {
    return arid;
  }

  public int getParid() {
    return parid;
  }

  public String getChan() {
    return chan;
  }

  public double getAmp() {
    return amp;
  }

  public Duration getPer() {
    return per;
  }

  public double getSnr() {
    return snr;
  }

  public Instant getAmptime() {
    return amptime;
  }

  public Instant getTime() {
    return time;
  }

  public double getDuration() {
    return duration;
  }

  public double getDeltaf() {
    return deltaf;
  }

  public String getAmptype() {
    return amptype;
  }

  public String getUnits() {
    return units;
  }

  public String getClip() {
    return clip;
  }

  public String getInarrival() {
    return inarrival;
  }

  public String getAuth() {
    return auth;
  }

  public Instant getLddate() {
    return lddate;
  }

  public void validate() {
    Validate.notNaN(getAmpid(), "Ampid is NaN");
    Validate.notNaN(getArid(), "Arid is NaN");
    Validate.notNaN(getParid(), "Parid is NaN");
    Validate.notNull(getChan(), "Chan is null");
    Validate.notNaN(getAmp(), "Amp is NaN");
    Validate.notNull(getPer(), "Per is null");
    Validate.notNaN(getSnr(), "Snr is NaN");
    Validate.notNull(getAmptime(), "Amptime is null");
    Validate.notNull(getTime(), "Time is null");
    Validate.notNaN(getDuration(), "Duration is NaN");
    Validate.notNaN(getDeltaf(), "Deltaf is NaN");
    Validate.notNull(getAmptype(), "Amptype is null");
    Validate.notNull(getUnits(), "Units is null");
    Validate.notNull(getClip(), "Clip is null");
    Validate.notNull(getInarrival(), "Inarrival is null");
    Validate.notNull(getAuth(), "Auth is null");
    Validate.notNull(getLddate(), "Lddate is null");
  }

  @Override
  public String toString() {
    return "AmplitudeRecord{" +
        "ampid=" + ampid +
        ", arid=" + arid +
        ", parid=" + parid +
        ", chan='" + chan + '\'' +
        ", amp=" + amp +
        ", per=" + per +
        ", snr=" + snr +
        ", amptime=" + amptime +
        ", time=" + time +
        ", duration=" + duration +
        ", deltaf=" + deltaf +
        ", amptype='" + amptype + '\'' +
        ", units='" + units + '\'' +
        ", clip='" + clip + '\'' +
        ", inarrival='" + inarrival + '\'' +
        ", auth='" + auth + '\'' +
        ", lddate=" + lddate +
        '}';
  }
}
