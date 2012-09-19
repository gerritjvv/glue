package org.glue.rest;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router
import org.glue.unit.exec.GlueExecutor;
import org.restlet.Context;
import org.glue.rest.resources.*;

/**
 *
 * Helper class to return a router instance
 *
 */
class Server extends Application {

	Router router
	
	public Server(Router router) {
		this.router = router
	}

	@Override
	public Restlet createInboundRoot() {
		return router;
	}
}
