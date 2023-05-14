package hu.aestallon.jsmol.marshaller;

import java.util.Map;

final class JsonMarshallers {

  static final StringMarshaller STRING_MARSHALLER         = new StringMarshaller();
  static final ObjectMarshaller<Object> OBJECT_MARSHALLER = new ObjectMarshaller<>(Object::new);

  private static final Map<Class<?>, JsonMarshaller<?>> BUILT_IN_MARSHALLERS = Map.of(
      String.class, STRING_MARSHALLER,
      Object.class, OBJECT_MARSHALLER
  );

  private JsonMarshallers() {}
}
