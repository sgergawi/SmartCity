package edgenodes.controller;

import cloudserver.model.SmartCity;
import edgenodes.NodeMain;
import edgenodes.model.*;

public class GlobalStatisticsThread extends Thread {
	private SmartCity.Node node;

	public GlobalStatisticsThread (SmartCity.Node node) {
		this.node = node;
	}

	@Override
	public void run () {
		while (true) {
			ElectionInProgressSemaphore.getInstance().blockMeIfElectionInProgress();
			//ElectionLock.getInstance().lock();
			try {
				System.out.println("Thread " + Thread.currentThread().getId() + ": aggiornamento ?");
				Thread.sleep(5000);
				GlobalStatistic globalSituation = GlobalStatistic.getInstance();
				globalSituation.updateGlobal();
				if (globalSituation.getGlobal() != null) {
					System.out.println("Thread " + Thread.currentThread().getId() + ":ho calcolato una nuova global: " + globalSituation.getGlobal().getValue() + " " + globalSituation.getGlobal().getTimestamp());
					globalSituation.addLocalStatistics(this.node, GlobalStatistic.getInstance().getGlobal());
				}
				if (Coordinator.getInstance().getCoordinator().getId() == this.node.getId()) {
					//Questo nodo è il coordinatore perciò deve comunicare con il server
					NodeMain.sendGlobalStatisticsToServer();
				} else {
					//Il nodo non è coordinatore ma nodo interno perciò deve comunicare con altri nodi
					NodeMain.sendGlobalsLocalsToFather(this.node);
				}
				//GlobalStatistic.getInstance().clearLocals();
				GlobalStatistic.getInstance().setGlobal(null);
				GlobalStatistic.getInstance().clearGlobals();
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.out.println("Errore :- Il calcolo della statistica globale è stato interrotto");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Errore :- si è verificato un errore generico durante l'aggiornamento della statistica globale");
			}
			ElectionInProgressSemaphore.getInstance().exit();
			//ElectionLock.getInstance().unlock();
		}
	}
}
