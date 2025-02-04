/**
 * Copyright 2006-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.mybatis.generator.runtime.dynamic.sql;

import java.util.List;

import org.mybatis.generator.api.ProgressCallback;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractJavaGenerator;
import org.mybatis.generator.codegen.mybatis3.IntrospectedTableMyBatis3Impl;

/**
 *
 * @author Jeff Butler
 *
 */
public class IntrospectedTableMyBatis3DynamicSqlImpl extends IntrospectedTableMyBatis3Impl {
    public IntrospectedTableMyBatis3DynamicSqlImpl() {
        super();
    }

    @Override
    protected void calculateXmlMapperGenerator(AbstractJavaClientGenerator javaClientGenerator,
                                               List<String> warnings, ProgressCallback progressCallback) {
        // no XML with dynamic SQL support
        this.xmlMapperGenerator = null;
    }

    @Override
    protected AbstractJavaClientGenerator createJavaClientGenerator() {
        return new DynamicSqlMapperGenerator();
    }

    @Override
    protected void calculateJavaModelGenerators(List<String> warnings,
                                                ProgressCallback progressCallback) {

        AbstractJavaGenerator javaGenerator = new DynamicSqlModelGenerator();
        this.initializeAbstractGenerator(javaGenerator, warnings, progressCallback);
        this.javaModelGenerators.add(javaGenerator);
    }

    @Override
    public boolean requiresXMLGenerator() {
        return false;
    }
}
