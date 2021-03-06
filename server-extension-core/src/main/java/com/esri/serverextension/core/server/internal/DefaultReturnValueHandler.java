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

package com.esri.serverextension.core.server.internal;

import com.esri.arcgis.geodatabase.IRecordSet;
import com.esri.arcgis.server.json.JSONArray;
import com.esri.arcgis.server.json.JSONObject;
import com.esri.serverextension.core.rest.json.JSONConverter;
import com.esri.serverextension.core.server.RestRequest;
import com.esri.serverextension.core.server.RestResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

public final class DefaultReturnValueHandler implements ReturnValueHandler {

    private final ObjectMapper objectMapper;

    public DefaultReturnValueHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public RestResponse handleReturnValue(Object returnValue,
                                          MethodParameter parameter, RestRequest request) throws Exception {
        Class<?> type = parameter.getParameterType();
        XmlRootElement xmlRootElement = type
                .getAnnotation(XmlRootElement.class);
        if (RestResponse.class.isAssignableFrom(type)) {
            return (RestResponse) returnValue;
		} else if (CharSequence.class.isAssignableFrom(type)) {
            return new RestResponse(null,
                    JSONConverter.toByteArray((String) returnValue));
        } else if (xmlRootElement != null) {
            return new RestResponse(null,
                    objectMapper.writeValueAsBytes(returnValue));
        } else if (IRecordSet.class.isAssignableFrom(type)) {
            return new RestResponse(null,
                    objectMapper.writeValueAsBytes(returnValue));
		} else if (JSONObject.class.isAssignableFrom(type)) {
            return new RestResponse(null,
					JSONConverter.toByteArray(((JSONObject) returnValue)
							.toString()));
		} else if (JSONArray.class.isAssignableFrom(type)) {
            return new RestResponse(null,
					JSONConverter.toByteArray(((JSONArray) returnValue)
							.toString()));
		} else if (Collection.class.isAssignableFrom(type)) {
			return new RestResponse(null,
					objectMapper.writeValueAsBytes(returnValue));
        }
        throw new IllegalArgumentException(String.format(
                "Cannot handle return value: %1$s", returnValue));
    }

    @Override
    public boolean handles(MethodParameter parameter) {
        if (parameter.getParameterIndex() != -1) {
            throw new IllegalArgumentException(
                    "Method parameter must be a return value.");
        }
        Class<?> type = parameter.getParameterType();
		return RestRequest.class.isAssignableFrom(type)
				|| RestResponse.class.isAssignableFrom(type)
				|| CharSequence.class.isAssignableFrom(type)
                || IRecordSet.class.isAssignableFrom(type)
                || type.getAnnotation(XmlRootElement.class) != null
                || IRecordSet.class.isAssignableFrom(type)
				|| JSONObject.class.isAssignableFrom(type)
				|| JSONArray.class.isAssignableFrom(type)
				|| Collection.class.isAssignableFrom(type);

    }
}
