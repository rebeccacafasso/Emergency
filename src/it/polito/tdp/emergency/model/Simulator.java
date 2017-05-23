package it.polito.tdp.emergency.model;

import java.util.PriorityQueue;

import it.polito.tdp.emergency.model.Event.EventType;
import it.polito.tdp.emergency.model.Patient.PatientStatus;

public class Simulator {

	// Simulation parameters

	private int NS; // number of studios

	private int DURATION_TRIAGE = 5 * 60;
	private int DURATION_WHITE = 10 * 60;
	private int DURATION_YELLOW = 15 * 60;
	private int DURATION_RED = 30 * 60;

	private int WHITE_TIMEOUT = 30 * 60;
	private int YELLOW_TIMEOUT = 30 * 60;
	private int RED_TIMEOUT = 60 * 60;

	// World model
	private PriorityQueue<Patient> waitingRoom;
	private int occupiedStudios = 0;

	// Measures of Interest
	private int patientsTreated = 0;
	private int patientsDead = 0;
	private int patientsAbandoned = 0;

	// Event queue
	private PriorityQueue<Event> queue;

	public Simulator(int NS) {
		this.NS = NS;

		this.queue = new PriorityQueue<>();
		this.waitingRoom = new PriorityQueue<>(new PatientComparator());
	}

	public void addPatient(Patient patient, int time) {
		patient.setStatus(PatientStatus.NEW);
		Event e = new Event(patient, time+DURATION_TRIAGE, EventType.TRIAGE) ;
		queue.add(e) ;
	}

	public void run() {
		while (!queue.isEmpty()) {
			Event e = queue.poll();
			System.out.println(e);

			switch (e.getType()) {
			case TRIAGE:
				processTriageEvent(e);
				break;
			case TIMEOUT:
				processTimeoutEvent(e);
				break;
			case FREE_STUDIO:
				processFreeStudioEvent(e);
				break;
			}
		}
	}

	/**
	 * A patient finished treatment. The studio is freed, and a new patient is
	 * called in.
	 * 
	 * @param e
	 */
	private void processFreeStudioEvent(Event e) {
		Patient p = e.getPatient() ;
		
		// un paziente ha liberato lo studio
		this.patientsTreated++ ;
		p.setStatus(PatientStatus.OUT);
		this.occupiedStudios-- ;
		
		// devo chiamare il prossimo paziente
		Patient next = waitingRoom.poll() ;
		
		if(next!=null) {
			int duration = 0 ;
			if(next.getStatus()==PatientStatus.WHITE)
				duration = DURATION_WHITE ;
			else if(next.getStatus()==PatientStatus.YELLOW)
				duration = DURATION_YELLOW ;
			else if(next.getStatus()==PatientStatus.RED)
				duration = DURATION_RED ;
			
			this.occupiedStudios++ ;
			next.setStatus(PatientStatus.TREATING);
			// eliminare i TIMEOUT FUTURI dalla coda degli eventi
			queue.add(new Event(next, e.getTime()+duration, EventType.FREE_STUDIO)) ;
		}
		
		
	}

	private void processTimeoutEvent(Event e) {

		Patient p = e.getPatient() ;
		
		switch(p.getStatus()) {
		case WHITE:
			// abbandona
			this.patientsAbandoned++ ;
			p.setStatus(PatientStatus.OUT);
			waitingRoom.remove(p) ;
			break;
			
		case YELLOW:
			// diventa rosso
			waitingRoom.remove(p) ;
			p.setStatus(PatientStatus.RED);
			waitingRoom.add(p) ;
			queue.add(new Event(p, e.getTime()+RED_TIMEOUT, EventType.TIMEOUT)) ;
			break ;
			
		case RED:
			// muori
			this.patientsDead++ ;
			p.setStatus(PatientStatus.BLACK);
			waitingRoom.remove(p) ;
			break ;
			
		case OUT:
		case TREATING:
			// timeout arrivato troppo tardi, non serve più
			// ignoriamolo
			break ;
		
		default:
			throw new InternalError("Stato paziente errato "+p.toString()) ;
		
		}
		
		
	}

	/**
	 * Patient goes out of triage. A severity code is assigned. If a studio is
	 * free, then it is immediately assigned. Otherwise, he is put in the waiting
	 * list.
	 * 
	 * @param e
	 */
	private void processTriageEvent(Event e) {

		Patient p = e.getPatient() ;
		
		// fine del triage
		
		// devo assegnare un codice (random)
		int rand = (int)(1+Math.random()*3) ;
		if(rand==1) p.setStatus(PatientStatus.WHITE);
		else if(rand==2) p.setStatus(PatientStatus.YELLOW);
		else if(rand==3) p.setStatus(PatientStatus.RED);
		
		// se c'è uno studio libero, lo mando in cura
		if(this.occupiedStudios<NS) {
			
			int duration = 0 ;
			if(p.getStatus()==PatientStatus.WHITE)
				duration = DURATION_WHITE ;
			else if(p.getStatus()==PatientStatus.YELLOW)
				duration = DURATION_YELLOW ;
			else if(p.getStatus()==PatientStatus.RED)
				duration = DURATION_RED ;
			
			this.occupiedStudios++ ;
			p.setStatus(PatientStatus.TREATING);
			
			queue.add(new Event(p, e.getTime()+duration, EventType.FREE_STUDIO)) ;
		} else {
			// se no, lo metto in lista d'attesa
			// e schedulo l'azione di time-out
			
			int timeout = 0 ;
			if(p.getStatus()==PatientStatus.WHITE)
				timeout = WHITE_TIMEOUT ;
			else if(p.getStatus()==PatientStatus.YELLOW)
				timeout = YELLOW_TIMEOUT;
			else if(p.getStatus()==PatientStatus.RED)
				timeout = RED_TIMEOUT;

			p.setQueueTime(e.getTime());
			waitingRoom.add(p) ;
			
			queue.add(new Event(p, e.getTime()+timeout, EventType.TIMEOUT)) ;
			
		}
	
	}

	public int getPatientsTreated() {
		return patientsTreated;
	}

	public int getPatientsDead() {
		return patientsDead;
	}

	public int getPatientsAbandoned() {
		return patientsAbandoned;
	}
}
