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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.stereotype.Component;

import javax.management.MalformedObjectNameException;
import java.net.MalformedURLException;

/**
 * Parameters
 * <ul>
 * <li>serviceUrl</li>
 * <li>objectName</li>
 * <li>attributeName</li>
 * <li>username</li>
 * <li>password</li>
 * <li>heading - number indicates after how many lines the heading should be outputted</li>
 * <li>interval - every how many seconds to query and output the value</li>
 * <li>timestamp (boolean) - output the time in milliseconds since start of the tool</li>
 * <li>unixtime (boolean) - output the time in milliseconds from begin of unix time</li>
 * </ul>
 *
 * @author Malte Franken
 */
@Component
public class JmxStatsCli implements CommandMarker {
  private final static Logger LOGGER = LoggerFactory.getLogger(JmxStatsCli.class);

  private static final String LONG_OPT_SERVICE_URL = "serviceUrl";
  private static final String OPT_SERVICE_URL = "s";
  private static final String HELP_SERVICE_URL = "The full JMX service URL";

  private static final String LONG_OPT_OBJECT_NAME = "objectName";
  private static final String OPT_OBJECT_NAME = "o";
  private static final String HELP_OBJECT_NAME = "The object name";

  private static final String LONG_OPT_ATTRIBUTE_NAME = "attributeName";
  private static final String OPT_ATTRIBUTE_NAME = "a";
  private static final String HELP_ATTRIBUTE_NAME = "The attribute name, or a list of comma-separated attribute names";

  private static final String LONG_OPT_USERNAME = "username";
  private static final String OPT_USERNAME = "u";
  private static final String HELP_USERNAME = "The username for JMX access";

  private static final String LONG_OPT_PASSWORD = "password";
  private static final String OPT_PASSWORD = "p";
  private static final String HELP_PASSWORD = "The password for JMX access";

  private static final String LONG_OPT_INTERVAL = "interval";
  private static final String OPT_INTERVAL = "i";
  private static final String HELP_INTERVAL = "Sampling interval in milliseconds (minimum is 250ms)";

  private static final String LONG_OPT_LINES = "lines";
  private static final String OPT_LINES = "l";
  private static final String HELP_LINES = "Number of samples between header lines";

  private static final String LONG_OPT_TIMESTAMP = "timestamp";
  private static final String OPT_TIMESTAMP = "t";
  private static final String HELP_TIMESTAMP = "Display a timestamp for each value";

  private static final String LONG_OPT_UNIXTIME = "unixtime";
  private static final String OPT_UNIXTIME = "unix";
  private static final String HELP_UNIXTIME = "Display the timestamp in unix time (milliseconds since 1 Jan 1970)";

  private static final String MAIN_BEAN_NAME = "jmxStats";

  @Autowired
  private ApplicationContext applicationContext;

  @CliCommand(value = "stats", help = "Collect statistics information from a JMX source")
  public void stats(@CliOption(key = {LONG_OPT_SERVICE_URL, OPT_SERVICE_URL}, mandatory = true, help = HELP_SERVICE_URL) String serviceUrl,
                    @CliOption(key = {LONG_OPT_OBJECT_NAME, OPT_OBJECT_NAME}, mandatory = true, help = HELP_OBJECT_NAME) String objectName,
                    @CliOption(key = {LONG_OPT_ATTRIBUTE_NAME, OPT_ATTRIBUTE_NAME}, mandatory = true, help = HELP_ATTRIBUTE_NAME) String attributeName,
                    @CliOption(key = {LONG_OPT_USERNAME, OPT_USERNAME}, mandatory = false, help = HELP_USERNAME, specifiedDefaultValue = "", unspecifiedDefaultValue = "") String username,
                    @CliOption(key = {LONG_OPT_PASSWORD, OPT_PASSWORD}, mandatory = false, help = HELP_PASSWORD, specifiedDefaultValue = "", unspecifiedDefaultValue = "") String password,
                    @CliOption(key = {LONG_OPT_INTERVAL, OPT_INTERVAL}, mandatory = false, help = HELP_INTERVAL, specifiedDefaultValue = "250", unspecifiedDefaultValue = "250") long interval,
                    @CliOption(key = {LONG_OPT_LINES, OPT_LINES}, mandatory = false, help = HELP_LINES, specifiedDefaultValue = "0", unspecifiedDefaultValue = "0") int linesHeading,
                    @CliOption(key = {LONG_OPT_TIMESTAMP, OPT_TIMESTAMP}, mandatory = false, help = HELP_TIMESTAMP, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") boolean showTimestamp,
                    @CliOption(key = {LONG_OPT_UNIXTIME, OPT_UNIXTIME}, mandatory = false, help = HELP_UNIXTIME, specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") boolean showUnixTime) {
    try {
      // a few more sanity checks
      if (interval < 250) {
        Output.OUT.println("Warning: interval value too small, setting to 250");
        interval = 250;
      }
      if (linesHeading < 0) {
        Output.OUT.println("Warning: lines value too small, setting to 0");
        interval = 0;
      }
      // finally, find and start the tool
      JmxStats jmxStats = (JmxStats) applicationContext.getBean(MAIN_BEAN_NAME);
      jmxStats.setServiceUrl(serviceUrl);
      jmxStats.setObjectName(objectName);
      jmxStats.setAttributeName(attributeName);
      jmxStats.setUsername(username);
      jmxStats.setPassword(password);
      jmxStats.setIntervalMilliseconds(interval);
      jmxStats.setLinesHeading(linesHeading);
      jmxStats.setShowTimestamp(showTimestamp);
      jmxStats.setShowUnixTime(showUnixTime);
      jmxStats.run();
    } catch (MalformedURLException e) {
      Output.OUT.println("Service URL incorrect: " + e.getMessage());
      LOGGER.error("Service URL malformed: " + serviceUrl, e);
    } catch (MalformedObjectNameException e) {
      Output.OUT.println("Object name incorrect: " + e.getMessage());
      LOGGER.error("Object name malformed: " + objectName, e);
    } catch (Exception e) {
      Output.OUT.println("Unexpected error occurred: " + e.getMessage());
      LOGGER.error("Unexpected error occurred", e);
    }
  }

}
