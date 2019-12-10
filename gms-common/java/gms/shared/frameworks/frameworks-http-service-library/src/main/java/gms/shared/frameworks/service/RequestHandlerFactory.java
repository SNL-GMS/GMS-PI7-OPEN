package gms.shared.frameworks.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RequestHandlerFactory {

  private RequestHandlerFactory() {
  }

  public static <T> RequestHandler<Collection<UUID>> storageHandler(
      Class<T[]> inputClass,
      Consumer<Collection<T>> safeStorageFunction,
      Function<T, UUID> idExtractor) {

    return (request, deser) -> {
      // deserialize the input
      final T[] input;
      try {
        input = deser.readValue(request.getBody(), inputClass);
      } catch (IOException e) {
        return Response.clientError(
            "Could not deserialize input as array of " + inputClass.getName());
      }
      // invoke the storage function
      try {
        safeStorageFunction.accept(Arrays.asList(input));
      } catch(Exception ex) {
        return Response.serverError("Error storing array of " + inputClass.getName()
            + "; " + ex.getMessage());
      }
      // return the UUID's of the stored objects
      final Collection<UUID> storedIds = Arrays.stream(input)
          .map(idExtractor)
          .collect(Collectors.toList());
      return Response.success(storedIds);
    };
  }

}
