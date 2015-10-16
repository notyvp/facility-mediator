package com.apelon.mediator;

import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import org.apache.http.HttpStatus;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;

public class DefaultOrchestrator extends UntypedActor {
	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private final MediatorConfig config;
	
	private MediatorHTTPRequest originalRequest;

	public DefaultOrchestrator(MediatorConfig config) {
		this.config = config;
	}

	private void queryFacility(MediatorHTTPRequest request) {
		log.info("Querying Facility");
		
		originalRequest = request;
		
		log.debug("Request Path: " + request.getPath());
		
		ActorSelection httpConnector = getContext().actorSelection(config.userPathFor("http-connector"));
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");
		
		MediatorHTTPRequest serviceRequest = new MediatorHTTPRequest(
				request.getRequestHandler(),
				getSelf(),
				"Facility Record Service",
				"GET",
				"http",
				"localhost",
				3444,
				request.getPath(),
				null,
				headers,
				null
			);
		httpConnector.tell(serviceRequest, getSelf());
	}
	
	private void processFacilityResponse(MediatorHTTPResponse response) {
		log.info("Received response from Facility mediator service");
		originalRequest.getRespondTo().tell(response.toFinishRequest(), getSelf());
	}
	
	@Override
	public void onReceive(Object msg) throws Exception {
		log.info("Accessing Default Orchestrator");
//
//		System.out.println("Here");	
//	
//		if (msg instanceof MediatorHTTPRequest) {
//			queryFacility((MediatorHTTPRequest) msg);
//		} else if (msg instanceof MediatorHTTPResponse) {
//			processFacilityResponse((MediatorHTTPResponse) msg);
//		} else {
//			unhandled(msg);
//		}
		
		//YEOMAN GENERATED METHOD
		if (msg instanceof MediatorHTTPRequest) {
			FinishRequest finishRequest = new FinishRequest(
					"A message from my new mediator!", "text/plain",
					HttpStatus.SC_OK);
			((MediatorHTTPRequest) msg).getRequestHandler().tell(finishRequest,
					getSelf());
		} else {
			unhandled(msg);
		}
	}
}
