package de.hshannover.f4.trust.irongenlog.publisher.strategies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.codehaus.jackson.JsonNode;
import org.w3c.dom.Document;

import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.exception.IfmapErrorResult;
import de.hshannover.f4.trust.ifmapj.exception.IfmapException;
import de.hshannover.f4.trust.ifmapj.exception.MarshalException;
import de.hshannover.f4.trust.ifmapj.identifier.Identifier;
import de.hshannover.f4.trust.ifmapj.identifier.Identifiers;
import de.hshannover.f4.trust.ifmapj.identifier.IdentityType;
import de.hshannover.f4.trust.ifmapj.messages.MetadataLifetime;
import de.hshannover.f4.trust.ifmapj.messages.PublishElement;
import de.hshannover.f4.trust.ifmapj.messages.PublishRequest;
import de.hshannover.f4.trust.ifmapj.messages.PublishUpdate;
import de.hshannover.f4.trust.ifmapj.messages.Requests;
import de.hshannover.f4.trust.ifmapj.metadata.VendorSpecificMetadataFactory;
import de.hshannover.f4.trust.ifmapj.metadata.VendorSpecificMetadataFactoryImpl;
import de.hshannover.f4.trust.irongenlog.publisher.PublishLogDataStrategy;

public class FreeRadiusStrategy extends PublishLogDataStrategy {

	private static final Logger LOGGER = Logger.getLogger(FreeRadiusStrategy.class.getName());

	private Map<Integer, Map<String, String>> mInfoPerRequest = new HashMap<Integer, Map<String, String>>();

	private static final String SIMU_METADATA_PREFIX = "simu";
	private static final String SIMU_METADATA_URI = "http://simu-project.de/XMLSchema/1";

	private static final String SIMU_IDENTIFIER_PREFIX = "simu";
	private static final String SIMU_IDENTIFIER_URI = "http://simu-project.de/XMLSchema/1";

	private static final VendorSpecificMetadataFactory VENDOR_FACTORY = new VendorSpecificMetadataFactoryImpl();

	private static final String FREERADIUS_SERVICE_NAME = "radius";
	private static final String FREERADIUS_SERVICE_TYPE = "pdp";
	private static final String FREERADIUS_SERVICE_ADMINISTRATIVE_DOMAIN = "";
	private static final String FREERADIUS_DEVICE = "freeradius-pdp";

	private static final String FREERADIUS_IMPLEMENTATION_NAME = "FreeRADIUS";
	private static final String FREERADIUS_IMPLEMENTATION_VERSION = "3";
	private static final String FREERADIUS_IMPLEMENTATION_PATCH = "0";
	private static final String FREERADIUS_IMPLEMENTATION_PLATFORM = "11";
	private static final String FREERADIUS_IMPLEMENTATION_ADMINISTRATIVE_DOMAIN = "";

	private static final String FREERADIUS_IP = "192.168.1.200";

	private boolean mInitialized;

	private Identifier mFreeradiusDevice;
	private Identifier mFreeradiusService;

	@Override
	public void publishLogData(SSRC ssrc, JsonNode rootNode) {
		if (rootNode.has("strategy")) {
			if (rootNode.path("strategy").getTextValue().equals("freeradius")) {
				int requestId = 0;

				Map<String, String> info = null;

				if (rootNode.has("request_id")) {
					requestId = Integer.valueOf(rootNode.path("request_id").getTextValue());

					info = mInfoPerRequest.get(requestId);

					if (info == null) {
						info = new HashMap<String, String>();
						mInfoPerRequest.put(requestId, info);
					}

					if (rootNode.path("fr_logclass").getTextValue().equals("Auth")) {

						if (rootNode.path("fr_message").getTextValue().contains("Login OK")) {
							info.put("username", rootNode.path("username").getTextValue());
							info.put("radius_client", rootNode.path("radius_client").getTextValue());
							info.put("radius_port", rootNode.path("radius_port").getTextValue());
							info.put("login", "successful");
							LOGGER.info("Successful login");
						}

						if (rootNode.path("fr_message").getTextValue().contains("incorrect")) {
							info.put("username", rootNode.path("username").getTextValue());
							info.put("radius_reason", rootNode.path("radius_reason").getTextValue());
							info.put("radius_client", rootNode.path("radius_client").getTextValue());
							info.put("radius_port", rootNode.path("radius_port").getTextValue());
							info.put("login", "unsuccessful");
							LOGGER.info("Unsuccessful login");
						}
					} else if (rootNode.path("fr_logclass").getTextValue().equals("Debug")) {
						if (rootNode.path("fr_message").getTextValue().contains("Role")) {
							info.put("radius_role", rootNode.path("radius_role").getTextValue());
						}

						if (rootNode.path("fr_message").getTextValue().contains("Sent Access-Accept")) {
							// info.put("radius_client", rootNode.path("radius_client").getTextValue());
							// info.put("radius_port", rootNode.path("radius_port").getTextValue());
							info.put("radius_ar_from", rootNode.path("radius_ar_from").getTextValue());
							info.put("radius_ar_port", rootNode.path("radius_ar_port").getTextValue());
						}

						if (rootNode.path("fr_message").getTextValue().contains("Sent Access-Reject")) {
							// info.put("radius_client", rootNode.path("radius_client").getTextValue());
							// info.put("radius_port", rootNode.path("radius_port").getTextValue());
							info.put("radius_ar_from", rootNode.path("radius_ar_from").getTextValue());
							info.put("radius_ar_port", rootNode.path("radius_ar_port").getTextValue());
						}

						if (rootNode.path("fr_message").getTextValue().contains("Cleaning up request")) {
							if (!mInitialized) {
								selfPublish(ssrc, info);
								mInitialized = true;
							}

							LOGGER.info("Request finished, try to send information.");
							publishInformation(ssrc, requestId, info);
						}
					}
				}
			}
		}
	}

	private void selfPublish(SSRC ssrc, Map<String, String> info) {
		try {
			mFreeradiusDevice = Identifiers.createDev(FREERADIUS_DEVICE);
			mFreeradiusService = createSimuService(info.get("radius_port"));

			List<PublishElement> publishElements = new ArrayList<PublishElement>();

			// Identifier ip = Identifiers.createIp4(info.get("radius_client"));
			Identifier ip = Identifiers.createIp4(FREERADIUS_IP);
			publishElements.add(createDeviceIpPubElement(mFreeradiusDevice, ip));

			Identifier implementation = createImplementation();

			publishElements.add(createServiceIpPubElement(mFreeradiusService, ip));
			publishElements.add(createServiceImplementationPubElement(mFreeradiusService, implementation));

			ssrc.publish(Requests.createPublishReq(publishElements));
		} catch (MarshalException e) {
			LOGGER.warning(e.getMessage());
		} catch (IfmapErrorResult e) {
			LOGGER.warning(e.getMessage());
		} catch (IfmapException e) {
			LOGGER.warning(e.getMessage());
		}

	}

	private Identifier createImplementation()
			throws MarshalException {
		StringBuilder implementationDocument = new StringBuilder();
		implementationDocument.append("<"
				+ SIMU_IDENTIFIER_PREFIX + ":implementation ");
		implementationDocument.append("administrative-domain=\""
				+ FREERADIUS_IMPLEMENTATION_ADMINISTRATIVE_DOMAIN + "\" ");
		implementationDocument.append("xmlns:"
				+ SIMU_IDENTIFIER_PREFIX + "=\"" + SIMU_IDENTIFIER_URI + "\" ");
		implementationDocument.append("name=\""
				+ FREERADIUS_IMPLEMENTATION_NAME + "\" ");
		implementationDocument.append("version=\""
				+ FREERADIUS_IMPLEMENTATION_VERSION + "\" ");
		implementationDocument.append("local-version=\""
				+ FREERADIUS_IMPLEMENTATION_PATCH + "\" ");
		implementationDocument.append("platform=\""
				+ FREERADIUS_IMPLEMENTATION_PLATFORM + "\" ");
		implementationDocument.append(">");
		implementationDocument.append("</"
				+ SIMU_IDENTIFIER_PREFIX + ":implementation>");

		return Identifiers.createExtendedIdentity(implementationDocument.toString());
	}

	private PublishElement createServiceImplementationPubElement(Identifier service, Identifier implementation) {
		PublishUpdate result = Requests.createPublishUpdate();
		String xmlString = "<"
				+ SIMU_METADATA_PREFIX + ":service-implementation "
				+ "ifmap-cardinality=\"singleValue\" "
				+ "xmlns:" + SIMU_METADATA_PREFIX + "=\"" + SIMU_METADATA_URI + "\">"
				+ "</" + SIMU_METADATA_PREFIX + ":service-implementation>";
		Document link = VENDOR_FACTORY.createMetadata(xmlString);

		result.setIdentifier1(service);
		result.setIdentifier2(implementation);
		result.addMetadata(link);
		result.setLifeTime(MetadataLifetime.session);

		return result;
	}

	private PublishElement createServiceIpPubElement(Identifier service, Identifier ip) {
		PublishUpdate result = Requests.createPublishUpdate();
		String xmlString = "<"
				+ SIMU_METADATA_PREFIX + ":service-ip "
				+ "ifmap-cardinality=\"singleValue\" "
				+ "xmlns:" + SIMU_METADATA_PREFIX + "=\"" + SIMU_METADATA_URI + "\">"
				+ "</" + SIMU_METADATA_PREFIX + ":service-ip>";
		Document link = VENDOR_FACTORY.createMetadata(xmlString);

		result.setIdentifier1(service);
		result.setIdentifier2(ip);
		result.addMetadata(link);
		result.setLifeTime(MetadataLifetime.session);

		return result;
	}

	private PublishElement createDeviceIpPubElement(Identifier device, Identifier ip) {
		PublishUpdate result = Requests.createPublishUpdate();
		Document link = getMetadataFactory().createDevIp();

		result.setIdentifier1(device);
		result.setIdentifier2(ip);
		result.addMetadata(link);
		result.setLifeTime(MetadataLifetime.session);

		return result;
	}

	private void publishInformation(SSRC ssrc, int requestId, Map<String, String> info) {
		LOGGER.info("requestId: "
				+ requestId + ", info: " + info.toString());
		Identifier accessRequest = Identifiers.createAr("ar:"
				+ requestId);
		Identifier username = Identifiers.createIdentity(IdentityType.userName, info.get("username").split("/")[0]);
		Identifier ipAddress = Identifiers.createIp4(info.get("radius_ar_from"));
		Identifier clientDevice = Identifiers.createDev(info.get("radius_client"));

		Document arIp = getMetadataFactory().createArIp();
		Document authBy = getMetadataFactory().createAuthBy();
		Document arDev = getMetadataFactory().createArDev();

		PublishElement p2 = Requests.createPublishUpdate(accessRequest, ipAddress, arIp, MetadataLifetime.session);
		PublishElement p4 =
				Requests.createPublishUpdate(accessRequest, mFreeradiusDevice, authBy, MetadataLifetime.session);
		PublishElement p6 =
				Requests.createPublishUpdate(accessRequest, clientDevice, arDev, MetadataLifetime.session);

		PublishRequest publishRequest = Requests.createPublishReq();
		if (info.get("login").equals("successful")) {
			Document role = getMetadataFactory().createRole(info.get("radius_role").split("@")[0]);
			Document authAs = getMetadataFactory().createAuthAs();
			Document loginSuccess = createSimuLoginSuccess();

			PublishElement p1 = Requests.createPublishUpdate(accessRequest, username, role, MetadataLifetime.session);
			PublishElement p3 = Requests.createPublishUpdate(accessRequest, username, authAs, MetadataLifetime.session);
			PublishElement p5 = Requests.createPublishUpdate(accessRequest, mFreeradiusService, loginSuccess,
					MetadataLifetime.session);

			publishRequest.addPublishElement(p1);
			publishRequest.addPublishElement(p3);
			publishRequest.addPublishElement(p5);
		} else {
			Document loginFailed = createSimuLoginFailure(info);

			PublishElement p7 = Requests.createPublishUpdate(accessRequest, mFreeradiusService, loginFailed,
					MetadataLifetime.session);

			publishRequest.addPublishElement(p7);
		}
		publishRequest.addPublishElement(p2);
		publishRequest.addPublishElement(p4);
		publishRequest.addPublishElement(p6);

		try {
			ssrc.publish(publishRequest);
			LOGGER.info("Information published");
		} catch (IfmapErrorResult e) {
			LOGGER.severe("Error publishing update data: "
					+ e);
		} catch (IfmapException e) {
			LOGGER.severe("Error publishing update data: "
					+ e);
		}
	}

	private Identifier createSimuService(String port) throws MarshalException {
		StringBuilder serviceDocument = new StringBuilder();
		serviceDocument.append("<"
				+ SIMU_IDENTIFIER_PREFIX + ":service ");
		serviceDocument.append("administrative-domain=\""
				+ FREERADIUS_SERVICE_ADMINISTRATIVE_DOMAIN + "\" ");
		serviceDocument.append("xmlns:"
				+ SIMU_IDENTIFIER_PREFIX + "=\"" + SIMU_IDENTIFIER_URI + "\" ");
		serviceDocument.append("type=\""
				+ FREERADIUS_SERVICE_TYPE + "\" ");
		serviceDocument.append("name=\""
				+ FREERADIUS_SERVICE_NAME + "\" ");
		serviceDocument.append("port=\""
				+ port + "\" ");
		serviceDocument.append(">");
		serviceDocument.append("</"
				+ SIMU_IDENTIFIER_PREFIX + ":service>");

		return Identifiers.createExtendedIdentity(serviceDocument.toString());
	}

	private Document createSimuLoginFailure(Map<String, String> info) {
		String xmlString = "<"
				+ SIMU_METADATA_PREFIX + ":login-failure "
				+ "ifmap-cardinality=\"singleValue\" "
				+ "xmlns:" + SIMU_METADATA_PREFIX + "=\"" + SIMU_METADATA_URI + "\" "
				+ "credential-type=\"password\" "
				+ "reason=\"wrong password\" >" // TODO extract from info.get("radius_reason")
				+ "</" + SIMU_METADATA_PREFIX + ":login-failure>";
		return VENDOR_FACTORY.createMetadata(xmlString);
	}

	private Document createSimuLoginSuccess() {
		String xmlString = "<"
				+ SIMU_METADATA_PREFIX + ":login-success "
				+ "ifmap-cardinality=\"singleValue\" "
				+ "xmlns:" + SIMU_METADATA_PREFIX + "=\"" + SIMU_METADATA_URI + "\" "
				+ "credential-type=\"password\" >"
				+ "</" + SIMU_METADATA_PREFIX + ":login-success>";
		return VENDOR_FACTORY.createMetadata(xmlString);
	}

}
