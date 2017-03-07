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

package com.esri.serverextension.core.server.internal;

import com.esri.serverextension.core.server.RestDelegate;
import com.esri.serverextension.core.server.RestRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.MethodParameter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriTemplate;

import java.lang.reflect.Method;
import java.util.Map;

public final class PathVariableArgumentResolver implements ArgumentResolver {

    public PathVariableArgumentResolver() {
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  RestRequest request, RestDelegate handler) throws Exception {
        Method method = parameter.getMethod();
        RequestMapping requestMapping = method
                .getAnnotation(RequestMapping.class);
        String[] templates = requestMapping.value();

        Class<?> type = parameter.getParameterType();
        PathVariable annotation = parameter
                .getParameterAnnotation(PathVariable.class);
        String variable = annotation.value();

        String path = new UriPath(request).getPath();
        Object value = null;
        for (String template : templates) {
            UriTemplate uriTemplate = new UriTemplate(template);
            if (uriTemplate.matches(path)) {
                Map<String, String> uriTemplateVars = uriTemplate.match(path);
                if (uriTemplateVars != null) {
                    if (StringUtils.isEmpty(variable)) {
                        // PathVariable does not contain name
                        // get first value in list
                        if (!CollectionUtils.isEmpty(uriTemplateVars)) {
                            value = uriTemplateVars.values().iterator().next();
                        }
                    } else {
                        value = uriTemplateVars.get(variable);
                    }
                    break;
                }
            }
        }

        if (value == null) {
            return value;
        }

        SimpleTypeConverter converter = new SimpleTypeConverter();
        return converter.convertIfNecessary(value, type, parameter);
    }

    @Override
    public boolean resolves(MethodParameter parameter) {
        return parameter.getMethodAnnotation(RequestMapping.class) != null
                && parameter.getParameterAnnotation(PathVariable.class) != null;
    }

}
