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
package org.mybatis.generator.api.dom.xml;

import java.util.ArrayList;
import java.util.List;

import org.mybatis.generator.api.dom.OutputUtilities;

/**
 * The Class XmlElement.
 *
 * @author Jeff Butler
 */
public class XmlElement extends Element {

    /** The attributes. */
    private List<Attribute> attributes;

    /** The elements. */
    private List<Element> elements;

    /** The name. */
    private String name;

    /**
     * Instantiates a new xml element.
     *
     * @param name the name
     */
    public XmlElement(String name) {
        super();
        this.attributes = new ArrayList<Attribute>();
        this.elements = new ArrayList<Element>();
        this.name = name;
    }

    /**
     * Copy constructor. Not a truly deep copy, but close enough for most purposes.
     *
     * @param original the original
     */
    public XmlElement(XmlElement original) {
        super();
        this.attributes = new ArrayList<Attribute>();
        this.attributes.addAll(original.attributes);
        this.elements = new ArrayList<Element>();
        this.elements.addAll(original.elements);
        this.name = original.name;
    }

    /**
     * Gets the attributes.
     *
     * @return Returns the attributes.
     */
    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    /**
     * Adds the attribute.
     *
     * @param attribute the attribute
     */
    public void addAttribute(Attribute attribute) {
        this.attributes.add(attribute);
    }

    /**
     * Gets the elements.
     *
     * @return Returns the elements.
     */
    public List<Element> getElements() {
        return this.elements;
    }

    /**
     * Adds the element.
     *
     * @param element the element
     */
    public void addElement(Element element) {
        this.elements.add(element);
    }

    /**
     * Adds the element.
     *
     * @param index the index
     * @param element the element
     */
    public void addElement(int index, Element element) {
        this.elements.add(index, element);
    }

    /**
     * Gets the name.
     *
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.mybatis.generator.api.dom.xml.Element#getFormattedContent(int)
     */
    @Override
    public String getFormattedContent(int indentLevel) {
        StringBuilder sb = new StringBuilder();

        OutputUtilities.xmlIndent(sb, indentLevel);
        sb.append('<');
        sb.append(this.name);

        // Collections.sort(attributes);
        for (Attribute att : this.attributes) {
            sb.append(' ');
            sb.append(att.getFormattedContent());
        }

        if (this.elements.size() > 0) {
            sb.append(">"); //$NON-NLS-1$
            for (Element element : this.elements) {
                OutputUtilities.newLine(sb);
                sb.append(element.getFormattedContent(indentLevel + 1));
            }
            OutputUtilities.newLine(sb);
            OutputUtilities.xmlIndent(sb, indentLevel);
            sb.append("</"); //$NON-NLS-1$
            sb.append(this.name);
            sb.append('>');

        } else {
            sb.append(" />"); //$NON-NLS-1$
        }

        return sb.toString();
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
}
