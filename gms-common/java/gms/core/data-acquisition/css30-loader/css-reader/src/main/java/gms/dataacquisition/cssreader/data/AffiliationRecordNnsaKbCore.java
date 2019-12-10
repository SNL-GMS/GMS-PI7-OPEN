package gms.dataacquisition.cssreader.data;

import com.github.ffpojo.metadata.positional.annotation.PositionalField;
import com.github.ffpojo.metadata.positional.annotation.PositionalRecord;
import gms.dataacquisition.cssreader.utilities.Utility;
import java.time.Instant;
import org.apache.commons.lang3.Validate;

@PositionalRecord
public class AffiliationRecordNnsaKbCore extends AffiliationRecord {

    public static final int RECORD_LENGTH = 69;

    @PositionalField(initialPosition = 1, finalPosition = 8)
    public String getNet() { return this.net; }
    public void setNet(String s) { this.net = s.trim(); }

    @PositionalField(initialPosition = 10, finalPosition = 15)
    public String getSta() { return this.sta; }
    public void setSta(String s) { this.sta = s.trim(); }

    @PositionalField(initialPosition = 17, finalPosition = 33)
    public Instant getTime() { return this.time; }
    public void setTime(String s) { this.time = Utility.toInstant(s); }

    @PositionalField(initialPosition = 35, finalPosition = 51)
    public Instant getEndtime() { return endtime; }
    public void setEndtime(String s) { this.endtime = Utility.toInstant(s); }

    @PositionalField(initialPosition = 53, finalPosition = 69)
    public Instant getLddate() { return lddate; }
    public void setLddate(String s) { this.lddate = Utility.parseDate(s); }
    
    @Override
    public void validate() {
        Validate.notEmpty(getNet(),"NET field is empty");
        Validate.notEmpty(getSta(), "STA field is empty");
        Validate.notNull(getLddate(), "LDDATE field is null");

        if (getTime().getEpochSecond() > getEndtime().getEpochSecond()) {
            throw new IllegalArgumentException("TIME is greater than ENDTIME");
        }
    }
}
