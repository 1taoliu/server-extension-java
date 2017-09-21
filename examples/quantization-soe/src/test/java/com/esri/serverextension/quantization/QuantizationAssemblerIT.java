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

import com.esri.arcgis.geodatabase.*;
import com.esri.arcgis.geometry.*;
import com.esri.arcgis.system._WKSPoint;
import com.esri.serverextension.core.geodatabase.GeodatabaseFieldMap;
import com.esri.serverextension.core.geodatabase.GeodatabaseObjectCallbackHandler;
import com.esri.serverextension.core.geodatabase.GeodatabaseTemplate;
import com.esri.serverextension.core.geodatabase.ShapefileWorkspaceFactoryBean;
import com.esri.serverextension.core.util.ArcObjectsInitializer;
import com.esri.serverextension.core.util.ArcObjectsUtilities;
import com.esri.serverextension.core.util.StopWatch;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;

import java.io.IOException;
import java.util.Arrays;

public class QuantizationAssemblerIT {

    public static void main(String[] args) throws Exception {
        StopWatch timer = StopWatch.createAndStart();
        System.out.println("1. Starting ArcObjects runtime.");
        ArcObjectsInitializer.getInstance().init();

        System.out.println("2. Opening shapefile.");
        ShapefileWorkspaceFactoryBean shapefileWorkspaceFactoryBean = new ShapefileWorkspaceFactoryBean();
        shapefileWorkspaceFactoryBean.setDatabase("D:\\Development\\Projects\\sever-extension-java\\examples\\quantization-soe\\data\\Quantization");
        IWorkspace workspace = shapefileWorkspaceFactoryBean.getObject();
        IFeatureClass featureClass = ((IFeatureWorkspace) workspace).openFeatureClass("tl_2016_06_bg");

        System.out.println("3. Setting up query filter.");
        SpatialFilter spatialFilter = new SpatialFilter();
        spatialFilter.setSubFields("*");
        spatialFilter.setGeometryField("Shape");
        spatialFilter.setWhereClause("1=1");
        spatialFilter.setOutputSpatialReferenceByRef("Shape", ArcObjectsUtilities.createSpatialReference(102100));

        System.out.println("4. Executing query.");
        GeodatabaseTemplate geodatabaseTemplate = new GeodatabaseTemplate();
        final IGeometryBridge2 geometryBridge = new GeometryEnvironment();
        final IGeometryFactory2  geometryFactory = (IGeometryFactory2)geometryBridge;
        final WKBReader wkbReader = new WKBReader();
        final int[] featureCount = new int[1];
        featureCount[0] = 0;
        final int[] ringCount = new int[1];
        ringCount[0] = 0;
        final int[] vertexCount = new int[1];
        vertexCount[0] = 0;
        geodatabaseTemplate.query(featureClass, spatialFilter, new GeodatabaseObjectCallbackHandler() {
            @Override
            public void setGeodatabaseFieldMap(GeodatabaseFieldMap fieldMap) throws IOException {

            }

            @Override
            public void processRow(IRow row) throws IOException {

            }

            @Override
            public void processFeature(IFeature feature) throws IOException {
                featureCount[0] = featureCount[0] + 1;
                IPolygon polygon = (IPolygon) feature.getShape();
//                IGeometryCollection geometryCollection = (IGeometryCollection) polygon;
//                int ringCount = geometryCollection.getGeometryCount();
//                for (int i = 0; i < ringCount; i++) {
//                    ringCount[0] = ringCount[0] + 1;
//                    IPointCollection4 ring = (IPointCollection4) geometryCollection.getGeometry(i);
//                    int pointCount = ring.getPointCount();
//                    vertexCount[0] = vertexCount[0] + pointCount;
//                    _WKSPoint[][] points = new _WKSPoint[1][ring.getPointCount()];
//                    for (int j = 0; j < pointCount; j++) {
//                        points[0][j] = new _WKSPoint();
//                    }
//                    geometryBridge.queryWKSPoints(ring, 0, points);//
//                    for (_WKSPoint point : points[0]) {
//                        if (point.x == 0.0d && point.y == 0.0d) {
//                            System.out.println("Coordinates are zero.");
//                        }
//                    }
//                }
                byte[] wkb = (byte[])geometryFactory.createWkbVariantFromGeometry(polygon);
                Geometry geometry = null;
                try {
                    geometry = wkbReader.read(wkb);
                } catch (ParseException e) {
                    throw new RuntimeException("Cannot read WKB.");
                }
            }
        });

        ArcObjectsInitializer.getInstance().shutdown();
        System.out.println(String.format("Features fetched: %1$d", featureCount[0]));
        System.out.println(String.format("Vertices fetched: %1$d", vertexCount[0]));
        System.out.println(String.format("Time elapsed: %1$f", timer.stop().elapsedTimeSeconds()));
    }
}
