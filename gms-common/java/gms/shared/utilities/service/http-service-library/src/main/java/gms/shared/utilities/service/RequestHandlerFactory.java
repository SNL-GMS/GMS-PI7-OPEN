package gms.shared.utilities.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RequestHandlerFactory {

  public static <InputType> RequestHandler<Collection<UUID>> storageHandler(
      Class<InputType[]> inputClass,
      Consumer<Collection<InputType>> safeStorageFunction,
      Function<InputType, UUID> idExtractor) {

    return (request, deser) -> {
      // deserialize the input
      final InputType[] input;
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
