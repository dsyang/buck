/*
 * Copyright 2016-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.buck.jvm.kotlin;

import static org.junit.Assert.assertEquals;

import com.facebook.buck.cli.BuckConfig;
import com.facebook.buck.cli.FakeBuckConfig;
import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.model.Either;
import com.facebook.buck.rules.PathSourcePath;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.testutil.integration.TemporaryPaths;
import com.facebook.buck.util.HumanReadableException;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class KotlinBuckConfigTest {

  @Rule public TemporaryPaths tmp = new TemporaryPaths();

  private ProjectFilesystem defaultFilesystem;
  private Path fakeKotlinHome;

  @Before
  public void setUp() throws InterruptedException, IOException {
    KotlinTestAssumptions.assumeUnixLike();

    fakeKotlinHome = tmp.newFolder("kotlin_home");
    tmp.newFolder("kotlin_home/bin");
    tmp.newFolder("kotlin_home/lib");


    defaultFilesystem = new ProjectFilesystem(tmp.getRoot());
  }

  @Test
  public void testFindsKotlinCompilerInPath() throws HumanReadableException, IOException {
    Path kotlincPath = tmp.newExecutableFile("kotlinc");

    BuckConfig buckConfig = FakeBuckConfig.builder()
        .setSections(
            ImmutableMap.of("kotlin", ImmutableMap.of("external", "true")))
        .setEnvironment(
            ImmutableMap.of("PATH", kotlincPath.getParent().toString()))
        .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    String command = kotlinBuckConfig.getCompilerPath().toString();
    assertEquals(command, kotlincPath.toString());
  }

  @Test
  public void testFindsKotlinCompilerInHome() throws HumanReadableException, IOException {
    Path kotlinCompiler = tmp.newExecutableFile(fakeKotlinHome + "/bin/kotlinc");

    BuckConfig buckConfig = FakeBuckConfig.builder()
        .setSections(
            ImmutableMap.of("kotlin", ImmutableMap.of("external", "true")))
        .setEnvironment(
          ImmutableMap.of("KOTLIN_HOME", fakeKotlinHome.toString()))
        .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    String command = kotlinBuckConfig.getCompilerPath().toString();
    assertEquals(command, kotlinCompiler.toString());
  }

  @Test
  public void testFindsKotlinCompilerInConfigWithAbsolutePath()
      throws HumanReadableException, IOException {
    Path kotlinCompiler = tmp.newExecutableFile("kotlinc");

    BuckConfig buckConfig = FakeBuckConfig.builder()
        .setSections(ImmutableMap.of(
            "kotlin", ImmutableMap.of("compiler", kotlinCompiler.toString(),
                                      "external", "true")))
        .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    String command = kotlinBuckConfig.getCompilerPath().toString();
    assertEquals(command, kotlinCompiler.toString());
  }

  @Test
  public void testFindsKotlinCompilerInConfigWithRelativePath()
      throws HumanReadableException, InterruptedException, IOException {
    tmp.newFolder("randomFolder");
    Path kotlinCompiler = tmp.newExecutableFile("randomFolder/kotlinc");

    BuckConfig buckConfig = FakeBuckConfig.builder()
        .setFilesystem(defaultFilesystem)
        .setSections(ImmutableMap.of(
            "kotlin", ImmutableMap.of("compiler", "randomFolder/kotlinc",
                                      "external", "true")))
        .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    String command = kotlinBuckConfig.getCompilerPath().toString();
    assertEquals(command, kotlinCompiler.toString());
  }

  @Test
  public void testAssumesKotlinHomeIfCompilerInBinFolder() throws HumanReadableException, IOException {

  }

  @Test
  public void testFindsKotlinRuntimeLibraryInLibFolder() throws HumanReadableException, IOException {
    Path kotlinCompiler = tmp.newExecutableFile(fakeKotlinHome + "/bin/kotlinc");
    Path kotlinRuntime = tmp.newExecutableFile(fakeKotlinHome + "/lib/kotlin-runtime.jar");

    BuckConfig buckConfig = FakeBuckConfig.builder()
        .setSections(
            ImmutableMap.of("kotlin", ImmutableMap.of("external", "true")))
        .setEnvironment(
            ImmutableMap.of("PATH",
                kotlinCompiler.getParent().toString()))
        .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    Either<SourcePath, Path> runtimeJar = kotlinBuckConfig.getPathToRuntimeJar();
    assertEquals(runtimeJar.getRight().toString(), kotlinRuntime.normalize().toString());
  }

  @Test
  public void testFindsKotlinRuntimeLibraryInKotlinHome()
      throws HumanReadableException, IOException {
    Path kotlinCompiler = tmp.newExecutableFile(fakeKotlinHome + "/bin/kotlinc");
    Path kotlinRuntime = tmp.newExecutableFile(fakeKotlinHome + "/kotlin-runtime.jar");

    BuckConfig buckConfig = FakeBuckConfig.builder()
        .setSections(
            ImmutableMap.of("kotlin", ImmutableMap.of("external", "true")))
        .setEnvironment(
            ImmutableMap.of("PATH",
                kotlinCompiler.getParent().toString()))
        .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    Either<SourcePath, Path> runtimeJar = kotlinBuckConfig.getPathToRuntimeJar();
    assertEquals(runtimeJar.getRight().toString(), kotlinRuntime.normalize().toString());
  }

  @Test
  public void testFindsKotlinRuntimeInConfigWithAbsolutePath()
      throws HumanReadableException, IOException {
    Path kotlinRuntime = tmp.newExecutableFile(fakeKotlinHome + "/lib/kotlin-runtime.jar");

    BuckConfig buckConfig = FakeBuckConfig.builder()
        .setFilesystem(defaultFilesystem)
        .setSections(ImmutableMap.of(
            "kotlin",
            ImmutableMap.of("runtime_jar", kotlinRuntime.normalize().toString(),
                            "external", "true")))
        .setEnvironment(
            ImmutableMap.of("KOTLIN_HOME", fakeKotlinHome.normalize().toString()))
        .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    SourcePath runtimeJar = kotlinBuckConfig.getPathToRuntimeJar().getLeft();
    assertEquals(runtimeJar.toString(), kotlinRuntime.toString());
  }

  @Test
  public void testFindsKotlinRuntimeInConfigWithRelativePath()
      throws HumanReadableException, InterruptedException, IOException {
    tmp.newFolder("randomFolder");
    Path kotlinCompiler = tmp.newExecutableFile("randomFolder/kotlinc");
    Path kotlinRuntime = tmp.newExecutableFile("randomFolder/kotlin-runtime.jar");

    BuckConfig buckConfig = FakeBuckConfig.builder()
        .setFilesystem(defaultFilesystem)
        .setSections(ImmutableMap.of(
            "kotlin",
            ImmutableMap.of("runtime_jar", "randomFolder/kotlin-runtime.jar",
                            "external", "true")))
        .setEnvironment(ImmutableMap.of())
        .build();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(buckConfig);
    PathSourcePath runtimeJar = (PathSourcePath) kotlinBuckConfig.getPathToRuntimeJar().getLeft();
    assertEquals(runtimeJar.getRelativePath().toString(), "randomFolder/kotlin-runtime.jar");
  }
}
