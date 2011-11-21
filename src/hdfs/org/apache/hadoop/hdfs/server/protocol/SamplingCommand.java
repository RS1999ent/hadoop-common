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
package org.apache.hadoop.hdfs.server.protocol;

import java.io.*;
import org.apache.hadoop.io.*;

/****************************************************
 * A SamplingCommand is an instruction to a datanode 
 * informing it of the request-sampling percentage.  This 
 * value is used by the @link{org.berkeley.xtrace} library 
 * to control the percentage of requests for which trace 
 * data is collected.  For example, if set to 10%, trace 
 * data for 10% of all requests serviced by the datanode 
 * will be sampled.
 ****************************************************/
public class SamplingCommand extends DatanodeCommand {

  private int samplingPercentage;

  public SamplingCommand() {
  }

  /**
   * Create a SamplingCommand for sending the current sampling percentage to
   * the datanodes.  
   * *
   * @param samplingPercentage: The sampling percentage to be sent
   */
  public SamplingCommand(int action, int samplingPercentage) {
    super(action);
    this.samplingPercentage = samplingPercentage;
  }

  /**
   * Accessor method for the sampling percentage
   * @return returns the sampling percentage to be used by datanodes
   */
  public int getSamplingPercentage() {
    return this.samplingPercentage;
  }

  ///////////////////////////////////////////
  // Writable
  ///////////////////////////////////////////
  static {                                      // register a ctor
    WritableFactories.setFactory(SamplingCommand.class,
            new WritableFactory() {

              @Override
              public Writable newInstance() {
                return new SamplingCommand();
              }
            });
  }

  /**
   * Writes this class's members to the Dataoutput stream
   * @param out: The dataoutput stream
   * @throws IOException 
   */
  @Override
  public void write(DataOutput out) throws IOException {
    super.write(out);
    out.writeInt(samplingPercentage);
  }

  /**
   * Reads this class's members from the DataInput stream
   * @param in: The DataInput stream
   * @throws IOException 
   */
  @Override
  public void readFields(DataInput in) throws IOException {
    super.readFields(in);
    this.samplingPercentage = in.readInt();
  }
}
