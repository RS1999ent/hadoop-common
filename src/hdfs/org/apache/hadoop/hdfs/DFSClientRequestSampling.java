/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.hdfs;

import edu.berkeley.xtrace.XTraceSampling;
import org.apache.commons.logging.*;

/**
 * Daemon thread that queries the namenode periodically for the current 
 * @link{edu.berkeley.xtrace} request-sampling percentage, which controls the 
 * percentage of requests for which XTrace data will be collected
 */
public class DFSClientRequestSampling implements Runnable {
  DFSClient client;
  public static final Log LOG = LogFactory.getLog(DFSClientRequestSampling.class);
  public static final long sleepInterval = 60 * 1000; // 60s
  
  public DFSClientRequestSampling(DFSClient client) {
    this.client = client;
  }
  
  public void StartServer() {
    Thread t = new Thread(this, "DFSClient Request Sampling");
    t.setDaemon(true);
    t.start();
  }
  
  @Override
  public void run() {
    while(true) {
      int oldValue = XTraceSampling.getSamplingPercentage();
      int value = client.getRequestSamplingPercentage();
      if (oldValue != value) {
        LOG.info("Setting request-sampling percentage to " + value +  "%");
        XTraceSampling.setSamplingPercentage(value);
      }
      try {
        Thread.sleep(sleepInterval); // sleep for a minute
      } catch (InterruptedException e) {}
    }
  }
}
