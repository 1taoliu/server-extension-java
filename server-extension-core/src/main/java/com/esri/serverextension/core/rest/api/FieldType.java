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

import com.esri.arcgis.geodatabase.esriFieldType;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

@XmlType
public enum FieldType implements Serializable {
	esriFieldTypeSmallInteger, esriFieldTypeInteger, esriFieldTypeSingle, esriFieldTypeDouble,
	esriFieldTypeString, esriFieldTypeDate, esriFieldTypeOID, esriFieldTypeGeometry, esriFieldTypeBlob,
	esriFieldTypeRaster, esriFieldTypeGUID, esriFieldTypeGlobalID, esriFieldTypeXML;

	static public FieldType convertIntegerTypeToFieldType(int intType)
	{
		switch(intType){
			case esriFieldType.esriFieldTypeBlob:
				return FieldType.esriFieldTypeBlob;
			case esriFieldType.esriFieldTypeDate:
				return FieldType.esriFieldTypeDate;
			case esriFieldType.esriFieldTypeDouble:
				return FieldType.esriFieldTypeDouble;
			case esriFieldType.esriFieldTypeGeometry:
				return FieldType.esriFieldTypeGeometry;
			case esriFieldType.esriFieldTypeGlobalID:
				return FieldType.esriFieldTypeGlobalID;
			case esriFieldType.esriFieldTypeGUID:
				return FieldType.esriFieldTypeGUID;
			case esriFieldType.esriFieldTypeInteger:
				return FieldType.esriFieldTypeInteger;
			case esriFieldType.esriFieldTypeOID:
				return FieldType.esriFieldTypeOID;
			case esriFieldType.esriFieldTypeRaster:
				return FieldType.esriFieldTypeRaster;
			case esriFieldType.esriFieldTypeSingle:
				return FieldType.esriFieldTypeSingle;
			case esriFieldType.esriFieldTypeSmallInteger:
				return FieldType.esriFieldTypeSmallInteger;
			default:
			case esriFieldType.esriFieldTypeString:
				return FieldType.esriFieldTypeString;
			case esriFieldType.esriFieldTypeXML:
				return FieldType.esriFieldTypeXML;
		}


	}

}
