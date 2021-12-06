package csp_V3;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.ApplicationConstants;
import org.apache.hadoop.yarn.api.protocolrecords.AllocateResponse;
import org.apache.hadoop.yarn.client.api.AMRMClient.ContainerRequest;
import org.apache.hadoop.yarn.api.records.*;
import org.apache.hadoop.yarn.client.api.AMRMClient;
import org.apache.hadoop.yarn.client.api.NMClient;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.util.*;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;


import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType.*;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public class YARNApplicationMaster {

  private static final String zoo_id = "/"+System.getProperty("user.name");
  private static final String zoo_racine_path = "/node-map";
  private static final String zoo_node_map_path = zoo_id + zoo_racine_path;

  byte zoo_data[] = null;
  private static ZooKeeper zk;

  private static final String JAR_NAME= "dshell.jar";
  private static final String PACKAGE_NAME= "dshell";
  private static final String APP_NAME= "YARNApplicationMaster";


  private Configuration conf = new YarnConfiguration();

  /**
  * Creation ZooKeeper and create if necessary main path /dubois
  **/
  public static void setUpZookeeper(){
    try {
      //ZooKeeper address : dmis:2181, session timout : 2000
      zk = new ZooKeeper("dmis:2181", 2000, null);
      if (zk != null) {
        try {
          if (zk.exists(zoo_id, false) == null) {//On verifie que le chemin n'existe pas
            zk.create(zoo_id, "".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
          }
          if (zk.exists(zoo_node_map_path, false) == null) {//On verifie que le chemin n'existe pas
            zk.create(zoo_node_map_path, "".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
          }
        } catch (KeeperException | InterruptedException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public static void main(String[] args) throws YarnException, IOException, InterruptedException {
          int number_of_instance = Integer.parseInt(args[0]);
          Path path_jar = new Path(args[1]);
          Path config_path = new Path(args[2]);

          Configuration configuration = new YarnConfiguration();
          System.out.println("Starting Application Master Znode");
          AMRMClient<AMRMClient.ContainerRequest> resourceManagerClient = AMRMClient.createAMRMClient();
          resourceManagerClient.init(configuration);
          resourceManagerClient.start();
          System.out.println("Started AMRMClient");

          NMClient nodeManagerClient = NMClient.createNMClient();
          nodeManagerClient.init(configuration);
          nodeManagerClient.start();
          System.out.println("Started nodeManagerClient");

          resourceManagerClient.registerApplicationMaster("localhost", 80010, "myappmaster");
          System.out.println("Registration done");
          // Priority for worker containers - priorities are intra-application
          Priority priority = Records.newRecord(Priority.class);
          priority.setPriority(0);
          // Resource requirements for worker containers
          Resource capability = Records.newRecord(Resource.class);
          capability.setVirtualCores(1);
          int completedContainers = 0;
          int containerId = 0;
          int id_process = 0;

          for(completedContainers = 0; completedContainers < number_of_instance; ++completedContainers) {
              ContainerRequest containerRequest = new ContainerRequest(capability, (String[])null, (String[])null, priority);
              resourceManagerClient.addContainerRequest(containerRequest);
          }
          //Set up zookeeper environment
          setUpZookeeper();
          while (completedContainers < number_of_instance) { //for each node -> execute YARNConcurrentProcess with the config file
              AllocateResponse ar = resourceManagerClient.allocate(containerId++);
              for (Container container : ar.getAllocatedContainers()) {
                  ContainerLaunchContext shellContainerContext = Records.newRecord(ContainerLaunchContext.class);
                  shellContainerContext.setCommands(
                      Collections.singletonList(
                          path_jar +
                          " " + config_path +
                          " " + id_process +
                          " 40000" +  // port number offset
                          " 1>"  +
                           ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stdout "  +
                          " 2>"  +
                           ApplicationConstants.LOG_DIR_EXPANSION_VAR + "/stderr"));

                  //Add the jar of the client in the current container
                  LocalResource appJar = Records.newRecord(LocalResource.class);

                  FileStatus jarStat = FileSystem.get(configuration).getFileStatus(path_jar);
                  appJar.setResource(ConverterUtils.getYarnUrlFromPath(path_jar));
                  appJar.setSize(jarStat.getLen());
                  appJar.setTimestamp(jarStat.getModificationTime());
                  appJar.setType(LocalResourceType.FILE);
                  appJar.setVisibility(LocalResourceVisibility.APPLICATION);
                  shellContainerContext.setLocalResources(Collections.singletonMap(YARNClient.JAR_NAME, appJar));
                  //Get the name of the znode for ZooKeeper
                  final String containerHostname = container.getNodeHttpAddress().split("\\.")[0];
                  try{ // try to create znode path for the current container
                    creationZnodeProcess(containerHostname, id_process);
                  } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                  }

                  nodeManagerClient.startContainer(container, shellContainerContext);
                  id_process++;
              }
              for (ContainerStatus cs : ar.getCompletedContainersStatuses()) {
                  completedContainers++;
                  System.out.println("completed Container " + completedContainers + " " + cs);
              }
              Thread.sleep(500);
          }
          try { zk.close(); } catch (InterruptedException e) {}
          resourceManagerClient.unregisterApplicationMaster(FinalApplicationStatus.SUCCEEDED, "", "");
      }

      /**
       * Create a znode path for the current process
       */
      private static void creationZnodeProcess(String containerHostname, int pid) throws KeeperException, InterruptedException {
          if (zk.exists(zoo_node_map_path + "/p"+ pid, false) == null) {//On verifie que le chemin n'existe pas
            zk.create(zoo_node_map_path + "/p"+ pid,  containerHostname.getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT);
            System.out.println("Znode create for p" + pid + " - data: " + containerHostname);
          }
      }
}
