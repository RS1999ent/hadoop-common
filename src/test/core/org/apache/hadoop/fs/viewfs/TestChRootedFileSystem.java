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

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileSystemTestHelper;
import org.apache.hadoop.fs.FsConstants;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.viewfs.ChRootedFileSystem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestChRootedFileSystem {
  FileSystem fSys; // The ChRoootedFs
  FileSystem fSysTarget; //
  Path chrootedTo;

  @Before
  public void setUp() throws Exception {
    // create the test root on local_fs
    Configuration conf = new Configuration();
    fSysTarget = FileSystem.getLocal(conf);
    chrootedTo = FileSystemTestHelper.getAbsoluteTestRootPath(fSysTarget);
    // In case previous test was killed before cleanup
    fSysTarget.delete(chrootedTo, true);
    
    fSysTarget.mkdirs(chrootedTo);


    // ChRoot to the root of the testDirectory
    fSys = new ChRootedFileSystem(fSysTarget, chrootedTo);
  }

  @After
  public void tearDown() throws Exception {
    fSysTarget.delete(chrootedTo, true);
  }
  
  @Test
  public void testURI() {
    URI uri = fSys.getUri();
    Assert.assertEquals(chrootedTo.toUri(), uri);
  }
  
  @Test
  public void testBasicPaths() {
    URI uri = fSys.getUri();
    Assert.assertEquals(chrootedTo.toUri(), uri);
    Assert.assertEquals(fSys.makeQualified(
        new Path("/user/" + System.getProperty("user.name"))),
        fSys.getWorkingDirectory());
    Assert.assertEquals(fSys.makeQualified(
        new Path("/user/" + System.getProperty("user.name"))),
        fSys.getHomeDirectory());
    /*
     * ChRootedFs as its uri like file:///chrootRoot.
     * This is questionable since path.makequalified(uri, path) ignores
     * the pathPart of a uri. So our notion of chrooted URI is questionable.
     * But if we were to fix Path#makeQualified() then  the next test should
     *  have been:

    Assert.assertEquals(
        new Path(chrootedTo + "/foo/bar").makeQualified(
            FsConstants.LOCAL_FS_URI, null),
        fSys.makeQualified(new Path( "/foo/bar")));
    */
    
    Assert.assertEquals(
        new Path("/foo/bar").makeQualified(FsConstants.LOCAL_FS_URI, null),
        fSys.makeQualified(new Path("/foo/bar")));
  }
  
  /** 
   * Test modify operations (create, mkdir, delete, etc) 
   * 
   * Verify the operation via chrootedfs (ie fSys) and *also* via the
   *  target file system (ie fSysTarget) that has been chrooted.
   */
  @Test
  public void testCreateDelete() throws IOException {
    

    // Create file 
    FileSystemTestHelper.createFile(fSys, "/foo");
    Assert.assertTrue(fSys.isFile(new Path("/foo")));
    Assert.assertTrue(fSysTarget.isFile(new Path(chrootedTo, "foo")));
    
    // Create file with recursive dir
    FileSystemTestHelper.createFile(fSys, "/newDir/foo");
    Assert.assertTrue(fSys.isFile(new Path("/newDir/foo")));
    Assert.assertTrue(fSysTarget.isFile(new Path(chrootedTo,"newDir/foo")));
    
    // Delete the created file
    Assert.assertTrue(fSys.delete(new Path("/newDir/foo"), false));
    Assert.assertFalse(fSys.exists(new Path("/newDir/foo")));
    Assert.assertFalse(fSysTarget.exists(new Path(chrootedTo, "newDir/foo")));
    
    // Create file with a 2 component dirs recursively
    FileSystemTestHelper.createFile(fSys, "/newDir/newDir2/foo");
    Assert.assertTrue(fSys.isFile(new Path("/newDir/newDir2/foo")));
    Assert.assertTrue(fSysTarget.isFile(new Path(chrootedTo,"newDir/newDir2/foo")));
    
    // Delete the created file
    Assert.assertTrue(fSys.delete(new Path("/newDir/newDir2/foo"), false));
    Assert.assertFalse(fSys.exists(new Path("/newDir/newDir2/foo")));
    Assert.assertFalse(fSysTarget.exists(new Path(chrootedTo,"newDir/newDir2/foo")));
  }
  
  
  @Test
  public void testMkdirDelete() throws IOException {
    fSys.mkdirs(FileSystemTestHelper.getTestRootPath(fSys, "/dirX"));
    Assert.assertTrue(fSys.isDirectory(new Path("/dirX")));
    Assert.assertTrue(fSysTarget.isDirectory(new Path(chrootedTo,"dirX")));
    
    fSys.mkdirs(FileSystemTestHelper.getTestRootPath(fSys, "/dirX/dirY"));
    Assert.assertTrue(fSys.isDirectory(new Path("/dirX/dirY")));
    Assert.assertTrue(fSysTarget.isDirectory(new Path(chrootedTo,"dirX/dirY")));
    

    // Delete the created dir
    Assert.assertTrue(fSys.delete(new Path("/dirX/dirY"), false));
    Assert.assertFalse(fSys.exists(new Path("/dirX/dirY")));
    Assert.assertFalse(fSysTarget.exists(new Path(chrootedTo,"dirX/dirY")));
    
    Assert.assertTrue(fSys.delete(new Path("/dirX"), false));
    Assert.assertFalse(fSys.exists(new Path("/dirX")));
    Assert.assertFalse(fSysTarget.exists(new Path(chrootedTo,"dirX")));
    
  }
  @Test
  public void testRename() throws IOException {
    // Rename a file
    FileSystemTestHelper.createFile(fSys, "/newDir/foo");
    fSys.rename(new Path("/newDir/foo"), new Path("/newDir/fooBar"));
    Assert.assertFalse(fSys.exists(new Path("/newDir/foo")));
    Assert.assertFalse(fSysTarget.exists(new Path(chrootedTo,"newDir/foo")));
    Assert.assertTrue(fSys.isFile(FileSystemTestHelper.getTestRootPath(fSys,"/newDir/fooBar")));
    Assert.assertTrue(fSysTarget.isFile(new Path(chrootedTo,"newDir/fooBar")));
    
    
    // Rename a dir
    fSys.mkdirs(new Path("/newDir/dirFoo"));
    fSys.rename(new Path("/newDir/dirFoo"), new Path("/newDir/dirFooBar"));
    Assert.assertFalse(fSys.exists(new Path("/newDir/dirFoo")));
    Assert.assertFalse(fSysTarget.exists(new Path(chrootedTo,"newDir/dirFoo")));
    Assert.assertTrue(fSys.isDirectory(FileSystemTestHelper.getTestRootPath(fSys,"/newDir/dirFooBar")));
    Assert.assertTrue(fSysTarget.isDirectory(new Path(chrootedTo,"newDir/dirFooBar")));
  }
  
  
  /**
   * We would have liked renames across file system to fail but 
   * Unfortunately there is not way to distinguish the two file systems 
   * @throws IOException
   */
  @Test
  public void testRenameAcrossFs() throws IOException {
    fSys.mkdirs(new Path("/newDir/dirFoo"));
    fSys.rename(new Path("/newDir/dirFoo"), new Path("file:///tmp/dirFooBar"));
    FileSystemTestHelper.isDir(fSys, new Path("/tmp/dirFooBar"));
  }
 
  
  
  
  @Test
  public void testList() throws IOException {
    
    FileStatus fs = fSys.getFileStatus(new Path("/"));
    Assert.assertTrue(fs.isDirectory());
    //  should return the full path not the chrooted path
    Assert.assertEquals(fs.getPath(), chrootedTo);
    
    // list on Slash
    
    FileStatus[] dirPaths = fSys.listStatus(new Path("/"));

    Assert.assertEquals(0, dirPaths.length);
    
    

    FileSystemTestHelper.createFile(fSys, "/foo");
    FileSystemTestHelper.createFile(fSys, "/bar");
    fSys.mkdirs(new Path("/dirX"));
    fSys.mkdirs(FileSystemTestHelper.getTestRootPath(fSys, "/dirY"));
    fSys.mkdirs(new Path("/dirX/dirXX"));
    
    dirPaths = fSys.listStatus(new Path("/"));
    Assert.assertEquals(4, dirPaths.length); // note 2 crc files
    
    // Note the the file status paths are the full paths on target
    fs = FileSystemTestHelper.containsPath(new Path(chrootedTo, "foo"), dirPaths);
      Assert.assertNotNull(fs);
      Assert.assertTrue(fs.isFile());
    fs = FileSystemTestHelper.containsPath(new Path(chrootedTo, "bar"), dirPaths);
      Assert.assertNotNull(fs);
      Assert.assertTrue(fs.isFile());
    fs = FileSystemTestHelper.containsPath(new Path(chrootedTo, "dirX"), dirPaths);
      Assert.assertNotNull(fs);
      Assert.assertTrue(fs.isDirectory());
    fs = FileSystemTestHelper.containsPath(new Path(chrootedTo, "dirY"), dirPaths);
      Assert.assertNotNull(fs);
      Assert.assertTrue(fs.isDirectory());
  }
  
  @Test
  public void testWorkingDirectory() throws Exception {

    // First we cd to our test root
    fSys.mkdirs(new Path("/testWd"));
    Path workDir = new Path("/testWd");
    fSys.setWorkingDirectory(workDir);
    Assert.assertEquals(workDir, fSys.getWorkingDirectory());

    fSys.setWorkingDirectory(new Path("."));
    Assert.assertEquals(workDir, fSys.getWorkingDirectory());

    fSys.setWorkingDirectory(new Path(".."));
    Assert.assertEquals(workDir.getParent(), fSys.getWorkingDirectory());
    
    // cd using a relative path

    // Go back to our test root
    workDir = new Path("/testWd");
    fSys.setWorkingDirectory(workDir);
    Assert.assertEquals(workDir, fSys.getWorkingDirectory());
    
    Path relativeDir = new Path("existingDir1");
    Path absoluteDir = new Path(workDir,"existingDir1");
    fSys.mkdirs(absoluteDir);
    fSys.setWorkingDirectory(relativeDir);
    Assert.assertEquals(absoluteDir, fSys.getWorkingDirectory());
    // cd using a absolute path
    absoluteDir = new Path("/test/existingDir2");
    fSys.mkdirs(absoluteDir);
    fSys.setWorkingDirectory(absoluteDir);
    Assert.assertEquals(absoluteDir, fSys.getWorkingDirectory());
    
    // Now open a file relative to the wd we just set above.
    Path absoluteFooPath = new Path(absoluteDir, "foo");
    fSys.create(absoluteFooPath).close();
    fSys.open(new Path("foo")).close();
    
    // Now mkdir relative to the dir we cd'ed to
    fSys.mkdirs(new Path("newDir"));
    Assert.assertTrue(fSys.isDirectory(new Path(absoluteDir, "newDir")));

    /* Filesystem impls (RawLocal and DistributedFileSystem do not check
     * for existing of working dir
    absoluteDir = getTestRootPath(fSys, "nonexistingPath");
    try {
      fSys.setWorkingDirectory(absoluteDir);
      Assert.fail("cd to non existing dir should have failed");
    } catch (Exception e) {
      // Exception as expected
    }
    */
    
    // Try a URI
    final String LOCAL_FS_ROOT_URI = "file:///tmp/test";
    absoluteDir = new Path(LOCAL_FS_ROOT_URI + "/existingDir");
    fSys.mkdirs(absoluteDir);
    fSys.setWorkingDirectory(absoluteDir);
    Assert.assertEquals(absoluteDir, fSys.getWorkingDirectory());

  }
 
}
