/*
 * Copyright 2017-present Facebook, Inc.
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

import com.facebook.buck.jvm.java.CompileToJarStepFactory;
import com.facebook.buck.jvm.java.DefaultJavaLibraryBuilder;
import com.facebook.buck.jvm.java.JavaLibraryDescription;
import com.facebook.buck.parser.NoSuchBuildTargetException;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.BuildTargetSourcePath;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.util.HumanReadableException;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import java.util.Optional;


public class DefaultKotlinLibraryBuilder extends DefaultJavaLibraryBuilder {
  private final KotlinBuckConfig kotlinBuckConfig;
  private ImmutableList<String> extraKotlincArguments = ImmutableList.of();

  public DefaultKotlinLibraryBuilder(
      BuildRuleParams params,
      BuildRuleResolver buildRuleResolver,
      KotlinBuckConfig kotlinBuckConfig) {
    super(params, buildRuleResolver);
    this.kotlinBuckConfig = kotlinBuckConfig;
  }

  @Override
  public DefaultKotlinLibraryBuilder setArgs(JavaLibraryDescription.CoreArg args) {
    super.setArgs(args);

    KotlinLibraryDescription.CoreArg kotlinArgs = (KotlinLibraryDescription.CoreArg) args;
    extraKotlincArguments = kotlinArgs.getExtraKotlincArguments();
    return this;
  }

  @Override
  protected BuilderHelper newHelper() {
    return new BuilderHelper();
  }

  protected class BuilderHelper extends DefaultJavaLibraryBuilder.BuilderHelper {
    @Override
    protected DefaultKotlinLibrary build() throws NoSuchBuildTargetException {
      return new DefaultKotlinLibrary(
          getFinalParams(),
          sourcePathResolver,
          ruleFinder,
          srcs,
          resources,
          generatedSourceFolder,
          proguardConfig,
          postprocessClassesCommands,
          getFinalFullJarDeclaredDeps(),
          fullJarExportedDeps,
          fullJarProvidedDeps,
          getFinalCompileTimeClasspathSourcePaths(),
          getAbiInputs(),
          getAbiJar(),
          trackClassUsage,
          getCompileStepFactory(),
          resourcesRoot,
          manifestFile,
          mavenCoords,
          tests,
          classesToRemoveFromJar);
    }

    @Override
    protected CompileToJarStepFactory buildCompileStepFactory() {
      Kotlinc kotlinc = Preconditions.checkNotNull(kotlinBuckConfig).getKotlinc();
      ImmutableCollection<SourcePath> inputs = kotlinc.getInputs();
      for (SourcePath path : inputs) {
        BuildTargetSourcePath<?> btsp = (BuildTargetSourcePath<?>) path;
        Optional<BuildRule> ruleOptional = buildRuleResolver.getRuleOptional(btsp.getTarget());
        if (!ruleOptional.isPresent()) {
          try {
            buildRuleResolver.requireRule(btsp.getTarget());
          } catch (NoSuchBuildTargetException e) {
            throw new HumanReadableException("Rule for target '%s' could not be resolved.", btsp.getTarget());
          }
        }
        System.err.println(ruleOptional);
      }
      return new KotlincToJarStepFactory(
          Preconditions.checkNotNull(kotlinBuckConfig).getKotlinc(),
          extraKotlincArguments);
    }
  }
}
