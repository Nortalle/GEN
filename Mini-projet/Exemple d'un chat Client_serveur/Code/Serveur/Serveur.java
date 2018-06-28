//******************************************************************************
// Server.java
// E.Lefrançois 1er janvier 2000
// Projet "Chat"
//******************************************************************************
import java.util.*;
import java.net.*;
import java.lang.System;
import java.io.*;

// Pour accès à SQL
import java.sql.*;
import java.net.URL;

//==============================================================================
// Application de type "Standalone"
// Le serveur est un objet actif.
// Cet objet attend des requêtes de connexion.  A cette fin, une instance de
// "ServerSocket" est créée.  Cette insatnce attend une demande de connexion
// et retourne une socquette associée à cette connexion.
// Une fois que la socquette est créée, le programme crèe une instance de
// "Connection".  Cette instance sera responsable de la communication avec le
// client.  Cette "Connection" est ajoutée à la liste dynamique rassemblant
// toutes les connexions.
//
// Enfin, le serveur crèe une instance de "ConnectionsController", un objet
// responsable de contrôler périodiquement chacune des connexions, et de retirer
// de la liste toute connexion qui serait repérée inactive.
//==============================================================================
public class Serveur implements Runnable  {
   private Thread      activité = null;       // Activité associée

   private Vector      listeDesConnexions;	      // Liste des connexions avec
                                              // les clients

   private ServerSocket socketServeur;
           // La "socketServeur" attend des demandes de connexions opérées
           // sur le no de port associé.
           // La taille de la queue d'entrée est de 50 (nombres max. de demandes
           // de connexion en attente).

   private ControleurConnexions leControleurDesConnexions;
           // Pour contrôler périodiquement
           // l'activité de chaque connexion

   private static final int NO_PORT = 5002;
           // No de port associé à ce service

   // Constructeur
   public Serveur() {
        listeDesConnexions = new Vector();
        leControleurDesConnexions = new ControleurConnexions (listeDesConnexions);
        try {
                socketServeur = new ServerSocket (NO_PORT);
        }
        catch (IOException e) {
                System.err.println (e.getMessage());
                System.exit(1);  // Status == "1" pour signaler
                                 // une terminaison anormale de l'application
        }
        activité = new Thread (this);
        activité.start();
   }

   // Méthodes publiques
   public static void main (String args[]) {
        new Serveur();
   }

   public void run () {
        System.out.println ("Attend une nouvelle connexion ...........");
        while (true) {
                try {
                        // Attendre une nouvelle connexion
                        Socket nouvelleSocket = socketServeur.accept();
                           // Attend qu'une demande de connexion soit reçue par
                           // cette socket, et accepte cette demande
                           // Cette méthode bloque l'activité jusqu'à ce qu'une
                           // connexion soit faite
                        Connexion nouvelleConnexion =
                                new Connexion (nouvelleSocket,
                                               leControleurDesConnexions,
                                               listeDesConnexions);
                        synchronized (listeDesConnexions) {
                             listeDesConnexions.addElement (nouvelleConnexion);
                        }
                }
                catch (IOException e) {
                        System.err.println (e.getMessage());
                        System.exit (1);
                }
        }
   }
}

