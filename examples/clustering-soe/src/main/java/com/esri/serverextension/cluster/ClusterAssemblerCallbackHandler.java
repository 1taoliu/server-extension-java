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

package com.esri.serverextension.cluster;

import com.esri.arcgis.geodatabase.IFeature;
import com.esri.arcgis.geodatabase.IRow;
import com.esri.arcgis.geometry.GeometryEnvironment;
import com.esri.arcgis.geometry.IGeometry;
import com.esri.arcgis.geometry.IGeometryFactory2;
import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.system.Cleaner;
import com.esri.serverextension.core.geodatabase.GeodatabaseFieldMap;
import com.esri.serverextension.core.geodatabase.GeodatabaseObjectCallbackHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClusterAssemblerCallbackHandler implements GeodatabaseObjectCallbackHandler {

    private ClusterAssembler clusterAssembler;
    private GeodatabaseFieldMap fieldMap;
    private String clusterFieldName;
    private int clusterFieldIndex = -1;
    private IGeometryFactory2 geometryFactory;

    public ClusterAssemblerCallbackHandler(ClusterAssembler clusterAssembler, String clusterFieldName) {
        this.clusterAssembler = clusterAssembler;
        this.clusterFieldName = clusterFieldName;
    }

    public void destroy() {
        if (geometryFactory != null) {
            Cleaner.release(geometryFactory);
            geometryFactory = null;
        }
    }

    @Override
    public void setGeodatabaseFieldMap(GeodatabaseFieldMap fieldMap) throws IOException {
        this.fieldMap = fieldMap;
        clusterFieldIndex = fieldMap.get(clusterFieldName).getIndex();
    }

    @Override
    public void processRow(IRow row) throws IOException {
        throw new UnsupportedOperationException("This callback handler only supports features.");
    }

    @Override
    public void processFeature(IFeature feature) throws IOException {
        Map<String, Object> attributes = new LinkedHashMap<>();
        for (GeodatabaseFieldMap.FieldIndex fieldIndex : fieldMap.getFieldIndices()) {
            attributes.put(fieldIndex.getField().getName(), feature.getValue(fieldIndex.getIndex()));
        }
        if (geometryFactory == null) {
            geometryFactory = new GeometryEnvironment();
        }
        IGeometry geometry = feature.getShape();
        byte[] wkbGeometry = (byte[]) geometryFactory.createWkbVariantFromGeometry(geometry);
        ClusterPoint clusterPoint = readClusterPoint(wkbGeometry);
        Object value = feature.getValue(clusterFieldIndex);
        if (value == null) {
            return;
        }
        if (value instanceof Number) {
            ClusterFeature clusterFeature = new ClusterFeature(clusterPoint,
                    ((Number) value).doubleValue());
            clusterAssembler.addFeature(clusterFeature);
        }
    }

    private ClusterPoint readClusterPoint(byte[] bytes){
        int byteOrder = bytes[0];
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        if (byteOrder == 1){
            buf.order(ByteOrder.LITTLE_ENDIAN);
        }

        buf.get();
        int geomType = buf.getInt();
        if (geomType != 1){
            throw new IllegalArgumentException(String.format("Unexpected geometry type: %1$d. Expected 1 (point).", geomType));
        }

        double x = buf.getDouble();
        double y = buf.getDouble();
        return new ClusterPoint(x, y);
    }
}
