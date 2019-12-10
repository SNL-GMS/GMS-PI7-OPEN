package gms.dataacquisition.cssreader.data;

import java.time.Instant;
import org.apache.commons.lang3.Validate;

/**
 * Represents the 'AffiliationRecord' table in CSS.
 * Uses the FFPojo library annotations to make this class parsable from a flat file representation.
 */
public class AffiliationRecord {

    protected String net;
    protected String sta;
    protected Instant time;
    protected Instant endtime;
    protected Instant lddate;

    public String getNet() { return net; }
    public String getSta() { return sta; }
    public Instant getTime() { return time; }
    public Instant getEndtime() { return endtime; }
    public Instant getLddate() { return lddate; }

    @Override
    public String toString() {
        return "AffiliationRecord{" +
                "net='" + net + '\'' +
                ", sta='" + sta + '\'' +
                ", time=" + time +
                ", endtime=" + endtime +
                ", lddate='" + lddate + '\'' +
                '}';
    }

    /**
     * Perform validation on the CSS affiliation row and exceptions will be thrown if invalid (such as
     * NullPointerException and IllegalArgumentException).
     */
    public void validate() {
        Validate.notEmpty(getNet(),"NET field is empty");
        Validate.notEmpty(getSta(), "STA field is empty");
        Validate.notNull(getLddate(), "LDDATE field is null");
    }
}
