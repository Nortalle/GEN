//******************************************************************************
// ServeurEcouteur.java
// E.Lefrançois 10 février 2001
// Projet "Chat"
//******************************************************************************
import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.StringTokenizer;

//==============================================================================
// Classe "ServeurEcouteur"
// Activité concurrente dont le rôle consiste à écouter toutes les informations
// envoyées par le serveur.
// La connexion avec le serveur est contrôlée de manière permanente.  En cas de
// problème, cette activité affiche un message d'avertissement et stoppe son
// exécution.
//==============================================================================
public class ServeurEcouteur extends Thread {
// Variables d'instance


    /**
     * @supplierRole canalDentree 
     */
    private BufferedReader canalDentree; // Canal d'entrée avec le serveur


    /**
     * @supplierRole a_gui 
     */
    private InterfaceUtilisateur a_gui;  // Interface utilisateur (boutons,
                                         // zones d'affichage)


    /**
     * @supplierRole a_client 
     */
    private Client a_client;            // client associé

// Constructeur(s)
   public ServeurEcouteur (InputStream is, InterfaceUtilisateur gui,
                           Client client) {
      a_client = client;
      a_gui = gui;

      try {
          canalDentree = new BufferedReader (new InputStreamReader(is));
          this.start();		// Pour démarrer l'activité concurrente
      }
      catch (Exception e) {
         a_gui.afficherAvertissement ("!! PROBLEME DE CONNEXION !!",
                                       e.toString());
      }
   }


// Code de l'activité concurrente
   public void run () {
          String message = "";
          String ligneRecue;
          try {
              while (true) {
                    ligneRecue = canalDentree.readLine();
                    if (ligneRecue == null) {   // Connexion interrompue
                        message = "Terminaison normale";    // par le serveur
                        break;        // ("null"signale la fin d'un stream)
                    }
                    analyserLigneRecue(ligneRecue);
              }
          }

          catch (IOException e) { message = e.toString(); }
          finally {
                a_client.ev_connexionFermee (message);
          }
   }

   private void analyserLigneRecue(String ligne) {
      StringTokenizer st = new StringTokenizer (ligne, "#");
      try {
          String typeInfo = st.nextToken();
          if (typeInfo.equals("CTR")){
              String info = st.nextToken();
              if (info.equals("LOGIN_OK")) a_client.ev_sessionAcceptee();
              else if (info.equals("LOGIN_ERROR"))
                  a_client.ev_sessionRefusee(" !! SESSION REFUSEE !! " +
                                             st.nextToken());
              else if (info.equals("CONNEXION_FERMEE"))
                  a_client.ev_connexionFermee("FIN SESSION " + st.nextToken());
              else signalerErreur(ligne);
          }
          else if (typeInfo.equals("DATA")){
              String idSource = st.nextToken();
              a_gui.afficherDansZoneChat  (idSource, st.nextToken());
          }
          else signalerErreur(ligne);
      }
      catch (Exception e) {signalerErreur(ligne);}
   }

   private void signalerErreur (String ligneRecue) {
      a_gui.afficherAvertissement
         (" !! INFORMATION RECUE ERRONEE !! ", ligneRecue);
   }
}



