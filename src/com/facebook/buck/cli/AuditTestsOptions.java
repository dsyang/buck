/*
 * Copyright 2015-present Facebook, Inc.
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

package com.facebook.buck.cli;


import org.kohsuke.args4j.Option;

public class AuditTestsOptions extends AuditCommandOptions {

  @Option(name = "--include-dependencies",
      aliases = { "-d" },
      usage = "Whether to include tests for dependencies in the output as well")
  private boolean includeDependencies = false;

  AuditTestsOptions(BuckConfig buckConfig) {
    super(buckConfig);
  }

  public boolean shouldIncludeDependencies() { return includeDependencies; }
}
