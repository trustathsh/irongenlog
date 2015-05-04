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
 * Copyright (C) 2014 - 2015 Trust@HsH
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

import de.hshannover.f4.trust.ifmapj.channel.*;
import de.hshannover.f4.trust.ifmapj.exception.*;
import de.hshannover.f4.trust.ifmapj.identifier.*;
import de.hshannover.f4.trust.ifmapj.messages.*;
import de.hshannover.f4.trust.ifmapj.metadata.*;
import de.hshannover.f4.trust.irongenlog.publisher.PublishLogDataStrategy;

@SuppressWarnings("unused")
public class DhcpStrategy extends PublishLogDataStrategy {

    private static final Logger LOGGER = Logger.getLogger(DhcpStrategy.class.getName());

    @Override
    public void publishLogData(SSRC ssrc, JsonNode rootNode) {
        if(rootNode.path( "strategy" ).getTextValue().equals("dnsmasq-dhcp")){
        if(rootNode.path( "METHOD" ).getTextValue().equals("DHCPDISCOVER")){
        publishDhcpDiscover(ssrc, rootNode);
        }
        else if(rootNode.path( "METHOD" ).getTextValue().equals("DHCPREQUEST")){
        publishDhcpRequest(ssrc, rootNode);
        }
        else if(rootNode.path( "METHOD" ).getTextValue().equals("DHCPACK")){
        publishDhcpAck(ssrc, rootNode);
        }
        }
    }

    public void publishDhcpDiscover(SSRC ssrc, JsonNode rootNode){
        {
        try {
            Identifier ident1 = Identifiers.createMac(rootNode.path( "MAC" ).getTextValue());
            Identifier ident2 = Identifiers.createDev(rootNode.path( "DHCPSERVERNAME" ).getTextValue());
            Document docMeta = getMetadataFactory().createDiscoveredBy();
            PublishUpdate publishUpdate = Requests.createPublishUpdate(ident1, ident2, docMeta, MetadataLifetime.session);
            ssrc.publish(Requests.createPublishReq(publishUpdate));
        } catch (IfmapErrorResult e) {
            LOGGER.severe("Error publishing update data: " + e);
        } catch (IfmapException e) {
            LOGGER.severe("Error publishing update data: " + e);
        }
        }
    }

    public void publishDhcpRequest(SSRC ssrc, JsonNode rootNode){
        {
        try {
            Identifier ident1 = Identifiers.createMac(rootNode.path( "MAC" ).getTextValue());
            Identifier ident2 = Identifiers.createAr(rootNode.path( "IP" ).getTextValue());
            Document docMeta = getMetadataFactory().createArMac();
            PublishUpdate publishUpdate = Requests.createPublishUpdate(ident1, ident2, docMeta, MetadataLifetime.session);
            ssrc.publish(Requests.createPublishReq(publishUpdate));
        } catch (IfmapErrorResult e) {
            LOGGER.severe("Error publishing update data: " + e);
        } catch (IfmapException e) {
            LOGGER.severe("Error publishing update data: " + e);
        }
        }
        {
        try {
            Identifier ident1 = Identifiers.createMac(rootNode.path( "MAC" ).getTextValue());
            Identifier ident2 = Identifiers.createIp4(rootNode.path( "IP" ).getTextValue());
            String metaDeleteString = "meta:ip-mac[@ifmap-publisher-id='" + ssrc.getPublisherId() + "']";
            PublishDelete publishDelete = Requests.createPublishDelete(ident1, ident2, metaDeleteString);
            ssrc.publish(Requests.createPublishReq(publishDelete));
        } catch (IfmapErrorResult e) {
            LOGGER.severe("Error publishing update data: " + e);
        } catch (IfmapException e) {
            LOGGER.severe("Error publishing update data: " + e);
        }
        }
        {
        try {
            Identifier ident1 = Identifiers.createMac(rootNode.path( "MAC" ).getTextValue());
            Identifier ident2 = Identifiers.createIp4(rootNode.path( "IP" ).getTextValue());
            Document docMeta = getMetadataFactory().createIpMac(null, null, rootNode.path("DHCPSERVERNAME").getTextValue());
            PublishUpdate publishUpdate = Requests.createPublishUpdate(ident1, ident2, docMeta, MetadataLifetime.session);
            ssrc.publish(Requests.createPublishReq(publishUpdate));
        } catch (IfmapErrorResult e) {
            LOGGER.severe("Error publishing update data: " + e);
        } catch (IfmapException e) {
            LOGGER.severe("Error publishing update data: " + e);
        }
        }
        {
        try {
            Identifier ident1 = Identifiers.createIp4(rootNode.path( "IP" ).getTextValue());
            Identifier ident2 = Identifiers.createAr(rootNode.path( "IP" ).getTextValue());
            Document docMeta = getMetadataFactory().createArIp();
            PublishUpdate publishUpdate = Requests.createPublishUpdate(ident1, ident2, docMeta, MetadataLifetime.session);
            ssrc.publish(Requests.createPublishReq(publishUpdate));
        } catch (IfmapErrorResult e) {
            LOGGER.severe("Error publishing update data: " + e);
        } catch (IfmapException e) {
            LOGGER.severe("Error publishing update data: " + e);
        }
        }
    }

    public void publishDhcpAck(SSRC ssrc, JsonNode rootNode){
        {
        try {
            Identifier ident1 = Identifiers.createAr(rootNode.path( "IP" ).getTextValue());
            Identifier ident2 = Identifiers.createDev(rootNode.path( "DHCPSERVERNAME" ).getTextValue());
            Document docMeta = getMetadataFactory().createAuthBy();
            PublishUpdate publishUpdate = Requests.createPublishUpdate(ident1, ident2, docMeta, MetadataLifetime.session);
            ssrc.publish(Requests.createPublishReq(publishUpdate));
        } catch (IfmapErrorResult e) {
            LOGGER.severe("Error publishing update data: " + e);
        } catch (IfmapException e) {
            LOGGER.severe("Error publishing update data: " + e);
        }
        }
    }

}