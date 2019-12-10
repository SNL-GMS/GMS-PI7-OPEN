package gms.processors.dataacquisition.ims2.waveformparser;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.ims20.receiver.Ims20RawStationDataFrameUtility;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSoh;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

public class Ims20WaveformParserProcessor extends AbstractProcessor {

  public static final Relationship WAVEFORM_SUCCESS = new Relationship.Builder()
      .name("waveform-success")
      .description("Successfully parsed waveforms")
      .build();
  public static final Relationship SOH_SUCCESS = new Relationship.Builder()
      .name("soh-success")
      .description("Successfully parsed state-of-health")
      .build();
  public static final Relationship FAILURE = new Relationship.Builder()
      .name("failure")
      .description("Processing the frame failed")
      .build();
  private final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private Set<Relationship> relationships;

  @Override
  protected void init(final ProcessorInitializationContext context) {
    getLogger().info("init");
    final Set<Relationship> relationships = new HashSet<>();
    relationships.add(WAVEFORM_SUCCESS);
    relationships.add(SOH_SUCCESS);
    relationships.add(FAILURE);
    this.relationships = Collections.unmodifiableSet(relationships);
  }

  @Override
  public Set<Relationship> getRelationships() {
    return this.relationships;
  }

  @Override
  public void onTrigger(final ProcessContext context, final ProcessSession session)
      throws ProcessException {
    try {
      final FlowFile flowFile = session.get();
      if (flowFile == null) {
        getLogger().info("Flowfile is null");
        return;
      }

      final InputStream inputStream = session.read(flowFile);
      final String flowFileStr = new BufferedReader(new InputStreamReader(inputStream))
          .lines().collect(Collectors.joining());
      inputStream.close();

      final ArrayList<ChannelSegment<Waveform>> waveforms = new ArrayList<>();
      final ArrayList<AcquiredChannelSoh> sohs = new ArrayList<>();
      final ArrayList<RawStationDataFrame> failedRSDFS = new ArrayList<>();
      try {
        RawStationDataFrame rsdf = objectMapper.readValue(flowFileStr, RawStationDataFrame.class);
        Objects.requireNonNull(rsdf, "Parsed frame as null");

        try {
          //Parse Waveforms, SOH from rsdf
          final List<Optional<Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>>>> dataPairs =
              getFrameParser().parse(rsdf.getRawPayload());

          for (Optional<Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>>> wfs : dataPairs) {
            Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>> dataPair =
                wfs.orElseThrow(
                    () -> new RuntimeException("Could not parse frame into waveform and SOH"));

            //add to lists
            waveforms.add(dataPair.getLeft());
            sohs.addAll(dataPair.getRight());
          }
        } catch (Exception e) {
          getLogger().error("Error processing RawStationDataFrame: ", e);
          failedRSDFS.add(rsdf);
        }

        flowIfNonEmpty(flowFile, session, WAVEFORM_SUCCESS, waveforms);
        flowIfNonEmpty(flowFile, session, SOH_SUCCESS, sohs);
        flowIfNonEmpty(flowFile, session, FAILURE, failedRSDFS);
      } catch (Exception e) {
        getLogger().error("Failure processing flow file expecting RawStationDataFrame: ", e);
        session.transfer(flowFile, FAILURE);
      }
      session.remove(flowFile);
      session.commit();
    } catch (
        Exception e) {
      getLogger().error("Error in onTrigger: ", e);
      session.rollback();
    }
  }

  private <T> void flowIfNonEmpty(FlowFile parentFlow, ProcessSession session,
      Relationship relationship,
      Collection<T> coll) {
    if (coll != null && !coll.isEmpty()) {
      final FlowFile flow = session.create(parentFlow);
      session.write(flow, outputStream -> objectMapper.writeValue(outputStream, coll));
      session.transfer(flow, relationship);
    }
  }

  @FunctionalInterface
  protected interface FrameParser {

    List<Optional<Pair<ChannelSegment<Waveform>, Collection<AcquiredChannelSoh>>>> parse(
        byte[] rawFramePayload);

  }

  protected FrameParser getFrameParser() {
    return Ims20RawStationDataFrameUtility::parseRawStationDataFrame;
  }

}
