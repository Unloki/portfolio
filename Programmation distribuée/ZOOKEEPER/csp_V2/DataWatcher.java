package watcher_V1;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import csp_V1.ThreadLoop;

public class DataWatcher extends ThreadLoop implements Watcher {
  private static final String my_id= "/"+System.getProperty("user.name");
  private static String hostPort = "np1:2181,np2:2181,np3:2181,np4:2181,np5:2181,np6:2181,np7:2181,np8:2181,np9:2181,np10:2181,np11:2181,np12:2181,np13:2181";
  private static final String zooDataPath = "/MyConfig";

  byte zoo_data[] = null;
  ZooKeeper zk;

  /**
   * Constructeur du DataWatcher, heritage de ThreadLoop
   */
  public DataWatcher() {
    super("DataWatcher");
    try {
      zk = new ZooKeeper(hostPort, 2000, this);
      if (zk != null) {
        try {
          if (zk.exists(my_id+zooDataPath, this) == null) {//On verifie que le chemin existe pas
            zk.create(my_id+zooDataPath, "".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT); //On cree le chemin /MyConfig
          }
        } catch (KeeperException | InterruptedException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * BeforeLoop, herité de ThreadLoop
   */
  @Override
  public void beforeLoop() {
    try {
      trace("Starting ...");
      printData();
    } catch(InterruptedException | KeeperException e) {}
  }

  /**
  * inLoop, herité de ThreadLoop
   */
  @Override
  public void inLoop() {
    try {
       Thread.sleep(1000);
     } catch (InterruptedException e) {
       e.printStackTrace(); exitLoop();
     }
  }

  /**
   *  afterLoop, herité de ThreadLoop
   * Fermeture de la connexion avec ZooKeeper
   */
  @Override
  public void afterLoop() {
    try {
      if (this.zk != null) this.zk.close();
    } catch(InterruptedException e) {}
  }

  /**
   * Recuperation des données du znode et ecriture dans la console
   */
  public void printData() throws InterruptedException, KeeperException {
    zoo_data = zk.getData(my_id+zooDataPath, this, null);
    String zString = new String(zoo_data);
    System.out.printf("\nCurrent Data @ ZK Path %s: %s",my_id+zooDataPath, zString);
  }

  /**
   * process, herité de ThreadLoop
   */
  @Override
  public void process(WatchedEvent event) {
    System.out.printf("\nEvent Received: %s", event.toString());
    if (event.getType() == Event.EventType.NodeDataChanged) { // en cas de changement de données, watcher
      try {
        printData();
      } catch (InterruptedException e) {
        e.printStackTrace();
      } catch (KeeperException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Lancement du programme, Main
   **/
  public static void main(String[] args) throws InterruptedException, KeeperException, IOException {
    DataWatcher dataWatcher = new DataWatcher();
    dataWatcher.startLoop();
  }

}
