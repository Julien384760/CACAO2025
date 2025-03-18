package abstraction.eq4Transformateur1;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import abstraction.eqXRomu.acteurs.Romu;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IFabricantChocolatDeMarque;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;

/**
 * @author YAOU Reda
 */

public class Transformateur1Stocks extends Transformateur1Acteur implements IFabricantChocolatDeMarque {

	private double coutStockage; 
	protected double STOCK_MAX_TOTAL_FEVES = 100000;

	private List<ChocolatDeMarque> chocosProduits; // la liste de toutes les sortes de ChocolatDeMarque que l'acteur produit et peut vendre
	protected HashMap<Feve, HashMap<Chocolat, Double>> pourcentageTransfo; // pour les differentes feves, le chocolat qu'elles peuvent contribuer a produire avec le ratio
	protected List<ChocolatDeMarque> chocolatsLimDt; // la liste des chocolats de marque "LimDt" que l'acteur produit

	public Transformateur1Stocks() {
		super();
		this.chocosProduits = new LinkedList<ChocolatDeMarque>();
		this.pourcentageTransfo = new HashMap<Feve, HashMap<Chocolat, Double>>();
		this.chocolatsLimDt=new LinkedList<ChocolatDeMarque>();
	}
	
	public void initialiser() {

		super.initialiser();

		this.coutStockage = Filiere.LA_FILIERE.getParametre("cout moyen stockage producteur").getValeur()*4;

		this.lesFeves = new LinkedList<Feve>();
		for (Feve f : Feve.values()) {
			this.lesFeves.add(f);
		}
		
		for (Feve f : this.lesFeves) {
			this.stockFeves.put(f, 20000.0);
			this.totalStocksFeves.ajouter(this, 20000.0, this.cryptogramme);
			this.journalStock.ajouter("ajout de 20000 de "+f+" au stock de feves --> total="+this.totalStocksFeves.getValeur(this.cryptogramme));
		}
		
		for (Chocolat c : Chocolat.values()) {
			this.stockChoco.put(c, 20000.0);
			this.totalStocksChoco.ajouter(this, 20000.0, this.cryptogramme);
			this.journalStock.ajouter("ajout de 20000 de "+c+" au stock de chocolat --> total="+this.totalStocksFeves.getValeur(this.cryptogramme));
		}
		
		
		this.pourcentageTransfo.put(Feve.F_HQ_BE, new HashMap<Chocolat, Double>());
		double conversion = 1.0 + (100.0 - Filiere.LA_FILIERE.getParametre("pourcentage min cacao HQ").getValeur())/100.0;
		this.pourcentageTransfo.get(Feve.F_HQ_BE).put(Chocolat.C_HQ_BE, conversion);// la masse de chocolat obtenue est plus importante que la masse de feve vue l'ajout d'autres ingredients

		this.pourcentageTransfo.put(Feve.F_MQ_E, new HashMap<Chocolat, Double>());
		conversion = 1.0 + (100.0 - Filiere.LA_FILIERE.getParametre("pourcentage min cacao MQ").getValeur())/100.0;
		this.pourcentageTransfo.get(Feve.F_MQ_E).put(Chocolat.C_MQ_E, conversion);

		this.pourcentageTransfo.put(Feve.F_BQ_E, new HashMap<Chocolat, Double>());
		conversion = 1.0 + (100.0 - Filiere.LA_FILIERE.getParametre("pourcentage min cacao BQ").getValeur())/100.0;
		this.pourcentageTransfo.get(Feve.F_BQ_E).put(Chocolat.C_BQ_E, conversion);

		this.pourcentageTransfo.put(Feve.F_BQ, new HashMap<Chocolat, Double>());
		conversion = 1.0 + (100.0 - Filiere.LA_FILIERE.getParametre("pourcentage min cacao BQ").getValeur())/100.0;
		this.pourcentageTransfo.get(Feve.F_BQ).put(Chocolat.C_BQ, conversion);

		this.journalStock.ajouter(Romu.COLOR_LLGRAY, Color.PINK, "Stock initial chocolat de marque : ");
		
		for (Feve f : Feve.values()) {
			if (this.pourcentageTransfo.keySet().contains(f)) {
				for (Chocolat c : this.pourcentageTransfo.get(f).keySet()) {
					int pourcentageCacao =  (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+c.getGamme()).getValeur());
					ChocolatDeMarque cm= new ChocolatDeMarque(c, "LimDt", pourcentageCacao);
					this.chocolatsLimDt.add(cm);
					this.stockChocoMarque.put(cm, 40000.0);
					this.journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN," stock("+cm+")->"+this.stockChocoMarque.get(cm));
				}
			}
		}
	}

	////////////////////////////////////////////////////////
	//         En lien avec l'interface graphique         //
	////////////////////////////////////////////////////////

	public void next() {
		super.next();

		this.journal.ajouter("N° Etape " + Filiere.LA_FILIERE.getEtape());
		this.journal.ajouter("Solde : " + this.getSolde());

		this.journalStock.ajouter("N° Etape " + Filiere.LA_FILIERE.getEtape());
		this.journalCC.ajouter("N° Etape " + Filiere.LA_FILIERE.getEtape());
		this.journalTransactions.ajouter("N° Etape " + Filiere.LA_FILIERE.getEtape());
		
		this.journalStock.ajouter("Stock de fèves : " + this.totalStocksFeves.getValeur(this.cryptogramme));
		this.journalStock.ajouter("Stock de chocolat : " + this.totalStocksChoco.getValeur(this.cryptogramme));

		this.journalStock.ajouter("=== STOCKS === ");
		for (Feve f : this.lesFeves) {
			this.journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN,"Stock de "+Journal.texteSurUneLargeurDe(f+"", 15)+" = "+this.stockFeves.get(f));
		}
		for (Chocolat c : Chocolat.values()) {
			this.journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN,"Stock de "+Journal.texteSurUneLargeurDe(c+"", 15)+" = "+this.stockChoco.get(c));
		}
		if (this.stockChocoMarque.keySet().size()>0) {
			for (ChocolatDeMarque cm : this.stockChocoMarque.keySet()) {
				this.journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN,"Stock de "+Journal.texteSurUneLargeurDe(cm+"", 15)+" = "+this.stockChocoMarque.get(cm));
			}
		}

		Filiere.LA_FILIERE.getBanque().payerCout(this, cryptogramme, "Stockage", (this.totalStocksFeves.getValeur(cryptogramme)+this.totalStocksChoco.getValeur(cryptogramme)+this.totalStocksChocoMarque.getValeur(cryptogramme))*this.coutStockage);

		for (Feve f : this.pourcentageTransfo.keySet()) {
			for (Chocolat c : this.pourcentageTransfo.get(f).keySet()) {
				double transfo = 0.8 * stockFeves.get(f);
				if (transfo>0) {
					this.stockFeves.put(f, this.stockFeves.get(f)-transfo);
					this.totalStocksFeves.retirer(this, transfo, this.cryptogramme);

					//A MODIFIER 
					// La moitie sera stockee sous forme de chocolat, l'autre moitie directement etiquetee "LimDt"
					//Le facteur 0.5 pour répartir en choco marqué et non marqué est arbitraire, il pourra êtere modifié
					double repartitionMarque = 0.5;
					this.stockChoco.put(c, this.stockChoco.get(c)+((transfo*repartitionMarque)*this.pourcentageTransfo.get(f).get(c)));
					int pourcentageCacao =  (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+c.getGamme()).getValeur());
					ChocolatDeMarque cm= new ChocolatDeMarque(c, "LimDt", pourcentageCacao);
					double scm = this.stockChocoMarque.keySet().contains(cm) ?this.stockChocoMarque.get(cm) : 0.0;
					this.stockChocoMarque.put(cm, scm+((transfo*repartitionMarque)*this.pourcentageTransfo.get(f).get(c)));

					this.totalStocksChocoMarque.ajouter(this, ((transfo*repartitionMarque)*this.pourcentageTransfo.get(f).get(c)), this.cryptogramme);
					this.totalStocksChoco.ajouter(this, ((transfo*repartitionMarque)*this.pourcentageTransfo.get(f).get(c)), this.cryptogramme);


					this.journalStock.ajouter(Romu.COLOR_LLGRAY, Color.PINK, "Transfo de "+(transfo<10?" "+transfo:transfo)+" T de "+f+" en "+Journal.doubleSur(transfo*this.pourcentageTransfo.get(f).get(c),3,2)+" T de "+c);
					this.journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN," stock("+f+")->"+this.stockFeves.get(f));
					this.journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN," stock("+c+")->"+this.stockChoco.get(c));
					this.journalStock.ajouter(Romu.COLOR_LLGRAY, Romu.COLOR_BROWN," stock("+cm+")->"+this.stockChocoMarque.get(cm));
				}
			}
		}
	}

	////////////////////////////////////////////////////////
	//        Pour la creation de filieres de test        //
	////////////////////////////////////////////////////////


	public List<String> getMarquesChocolat() {
		LinkedList<String> marques = new LinkedList<String>();
		marques.add("LimDt");
		return marques;
	}

	public List<ChocolatDeMarque> getChocolatsProduits() {
		if (this.chocosProduits.size()==0) {
			for (Chocolat c : Chocolat.values()) {
				int pourcentageCacao =  (int) (Filiere.LA_FILIERE.getParametre("pourcentage min cacao "+c.getGamme()).getValeur());
				this.chocosProduits.add(new ChocolatDeMarque(c, "LimDt", pourcentageCacao));
			}
		}
		return this.chocosProduits;
	}

}
