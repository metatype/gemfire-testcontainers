/*
 *  Copyright (c) VMware, Inc. 2022. All rights reserved.
 */

package com.vmware.gemfire.testcontainers;

import java.util.List;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class AbstractGemFireContainer<SELF extends AbstractGemFireContainer<SELF>>
    extends GenericContainer<SELF> {

  protected List<String> jvmArgs;

  protected AbstractGemFireContainer(DockerImageName image) {
    super(image);
  }

  protected void addJvmArg(String arg) {
    jvmArgs.add(arg);
  }

}
