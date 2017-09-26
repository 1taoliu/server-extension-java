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

import com.esri.serverextension.core.util.ArcObjectsInitializer;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

public class ArcObjectsIntegrationTestRunner extends BlockJUnit4ClassRunner {

    private final Logger logger = LoggerFactory.getLogger(ArcObjectsIntegrationTestRunner.class);

    public ArcObjectsIntegrationTestRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    protected Statement classBlock(final RunNotifier notifier) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                ArcObjectsInitializer.getInstance().init();
                try {
                    ArcObjectsIntegrationTestRunner.super.classBlock(notifier).evaluate();
                } finally {
                    ArcObjectsInitializer.getInstance().shutdown();
                }
            }
        };
    }
}
