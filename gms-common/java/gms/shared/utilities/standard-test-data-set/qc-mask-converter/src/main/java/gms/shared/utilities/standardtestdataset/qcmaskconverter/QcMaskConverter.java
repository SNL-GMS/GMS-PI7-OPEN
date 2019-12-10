package gms.shared.utilities.standardtestdataset.qcmaskconverter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskCategory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersion;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMaskVersionDescriptor;
import gms.shared.utilities.standardtestdataset.ReferenceChannelFileReader;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CLI that converts SME formatted JSONs for Qc Masks (deemed .smeware) to GMS COI format
 */
public class QcMaskConverter {

  private static final Logger logger = LoggerFactory.getLogger(QcMaskConverter.class);

  private final List<QcMask> convertedQcMasks;

  public QcMaskConverter(String masksFile, String channelsFile) throws Exception {
    this(new ObjectMapper().readTree(new File(masksFile)),
        new ReferenceChannelFileReader(channelsFile));
  }

  public QcMaskConverter(JsonNode maskArrayJson, ReferenceChannelFileReader channelReader) {
    this.convertedQcMasks = convertJsonToCOI(maskArrayJson, channelReader);
  }

  public List<QcMask> getConvertedMasks() {
    return Collections.unmodifiableList(this.convertedQcMasks);
  }

  /**
   * Default factory method used to create a QcMaskVersion. Primarily used by other factory methods
   * and for serialization.
   *
   * @param maskArrayJson A JSON object representing a QcMask[] to parse
   */
  private static List<QcMask> convertJsonToCOI(JsonNode maskArrayJson,
      ReferenceChannelFileReader channelReader) {
    final List<QcMask> masks = new ArrayList<>();
    //Iterate though every sub object in the JSON file because each file contains an array of objects
    for (int i = 0; i < maskArrayJson.size(); i++) {
      //Try to store convert object, upon success add it to successfullyConvertedQcMasks
      try {
        final JsonNode jsonObject = maskArrayJson.get(i);

        //Parse stuff from JSON File
        final String site = jsonObject.findValue("site").textValue();
        final String chan = jsonObject.findValue("chan").textValue();
        final double wfid = jsonObject.findValue("wfid").doubleValue();
        final QcMaskCategory qcMaskCategory = QcMaskCategory
            .valueOf(formatValueForEnum(jsonObject.findValue("category").textValue()));
        final QcMaskType qcMaskType = QcMaskType
            .valueOf(formatValueForEnum(jsonObject.findValue("type").textValue()));
        final String rationale = jsonObject.findValue("rationale").textValue();
        final Double startDouble = jsonObject.findValue("start_time").asDouble();
        final Double endDouble = jsonObject.findValue("end_time").asDouble();
        final Instant startTime = Instant.ofEpochSecond(startDouble.longValue());
        final Instant endTime = Instant.ofEpochSecond(endDouble.longValue());

        //Hash UUIDs
        final String qcIdString = site + chan + startTime.toString() + endTime.toString();
        // find channel that corresponds to this mask
        final Optional<UUID> channelId = channelReader
            .findChannelIdByNameAndTime(site, chan, startTime);
        if (!channelId.isPresent()) {
          logger.warn(
              String.format("Could not find ReferenceChannel for site %s, channel %s at time %s",
                  site, chan, startTime));
          continue;
        }

        final UUID qcId = UUID.nameUUIDFromBytes(qcIdString.getBytes());
        final UUID processingChannelId = channelId.get();
        final UUID channelSegmentId = UUID.nameUUIDFromBytes(Double.toString(wfid).getBytes());

        //Create objects for QcMask creation
        final QcMaskVersionDescriptor qcMaskVersionDescriptor = QcMaskVersionDescriptor.from(qcId, 0);
        final QcMaskVersion qcMaskVersion = QcMaskVersion.from(0, List.of(qcMaskVersionDescriptor),
            List.of(channelSegmentId), qcMaskCategory, qcMaskType, rationale, startTime,
            endTime);
        //Add mask to list
        masks.add(QcMask.from(qcId, processingChannelId, List.of(qcMaskVersion)));
      } catch (Exception e) {
        logger.error("Failed to convert and store Qc Mask at position " + i + ": " + e);
      }
    }
    return masks;
  }


  /**
   * Makes it so strings like "Waveform Quality" become "WAVEFORM_QUALITY" for enum lookup
   *
   * @param s String that matches an enum type character-wise
   * @return Correctly formatted string for Enum lookup
   */
  private static String formatValueForEnum(String s) {
    return s.replaceAll(" ", "_").toUpperCase();
  }
}
