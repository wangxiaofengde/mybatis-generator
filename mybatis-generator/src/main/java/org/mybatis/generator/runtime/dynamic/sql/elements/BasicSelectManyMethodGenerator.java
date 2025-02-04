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
package org.mybatis.generator.runtime.dynamic.sql.elements;

import java.util.HashSet;
import java.util.Set;

import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.Parameter;

public class BasicSelectManyMethodGenerator extends AbstractMethodGenerator {
    private FullyQualifiedJavaType recordType;
    private FragmentGenerator fragmentGenerator;

    private BasicSelectManyMethodGenerator(Builder builder) {
        super(builder);
        recordType = builder.recordType;
        fragmentGenerator = builder.fragmentGenerator;
    }

    @Override
    public MethodAndImports generateMethodAndImports() {
        if (!introspectedTable.getRules().generateSelectByExampleWithBLOBs()
                && !introspectedTable.getRules().generateSelectByExampleWithoutBLOBs()) {
            return null;
        }

        Set<FullyQualifiedJavaType> imports = new HashSet<FullyQualifiedJavaType>();

        FullyQualifiedJavaType parameterType =
                new FullyQualifiedJavaType("org.mybatis.dynamic.sql.select.render.SelectStatementProvider"); //$NON-NLS-1$
        FullyQualifiedJavaType adapter =
                new FullyQualifiedJavaType("org.mybatis.dynamic.sql.util.SqlProviderAdapter"); //$NON-NLS-1$
        FullyQualifiedJavaType annotation =
                new FullyQualifiedJavaType("org.apache.ibatis.annotations.SelectProvider"); //$NON-NLS-1$

        imports.add(parameterType);
        imports.add(adapter);
        imports.add(annotation);

        Method method = new Method("selectMany"); //$NON-NLS-1$

        imports.add(FullyQualifiedJavaType.getNewListInstance());

        imports.add(recordType);
        FullyQualifiedJavaType returnType = FullyQualifiedJavaType.getNewListInstance();
        returnType.addTypeArgument(recordType);
        method.setReturnType(returnType);
        method.addParameter(new Parameter(parameterType, "selectStatement")); //$NON-NLS-1$
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, imports);
        method.addAnnotation("@SelectProvider(type=SqlProviderAdapter.class, method=\"select\")"); //$NON-NLS-1$

        MethodAndImports.Builder builder = MethodAndImports.withMethod(method).withImports(imports);

        MethodParts methodParts;
        if (introspectedTable.isConstructorBased()) {
            methodParts = fragmentGenerator.getAnnotatedConstructorArgs();
        } else {
            methodParts = fragmentGenerator.getAnnotatedResults();
        }
        acceptParts(builder, method, methodParts);

        return builder.build();
    }

    @Override
    public boolean callPlugins(Method method, Interface interfaze) {
        // we don't have a plugin method for this
        return true;
    }

    public static class Builder extends BaseBuilder<Builder, BasicSelectManyMethodGenerator> {
        private FullyQualifiedJavaType recordType;
        private FragmentGenerator fragmentGenerator;

        public Builder withRecordType(FullyQualifiedJavaType recordType) {
            this.recordType = recordType;
            return this;
        }

        public Builder withFragmentGenerator(FragmentGenerator fragmentGenerator) {
            this.fragmentGenerator = fragmentGenerator;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BasicSelectManyMethodGenerator build() {
            return new BasicSelectManyMethodGenerator(this);
        }
    }
}
