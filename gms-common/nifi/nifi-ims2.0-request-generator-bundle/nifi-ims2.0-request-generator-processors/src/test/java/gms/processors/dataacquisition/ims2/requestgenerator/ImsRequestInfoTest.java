package gms.processors.dataacquisition.ims2.requestgenerator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImsRequestInfoTest {

    @Test
    public void testRequestOutput() throws JsonProcessingException {
        Instant start = Instant.parse("2019-03-04T17:45:16.744227Z");
        Instant end = Instant.parse("2019-03-04T17:50:16.744227Z");
        String station = "MKAR";
        ImsRequestInfo requestInfo = new ImsRequestInfo(station, start, end);
        final String reqInfoStr = CoiObjectMapperFactory.getJsonObjectMapper().
                writeValueAsString(requestInfo);
        assertEquals("MKAR", requestInfo.getStation());
        assertEquals("2019/03/04 17:45:16 to 2019/03/04 17:50:16", requestInfo.getTimeRange());
        //make sure we have the escape chars
        assertEquals("{\"timeRange\":\"2019/03/04 17:45:16 to 2019/03/04 17:50:16\",\"station\":\"MKAR\"}",
                reqInfoStr);
    }
}
