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
package org.apache.hadoop.hdfs.protocol;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import edu.berkeley.xtrace.*;

/**
 * 
 * The Client transfers data to/from datanode using a streaming protocol.
 *
 */
public interface DataTransferProtocol {
  
  
  /** Version for data transfers between clients and datanodes
   * This should change when serialization of DatanodeInfo, not just
   * when protocol changes. It is not very obvious. 
   */
  /*
   * Version 15:
   * A heartbeat is sent from the client to pipeline and then acked back
   */
  public static final int DATA_TRANSFER_VERSION = 15;

  // Processed at datanode stream-handler
  public static final byte OP_WRITE_BLOCK = (byte) 80;
  public static final byte OP_READ_BLOCK = (byte) 81;
  public static final byte OP_READ_METADATA = (byte) 82;
  public static final byte OP_REPLACE_BLOCK = (byte) 83;
  public static final byte OP_COPY_BLOCK = (byte) 84;
  public static final byte OP_BLOCK_CHECKSUM = (byte) 85;
  
  public static final int OP_STATUS_SUCCESS = 0;  
  public static final int OP_STATUS_ERROR = 1;  
  public static final int OP_STATUS_ERROR_CHECKSUM = 2;  
  public static final int OP_STATUS_ERROR_INVALID = 3;  
  public static final int OP_STATUS_ERROR_EXISTS = 4;  
  public static final int OP_STATUS_CHECKSUM_OK = 5;  

  /* seqno for a heartbeat packet */
  public static final int HEARTBEAT_SEQNO = -1;

  /** reply **/
  public static class PipelineAck {
    private long seqno;
    private short replies[];
    final public static long UNKOWN_SEQNO = -2; 
    //ww2
    public XTraceMetadata metadata = null;

    /** default constructor **/
    public PipelineAck() {
    }

    /**
     * Constructor
     * @param seqno sequence number
     * @param replies an array of replies
     */
    public PipelineAck(long seqno, short[] replies) {
      this.seqno = seqno;
      this.replies = replies;
    }

    /**
     * Get the sequence number
     * @return the sequence number
     */
    public long getSeqno() {
      return seqno;
    }

    /**
     * Get the number of replies
     * @return the number of replies
     */
    public short getNumOfReplies() {
      return (short)replies.length;
    }


    /**
     * get the ith reply
     * @return the the ith reply
     */
    public short getReply(int i) {
      return replies[i];
    }

    /**
     * Check if this ack contains error status
     * @return true if all statuses are SUCCESS
     */
    public boolean isSuccess() {
      for (short reply : replies) {
        if (reply != OP_STATUS_SUCCESS) {
          return false;
        }
      }
      return true;
    }

    public void readFields(DataInput in, int numRepliesExpected)
      throws IOException {
      assert numRepliesExpected > 0;

      seqno = in.readLong();
      replies = new short[numRepliesExpected];
      for (int i=0; i < replies.length; i++) {
    	  replies[i] = in.readShort();
      }
      
      //ww2
      int len = in.readInt();
      if (len > 0) {
        byte[] md = new byte[len];
        in.readFully(md);
        metadata = XTraceMetadata.createFromBytes(md, 0, len);
      } else
        metadata = null;
    }

    public void write(DataOutput out) throws IOException {
      out.writeLong(seqno);
      for(short reply : replies) {
        out.writeShort(reply);
      }
      
      //ww2
      if (metadata == null)
        out.writeInt(0);
      else {
        byte[] md = metadata.pack();
        out.writeInt(md.length);
        out.write(md);
      }
    }

    @Override //Object
    public String toString() {
      StringBuilder ack = new StringBuilder("Replies for seqno ");
      ack.append( seqno ).append( " are" );
      for(short reply : replies) {
        ack.append(" ");
        if (reply == OP_STATUS_SUCCESS) {
          ack.append("SUCCESS");
        } else {
          ack.append("FAILED");
        }
      }
      return ack.toString();
    }
  }
}
