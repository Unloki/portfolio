package csp_V3;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.ApplicationSubmissionContext;
import org.apache.hadoop.yarn.api.records.ContainerLaunchContext;
import org.apache.hadoop.yarn.api.records.LocalResource;
import org.apache.hadoop.yarn.api.records.LocalResourceType;
import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
import org.apache.hadoop.yarn.api.records.Resource;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.client.api.YarnClientApplication;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.Apps;
import org.apache.hadoop.yarn.util.ConverterUtils;
import org.apache.hadoop.yarn.util.Records;

public class YARNClient {

  public static final String JAR_NAME= "csp_V3.jar";
  public static final String PACKAGE_NAME= "csp_V3";
  public static final String APP_NAME= "YARNApplicationMaster";
  private Configuration conf = new YarnConfiguration();

  //Get the config path and the jar passed on the command
  public void run(String[] args) throws YarnException, IOException, InterruptedException {
    final int n_instances = Integer.parseInt(args[0]);
    final Path config_file_path =  new Path(args[1]);
    final Path jar_path = new Path(args[2]); // dans le HDFS

    YarnConfiguration yarnConfiguration = new YarnConfiguration();
    YarnClient yarnClient = YarnClient.createYarnClient();
    yarnClient.init(yarnConfiguration);
    yarnClient.start();
    YarnClientApplication yarnClientApplication = yarnClient.createApplication();
    //container launch context for application master
    ContainerLaunchContext applicationMasterContainer = Records.newRecord(ContainerLaunchContext.class);
    applicationMasterContainer.setCommands(
      Collections.singletonList(
          "$JAVA_HOME/bin/java " + PACKAGE_NAME + "." + APP_NAME +
          " " + n_instances +
          " " + jar_path +
          " " + config_file_path +
          " 1>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout" +
          " 2>" + ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"
          )
        );
    LocalResource applicationMasterJar = Records.newRecord(LocalResource.class);
    setupJarFileForApplicationMaster(jar_path, applicationMasterJar);
    applicationMasterContainer.setLocalResources(Collections.singletonMap(JAR_NAME, applicationMasterJar));
    Map<String, String> appMasterEnv = new HashMap<>();
    setupEnvironmentForApplicationMaster(appMasterEnv);
    applicationMasterContainer.setEnvironment(appMasterEnv);
    Resource resources = Records.newRecord(Resource.class);
    resources.setVirtualCores(1);
    //resources.setMemory(100);
    ApplicationSubmissionContext submissionContext = yarnClientApplication.getApplicationSubmissionContext();
    submissionContext.setAMContainerSpec(applicationMasterContainer);
    submissionContext.setQueue("default");
    submissionContext.setApplicationName("DistributedShell");
    submissionContext.setResource(resources);
    ApplicationId applicationId = submissionContext.getApplicationId();
    System.out.println("Submitting " + applicationId);
    yarnClient.submitApplication(submissionContext);
    System.out.println("Post submission " + applicationId);
    ApplicationReport applicationReport;
    YarnApplicationState applicationState;
    do{
      Thread.sleep(2000);
      applicationReport = yarnClient.getApplicationReport(applicationId);
      applicationState = applicationReport.getYarnApplicationState();
      System.out.println("Application states report: " + applicationReport.getDiagnostics());
    }while(applicationState != YarnApplicationState.FAILED &&
        applicationState != YarnApplicationState.FINISHED &&
        applicationState != YarnApplicationState.KILLED );
    System.out.println("Application finished with " + applicationState + " state and id " + applicationId);
  }

  private void setupJarFileForApplicationMaster(Path jarPath, LocalResource localResource) throws IOException {
    FileStatus jarStat = FileSystem.get(conf).getFileStatus(jarPath);
    localResource.setResource(ConverterUtils.getYarnUrlFromPath(jarPath));
    localResource.setSize(jarStat.getLen());
    localResource.setTimestamp(jarStat.getModificationTime());
    localResource.setType(LocalResourceType.FILE);
    localResource.setVisibility(LocalResourceVisibility.PUBLIC);
  }
  private void setupEnvironmentForApplicationMaster(Map<String, String> environmentMap) {
    for (String c : conf.getStrings(YarnConfiguration.YARN_APPLICATION_CLASSPATH,
                                    YarnConfiguration.DEFAULT_YARN_APPLICATION_CLASSPATH)) {
      Apps.addToEnvironment(environmentMap, ApplicationConstants.Environment.CLASSPATH.name(),c.trim());
    }
    Apps.addToEnvironment(environmentMap,
        ApplicationConstants.Environment.CLASSPATH.name(),
        ApplicationConstants.Environment.PWD.$() + File.separator + "*");
  }

  public static void main(String[] args) throws Exception {
    YARNClient client = new YARNClient();
    client.run(args);
  }
}
