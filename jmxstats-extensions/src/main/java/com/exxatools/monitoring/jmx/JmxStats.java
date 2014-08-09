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

import com.exxatools.monitoring.jmx.converters.Converter;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

/**
 *
 * TODO
 * <ul>
 *   <li>comma-separated list of attribute names</li>
 *   <li>more converter</li>
 *   <li>Read URL from property files</li>
 *   <li>improve formatting for multiple values (fixed distance instead of just tabs)</li>
 * </ul>
 *
 * @author Malte Franken
 */
public class JmxStats {
  private final static Logger LOGGER = LoggerFactory.getLogger(JmxStats.class);

  /**
   * The minimum time between two statistic outputs in milliseconds.
   */
  public static final int MINIMUM_WAIT_BETWEEN_STATISTICS_OUTPUT = 250;

  private static final String SEPARATOR_CHAR = "\t";

  /**
   * Indicates whether the tool is started (true) or being shut down (false).
   */
  private boolean started = true;

  /**
   * The JMX connector.
   */
  private JMXConnector connector;

  /**
   * The MBean server connection.
   */
  private MBeanServerConnection connection;

  /**
   * The attribute value converter.
   */
  private Converter<Object> converter;

  /**
   * The current number of statistic outputs.
   */
  private int counter = 0;

  /**
   * The time in milliseconds when the tool was started up.
   */
  private long startTime = 0;

  /**
   * Contains the header for the statistic output.
   */
  private String header;

  /**
   * The JMX service URL to connect to.
   */
  private JMXServiceURL serviceUrl;

  /**
   * The JMX object name to read from.
   */
  private ObjectName objectName;

  /**
   * The attribute name to read.
   */
  private List<String> attributeNames;

  /**
   * The JMX username to connect with.
   */
  private String username;

  /**
   * The password of the JMX user.
   */
  private String password;

  /**
   * How often to fetch and output the JMX attribute. In milliseconds.
   */
  private long intervalMilliseconds;

  /**
   * How often to repeat outputting the heading. Default is <code>0</code> (=never repeated).
   */
  private int linesHeading = 0;

  /**
   * Whether or not to output a timestamp with each value. Default is <code>false</code>.
   */
  private boolean showTimestamp = false;

  private boolean showUnixTime = false;

  public JmxStats() {
  }

  public void setServiceUrl(String serviceUrl) throws MalformedURLException {
    this.serviceUrl = new JMXServiceURL(serviceUrl);
  }

  public void setObjectName(String objectName) throws MalformedObjectNameException {
    this.objectName = new ObjectName(objectName);
  }

  public void setAttributeName(String attributeName) {
    //this.attributeName = attributeName;
    this.attributeNames = Arrays.asList(StringUtils.split(attributeName, ","));
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setIntervalMilliseconds(long intervalMilliseconds) {
    this.intervalMilliseconds = intervalMilliseconds;
  }

  public void setLinesHeading(int linesHeading) {
    this.linesHeading = linesHeading;
  }

  public void setShowTimestamp(boolean showTimestamp) {
    this.showTimestamp = showTimestamp;
  }

  public void setShowUnixTime(boolean showUnixTime) {
    this.showUnixTime = showUnixTime;
  }

  @Required
  public void setConverter(Converter<Object> converter) {
    this.converter = converter;
  }

  /**
   * Fetch the current attribute values.
   *
   * @return the attribute values
   * @throws IOException
   * @throws InstanceNotFoundException
   * @throws ReflectionException
   * @throws AttributeNotFoundException
   * @throws MBeanException
   */
  protected List<Object> getAttributeValues() throws IOException, InstanceNotFoundException, ReflectionException, AttributeNotFoundException, MBeanException {
    List<Object> attributeValues = new ArrayList<Object>();
    for (String attributeName : attributeNames) {
      Object attributeValue = getConnection().getAttribute(objectName, attributeName);
      attributeValues.add(attributeValue);
    }
    return attributeValues;
  }

  /**
   * Get a MBean server connection. Either opens a new connection or returns an already opened connection.
   *
   * @return the opened connection
   * @throws IOException in case the connection cannot be established
   */
  protected MBeanServerConnection getConnection() throws IOException {
    if (connection == null) {
      Map<String, Object> environment = new HashMap<String,Object>();
      if( username != null && password != null ) {
        environment.put(JMXConnector.CREDENTIALS, new String[] {username, password});
      }
      connector = JMXConnectorFactory.connect(serviceUrl, environment);
      connection = connector.getMBeanServerConnection();
    }
    return connection;
  }

  /**
   * Shut down the tool, closes the JMX connection.
   *
   * @throws IOException in case something goes wrong while closing the JMX connection
   */
  protected void shutdown() throws IOException {
    started = false;
    if (connector != null) {
      LOGGER.info("Closed connection " + connector);
      connector.close();
    }
  }

  /**
   * Run the JMX statistics.
   *
   * @throws Exception in case statistics cannot be produced
   */
  protected void run() throws Exception {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        try {
          shutdown();
        } catch (IOException e) {
          LOGGER.error("Error while shutting down", e);
        }
        Output.OUT.println();
      }
    });

    outputHeader();
    if (intervalMilliseconds > 0) {
      // output statistics on a regular basis
      while (started) {
        long currentRun = System.currentTimeMillis();
        if (startTime == 0) {
          startTime = System.currentTimeMillis();
        }
        long timeSinceStart = currentRun - startTime;
        if (linesHeading > 0 && counter++ >= linesHeading) {
          // TODO: probably does not work as expected for linesHeading == 1
          outputHeader();
          counter = 0;
        }
        // get value and output it
        outputStatistics(timeSinceStart);
        // wait
        long timeTaken = System.currentTimeMillis() - currentRun;
        long timeToSleep = intervalMilliseconds - timeTaken;
        // if fetching the value takes longer than the desired interval, wait at least 250ms
        if (timeToSleep <= MINIMUM_WAIT_BETWEEN_STATISTICS_OUTPUT) {
          timeToSleep = MINIMUM_WAIT_BETWEEN_STATISTICS_OUTPUT;
        }
        Thread.sleep(timeToSleep);
      }
    } else {
      // just a one-off
      outputStatistics(0);
    }
  }

  /**
   * Output the header for the statistics.
   */
  protected void outputHeader() {
    if (header == null) {
    List<String> headerValues = new ArrayList<String>();
      if (showTimestamp) {
        headerValues.add("time");
      }
      for (String attributeName : attributeNames) {
        headerValues.add(attributeName);
      }
      header = StringUtils.join(headerValues, SEPARATOR_CHAR);
    }
    Output.OUT.println(header);
  }

  /**
   * Fetch a value for the statistics and output a new row.
   *
   * @param timeSinceStart the time in milliseconds since tool has been started
   * @throws IOException
   * @throws InstanceNotFoundException
   * @throws ReflectionException
   * @throws AttributeNotFoundException
   * @throws MBeanException
   */
  protected void outputStatistics(long timeSinceStart) throws IOException, InstanceNotFoundException, ReflectionException, AttributeNotFoundException, MBeanException {
    List<String> outputValues = new ArrayList<String>();
    if (showTimestamp) {
      if (showUnixTime) {
        outputValues.add(Long.toString(System.currentTimeMillis()));
      } else {
        outputValues.add(Long.toString(timeSinceStart));
      }
    }
    // read actual values
    List<Object> attributeValues = getAttributeValues();
    for (Object attributeValue : attributeValues) {
      // convert the value read
      String value = converter.convert(attributeValue);
      outputValues.add(value);
    }
    // and output the value
    String output = StringUtils.join(outputValues, SEPARATOR_CHAR);
    Output.OUT.println(output);
  }
}
