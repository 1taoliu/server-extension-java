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
package com.esri.serverextension.quantization;

import com.esri.arcgis.carto.IMapLayerInfo;
import com.esri.arcgis.server.json.JSONObject;
import com.esri.serverextension.core.server.RestDelegate;
import com.esri.serverextension.core.server.RestRequest;
import com.esri.serverextension.core.server.RestResponse;
import com.esri.serverextension.core.server.ServerObjectExtensionContext;
import com.esri.serverextension.core.util.ArcObjectsInteropException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Component
public class LayerInfoDelegate {

    protected final Logger logger = LoggerFactory.getLogger(LayerInfoDelegate.class);
    @Order(0)
    @RequestMapping("//layers/0")
    public RestResponse query(
            RestRequest request,
            RestDelegate handler) throws IOException {
        logger.debug("LayerInfo Delegate ...!!!");

        RestResponse response =  handler.process(request, null);

        JSONObject jsonObject = new JSONObject(new String(response.getResponseBody(), "utf-8"));
        jsonObject.put("supportsCoordinatesQuantization", true);

        JSONObject advObject = jsonObject.getJSONObject("advancedQueryCapabilities");
        advObject.put("supportsReturningGeometryCentroid", true);

        //Not sure if I have to put this out again to take affect, documentation really doesn't say.
        //this is what would need to happen if it doesn't show.
        jsonObject.put("advancedQueryCapabilities", advObject);

        byte[] data = jsonObject.toString().getBytes("utf-8");
        return new RestResponse(null, data);
    }
}
