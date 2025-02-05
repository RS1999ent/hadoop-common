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
package org.apache.hadoop.fs.viewfs;

import java.net.URI;

import org.apache.hadoop.conf.Configuration;

/**
 * Utilities for config variables of the viewFs See {@link ViewFs}
 */
public class ConfigUtil {
  /**
   * Get the config variable prefix for the specified mount table
   * @param mountTableName - the name of the mount table
   * @return the config variable prefix for the specified mount table
   */
  public static String getConfigViewFsPrefix(final String mountTableName) {
    return Constants.CONFIG_VIEWFS_PREFIX + "." + mountTableName;
  }
  
  /**
   * Get the config variable prefix for the default mount table
   * @return the config variable prefix for the default mount table
   */
  public static String getConfigViewFsPrefix() {
    return 
      getConfigViewFsPrefix(Constants.CONFIG_VIEWFS_PREFIX_DEFAULT_MOUNT_TABLE);
  }
  
  /**
   * Add a link to the config for the specified mount table
   * @param conf - add the link to this conf
   * @param mountTableName
   * @param src - the src path name
   * @param target - the target URI link
   */
  public static void addLink(Configuration conf, final String mountTableName, 
      final String src, final URI target) {
    conf.set(getConfigViewFsPrefix(mountTableName) + "." +
        Constants.CONFIG_VIEWFS_LINK + "." + src, target.toString());  
  }
  
  /**
   * Add a link to the config for the default mount table
   * @param conf - add the link to this conf
   * @param src - the src path name
   * @param target - the target URI link
   */
  public static void addLink(final Configuration conf, final String src,
      final URI target) {
    addLink( conf, Constants.CONFIG_VIEWFS_DEFAULT_MOUNT_TABLE, 
        src, target);   
  }
}
