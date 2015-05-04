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
package de.hshannover.f4.trust.irongenlog.publisher;

import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Logger;

import de.hshannover.f4.trust.irongenlog.strategyparser.IfMapStrategyParser;

/**
 * This class initialize the strategy chain to get the strategies for publishing
 * log data. The config file defines which strategies will be load by reflection
 * Objects
 * 
 * 
 * @author Marius Rohde
 * 
 */

public final class StrategyDomParserBuilder {

	private static final Logger LOGGER = Logger.getLogger(StrategyDomParserBuilder.class.getName());

	/**
	 * Death constructor for code convention -> final class because utility
	 * class
	 * 
	 */
	private StrategyDomParserBuilder() {
	}

	/**
	 * The init method initiate the strategy chain and looks for the classes in
	 * packagepath
	 */

	public static void init(Set<Entry<String, Object>> strategieNames, String packagePath) {

		LOGGER.info("looking for dom classes in folder src/main/templates ");

		IfMapStrategyParser parser;
		Iterator<Entry<String, Object>> iteClassnames = strategieNames.iterator();
		String packageSourcePath = packagePath.replace(".", "/");

		while (iteClassnames.hasNext()) {

			Entry<String, Object> classname = iteClassnames.next();
			LOGGER.info("found dom classString " + classname.getKey().toString());

			if (classname.getValue().toString().equals("enabled")) {

				try {
					parser = IfMapStrategyParser.getNewParser("/" + classname.getKey().toString() + ".dom",
							"src/main/java/" + packageSourcePath + classname.getKey().toString() + ".java");
					parser.parse();
				} catch (Exception e) {
					LOGGER.warning("Parser couldnt parse the file correctly: " + e.getMessage());
				}
			}
		}
	}
}
