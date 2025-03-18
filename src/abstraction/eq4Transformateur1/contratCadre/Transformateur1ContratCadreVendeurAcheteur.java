package abstraction.eq4Transformateur1.contratCadre;

import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.produits.IProduit;
import abstraction.eqXRomu.contratsCadres.*;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Chocolat;

import java.util.List;
import java.util.LinkedList;

public class Transformateur1ContratCadreVendeurAcheteur extends Transformateur1ContratCadreVendeur implements IAcheteurContratCadre {
    
    protected List<ExemplaireContratCadre> mesContratEnTantQuAcheteur;
    protected double qttInitialementVoulue;	
	protected double prixInitialementVoulu;
	protected double epsilon;


	//A MODIFIER 
	//Adapater l'écriture pour prendre en compte la nouvelle gestion des stocks
	public Transformateur1ContratCadreVendeurAcheteur() {
		super();
		this.mesContratEnTantQuAcheteur=new LinkedList<ExemplaireContratCadre>();
        this.epsilon  = 0.1;
        this.qttInitialementVoulue = 11;//On cherche à acheter de quoi remplir notre stock //A MODIFIER
        this.prixInitialementVoulu = 0.75*9500; //Une valeur arbitraire s'appuyant sur le prix moyen des fèves de cacao en 2024
	}

	public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {
		//On ne contrepropose que si la quantité figurant dans le contrat actuellement est supérieur au minimum de quantité totale de marchandise
		if (contrat.getEcheancier().getQuantiteTotale() > SuperviseurVentesContratCadre.QUANTITE_MIN_ECHEANCIER) {
			//Si la qtt proposée est cohérente avec la quantité que nous voulions initialement, on accepte l'echeancier
			if (Math.abs(this.qttInitialementVoulue-contrat.getEcheancier().getQuantiteTotale())/this.qttInitialementVoulue <= epsilon){
				return contrat.getEcheancier();
			}
			//Sinon on négocie par dichotomie
			else{
				//Mise à jour de l'échéancier pour prendre en compte ces modifications
				Echeancier e = contrat.getEcheancier();
				//On procède par dichotomie en augmentant ou diminuant le volume de vente de nos produits
				double delta = contrat.getEcheancier().getQuantiteTotale()- qttInitialementVoulue;
				double signe = delta/Math.abs(delta);
				this.qttInitialementVoulue = this.qttInitialementVoulue * (1 + signe*0.125);//si detla<0, proposition trop faible donc on diminue notre demande
																							//si delta>0, proposition trop forte donc on augmante notre demande

				//Redistribution uniforme de la hausse ou de la baisse des qtt vendues 
				for(int i = e.getStepDebut() ; i< e.getStepFin() ; i++){
					double qtti = e.getQuantite(i);
					e.set(i, qtti*(1+signe*0.125));
				}
				if (e.getQuantiteTotale()>SuperviseurVentesContratCadre.QUANTITE_MIN_ECHEANCIER) return e;
				else return null;
			}
		}
		else {
			return null;
		}
	}
	

	public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
		//Si le prix est aberrant, on refuse d'office la négociation
        if (contrat.getPrix() > 20000){
			return -1;
		}
		else{
			//On procède par dichotomie sur le prix proposé et notre prix voulu.
			//Si le prix proposé est inférieur à notre prix, on accepte le contrat
			if(contrat.getPrix() < this.prixInitialementVoulu){
				return contrat.getPrix(); 
			}

			//Sinon on vérifie si le prix est cohérent avec le notre d'un seuil epsilon
			if (Math.abs((contrat.getPrix()-prixInitialementVoulu)/prixInitialementVoulu) <= this.epsilon ){
				return contrat.getPrix();
			}
			//Sinon on contre-porpose un prix intermédiaire par rapport au prix proposé
			else{
				this.prixInitialementVoulu = this.prixInitialementVoulu + (contrat.getPrix()-prixInitialementVoulu)*0.2;
				return this.prixInitialementVoulu;
			}
		}
	}


	public void next() {
		super.next();

		// On enleve les contrats obsolete (nous pourrions vouloir les conserver pour "archive"...)
		List<ExemplaireContratCadre> contratsObsoletes=new LinkedList<ExemplaireContratCadre>();
		for (ExemplaireContratCadre contrat : this.mesContratEnTantQuAcheteur) {
			if (contrat.getQuantiteRestantALivrer()==0.0 && contrat.getMontantRestantARegler()==0.0) {
				contratsObsoletes.add(contrat);
			}
		}
		this.mesContratEnTantQuAcheteur.removeAll(contratsObsoletes);
		
		//A MODIFIER
		//Pour tous nos produits, on cherche des contrats cadre pour acheter des fèves
		//Cette rechercche est systématique, il faudrait en réalité prendre en compte la nécessité de faire un CC
		if (totalStocksFeves.getValeur()< 0.75*this.STOCK_MAX_TOTAL_FEVES){
		
			journalCC.ajouter("Recherche d'un vendeur aupres de qui acheter des fèves");
			for(IProduit produit : stockFeves.keySet()){
				
				List<IVendeurContratCadre> vendeurs = supCCadre.getVendeurs(produit);
				if (vendeurs.contains(this)) {
					vendeurs.remove(this);
				}
				IVendeurContratCadre vendeur = null;
				if (vendeurs.size()==1) {
					vendeur=vendeurs.get(0);
				} else if (vendeurs.size()>1) {
					//A MODIFIER
					//Recherche aléatoire d'un vendeur
					vendeur = vendeurs.get((int)( Filiere.random.nextDouble()*vendeurs.size()));
				}
				if (vendeur!=null) {
					//A MODIFIER
					//Echéancier à modifier
					journalCC.ajouter("Demande au superviseur de debuter les negociations pour un contrat cadre de "+produit+" avec le vendeur "+vendeur);
					ExemplaireContratCadre cc = supCCadre.demandeAcheteur((IAcheteurContratCadre)this, vendeur, produit, new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 10, 20.), cryptogramme,false);
					journalCC.ajouter("-->aboutit au contrat "+cc);
				}
			}
			
		}

		//A MODIFIER
		//Pour tous nos produits, on cherche des contrats cadre pour vendre notre production
		//Cette rechercche est systématique, il faudrait en réalité prendre en compte la nécessité de faire un CC
		journalCC.ajouter("Recherche d'un acheteur aupres de qui vendre");
		for (Chocolat produit : stockChoco.keySet()){
			// Proposition d'un contrat a un des achteur choisi aleatoirement

			List<IAcheteurContratCadre> acheteurs = supCCadre.getAcheteurs(produit);
			if (acheteurs.contains(this)) {
				acheteurs.remove(this);
			}
			IAcheteurContratCadre acheteur = null;
			if (acheteurs.size()==1) {
				acheteur=acheteurs.get(0);
			} else if (acheteurs.size()>1) {
				acheteur = acheteurs.get((int)( Filiere.random.nextDouble()*acheteurs.size()));
			}
			if (acheteur!=null) {
				journalCC.ajouter("Demande au superviseur de debuter les negociations pour un contrat cadre de "+produit+" avec l'acheteur "+acheteur);
				ExemplaireContratCadre cc = supCCadre.demandeVendeur(acheteur, (IVendeurContratCadre)this, produit, new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 10, (SuperviseurVentesContratCadre.QUANTITE_MIN_ECHEANCIER+10.0)/10), cryptogramme,false);
				journalCC.ajouter("-->aboutit au contrat "+cc);
			}
		}
	}

	public void receptionner(IProduit produit, double quantiteEnTonnes, ExemplaireContratCadre contrat) {
		totalStocksFeves.ajouter(this, quantiteEnTonnes, cryptogramme); 
        double currStockFeves = stockFeves.get((Feve) produit);
        stockFeves.put((Feve) produit, currStockFeves+quantiteEnTonnes);
        journalStock.ajouter("Reception de " + quantiteEnTonnes +"feves " + ((Feve)produit).getGamme() + "(CC avec" + contrat.getVendeur() + ")");
	}

	public boolean achete(IProduit produit) {
		//Nous n'achetons que des fèves de cacao, pas de chocolat
		return stockFeves.keySet().contains(produit);
	}

	public String toString() {
		return this.getNom();
	}

	public int fixerPourcentageRSE(IAcheteurContratCadre acheteur, IVendeurContratCadre vendeur, IProduit produit,
			Echeancier echeancier, long cryptogramme, boolean tg) {
		return 5;
	}


}
