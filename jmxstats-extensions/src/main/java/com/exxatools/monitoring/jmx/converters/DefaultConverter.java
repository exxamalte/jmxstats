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

/**
 * The default converter that simply calls #toString() on the incoming value.
 *
 * @author Malte Franken
 */
public class DefaultConverter implements Converter<Object> {

  /**
   * This converter can convert everything.
   *
   * @param sourceClass the source class to check
   * @return <code>true</code>
   */
  public boolean canConvert(Class<?> sourceClass) {
    return true;
  }

  public String convert(Object value) {
    return value.toString();
  }
}
