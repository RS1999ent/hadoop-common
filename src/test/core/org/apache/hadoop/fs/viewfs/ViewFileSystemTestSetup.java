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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileSystemTestHelper;
import org.apache.hadoop.fs.FsConstants;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.viewfs.ConfigUtil;


/**
 * This class is for  setup and teardown for viewFileSystem so that
 * it can be tested via the standard FileSystem tests.
 * 
 * If tests launched via ant (build.xml) the test root is absolute path
 * If tests launched via eclipse, the test root is 
 * is a test dir below the working directory. (see FileSystemTestHelper).
 * Since viewFs has no built-in wd, its wd is /user/<username>.
 * 
 * We set a viewFileSystems with mount point for 
 * /<firstComponent>" pointing to the target fs's  testdir 
 */
public class ViewFileSystemTestSetup {

  /**
   * 
   * @param fsTarget - the target fs of the view fs.
   * @return return the ViewFS File context to be used for tests
   * @throws Exception
   */
  static public FileSystem setupForViewFs(FileSystem fsTarget) throws Exception {
    /**
     * create the test root on local_fs - the  mount table will point here
     */
    Configuration conf = configWithViewfsScheme();
    Path targetOfTests = FileSystemTestHelper.getTestRootPath(fsTarget);
    // In case previous test was killed before cleanup
    fsTarget.delete(targetOfTests, true);
    
    fsTarget.mkdirs(targetOfTests);
  
    String srcTestFirstDir;
    if (FileSystemTestHelper.TEST_ROOT_DIR.startsWith("/")) {
      int indexOf2ndSlash = FileSystemTestHelper.TEST_ROOT_DIR.indexOf('/', 1);
      srcTestFirstDir = FileSystemTestHelper.TEST_ROOT_DIR.substring(0, indexOf2ndSlash);
    } else {
      srcTestFirstDir = "/user"; 
  
    }
    //System.out.println("srcTestFirstDir=" + srcTestFirstDir);
  
    // Set up the defaultMT in the config with mount point links
    // The test dir is root is below  /user/<userid>

    ConfigUtil.addLink(conf, srcTestFirstDir,
        targetOfTests.toUri());
    
    FileSystem fsView = FileSystem.get(FsConstants.VIEWFS_URI, conf);
    //System.out.println("SRCOfTests = "+ getTestRootPath(fs, "test"));
    //System.out.println("TargetOfTests = "+ targetOfTests.toUri());
    return fsView;
  }

  /**
   * 
   * delete the test directory in the target  fs
   */
  static public void tearDownForViewFs(FileSystem fsTarget) throws Exception {
    Path targetOfTests = FileSystemTestHelper.getTestRootPath(fsTarget);
    fsTarget.delete(targetOfTests, true);
  }
  
  public static Configuration configWithViewfsScheme() {
    Configuration conf = new Configuration();
    conf.set("fs.viewfs.impl", ViewFileSystem.class.getName());
    return conf; 
  }
}
