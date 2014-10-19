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
 * This file is part of ironflow, version 0.0.1, implemented by the Trust@HsH
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
package de.hshannover.f4.trust.irongenlog.publisher;

import de.hshannover.f4.trust.ifmapj.IfmapJ;
import de.hshannover.f4.trust.ifmapj.channel.SSRC;
import de.hshannover.f4.trust.ifmapj.metadata.StandardIfmapMetadataFactory;


/**
 * This class is an abstract represent of the Implementation of the
 * different logdata publish strategies
 * 
 * 
 * @author Marius Rohde
 * 
 */

public abstract class PublishLogDataStrategy {

	/**
	 * Abstract methode to publish the logdata. Has to be
	 * implemented by the different subclass strategies
	 * 
	 * @param ssrc
	 *            : the ssrc to publish data
	 */

	public abstract void publishLogData(SSRC ssrc, String jsonMsg);



	/**
	 * Helper Methode to get if Map MetaData factory
	 * 
	 * @return mf
	 * 			the Metadatafactory
	 */

	public StandardIfmapMetadataFactory getMetadataFactory() {
		StandardIfmapMetadataFactory mf = IfmapJ.createStandardMetadataFactory();
		return mf;
	}

}
