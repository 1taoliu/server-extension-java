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

package com.esri.serverextension.core.geodatabase;

import com.esri.arcgis.geodatabase.ICursor;
import com.esri.arcgis.geodatabase.IFeatureCursor;

import java.io.IOException;

public interface GeodatabaseCursorExtractor<T> {

	T extractData(ICursor cursor, GeodatabaseFieldMap fieldMap) throws IOException;

	T extractData(IFeatureCursor featureCursor, GeodatabaseFieldMap fieldMap) throws IOException;
}
