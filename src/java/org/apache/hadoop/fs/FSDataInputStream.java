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
package org.apache.hadoop.fs;

import java.io.*;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;

/** Utility that wraps a {@link FSInputStream} in a {@link DataInputStream}
 * and buffers input through a {@link BufferedInputStream}. */
@InterfaceAudience.Public
@InterfaceStability.Stable
public class FSDataInputStream extends DataInputStream
    implements Seekable, PositionedReadable, Closeable {

  public FSDataInputStream(InputStream in)
    throws IOException {
    super(in);
    if( !(in instanceof Seekable) || !(in instanceof PositionedReadable) ) {
      throw new IllegalArgumentException(
          "In is not an instance of Seekable or PositionedReadable");
    }
  }
  
  public synchronized void seek(long desired) throws IOException {
    ((Seekable)in).seek(desired);
  }

  public long getPos() throws IOException {
    return ((Seekable)in).getPos();
  }
  
  public int read(long position, byte[] buffer, int offset, int length)
    throws IOException {
    return ((PositionedReadable)in).read(position, buffer, offset, length);
  }
  
  public void readFully(long position, byte[] buffer, int offset, int length)
    throws IOException {
    ((PositionedReadable)in).readFully(position, buffer, offset, length);
  }
  
  public void readFully(long position, byte[] buffer)
    throws IOException {
    ((PositionedReadable)in).readFully(position, buffer, 0, buffer.length);
  }
  
  public boolean seekToNewSource(long targetPos) throws IOException {
    return ((Seekable)in).seekToNewSource(targetPos); 
  }
  
  // Returns the underlying input stream. This is used by unit tests.
  public InputStream getWrappedStream() {
    return in;
  }
}
