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

import com.esri.arcgis.geodatabase.*;
import com.esri.arcgis.geometry.IEnvelope;
import com.esri.serverextension.core.geodatabase.FileGDBWorkspaceFactoryBean;
import com.esri.serverextension.core.geodatabase.GeodatabaseTemplate;
import com.esri.serverextension.core.rest.json.JSONGeometryMapper;
import com.esri.serverextension.core.util.ArcObjectsInitializer;
import com.esri.serverextension.core.util.ArcObjectsUtilities;
import com.esri.serverextension.core.util.StopWatch;
import com.esri.serverextension.test.AbstractArcObjectsIT;
import com.esri.serverextension.test.ArcObjectsSpringIntegrationTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;

@RunWith(ArcObjectsSpringIntegrationTestRunner.class)
@ContextConfiguration(locations = {"/spring/config/applicationContext-file-gdb-workspace-test.xml"})
public class ClusterAssemblerIT {

    private final Logger logger = LoggerFactory.getLogger(ClusterAssemblerIT.class);

    @Inject
    private IWorkspace workspace;

    @Test
    public void testAssembleClusters() throws IOException {
        assembleClusters("Issue_Date >= date '2017-01-01 00:00:00'");
    }

    private void assembleClusters(String where) throws IOException {
        logger.info("Step 1: Setting up query filter.");
        IFeatureClass featureClass = ((IFeatureWorkspace)workspace).openFeatureClass("Permit_Features");
        SpatialFilter spatialFilter = new SpatialFilter();
        spatialFilter.setSubFields("Valuation,Shape");
        spatialFilter.setGeometryField("Shape");
        spatialFilter.setSpatialRel(esriSpatialRelEnum.esriSpatialRelIntersects);
        JSONGeometryMapper geometryMapper = new JSONGeometryMapper();
        IEnvelope envelope = geometryMapper.readEnvelope("{\"xmin\":-13244092.36900171," +
                "\"ymin\":4000883.3498998554," +
                "\"xmax\":-13118812.079642477," +
                "\"ymax\":4061574.350358204," +
                "\"spatialReference\":{\"wkid\":102100}}");
        spatialFilter.setGeometryByRef(envelope);
        spatialFilter.setWhereClause(where);
        spatialFilter.setOutputSpatialReferenceByRef("Shape", ArcObjectsUtilities.createSpatialReference(102100));

        logger.info("Step 2: Configuring cluster assembler.");
        ClusterExtent clusterExtent = new ClusterExtent(-13244092.36900171,
                4000883.3498998554,
                -13118812.079642477,
                4061574.350358204);
        ClusterAssembler clusterAssembler = new ClusterAssembler(
                76.43702828507277,
                100,
                clusterExtent);
        ClusterAssemblerCallbackHandler clusterAssemblerCallbackHandler = new ClusterAssemblerCallbackHandler(
                clusterAssembler,"Valuation");

        logger.info("Step 3: Executing query.");
        GeodatabaseTemplate geodatabaseTemplate = new GeodatabaseTemplate();
        geodatabaseTemplate.query(featureClass, spatialFilter, clusterAssemblerCallbackHandler);
        logger.info(String.format("# of input features: %1$d", clusterAssembler.getNumberOfFeatures()));

        logger.info("Step 4: Building clusters.");
        clusterAssembler.fixClusters();
        List<Cluster> clusters = clusterAssembler.getClusters();
        int clusterCount = 0;
        for (Cluster cluster : clusters) {
            logger.info(String.format("Cluster %1$d: (x: %2$f y: %3$f), %4$f", ++clusterCount,
                    cluster.getPoint().x, cluster.getPoint().y, cluster.getValue()));
        }
    }
}
