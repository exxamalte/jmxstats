/*
 * Copyright 2014 Malte Franken (http://exxatools.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exxatools.monitoring.jmx.converters;

import org.springframework.beans.factory.annotation.Required;

import java.util.Map;

/**
 * This converter converts a map into a readable format by getting key and value for each entry in the map. The key will
 * be transformed using the configured {@link #keyConverter} and the value will be transformed using the configured
 * {@link #valueConverter}. The transformed key-value-pairs are then concatenated. The whole map is surrounded by [...] brackets.
 *
 * @author Malte Franken
 */
public class GenericMapConverter implements Converter<Map<Object, Object>> {

  private Converter<Object> keyConverter;
  private Converter<Object> valueConverter;
  private String keyValueSeparator = "=";
  private String entrySeparator = ",";

  @Required
  public void setKeyConverter(Converter<Object> keyConverter) {
    this.keyConverter = keyConverter;
  }

  @Required
  public void setValueConverter(Converter<Object> valueConverter) {
    this.valueConverter = valueConverter;
  }

  public void setKeyValueSeparator(String keyValueSeparator) {
    this.keyValueSeparator = keyValueSeparator;
  }

  public void setEntrySeparator(String entrySeparator) {
    this.entrySeparator = entrySeparator;
  }

  public boolean canConvert(Class<?> sourceClass) {
    return Map.class.isAssignableFrom(sourceClass);
  }

  public String convert(Map<Object, Object> map) {
    StringBuilder builder = new StringBuilder("[");
    if (map != null) {
      for (Map.Entry<Object, Object> entry : map.entrySet()) {
        if (builder.length() > 1) {
          builder.append(entrySeparator);
        }
        String convertedKey = keyConverter.convert(entry.getKey());
        builder.append(convertedKey);
        String convertedValue = valueConverter.convert(entry.getValue());
        builder.append(keyValueSeparator).append(convertedValue);
      }
    }
    builder.append("]");
    return builder.toString();
  }
}
