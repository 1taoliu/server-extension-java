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
import com.esri.arcgis.geodatabase.*;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.serverextension.core.geodatabase.GeodatabaseTemplate;
import com.esri.serverextension.core.rest.api.Field;
import com.esri.serverextension.core.rest.api.FieldType;
import com.esri.serverextension.core.rest.api.QueryMapServiceLayerOperationInput;
import com.esri.serverextension.core.rest.api.*;
import com.esri.serverextension.core.security.SecurityContext;
import com.esri.serverextension.core.server.RestDelegate;
import com.esri.serverextension.core.server.RestRequest;
import com.esri.serverextension.core.server.RestResponse;
import com.esri.serverextension.core.server.ServerObjectExtensionContext;
import com.esri.serverextension.core.util.ArcObjectsInteropException;
import com.esri.serverextension.core.util.GenericEsriEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.apache.commons.lang3.StringUtils;
import com.esri.arcgis.interop.Cleaner;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.esri.arcgis.server.json.JSONObject;
import com.esri.arcgis.server.json.JSONArray;


@Component
public class QueryOperationDelegate {

    protected final Logger logger = LoggerFactory.getLogger(QueryOperationDelegate.class);

    private ObjectMapper objectMapper;

    public QueryOperationDelegate() {
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    @RequestMapping("/layers/0/query")
    public JSONObject query(
            RestRequest request,
            RestDelegate handler,
            ServerObjectExtensionContext serverContext,
            SecurityContext securityContext) throws IOException {

        int layerId = 0;
        String requestInput = request.getOperationInput();
        logger.debug("Request Input:" + requestInput);
        QuantizationQueryOperationInput quantizationInput = objectMapper.readValue(
                requestInput, QuantizationQueryOperationInput.class);


      //  if (quantizationInput.getResultType().equalsIgnoreCase("tile")){

            IMapLayerInfo layerInfo = MapServerUtilities.getPolygonFeatureLayerByID(layerId, serverContext);

            IFeatureClass featureClass = MapServerUtilities.getPolygonFeatureClassByLayerID(layerId, serverContext);
            IQueryFilter queryFilter = getQueryFilter(quantizationInput, featureClass.getShapeFieldName());

        QuantizationParameters params =  quantizationInput.getQuantizationParameters();
        logger.debug("Quantization Parameters:" + params);
        if (params != null){
            logger.debug("Mode:"+params.getMode());
            logger.debug("Origin Position:"+params.getOriginPosition());
            logger.debug("Tolerance:"+params.getTolerance());
            logger.debug("Extent:"+params.getExtent());
        }



        QuantizationCallbackHandler quantizationCallbackHandler = new QuantizationCallbackHandler(quantizationInput.getQuantizationParameters());
            GeodatabaseTemplate geodatabaseTemplate = new GeodatabaseTemplate();
            geodatabaseTemplate.query(featureClass, queryFilter, quantizationCallbackHandler);



            /*
            FeatureSet featureSet = new FeatureSet();
            featureSet.setDisplayFieldName(layerInfo.getDisplayField());
            //List<Field> fields = new ArrayList<>();
            //fields.add(field);
            featureSet.setFields(quantizationCallbackHandler.getFieldList());
            featureSet.setSpatialReference(getOutSpatialReference(quantizationInput, serverContext));
            featureSet.setGeometryType(GeometryType.esriGeometryPoint);
            featureSet.setFeatures(quantizationCallbackHandler.getQuantizationFeatures());
            return featureSet;
            */

            JSONObject rootResource = new JSONObject();
            rootResource.put("features", quantizationCallbackHandler.getQuantizationFeatures());

            return rootResource;
 /*
        }

        else{
            RestRequest filteredRequest = RestRequest.create(
                    request.getCapabilities(),
                    request.getResourceName(),
                    request.getOperationName(),
                    requestInput,
                    request.getOutputFormat(),
                    request.getRequestProperties(),
                    request);

            return handler.process(filteredRequest, null);

        }
*/
    }


    private ISpatialReference getOutSpatialReference(QuantizationQueryOperationInput input, ServerObjectExtensionContext serverContext) {
        if (input.getOutSR() != null) {
            return input.getOutSR();
        }
        return MapServerUtilities.getMapSpatialReference(serverContext);
    }



    private IQueryFilter getQueryFilter(QuantizationQueryOperationInput input, String shapeFieldName) {
        try {
            IQueryFilter2 queryFilter = null;
            if (input.getGeometry() != null) {
                SpatialFilter spatialFilter = new SpatialFilter();
                spatialFilter.setGeometryByRef(input.getGeometry());
                spatialFilter.setGeometryField(shapeFieldName);
                if (input.getSpatialRel() != null) {
                    spatialFilter.setSpatialRel(GenericEsriEnum.valueOf(esriSpatialRelEnum.class, input.getSpatialRel().name()));
                }
                if (StringUtils.isNotEmpty(input.getRelationParam())) {
                    spatialFilter.setSpatialRelDescription(input.getRelationParam());
                }
                if (input.getOutSR() != null) {
                    spatialFilter.setOutputSpatialReferenceByRef(shapeFieldName, input.getOutSR());
                }
                queryFilter = spatialFilter;
            } else {
                queryFilter = new QueryFilter();
            }
            if (StringUtils.isNotEmpty(input.getWhere())) {
                SQLCheck sqlCheck = new SQLCheck();
                sqlCheck.checkWhereClause(input.getWhere());
                Cleaner.release(sqlCheck);
                queryFilter.setWhereClause(input.getWhere());
            }
            return queryFilter;
        } catch (IOException ex) {
            throw new ArcObjectsInteropException("Failed to create query filter.", ex);
        }
    }


}
