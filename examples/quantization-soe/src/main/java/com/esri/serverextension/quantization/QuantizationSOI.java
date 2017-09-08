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

import com.esri.arcgis.interop.extn.ArcGISExtension;
import com.esri.arcgis.interop.extn.ServerObjectExtProperties;
import com.esri.serverextension.core.server.AbstractRestServerObjectInterceptor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@ArcGISExtension
@ServerObjectExtProperties(displayName = "Quantization SOI",
        description = "Quantization SOI",
        interceptor = true,
        servicetype = "MapService")
public class QuantizationSOI extends AbstractRestServerObjectInterceptor {

    @Override
    protected void doConfigure(
            AnnotationConfigApplicationContext applicationContext) {
        super.doConfigure(applicationContext);

        applicationContext.register(QuantizationSOIConfig.class);
    }

    @Override
    protected void doShutdown() {
        super.doShutdown();
    }
}
