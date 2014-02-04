package edu.umn.ao.ao_batch_analyst;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.core.TraverseModeSet;
import org.opentripplanner.routing.services.GraphService;

import lombok.Getter;
import lombok.Setter;

public class IndividualRoutingRequestGenerator {
	
	private final GraphService graphSerivce;
	private final Population origins;
	private final long [] depTimes;
	private final TraverseModeSet [] modeSets;
	private final IndividualRoutingRequest protoReq;
	
	private Queue<List<Integer>> queue;
	
	public IndividualRoutingRequestGenerator(GraphService graphService, IndividualRoutingRequest protoReq, Population origins, long [] depTimes, TraverseModeSet [] modeSets) {
		this.graphSerivce = graphService;
		this.protoReq = protoReq;
		this.origins = origins;
		
		if (depTimes != null) {
			this.depTimes = depTimes;
			
		} else {
			this.depTimes = new long [] {protoReq.dateTime};
		}
		
		if (modeSets != null) {
			this.modeSets = modeSets;
		} else {
			this.modeSets = new TraverseModeSet [] {protoReq.modes};
		}
		
		this.queue = createQueue();
		
	}
	
	public IndividualRoutingRequest next() {
		IndividualRoutingRequest nextReq = protoReq.clone();
		
		return nextReq;
	}
	
	public void remove () {
		
	}
	
	private void createQueue() {
		Queue<List<Integer>> queue = new ConcurrentLinkedQueue<List<Integer>>();
		for (int m=0; m < modeSets.length; m++) {
			for (int i=0; i < origins.size(); i++) {
				for (int t=0; t < depTimes.length; t++) {
					List<Integer> l = new ArrayList<Integer>(3);
					l.set(0, m);
					l.set(1, i);
					l.set(2, t);
					queue.add(l);
				}
			}
		}
	}
	
}
