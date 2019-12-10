package gms.shared.frameworks.swagger;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.time.Instant;
import java.util.Iterator;

public class JavaInstantSwaggerConverter implements ModelConverter {

  @Override
  public Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
    if (type.isSchemaProperty()) {
      JavaType javaType = Json.mapper().constructType(type.getType());
      if (javaType != null) {
        Class<?> cls = javaType.getRawClass();
        if (Instant.class.isAssignableFrom(cls)) {
          return new DateTimeSchema();
        }
      }
    }
    if (chain.hasNext()) {
      return chain.next().resolve(type, context, chain);
    } else {
      return null;
    }
  }
}