/*******************************************************************************
 * This file is part of the RozkladPKP project.
 * 
 *     RozkladPKP is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     RozkladPKP is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License 
 *     along with RozkladPKP.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.tyszecki.rozkladpkp;

import java.io.Serializable;
import org.apache.http.NameValuePair;


/**
 * @author Krzysztof Tyszecki
 * Google nie chciało się update'ować tej biblioteki. Więc potrzebna kolejna klasa, tylko po to
 * żeby sobie serializację zrobić...
 */
public class SerializableNameValuePair implements NameValuePair, Serializable {

	private static final long serialVersionUID = -3076858765770770356L;

	String name,value;
	
	public SerializableNameValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}
}
