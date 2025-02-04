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
package org.mybatis.generator.codegen.mybatis3.javamapper;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.dom.java.CompilationUnit;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.codegen.AbstractJavaClientGenerator;
import org.mybatis.generator.codegen.AbstractXmlGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.AbstractJavaMapperMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.DeleteByPrimaryKeyMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.InsertMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.SelectAllMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.SelectByPrimaryKeyMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.javamapper.elements.UpdateByPrimaryKeyWithoutBLOBsMethodGenerator;
import org.mybatis.generator.codegen.mybatis3.xmlmapper.SimpleXMLMapperGenerator;
import org.mybatis.generator.config.PropertyRegistry;

/**
 * @author Jeff Butler
 *
 */
public class SimpleJavaClientGenerator extends AbstractJavaClientGenerator {

    public SimpleJavaClientGenerator() {
        super(true);
    }

    public SimpleJavaClientGenerator(boolean requiresMatchedXMLGenerator) {
        super(requiresMatchedXMLGenerator);
    }

    @Override
    public List<CompilationUnit> getCompilationUnits() {
        this.progressCallback.startTask(getString("Progress.17", //$NON-NLS-1$
                this.introspectedTable.getFullyQualifiedTable().toString()));
        CommentGenerator commentGenerator = this.context.getCommentGenerator();

        FullyQualifiedJavaType type =
                new FullyQualifiedJavaType(this.introspectedTable.getMyBatis3JavaMapperType());
        Interface interfaze = new Interface(type);
        interfaze.setVisibility(JavaVisibility.PUBLIC);
        commentGenerator.addJavaFileComment(interfaze);

        String rootInterface =
                this.introspectedTable.getTableConfigurationProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        if (!stringHasValue(rootInterface)) {
            rootInterface = this.context.getJavaClientGeneratorConfiguration()
                    .getProperty(PropertyRegistry.ANY_ROOT_INTERFACE);
        }

        if (stringHasValue(rootInterface)) {
            FullyQualifiedJavaType fqjt = new FullyQualifiedJavaType(rootInterface);
            interfaze.addSuperInterface(fqjt);
            interfaze.addImportedType(fqjt);
        }

        this.addDeleteByPrimaryKeyMethod(interfaze);
        this.addInsertMethod(interfaze);
        this.addSelectByPrimaryKeyMethod(interfaze);
        this.addSelectAllMethod(interfaze);
        this.addUpdateByPrimaryKeyMethod(interfaze);

        List<CompilationUnit> answer = new ArrayList<CompilationUnit>();
        if (this.context.getPlugins().clientGenerated(interfaze, null, this.introspectedTable)) {
            answer.add(interfaze);
        }

        List<CompilationUnit> extraCompilationUnits = this.getExtraCompilationUnits();
        if (extraCompilationUnits != null) {
            answer.addAll(extraCompilationUnits);
        }

        return answer;
    }

    protected void addDeleteByPrimaryKeyMethod(Interface interfaze) {
        if (this.introspectedTable.getRules().generateDeleteByPrimaryKey()) {
            AbstractJavaMapperMethodGenerator methodGenerator =
                    new DeleteByPrimaryKeyMethodGenerator(true);
            this.initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addInsertMethod(Interface interfaze) {
        if (this.introspectedTable.getRules().generateInsert()) {
            AbstractJavaMapperMethodGenerator methodGenerator = new InsertMethodGenerator(true);
            this.initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addSelectByPrimaryKeyMethod(Interface interfaze) {
        if (this.introspectedTable.getRules().generateSelectByPrimaryKey()) {
            AbstractJavaMapperMethodGenerator methodGenerator =
                    new SelectByPrimaryKeyMethodGenerator(true);
            this.initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void addSelectAllMethod(Interface interfaze) {
        AbstractJavaMapperMethodGenerator methodGenerator = new SelectAllMethodGenerator();
        this.initializeAndExecuteGenerator(methodGenerator, interfaze);
    }

    protected void addUpdateByPrimaryKeyMethod(Interface interfaze) {
        if (this.introspectedTable.getRules().generateUpdateByPrimaryKeySelective()) {
            AbstractJavaMapperMethodGenerator methodGenerator =
                    new UpdateByPrimaryKeyWithoutBLOBsMethodGenerator();
            this.initializeAndExecuteGenerator(methodGenerator, interfaze);
        }
    }

    protected void initializeAndExecuteGenerator(AbstractJavaMapperMethodGenerator methodGenerator,
                                                 Interface interfaze) {
        methodGenerator.setContext(this.context);
        methodGenerator.setIntrospectedTable(this.introspectedTable);
        methodGenerator.setProgressCallback(this.progressCallback);
        methodGenerator.setWarnings(this.warnings);
        methodGenerator.addInterfaceElements(interfaze);
    }

    public List<CompilationUnit> getExtraCompilationUnits() {
        return null;
    }

    @Override
    public AbstractXmlGenerator getMatchedXMLGenerator() {
        return new SimpleXMLMapperGenerator();
    }
}
