
class Chien implements Runnable {

  private Os os;            // os associé
  private Thread activite;
  private String nom;

  public Chien (String nom, Os os) {
    this.nom = nom;
    this.os = os;
    activite = new Thread(this);
    activite.start();
  }

  public void run() {
    boolean osTermine = false;
    while (! osTermine) {
      synchronized (os) {
        if (!os.estMange()) {
          os.mordre();
          System.out.println (nom + "Gloups");
        }
        else {
          osTermine = true;
        }
      }
      if (!osTermine){
        try { Thread.sleep(1000); }
        catch (InterruptedException e) {}
      }
    }

  } 


}

class Os {
  private int taille;
  public Os (int taille) {this.taille = taille;}

  public synchronized void mordre() {
    if (taille <= 0) throw new RuntimeException("Os deja mange");
    taille--;
  }
  public boolean estMange() {return taille == 0; }
}
