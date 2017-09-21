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

package com.esri.serverextension.test;

import com.esri.arcgis.geometry.IPoint;
import com.esri.arcgis.geometry.Point;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(ArcObjectsIntegrationTestRunner.class)
public class ArcObjectsTestRunnerIT {

    @Test
    public void runTest() throws IOException {
        IPoint point = new Point();
        point.setX(1.0);
        point.setY(2.0);
    }

}
