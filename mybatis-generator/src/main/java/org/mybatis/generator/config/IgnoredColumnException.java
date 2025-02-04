/**
 * Copyright 2006-2017 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mybatis.generator.config;

import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;
import static org.mybatis.generator.internal.util.messages.Messages.getString;

import java.util.List;

import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;

public class IgnoredColumnException extends IgnoredColumn {

    public IgnoredColumnException(String columnName) {
        super(columnName);
    }

    @Override
    public XmlElement toXmlElement() {
        XmlElement xmlElement = new XmlElement("except"); //$NON-NLS-1$
        xmlElement.addAttribute(new Attribute("column", columnName)); //$NON-NLS-1$

        if (stringHasValue(configuredDelimitedColumnName)) {
            xmlElement.addAttribute(new Attribute(
                    "delimitedColumnName", configuredDelimitedColumnName)); //$NON-NLS-1$
        }

        return xmlElement;
    }

    @Override
    public void validate(List<String> errors, String tableName) {
        if (!stringHasValue(columnName)) {
            errors.add(getString("ValidationError.26", //$NON-NLS-1$
                    tableName));
        }
    }
}
