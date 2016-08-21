/*
 * Copyright 2012-present Facebook, Inc.
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

package com.facebook.buck.rules;

import com.facebook.buck.android.AndroidAarDescription;
import com.facebook.buck.android.AndroidBinaryDescription;
import com.facebook.buck.android.AndroidBuckConfig;
import com.facebook.buck.android.AndroidBuildConfigDescription;
import com.facebook.buck.android.AndroidDirectoryResolver;
import com.facebook.buck.android.AndroidInstrumentationApkDescription;
import com.facebook.buck.android.AndroidInstrumentationTestDescription;
import com.facebook.buck.android.AndroidLibraryDescription;
import com.facebook.buck.android.AndroidManifestDescription;
import com.facebook.buck.android.AndroidPrebuiltAarDescription;
import com.facebook.buck.android.AndroidResourceDescription;
import com.facebook.buck.android.ApkGenruleDescription;
import com.facebook.buck.android.GenAidlDescription;
import com.facebook.buck.android.NdkCxxPlatform;
import com.facebook.buck.android.NdkCxxPlatformCompiler;
import com.facebook.buck.android.NdkCxxPlatforms;
import com.facebook.buck.android.NdkLibraryDescription;
import com.facebook.buck.android.PrebuiltNativeLibraryDescription;
import com.facebook.buck.android.ProGuardConfig;
import com.facebook.buck.android.RobolectricTestDescription;
import com.facebook.buck.android.SmartDexingStep;
import com.facebook.buck.apple.AppleAssetCatalogDescription;
import com.facebook.buck.apple.AppleBinaryDescription;
import com.facebook.buck.apple.AppleBundleDescription;
import com.facebook.buck.apple.AppleConfig;
import com.facebook.buck.apple.AppleCxxPlatform;
import com.facebook.buck.apple.AppleCxxPlatforms;
import com.facebook.buck.apple.AppleLibraryDescription;
import com.facebook.buck.apple.ApplePackageDescription;
import com.facebook.buck.apple.AppleResourceDescription;
import com.facebook.buck.apple.AppleSdk;
import com.facebook.buck.apple.AppleSdkDiscovery;
import com.facebook.buck.apple.AppleSdkPaths;
import com.facebook.buck.apple.AppleTestDescription;
import com.facebook.buck.apple.AppleToolchain;
import com.facebook.buck.apple.AppleToolchainDiscovery;
import com.facebook.buck.apple.CodeSignIdentityStore;
import com.facebook.buck.apple.CoreDataModelDescription;
import com.facebook.buck.apple.PrebuiltAppleFrameworkDescription;
import com.facebook.buck.apple.ProvisioningProfileStore;
import com.facebook.buck.apple.XcodePostbuildScriptDescription;
import com.facebook.buck.apple.XcodePrebuildScriptDescription;
import com.facebook.buck.apple.XcodeWorkspaceConfigDescription;
import com.facebook.buck.cli.BuckConfig;
import com.facebook.buck.cli.DownloadConfig;
import com.facebook.buck.cxx.CxxBinaryDescription;
import com.facebook.buck.cxx.CxxBuckConfig;
import com.facebook.buck.cxx.CxxGenruleDescription;
import com.facebook.buck.cxx.CxxLibraryDescription;
import com.facebook.buck.cxx.CxxPlatform;
import com.facebook.buck.cxx.CxxPlatforms;
import com.facebook.buck.cxx.CxxTestDescription;
import com.facebook.buck.cxx.DefaultCxxPlatforms;
import com.facebook.buck.cxx.InferBuckConfig;
import com.facebook.buck.cxx.PrebuiltCxxLibraryDescription;
import com.facebook.buck.d.DBinaryDescription;
import com.facebook.buck.d.DBuckConfig;
import com.facebook.buck.d.DLibraryDescription;
import com.facebook.buck.d.DTestDescription;
import com.facebook.buck.dotnet.CSharpLibraryDescription;
import com.facebook.buck.dotnet.PrebuiltDotNetLibraryDescription;
import com.facebook.buck.file.Downloader;
import com.facebook.buck.file.ExplodingDownloader;
import com.facebook.buck.file.RemoteFileDescription;
import com.facebook.buck.file.StackedDownloader;
import com.facebook.buck.go.GoBinaryDescription;
import com.facebook.buck.go.GoBuckConfig;
import com.facebook.buck.go.GoLibraryDescription;
import com.facebook.buck.go.GoTestDescription;
import com.facebook.buck.groups.TargetGroupDescription;
import com.facebook.buck.gwt.GwtBinaryDescription;
import com.facebook.buck.halide.HalideBuckConfig;
import com.facebook.buck.halide.HalideLibraryDescription;
import com.facebook.buck.haskell.HaskellBinaryDescription;
import com.facebook.buck.haskell.HaskellBuckConfig;
import com.facebook.buck.haskell.HaskellLibraryDescription;
import com.facebook.buck.haskell.PrebuiltHaskellLibraryDescription;
import com.facebook.buck.io.ExecutableFinder;
import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.js.AndroidReactNativeLibraryDescription;
import com.facebook.buck.js.IosReactNativeLibraryDescription;
import com.facebook.buck.js.ReactNativeBuckConfig;
import com.facebook.buck.jvm.groovy.GroovyBuckConfig;
import com.facebook.buck.jvm.groovy.GroovyLibraryDescription;
import com.facebook.buck.jvm.groovy.GroovyTestDescription;
import com.facebook.buck.jvm.java.JavaBinaryDescription;
import com.facebook.buck.jvm.java.JavaBuckConfig;
import com.facebook.buck.jvm.java.JavaLibraryDescription;
import com.facebook.buck.jvm.java.JavaOptions;
import com.facebook.buck.jvm.java.JavaTestDescription;
import com.facebook.buck.jvm.java.JavacOptions;
import com.facebook.buck.jvm.java.KeystoreDescription;
import com.facebook.buck.jvm.java.PrebuiltJarDescription;
import com.facebook.buck.jvm.kotlin.config.KotlinBuckConfig;
import com.facebook.buck.jvm.kotlin.KotlinLibraryDescription;
import com.facebook.buck.jvm.kotlin.KotlinTestDescription;
import com.facebook.buck.jvm.scala.ScalaBuckConfig;
import com.facebook.buck.jvm.scala.ScalaLibraryDescription;
import com.facebook.buck.jvm.scala.ScalaTestDescription;
import com.facebook.buck.log.CommandThreadFactory;
import com.facebook.buck.log.Logger;
import com.facebook.buck.lua.CxxLuaExtensionDescription;
import com.facebook.buck.lua.LuaBinaryDescription;
import com.facebook.buck.lua.LuaBuckConfig;
import com.facebook.buck.lua.LuaConfig;
import com.facebook.buck.lua.LuaLibraryDescription;
import com.facebook.buck.model.Flavor;
import com.facebook.buck.model.FlavorDomain;
import com.facebook.buck.model.ImmutableFlavor;
import com.facebook.buck.ocaml.OCamlBinaryDescription;
import com.facebook.buck.ocaml.OCamlBuckConfig;
import com.facebook.buck.ocaml.OCamlLibraryDescription;
import com.facebook.buck.ocaml.PrebuiltOCamlLibraryDescription;
import com.facebook.buck.python.CxxPythonExtensionDescription;
import com.facebook.buck.python.PrebuiltPythonLibraryDescription;
import com.facebook.buck.python.PythonBinaryDescription;
import com.facebook.buck.python.PythonBuckConfig;
import com.facebook.buck.python.PythonLibraryDescription;
import com.facebook.buck.python.PythonPlatform;
import com.facebook.buck.python.PythonTestDescription;
import com.facebook.buck.rust.RustBinaryDescription;
import com.facebook.buck.rust.RustBuckConfig;
import com.facebook.buck.rust.RustLibraryDescription;
import com.facebook.buck.shell.ExportFileDescription;
import com.facebook.buck.shell.GenruleDescription;
import com.facebook.buck.shell.ShBinaryDescription;
import com.facebook.buck.shell.ShTestDescription;
import com.facebook.buck.shell.WorkerToolDescription;
import com.facebook.buck.swift.SwiftLibraryDescription;
import com.facebook.buck.thrift.ThriftBuckConfig;
import com.facebook.buck.thrift.ThriftCxxEnhancer;
import com.facebook.buck.thrift.ThriftJavaEnhancer;
import com.facebook.buck.thrift.ThriftLibraryDescription;
import com.facebook.buck.thrift.ThriftPythonEnhancer;
import com.facebook.buck.util.HumanReadableException;
import com.facebook.buck.util.ProcessExecutor;
import com.facebook.buck.util.environment.Platform;
import com.facebook.buck.zip.ZipDescription;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

/**
 * A registry of all the build rules types understood by Buck.
 */
public class KnownBuildRuleTypes {

  private static final Logger LOG = Logger.get(KnownBuildRuleTypes.class);
  private final ImmutableMap<BuildRuleType, Description<?>> descriptions;
  private final ImmutableMap<String, BuildRuleType> types;
  private final FlavorDomain<CxxPlatform> cxxPlatforms;
  private final CxxPlatform defaultCxxPlatforms;

  private KnownBuildRuleTypes(
      Map<BuildRuleType, Description<?>> descriptions,
      Map<String, BuildRuleType> types,
      FlavorDomain<CxxPlatform> cxxPlatforms,
      CxxPlatform defaultCxxPlatforms) {
    this.descriptions = ImmutableMap.copyOf(descriptions);
    this.types = ImmutableMap.copyOf(types);
    this.cxxPlatforms = cxxPlatforms;
    this.defaultCxxPlatforms = defaultCxxPlatforms;
  }

  public BuildRuleType getBuildRuleType(String named) {
    BuildRuleType type = types.get(named);
    if (type == null) {
      throw new HumanReadableException("Unable to find build rule type: " + named);
    }
    return type;
  }

  public Description<?> getDescription(BuildRuleType buildRuleType) {
    Description<?> description = descriptions.get(buildRuleType);
    if (description == null) {
      throw new HumanReadableException(
          "Unable to find description for build rule type: " + buildRuleType);
    }
    return description;
  }

  public ImmutableSet<Description<?>> getAllDescriptions() {
    return ImmutableSet.copyOf(descriptions.values());
  }

  public FlavorDomain<CxxPlatform> getCxxPlatforms() {
    return cxxPlatforms;
  }

  public CxxPlatform getDefaultCxxPlatforms() {
    return defaultCxxPlatforms;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static KnownBuildRuleTypes createInstance(
      BuckConfig config,
      ProcessExecutor processExecutor,
      AndroidDirectoryResolver androidDirectoryResolver) throws InterruptedException, IOException {
    return createBuilder(
        config,
        processExecutor,
        androidDirectoryResolver).build();
  }

  private static ImmutableList<AppleCxxPlatform> buildAppleCxxPlatforms(
      Supplier<Optional<Path>> appleDeveloperDirectorySupplier,
      ImmutableList<Path> extraToolchainPaths,
      ImmutableList<Path> extraPlatformPaths,
      BuckConfig buckConfig,
      AppleConfig appleConfig,
      ProcessExecutor processExecutor)
      throws IOException {
    Optional<Path> appleDeveloperDirectory = appleDeveloperDirectorySupplier.get();
    if (appleDeveloperDirectory.isPresent() &&
        !Files.isDirectory(appleDeveloperDirectory.get())) {
      LOG.error(
        "Developer directory is set to %s, but is not a directory",
        appleDeveloperDirectory.get());
      return ImmutableList.of();
    }

    ImmutableList.Builder<AppleCxxPlatform> appleCxxPlatformsBuilder = ImmutableList.builder();
    ImmutableMap<String, AppleToolchain> toolchains =
        AppleToolchainDiscovery.discoverAppleToolchains(
            appleDeveloperDirectory,
            extraToolchainPaths);

    ImmutableMap<AppleSdk, AppleSdkPaths> sdkPaths = AppleSdkDiscovery.discoverAppleSdkPaths(
        appleDeveloperDirectory,
        extraPlatformPaths,
        toolchains,
        appleConfig);

    for (Map.Entry<AppleSdk, AppleSdkPaths> entry : sdkPaths.entrySet()) {
      AppleSdk sdk = entry.getKey();
      AppleSdkPaths appleSdkPaths = entry.getValue();
      String targetSdkVersion = appleConfig.getTargetSdkVersion(
          sdk.getApplePlatform()).or(sdk.getVersion());
      LOG.debug("SDK %s using default version %s", sdk, targetSdkVersion);
      for (String architecture : sdk.getArchitectures()) {
        AppleCxxPlatform appleCxxPlatform = AppleCxxPlatforms.build(
            sdk,
            targetSdkVersion,
            architecture,
            appleSdkPaths,
            buckConfig,
            appleConfig,
            Optional.of(processExecutor));
        appleCxxPlatformsBuilder.add(appleCxxPlatform);
      }
    }
    return appleCxxPlatformsBuilder.build();
  }

  @VisibleForTesting
  static Builder createBuilder(
      BuckConfig config,
      ProcessExecutor processExecutor,
      AndroidDirectoryResolver androidDirectoryResolver) throws InterruptedException, IOException {

    Platform platform = Platform.detect();

    AndroidBuckConfig androidConfig = new AndroidBuckConfig(config, platform);
    Optional<String> ndkVersion = androidConfig.getNdkVersion();
    // If a NDK version isn't specified, we've got to reach into the runtime environment to find
    // out which one we will end up using.
    if (!ndkVersion.isPresent()) {
      ndkVersion = androidDirectoryResolver.getNdkVersion();
    }

    AppleConfig appleConfig = new AppleConfig(config);

    ImmutableList<AppleCxxPlatform> appleCxxPlatforms = buildAppleCxxPlatforms(
        appleConfig.getAppleDeveloperDirectorySupplier(processExecutor),
        appleConfig.getExtraToolchainPaths(),
        appleConfig.getExtraPlatformPaths(),
        config,
        appleConfig,
        processExecutor);
    FlavorDomain<AppleCxxPlatform> platformFlavorsToAppleCxxPlatforms =
        FlavorDomain.from("Apple C++ Platform", appleCxxPlatforms);

    CxxBuckConfig cxxBuckConfig = new CxxBuckConfig(config);

    // Setup the NDK C/C++ platforms.
    Optional<Path> ndkRoot = androidDirectoryResolver.getNdkOrAbsent();
    ImmutableMap.Builder<NdkCxxPlatforms.TargetCpuType, NdkCxxPlatform> ndkCxxPlatformsBuilder =
        ImmutableMap.builder();
    if (ndkRoot.isPresent()) {
      NdkCxxPlatformCompiler.Type compilerType =
          androidConfig.getNdkCompiler().or(NdkCxxPlatforms.DEFAULT_COMPILER_TYPE);
      String gccVersion = androidConfig.getNdkGccVersion().or(NdkCxxPlatforms.DEFAULT_GCC_VERSION);
      NdkCxxPlatformCompiler compiler =
          NdkCxxPlatformCompiler.builder()
              .setType(compilerType)
              .setVersion(
                  compilerType == NdkCxxPlatformCompiler.Type.GCC ?
                      gccVersion :
                      androidConfig.getNdkClangVersion().or(NdkCxxPlatforms.DEFAULT_CLANG_VERSION))
              .setGccVersion(gccVersion)
              .build();
      ndkCxxPlatformsBuilder.putAll(
          NdkCxxPlatforms.getPlatforms(
              cxxBuckConfig,
              new ProjectFilesystem(ndkRoot.get()),
              compiler,
              androidConfig.getNdkCxxRuntime().or(NdkCxxPlatforms.DEFAULT_CXX_RUNTIME),
              androidConfig.getNdkAppPlatform().or(NdkCxxPlatforms.DEFAULT_TARGET_APP_PLATFORM),
              androidConfig.getNdkCpuAbis().or(NdkCxxPlatforms.DEFAULT_CPU_ABIS),
              platform));
    }
    ImmutableMap<NdkCxxPlatforms.TargetCpuType, NdkCxxPlatform> ndkCxxPlatforms =
        ndkCxxPlatformsBuilder.build();

    // Create a map of system platforms.
    ImmutableMap.Builder<Flavor, CxxPlatform> cxxSystemPlatformsBuilder = ImmutableMap.builder();

    // If an Android NDK is present, add platforms for that.  This is mostly useful for
    // testing our Android NDK support for right now.
    for (NdkCxxPlatform ndkCxxPlatform : ndkCxxPlatforms.values()) {
      cxxSystemPlatformsBuilder.put(
          ndkCxxPlatform.getCxxPlatform().getFlavor(),
          ndkCxxPlatform.getCxxPlatform());
    }

    for (AppleCxxPlatform appleCxxPlatform : platformFlavorsToAppleCxxPlatforms.getValues()) {
      cxxSystemPlatformsBuilder.put(
          appleCxxPlatform.getCxxPlatform().getFlavor(),
          appleCxxPlatform.getCxxPlatform());
    }

    CxxPlatform defaultHostCxxPlatform = DefaultCxxPlatforms.build(platform, cxxBuckConfig);
    cxxSystemPlatformsBuilder.put(defaultHostCxxPlatform.getFlavor(), defaultHostCxxPlatform);
    ImmutableMap<Flavor, CxxPlatform> cxxSystemPlatformsMap = cxxSystemPlatformsBuilder.build();

    // Add the host platform if needed (for example, when building on Linux).
    Flavor hostFlavor = CxxPlatforms.getHostFlavor();
    if (!cxxSystemPlatformsMap.containsKey(hostFlavor)) {
      cxxSystemPlatformsBuilder.put(
          hostFlavor,
          CxxPlatform.builder()
              .from(defaultHostCxxPlatform)
              .setFlavor(hostFlavor)
              .build());
      cxxSystemPlatformsMap = cxxSystemPlatformsBuilder.build();
    }

    // Add platforms for each cxx flavor obtained from the buck config files
    // from sections of the form cxx#{flavor name}.
    // These platforms are overrides for existing system platforms.
    ImmutableList<ImmutableFlavor> possibleHostFlavors = CxxPlatforms.getAllPossibleHostFlavors();
    HashMap<Flavor, CxxPlatform> cxxOverridePlatformsMap =
        new HashMap<Flavor, CxxPlatform>(cxxSystemPlatformsMap);
    ImmutableSet<Flavor> cxxFlavors = CxxBuckConfig.getCxxFlavors(config);
    for (Flavor flavor: cxxFlavors) {
      if (!cxxSystemPlatformsMap.containsKey(flavor)) {
        if (possibleHostFlavors.contains(flavor)) {
            // If a flavor is for an alternate host, it's safe to skip.
            continue;
        }
        throw new HumanReadableException(
            "Could not find platform for which overrides were specified: " + flavor);
      }

      cxxOverridePlatformsMap.put(flavor, CxxPlatforms.copyPlatformWithFlavorAndConfig(
          cxxOverridePlatformsMap.get(flavor),
          new CxxBuckConfig(config, flavor),
          flavor));
    }

    // Finalize our "default" host.
    // TODO(Ktwu) The host flavor should default to a concrete flavor
    // like "linux-x86_64", not "default".
    hostFlavor = DefaultCxxPlatforms.FLAVOR;
    Optional<String> hostCxxPlatformOverride = cxxBuckConfig.getHostPlatform();
    if (hostCxxPlatformOverride.isPresent()) {
      Flavor overrideFlavor = ImmutableFlavor.of(hostCxxPlatformOverride.get());
      if (cxxOverridePlatformsMap.containsKey(overrideFlavor)) {
        hostFlavor = overrideFlavor;
      }
    }
    CxxPlatform hostCxxPlatform = CxxPlatform.builder()
        .from(cxxOverridePlatformsMap.get(hostFlavor))
        .setFlavor(DefaultCxxPlatforms.FLAVOR)
        .build();
    cxxOverridePlatformsMap.put(DefaultCxxPlatforms.FLAVOR, hostCxxPlatform);

    ImmutableMap<Flavor, CxxPlatform> cxxPlatformsMap = ImmutableMap
        .<Flavor, CxxPlatform>builder()
        .putAll(cxxOverridePlatformsMap)
        .build();

    ExecutableFinder executableFinder = new ExecutableFinder();

    // Build up the final list of C/C++ platforms.
    FlavorDomain<CxxPlatform> cxxPlatforms = new FlavorDomain<>(
        "C/C++ platform",
        cxxPlatformsMap);

    // Get the default target platform from config.
    CxxPlatform defaultCxxPlatform = CxxPlatforms.getConfigDefaultCxxPlatform(
        cxxBuckConfig,
        cxxPlatformsMap,
        hostCxxPlatform);

    DBuckConfig dBuckConfig = new DBuckConfig(config);

    ReactNativeBuckConfig reactNativeBuckConfig = new ReactNativeBuckConfig(config);

    RustBuckConfig rustBuckConfig = new RustBuckConfig(config);

    GoBuckConfig goBuckConfig = new GoBuckConfig(config, processExecutor, cxxPlatforms);

    HalideBuckConfig halideBuckConfig = new HalideBuckConfig(config);

    ProGuardConfig proGuardConfig = new ProGuardConfig(config);

    PythonBuckConfig pyConfig = new PythonBuckConfig(config, executableFinder);
    ImmutableList<PythonPlatform> pythonPlatformsList =
        pyConfig.getPythonPlatforms(processExecutor);
    FlavorDomain<PythonPlatform> pythonPlatforms =
        FlavorDomain.from("Python Platform", pythonPlatformsList);
    PythonBinaryDescription pythonBinaryDescription =
        new PythonBinaryDescription(
            pyConfig,
            pythonPlatforms,
            cxxBuckConfig,
            defaultCxxPlatform,
            cxxPlatforms);

    // Look up the timeout to apply to entire test rules.
    Optional<Long> defaultTestRuleTimeoutMs = config.getLong("test", "rule_timeout");


    // Prepare the downloader if we're allowing mid-build downloads
    Downloader downloader;
    DownloadConfig downloadConfig = new DownloadConfig(config);
    if (downloadConfig.isDownloadAtRuntimeOk()) {
      downloader = StackedDownloader.createFromConfig(
          config,
          androidDirectoryResolver.getSdkOrAbsent());
    } else {
      // Or just set one that blows up
      downloader = new ExplodingDownloader();
    }

    Builder builder = builder();

    JavaBuckConfig javaConfig = new JavaBuckConfig(config);
    JavacOptions defaultJavacOptions = javaConfig.getDefaultJavacOptions();
    JavaOptions defaultJavaOptions = javaConfig.getDefaultJavaOptions();

    KotlinBuckConfig kotlinBuckConfig = new KotlinBuckConfig(config);

    ScalaBuckConfig scalaConfig = new ScalaBuckConfig(config);

    InferBuckConfig inferBuckConfig = new InferBuckConfig(config);

    LuaConfig luaConfig = new LuaBuckConfig(config, executableFinder);

    CxxBinaryDescription cxxBinaryDescription =
        new CxxBinaryDescription(
            cxxBuckConfig,
            inferBuckConfig,
            defaultCxxPlatform,
            cxxPlatforms);

    CxxLibraryDescription cxxLibraryDescription =
        new CxxLibraryDescription(
            cxxBuckConfig,
            defaultCxxPlatform,
            inferBuckConfig,
            cxxPlatforms);

    CodeSignIdentityStore codeSignIdentityStore =
        CodeSignIdentityStore.fromSystem(processExecutor);
    ProvisioningProfileStore provisioningProfileStore =
        ProvisioningProfileStore.fromSearchPath(appleConfig.getProvisioningProfileSearchPath());

    AppleLibraryDescription appleLibraryDescription =
        new AppleLibraryDescription(
            cxxLibraryDescription,
            platformFlavorsToAppleCxxPlatforms,
            defaultCxxPlatform,
            codeSignIdentityStore,
            provisioningProfileStore,
            appleConfig.getDefaultDebugInfoFormatForLibraries());
    builder.register(appleLibraryDescription);
    PrebuiltAppleFrameworkDescription appleFrameworkDescription =
        new PrebuiltAppleFrameworkDescription();
    builder.register(appleFrameworkDescription);

    AppleBinaryDescription appleBinaryDescription =
        new AppleBinaryDescription(
            cxxBinaryDescription,
            platformFlavorsToAppleCxxPlatforms,
            codeSignIdentityStore,
            provisioningProfileStore,
            appleConfig.getDefaultDebugInfoFormatForBinaries());
    builder.register(appleBinaryDescription);

    SwiftLibraryDescription swiftLibraryDescription =
        new SwiftLibraryDescription(
            cxxPlatforms,
            platformFlavorsToAppleCxxPlatforms,
            defaultCxxPlatform);
    builder.register(swiftLibraryDescription);

    HaskellBuckConfig haskellBuckConfig = new HaskellBuckConfig(config, executableFinder);
    builder.register(new HaskellLibraryDescription(haskellBuckConfig, cxxBuckConfig, cxxPlatforms));
    builder.register(
        new HaskellBinaryDescription(haskellBuckConfig, cxxPlatforms, defaultCxxPlatform));
    builder.register(new PrebuiltHaskellLibraryDescription());

    // Create an executor service exclusively for the smart dexing step.
    ListeningExecutorService dxExecutorService =
        MoreExecutors.listeningDecorator(
            Executors.newFixedThreadPool(
                javaConfig.getDxThreadCount().or(SmartDexingStep.determineOptimalThreadCount()),
                new CommandThreadFactory("SmartDexing")));


    builder.register(
        new AndroidAarDescription(
            new AndroidManifestDescription(),
            cxxBuckConfig,
            ndkCxxPlatforms));
    builder.register(
        new AndroidBinaryDescription(
            defaultJavaOptions,
            defaultJavacOptions,
            proGuardConfig,
            ndkCxxPlatforms,
            dxExecutorService,
            cxxBuckConfig));
    builder.register(new AndroidBuildConfigDescription(defaultJavacOptions));
    builder.register(
        new AndroidInstrumentationApkDescription(
            proGuardConfig,
            defaultJavacOptions,
            ndkCxxPlatforms,
            dxExecutorService,
            cxxBuckConfig));
    builder.register(new AndroidInstrumentationTestDescription(
        defaultJavaOptions,
        defaultTestRuleTimeoutMs));
    builder.register(new AndroidLibraryDescription(defaultJavacOptions));
    builder.register(new AndroidManifestDescription());
    builder.register(new AndroidPrebuiltAarDescription(defaultJavacOptions));
    builder.register(new AndroidReactNativeLibraryDescription(reactNativeBuckConfig));
    builder.register(new AndroidResourceDescription());
    builder.register(new ApkGenruleDescription());
    builder.register(new AppleAssetCatalogDescription());
    builder.register(
        new ApplePackageDescription(
            appleConfig,
            defaultCxxPlatform,
            platformFlavorsToAppleCxxPlatforms));
    AppleBundleDescription appleBundleDescription =
        new AppleBundleDescription(
            appleBinaryDescription,
            appleLibraryDescription,
            cxxPlatforms,
            platformFlavorsToAppleCxxPlatforms,
            defaultCxxPlatform,
            codeSignIdentityStore,
            provisioningProfileStore,
            appleConfig.getDefaultDebugInfoFormatForBinaries());
    builder.register(appleBundleDescription);
    builder.register(new AppleResourceDescription());
    builder.register(
        new AppleTestDescription(
            appleConfig,
            appleLibraryDescription,
            cxxPlatforms,
            platformFlavorsToAppleCxxPlatforms,
            defaultCxxPlatform,
            codeSignIdentityStore,
            provisioningProfileStore,
            appleConfig.getAppleDeveloperDirectorySupplierForTests(processExecutor),
            appleConfig.getDefaultDebugInfoFormatForTests(),
            defaultTestRuleTimeoutMs));
    builder.register(new CoreDataModelDescription());
    builder.register(new CSharpLibraryDescription());
    builder.register(cxxBinaryDescription);
    builder.register(cxxLibraryDescription);
    builder.register(new CxxGenruleDescription(cxxPlatforms));
    builder.register(new CxxLuaExtensionDescription(luaConfig, cxxBuckConfig, cxxPlatforms));
    builder.register(
        new CxxPythonExtensionDescription(pythonPlatforms, cxxBuckConfig, cxxPlatforms));
    builder.register(
        new CxxTestDescription(
            cxxBuckConfig,
            defaultCxxPlatform,
            cxxPlatforms,
            defaultTestRuleTimeoutMs));
    builder.register(new DBinaryDescription(dBuckConfig, cxxBuckConfig, defaultCxxPlatform));
    builder.register(new DLibraryDescription(dBuckConfig, cxxBuckConfig, defaultCxxPlatform));
    builder.register(
        new DTestDescription(
            dBuckConfig,
            cxxBuckConfig,
            defaultCxxPlatform,
            defaultTestRuleTimeoutMs));
    builder.register(new ExportFileDescription());
    builder.register(new GenruleDescription());
    builder.register(new GenAidlDescription());
    builder.register(new GoBinaryDescription(goBuckConfig));
    builder.register(new GoLibraryDescription(goBuckConfig));
    builder.register(
        new GoTestDescription(
            goBuckConfig,
            defaultTestRuleTimeoutMs));
    GroovyBuckConfig groovyBuckConfig = new GroovyBuckConfig(config);
    builder.register(
        new GroovyLibraryDescription(
            groovyBuckConfig,
            defaultJavacOptions));
    builder.register(
        new GroovyTestDescription(
            groovyBuckConfig,
            defaultJavaOptions,
            defaultJavacOptions,
            defaultTestRuleTimeoutMs)
    );
    builder.register(new GwtBinaryDescription(defaultJavaOptions));
    builder.register(
      new HalideLibraryDescription(
          cxxBuckConfig,
          defaultCxxPlatform,
          cxxPlatforms,
          halideBuckConfig));
    builder.register(new IosReactNativeLibraryDescription(reactNativeBuckConfig));
    builder.register(new JavaBinaryDescription(
        defaultJavaOptions,
        defaultJavacOptions,
        defaultCxxPlatform));
    builder.register(new JavaLibraryDescription(defaultJavacOptions));
    builder.register(
        new JavaTestDescription(
            defaultJavaOptions,
            defaultJavacOptions,
            defaultTestRuleTimeoutMs,
            defaultCxxPlatform));
    builder.register(new KeystoreDescription());
    builder.register(
        new KotlinLibraryDescription(kotlinBuckConfig));
    builder.register(
        new KotlinTestDescription(
            kotlinBuckConfig,
            defaultJavaOptions,
            defaultJavacOptions,
            defaultTestRuleTimeoutMs));
    builder.register(
        new LuaBinaryDescription(
            luaConfig,
            cxxBuckConfig,
            defaultCxxPlatform,
            cxxPlatforms,
            pythonPlatforms));
    builder.register(new LuaLibraryDescription());
    builder.register(new NdkLibraryDescription(ndkVersion, ndkCxxPlatforms));
    OCamlBuckConfig ocamlBuckConfig = new OCamlBuckConfig(platform, config);
    builder.register(new OCamlBinaryDescription(ocamlBuckConfig));
    builder.register(new OCamlLibraryDescription(ocamlBuckConfig));
    builder.register(new PrebuiltCxxLibraryDescription(cxxBuckConfig, cxxPlatforms));
    builder.register(new PrebuiltDotNetLibraryDescription());
    builder.register(new PrebuiltJarDescription());
    builder.register(new PrebuiltNativeLibraryDescription());
    builder.register(new PrebuiltOCamlLibraryDescription());
    builder.register(new PrebuiltPythonLibraryDescription());
    builder.register(new ProjectConfigDescription());
    builder.register(pythonBinaryDescription);
    builder.register(new PythonLibraryDescription());
    builder.register(
        new PythonTestDescription(
            pythonBinaryDescription,
            pyConfig,
            pythonPlatforms,
            cxxBuckConfig,
            defaultCxxPlatform,
            defaultTestRuleTimeoutMs,
            cxxPlatforms));
    builder.register(new RemoteFileDescription(downloader));
    builder.register(new RobolectricTestDescription(
            defaultJavaOptions,
            defaultJavacOptions,
            defaultTestRuleTimeoutMs,
            defaultCxxPlatform));
    builder.register(new RustBinaryDescription(rustBuckConfig));
    builder.register(new RustLibraryDescription(rustBuckConfig));
    builder.register(new ScalaLibraryDescription(scalaConfig));
    builder.register(new ScalaTestDescription(
        scalaConfig,
        defaultJavaOptions,
        defaultTestRuleTimeoutMs,
        defaultCxxPlatform));
    builder.register(new ShBinaryDescription());
    builder.register(new ShTestDescription(defaultTestRuleTimeoutMs));
    ThriftBuckConfig thriftBuckConfig = new ThriftBuckConfig(config);
    builder.register(
        new ThriftLibraryDescription(
            thriftBuckConfig,
            ImmutableList.of(
                new ThriftJavaEnhancer(thriftBuckConfig, defaultJavacOptions),
                new ThriftCxxEnhancer(
                    thriftBuckConfig,
                    cxxLibraryDescription,
                    /* cpp2 */ false),
                new ThriftCxxEnhancer(
                    thriftBuckConfig,
                    cxxLibraryDescription,
                    /* cpp2 */ true),
                new ThriftPythonEnhancer(thriftBuckConfig, ThriftPythonEnhancer.Type.NORMAL),
                new ThriftPythonEnhancer(thriftBuckConfig, ThriftPythonEnhancer.Type.TWISTED),
                new ThriftPythonEnhancer(thriftBuckConfig, ThriftPythonEnhancer.Type.ASYNCIO))));
    builder.register(new WorkerToolDescription());
    builder.register(new XcodePostbuildScriptDescription());
    builder.register(new XcodePrebuildScriptDescription());
    builder.register(new XcodeWorkspaceConfigDescription());
    builder.register(new ZipDescription());
    builder.register(new TargetGroupDescription());

    builder.setCxxPlatforms(cxxPlatforms);
    builder.setDefaultCxxPlatform(defaultCxxPlatform);

    return builder;
  }

  public static class Builder {
    private final Map<BuildRuleType, Description<?>> descriptions;
    private final Map<String, BuildRuleType> types;

    @Nullable
    private FlavorDomain<CxxPlatform> cxxPlatforms;
    @Nullable
    private CxxPlatform defaultCxxPlatform;

    protected Builder() {
      this.descriptions = Maps.newConcurrentMap();
      this.types = Maps.newConcurrentMap();
    }

    public Builder register(Description<?> description) {
      BuildRuleType type = description.getBuildRuleType();
      types.put(type.getName(), type);
      descriptions.put(type, description);
      return this;
    }

    public Builder setCxxPlatforms(FlavorDomain<CxxPlatform> cxxPlatforms) {
      this.cxxPlatforms = cxxPlatforms;
      return this;
    }

    public Builder setDefaultCxxPlatform(CxxPlatform defaultCxxPlatform) {
      this.defaultCxxPlatform = defaultCxxPlatform;
      return this;
    }

    public KnownBuildRuleTypes build() {
      return new KnownBuildRuleTypes(
          descriptions,
          types,
          Preconditions.checkNotNull(cxxPlatforms),
          Preconditions.checkNotNull(defaultCxxPlatform));
    }
  }
}
