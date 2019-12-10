package gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.AnalystActionReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingContext;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.commonobjects.ProcessingStepReference;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.datatransferobjects.AnalystActionReferenceDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.datatransferobjects.ProcessingContextDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.processingcontrol.datatransferobjects.ProcessingStepReferenceDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.CreationInformation;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.InformationSource;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.commonobjects.SoftwareComponentInfo;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.CreationInfoDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.CreationInformationDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.InformationSourceDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.emerging.provenance.datatransferobjects.SoftwareComponentInfoDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.commonobjects.Event;
import gms.shared.mechanisms.objectstoragedistribution.coi.event.datatransferobjects.EventDtoConverter;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Calibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Channel;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Network;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.QcMask;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Response;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.SignalDetection;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Site;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.Station;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.CalibrationDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.ChannelDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.ChannelProcessingGroupDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.NetworkDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.QcMaskDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.ResponseDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.SignalDetectionDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.SiteDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.datatransferobjects.StationDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceAlias;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceCalibration;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceChannel;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizer;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceDigitizerMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetwork;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceNetworkMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceResponse;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSensor;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSite;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceSiteMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.ReferenceStationMembership;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceAliasDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceCalibrationDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceDigitizerDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceDigitizerMembershipDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceNetworkDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceNetworkMembershipDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceResponseDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceSensorDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceSiteDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceSiteMembershipDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.stationreference.commonobjects.datatransferobjects.ReferenceStationMembershipDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohAnalog;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.AcquiredChannelSohBoolean;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.ChannelSegment;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Immutable2dDoubleArray;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.RawStationDataFrame;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.commonobjects.Waveform;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.AcquiredChannelSohAnalogDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.AcquiredChannelSohBooleanDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.ChannelSegmentDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.Immutable2dDoubleArrayDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.RawStationDataFrameDto;
import gms.shared.mechanisms.objectstoragedistribution.coi.waveforms.datatransferobjects.WaveformDto;
import org.msgpack.jackson.dataformat.MessagePackFactory;

/**
 * Factory for getting properly-configured ObjectMapper's for use by most of the code.
 */
public final class CoiObjectMapperFactory {

  private CoiObjectMapperFactory() {
  }

  /**
   * Gets an ObjectMapper for use in JSON serialization. This ObjectMapper can serialize/deserialize
   * any COI object, and has common modules registered such as for Java 8 Instant.
   *
   * @return an ObjectMapper for use with JSON
   */
  public static ObjectMapper getJsonObjectMapper() {
    return configureObjectMapper(new ObjectMapper());
  }

  /**
   * Gets an ObjectMapper for use in msgpack serialization. This ObjectMapper can
   * serialize/deserialize any COI object, and has common modules registered such as for Java 8
   * Instant.
   *
   * @return an ObjectMapper for use with msgpack
   */
  public static ObjectMapper getMsgpackObjectMapper() {
    return configureObjectMapper(new ObjectMapper(new MessagePackFactory()));
  }

  /**
   * Gets an ObjectMapper for use in YAML serialization. This ObjectMapper can serialize/deserialize
   * any COI object, and has common modules registered such as for Java 8 Instant.
   *
   * @return an ObjectMapper for use with YAML
   */
  public static ObjectMapper getYamlObjectMapper() {
    // Disabling WRITE_DOC_START_MARKER and USE_NATIVE_TYPE_ID features prevents Jackson from
    // outputting lines line
    //   ---!<TYPE_NAME>
    // when serializing classes annotated with @JsonSubtypes.  These lines caused the
    // deserializer to fail.  The WRITE_DOC_START_MARKER is "---" and the USE_NATIVE_TYPE_ID is
    // the "!<...>" statement. Disabling only USE_NATIVE_TYPE_ID results in deserializable YAML, so
    // investigate leaving WRITE_DOC_START_MARKER enabled if disabling it leads to other issues.
    return configureObjectMapper(new ObjectMapper(
        new YAMLFactory()
            .disable(Feature.WRITE_DOC_START_MARKER)
            .disable(Feature.USE_NATIVE_TYPE_ID))
    );
  }

  private static ObjectMapper configureObjectMapper(ObjectMapper objMapper) {
    return registerMixins(objMapper.findAndRegisterModules()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
  }

  private static ObjectMapper registerMixins(ObjectMapper objMapper) {
    objMapper
        .addMixIn(AcquiredChannelSohAnalog.class, AcquiredChannelSohAnalogDto.class)
        .addMixIn(AcquiredChannelSohBoolean.class, AcquiredChannelSohBooleanDto.class)
        .addMixIn(AnalystActionReference.class, AnalystActionReferenceDto.class)
        .addMixIn(Calibration.class, CalibrationDto.class)
        .addMixIn(Channel.class, ChannelDto.class)
        .addMixIn(ChannelProcessingGroup.class, ChannelProcessingGroupDto.class)
        .addMixIn(ChannelSegment.class, ChannelSegmentDto.class)
        .addMixIn(CreationInfo.class, CreationInfoDto.class)
        .addMixIn(CreationInformation.class, CreationInformationDto.class)
        .addMixIn(Immutable2dDoubleArray.class, Immutable2dDoubleArrayDto.class)
        .addMixIn(InformationSource.class, InformationSourceDto.class)
        .addMixIn(Network.class, NetworkDto.class)
        .addMixIn(ProcessingContext.class, ProcessingContextDto.class)
        .addMixIn(ProcessingStepReference.class, ProcessingStepReferenceDto.class)
        .addMixIn(QcMask.class, QcMaskDto.class)
        .addMixIn(RawStationDataFrame.class, RawStationDataFrameDto.class)
        .addMixIn(ReferenceAlias.class, ReferenceAliasDto.class)
        .addMixIn(ReferenceCalibration.class, ReferenceCalibrationDto.class)
        .addMixIn(ReferenceDigitizer.class, ReferenceDigitizerDto.class)
        .addMixIn(ReferenceDigitizerMembership.class, ReferenceDigitizerMembershipDto.class)
        .addMixIn(ReferenceNetwork.class, ReferenceNetworkDto.class)
        .addMixIn(ReferenceNetworkMembership.class, ReferenceNetworkMembershipDto.class)
        .addMixIn(ReferenceResponse.class, ReferenceResponseDto.class)
        .addMixIn(ReferenceSensor.class, ReferenceSensorDto.class)
        .addMixIn(ReferenceSite.class, ReferenceSiteDto.class)
        .addMixIn(ReferenceSiteMembership.class, ReferenceSiteMembershipDto.class)
        .addMixIn(ReferenceStationMembership.class, ReferenceStationMembershipDto.class)
        .addMixIn(Response.class, ResponseDto.class)
        .addMixIn(SignalDetection.class, SignalDetectionDto.class)
        .addMixIn(Site.class, SiteDto.class)
        .addMixIn(SoftwareComponentInfo.class, SoftwareComponentInfoDto.class)
        .addMixIn(Station.class, StationDto.class)
        .addMixIn(Waveform.class, WaveformDto.class)
    ;
    // custom deserialization modules
    objMapper = registerEventSerializationModule(objMapper);

    return objMapper;
  }

  private static ObjectMapper registerEventSerializationModule(ObjectMapper objMapper) {
    // Register the Event serialization module.
    final SimpleModule eventSerializationModule = new SimpleModule();
    eventSerializationModule.addSerializer(Event.class, EventDtoConverter.SERIALIZER);
    eventSerializationModule.addDeserializer(Event.class, EventDtoConverter.DESERIALIZER);
    return objMapper.registerModule(eventSerializationModule);
  }
}
