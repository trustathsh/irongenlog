/*
 * #%L
 * =====================================================
 *   _____                _     ____  _   _       _   _
 *  |_   _|_ __ _   _ ___| |_  / __ \| | | | ___ | | | |
 *    | | | '__| | | / __| __|/ / _` | |_| |/ __|| |_| |
 *    | | | |  | |_| \__ \ |_| | (_| |  _  |\__ \|  _  |
 *    |_| |_|   \__,_|___/\__|\ \__,_|_| |_||___/|_| |_|
 *                             \____/
 * 
 * =====================================================
 * 
 * Hochschule Hannover
 * (University of Applied Sciences and Arts, Hannover)
 * Faculty IV, Dept. of Computer Science
 * Ricklinger Stadtweg 118, 30459 Hannover, Germany
 * 
 * Email: trust@f4-i.fh-hannover.de
 * Website: http://trust.f4.hs-hannover.de
 * 
 * This file is part of irongenlog, version 0.0.1, implemented by the Trust@HsH
 * research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2013 - 2014 Trust@HsH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package de.hshannover.f4.trust.irongenlog.websocketconnector;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

/**
 * This class starts the websocket to hear for websocket messages
 * 
 * @author Marius Rohde
 * 
 */

public class WebSocketConnector {

	private static final Logger LOGGER = Logger.getLogger(WebSocketConnector.class.getName());

	/**
	 * The WebSocketclient instance
	 */
	private WebSocketClient mClient;

	/**
	 * The Uri of the websocket server
	 */
	private URI mDestUri;

	/**
	 * The Constructor do create a new Websocketconnector
	 * 
	 * @param destUri
	 *            The uri to the Websocket server
	 * 
	 */

	public WebSocketConnector(String destUri) throws URISyntaxException {
		mDestUri = new URI(destUri);
	}

	/**
	 * Method to activate the websocket to hear for new events
	 */
	public void setActive() throws IOException, Exception {

		mClient = new WebSocketClient();
		ClientWebSocketHandler socket = new ClientWebSocketHandler();

		try {
			mClient.start();

			try {
				ClientUpgradeRequest request = new ClientUpgradeRequest();
				LOGGER.info("Connecting to : " + mDestUri);
				Future<Session> f = mClient.connect(socket, mDestUri, request);
				f.get(15, TimeUnit.SECONDS);
			} catch (IOException t) {
				LOGGER.severe("Can't connect to the Socket! Try to stop the WebSocketClient!");
				try {
					mClient.stop();
					mClient = null;
				} catch (Exception e) {
					LOGGER.severe("Can't stop the WebSocketClient!");
				}
				throw t;
			}

		} catch (Exception e) {
			LOGGER.severe("Can't connect to the WebSocketServer!");
			mClient.stop();
			mClient = null;
			throw e;
		}

	}

	/**
	 * Method to deactivate the websocket
	 */

	public void setInactive() {

		try {
			mClient.stop();
		} catch (Exception e) {
			LOGGER.severe("Can't stop the WebSocketClient! " + e);
		} finally {
			mClient = null;
		}

	}

}
