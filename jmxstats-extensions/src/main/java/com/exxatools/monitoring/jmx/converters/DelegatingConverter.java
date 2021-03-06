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

import java.util.List;

/**
 * This converter is delegating to its configured {@link #converters}. The first converter that matches will be used.
 *
 * @author Malte Franken
 */
public class DelegatingConverter implements Converter<Object> {

  /**
   * The converters to delegate to.
   */
  private List<Converter> converters;

  @Required
  public void setConverters(List<Converter> converters) {
    this.converters = converters;
  }

  public boolean canConvert(Class<?> sourceClass) {
    return true;
  }

  /**
   * Convert the incoming value into a string.
   *
   * @param value the incoming attribute value
   * @return a string representation of the incoming value, or an empty string if the incoming value is <code>null</code>
   */
  public String convert(Object value) {
    if (value != null) {
      for (Converter converter : converters) {
        if (converter.canConvert(value.getClass())) {
          // the first converter that matches is used
          return converter.convert(value);
        }
      }
      // fallback if no converter could be found
      return value.toString();
    } else {
      // if the incoming value is *null* just return an empty string
      return "";
    }
  }
}
