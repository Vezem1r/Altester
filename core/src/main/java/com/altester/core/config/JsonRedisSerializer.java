package com.altester.core.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class JsonRedisSerializer<T> implements RedisSerializer<T> {

  private final ObjectMapper objectMapper;
  private final Class<T> targetType;

  public JsonRedisSerializer(Class<T> targetType) {
    this.targetType = targetType;
    this.objectMapper = createObjectMapper();
  }

  protected ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    mapper.disableDefaultTyping();
    return mapper;
  }

  @Override
  public byte[] serialize(T t) throws SerializationException {
    if (t == null) {
      return new byte[0];
    }
    try {
      return objectMapper.writeValueAsBytes(t);
    } catch (Exception ex) {
      throw new SerializationException("Could not serialize object: " + ex.getMessage(), ex);
    }
  }

  @Override
  public T deserialize(byte[] bytes) throws SerializationException {
    if (bytes == null || bytes.length == 0) {
      return null;
    }
    try {
      return objectMapper.readValue(bytes, targetType);
    } catch (Exception ex) {
      throw new SerializationException("Could not deserialize object: " + ex.getMessage(), ex);
    }
  }
}
