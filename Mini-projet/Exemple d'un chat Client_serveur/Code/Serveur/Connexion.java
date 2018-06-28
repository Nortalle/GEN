//******************************************************************************
// Connexion.java
// E.Lefrançois 8 février 2001
// Projet "Chat"
//******************************************************************************
import java.lang.Thread;
import java.io.*;
import java.net.*;
import java.util.*;

//==============================================================================
// Objet concurrent, responsable de la connexion avec le client
// Cet objet a un accès direct à la liste des connexions maintenue par l'objet
// "Serveur" (accès en exclusion mutuelle)
//==============================================================================

class Connexion extends Thread {

// Variables d'instance

   private Socket a_socketClient; 	// socket connectée au client

   private Vector a_listeDesConnexions;   // Liste des connexions avec les clients

   private ControleurConnexions a_leControleurDesConnexions;
                                  // Pour contrôler si la connexion
                                  // devient inactive

   private boolean clientActif;   // "true" tant que le client n'a pas
                                  // demandé une fermeture de la ssession
   private String idClient;       // Identificateur du client
   private BufferedReader canalDentree;
                                  // Canal d'entrée avec le client (texte)

   private PrintWriter canalDeSortie;
                                  // Canal de sortie avec le client (Texte)

   private DataOutputStream outputDataCanal;
       // Canal de sortie pour données uniquement:  non utilisé !!
       // Pour le créer éventuellement:
       // outputDataCanal = new DataOutputStream (clientSocket.getOutputStream());

   // Constructeur
   public Connexion (	Socket socketClientAssociée,
                        ControleurConnexions controleurAssocié,
                        Vector listeDesConnexions) {

        System.out.println (">> Nouvelle connexion ..");
        a_socketClient = socketClientAssociée;
        a_leControleurDesConnexions = controleurAssocié;
        a_listeDesConnexions = listeDesConnexions;
        clientActif = true;
        try {
            canalDentree = new BufferedReader
                  (new InputStreamReader(a_socketClient.getInputStream()));
            canalDeSortie = new PrintWriter
                  (a_socketClient.getOutputStream());
        }
        catch (IOException e) {
              fermerConnexion();
              return;
        }
        this.start();	// Démarrer l'activité
   }


// Activité concurrente
   public void run() {
      String ligneDeTexte;  // Texte envoyé par le client

      // Boucle d'attente
      try {
          while (clientActif) {
              ligneDeTexte = canalDentree.readLine();
              System.out.println (ligneDeTexte);

              analyserLigneRecue(ligneDeTexte);
          }
      }
      catch (IOException e) {}
      finally {
          fermerConnexion();
      }
   }


// Méthodes privées
  private void analyserLigneRecue (String ligne) {
     StringTokenizer st = new StringTokenizer (ligne, "#");
      try {
          String typeInfo = st.nextToken();
          if (typeInfo.equals("CTR")){
              String info = st.nextToken();
              if (info.equals("LOGIN")) {
                    this.idClient = st.nextToken();
                    envoyerTexteAuClient ("#CTR#LOGIN_OK");
                    broadcast ("#DATA#*chat*#" + "Bienvenue à " + idClient);
              }
              else if (info.equals("LOGOUT")) {
                    broadcast ("#DATA#*chat*#" + idClient + " quitte le Chat ! bye bye");
                    clientActif = false;
              }
              else erreurDeDonnees(ligne);
          }
          else if (typeInfo.equals("DATA")){
              broadcast ("#DATA#" + idClient + "#" + st.nextToken());
          }
          else erreurDeDonnees(ligne);
      }
      catch (Exception e) {erreurDeDonnees(ligne);}
   }

    private void broadcast(String texte) {
    // Envoyer le message à chaque client
      Connexion uneConnexion;
      synchronized (a_listeDesConnexions) {
           for (   Enumeration en =
                   a_listeDesConnexions.elements();
                   en.hasMoreElements();) {
                uneConnexion = (Connexion)en.nextElement();
                synchronized (uneConnexion) {
                  uneConnexion.canalDeSortie.println (texte);
                  uneConnexion.canalDeSortie.flush();
                }
           }
      }
    }

    private synchronized void envoyerTexteAuClient (String texte) {
          canalDeSortie.println (texte);
          canalDeSortie.flush();
    }

    private void erreurDeDonnees (String ligneRecue) {
      fermerConnexion();
    }

    private void fermerConnexion() {
        try {
              a_socketClient.close();
        }
        catch (IOException e) {}

        synchronized (a_leControleurDesConnexions) {
            a_leControleurDesConnexions.finDeConnexion(this);
            // Le signaler au contrôleur des connexions
        }
    }
}

