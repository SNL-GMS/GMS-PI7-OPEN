package gms.dataacquisition.cssreader.data;

import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiteRecord {

  private static final Logger logger = LoggerFactory.getLogger(SiteRecord.class);

  protected static int recordLength;

  protected String sta;
  protected Instant ondate;
  protected Instant offdate;
  protected double lat;
  protected double lon;
  protected double elev;
  protected String staname;
  protected String statype;
  protected String refsta;
  protected double dnorth;
  protected double deast;
  protected Instant lddate;

  public static int getRecordLength() {
    return recordLength;
  }

  public String getSta() {
    return sta;
  }

  public Instant getOndate() {
    return ondate;
  }

  public Instant getOffdate() {
    return offdate;
  }

  public double getLat() {
    return lat;
  }

  public double getLon() {
    return lon;
  }

  public double getElev() {
    return elev;
  }

  public String getStaname() {
    return staname;
  }

  public String getStatype() {
    return statype;
  }

  public String getRefsta() {
    return refsta;
  }

  public double getDnorth() {
    return dnorth;
  }

  public double getDeast() {
    return deast;
  }

  public Instant getLddate() {
    return lddate;
  }

  @Override
  public String toString() {
    return "SiteRecord{" +
        ", sta='" + sta + '\'' +
        ", ondate=" + ondate +
        ", offdate=" + offdate +
        ", lat=" + lat +
        ", lon=" + lon +
        ", elev=" + elev +
        ", staname='" + staname + '\'' +
        ", statype='" + statype + '\'' +
        ", refsta='" + refsta + '\'' +
        ", dnorth=" + dnorth +
        ", deast=" + deast +
        ", lddate=" + lddate +
        '}';
  }

  public void validate() {
    Validate.notEmpty(getSta(), "Station name is empty");
    Validate.notNull(getOndate(), "On date is null");
    Validate.notNull(getOffdate(), "Off date is null");
    Validate.notEmpty(getStaname(), "Station name type is empty");
    Validate.notEmpty(getStatype(), "Station type is empty");
    Validate.notEmpty(getRefsta(), "Reference station name is empty");
    Validate.notNaN(getLat(), "Latitude is NaN");
    Validate.notNaN(getLon(), "Longitude angle is NaN");
    Validate.notNaN(getElev(), "Elevation angle is NaN");
    Validate.notNaN(getDnorth(), "Displacement north is NaN");
    Validate.notNaN(getDeast(), "Displacement east is NaN");
    Validate.notNull(getLddate(), "Load date is null");
  }


}
