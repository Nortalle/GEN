**Mandatée par le client X, notre société de développement doit se déterminer quand au cycle de vie à mettre en oeuvre dans le cadre du projet à réaliser. Ce projet ne présente pas de risque particulier. Nous avons réalisé avec succès un projet très similaire il y a deux années en arrière. Que choisirons-nous ?**

- Cycle de vie classique
- ~~Cycle de vie en V~~
- ~~Cycle de vie en spirale~~
- ~~Cycle de vie itératif~~

**La lecture d'un modèle de domaine donne un certain nombre d'indications explicites sur la manière de concevoir l'architecture du logiciel. Notamment, des indications portées habituellement sur un modèle de domaine, on peut déduire**:

- Les interfaces potentielles que devrait implémenter telle ou telle classe
- Les variables d'instance potentielles devant être déclarées dans telles ou telles classe
- ~~Le caractère abstrait de telle ou telle classe~~
- ~~Les constructeurs à mettre en oeuvre dans telle ou telle classe~~

**Parmi les assertions suivantes, cochez celles qui reflètent correctement la méthode XP:**

- ~~Avec XP, une des toutes première itérations consiste à mettre en place l'architecture du système.~~
- ~~C'est le "manager", appuyé par le "coach", qui organise la répartition du travail entre les différents collaborateurs au sein de chaque itération.~~
- Avec XP, les spécifications détaillées sont écrites par le client lui-même. Il les exprime en écrivant les tests "de recette"(tests fonctionnels).
- Tests de recette et tests unitaires doivent être dans leur ensemble exécutés de manière automatique.
- Avec XP, seul le développeur est habilité à estimer le temps de réalisation d'un scénario

**Parmi les acteurs participant à un développement XP, dites quel est l'acteur principal qui accomplit chacune des activités énoncées ci-dessous:**

| Activités                                                 | Acteur principal    |
| --------------------------------------------------------- | ------------------- |
| Rédaction des scénarios                                   | Client              |
| Spécification des tests unitaires                         | testeur/Développeur |
| Spécification des tests fonctionnels                      | Client              |
| Spécification du plan d'itération                         | Client              |
| Détermination de la charge de développement d'un scénario | Développeur         |

**Méthodes XP : citez trois avantages de travailler en binôme**:

- Formation : un nouveau peut travailler avec un plus expérimenté
- Contrôle mutuel au fur-et-à-mesure
- Meilleure collaboration/Stratégie d'équipe(tournus réguliers)
- Personne n'est "indispensable" au projet

**Méthodes Agiles : Que signifie "Le processus de développement doit être adaptatifs" ?**

Que le processus doit être capable de s'adapter à des changements de besoins du clients, ou de changements dans l'équipe, sans compromettre le projet

**Méthodes XP et UP : Cochez les propositions qui vous paraissent vraies :**

- XP comme UP sont deux méthodes "conduites par les cas d'utilisation".
- ~~Avec XP, contrairement à UP, les scénarios(user stories) sont écrits au fur et à mesure des itérations plutôt qu'en une fois, avant de commencer le développement.~~
- ~~Au fil des itérations, modules par modules, XP comme UP complètent le code présenté au client en s'appuyant sur une architecture centrale élaborée dans l'une des première phase du développement.~~
- ~~Avec XP comme UP, il est préconisé d'écrire l'ensemble des classes métier(modèle de domaine) avant de commencer les itérations à proprement parler.~~

**Méthode XP pure: Que signifie "La méthode est pilotée par les tests" ?**

Cela signifie que le but principal est que le programme passe les tests qui ont été définis pour cette itération.

**Méthodes agiles : Parmi les assertions suivantes, cochez celles qui reflètent correctement la notion méthode agile.**

- La planification des itérations est un processus évolutif.
- L'observation de la vélocité d'une équipe de développement peut entraîner une révision de la planification des itérations.
- ~~Contrairement à UP, les méthodes agiles préconisent un développement itératif incrémental.~~
- ~~Les méthodes agiles souhaitent que l'équipe soit constituée de programmeurs "séniors", (avec expérience), des programmeurs suffisamment "agiles", susceptibles d'écrire directement leur code de manière propre et définitive.~~

**Problématique**

| Petits logiciels           | Gros logiciels           |
| -------------------------- | ------------------------ |
| Faciles à comprendre       | Difficile à comprendre   |
| Coûts faibles              | Coût énorme              |
| Développé par une personne | Développement par équipe |
| Rarement documenté         | Parfaitement documenté   |

**MVC**

```java
class Controleur {
    private Vue1 vue1;
    private Vue2 vue2;
    private Modele modele1, modele2;
    public Controleur() {
        modele1 = new Modele();
        modele2 = new Modele();
        vue2 = new Vue2();
        vue1 = new Vue1(modele1, modele1);
        modele1.addObserver (vue1);
        modele1.addObserver(vue2);
        modele2.addObserver(vue1);
        modele2.addObserver(modele1);π
    }
    public void changeVal (int val) {
    	m2.setVal(val);
    }
}

class Vue2 implements Observer {
	public void update (Observable obs, Object o) {
		System.out.println ((Modele)obs.getVal(););
	}
}

class Vue1 extends Vue2 {
	private Modele m2, m1;
	public Vue2(Modele m1, Modele m2) {
        this.m1=m1;
        this.m2=m2;
	}
	
	public void changeVal (int val) {
        m2.setVal(val);
        m1.setVal(val);
	}
}

class Modele extends Observable implements Observer {
    private int valeur;
    public void setVal (int val) {
        valeur=val;
        setChanged();
        notifyObservers();
	}

    public void update (Observable obs, Object o) {
        System.out.println ((Modele)obs.getVal(););
    }

    public int getVal() {return valeur ;}
}
```

**Observable/Observé**

```java
import java.util.Observer;
import java.util.Observable;
class Chrono {
    public static void main (String ... args) {
        ChronoView view = new ChronoView();
        Counter cents, seconds, minutes;
        minutes = new Counter (0, 60, null, view);
        seconds = new Counter (0, 60, minutes, view);
        cents = new Counter(10, 100, seconds, view);
        view.setCounters(minutes, seconds, cents);
        cents.go();
    }
}

class ChronoView implements Observer{
    private Counter[] counters;
    public ChronoView () {}
    public void setCounters(Counter ... counters){
    	this.counters= counters;
	}
    
    public void renderChronoState() {
        String txt = "";
        for (Counter cnt : counters) txt += cnt.getValue() + " ";
        System.out.println(txt);
    }
    
    public void update(Observable obs, Object arg) {
    	renderChronoState();
    }
}

class Counter implements Observer {
    private int value = 0;
    private int maxVal;
    private Counter next;
    private Observer view;
    private int pulse;
    
    class ChangedValueEventObservers extends Observable {
    	public void notifyObservers(){
    		this.setChanged(); super.notifyObservers();
    	}
	}
    
    class MaxValueReachedEventObservers extends Observable {
        public void notifyObservers(){
        	this.setChanged(); super.notifyObservers();
        }
    }
    
    private ChangedValueEventObservers changedValueEventObservers
    = new ChangedValueEventObservers();
    
    private MaxValueReachedEventObservers maxValueReachedEventObservers
    = new MaxValueReachedEventObservers();
    
    public Counter (int pulse, int maxValue,
    				Counter nextCounter, Observer view) {
    this.maxVal = maxValue;
    this.next = nextCounter;
    this.view = view;
    this.pulse = pulse;
        
    changedValueEventObservers.addObserver(view);
        
    if (nextCounter != null)
    	maxValueReachedEventObservers.addObserver(nextCounter);
    }
    public void go() {
        if (pulse > 0) {
            new Thread() {
                public void run() {
                    while(true) {
                        try {
                            Thread.sleep(pulse);
                            incrementValue();
                        }
                        catch (Exception e) {}
                    }
                }
            }.start();
        }
    }
    
    public int getValue() {return value;}
    
    public void update(Observable obs, Object arg) {
    	incrementValue();
    }
    
    public void incrementValue(){
        value = ++ value % (maxVal);
        if (value == 0)
        	maxValueReachedEventObservers.notifyObservers();
        changedValueEventObservers.notifyObservers();
    }
}
```

**La crise du logiciel des années 70 a eu pour conséquence - dans le courant des deux ou trois années qui ont suivi ce constat - d'opérer une remise en question du processus de développement logiciel et des outils utilisés. Notamment :**

- ~~L'arrêt de la programmation procédurale au profit d'une programmation orientée objet.~~
- ~~La promotion d'un cycle de vie de type itératif~~
- ~~La réduction systématique de la taille des équipes de développement~~
- La promotion de la phase d'analyse des besoins

**Dans une méthode de type UP, à quel moment et dans quelle phase doit-on prendre en compte ce que l'on appelle la "gestion des risques" ?**

On doit prendre en compte la gestion des risques dans la phase de l'élaboration, c'est à dire lorsqu'on va définir nos itérations qui à la fin de chacune permet d'avoir un résultat testé et exécutable. ==> Dans la phase d'initialisation on va également décider si on veut poursuivre le projet ou non, dé