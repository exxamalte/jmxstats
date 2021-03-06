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

package com.exxatools.monitoring.jmx;

import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Controls the output of a tool.
 *
 * @author Malte Franken
 */
public class Output {
  //public static final PrintWriter OUT = new PrintWriter(new OutputStreamWriter(System.out));
  public static final PrintStream OUT = System.out;

}
