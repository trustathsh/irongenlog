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

package de.hshannover.f4.trust.irongenlog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

/**
 * This class loads the configuration file from the file system and provides a
 * set of constants and a getter method to access these values.
 * 
 * @author Marius Rohde
 * 
 */

public final class Configuration {

	private static final Logger LOGGER = Logger.getLogger(Configuration.class.getName());

	/**
	 * The path to the configuration file.
	 */

	private static final String CONFIG_FILE = "/irongenlog.properties";

	private static Properties mProperties;

	private static Properties mClassnamesForRequestStrategy;

	// begin configuration parameter -------------------------------------------

	private static final String IFMAP_AUTH_METHOD = "ifmap.server.auth.method";
	private static final String IFMAP_URL_BASIC = "ifmap.server.url.basic";
	private static final String IFMAP_URL_CERT = "ifmap.server.url.cert";
	private static final String IFMAP_BASIC_USER = "ifmap.server.auth.basic.user";
	private static final String IFMAP_BASIC_PASSWORD = "ifmap.server.auth.basic.password";

	private static final String KEYSTORE_PATH = "keystore.path";
	private static final String KEYSTORE_PASSWORD = "keystore.password";
	
	private static final String IFMAP_KEEPALIVE = "ifmap.client.keepalive";

	private static final String WEBSOCKET_SERVER_URL = "websocket.server.url";

	// publisher
	private static final String REQUEST_STRATEGIES_CLASSNAMES_FILENAME = "irongenlog.publisher.requeststrategies";

	// end configuration parameter ---------------------------------------------

	/**
	 * Death constructor for code convention -> final class because utility
	 * class
	 */
	private Configuration() {
	}

	/**
	 * Loads the configuration file. Every time this method is called the file
	 * is read again.
	 * 
	 * @throws IOException
	 *             To signalise a failure while reading to calling classes
	 * @throws FileNotFoundException
	 *             To signalise a failure while opening to calling classes
	 */
	public static void init() throws IOException, FileNotFoundException  {
		LOGGER.info("reading " + CONFIG_FILE + " ...");

		mProperties = new Properties();
		mClassnamesForRequestStrategy = new Properties();

		InputStream in = Configuration.class.getResourceAsStream(CONFIG_FILE);
		loadPropertiesfromFile(in, mProperties);

		in = Configuration.class.getResourceAsStream("/" + irongenlogRequestStrategiesClassnamePropertiesFilename());
		loadPropertiesfromFile(in, mClassnamesForRequestStrategy);

	}

	/**
	 * Loads the configuration file. Every time this method is called the file
	 * is read again.
	 * 
	 * @param in
	 *            Streamreader
	 * @param props
	 *            properties
	 * @throws IOException
	 *             To signalise a failure while reading to calling classes
	 */
	private static void loadPropertiesfromFile(InputStream in, Properties props) throws IOException, FileNotFoundException {

		try {
			props.load(in);
		} catch (FileNotFoundException e) {
			LOGGER.severe("could not find " + CONFIG_FILE);
			throw e;
		} catch (IOException e) {
			LOGGER.severe("error while reading " + CONFIG_FILE);
			throw e;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				LOGGER.warning("error while closing properties inputstream: " + e);
			}

		}
	}

	/**
	 * Returns the value assigned to the given key. If the configuration has not
	 * been loaded jet this method loads it.
	 * 
	 * @param key
	 * @return the value assigned to key or null if it is none
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	private static String get(String key) {
		return mProperties.getProperty(key);
	}

	/**
	 * Getter for the request Strategies classname map.
	 * 
	 * @return the set of classnames for request strategies
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static Set<Entry<Object, Object>> getRequestStrategiesClassnameMap() {
		return mClassnamesForRequestStrategy.entrySet();
	}

	/**
	 * Getter for the ifmapAuthMethod property.
	 * 
	 * @return property string
	 */
	public static String ifmapAuthMethod() {
		return get(IFMAP_AUTH_METHOD);
	}

	/**
	 * Getter for the ifmapUrlBasic property.
	 * 
	 * @return property string
	 */
	public static String ifmapUrlBasic() {
		return get(IFMAP_URL_BASIC);
	}

	/**
	 * Getter for the ifmapUrlCert property.
	 * 
	 * @return property string
	 */
	public static String ifmapUrlCert() {
		return get(IFMAP_URL_CERT);
	}

	/**
	 * Getter for the ifmapBasicUser property.
	 * 
	 * @return property string
	 */
	public static String ifmapBasicUser() {
		return get(IFMAP_BASIC_USER);
	}

	/**
	 * Getter for the ifmapBasicPassword property.
	 * 
	 * @return property string
	 */
	public static String ifmapBasicPassword() {
		return get(IFMAP_BASIC_PASSWORD);
	}

	/**
	 * Getter for the keyStorePath property.
	 * 
	 * @return property string
	 */
	public static String keyStorePath() {
		return get(KEYSTORE_PATH);
	}

	/**
	 * Getter for the keyStorePassword property.
	 * 
	 * @return property string
	 */
	public static String keyStorePassword() {
		return get(KEYSTORE_PASSWORD);
	}

	/**
	 * Getter for the openFlowControllerIP property.
	 * 
	 * @return property string
	 */
	public static String websocketServerUrl() {
		return get(WEBSOCKET_SERVER_URL);
	}



	/**
	 * Getter for the request strategies ClassnamePropertiesFilename property.
	 * 
	 * @return property string
	 */
	public static String irongenlogRequestStrategiesClassnamePropertiesFilename() {
		return get(REQUEST_STRATEGIES_CLASSNAMES_FILENAME);
	}
	
	/**
	 * Getter for the ifmapKeepalive property.
	 * 
	 * @return property integer
	 */
	public static int ifmapKeepalive() {
		return Integer.parseInt(get(IFMAP_KEEPALIVE));
	}

}
