package csp_V0;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

public final class ConcurrentProcess extends ThreadLoop {
    private static final String zoo_id = "/"+System.getProperty("user.name");
    private static final String zoo_barrier_path = "/Barrier";
    private static final String zoo_verrou_path = "/Verrou";
    private static final String zoo_final_path = "/Final";

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
     * Initialisation d'un nouveau processus
     */
    public ConcurrentProcess(int id, String nm, int offset) {
        super(name);
        this.master = false;
        try {
          zk = new ZooKeeper(2181, 2000, this);
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

    public ConcurrentProcess(int id, String name) {
        this(id, name, 0);
    }

    @Override
    void beforeLoop() {
        printData();
        waitNeighbouring("waiting neighbours ...");
    }
    @Override
    void inLoop() {
        try { ThreadLoop.sleep(1000);
        } catch(InterruptedException ex) {}
    }
    @Override
    void afterLoop() {
        if (zk != null) zk.close();
        this.neighbouring.close();
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
            synchronized(this.syncState) {
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
            String data = this.zk.getData(zoo_id+zoo_final_path, false, null);
            //TODO recupération de la liste de noeud dans les données ZooKeeper
            System.out.println("TOUT LES NOEUDS SYNCHRO");
        } catch (KeeperException | InterruptedException e) { e.printStackTrace(); exitLoop(); }
    }

    /**
     * On change la valeur du verrou en l'incrementant
     */
    private void changeDataVerrou() throws KeeperException, InterruptedException {
        String readyData = new String(this.zk.getData(zoo_id+zoo_final_path, false, null));
        // TODO ajouté l'id du neoud aux donnée existantes
        System.out.println("Verrou incrementé par Node"+this.my_id);
    }

    /**
     * On crée un verrou au besoin
     */
    private boolean verrou() throws KeeperException, InterruptedException {
        try {
            this.zk.create(zoo_id+zoo_verrou_path, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.out.println("Création Verrou");
            return true;
        } catch(KeeperException | InterruptedException e) {
            System.out.println("Erreur verrou déjà existant");
            return false;
        }
    }

    /**
     * S'inscrit sous la barriere, en ajoutant son id
     **/
    private void enterInCustomDBBarrier() throws KeeperException, InterruptedException{
        zk.create(zoo_id+zoo_barrier_path + "/node" + this.my_id, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
    }

    /**
     * On verifie que les voisins sont tous inscrit a la suite de la barriere /Barrier/node"id"
     */
    private boolean verifVoisinsPret() throws KeeperException, InterruptedException {
        List<String> noeud_inscrit = this.zk.getChildren(zoo_barrier_path, true);
        System.out.println("Noeud inscrit sous la barriere -> " + noeud_inscrit + " - neighbors: " + this.neighbouring.getIdentities());
        //Pour chaque voisins,  on verifie qu'il est inscrit sous la barriere zookeeper, si un d'eux n'est pas inscrit, on retourne faux
        for (Integer voisin_id : this.neighbouring.getIdentities()) {
            if (!noeud_inscrit.contains(String.valueOf(voisin_id))) return false;
        }
        System.out.println("Tous les noeuds sont inscrit");
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

}
