package abstraction.eq4Transformateur1;

import abstraction.eqXRomu.appelDOffre.AppelDOffre;
import abstraction.eqXRomu.appelDOffre.IVendeurAO;
import abstraction.eqXRomu.appelDOffre.OffreVente;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import java.util.Scanner;

/**
 * @author YAOU Reda
 */

public class Transformateur1VendeurAppelDoffre extends Transformateur1Stocks implements IVendeurAO {

    public Transformateur1VendeurAppelDoffre() {
		super();
	}

	@Override
	public OffreVente proposerVente(AppelDOffre offre) {
		System.err.println(offre.toString());
		if (super.getChocolatsProduits().contains(offre.getProduit()) && offre.getQuantiteT() <= stockChocoMarque.get(offre.getProduit())) {
            Scanner scanner = new Scanner(System.in);
			System.out.println("Proposer votre prix de vente:");
			double prix = scanner.nextDouble();
			scanner.close();
			this.journalTransactions.ajouter("Je propose " + offre.getQuantiteT() + " tonnes de " + offre.getProduit() + " au cours de " + prix + " euros par tonne.");
			return new OffreVente(offre, this, offre.getProduit(), prix);
		} else {
			return null;	
		}
	}

	@Override
	public void notifierVenteAO(OffreVente propositionRetenue) {
		System.out.println("Votre proposition de vente a été retenue");
		this.journalTransactions.ajouter("J'ai vendu " + propositionRetenue.getQuantiteT() + " tonnes de " + propositionRetenue.getProduit() + " au cours de " + propositionRetenue.getPrixT() + " euros par tonne.");
	
		/* Mettre à jour les autres variables */
		stockChocoMarque.put((ChocolatDeMarque)(propositionRetenue.getProduit()), stockChocoMarque.get(propositionRetenue.getProduit()) - propositionRetenue.getQuantiteT());
		this.journalTransactions.ajouter("J'ai maintenant " + this.stockChocoMarque.get(propositionRetenue.getProduit()) + " tonnes de " + propositionRetenue.getProduit() + " en stock.");

		totalStocksChocoMarque.setValeur(this, this.totalStocksChocoMarque.getValeur(this.cryptogramme) - propositionRetenue.getQuantiteT(), this.cryptogramme); 
		this.journalTransactions.ajouter("J'ai maintenant " + this.totalStocksChocoMarque.getValeur(this.cryptogramme) + " tonnes de chocolat de marque en stock.");

		VolumeTotalDeStock.setValeur(this, VolumeTotalDeStock.getValeur(this.cryptogramme) - propositionRetenue.getQuantiteT(), this.cryptogramme);
		this.journalTransactions.ajouter("J'ai maintenant " + this.VolumeTotalDeStock.getValeur(this.cryptogramme) + " tonnes de chocolat en stock.");
	}
	

	@Override
	public void notifierPropositionNonRetenueAO(OffreVente propositionRefusee) {
		System.out.println("Votre proposition de vente n'a pas été retenue");
		this.journalTransactions.ajouter("J'ai proposé " + propositionRefusee.getQuantiteT() + " tonnes de " + propositionRefusee.getProduit() + " au cours de " + propositionRefusee.getPrixT() + " euros par tonne mais elle n'a pas été retenue.");
	}
}


