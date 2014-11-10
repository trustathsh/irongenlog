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
package de.hshannover.f4.trust.irongenlog.publisher.strategies;

import java.util.logging.Logger;

import org.codehaus.jackson.JsonNode;
import org.w3c.dom.Document;

import de.hshannover.f4.trust.ifmapj.binding.IfmapStrings;
import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.identifier.AccessRequest;
import de.hshannover.f4.trust.ifmapj.identifier.Device;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.identifier.IpAddress;
import de.hshannover.f4.trust.ifmapj.identifier.MacAddress;
import de.hshannover.f4.trust.ifmapj.messages.MetadataLifetime;
import de.hshannover.f4.trust.ifmapj.messages.PublishDelete;
import de.hshannover.f4.trust.ifmapj.messages.PublishUpdate;
import de.hshannover.f4.trust.ifmapj.messages.Requests;
import de.hshannover.f4.trust.irongenlog.publisher.PublishLogDataStrategy;

/**
 * This class implements the publish strategy for dhcp events comming from
 * dnsmasq
 * 
 * @author Marius Rohde
 * 
 */

public class PublishDnsMasqDhcpEvents extends PublishLogDataStrategy {

	private static final Logger LOGGER = Logger.getLogger(PublishDnsMasqDhcpEvents.class.getName());

	/**
	 * Method checks for right publish strategy
	 * 
	 * @param ssrc
	 *            : the ssrc to publish data
	 * @param rootNode
	 *            : the json message root node
	 */

	@Override
	public void publishLogData(SSRC ssrc, JsonNode rootNode) {

		if (rootNode.path("strategy").getTextValue().equals("dnsmasq-dhcp")) {

			String method = rootNode.path("METHOD").getTextValue();

			if (method.equals("DHCPDISCOVER")) {
				publishDhcpDiscover(ssrc, rootNode);
			} else if (method.equals("DHCPOFFER")) {
				// nothing to do right now
			} else if (method.equals("DHCPREQUEST")) {
				publishDhcpRequest(ssrc, rootNode);
			} else if (method.equals("DHCPACK")) {
				publishDhcpAck(ssrc, rootNode);
			}
		}

	}

	/**
	 * Method to publish the dhcp logdata performed by DHCP Discover message
	 * 
	 * @param ssrc
	 *            : the ssrc to publish data
	 * @param rootNode
	 *            : the json message root node
	 */

	private void publishDhcpDiscover(SSRC ssrc, JsonNode rootNode) {

		try {

			MacAddress macHost = Identifiers.createMac(rootNode.path("MAC").getTextValue());
			Device dhcpserver = Identifiers.createDev(rootNode.path("DHCPSERVERNAME").getTextValue());

			Document docDiscoMac = getMetadataFactory().createDiscoveredBy();

			PublishUpdate publishDiscoMac = Requests.createPublishUpdate(macHost, dhcpserver, docDiscoMac,
					MetadataLifetime.session);
			ssrc.publish(Requests.createPublishReq(publishDiscoMac));

		} catch (IfmapErrorResult e) {
			LOGGER.severe("Error publishing dhcp data: " + e);
		} catch (IfmapException e) {
			LOGGER.severe("Error publishing dhcp data: " + e);
		}

	}

	/**
	 * Method to publish the dhcp logdata performed by DHCP Request message
	 * 
	 * @param ssrc
	 *            : the ssrc to publish data
	 * @param rootNode
	 *            : the json message root node
	 */

	private void publishDhcpRequest(SSRC ssrc, JsonNode rootNode) {

		try {
			
			// accessrequest to mac
			MacAddress macHost = Identifiers.createMac(rootNode.path("MAC").getTextValue());
			AccessRequest ar = Identifiers.createAr("DHCPREQUEST_" + rootNode.path("IP").getTextValue());
			Document docArMac = getMetadataFactory().createArMac();

			PublishUpdate publishArMac = Requests.createPublishUpdate(macHost, ar, docArMac, MetadataLifetime.session);
			ssrc.publish(Requests.createPublishReq(publishArMac));

			// del ip-mac
			IpAddress ipHost = Identifiers.createIp4(rootNode.path("IP").getTextValue());			
			PublishDelete del = Requests.createPublishDelete(ipHost, macHost,
					"meta:ip-mac[@ifmap-publisher-id='" + ssrc.getPublisherId() + "']");
			del.addNamespaceDeclaration(IfmapStrings.STD_METADATA_PREFIX, IfmapStrings.STD_METADATA_NS_URI);
			ssrc.publish(Requests.createPublishReq(del));	
			
			
			// ip to mac
			Document docIpMac = getMetadataFactory().createIpMac(null, null,
					rootNode.path("DHCPSERVERNAME").getTextValue());		
			
			PublishUpdate publishIpMac = Requests.createPublishUpdate(macHost, ipHost, docIpMac,
					MetadataLifetime.session);
			ssrc.publish(Requests.createPublishReq(publishIpMac));

			// accessrequest to ip
			Document docIpAr = getMetadataFactory().createArIp();

			PublishUpdate publishIpAr = Requests.createPublishUpdate(ar, ipHost, docIpAr, MetadataLifetime.session);
			ssrc.publish(Requests.createPublishReq(publishIpAr));

		} catch (IfmapErrorResult e) {
			LOGGER.severe("Error publishing dhcp data: " + e);
		} catch (IfmapException e) {
			LOGGER.severe("Error publishing dhcp data: " + e);
		}

	}

	/**
	 * Method to publish the dhcp logdata performed by DHCP Request message
	 * 
	 * @param ssrc
	 *            : the ssrc to publish data
	 * @param rootNode
	 *            : the json message root node
	 */

	private void publishDhcpAck(SSRC ssrc, JsonNode rootNode) {

		try {

			AccessRequest ar = Identifiers.createAr("DHCPREQUEST_" + rootNode.path("IP").getTextValue());
			Device dhcpserver = Identifiers.createDev(rootNode.path("DHCPSERVERNAME").getTextValue());

			Document docAuthAr = getMetadataFactory().createAuthBy();

			PublishUpdate publishDiscoMac = Requests.createPublishUpdate(ar, dhcpserver, docAuthAr,
					MetadataLifetime.session);
			ssrc.publish(Requests.createPublishReq(publishDiscoMac));

		} catch (IfmapErrorResult e) {
			LOGGER.severe("Error publishing dhcp data: " + e);
		} catch (IfmapException e) {
			LOGGER.severe("Error publishing dhcp data: " + e);
		}

	}

}
