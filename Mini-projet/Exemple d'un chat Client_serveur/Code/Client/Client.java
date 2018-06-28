import java.applet.*;
import java.awt.*;
import java.net.*;
import java.awt.event.*;
import java.io.*;
import java.util.StringTokenizer;

//******************************************************************************
// Client.java
// E.Lefrançois 10 février 2001
// Projet "Chat", avec soquettes
//******************************************************************************

//==============================================================================
// classe "FenetreApplication"
// -->  Support pour l'application exécutée en mode "Standalone"  (Application
// autonome)
//
// Cette classe agit en tant que fenêtre "top level" dans laquelle s'exécute
// l'applet.
//==============================================================================
class FenetreApplication extends Frame {
// Classe imbriquée utilisée pour traiter les événements "fenêtre", comme
// "window closing" notamment.
   class WindowEventListener extends WindowAdapter {
         public void windowClosing (WindowEvent e) {
                a_client.ev_deconnecter();
                System.exit(0);
         }
   }

// Variables d'instance
   private WindowEventListener wEL = new WindowEventListener();

   /**
    * @supplierRole a_client 
    */
   private Client a_client;   // Client associé

// Constructeur (s)
   public FenetreApplication (Client leClient) {
          setTitle (" C L I E N T - C H A T ");
          addWindowListener (wEL);
          setBackground(Color.lightGray);
          a_client = leClient;
   }
}

//==============================================================================
// Classe Client
// Attend des événements utilisateurs:
//        1) "connecter":  demande de connexion demandée par l'utilisateur
//        2) "envoyer": un message doit être envoyé aux autres clients
//        3) "deconnecter":  demande de déconnexion demandée par l'utilisateur
//
// Attend des événements générés par le serveur
//        1) "sessionAcceptee": la demande de session a été acceptée
//        3) "sessionRefusee": la demande de session a été refusée
//        2) "connexionFermee": la connexion est terminée, et la session par
//            la même occasion
//            Causes possibles (non distinguées)
//                - Normale: suite à demande de déconnexion demandée par
//                  l'utilisateur
//                - Anormale:  le serveur est "tombé", problème d'I/O
//
// En réponse à une demande de connexion de l'utilisateur:
// 1/ une socket est créée afin de pouvoir communiquer avec le serveur.
// 2/ le programme crée une instance de la classe "Connexion", un objet
//    actif qui sera responsable de la réception et de l'analyse des messages
//    envoyés par le serveur.
//
// En réponse à une demande d'envoi, la ligne de texte spécifiée par
// l'utilisateur est envoyée au serveur.
//
// En réponse à une demande de déconnexion, ou lorsque l'application est
// "quittée", le message de fin de session est envoyé au serveur.  C'est le
// serveur lui-même qui fermera la socket.
//==============================================================================

//------------------------------------------------------------------------------
public class Client extends Applet {
// Constantes de classes
   private static final int NO_PORT = 5002;    // No du port attaché au serveur
// Variables d'instance

   private boolean connexionEnCours = false;
   private boolean sessionEnCours = false;
   private String monId ="";    // Identificateur du client

   private boolean standAloneMode = false;
                   // Si true: exécution en tant qu'application "standalone"
                   // Si false: exécution en tant qu'applet


   /**
    * @supplierRole fenetre 
    */
   private static FenetreApplication fenetre;
                    // Fenêtre "Top-level" contenant l'applet
                    // Utilisé uniquement en mode "standalone"


   /**
    * @link aggregationByValue 
    */
   private InterfaceUtilisateur gui;
                  // Définition de l'interface utilisateur (boutons, ..)


   /**
    * @supplierRole socket
    * @link aggregationByValue 
    */
   private Socket socket;
           	  // socket connectée au serveur


   /**
    * @supplierRole ecouteurDuServeur
    * @link aggregationByValue 
    */
   private ServeurEcouteur ecouteurDuServeur;
                  // Objet actif associé, qui écoute le serveur de manière
                  // concurrente au reste du programme


   /**
    * @supplierRole canalDeSortie 
    */
   private PrintWriter canalSeSortie;	  // Canal de sortie, pour le texte
                                          // envoyé au serveur

   private DataOutputStream dataOutCanal;
      // Canal de sortie pour les données uniquement:  non utilisé !
      // Pour le créer:
      // dataOutCanal = new DataOutputStream (socketWithServer.getOutputStream());



   // Constructeur(s)
   public Client() {}


// Méthodes publiques
   public static void main(String[] args) {
   // Point d'entrée de l'application, si exécutée en mode "standalone"

   // Création et démarrage de l'applet
      Client appletClient = new Client();
      appletClient.standAloneMode = true;

   // Création de la fenêtre "top-level", dans laquelle s'exécute l'applet Client
      fenetre = new  FenetreApplication(appletClient);
      fenetre.add("Center", appletClient);

      appletClient.init();
      fenetre.pack();   // Ajustement de la taille de la fenêtre à la taille
                        // de ses composants
      fenetre.show();
      appletClient.start();
   }

   public void init() {
   // Méthode appelée lorsque l'applet est chargée pour la première fois, ou
   // lorsqu'elle est rechargée.
   // Cette méthode est utilisée pour opérer diverses initialisations
   // (structures de données, composants de la fenêtre, ..)
      gui = new InterfaceUtilisateur(this);
      setLayout (new BorderLayout());
      add (gui, "Center");
      gui.desactiverEtatConnexion();
      gui.desactiverEnvoi();
   }

   public void start() {
   // Méthode appelée lorsque l'applet est initialisée (suit le message init()),
   // et à chaque fois que la page qui contient l'applet est à nouveau activée
      gui.effacerAvertissement();
      if (! connexionEnCours) demarrerSession();
   }

   public void stop(){}
   // Méthode appelée lorsque la page qui contient l'applet est désactivée
   // Le chat continue en arrière plan

    public void ev_connexionFermee (String message) {
    // Evénement envoyé par l'écouteur du serveur
        gui.afficherAvertissement("CONNEXION TERMINEE", message);
        fermerSession();
    }

// Gestion des événements
   public void ev_connecter () {
   // Demande de connexion demandée par le client
      gui.effacerAvertissement();
      demarrerSession();
   }

   public void ev_deconnecter () {
   // Fin de connexion demandée par le client
      gui.effacerAvertissement();
      seDeconnecter();

   }

   public void ev_envoyer() {
      if (gui.getMessage().length()!=0)
            envoyerTexteAuServeur("#DATA#"+gui.getMessage());
   }

// Evénements
   public void ev_sessionAcceptee() {
   // Evénement envoyé par le serveur (Via écouteur de serveur)
      sessionEnCours = true;
      gui.activerEnvoi();
   }

   public void ev_sessionRefusee(String message) {
   // Evénement envoyé par le serveur (Via écouteur de serveur)
      gui.afficherAvertissement("!! SESSION REFUSEE !! ", message);
      seDeconnecter();
   }

   // Méthodes diverses
   public String getIdClient () {return monId; }
   public void setIdClient(String id) {monId = id; }

   // Méthodes privées
   private void envoyerTexteAuServeur (String text) {
        if (! connexionEnCours) return;
        String t = text.replace('\n',(char)0);
        try {
            canalSeSortie.println (t);
            canalSeSortie.flush();// le canal a été créé sans  "automatic flush"
        }
        catch (Exception e) {
             gui.afficherAvertissement
                    ("!! PROBLEME DE CONNEXION !! " , e.toString());
             fermerSession();
        }
   }

   private String saisirIdentificateurClient () {
       monId = null;
       LoginDialogue ld = new LoginDialogue ((Frame)getParent(), this);
       ld.show();
       return monId;
   }

   private void demarrerSession() {
      String id = saisirIdentificateurClient();
      if (id != null) {
          connexionEnCours = seConnecter();
          if (connexionEnCours) {
              gui.activerEtatConnexion();
              envoyerTexteAuServeur("#CTR#LOGIN#"+id);
          }
      }
   }

   private void fermerSession() {
      connexionEnCours = false;
      sessionEnCours = false;
      gui.desactiverEtatConnexion();
      gui.desactiverEnvoi();
      if (socket != null)
        try {socket.close();} catch (IOException e ) {}
   }

   private void seDeconnecter() {
       if (connexionEnCours) {    // Programmation défensive
             envoyerTexteAuServeur ("#CTR#LOGOUT");
      }
   }

   private boolean seConnecter () {
   // Retourne "true" si la connexion est établie, "false" dans les autres cas
        if (connexionEnCours) return true;

        InetAddress adresseIP;
        //------------ Essayer l'adresse
        try {
            //adresseIP = InetAddress.getByName("xxx.xxx.xxx.xxx");
            adresseIP = InetAddress.getLocalHost();   // Connexion en local
            //System.out.println( adresseIP.getHostAddress());
            //System.out.println( adresseIP.getHostName());
        }
        catch (UnknownHostException e) {
              gui.afficherAvertissement
                   ("!! PROBLEME DE CONNEXION !!", e.toString());
              return false;
        }
        //------------- Création de la socket permettant de communiquer avec
        //              le serveur
        try {
            socket = new Socket (adresseIP, NO_PORT);
        }
        catch (IOException e) {
              gui.afficherAvertissement
                   ("!! PROBLEME DE CONNEXION !!" ,e.toString());
              return false;
        }
        //------------- Ouverture des canaux d'entrée et de sortie
        try {
            canalSeSortie = new PrintWriter
                              (socket.getOutputStream());
            ecouteurDuServeur = new ServeurEcouteur
                                   (socket.getInputStream(),
                                    gui,
                                    this);

            return true;    // Connexion OK
        }
        catch (IOException e) {
            try {socket.close(); } catch (IOException ioe) {};
            gui.afficherAvertissement
                   ("!! PROBLEME DE CONNEXION !!", e.toString());
            return false;
        }
   }
}

