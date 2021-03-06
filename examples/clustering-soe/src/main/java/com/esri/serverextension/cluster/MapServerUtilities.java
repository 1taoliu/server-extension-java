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

import com.esri.arcgis.carto.*;
import com.esri.arcgis.geodatabase.FeatureClass;
import com.esri.arcgis.geodatabase.IFeatureClass;
import com.esri.arcgis.geometry.ISpatialReference;
import com.esri.arcgis.geometry.esriGeometryType;
import com.esri.arcgis.interop.Cleaner;
import com.esri.serverextension.core.server.ServerObjectExtensionContext;
import com.esri.serverextension.core.util.ArcObjectsInteropException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapServerUtilities {

    public static final List<IMapLayerInfo> getPointFeatureLayers(
            ServerObjectExtensionContext serverContext) {
        try {
            List<IMapLayerInfo> layerInfoList = new ArrayList<>();
            IMapServerDataAccess mapServerDataAccess = (IMapServerDataAccess) serverContext
                    .getServerObject();
            IMapServer3 mapServer = (IMapServer3) mapServerDataAccess;
            String mapName = mapServer.getDefaultMapName();
            IMapServerInfo4 mapServerInfo = (IMapServerInfo4) mapServer
                    .getServerInfo(mapName);
            IMapLayerInfos layerInfos = mapServerInfo.getMapLayerInfos();
            int layerCount = layerInfos.getCount();
            for (int i = 0; i < layerCount; i++) {
                IMapLayerInfo layerInfo = layerInfos.getElement(i);
                if (layerInfo.isComposite()) {
                    continue;
                }
                if (layerInfo.isFeatureLayer()) {
                    Object dataSource = mapServerDataAccess
                            .getDisplayDataSource(mapName, layerInfo.getID());
                    IFeatureClass featureClass = new FeatureClass(dataSource);
                    int geometryType = featureClass.getShapeType();
                    if (geometryType == esriGeometryType.esriGeometryPoint) {
                        layerInfoList.add(layerInfo);
                    }
                    Cleaner.release(featureClass);
                }
            }
            return layerInfoList;
        } catch (IOException ex) {
            throw new ArcObjectsInteropException(
                    "Failed to get point feature layers from map server object.");
        }
    }

    public static final IMapLayerInfo getPointFeatureLayerByID(int id,
                                                               ServerObjectExtensionContext serverContext) {
        List<IMapLayerInfo> layerInfoList = getPointFeatureLayers(serverContext);
        for (IMapLayerInfo layerInfo : layerInfoList) {
            try {
                if (layerInfo.getID() == id) {
                    return layerInfo;
                }
            } catch (IOException ex) {
                throw new ArcObjectsInteropException(
                        String.format("Failed to access point feature layer: %1$d", id));
            }
        }
        throw new IllegalArgumentException(String.format("No such point feature layer: %1$d", id));
    }

    public static final IFeatureClass getPointFeatureClassByLayerID(int layerId,
                                                               ServerObjectExtensionContext serverContext) {
        try {
            IMapLayerInfo layerInfo = getPointFeatureLayerByID(layerId, serverContext);
            IMapServerDataAccess mapServerDataAccess = (IMapServerDataAccess) serverContext
                    .getServerObject();
            IMapServer3 mapServer = (IMapServer3) mapServerDataAccess;
            String mapName = mapServer.getDefaultMapName();
            Object dataSource = mapServerDataAccess
                    .getDisplayDataSource(mapName, layerInfo.getID());
            return new FeatureClass(dataSource);
        } catch (IOException ex) {
            throw new ArcObjectsInteropException(
                    "Failed to get point feature class from map server object.");
        }
    }

    public static final ISpatialReference getMapSpatialReference(ServerObjectExtensionContext serverContext) {
        try {
            IMapServer3 mapServer = (IMapServer3) serverContext
                    .getServerObject();
            String mapName = mapServer.getDefaultMapName();
            IMapServerInfo mapServerInfo = (IMapServerInfo) mapServer
                    .getServerInfo(mapName);
            IMapDescription mapDescription = mapServerInfo.getDefaultMapDescription();
            return mapDescription.getSpatialReference();
        } catch (IOException ex) {
            throw new ArcObjectsInteropException(
                    "Failed to get point feature class from map server object.");
        }
    }
}
