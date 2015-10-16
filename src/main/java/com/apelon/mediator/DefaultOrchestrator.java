package com.apelon.mediator;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.messages.FinishRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPRequest;
import org.openhim.mediator.engine.messages.MediatorHTTPResponse;

import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
	
	private void processResponse(MediatorHTTPResponse response) {
		log.info("Received response from Facility mediator service");
		originalRequest.getRespondTo().tell(response.toFinishRequest(), getSelf());
	}
	
	@Override
	public void onReceive(Object msg) throws Exception {
		log.info("Accessing Default Orchestrator");

		System.out.println("Here");	
	
		if (msg instanceof MediatorHTTPRequest) {
			queryFacility((MediatorHTTPRequest) msg);
		} else if (msg instanceof MediatorHTTPResponse) {
			if(((MediatorHTTPResponse) msg).getStatusCode() == HttpStatus.SC_OK) {
				//TODO Finish this
				//Gson gson = new GsonBuilder().create();
				//FacilityRecord facilityRecord = gson.fromJson(((MediatorHTTPResponse) msg).getBody(), FacilityRecord.class);
				
				StringBuilder html = new StringBuilder("<html><body><h1>Facilities</h1></body></html>");
				
				FinishRequest fr = new FinishRequest(html.toString(), "text/html", HttpStatus.SC_OK);
				
				originalRequest.getRespondTo().tell(fr, getSelf());
			}
		} else {
			unhandled(msg);
		}
		
//		//YEOMAN GENERATED METHOD
//		if (msg instanceof MediatorHTTPRequest) {
//			FinishRequest finishRequest = new FinishRequest(
//					"A message from my new mediator!", "text/plain",
//					HttpStatus.SC_OK);
//			((MediatorHTTPRequest) msg).getRequestHandler().tell(finishRequest,
//					getSelf());
//		} else {
//			unhandled(msg);
//		}
	}
}
