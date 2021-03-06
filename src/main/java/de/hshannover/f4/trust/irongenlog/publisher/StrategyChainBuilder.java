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
 * This file is part of irongenlog, version 0.1.0, implemented by the Trust@HsH
 * research group at the Hochschule Hannover.
 * %%
 * Copyright (C) 2014 - 2016 Trust@HsH
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
package de.hshannover.f4.trust.irongenlog.publisher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * This class initialize the strategy chain to get the strategies for publishing
 * log data. The config file defines which strategies will be load by reflection
 * Objects
 * 
 * 
 * @author Marius Rohde
 * 
 */

public final class StrategyChainBuilder {

	private static final Logger LOGGER = Logger.getLogger(StrategyChainBuilder.class.getName());

	/**
	 * the List/Chain with the different strategy objects
	 */
	private static ArrayList<PublishLogDataStrategy> strategyChain;

	/**
	 * Death constructor for code convention -> final class because utility
	 * class
	 * 
	 */
	private StrategyChainBuilder() {
	}

	/**
	 * The init method initiate the strategy chain and looks for the classes in
	 * packagepath
	 */

	public static void init(Set<Entry<String, Object>> strategieNames, String packagePath) {

		LOGGER.info("looking for classes in package " + packagePath);

		PublishLogDataStrategy publisherStrategy;
		Iterator<Entry<String, Object>> iteClassnames = strategieNames.iterator();
		strategyChain = new ArrayList<PublishLogDataStrategy>();

		while (iteClassnames.hasNext()) {

			Entry<String, Object> classname = iteClassnames.next();
			LOGGER.info("found classString " + classname.getKey().toString());

			if (classname.getValue().toString().equals("enabled")) {

				publisherStrategy = createNewStrategie(packagePath + classname.getKey().toString());
				if (publisherStrategy != null) {
					strategyChain.add(publisherStrategy);
				}
			}
		}
	}

	/**
	 * This helper method creates a new StrategieObject
	 * 
	 * @param className
	 * @return Strategy object
	 */

	private static PublishLogDataStrategy createNewStrategie(String className) {

		PublishLogDataStrategy request = null;

		try {
			Class<?> cl = Class.forName(className);
			LOGGER.info(cl.toString() + " instantiated");
			if (cl.getSuperclass() == PublishLogDataStrategy.class) {
				request = (PublishLogDataStrategy) cl.newInstance();
			}

		} catch (ClassNotFoundException e) {
			LOGGER.severe("ClassNotFound");
		} catch (InstantiationException e) {
			LOGGER.severe("InstantiationException");
		} catch (IllegalAccessException e) {
			LOGGER.severe("IllegalAccessException");
		}

		return request;
	}

	/**
	 * The Size of the Chain
	 * 
	 * @return the size
	 */

	public static int getSize() {

		return strategyChain.size();
	}

	/**
	 * This method delivers a StrategyObject stored in the chain
	 * 
	 * @param index
	 *            the index of the element
	 * @return an Element
	 */

	public static PublishLogDataStrategy getElementAt(int index) {

		return strategyChain.get(index);
	}

}
