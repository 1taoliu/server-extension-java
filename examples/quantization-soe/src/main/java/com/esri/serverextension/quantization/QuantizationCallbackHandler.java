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

import com.esri.arcgis.geometry.*;

import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.system._WKSPoint;
import com.esri.arcgis.system._WKSPointZ;
import com.esri.serverextension.core.geodatabase.GeodatabaseFieldMap;
import com.esri.serverextension.core.geodatabase.GeodatabaseObjectCallbackHandler;
import com.esri.serverextension.core.rest.api.Extent;
import com.esri.serverextension.core.rest.api.Feature;
import com.esri.serverextension.core.rest.api.Field;
import com.esri.serverextension.core.rest.api.FieldType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.esri.arcgis.server.json.JSONObject;
import com.esri.arcgis.server.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.java2d.pipe.SpanShapeRenderer;


public class QuantizationCallbackHandler implements GeodatabaseObjectCallbackHandler {
    protected final Logger logger = LoggerFactory.getLogger(QuantizationCallbackHandler.class);
    private GeodatabaseFieldMap fieldMap;
    private QuantizationParameters quantizationParameters;
    private JSONArray quantizedFeatures = new JSONArray();
    private List<Field> fieldList;
    private double originX = 0.0;
    private double originY = 0.0;
    private double qTolerance = 1.0;
    private String shapeFieldName;
    private QuantizationQueryOperationInput input;


    public QuantizationCallbackHandler(QuantizationQueryOperationInput input, String shapeFieldName) {
        this.input = input;
        this.quantizationParameters = input.getQuantizationParameters();
        this.shapeFieldName = shapeFieldName;
        if (this.quantizationParameters != null){
            Extent extent = this.quantizationParameters.getExtent();
            if (extent != null){
                originX = extent.getXmin();
                originY = extent.getYmax();
                qTolerance = this.quantizationParameters.getTolerance();
            }

        }


    }

    public JSONArray getQuantizationFeatures() {
        return quantizedFeatures;
    }

    public List<Field> getFieldList(){
        return fieldList;
    }



    @Override
    public void setGeodatabaseFieldMap(GeodatabaseFieldMap fieldMap) throws IOException {
        this.fieldMap = fieldMap;
        //clusterFieldIndex = fieldMap.get(clusterFieldName).getIndex();
        this.fieldList = new ArrayList<>();
        for (GeodatabaseFieldMap.FieldIndex fieldIndex : fieldMap.getFieldIndices()) {
            Field field = new Field(fieldIndex.getField().getName(), FieldType.convertIntegerTypeToFieldType(fieldIndex.getField().getType()),
                    fieldIndex.getField().getAliasName(), fieldIndex.getField().getLength());
            fieldList.add(field);
        }

    }




    @Override
    public void processRow(IRow row) throws IOException {
        throw new UnsupportedOperationException("This callback handler only supports features.");
    }

    @Override
    public void processFeature(IFeature feature) throws IOException {
        IGeometry geometry = feature.getShape();

        if (geometry instanceof IPolygon && !geometry.isEmpty()) {

            Map<String, Object> attributes = new LinkedHashMap<>();
            for (GeodatabaseFieldMap.FieldIndex fieldIndex : fieldMap.getFieldIndices()) {
                attributes.put(fieldIndex.getField().getName(), feature.getValue(fieldIndex.getIndex()));
            }
            //IPolygon pgon = (IPolygon)geometry;
            IArea area = (IArea)geometry;
            IPoint centroid = area.getCentroid();

            JSONObject polyJson = quantizePolygon((IPolygon)geometry);
            Feature newFeature = new Feature();
            //newFeature.setAttributes(attributes);
            //newFeature.setGeometry(quantizedPolygon);
            //newFeature.setGeometry(null);

            JSONObject featureObject = new JSONObject();

            //featureObject.put("name", "dummy");
            //    layerObject.put("id", layerInfo.getID());
             //   layerObject.put("description", layerInfo.getDescription());

            if (input.getReturnCentroid()){
                JSONObject centroidObject = new JSONObject();
                centroidObject.put("x", snapX(centroid.getX()));
                centroidObject.put("y", snapY(centroid.getY()));
                featureObject.put("centroid", centroidObject);
            }


            featureObject.put("geometry", polyJson);

            JSONObject jsonAttributes = new JSONObject();
            for (Map.Entry<String, Object> entry:attributes.entrySet()){
                if (!entry.getKey().equalsIgnoreCase(shapeFieldName)){
                    jsonAttributes.put(entry.getKey(), entry.getValue().toString());
                }
            }
            featureObject.put("attributes", jsonAttributes);


            quantizedFeatures.put(featureObject);
        }
    }

    /**
     * Quantizes the polygon based on parameters within this class
     * @param pgon the passed polygon
     * @return the json geometry
     */
    private JSONObject quantizePolygon(IPolygon pgon){


    /*
    quantizationParameters:{"mode":"view","originPosition":"upperLeft",
    "tolerance":2445.984905125002,
    "extent":{"xmin":-12523442.714242995,"ymin":5009377.085698988,"xmax":-11271098.442818994,"ymax":6261721.357122989,
    "spatialReference":{"latestWkid":3857,"wkid":102100}}}
     */
        long numPoints = 0;
        JSONObject tally = new JSONObject();

        JSONArray jsonRingsArray = new JSONArray();
        try {

            /*
            IGeometryCollection collection = (IGeometryCollection)pgon;
            int numRings = collection.getGeometryCount();
            for (int i=0;i<numRings;i++){
                IGeometry geom = collection.getGeometry(i);
                ArrayList<SimplePoint> ringPoints = getAndQuantize((IPointCollection4)geom);
                if (ringPoints.size() > 3){

                    JSONArray jsonRingArray = new JSONArray();
                    SimplePoint lastPoint = null;
                    for (SimplePoint pt:ringPoints){
                        JSONArray jsonPoint = new JSONArray();
                        if (lastPoint != null){
                            jsonPoint.put(pt.x-lastPoint.x);
                            jsonPoint.put(pt.y-lastPoint.y);
                        }else {
                            jsonPoint.put(pt.x);
                            jsonPoint.put(pt.y);
                        }
                        lastPoint = pt;
                        jsonRingArray.put(jsonPoint);
                    }
                    jsonRingsArray.put(jsonRingArray);
                }

            }
            */

            IGeometryFactory2 geometryFactory2 = new GeometryEnvironment();


            byte []wkbGeometry = (byte[]) geometryFactory2.createWkbVariantFromGeometry(pgon);

            ArrayList<ArrayList<SimplePoint>> polygon = readPolygon(wkbGeometry);
            for(ArrayList<SimplePoint> ring:polygon){

                    JSONArray jsonRingArray = new JSONArray();
                    SimplePoint lastPoint = null;
                    for (SimplePoint pt:ring){
                        JSONArray jsonPoint = new JSONArray();
                        if (lastPoint != null){
                            jsonPoint.put(pt.x-lastPoint.x);
                            jsonPoint.put(pt.y-lastPoint.y);
                        }else {
                            jsonPoint.put(pt.x);
                            jsonPoint.put(pt.y);
                        }
                        lastPoint = pt;
                        jsonRingArray.put(jsonPoint);
                    }
                    jsonRingsArray.put(jsonRingArray);
            }




        } catch (Exception e) {

            logger.debug("Error", e);
            tally.put("Error", "1");
            return tally;
        }


        tally.put("rings", jsonRingsArray);

        return tally;

    }


    public  ArrayList<ArrayList<SimplePoint>> readPolygon(byte[] bytes){
        ArrayList<ArrayList<SimplePoint>> allRings = new ArrayList<ArrayList<SimplePoint>>();
        int byteOrder = bytes[0];
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        if (byteOrder == 1){
            buf.order(ByteOrder.LITTLE_ENDIAN);
        }

        buf.get();
        int geomType = buf.getInt();
        int numPolygons = 1;

        if (geomType == 6){
            numPolygons = buf.getInt();
        }else if (geomType != 3){
            return allRings;
        }

        for (int k=0;k<numPolygons;k++){

            if (geomType == 6){
                buf.get();//byteorder
                buf.getInt();//wkbType
            }

            int numRings = buf.getInt();

            for (int i=0;i<numRings;i++){
                ArrayList<SimplePoint> ring = new ArrayList<SimplePoint>();
                int numPoints = buf.getInt();
                try {
                    SimplePoint lastPoint = null;
                    for (int j=0;j<numPoints;j++){
                        double x =buf.getDouble();

                        double y = buf.getDouble();
                        long xx = snapX(x);
                        long yy = snapY(y);

                        if (lastPoint == null || !lastPoint.equals(xx,yy)){
                            lastPoint = new SimplePoint(xx,yy);
                            ring.add(lastPoint);
                        }


                    }
                    if (ring.size() > 2){
                        allRings.add(ring);
                    }
                } catch (Exception e) {
                    logger.debug("geomType:"+geomType+"  numRIngs:"+numRings+" numPoints:"+numPoints, e);
                }

            }
        }

        return allRings;
    }
    private ArrayList<SimplePoint> getAndQuantize(IPointCollection4 ptCollection) throws IOException{
        ArrayList<SimplePoint> retList = new ArrayList<SimplePoint>();
        SimplePoint lastPoint = null;
        int ptCount = ptCollection.getPointCount();

        GeometryEnvironment gBridge = new GeometryEnvironment();


        _WKSPoint[][] wksPointBuffer = new _WKSPoint[1][ptCount];
        for (int j=0;j<ptCount;j++){
            wksPointBuffer[0][j] = new _WKSPoint();
        }

        gBridge.queryWKSPoints(ptCollection, 0, wksPointBuffer);

        for (int i=0;i<ptCount;i++){

            //IPoint ipt = ptCollection.getPoint(i);
            _WKSPoint pt = wksPointBuffer[0][i];

            //SimplePoint pt = new SimplePoint(snapX(ipt.getX()), snapY(ipt.getY()));
            long xx = snapX(pt.x);
            long yy = snapY(pt.y);

            if (lastPoint == null || !lastPoint.equals(xx,yy)){
                lastPoint = new SimplePoint(xx,yy);
                retList.add(lastPoint);
            }

        }
        return retList;
    }

    /*
    private ArrayList<SimplePoint> getAndQuantize(IPointCollection4 ptCollection) throws IOException{
        ArrayList<SimplePoint> retList = new ArrayList<SimplePoint>();
        SimplePoint lastPoint = null;
        int ptCount = ptCollection.getPointCount();



        for (int i=0;i<ptCount;i++){

            IPoint ipt = ptCollection.getPoint(i);

            //SimplePoint pt = new SimplePoint(snapX(ipt.getX()), snapY(ipt.getY()));
            long xx = snapX(ipt.getX());
            long yy = snapY(ipt.getY());

            if (lastPoint == null || !lastPoint.equals(xx,yy)){
                lastPoint = new SimplePoint(xx,yy);
                retList.add(lastPoint);
            }

        }
        return retList;
    }
    */

    //the two function below assume upper left

    private long snapX(double x){
        return Math.round((x-originX)/qTolerance);
    }
    private long snapY(double y){
        return Math.round((originY-y)/qTolerance);
    }
}
