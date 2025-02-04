/**
 * Copyright 2006-2016 the original author or authors.
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
package org.mybatis.generator.codegen.mybatis3.xmlmapper.elements;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.util.List;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.mybatis3.MyBatis3FormattingUtilities;

/**
 *
 * @author Jeff Butler
 *
 */
public class ResultMapWithoutBLOBsElementGenerator extends AbstractXmlElementGenerator {

    private boolean isSimple;

    public ResultMapWithoutBLOBsElementGenerator(boolean isSimple) {
        super();
        this.isSimple = isSimple;
    }

    @Override
    public void addElements(XmlElement parentElement) {
        XmlElement answer = new XmlElement("resultMap"); //$NON-NLS-1$
        answer.addAttribute(new Attribute("id", //$NON-NLS-1$
                this.introspectedTable.getBaseResultMapId()));

        String returnType;
        if (this.isSimple) {
            returnType = this.introspectedTable.getBaseRecordType();
        } else {
            // if (introspectedTable.getRules().generateBaseRecordClass()) {
            // returnType = introspectedTable.getBaseRecordType();
            // } else {
            // returnType = introspectedTable.getPrimaryKeyType();
            // }
            returnType = this.introspectedTable.getBaseRecordType();
        }

        answer.addAttribute(new Attribute("type", //$NON-NLS-1$
                returnType));

        this.context.getCommentGenerator().addComment(answer);

        if (this.introspectedTable.isConstructorBased()) {
            this.addResultMapConstructorElements(answer);
        } else {
            this.addResultMapElements(answer);
        }

        if (this.context.getPlugins().sqlMapResultMapWithoutBLOBsElementGenerated(answer,
                this.introspectedTable)) {
            parentElement.addElement(answer);
        }
    }

    private void addResultMapElements(XmlElement answer) {
        for (IntrospectedColumn introspectedColumn : this.introspectedTable.getPrimaryKeyColumns()) {
            XmlElement resultElement = new XmlElement("id"); //$NON-NLS-1$

            resultElement.addAttribute(new Attribute("column", //$NON-NLS-1$
                    MyBatis3FormattingUtilities.getRenamedColumnNameForResultMap(introspectedColumn)));
            resultElement.addAttribute(new Attribute("property", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
            resultElement.addAttribute(new Attribute("jdbcType", //$NON-NLS-1$
                    introspectedColumn.getJdbcTypeName()));

            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                resultElement
                        .addAttribute(new Attribute("typeHandler", introspectedColumn.getTypeHandler())); //$NON-NLS-1$
            }

            answer.addElement(resultElement);
        }

        List<IntrospectedColumn> columns;
        if (this.isSimple) {
            columns = this.introspectedTable.getNonPrimaryKeyColumns();
        } else {
            columns = this.introspectedTable.getBaseColumns();
        }
        for (IntrospectedColumn introspectedColumn : columns) {
            XmlElement resultElement = new XmlElement("result"); //$NON-NLS-1$

            resultElement.addAttribute(new Attribute("column", //$NON-NLS-1$
                    MyBatis3FormattingUtilities.getRenamedColumnNameForResultMap(introspectedColumn)));
            resultElement.addAttribute(new Attribute("property", introspectedColumn.getJavaProperty())); //$NON-NLS-1$
            resultElement.addAttribute(new Attribute("jdbcType", //$NON-NLS-1$
                    introspectedColumn.getJdbcTypeName()));

            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                resultElement
                        .addAttribute(new Attribute("typeHandler", introspectedColumn.getTypeHandler())); //$NON-NLS-1$
            }

            answer.addElement(resultElement);
        }
    }

    private void addResultMapConstructorElements(XmlElement answer) {
        XmlElement constructor = new XmlElement("constructor"); //$NON-NLS-1$

        for (IntrospectedColumn introspectedColumn : this.introspectedTable.getPrimaryKeyColumns()) {
            XmlElement resultElement = new XmlElement("idArg"); //$NON-NLS-1$

            resultElement.addAttribute(new Attribute("column", //$NON-NLS-1$
                    MyBatis3FormattingUtilities.getRenamedColumnNameForResultMap(introspectedColumn)));
            resultElement.addAttribute(new Attribute("jdbcType", //$NON-NLS-1$
                    introspectedColumn.getJdbcTypeName()));
            resultElement.addAttribute(new Attribute("javaType", //$NON-NLS-1$
                    introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName()));

            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                resultElement
                        .addAttribute(new Attribute("typeHandler", introspectedColumn.getTypeHandler())); //$NON-NLS-1$
            }

            constructor.addElement(resultElement);
        }

        List<IntrospectedColumn> columns;
        if (this.isSimple) {
            columns = this.introspectedTable.getNonPrimaryKeyColumns();
        } else {
            columns = this.introspectedTable.getBaseColumns();
        }
        for (IntrospectedColumn introspectedColumn : columns) {
            XmlElement resultElement = new XmlElement("arg"); //$NON-NLS-1$

            resultElement.addAttribute(new Attribute("column", //$NON-NLS-1$
                    MyBatis3FormattingUtilities.getRenamedColumnNameForResultMap(introspectedColumn)));
            resultElement.addAttribute(new Attribute("jdbcType", //$NON-NLS-1$
                    introspectedColumn.getJdbcTypeName()));
            resultElement.addAttribute(new Attribute("javaType", //$NON-NLS-1$
                    introspectedColumn.getFullyQualifiedJavaType().getFullyQualifiedName()));

            if (stringHasValue(introspectedColumn.getTypeHandler())) {
                resultElement
                        .addAttribute(new Attribute("typeHandler", introspectedColumn.getTypeHandler())); //$NON-NLS-1$
            }

            constructor.addElement(resultElement);
        }

        answer.addElement(constructor);
    }
}
