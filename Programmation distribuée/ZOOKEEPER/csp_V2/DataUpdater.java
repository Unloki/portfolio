package watcher_V1;

import java.io.IOException;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import java.time.LocalDateTime;

import csp_V1.ThreadLoop;

public class DataUpdater extends ThreadLoop implements Watcher {

  private static final String my_id= "/"+System.getProperty("user.name");
  private static String hostPort = "np1:2181,np2:2181,np3:2181,np4:2181,np5:2181,np6:2181,np7:2181,np8:2181,np9:2181,np10:2181,np11:2181,np12:2181,np13:2181";
  private static String zooDataPath = "/MyConfig";

  ZooKeeper zk;

  private int nombre_modifications;
  // Delai entre deux modifications en secondes
  private int intervalle;
  //Version actuelle modifié
  private int version = 1;
  private String hostname = "unknown"; // hostname

  /**
   * Constructeur de l'"Updater" avec en parametre le nombre de mofifications à apporter et l'intervalle de temps
   * On passe aussi l'id du noeud (passé en argument lors du lancement du programme)
   *
   */
  public DataUpdater(int nombre_modifications, int intervalle, int htnm_int) {
    // heritage de la classe ThreadLoop
    super("DataUpdater");
    this.nombre_modifications = nombre_modifications;
    this.intervalle = intervalle;
    this.hostname = System.getProperty("user.name")+":np"+htnm_int;

    try {
      //Recupération du ZooKeeper, etablisement de la connexion
      zk = new ZooKeeper(hostPort, 2000, this);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * BeforeLoop, herité de ThreadLoop
   */
  @Override
  public void beforeLoop() {
     printOut("Starting ...");
   }

  /**
  * inLoop, herité de ThreadLoop
   * Modifie le znode n nombre de fois avec un intervalle de temps de "intervalle";
   */
  @Override
  public void inLoop() {
    try {
      // a chaque fois on effectue une modifications
      if (this.nombre_modifications > 0) {
        //On incremente la version et on decremente le nombre de modifications restantes
      	this.nombre_modifications--;
      	this.version++;
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();
        String time = hour+"h"+ minute+"m" + second+"s" + String.valueOf(System.currentTimeMillis()) +"ms";
        // Les données qu'on va envoyer (avant l'UIID), mnt le nom de la machine, sa version et l'heure de la modification
        final String data = hostname + "_" + version + "_" + time;
        zk.setData(my_id + zooDataPath, data.getBytes(), -1); //
        try { //On attend l’intervalle de temps entre deux modifications (seconds * 1000 = equivalent milisecondes)
 		Thread.sleep(this.intervalle * 1000);
	} catch(InterruptedException e) { exitLoop(); }
      } else exitLoop();
    } catch(InterruptedException | KeeperException e) { printErr(e.getMessage()); exitLoop();}
  }

  /**
   *  afterLoop, herité de ThreadLoop
   * Fermeture de la connexion avec ZooKeeper
   */
  @Override
  public void afterLoop() {
    try {
      if (zk != null) zk.close();
    } catch(InterruptedException e) {}
  }

  /**
   * process, herité de ThreadLoop
   */
  @Override
  public void process(WatchedEvent event) {
    trace(String.format("Event Received: %s", event.toString()));
  }
  /**
   * Lancement du programme, Main, avec en arguments, le nombre de modif, l'intervalle de temps et l'id du noeud
   **/
  public static void main(String[] args) {
    try {
      final int nombre_modifications = Integer.parseInt(args[0]);
      final int intervalle = Integer.parseInt(args[1]);
      // on recupere le nom de la machine actuelle (npX), seul moyen trouvé
      final int hostname_int = Integer.parseInt(args[2]);
      DataUpdater dataUpdater = new DataUpdater(nombre_modifications, intervalle, hostname_int);
      //Lancement du thread
      dataUpdater.startLoop();
    } catch(ArrayIndexOutOfBoundsException | NumberFormatException e) {
      System.err.println("Error: " + e.getMessage());
      System.out.println("Utilisation: DataUpdater.jar <nombre de modif> <intervalle (sec)> <host>");
    }
  }

}
