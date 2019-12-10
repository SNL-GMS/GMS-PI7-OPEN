package gms.dataacquisition.cssreader.data;

import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SensorRecord {

  private static final Logger logger = LoggerFactory.getLogger(SensorRecord.class);

  protected static int recordLength;

  protected String sta;
  protected String chan;
  protected Instant time;
  protected Instant endTime;
  protected int inid;
  protected int chanid;
  protected int jdate;
  protected double calratio;
  protected double calper;
  protected double tshift;
  protected char instant;
  protected Instant lddate;

  public static int getRecordLength() {
    return recordLength;
  }

  public String getSta() {
    return sta;
  }

  public void setSta(String sta) {
    this.sta = sta;
  }

  public String getChan() {
    return chan;
  }

  public void setChan(String chan) {
    this.chan = chan;
  }

  public Instant getTime() {
    return time;
  }

  public void setTime(Instant time) {
    this.time = time;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  public int getInid() {
    return inid;
  }

  public void setInid(int inid) {
    this.inid = inid;
  }

  public int getChanid() {
    return chanid;
  }

  public void setChanid(int chanid) {
    this.chanid = chanid;
  }

  public int getJdate() {
    return jdate;
  }

  public void setJdate(int jdate) {
    this.jdate = jdate;
  }

  public double getCalratio() {
    return calratio;
  }

  public void setCalratio(double calratio) {
    this.calratio = calratio;
  }

  public double getCalper() {
    return calper;
  }

  public void setCalper(double calper) {
    this.calper = calper;
  }

  public double getTshift() {
    return tshift;
  }

  public void setTshift(double tshift) {
    this.tshift = tshift;
  }

  public char getInstant() {
    return instant;
  }

  public void setInstant(char instant) {
    this.instant = instant;
  }

  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(Instant lddate) {
    this.lddate = lddate;
  }

  public void validate() {
    Validate.notEmpty(getSta(), "Station name is empty");
    Validate.notNull(getChan(), "Channel name is null");
    Validate.notNull(getTime(), "Time is null");
    Validate.notNull(getEndTime(), "End time is null");
    Validate.notNaN(getCalratio(), "Calratio is NaN");
    Validate.notNaN(getCalper(), "Calper is NaN");
    Validate.notNaN(getTshift(), "Tshift is NaN");
    Validate.notNull(getInstant(), "Instant is null");
    Validate.notNull(getLddate(), "Load date is null");
  }

  @Override
  public String toString() {
    return "SensorRecord{" +
        "sta='" + sta + '\'' +
        ", chan='" + chan + '\'' +
        ", time=" + time +
        ", endTime=" + endTime +
        ", inid=" + inid +
        ", chanid=" + chanid +
        ", jdate=" + jdate +
        ", calratio=" + calratio +
        ", calper=" + calper +
        ", tshift=" + tshift +
        ", instant=" + instant +
        ", lddate=" + lddate +
        '}';
  }
}
