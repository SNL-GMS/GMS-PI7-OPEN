package gms.dataacquisition.cssreader.data;


import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.Utility;
import java.time.Instant;
import org.apache.commons.lang3.Validate;

/**
 * Represents the 'InstrumentRecord' table in CSS.
 * Uses the FFPojo library annotations to make this class parsable from a flat file representation.
 */
@PositionalRecord
public class InstrumentRecord {

    protected int inid;
    protected String insname;
    protected String instype;
    protected String band;
    protected String digitial;
    protected double samprate;
    protected double ncalib;
    protected double ncalper;
    protected String dir;
    protected String dfile;
    protected String rsptype;
    protected Instant lddate;

    @PositionalField(initialPosition = 1, finalPosition = 8)
    public int getInid() { return inid; }
    public void setInid(String s) { this.inid = Integer.valueOf(s); }

    @PositionalField(initialPosition = 10, finalPosition = 59)
    public String getInsname() { return this.insname; }
    public void setInsname(String s) { this.insname = s; }

    @PositionalField(initialPosition = 61, finalPosition = 66)
    public String getInstype() { return this.instype; }
    public void setInstype(String s) { this.instype = s; }
    
    @PositionalField(initialPosition = 68, finalPosition = 68)
    public String getBand() { return this.band; }
    public void setBand(String s) { this.band = s; }
    
    @PositionalField(initialPosition = 70, finalPosition = 70)
    public String getDigitial() { return this.digitial; }
    public void setDigitial(String s) { this.digitial = s; }

    @PositionalField(initialPosition = 72, finalPosition = 82)
    public double getSamprate() { return samprate; }
    public void setSamprate(String s) { this.samprate = Double.valueOf(s); }

    @PositionalField(initialPosition = 84, finalPosition = 99)
    public double getNcalib() { return ncalib; }
    public void setNcalib(String s) { this.ncalib = Double.valueOf(s); }

    @PositionalField(initialPosition = 101, finalPosition = 116)
    public double getNcalper() { return ncalper; }
    public void setNcalper(String s) { this.ncalper = Double.valueOf(s); }

    @PositionalField(initialPosition = 118, finalPosition = 181)
    public String getDir() { return dir; }
    public void setDir(String s) { this.dir = s; }

    @PositionalField(initialPosition = 183, finalPosition = 214)
    public String getDfile() { return dfile; }
    public void setDfile(String s) { this.dfile = s; }

    @PositionalField(initialPosition = 216, finalPosition = 221)
    public String getRsptype() { return rsptype; }
    public void setRsptype(String s) { this.rsptype = s; }

    @PositionalField(initialPosition = 223, finalPosition = 239)
    public Instant getLddate() { return lddate; }
    public void setLddate(String s) { this.lddate = Utility.parseDate(s); }

    @Override
    public String toString() {
        return "InstrumentRecord{" +
                "inid=" + inid +
                ", insname='" + insname + '\'' +
                ", instype='" + instype + '\'' +
                ", band='" + band + '\'' +
                ", digitial='" + digitial + '\'' +
                ", samprate=" + samprate +
                ", ncalib=" + ncalib +
                ", ncalper=" + ncalper +
                ", dir='" + dir + '\'' +
                ", dfile='" + dfile + '\'' +
                ", rsptype='" + rsptype + '\'' +
                ", lddate='" + lddate + '\'' +
                '}';
    }

    /**
     * Perform validation on the CSS instrument row and exceptions will be thrown if invalid (such as
     * NullPointerException and IllegalArgumentException).
     */
    public void validate() {
        Validate.isTrue(getInid() >= 0, "Invalid INID value - must be >= 0");
        Validate.notEmpty(getInsname(), "INSNAME field is empty");
        Validate.notEmpty(getInstype(), "INSTYPE field is empty");

        //TODO: Do we validate band / digital? They appear to be 0 bytes each
        
        Validate.isTrue(getSamprate() >= 0, "Invalid SAMPRATE value - must be >= 0");
        Validate.isTrue(getNcalib() >= 0, "Invalid NCALIB value - must be >= 0");
        Validate.isTrue(getNcalper() >= 0, "Invalid NCALPER value - must be >= 0");
        Validate.notEmpty(getDir(), "DIR field is empty");
        Validate.notEmpty(getDfile(), "DFILE field is empty");
        Validate.notEmpty(getRsptype(), "RSPTYPE field is empty");
        Validate.notNull(getLddate(), "LDDATE field is null");
    }
}
