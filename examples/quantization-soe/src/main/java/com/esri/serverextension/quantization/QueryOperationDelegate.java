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
import com.esri.arcgis.geometry.IJSONConverterGeometry;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.JSONConverterGeometry;
import com.esri.arcgis.system.IJSONObject;
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


    @RequestMapping("/layers/{layerId}/query")
    public RestResponse query(
            @PathVariable("layerId") int layerId,
            RestRequest request,
            RestDelegate handler,
            ServerObjectExtensionContext serverContext,
            SecurityContext securityContext) throws IOException {

        //int layerId = 0;
        String requestInput = request.getOperationInput();
        logger.debug("Request Input:" + requestInput);
        QuantizationQueryOperationInput quantizationInput = objectMapper.readValue(
                requestInput, QuantizationQueryOperationInput.class);


        if (quantizationInput != null && quantizationInput.getResultType() != null && quantizationInput.getResultType().equalsIgnoreCase("tile")){

            IMapLayerInfo layerInfo = MapServerUtilities.getPolygonFeatureLayerByID(layerId, serverContext);

            IFeatureClass featureClass = MapServerUtilities.getPolygonFeatureClassByLayerID(layerId, serverContext);
            IQueryFilter queryFilter = getQueryFilter(quantizationInput, featureClass.getShapeFieldName());

            if (quantizationInput.getReturnExceededLimitFeatures() == false){
                int featureCount = featureClass.featureCount(queryFilter);
                int maxRecordCount = MapServerUtilities.getMaxRecordCount(serverContext);
                if (featureCount > maxRecordCount){
                    /*
                    {"objectIdFieldName":"OBJECTID","globalIdFieldName":"",
                    "geometryProperties":{"shapeAreaFieldName":"Shape__Area",
                    "shapeLengthFieldName":"Shape__Length","units":"esriMeters"},
                    "exceededTransferLimit":true,"features":[]}
                     */
                    JSONObject retResource = new JSONObject();
                    retResource.put("objectIdFieldName", featureClass.getOIDFieldName());
                    retResource.put("exceededTransferLimit", true);
                    retResource.put("features", new JSONArray());
                    byte[] data = retResource.toString().getBytes("utf-8");
                    return new RestResponse(null, data);
                }
            }

            QuantizationParameters params =  quantizationInput.getQuantizationParameters();
            logger.debug("Quantization Parameters:" + params);
            if (params != null){
                logger.debug("Mode:"+params.getMode());
                logger.debug("Origin Position:"+params.getOriginPosition());
                logger.debug("Tolerance:"+params.getTolerance());
                logger.debug("Extent:"+params.getExtent());
            }



            QuantizationCallbackHandler quantizationCallbackHandler = new QuantizationCallbackHandler(quantizationInput, featureClass.getShapeFieldName());
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

            JSONObject jsonTransform = new JSONObject();
            jsonTransform.put("originPosition", "upperLeft");
            JSONArray jsonScale = new JSONArray();
            jsonScale.put(params.getTolerance());
            jsonScale.put(params.getTolerance());
            jsonTransform.put("scale", jsonScale);
            JSONArray jsonTranslate = new JSONArray();
            jsonTranslate.put(params.getExtent().getXmin());
            jsonTranslate.put(params.getExtent().getYmax());
            jsonTransform.put("translate", jsonTranslate);
            rootResource.put("transform", jsonTransform);



            ISpatialReference spRef = getOutSpatialReference(quantizationInput, serverContext);


            IJSONObject jsonObject = new com.esri.arcgis.system.JSONObject();
            IJSONConverterGeometry converterGeometry = new JSONConverterGeometry();
            converterGeometry.queryJSONSpatialReference(spRef,
                    jsonObject);
            String json = jsonObject.toJSONString(null);
            JSONObject jsonSpatialReference = new JSONObject(json);

            rootResource.put("spatialReference", jsonSpatialReference);

            JSONArray jsonFields = new JSONArray();
            List<Field> fields = quantizationCallbackHandler.getFieldList();
            for (Field field:fields){
                JSONObject jsonField = new JSONObject();
                jsonField.put("alias", field.getAlias());
                jsonField.put("name", field.getName());
                jsonField.put("type", field.getType());
                jsonFields.put(jsonField);
            }
            rootResource.put("fields", jsonFields);

            rootResource.put("geometryType", "esriGeometryPolygon");
            rootResource.put("objectIdFieldName", featureClass.getOIDFieldName());
            byte[] data = rootResource.toString().getBytes("utf-8");
            return new RestResponse(null, data);
            //return rootResource;

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

            String outFields = input.getOutFields();
            if (outFields != null && outFields.length()>0){
                String allFields[] = outFields.split(",");
                boolean bFoundShape = false;
                for (String field:allFields){
                    if (field.equalsIgnoreCase(shapeFieldName)){
                        bFoundShape = true;
                    }
                }
                if (!bFoundShape){
                    outFields = outFields+","+shapeFieldName;
                }
                queryFilter.setSubFields(outFields);
            }


            return queryFilter;
        } catch (IOException ex) {
            throw new ArcObjectsInteropException("Failed to create query filter.", ex);
        }
    }


}
