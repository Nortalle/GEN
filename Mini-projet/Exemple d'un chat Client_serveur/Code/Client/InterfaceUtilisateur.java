//******************************************************************************
// InterfaceUtilisateur.java
// E.Lefrançois 10 février 2001
// Projet "Chat
//******************************************************************************
import java.awt.*;
import java.awt.event.*;
import java.util.*;
////////////////////////////////////////////////////////////////////////////////
//==============================================================================
// classe LoginDialogue
// Saisie de l'identificateur du client
//==============================================================================

// -----------------------------------------------------------------------------
    class LoginDialogue extends Dialog {
      /**
       * @supplierRole a_controleur 
       */
      private Client a_controleur;  // Controleur associé (communication d'ev.)
      private TextField tf = new TextField (6);
      private Label message = new Label();
      private Button bOk = new Button ("OK");
      private Button bAnnuler = new Button ("Annuler");

      class Ecouteur implements ActionListener {
          private Window w;   // fenêtre associée
          public Ecouteur (Window w) {this.w = w;}
          public void actionPerformed(ActionEvent e) {
                if (e.getSource()==bAnnuler) { w.dispose();}
                else {
                    if (tf.getText().length() == 0) {
                        message.setText("Au moins 1 caractère !!");
                    }
                    else {
                        a_controleur.setIdClient(tf.getText());
                        w.dispose();
                    }
                }
          }
      }

      public LoginDialogue(Frame parent, Client controleur) {
          super (parent, "Chat - Ouverture Session");
          setModal(true);
          a_controleur = controleur;
          Label l_login = new Label ("Votre identificateur (6 car. max)");

          message.setForeground (Color.red);
          tf.addKeyListener( new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String s = tf.getText();
                if (s.length()> 6) {
                  tf.setText(s.substring(0,6));
                  tf.setCaretPosition(7);
                }
            }
          });
          Ecouteur ec = new Ecouteur (this);
          bOk.addActionListener(ec);
          bAnnuler.addActionListener (ec);
          Panel p1 = new Panel ();
          p1.add(l_login);
          p1.add(tf);
          add (p1, "North");
          Panel p2 = new Panel();
          p2.add (bOk);
          p2.add(bAnnuler);
          add (p2, "Center");
          add (message, "South");
          pack();
      }
    }

////////////////////////////////////////////////////////////////////////////////
//==============================================================================
// classe InterfaceUtilisateur
// Spécification de la liste des composants graphiques appartenant à l'interface
// utilisateur- Ecoute des événements à un premier niveau
//==============================================================================


class InterfaceUtilisateur extends Panel
                           implements Runnable  {

// Variables d'instance
    private Thread activite;    // Affichage "mobile", indiquant que la
                                // connexion est valide


    /**
     * @supplierRole a_controleur 
     */
    private Client a_controleur;        // Ecouteur des événements (client)


    // Déclaration des composants à visibilité globale
    private TextArea ta_message = new TextArea ("", 3, 40);
    private Label l_invite = new Label ("Message de ******");


    private boolean connexionActive;    // Etat de la connexion
    private Button b_connexion = new Button ("Se connecter .. ");
    private Button b_envoyer = new Button ("Envoyer");
    private Label l_temoinConnexion;                // Témoin de fonctionnement
    // Message d'avertissement
    private Label l_messageAvertissement = new Label(""),
                  l_detailAvertissement = new Label("");

    private TextArea ta_chat = new TextArea ("", 10, 80); // 10 lignes, 80 col.


// Constructeur
   public InterfaceUtilisateur (Client controleur){

          a_controleur = controleur;

          // Spécification de la fonte (applicable uniquement en mode "Applet")
          Font OldFnt = getFont();
          if (OldFnt != null) {
             int newFontSize = 12;
             Font NewFnt = new Font
               (OldFnt.getName(), OldFnt.getStyle(), newFontSize);
             setFont(NewFnt);
          }

          // Spécification du gestionnaire de présentation
          setLayout(new BorderLayout(0, 5));

          // Emplacement des panels
          Panel p_ms = new Panel();   // zone "Envoi de message"
          Panel p_cc = new Panel();   // zone "Chat collectif"
          Panel p_cx = new Panel();   // zone "Contrôle connexion"
          add (p_ms, "North");
          add (p_cc, "Center");
          add (p_cx, "South");

          // Panel - Envoi du message
          Button b_effacer = new Button ("Effacer");
          b_effacer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              ta_message.setText("");
            }
          });
          desactiverEnvoi();
          b_envoyer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                a_controleur.ev_envoyer();
            }
          });

          p_ms.setLayout(new BorderLayout());
          Panel p_ms1 = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 5));
          p_ms.add (p_ms1, "North");
          p_ms.add (ta_message);

          p_ms1.add (l_invite);
          p_ms1.add (b_envoyer);
          p_ms1.add (b_effacer);

          // Panel - Zone Chat Collectif
          Label l_titreZoneChat = new Label ("Zone Chat collective");
          p_cc.setLayout (new BorderLayout());
          p_cc.add (l_titreZoneChat, "North");
          p_cc.add (ta_chat, "Center");

          // Panel - Contrôle Connexion
          l_temoinConnexion = new Label ();
          l_temoinConnexion.setForeground (Color.red);
          b_connexion.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!connexionActive) {
                    a_controleur.ev_connecter();
                }
                else a_controleur.ev_deconnecter();
            }
          });
          p_cx.setLayout(new BorderLayout());
          p_cx.add(l_temoinConnexion, "Center");
          p_cx.add (b_connexion, "East");
          Panel p_avertissement = new Panel(new BorderLayout());
          p_avertissement.add (l_messageAvertissement, "North");
          p_avertissement.add (l_detailAvertissement, "South");
          p_cx.add (p_avertissement, "South");
    }


// Public methods
   public void run () {
     String txt = new String(">>        " +
                            "          " +
                            "          " +
                            "          " +
                            "          " +
                            "          "
                            );
      while (activite == Thread.currentThread()) {
        try {Thread.sleep(100);} catch (Exception e){};
        txt = new String (txt.charAt(59)+ txt);
        txt = txt.substring (0,60);
        l_temoinConnexion.setText (txt);
      }
   }

   public void desactiverEtatConnexion() {
          connexionActive = false;
          activite = null;
          b_connexion.setLabel("Se connecter .. ");
   }

   public void activerEtatConnexion() {
          connexionActive = true;
          b_connexion.setLabel("Se déconnecter");
          if (activite == null) {
            activite = new Thread(this);
            activite.start();
          }
   }

   public void activerEnvoi() {
          l_invite.setText ("Message de " + a_controleur.getIdClient());
          b_envoyer.setEnabled(true);
          b_envoyer.setBackground (Color.green);
   }

   public void desactiverEnvoi() {
          l_invite.setText ("Message de ******");
          b_envoyer.setEnabled(false);
          b_envoyer.setBackground (Color.gray);
   }

   public void afficherAvertissement (String message, String detail) {
      l_messageAvertissement.setText (message);
      l_detailAvertissement.setText (detail);
   }

    public void afficherDansZoneChat (String sourceId, String message) {
      ta_chat.append (sourceId);
      StringTokenizer st = new StringTokenizer (message, ""+(char)0);
      while (st.hasMoreTokens()){
          ta_chat.append ('\t'+ st.nextToken()+'\n');
      }
   }

   public void effacerAvertissement() {
       l_messageAvertissement.setText ("");
       l_detailAvertissement.setText ("");
   }

   public String getMessage () {
   // Retourner le message à envoyer au serveur
      return ta_message.getText();
   }

}


