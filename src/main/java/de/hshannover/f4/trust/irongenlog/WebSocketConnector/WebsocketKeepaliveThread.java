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

import java.io.IOException;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * This class pings the Websocket server to keep the connection alive 
 * 
 * @author Marius Rohde
 * 
 */
public class WebsocketKeepaliveThread extends TimerTask {

	private static final Logger LOGGER = Logger.getLogger(WebsocketKeepaliveThread.class.getName());
	
	private ClientWebSocketHandler mSocket;

	/**
	 * Constructor for the Websocketkeepalive Thread 
	 * 
	 * @param socket The ClientWebSocketHandler to get the webSocketsession to ping the Server
	 * 
	 */
	public WebsocketKeepaliveThread(ClientWebSocketHandler socket) {
		this.mSocket = socket;
	}

	/**
	 * Thread method run to ping the websocketserver
 	 * 
	 */
	@Override
	public void run() {
		try {			
			
			if (mSocket.getWebSocketSession().isOpen()) {
				LOGGER.info("Keepalive sending ping");
				mSocket.getWebSocketSession().getRemote().sendPing(null);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
