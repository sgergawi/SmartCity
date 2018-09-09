package edgenodes.controller;

import cloudserver.model.SmartCity;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import edgenodes.NodeMain;
import edgenodes.model.Coordinator;
import edgenodes.model.GlobalStatistic;
import edgenodes.model.MeasurementsBuffer;

public class GlobalStatisticsThread extends Thread {
	private SmartCity.Node node;

	public GlobalStatisticsThread (SmartCity.Node node) {
		this.node = node;
	}

	@Override
	public void run () {
		while (true) {
			try {
				Thread.sleep(5000);
				System.out.println("Aggiornamento globals");
				GlobalStatistic globalSituation = GlobalStatistic.getInstance();
				globalSituation.updateGlobal();
				if (globalSituation.getGlobal() != null) {
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
				System.out.println("Il calcolo della statistica globale è stato interrotto");
			}

		}
	}
}
