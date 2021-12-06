package csp_V3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.conf.YarnConfiguration;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType.*;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public final class YARNConcurrentProcess extends ThreadLoop implements Watcher {
    private static final String zoo_id = "/"+System.getProperty("user.name");
    private static final String zoo_barrier_path = "/Barrier";
    private static final String zoo_verrou_path = "/Verrou";
    private static final String zoo_final_path = "/Final";


    //Part ZooKeeper node-map
    private static final String zoo_node_map = "/node-map";

    /**
    * Table des noeud operationel sous l'enfant "node_map" de ZooKeeper
    */
    private ConcurrentHashMap<Integer, String> node_map;

    byte zoo_data[] = null;

    ZooKeeper zk;

    private Neighbouring neighbouring;
    /**
     * Table des actions à exécuter en fonction du tag des messages
     */
    private ConcurrentHashMap<String,MessageHandler> listener_map;
    /**
     * Identifiant (unique dans le réseau) du processus
     */
    private int my_id;
    /**
     * Nombre de noeud présent et attendus
     */
    private int number_node;
    /**
     * Nom (applicatif) du processus
     */
    private String name;
    /**
     * compteur du nombre de messages reçus
     */
    private AtomicInteger rcv_msg_cnt;
    /**
     * Drapeau indiquant si le processus est prêt à envoyer et à recevoir des messages
     */
    private AtomicBoolean ready;
    /**
     * compteur du nombre de messages envoyés
     */
    private AtomicInteger snd_msg_cnt;
    /**
     * si le processus est le maitre
     */
    private boolean master;
    /**
     * Etat actuel de la synchronisation
     */
    private String currentState;

    /**
     * Initialisation d'un nouveau processus
     */
    public YARNConcurrentProcess(int id, String nm, int offset) {
        super(nm);
        this.master = false;
        try {
          //ZooKeeper address : dmis:2181, session timout : 2000
          zk = new ZooKeeper("dmis:2181", 2000, this);
          if (zk != null) {
            try {
              if (zk.exists(zoo_id+zoo_barrier_path, this) == null) {//On verifie que le chemin n'existe pas
                zk.create(zoo_id+zoo_barrier_path, "".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT); //On cree le chemin /Barrier
                this.master = true;
              }
            } catch (KeeperException | InterruptedException e) {
              e.printStackTrace();
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        this.my_id = id;
        this.name = nm;
    }
    /**
     * Initialisation d'un nouveau processus avec 0 offset
     */
    public YARNConcurrentProcess(int id, String name) {
        this(id, name, 0);
    }

    @Override
    void beforeLoop() {
        waitNeighbouring("waiting neighbours ...");
    }
    @Override
    void inLoop() {
        try { ThreadLoop.sleep(1000);
        } catch(InterruptedException ex) {}
    }
    @Override
    void afterLoop() {
      try {
        if (zk != null) zk.close();
        this.neighbouring.close();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    /**
     * Recuperation des données du znode et ecriture dans la console
     */
    public void printData() throws InterruptedException, KeeperException {
      zoo_data = zk.getData(zoo_id+zoo_barrier_path, this, null);
      String zString = new String(zoo_data);
      System.out.printf("\nCurrent Data @ ZK Path %s: %s",zoo_id+zoo_barrier_path, zString);
    }

    /**
     * Attente des voisins sans msg
     */
    public final void waitNeighbouring() {
        this.waitNeighbouring("");
    }
    /**
     * Solution algorithmique remplaçant la primitive de synchronisation de CSP_V1 waitNeighbouring
     *
     * Lors du lancement du programme, chaque nœud essaye de créer le chemin zookeeper /"username"/Barrier, si il n'existe pas, le noeud le crée et devient le maître
     * Ensuite, chaque nœud "s'inscrit" sous la barrière, en ajoutant l'enfant /"username"/Barrier/Node"id" correspondant au noeud.
     * On vérifie par la suite si les voisins sont inscrits, à l'aide de neighbouring.getIdentities() on peut récupérer les voisins (leur id) est verifier si un enfant correspondant est
     * inscrit sous le chemin Barrier.
     * Si tout les noeuds ont un enfant correspondant sous la barriere alors on passe à la 2nd étape
     * La 2e étape repose sur un système de verrou, tout les noeuds ajoute un watcher au chemin /"username"/Verrou, lors du cahngement de valeur, les noeuds seront considéré comme synchronisés.
     * Chaque noeud ajoute son id en donnée au chemin /"username"/Final (On aura un string avec la suite des id des noeuds (1-3-5-2...)
     * Le maitre lis les donnée au chemin /"username"/Final, quand tout les nœuds sont ajoutés en données, il libère le verrou en changant sa valeur.
     * Tout les noeuds sont ensuite condiéré comme synchronisé
     */

    public final void waitNeighbouring(String msg) {
        try {
            synchronized(currentState) {
                enterInCustomDBBarrier(); // on inscrit le noeud
                if (verifVoisinsPret()) {// on passe a la 2eme étape, barriere verrou
                    if (this.master) {
                        if (verrou()) {
                            if (zk.exists(zoo_id+zoo_final_path, this) == null) {//On verifie que le chemin n'existe pas
                                zk.create(zoo_id+zoo_final_path, "".getBytes(),ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.PERSISTENT); //On cree le chemin /Barrier
                                changeDataVerrou();
                            }
                        }
                    }
                }
            }
            //String data = this.zk.getData(zoo_id+zoo_final_path, false, null);
            //TODO recupération de la liste de noeud dans les données ZooKeeper
            trace("TOUT LES NOEUDS SYNCHRO");
        } catch (KeeperException | InterruptedException e) { e.printStackTrace(); exitLoop(); }
    }
    @Override
    public void process(WatchedEvent event) {
      //TODO Process react by the type of the event
      /*
        try {
            switch(event.getType()) {

            }
        } catch (KeeperException | InterruptedException e) { e.printStackTrace(); }
        **/
    }

    public final void readNeighbouring(String filename){
      try {
          final List<String> nodeMaps = this.zk.getChildren(zoo_id+zoo_final_path, false);
          for (String id_noeud : nodeMaps) {
              String hostname_process = new String(this.zk.getData(zoo_id+zoo_final_path + "/" + id_noeud, false, null));
              Integer id_process = Integer.parseInt(id_noeud.substring(1));
              this.node_map.put(id_process, hostname_process);
          }
          trace("[p"+this.my_id+"] readNeighbouring -> node_map : " + node_map);
          Path cfgFilePath = new Path(filename);
          Configuration conf = new YarnConfiguration();
          FileSystem fs = FileSystem.get(conf);
          // Lecture des informations sur le fichier de config, a partir des nodes sur ZooKeeper, associe un num processus et node_map
          try (InputStreamReader isr = new InputStreamReader(fs.open(cfgFilePath)); BufferedReader reader = new BufferedReader(isr)) {
              String line;
              while ((line = reader.readLine()) != null) {
                  String[] characs = line.split(":");
                      try {
                          int neighbour_id_1   = Integer.parseInt(characs[0].trim());
                          int neighbour_dns_1 = Integer.parseInt(characs[1].trim());

                          int neighbour_id_2   = Integer.parseInt(characs[2].trim());
                          int neighbour_dns_2 = Integer.parseInt(characs[3].trim());

                          if (neighbour_id_1 == this.getMyId()) {
                              if (this.node_map.containsKey(neighbour_id_2))
                              this.addNeighbour(neighbour_dns_1, this.node_map.get(neighbour_id_2), neighbour_id_2, neighbour_dns_2);
                          } else if (neighbour_id_2 == this.getMyId()){
                              if (this.node_map.containsKey(neighbour_id_1))
                              this.addNeighbour(neighbour_dns_2, this.node_map.get(neighbour_id_1), neighbour_id_1, neighbour_dns_1);
                          }
                      } catch (NumberFormatException e) {
                          printErr(e.toString());
                      }
                  }
              }
      } catch (IOException | KeeperException | InterruptedException | NumberFormatException e) {
          printErr(e.getMessage());
      }
    }
    /**
     * Add neighbor
     */
    public final void addNeighbour(int localPort, String remoteHostname, int remoteId, int remotePort) {
        try {
            trace("[p"+this.my_id+"] :neighbor add");
            this.neighbouring.add(localPort, remoteHostname, remoteId, remotePort);
        } catch (UnknownHostException | SocketException e) {
            printErr(e.getMessage());
        }
    }

    /**
     * On change la valeur du verrou en l'incrementant
     */
    private void changeDataVerrou() throws KeeperException, InterruptedException {
        String readyData = new String(this.zk.getData(zoo_id+zoo_final_path, false, null));
        // TODO ajouté l'id du neoud aux donnée existantes
        trace("Verrou incrementé par Node "+this.my_id);
    }

    /**
     * On crée un verrou au besoin
     */
    private boolean verrou() throws KeeperException, InterruptedException {
        try {
            this.zk.create(zoo_id+zoo_verrou_path, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            trace("Création Verrou");
            return true;
        } catch(KeeperException | InterruptedException e) {
            trace("Erreur verrou déjà existant");
            return false;
        }
    }

    /**
     * S'inscrit sous la barriere, en ajoutant son id
     **/
    private void enterInCustomDBBarrier() throws KeeperException, InterruptedException{
        zk.create(zoo_id+zoo_barrier_path + "/node" + this.my_id, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    /**
     * On verifie que les voisins sont tous inscrit a la suite de la barriere /Barrier/node"id"
     */
    private boolean verifVoisinsPret() throws KeeperException, InterruptedException {
        List<String> noeud_inscrit = this.zk.getChildren(zoo_barrier_path, true);
        trace("Noeud inscrit sous la barriere -> " + noeud_inscrit + " - neighbors: " + this.neighbouring.getIdentities());
        //Pour chaque voisins,  on verifie qu'il est inscrit sous la barriere zookeeper, si un d'eux n'est pas inscrit, on retourne faux
        for (Integer voisin_id : this.neighbouring.getIdentities()) {
            if (!noeud_inscrit.contains(String.valueOf(voisin_id))) return false;
        }
        trace("Tous les noeuds sont inscrit");
        return true;
    }
	public final void addMessageListener(String tag, MessageHandler handler) {
        this.listener_map.put(tag, handler);
    }

	public final int getMyId() {
		return my_id;
	}

	public final int getSndMsgCnt() {
		return snd_msg_cnt.get();
	}

	public final int getRcvMsgCnt() {
		return rcv_msg_cnt.get();
    }
     /**
     * Remise à 0 du compteur de messages envoyés
     */
    public final void resetSndMsgCnt() {
        this.snd_msg_cnt.set(0);
    }

    /**
     * Remise à 0 du compteur de messages reçus
     */
    public final void resetRcvMsgCnt() {
        this.rcv_msg_cnt.set(0);
    }

    public boolean isReady() {
        return this.ready.get();
    }
    synchronized void receiveMessage(Message msg) {
        MessageHandler handler = this.listener_map.get(msg.getTag());
        this.rcv_msg_cnt.getAndIncrement();
        if (handler != null) {
            handler.onMessage(msg);
        } else super.printErr("receiveMessage -> bad Tag.");
    }

}
