package com.vmware.gemfire;

import static com.vmware.gemfire.GemFireLocatorContainer.LOCATOR_NAME;
import static com.vmware.gemfire.GemFireLocatorContainer.LOCATOR_PORT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.utility.DockerImageName;

public class GemFireServerContainer extends AbstractGemFireContainer {

  private static final Logger LOG = LoggerFactory.getLogger(GemFireServerContainer.class);

  private static final List<String> DEFAULT_JVM_ARGS = Arrays.asList(
      "-server",
      "-classpath /gemfire/lib/${BOOTSTRAP_JAR}:/build",
      "--add-exports=java.management/com.sun.jmx.remote.security=ALL-UNNAMED",
      "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED",
      "--add-opens=java.base/java.lang=ALL-UNNAMED",
      "--add-opens=java.base/java.nio=ALL-UNNAMED",
      "-DgemfirePropertyFile=/application/server_gemfire.properties",
      "-Dgemfire.start-dev-rest-api=false",
      "-Dgemfire.use-cluster-configuration=true",
      "-Dgemfire.log-level=fine",
      "-Dgemfire.locator-wait-time=120",
      "-XX:OnOutOfMemoryError=kill",
      "-Dgemfire.launcher.registerSignalHandlers=true",
      "-Djava.awt.headless=true",
      "-Dsun.rmi.dgc.server.gcInterval=9223372036854775806");

  public GemFireServerContainer(MemberConfig config, String imageName) {
    this(config, DockerImageName.parse(imageName));
  }

  public GemFireServerContainer(MemberConfig config, DockerImageName image) {
    super(image);

    jvmArgs = new ArrayList<>(DEFAULT_JVM_ARGS);

    withCreateContainerCmdModifier(it -> it.withName(config.getServerName()));

    // This is just so that TC can use the mapped port for the initial wait strategy.
    withExposedPorts(config.getProxyForwardPort());

    String locator = String.format("%s[%d]", LOCATOR_NAME, LOCATOR_PORT);
    jvmArgs.add("-Dgemfire.locators=" + locator);

    config.apply(this);

    String execPart =
        "export BOOTSTRAP_JAR=$(basename /gemfire/lib/gemfire-bootstrap-*.jar); exec java ";
    String launcherPart =
        " com.vmware.gemfire.bootstrap.ServerLauncher start " + config.getServerName() +
        " --automatic-module-classpath=/gemfire/extensions/*" +
        " --server-port=" + config.getProxyForwardPort() +
        " --hostname-for-clients=localhost";
    String jvmArgsPart = String.join(" ", jvmArgs);

    withCreateContainerCmdModifier(cmd -> cmd.withEntrypoint("sh"));

    withCommand("-c", execPart + jvmArgsPart + launcherPart);
  }

}
