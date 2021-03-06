/*
 * Copyright (c) 2017 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.​
 */

package com.esri.serverextension.core.rest.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class Field implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String name;
	private FieldType type;
	private String alias;
	private Integer length;

	public Field() {
	}

	public Field(String name, String alias) {
		this.name = name;
		this.alias = alias;
	}

	public Field(String name, FieldType type, String alias) {
		this.name = name;
		this.type = type;
		this.alias = alias;
	}

	public Field(String name, FieldType type, String alias, Integer length) {
		this.name = name;
		this.type = type;
		this.alias = alias;
		this.length = length;
	}

	public String getName() {
		return name;
	}

	public FieldType getType() {
		return type;
	}

	public String getAlias() {
		return alias;
	}

	public Integer getLength() {
		return length;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
