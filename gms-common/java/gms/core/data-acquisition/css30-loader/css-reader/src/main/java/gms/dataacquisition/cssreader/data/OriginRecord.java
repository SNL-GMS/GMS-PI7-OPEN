package gms.dataacquisition.cssreader.data;

import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.DepthRestraintType;
import java.time.Instant;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OriginRecord {
  private static final Logger logger = LoggerFactory.getLogger(OriginRecord.class);

  protected static int recordLength;


  protected double lat;
  protected double lon;
  protected double depth;
  protected Instant time;
  protected int orid;
  protected int evid;
  protected int jdate;
  protected int nass;
  protected int ndef;
  protected int ndp;
  protected int grn;
  protected int srn;
  protected String etype;
  protected double depdp;
  // todo
  //protected String dtype;
  protected DepthRestraintType dtype;
  protected double mb;
  protected int mbid;
  protected double ms;
  protected int msid;
  protected double ml;
  protected int mlid;
  protected String algorithm;
  protected String auth;
  protected int commid;
  protected Instant lddate;



  public double getLat() {
    return lat;
  }

  public double getLon() {
    return lon;
  }

  public double getDepth() {
    return depth;
  }

  public Instant getTime() {
    return time;
  }

  public int getOrid() {
    return orid;
  }

  public int getEvid() {
    return evid;
  }

  public int getJdate() {
    return jdate;
  }

  public int getNass() {
    return nass;
  }

  public int getNdef() {
    return ndef;
  }

  public int getNdp() {
    return ndp;
  }

  public int getGrn() {
    return grn;
  }

  public int getSrn() {
    return srn;
  }

  public String getEtype() {
    return etype;
  }

  public double getDepdp() {
    return depdp;
  }

  /** todo
  public String getDtype() {
    return dtype;
  }
   */
  public DepthRestraintType getDtype() {
    return dtype;
  }

  public double getMb() {
    return mb;
  }

  public int getMbid() {
    return mbid;
  }

  public double getMs() {
    return ms;
  }

  public int getMsid() {
    return msid;
  }

  public double getMl() {
    return ml;
  }

  public int getMlid() {
    return mlid;
  }

  public String getAlgorithm() {
    return algorithm;
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
    Validate.notNaN(getLat(), "Lat is NaN");
    Validate.notNaN(getLon(), "Lon is empty");
    Validate.notNaN(getDepth(), "Depth is NaN");
    Validate.notNull(getTime(), "Time is null");
    Validate.notNull(getLddate(), "Load date is null");

  }

  @Override
  public String toString() {
    return "OriginRecord{" +
        "lat=" + lat +
        ", lon=" + lon +
        ", depth=" + depth +
        ", time=" + time +
        ", orid=" + orid +
        ", evid=" + evid +
        ", jdate=" + jdate +
        ", nass=" + nass +
        ", ndef=" + ndef +
        ", ndp=" + ndp +
        ", grn=" + grn +
        ", srn=" + srn +
        ", etype='" + etype + '\'' +
        ", depdp=" + depdp +
        ", dtype='" + dtype + '\'' +
        ", mb=" + mb +
        ", mbid=" + mbid +
        ", ms=" + ms +
        ", msid=" + msid +
        ", ml=" + ml +
        ", mlid=" + mlid +
        ", algorithm='" + algorithm + '\'' +
        ", auth='" + auth + '\'' +
        ", commid=" + commid +
        ", lddate=" + lddate +
        '}';
  }
}
