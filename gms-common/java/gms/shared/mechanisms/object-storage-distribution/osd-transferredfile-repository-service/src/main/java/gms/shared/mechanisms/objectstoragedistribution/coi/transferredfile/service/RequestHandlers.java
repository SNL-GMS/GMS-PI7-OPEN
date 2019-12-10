package gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.mechanisms.objectstoragedistribution.coi.dataacquisitionstatus.commonobjects.TransferredFile;
import gms.shared.mechanisms.objectstoragedistribution.coi.transferredfile.repository.TransferredFileRepositoryInterface;
import gms.shared.utilities.service.Request;
import gms.shared.utilities.service.RequestParsingUtils;
import gms.shared.utilities.service.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RequestHandlers {
  private final Logger logger = LoggerFactory.getLogger(RequestHandlers.class);

  private final TransferredFileRepositoryInterface transferredFileRepository;

  RequestHandlers(TransferredFileRepositoryInterface transferredFileRepository) {
    this.transferredFileRepository = Objects.requireNonNull(transferredFileRepository);
  }

  /**
   * Handles the /is-alive endpoint.
   * Used to determine if the service is running.  Returns a 200 response with the current system
   * time.
   *
   * @param request      {@link Request} object representing the HTTP request.
   * @param deserializer {@link ObjectMapper} to use for deserializing request body contents.
   * @return {@link Response}, representing the HTTP response.  Not Null.
   */
  Response<String> isAlive(Request request, ObjectMapper deserializer) {

    return Response.success(Long.toString(System.currentTimeMillis()));
  }

  Response<List<TransferredFile>> retrieveTransferredFilesByTimeRange(Request request, ObjectMapper deserializer) {
    logger.info("Retrieving transferred files by provided time range.");
    Objects.requireNonNull(request);
    Objects.requireNonNull(deserializer);
    // parse the request params if present
    Optional<Instant> start;
    Optional<Instant> end;
    try {
      start = RequestParsingUtils.extractRequestElement(request, deserializer, "transferStartTime", Instant.class);
      end = RequestParsingUtils.extractRequestElement(request, deserializer, "transferEndTime", Instant.class);
    } catch (RequestParsingUtils.DeserializationException ex) {
      return Response.clientError("Cannot retrieve TransferredFiles by time range with invalid transfer start or end time.");
    }

    if (!start.isPresent() && !end.isPresent()) {
      try {
        return Response.success(this.transferredFileRepository.retrieveAll());
      } catch (Exception ex) {
        logger.error(ex.getMessage());
        return Response.serverError(ex.getMessage());
      }
    }
    if (start.isPresent() ^ end.isPresent()) {
      return Response.clientError("Cannot retrieve TransferredFiles by time range when one but not both of transfer start and end time is provided.");
    }
    Instant transferStartTime = start.get();
    Instant transferEndTime = end.get();
    if (transferStartTime.isAfter(transferEndTime)) {
      return Response.clientError("Cannot retrieve TransferredFiles by time range with transfer start time after end time or end time before start time.");
    }
    try {
      return Response.success(this.transferredFileRepository.retrieveByTransferTime(transferStartTime, transferEndTime));
    } catch (Exception ex) {
      logger.error(ex.getMessage());
      return Response.serverError(ex.getMessage());
    }
  }

}

