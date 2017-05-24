package it.polito.tdp.emergency.model;

import java.util.Comparator;

/**
 * Partial-order comparator, that implements the rules of the waiting list. Is
 * assumes that both Patients are in one of the WAITING states.
 * 
 * @author Fulvio
 *
 */
public class PatientComparator implements Comparator<Patient> {
	
	//il comparatore ha senso se il loro stato è bianco, giallo, rosso 
	@Override
	public int compare(Patient o1, Patient o2) {
		Patient.PatientStatus s1 = o1.getStatus();
		Patient.PatientStatus s2 = o2.getStatus();

		if ((s1 != Patient.PatientStatus.WHITE) && (s1 != Patient.PatientStatus.YELLOW)
				&& (s1 != Patient.PatientStatus.RED))
			throw new IllegalArgumentException(
					String.format("Patient %s should be in waiting state, instead of %s", o1.getName(), s1));
		
		if ((s2 != Patient.PatientStatus.WHITE) && (s2 != Patient.PatientStatus.YELLOW)
				&& (s2 != Patient.PatientStatus.RED))
			throw new IllegalArgumentException(
					String.format("Patient %s should be in waiting state, instead of %s", o2.getName(), s2));

		if( s1 == s2 ) {
			// codici uguali ==> mi interessa chi arriva prima
			return o1.getQueueTime()-o2.getQueueTime() ;
		} else if(s1==Patient.PatientStatus.RED) {
			//se il primo è rosso allora deve passare lui
			return -1 ;
		} else if(s2==Patient.PatientStatus.RED) {
			//deve passare  s2
			return +1 ;
		} else if(s1==Patient.PatientStatus.YELLOW) {
			return -1 ;
		} else if(s2==Patient.PatientStatus.YELLOW) {
			return +1 ;
		} else {
			throw new InternalError("Something wrong "+o1.toString()+o2.toString()) ;
		}
	}

}
