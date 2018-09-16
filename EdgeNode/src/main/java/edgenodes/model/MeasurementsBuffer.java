package edgenodes.model;

import cloudserver.model.SmartCity;
import edgenodes.NodeMain;
import edgenodes.utility.Utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Vector;

public class MeasurementsBuffer {

	private List<SmartCity.NodeMeasurement> measurementsBuffer;

	private List<SmartCity.NodeMeasurement> inPendingMeasurements;

	private static MeasurementsBuffer buffer;

	private MeasurementsBuffer () {
		this.measurementsBuffer = new Vector<>();
		this.inPendingMeasurements = new Vector<>();
	}

	public synchronized static MeasurementsBuffer getInstance () {
		if (buffer == null) {
			buffer = new MeasurementsBuffer();
		}
		return buffer;
	}

	public synchronized List<SmartCity.NodeMeasurement> getMeasurementsBuffer () {
		return this.measurementsBuffer;
	}

	public synchronized List<SmartCity.NodeMeasurement> getInPendingMeasurements () {
		return this.inPendingMeasurements;
	}

	public synchronized void addInPendingMeasurement (SmartCity.NodeMeasurement measurement) {
		this.inPendingMeasurements.add(measurement);
	}

	public synchronized void setMeasurementsBuffer (List<SmartCity.NodeMeasurement> measurements) {
		this.measurementsBuffer = measurements;
	}

/*
	public synchronized void setGlobal (SmartCity.NodeMeasurement global) {
		this.global = global;
	}
*/

	/**
	 * Una volta ricevuta la misurazione, se sono state raccolte più di 40 misurazionioni, devono essere spedite al
	 * nodo coordinatore e si deve utilizzare lo sliding window overlappato al 50% perciò le ultime 20 di queste 40 devono essere
	 * tenute per il giro successivo. Se durante la comunicazione con il nodo coordinatore si nota che non è più attivo
	 * si deve indire l'elezione.
	 *
	 * @param m
	 */
	public void addMeasurement (SmartCity.Node node, SmartCity.NodeMeasurement m) {
		MeasurementsBufferLock.getInstance().lock();
		//ElectionSingleton.getInstance().lock();
		System.out.println("Thread: " + Thread.currentThread().getId() + " Ho acquisito i locks");
		this.measurementsBuffer.add(m);
		//this.measurementsBuffer.sort(Utility.getComparator());
		if (this.measurementsBuffer.size() >= 40) {
			System.out.println("Superate 40 misurazioni");
			NodeMain.calculateOverlappedStats(node, MeasurementsBuffer.getInstance());
		}
		//ElectionSingleton.getInstance().unlock();
		MeasurementsBufferLock.getInstance().unlock();
	}

}
