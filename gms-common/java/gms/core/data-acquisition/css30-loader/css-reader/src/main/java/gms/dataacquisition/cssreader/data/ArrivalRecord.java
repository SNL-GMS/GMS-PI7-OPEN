package gms.dataacquisition.cssreader.data;

import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArrivalRecord {

  private static final Logger logger = LoggerFactory.getLogger(ArrivalRecord.class);

  protected static int recordLength;

  protected String sta;
  protected Instant time;
  protected int arid;
  protected Instant jdate;
  protected int stassid;
  protected int chanid;
  protected String chan;
  protected String iphase;
  protected String stype;
  protected double deltim;
  protected double azimuth;
  protected double delaz;
  protected double slow;
  protected double delslo;
  protected double ema;
  protected double rect;
  protected double amp;
  protected double per;
  protected double logat;
  protected String clip;
  protected String fm;
  protected double snr;
  protected String qual;
  protected String auth;
  protected int commid;
  protected Instant lddate;


  public String getSta() {
    return sta;
  }

  public Instant getTime() {
    return time;
  }

  public int getArid() {
    return arid;
  }

  public Instant getJdate() {
    return jdate;
  }

  public int getStassid() {
    return stassid;
  }

  public int getChanid() {
    return chanid;
  }

  public String getChan() {
    return chan;
  }

  public String getIphase() {
    return iphase;
  }

  public String getStype() {
    return stype;
  }

  public double getDeltim() {
    return deltim;
  }

  public double getAzimuth() {
    return azimuth;
  }

  public double getDelaz() {
    return delaz;
  }

  public double getSlow() {
    return slow;
  }

  public double getDelslo() {
    return delslo;
  }

  public double getEma() {
    return ema;
  }

  // get rect m8
  public double getRect() {
    return rect;
  }

  public double getAmp() {
    return amp;
  }

  public double getPer() {
    return per;
  }

  public double getLogat() {
    return logat;
  }

  public String getClip() {
    return clip;
  }

  public String getFm() {
    return fm;
  }

  public double getSnr() {
    return snr;
  }

  public String getQual() {
    return qual;
  }

  public String getAuth() {
    return auth;
  }

  public int getCommid() {
    return commid;
  }

  public Instant getLddate() {
    return lddate;
  }

  public void validate() {
    Validate.notNull(getSta(), "Sta is null");
    Validate.notNull(getTime(), "Time is null");
    Validate.notNaN(getArid(), "Arid is NaN");
//    Validate.notNull(getJdate(), "Jdate is null");
    Validate.notNaN(getStassid(), "Stassid is NaN");
    Validate.notNaN(getChanid(), "Chanid is NaN");
    Validate.notNull(getChan(), "Chan is null");
    Validate.notNull(getIphase(), "Iphase is null");
    Validate.notNull(getStype(), "Stype is null");
    Validate.notNaN(getDeltim(), "Deltim is NaN");
    Validate.notNaN(getAzimuth(), "Azimuth is NaN");
    Validate.notNaN(getDelaz(), "Delaz is NaN");
    Validate.notNaN(getSlow(), "Slow is NaN");
    Validate.notNaN(getDelslo(), "Delslo is NaN");
    Validate.notNaN(getEma(), "Ema is NaN");
    Validate.notNaN(getRect(), "Rect is NaN");
    Validate.notNaN(getAmp(), "Amp is NaN");
    Validate.notNaN(getPer(), "Per is NaN");
    Validate.notNaN(getLogat(), "Logat is NaN");
    Validate.notNull(getClip(), "Clip is null");
    Validate.notNull(getFm(), "Fm is null");
    Validate.notNaN(getSnr(), "Snr is NaN");
    Validate.notNull(getQual(), "Qual is null");
    Validate.notNull(getAuth(), "Auth is null");
    Validate.notNaN(getCommid(), "Commid is NaN");
    Validate.notNull(getLddate(), "Lddate is null");
  }

  @Override
  public String toString() {
    return "ArrivalRecord{" +
        "sta='" + sta + '\'' +
        ", time=" + time +
        ", arid=" + arid +
        ", jdate=" + jdate +
        ", stassid=" + stassid +
        ", chanid=" + chanid +
        ", chan='" + chan + '\'' +
        ", iphase='" + iphase + '\'' +
        ", stype='" + stype + '\'' +
        ", deltim=" + deltim +
        ", azimuth=" + azimuth +
        ", delaz=" + delaz +
        ", slow=" + slow +
        ", delslo=" + delslo +
        ", ema=" + ema +
        ", rect=" + rect +
        ", amp=" + amp +
        ", per=" + per +
        ", logat=" + logat +
        ", clip='" + clip + '\'' +
        ", fm='" + fm + '\'' +
        ", snr=" + snr +
        ", qual='" + qual + '\'' +
        ", auth='" + auth + '\'' +
        ", commid=" + commid +
        ", lddate=" + lddate +
        '}';
  }
}
