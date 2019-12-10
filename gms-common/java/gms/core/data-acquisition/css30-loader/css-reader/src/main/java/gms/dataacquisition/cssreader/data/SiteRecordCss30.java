package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.Utility;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PositionalRecord
public class SiteRecordCss30 extends SiteRecord {

  private static final Logger logger = LoggerFactory.getLogger(SiteRecord.class);

  protected static int recordLength = 155;

  public static int getRecordLength() {
    return recordLength;
  }


  @PositionalField(initialPosition = 1, finalPosition = 6)
  public String getSta() {
    return sta;
  }

  public void setSta(String sta) {
    this.sta = sta.trim();
  }

  @PositionalField(initialPosition = 8, finalPosition = 15)
  public Instant getOndate() {
    return ondate;
  }

  public void setOndate(String ondate) {
    this.ondate = Utility.jdToInstant(ondate);
  }

  @PositionalField(initialPosition = 17, finalPosition = 24)
  public Instant getOffdate() {
    return offdate;
  }

  public void setOffdate(String offdate) {
    this.offdate = Utility.jdToInstant(offdate);
  }

  @PositionalField(initialPosition = 26, finalPosition = 34)
  public double getLat() {
    return lat;
  }

  public void setLat(String lat) {
    this.lat = Double.valueOf(lat);
  }

  @PositionalField(initialPosition = 36, finalPosition = 44)
  public double getLon() {
    return lon;
  }

  public void setLon(String lon) {
    this.lon = Double.valueOf(lon);
  }

  @PositionalField(initialPosition = 46, finalPosition = 54)
  public double getElev() {
    return elev;
  }

  public void setElev(String elev) {
    this.elev = Double.valueOf(elev);
  }

  @PositionalField(initialPosition = 56, finalPosition = 105)
  public String getStaname() {
    return staname;
  }

  public void setStaname(String staname) {
    this.staname = staname.trim();
  }

  @PositionalField(initialPosition = 107, finalPosition = 110)
  public String getStatype() {
    return statype;
  }

  public void setStatype(String statype) {
    this.statype = statype.trim();
  }

  @PositionalField(initialPosition = 112, finalPosition = 117)
  public String getRefsta() {
    return refsta;
  }

  public void setRefsta(String refsta) {
    this.refsta = refsta.trim();
  }

  @PositionalField(initialPosition = 119, finalPosition = 127)
  public double getDnorth() {
    return dnorth;
  }

  public void setDnorth(String dnorth) {
    this.dnorth = Double.valueOf(dnorth);
  }

  @PositionalField(initialPosition = 129, finalPosition = 137)
  public double getDeast() {
    return deast;
  }

  public void setDeast(String deast) {
    this.deast = Double.valueOf(deast);
  }

  @PositionalField(initialPosition = 139, finalPosition = 155)
  public Instant getLddate() {
    return lddate;
  }

  public void setLddate(String lddate) {
    this.lddate = Utility.parseDate(lddate);
  }
}
