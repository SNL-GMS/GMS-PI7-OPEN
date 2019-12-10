package gms.shared.mechanisms.pluginregistry;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class TypeFactory {

  public static ParameterizedType createParameterizedType(Type ownerType, Type rawType,
      Type[] typeArguments) {
    return new ParameterizedType() {
      @Override
      public Type[] getActualTypeArguments() {
        return typeArguments;
      }

      @Override
      public Type getRawType() {
        return rawType;
      }

      @Override
      public Type getOwnerType() {
        return ownerType;
      }

      public boolean equals(Object o) {
        if (!(o instanceof ParameterizedType)) {
          return false;
        }

        ParameterizedType parameterizedType = (ParameterizedType) o;

        return Arrays.deepEquals(typeArguments, parameterizedType.getActualTypeArguments())
            && (rawType == null && parameterizedType.getRawType() == null || rawType
            .equals(parameterizedType.getRawType()))
            && (ownerType == null && parameterizedType.getOwnerType() == null || ownerType
            .equals(parameterizedType.getRawType()));
      }
    };
  }
}
