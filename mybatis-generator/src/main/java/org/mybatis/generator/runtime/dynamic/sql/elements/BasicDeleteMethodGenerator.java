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

public class BasicDeleteMethodGenerator extends AbstractMethodGenerator {

    private BasicDeleteMethodGenerator(Builder builder) {
        super(builder);
    }

    @Override
    public MethodAndImports generateMethodAndImports() {
        if (!introspectedTable.getRules().generateDeleteByExample()
                && !introspectedTable.getRules().generateDeleteByPrimaryKey()) {
            return null;
        }

        Set<FullyQualifiedJavaType> imports = new HashSet<FullyQualifiedJavaType>();

        FullyQualifiedJavaType parameterType =
                new FullyQualifiedJavaType("org.mybatis.dynamic.sql.delete.render.DeleteStatementProvider"); //$NON-NLS-1$
        FullyQualifiedJavaType adapter =
                new FullyQualifiedJavaType("org.mybatis.dynamic.sql.util.SqlProviderAdapter"); //$NON-NLS-1$
        FullyQualifiedJavaType annotation =
                new FullyQualifiedJavaType("org.apache.ibatis.annotations.DeleteProvider"); //$NON-NLS-1$

        imports.add(parameterType);
        imports.add(adapter);
        imports.add(annotation);

        Method method = new Method("delete"); //$NON-NLS-1$
        method.setReturnType(FullyQualifiedJavaType.getIntInstance());
        method.addParameter(new Parameter(parameterType, "deleteStatement")); //$NON-NLS-1$
        context.getCommentGenerator().addGeneralMethodAnnotation(method, introspectedTable, imports);
        method.addAnnotation("@DeleteProvider(type=SqlProviderAdapter.class, method=\"delete\")"); //$NON-NLS-1$

        return MethodAndImports.withMethod(method).withImports(imports).build();
    }

    @Override
    public boolean callPlugins(Method method, Interface interfaze) {
        // we don't have a plugin method for this
        return true;
    }

    public static class Builder extends BaseBuilder<Builder, BasicDeleteMethodGenerator> {

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public BasicDeleteMethodGenerator build() {
            return new BasicDeleteMethodGenerator(this);
        }
    }
}
