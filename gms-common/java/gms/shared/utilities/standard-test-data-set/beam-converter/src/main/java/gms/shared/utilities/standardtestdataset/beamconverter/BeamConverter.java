package gms.shared.utilities.standardtestdataset.beamconverter;

import static gms.shared.utilities.standardtestdataset.beamconverter.Application.objectMapper;

import com.fasterxml.jackson.databind.JsonNode;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.PhaseType;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.BeamDefinition;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Location;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.RelativePosition;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * CLI that converts SME formatted JSONs for Beam Definitions (deemed .smeware) to GMS COI format
 */
public class BeamConverter {

  private static final Logger logger = LogManager.getLogger(BeamConverter.class);

  private final List<BeamDefinition> convertedBeamDefinitions;

  public BeamConverter(String beamDefinitionFile) throws IOException {
    this(objectMapper.readTree(new File(beamDefinitionFile)));
  }

  public BeamConverter(JsonNode beamDefinitionArrayJson) {
    this.convertedBeamDefinitions = convertJsonToCOI(beamDefinitionArrayJson);
  }

  public final List<BeamDefinition> getConvertedBeams() {
    return Collections.unmodifiableList(this.convertedBeamDefinitions);
  }

  /**
   * Default factory method used to create a BeamDefinition. Primarily used by other factory methods
   * and for serialization.
   *
   * @param beamDefinitionArrayJson A JSON object representing a BeamDefinition[] to parse
   */
  private static List<BeamDefinition> convertJsonToCOI(JsonNode beamDefinitionArrayJson) {
    final List<BeamDefinition> beamDefinitions = new ArrayList<>();

    //Iterate though every array of objects in the JSON file
    for (int i = 0; i < beamDefinitionArrayJson.size(); i++) {
      //Try to store convert object, upon success add it to successfullyConvertedBeamDefinitions
      try {
        final JsonNode jsonObject = beamDefinitionArrayJson.get(i);
        Objects.requireNonNull(jsonObject,
            "Encountered null object at index " + i + " of the array");

        //Parse input JSON File
        final double azimuth = jsonObject.findValue("Azimuth").doubleValue();
        final double slowness = jsonObject.findValue("Slowness").doubleValue();
        final double nominalSampleRate = jsonObject.findValue("NominalSampleRate").doubleValue();
        final double sampleRateTolerance = jsonObject.findValue("SampleRateTolerance")
            .doubleValue();
        // Convert strings in json input file into boolean values
        final String coherentString = jsonObject.findValue("Coherent").textValue();
        final boolean coherent = coherentString.contains("TRUE");
        final String snappedSamplingString = jsonObject.findValue("SnappedSampling").textValue();
        final boolean snappedSampling = snappedSamplingString.contains("TRUE");
        final String twoDimensionalString = jsonObject.findValue("TwoDimensional").textValue();
        final boolean twoDimensional = twoDimensionalString.contains("TRUE");
        //Convert a PhaseType string of "-" in json input file to PhaseType enum UNKNOWN for BeamDefinition object
        final String phaseString = jsonObject.findValue("PhaseType").textValue();
        final PhaseType phaseType =
            phaseString.contains("-") ? PhaseType.UNKNOWN : PhaseType.valueOf(phaseString);
        final int minimumWaveformsForBeam = jsonObject.findValue("MinimumWaveformsForBeam").intValue();

        //Create objects for BeamDefinition creation
        /**
         * Create a Location object which is needed to create a RelativePosition object below.
         * Note that a single Location object is repeatedly used in each element of the RelativePosition
         * array for this particular element of the beamDefinitionArrayJson
         */
        final JsonNode locationJsonObject = jsonObject.get("ReferenceLocation");
        final double referenceLocationLatitudeDeg = locationJsonObject.findValue("Latitude")
            .doubleValue();
        final double referenceLocationLongitudeDeg = locationJsonObject.findValue("Longitude")
            .doubleValue();
        final double referenceLocationElevationKm = locationJsonObject.findValue("Elevation")
            .doubleValue();
        final double referenceLocationDepthKm = locationJsonObject.findValue("Depth").doubleValue();

        final Location location = Location
            .from(referenceLocationLatitudeDeg, referenceLocationLongitudeDeg,
                referenceLocationDepthKm, referenceLocationElevationKm
            );

        /** Map Channel IDs to different sets of directional displacement values (positions).
         * There is one RelativePosition for each Channel used to compute the Beam.
         * Each RelativePosition contains a Location object, which was defined above.
         */
        Map<UUID, RelativePosition> positions = new HashMap<>();
        // Iterate through each grouping of displacement values, creating a RelativePosition object for
        // each set, and add it to the list of positions
        final JsonNode relativePositionJsonObject = jsonObject.get("RelativePosition");
        for (int j = 0; j < relativePositionJsonObject.size(); j++) {
          final JsonNode positionJsonObject = relativePositionJsonObject.get(j);
          final double northDisplacementKm = positionJsonObject.findValue("NorthDisplacement")
              .doubleValue();
          final double eastDisplacementKm = positionJsonObject.findValue("EastDisplacement")
              .doubleValue();
          final double verticalDisplacementKm = positionJsonObject.findValue("VerticalDisplacement")
              .doubleValue();

          final RelativePosition relativePosition = RelativePosition
              .from(northDisplacementKm, eastDisplacementKm, verticalDisplacementKm
              );
          // Currently there is no linkage to the Channel ID, so a random UUID is being used instead
          positions.put(UUID.randomUUID(), relativePosition);
        }

        // Add beamDefinition to list
        beamDefinitions.add(BeamDefinition
            .from(phaseType, azimuth, slowness, coherent, snappedSampling, twoDimensional,
                nominalSampleRate, sampleRateTolerance, location, positions, minimumWaveformsForBeam));
      } catch (Exception e) {
        logger.error("Failed to convert and store Beam Definition at position " + i + ": " + e);
      }
    }
    return beamDefinitions;
  }

}



