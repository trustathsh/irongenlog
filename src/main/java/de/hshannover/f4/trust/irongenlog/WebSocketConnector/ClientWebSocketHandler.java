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

package de.hshannover.f4.trust.irongenlog.WebSocketConnector;

import java.util.Timer;
import java.util.logging.Logger;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import de.hshannover.f4.trust.irongenlog.publisher.PublishLogDataStrategy;
import de.hshannover.f4.trust.irongenlog.publisher.StrategyChainBuilder;
import de.hshannover.f4.trust.irongenlog.utilities.IfMap;

/**
 * Web Client Socket
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class ClientWebSocketHandler {

	private static final Logger LOGGER = Logger.getLogger(ClientWebSocketHandler.class.getName());
	
	private Session mSession;
	private Timer websocketKeepAliveTimer;

	/**
	 * This method returns the current WebsocketSession
	 * 
	 * @return The Websocket session
	 * 
	 */
	public Session getWebSocketSession() {
		return this.mSession;
	}

	/**
	 * This method will be called if the session will be closed It also stops
	 * the keepalive thread for this session
	 * 
	 * @param statusCode
	 *            A code to identify the reason of closing the connection
	 * @param reason
	 *            A message to identify the reason of closing the connection
	 */
	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		LOGGER.info("Connection closed: "+statusCode+" - "+reason);
		websocketKeepAliveTimer.cancel();
		this.mSession = null;
	}

	/**
	 * This method will be called if the session will be started It starts the
	 * Keepalive thread for the socket communication
	 * 
	 * @param session
	 *            The session Object of the communication
	 */
	@OnWebSocketConnect
	public void onConnect(Session session) {
		LOGGER.info("Got connect: "+ session);
		this.mSession = session;

		Timer websocketKeepAliveTimer = new Timer();
		websocketKeepAliveTimer.schedule(new WebsocketKeepaliveThread(this), 60000, 60000);

	}

	/**
	 * This method will be called if messages will received from server. It also
	 * delivers the message to the messagehandler to post the right Ifmap
	 * Metadata to the Ifmap server
	 * 
	 * @param msg
	 *            The Message delivered by the websocket server
	 */
	@OnWebSocketMessage
	public void onMessage(String msg) {
		LOGGER.info("Got msg: "+ msg);
		
		PublishLogDataStrategy strategyObj;

		for (int i = 0; i < StrategyChainBuilder.getSize(); i++) {
			strategyObj = StrategyChainBuilder.getElementAt(i);
			strategyObj.publishLogData(IfMap.getSsrc(), msg);
		}
	}
	
}