package gms.processors.dataacquisition.ims2.requestgenerator;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ImsRequestInfo {

    private String timeRange;
    private String station;
    private SimpleDateFormat formatterWithTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public ImsRequestInfo(String station, Instant start, Instant end){
        this.formatterWithTime.setTimeZone(TimeZone.getTimeZone("UTC"));
        this.timeRange = formatRange(start, end);
        this.station = station;
    }

    private String formatRange(Instant start, Instant end){
        Date startDateTime = Date.from(start);
        String formattedStartDateTime = formatterWithTime.format(startDateTime);

        Date endDateTime = Date.from(end);
        String formattedEndDateTime = formatterWithTime.format(endDateTime);

        return formattedStartDateTime + " to " + formattedEndDateTime;
    }

    public String getStation(){ return station; }

    public String getTimeRange() {
        return timeRange;
    }
}
