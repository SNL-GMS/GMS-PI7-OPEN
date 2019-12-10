package gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.common.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.repository.ChannelProcessingGroupRepository;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.ObjectSerialization;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.RequestUtil;
import gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.service.util.ResponseUtil;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

public class ChannelProcessingGroupRouteHandlers {

  private static Logger logger = LoggerFactory.getLogger(ChannelProcessingGroupRouteHandlers.class);

  /**
   * Serializes and deserializes signal detection common objects
   */
  private static final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();

  private final ChannelProcessingGroupRepository channelProcessingGroupRepository;

  private ChannelProcessingGroupRouteHandlers(
      ChannelProcessingGroupRepository channelProcessingGroupRepository) {
    this.channelProcessingGroupRepository = channelProcessingGroupRepository;
  }

  /**
   * Factory method for creating {@link ChannelProcessingGroupRouteHandlers}
   *
   * @param channelProcessingGroupRepository Signal Detection Repository class for retrieving {@link
   * gms.shared.mechanisms.objectstoragedistribution.coi.signaldetection.commonobjects.ChannelProcessingGroup}
   * objects from persistence
   * @return The route handlers object using the input repository
   */
  public static ChannelProcessingGroupRouteHandlers create(
      ChannelProcessingGroupRepository channelProcessingGroupRepository) {
    return new ChannelProcessingGroupRouteHandlers(
        Objects.requireNonNull(channelProcessingGroupRepository));
  }

  public String getChannelProcessingGroup(Request request, Response response) throws Exception {
    Objects.requireNonNull(request);
    Objects.requireNonNull(response);

    Optional<UUID> id = Optional.ofNullable(request.params(":id")).map(UUID::fromString);

    if (RequestUtil.clientAcceptsJson(request)) {
      //return a single signal detection if provided an id, otherwise return all signal detections

        return ObjectSerialization.writeValue(
            id.isPresent() ? channelProcessingGroupRepository.retrieve(id.get())
                : channelProcessingGroupRepository.retrieveAll());
    } else {
      return ResponseUtil.notAcceptable(request, response);
    }
  }

  public String storeChannelProcessingGroups(Request request, Response response) throws Exception {

    Objects.requireNonNull(request, "Cannot accept null request");
    Objects.requireNonNull(response, "Cannot accept null response");

    Validate.notNull(request.body(), "Cannot store null channel processing groups");
    ChannelProcessingGroup[] groups = objectMapper
        .readValue(request.body(), ChannelProcessingGroup[].class);

    if (logger.isInfoEnabled()) {
      logger.info("storeChannelProcessingGroups endpoint hit with parameters: {}",
          Arrays.toString(groups));
    }

    for (ChannelProcessingGroup group : groups) {
      channelProcessingGroupRepository.createChannelProcessingGroup(group);
    }
    return "";
  }
}
