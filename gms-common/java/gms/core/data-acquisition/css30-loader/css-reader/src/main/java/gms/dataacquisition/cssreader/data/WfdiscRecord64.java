package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.Utility;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extended WfdiscRecord to represent 64-bit IDs (e.g., big-ID wfid). The FFPOJO library used to
 * parse the flat file requires both getters and setters in the class. Therefore, although the
 * parent WfdiscRecord has getters, we must duplicate them here. Created by trsault on 10/20/17.
 */
@PositionalRecord
public class WfdiscRecord64 extends WfdiscRecord {

  private static final Logger logger = LoggerFactory.getLogger(WfdiscRecord64.class);

  @PositionalField(initialPosition = 1, finalPosition = 6)
  public String getSta() {
    return this.sta;
  }

  public void setSta(String s) {
    this.sta = s;
  }

  @PositionalField(initialPosition = 8, finalPosition = 15)
  public String getChan() {
    return this.chan;
  }

  public void setChan(String s) {
    this.chan = s;
  }

  @PositionalField(initialPosition = 17, finalPosition = 33)
  public Instant getTime() { return this.time; }
  public void setTime(String s) { this.time = Utility.toInstant(s);  }

  @PositionalField(initialPosition = 35, finalPosition = 48)
  public long getWfid() {
    return wfid;
  }

  public void setWfid(String s) {
    this.wfid = Long.valueOf(s);
  }

  @PositionalField(initialPosition = 50, finalPosition = 57)
  public long getChanid() {
    return chanid;
  }

  public void setChanid(String s) {
    this.chanid = Long.valueOf(s);
  }

  @PositionalField(initialPosition = 59, finalPosition = 66)
  public int getJdate() {
    return jdate;
  }

  public void setJdate(String s) {
    this.jdate = Integer.valueOf(s);
  }

  @PositionalField(initialPosition = 68, finalPosition = 84)
  public Instant getEndtime() { return this.endtime; }
  public void setEndtime(String s) { this.endtime = Utility.toInstant(s); }

  @PositionalField(initialPosition = 86, finalPosition = 93)
  public int getNsamp() {
    return nsamp;
  }

  public void setNsamp(String s) {
    this.nsamp = Integer.valueOf(s);
  }

  @PositionalField(initialPosition = 95, finalPosition = 105)
  public double getSamprate() {
    return samprate;
  }

  public void setSamprate(String s) {
    this.samprate = Double.valueOf(s);
  }

  @PositionalField(initialPosition = 107, finalPosition = 122)
  public double getCalib() {
    return calib;
  }

  public void setCalib(String s) {
    this.calib = Double.valueOf(s);
  }

  @PositionalField(initialPosition = 124, finalPosition = 139)
  public double getCalper() {
    return calper;
  }

  public void setCalper(String s) {
    this.calper = Double.valueOf(s);
  }

  @PositionalField(initialPosition = 141, finalPosition = 146)
  public String getInstype() {
    return instype;
  }

  public void setInstype(String s) {
    this.instype = s;
  }

  @PositionalField(initialPosition = 148, finalPosition = 148)
  public String getSegtype() {
    return segtype;
  }

  public void setSegtype(String s) {
    this.segtype = s;
  }

  @PositionalField(initialPosition = 150, finalPosition = 151)
  public String getDatatype() {
    return datatype;
  }

  public void setDatatype(String s) {
    this.datatype = s;
  }

  @PositionalField(initialPosition = 153, finalPosition = 153)
  public boolean getClip() {
    return clip;
  }

  public void setClip(String s) {
    this.clip = (s != null && s.trim().equalsIgnoreCase("c"));
  }

  @PositionalField(initialPosition = 155, finalPosition = 218)
  public String getDir() {
    return dir;
  }

  public void setDir(String s) {
    this.dir = s;
  }

  @PositionalField(initialPosition = 220, finalPosition = 251)
  public String getDfile() {
    return dfile;
  }

  public void setDfile(String s) {
    this.dfile = s;
  }

  @PositionalField(initialPosition = 253, finalPosition = 262)
  public int getFoff() {
    return foff;
  }

  public void setFoff(String s) {
    this.foff = Integer.valueOf(s);
  }

  @PositionalField(initialPosition = 264, finalPosition = 272)
  public int getCommid() {
    return commid;
  }

  public void setCommid(String s) {
    this.commid = Integer.valueOf(s);
  }

  @PositionalField(initialPosition = 274, finalPosition = 289)
  public String getLddate() {
    return lddate;
  }

  public void setLddate(String s) {
    this.lddate = s;
  }
}
