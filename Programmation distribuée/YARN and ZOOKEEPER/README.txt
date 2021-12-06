Pour lancer le démarrage des processus séquentiels communiquants (CSP) en utilisant YARN et ZooKeeper :
->   ./run-yarn-app.sh
Ce script compile et exécute le code source du TP, et crée un Jar csp_V3.jar
Ce jar est ensuite copié sur le "hdfs filesystem" puis lancé avec Yarn par le Client YARNClient et du gestionnaire de l'application YARNApplicationMaster


Problème lors de l'exécution du jar avec Yarn via le script:
-> Code source bien compilée et exécutée
-> Application soumise avec succès à l'application Master
-> Erreur avec le YARNConcurrentProcess -> Application finished with FINISHED state and id application_1637794840053_0023 (voir logs)

Travaux réalisés :
- Classe YARNClient (lancement d'un ApplicationMaster sur le cluster)
- Classe YARNApplicationMaster (création de l’arborescence ZooKeeper node-map, lancement des processus sur le cluster)
- Classe YARNConcurrentProcess (lecture de l’arborescence ZooKeeper node-map (readNeighbouring()))
