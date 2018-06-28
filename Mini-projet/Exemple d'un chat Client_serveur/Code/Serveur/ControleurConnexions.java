//******************************************************************************
// ControleurConnexions.java
// E.Lefrançois 1er janvier 2000
// Projet "Chat"
//******************************************************************************
import java.lang.Thread;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.System;

//==============================================================================
// Objet actif
// Cet objet attend le signal "Fin de connexion", envoyé par un objet de type
// "Connexion"
// En réponse à un tel signal, cet objet enlève la connexion correspondante
// de la liste dynamique des connexions.
//
// Toutes les 10 secondes, si aucun signal n'est reçu, chaque connexion
// est contrôlée, puis détruite si inactive (ce qui ne devrait pas arriver
// normalement).
// Cet objet a un accès direct à la liste des connexions
// maintenue par l'objet de type "Serveur" (liste accédée en exclusion mutuelle)
//==============================================================================

class ControleurConnexions extends Thread {

// Variables d'instance
   private Vector a_listeDesConnexions;    // Liste des connexions
   private static final int DUREE_DE_VEILLE = 10000;
                                        // Périodicité de contrôle (msec)

// Constructeur(s)
   public ControleurConnexions (Vector liste) {
          a_listeDesConnexions = liste;
          this.start();	// Pour démarrer l'activité
   }

// Méthodes publiques
   public void finDeConnexion (Connexion c) {
   // Fin de la connexion "c"
       this.notify();
   }

// Activité
   public synchronized void  run () {
          while (true) {
              try {
                   this.wait (DUREE_DE_VEILLE);
                   // attend le signal "fin de connexion"
                   // (au travers d'un signal "notify()")
                   // Attend max. 10 sec.
                   this.enleveConnexionsInactives();
              }
              catch (InterruptedException e) {
                    // Thread interrompu par un autre Thread (au travers
                    // de la méthode "interrupt" de la classe Thread)
                    // Cet événement ne devrait pas arriver ici.
              }
          }
   }

// Méthodes privées
   private void enleveConnexionsInactives() {
       Connexion uneConnexion;
       synchronized (a_listeDesConnexions) {
           for (int cpt = a_listeDesConnexions.size()-1; cpt >= 0; cpt--) {
               uneConnexion =
                    (Connexion)a_listeDesConnexions.elementAt(cpt);
               if (! uneConnexion.isAlive()) {
                  a_listeDesConnexions.removeElementAt(cpt);
                  System.out.println ("Deconnexion d'un client");
               }
           }
       }
   }
}

