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
package org.mybatis.generator.api;

import static org.mybatis.generator.internal.util.StringUtility.isTrue;
import static org.mybatis.generator.internal.util.StringUtility.stringHasValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.GeneratedKey;
import org.mybatis.generator.config.JavaClientGeneratorConfiguration;
import org.mybatis.generator.config.JavaModelGeneratorConfiguration;
import org.mybatis.generator.config.ModelType;
import org.mybatis.generator.config.PropertyHolder;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.config.SqlMapGeneratorConfiguration;
import org.mybatis.generator.config.TableConfiguration;
import org.mybatis.generator.internal.rules.ConditionalModelRules;
import org.mybatis.generator.internal.rules.FlatModelRules;
import org.mybatis.generator.internal.rules.HierarchicalModelRules;
import org.mybatis.generator.internal.rules.Rules;

/**
 * Base class for all code generator implementations. This class provides many of the housekeeping
 * methods needed to implement a code generator, with only the actual code generation methods left
 * unimplemented.
 *
 * @author Jeff Butler
 *
 */
public abstract class IntrospectedTable {

    public enum TargetRuntime {
        IBATIS2, MYBATIS3
    }

    protected enum InternalAttribute {
        ATTR_DAO_IMPLEMENTATION_TYPE, ATTR_DAO_INTERFACE_TYPE, ATTR_PRIMARY_KEY_TYPE, ATTR_BASE_RECORD_TYPE, ATTR_RECORD_WITH_BLOBS_TYPE, ATTR_EXAMPLE_TYPE, ATTR_IBATIS2_SQL_MAP_PACKAGE, ATTR_IBATIS2_SQL_MAP_FILE_NAME, ATTR_IBATIS2_SQL_MAP_NAMESPACE, ATTR_MYBATIS3_XML_MAPPER_PACKAGE, ATTR_MYBATIS3_XML_MAPPER_FILE_NAME,
        /** also used as XML Mapper namespace if a Java mapper is generated. */
        ATTR_MYBATIS3_JAVA_MAPPER_TYPE, ATTR_MYBATIS3_JAVA_SERVICE_TYPE, ATTR_MYBATIS3_JAVA_SERVICE_IMPL_TYPE,
        /** used as XML Mapper namespace if no client is generated. */
        ATTR_MYBATIS3_FALLBACK_SQL_MAP_NAMESPACE, ATTR_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME, ATTR_ALIASED_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME, ATTR_COUNT_BY_EXAMPLE_STATEMENT_ID, ATTR_DELETE_BY_EXAMPLE_STATEMENT_ID, ATTR_DELETE_BY_PRIMARY_KEY_STATEMENT_ID, ATTR_INSERT_STATEMENT_ID, ATTR_INSERT_SELECTIVE_STATEMENT_ID, ATTR_SELECT_ALL_STATEMENT_ID, ATTR_SELECT_BY_EXAMPLE_STATEMENT_ID, ATTR_SELECT_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID, ATTR_SELECT_BY_PRIMARY_KEY_STATEMENT_ID, ATTR_UPDATE_BY_EXAMPLE_STATEMENT_ID, ATTR_UPDATE_BY_EXAMPLE_SELECTIVE_STATEMENT_ID, ATTR_UPDATE_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID, ATTR_UPDATE_BY_PRIMARY_KEY_STATEMENT_ID, ATTR_UPDATE_BY_PRIMARY_KEY_SELECTIVE_STATEMENT_ID, ATTR_UPDATE_BY_PRIMARY_KEY_WITH_BLOBS_STATEMENT_ID, ATTR_BASE_RESULT_MAP_ID, ATTR_RESULT_MAP_WITH_BLOBS_ID, ATTR_EXAMPLE_WHERE_CLAUSE_ID, ATTR_BASE_COLUMN_LIST_ID, ATTR_BLOB_COLUMN_LIST_ID, ATTR_MYBATIS3_UPDATE_BY_EXAMPLE_WHERE_CLAUSE_ID, ATTR_MYBATIS3_SQL_PROVIDER_TYPE
    }

    protected TableConfiguration tableConfiguration;

    protected FullyQualifiedTable fullyQualifiedTable;

    protected Context context;

    protected Rules rules;

    protected List<IntrospectedColumn> primaryKeyColumns;

    protected List<IntrospectedColumn> baseColumns;

    protected List<IntrospectedColumn> blobColumns;

    protected TargetRuntime targetRuntime;

    /**
     * Attributes may be used by plugins to capture table related state between the different plugin
     * calls.
     */
    protected Map<String, Object> attributes;

    /** Internal attributes are used to store commonly accessed items by all code generators. */
    protected Map<IntrospectedTable.InternalAttribute, String> internalAttributes;

    /**
     * Table remarks retrieved from database metadata.
     */
    protected String remarks;

    /**
     * Table type retrieved from database metadata.
     */
    protected String tableType;

    public IntrospectedTable(TargetRuntime targetRuntime) {
        super();
        this.targetRuntime = targetRuntime;
        this.primaryKeyColumns = new ArrayList<IntrospectedColumn>();
        this.baseColumns = new ArrayList<IntrospectedColumn>();
        this.blobColumns = new ArrayList<IntrospectedColumn>();
        this.attributes = new HashMap<String, Object>();
        this.internalAttributes = new HashMap<IntrospectedTable.InternalAttribute, String>();
    }

    public FullyQualifiedTable getFullyQualifiedTable() {
        return this.fullyQualifiedTable;
    }

    public String getSelectByExampleQueryId() {
        return this.tableConfiguration.getSelectByExampleQueryId();
    }

    public String getSelectByPrimaryKeyQueryId() {
        return this.tableConfiguration.getSelectByPrimaryKeyQueryId();
    }

    public GeneratedKey getGeneratedKey() {
        return this.tableConfiguration.getGeneratedKey();
    }

    public IntrospectedColumn getColumn(String columnName) {
        if (columnName == null) {
            return null;
        } else {
            // search primary key columns
            for (IntrospectedColumn introspectedColumn : this.primaryKeyColumns) {
                if (introspectedColumn.isColumnNameDelimited()) {
                    if (introspectedColumn.getActualColumnName().equals(columnName)) {
                        return introspectedColumn;
                    }
                } else {
                    if (introspectedColumn.getActualColumnName().equalsIgnoreCase(columnName)) {
                        return introspectedColumn;
                    }
                }
            }

            // search base columns
            for (IntrospectedColumn introspectedColumn : this.baseColumns) {
                if (introspectedColumn.isColumnNameDelimited()) {
                    if (introspectedColumn.getActualColumnName().equals(columnName)) {
                        return introspectedColumn;
                    }
                } else {
                    if (introspectedColumn.getActualColumnName().equalsIgnoreCase(columnName)) {
                        return introspectedColumn;
                    }
                }
            }

            // search blob columns
            for (IntrospectedColumn introspectedColumn : this.blobColumns) {
                if (introspectedColumn.isColumnNameDelimited()) {
                    if (introspectedColumn.getActualColumnName().equals(columnName)) {
                        return introspectedColumn;
                    }
                } else {
                    if (introspectedColumn.getActualColumnName().equalsIgnoreCase(columnName)) {
                        return introspectedColumn;
                    }
                }
            }

            return null;
        }
    }

    /**
     * Returns true if any of the columns in the table are JDBC Dates (as opposed to timestamps).
     *
     * @return true if the table contains DATE columns
     */
    public boolean hasJDBCDateColumns() {
        boolean rc = false;

        for (IntrospectedColumn introspectedColumn : this.primaryKeyColumns) {
            if (introspectedColumn.isJDBCDateColumn()) {
                rc = true;
                break;
            }
        }

        if (!rc) {
            for (IntrospectedColumn introspectedColumn : this.baseColumns) {
                if (introspectedColumn.isJDBCDateColumn()) {
                    rc = true;
                    break;
                }
            }
        }

        return rc;
    }

    /**
     * Returns true if any of the columns in the table are JDBC Times (as opposed to timestamps).
     *
     * @return true if the table contains TIME columns
     */
    public boolean hasJDBCTimeColumns() {
        boolean rc = false;

        for (IntrospectedColumn introspectedColumn : this.primaryKeyColumns) {
            if (introspectedColumn.isJDBCTimeColumn()) {
                rc = true;
                break;
            }
        }

        if (!rc) {
            for (IntrospectedColumn introspectedColumn : this.baseColumns) {
                if (introspectedColumn.isJDBCTimeColumn()) {
                    rc = true;
                    break;
                }
            }
        }

        return rc;
    }

    /**
     * Returns the columns in the primary key. If the generatePrimaryKeyClass() method returns false,
     * then these columns will be iterated as the parameters of the selectByPrimaryKay and
     * deleteByPrimaryKey methods
     *
     * @return a List of ColumnDefinition objects for columns in the primary key
     */
    public List<IntrospectedColumn> getPrimaryKeyColumns() {
        return this.primaryKeyColumns;
    }

    public boolean hasPrimaryKeyColumns() {
        return this.primaryKeyColumns.size() > 0;
    }

    public List<IntrospectedColumn> getBaseColumns() {
        return this.baseColumns;
    }

    /**
     * Returns all columns in the table (for use by the select by primary key and select by example
     * with BLOBs methods).
     *
     * @return a List of ColumnDefinition objects for all columns in the table
     */
    public List<IntrospectedColumn> getAllColumns() {
        List<IntrospectedColumn> answer = new ArrayList<IntrospectedColumn>();
        answer.addAll(this.primaryKeyColumns);
        answer.addAll(this.baseColumns);
        answer.addAll(this.blobColumns);

        return answer;
    }

    /**
     * Returns all columns except BLOBs (for use by the select by example without BLOBs method).
     *
     * @return a List of ColumnDefinition objects for columns in the table that are non BLOBs
     */
    public List<IntrospectedColumn> getNonBLOBColumns() {
        List<IntrospectedColumn> answer = new ArrayList<IntrospectedColumn>();
        answer.addAll(this.primaryKeyColumns);
        answer.addAll(this.baseColumns);

        return answer;
    }

    public int getNonBLOBColumnCount() {
        return this.primaryKeyColumns.size() + this.baseColumns.size();
    }

    public List<IntrospectedColumn> getNonPrimaryKeyColumns() {
        List<IntrospectedColumn> answer = new ArrayList<IntrospectedColumn>();
        answer.addAll(this.baseColumns);
        answer.addAll(this.blobColumns);

        return answer;
    }

    public List<IntrospectedColumn> getBLOBColumns() {
        return this.blobColumns;
    }

    public boolean hasBLOBColumns() {
        return this.blobColumns.size() > 0;
    }

    public boolean hasBaseColumns() {
        return this.baseColumns.size() > 0;
    }

    public Rules getRules() {
        return this.rules;
    }

    public String getTableConfigurationProperty(String property) {
        return this.tableConfiguration.getProperty(property);
    }

    public String getPrimaryKeyType() {
        return this.internalAttributes.get(InternalAttribute.ATTR_PRIMARY_KEY_TYPE);
    }

    /**
     * Gets the base record type.
     *
     * @return the type for the record (the class that holds non-primary key and non-BLOB fields).
     *         Note that the value will be calculated regardless of whether the table has these
     *         columns or not.
     */
    public String getBaseRecordType() {
        return this.internalAttributes.get(InternalAttribute.ATTR_BASE_RECORD_TYPE);
    }

    /**
     * Gets the example type.
     *
     * @return the type for the example class.
     */
    public String getExampleType() {
        return this.internalAttributes.get(InternalAttribute.ATTR_EXAMPLE_TYPE);
    }

    /**
     * Gets the record with blo bs type.
     *
     * @return the type for the record with BLOBs class. Note that the value will be calculated
     *         regardless of whether the table has BLOB columns or not.
     */
    public String getRecordWithBLOBsType() {
        return this.internalAttributes.get(InternalAttribute.ATTR_RECORD_WITH_BLOBS_TYPE);
    }

    /**
     * Calculates an SQL Map file name for the table. Typically the name is "XXXX_SqlMap.xml" where
     * XXXX is the fully qualified table name (delimited with underscores).
     *
     * @return the name of the SqlMap file
     */
    public String getIbatis2SqlMapFileName() {
        return this.internalAttributes.get(InternalAttribute.ATTR_IBATIS2_SQL_MAP_FILE_NAME);
    }

    public String getIbatis2SqlMapNamespace() {
        return this.internalAttributes.get(InternalAttribute.ATTR_IBATIS2_SQL_MAP_NAMESPACE);
    }

    public String getMyBatis3SqlMapNamespace() {
        String namespace = this.getMyBatis3JavaMapperType();
        if (namespace == null) {
            namespace = this.getMyBatis3FallbackSqlMapNamespace();
        }

        return namespace;
    }

    public String getMyBatis3FallbackSqlMapNamespace() {
        return this.internalAttributes.get(InternalAttribute.ATTR_MYBATIS3_FALLBACK_SQL_MAP_NAMESPACE);
    }

    /**
     * Calculates the package for the current table.
     *
     * @return the package for the SqlMap for the current table
     */
    public String getIbatis2SqlMapPackage() {
        return this.internalAttributes.get(InternalAttribute.ATTR_IBATIS2_SQL_MAP_PACKAGE);
    }

    public String getDAOImplementationType() {
        return this.internalAttributes.get(InternalAttribute.ATTR_DAO_IMPLEMENTATION_TYPE);
    }

    public String getDAOInterfaceType() {
        return this.internalAttributes.get(InternalAttribute.ATTR_DAO_INTERFACE_TYPE);
    }

    public boolean hasAnyColumns() {
        return this.primaryKeyColumns.size() > 0 || this.baseColumns.size() > 0
                || this.blobColumns.size() > 0;
    }

    public void setTableConfiguration(TableConfiguration tableConfiguration) {
        this.tableConfiguration = tableConfiguration;
    }

    public void setFullyQualifiedTable(FullyQualifiedTable fullyQualifiedTable) {
        this.fullyQualifiedTable = fullyQualifiedTable;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void addColumn(IntrospectedColumn introspectedColumn) {
        if (introspectedColumn.isBLOBColumn()) {
            // this.blobColumns.add(introspectedColumn);
            // 不特殊处理blobColumns
            this.baseColumns.add(introspectedColumn);
        } else {
            this.baseColumns.add(introspectedColumn);
        }

        introspectedColumn.setIntrospectedTable(this);
    }

    public void addPrimaryKeyColumn(String columnName) {
        boolean found = false;
        // first search base columns
        Iterator<IntrospectedColumn> iter = this.baseColumns.iterator();
        while (iter.hasNext()) {
            IntrospectedColumn introspectedColumn = iter.next();
            if (introspectedColumn.getActualColumnName().equals(columnName)) {
                this.primaryKeyColumns.add(introspectedColumn);
                iter.remove();
                found = true;
                break;
            }
        }

        // search blob columns in the weird event that a blob is the primary key
        if (!found) {
            iter = this.blobColumns.iterator();
            while (iter.hasNext()) {
                IntrospectedColumn introspectedColumn = iter.next();
                if (introspectedColumn.getActualColumnName().equals(columnName)) {
                    this.primaryKeyColumns.add(introspectedColumn);
                    iter.remove();
                    found = true;
                    break;
                }
            }
        }
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public void initialize() {
        this.calculateJavaClientAttributes();
        this.calculateModelAttributes();
        this.calculateXmlAttributes();

        if (this.tableConfiguration.getModelType() == ModelType.HIERARCHICAL) {
            this.rules = new HierarchicalModelRules(this);
        } else if (this.tableConfiguration.getModelType() == ModelType.FLAT) {
            this.rules = new FlatModelRules(this);
        } else {
            this.rules = new ConditionalModelRules(this);
        }

        this.context.getPlugins().initialized(this);
    }

    protected void calculateXmlAttributes() {
        this.setIbatis2SqlMapPackage(this.calculateSqlMapPackage());
        this.setIbatis2SqlMapFileName(this.calculateIbatis2SqlMapFileName());
        this.setMyBatis3XmlMapperFileName(this.calculateMyBatis3XmlMapperFileName());
        this.setMyBatis3XmlMapperPackage(this.calculateSqlMapPackage());

        this.setIbatis2SqlMapNamespace(this.calculateIbatis2SqlMapNamespace());
        this.setMyBatis3FallbackSqlMapNamespace(this.calculateMyBatis3FallbackSqlMapNamespace());

        this.setSqlMapFullyQualifiedRuntimeTableName(
                this.calculateSqlMapFullyQualifiedRuntimeTableName());
        this.setSqlMapAliasedFullyQualifiedRuntimeTableName(
                this.calculateSqlMapAliasedFullyQualifiedRuntimeTableName());

        this.setCountByExampleStatementId("countByExample"); //$NON-NLS-1$
        this.setDeleteByExampleStatementId("deleteByExample"); //$NON-NLS-1$
        this.setDeleteByPrimaryKeyStatementId("deleteByPrimaryKey"); //$NON-NLS-1$
        this.setInsertStatementId("insert"); //$NON-NLS-1$
        this.setInsertSelectiveStatementId("insertSelective"); //$NON-NLS-1$
        this.setSelectAllStatementId("selectAll"); //$NON-NLS-1$
        this.setSelectByExampleStatementId("selectByExample"); //$NON-NLS-1$
        this.setSelectByExampleWithBLOBsStatementId("selectByExampleWithBLOBs"); //$NON-NLS-1$
        this.setSelectByPrimaryKeyStatementId("selectByPrimaryKey"); //$NON-NLS-1$
        this.setUpdateByExampleStatementId("updateByExample"); //$NON-NLS-1$
        this.setUpdateByExampleSelectiveStatementId("updateByExampleSelective"); //$NON-NLS-1$
        this.setUpdateByExampleWithBLOBsStatementId("updateByExampleWithBLOBs"); //$NON-NLS-1$
        this.setUpdateByPrimaryKeyStatementId("updateByPrimaryKey"); //$NON-NLS-1$
        this.setUpdateByPrimaryKeySelectiveStatementId("updateByPrimaryKeySelective"); //$NON-NLS-1$
        this.setUpdateByPrimaryKeyWithBLOBsStatementId("updateByPrimaryKeyWithBLOBs"); //$NON-NLS-1$
        // this.setBaseResultMapId("BaseResultMap");
        String baseResultMapId = this.fullyQualifiedTable.getDomainObjectName() + "Map";
        this.setBaseResultMapId(baseResultMapId);
        this.setResultMapWithBLOBsId("ResultMapWithBLOBs"); //$NON-NLS-1$
        this.setExampleWhereClauseId("Example_Where_Clause"); //$NON-NLS-1$
        this.setBaseColumnListId("Base_Column_List"); //$NON-NLS-1$
        this.setBlobColumnListId("Blob_Column_List"); //$NON-NLS-1$
        this.setMyBatis3UpdateByExampleWhereClauseId("Update_By_Example_Where_Clause"); //$NON-NLS-1$
    }

    public void setBlobColumnListId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_BLOB_COLUMN_LIST_ID, s);
    }

    public void setBaseColumnListId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_BASE_COLUMN_LIST_ID, s);
    }

    public void setExampleWhereClauseId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_EXAMPLE_WHERE_CLAUSE_ID, s);
    }

    public void setMyBatis3UpdateByExampleWhereClauseId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_MYBATIS3_UPDATE_BY_EXAMPLE_WHERE_CLAUSE_ID,
                s);
    }

    public void setResultMapWithBLOBsId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_RESULT_MAP_WITH_BLOBS_ID, s);
    }

    public void setBaseResultMapId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_BASE_RESULT_MAP_ID, s);
    }

    public void setUpdateByPrimaryKeyWithBLOBsStatementId(String s) {
        this.internalAttributes
                .put(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_WITH_BLOBS_STATEMENT_ID, s);
    }

    public void setUpdateByPrimaryKeySelectiveStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_SELECTIVE_STATEMENT_ID,
                s);
    }

    public void setUpdateByPrimaryKeyStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_STATEMENT_ID, s);
    }

    public void setUpdateByExampleWithBLOBsStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID,
                s);
    }

    public void setUpdateByExampleSelectiveStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_SELECTIVE_STATEMENT_ID, s);
    }

    public void setUpdateByExampleStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_STATEMENT_ID, s);
    }

    public void setSelectByPrimaryKeyStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_SELECT_BY_PRIMARY_KEY_STATEMENT_ID, s);
    }

    public void setSelectByExampleWithBLOBsStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID,
                s);
    }

    public void setSelectAllStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_SELECT_ALL_STATEMENT_ID, s);
    }

    public void setSelectByExampleStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_STATEMENT_ID, s);
    }

    public void setInsertSelectiveStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_INSERT_SELECTIVE_STATEMENT_ID, s);
    }

    public void setInsertStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_INSERT_STATEMENT_ID, s);
    }

    public void setDeleteByPrimaryKeyStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_DELETE_BY_PRIMARY_KEY_STATEMENT_ID, s);
    }

    public void setDeleteByExampleStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_DELETE_BY_EXAMPLE_STATEMENT_ID, s);
    }

    public void setCountByExampleStatementId(String s) {
        this.internalAttributes.put(InternalAttribute.ATTR_COUNT_BY_EXAMPLE_STATEMENT_ID, s);
    }

    public String getBlobColumnListId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_BLOB_COLUMN_LIST_ID);
    }

    public String getBaseColumnListId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_BASE_COLUMN_LIST_ID);
    }

    public String getExampleWhereClauseId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_EXAMPLE_WHERE_CLAUSE_ID);
    }

    public String getMyBatis3UpdateByExampleWhereClauseId() {
        return this.internalAttributes
                .get(InternalAttribute.ATTR_MYBATIS3_UPDATE_BY_EXAMPLE_WHERE_CLAUSE_ID);
    }

    public String getResultMapWithBLOBsId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_RESULT_MAP_WITH_BLOBS_ID);
    }

    public String getBaseResultMapId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_BASE_RESULT_MAP_ID);
    }

    public String getUpdateByPrimaryKeyWithBLOBsStatementId() {
        return this.internalAttributes
                .get(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_WITH_BLOBS_STATEMENT_ID);
    }

    public String getUpdateByPrimaryKeySelectiveStatementId() {
        return this.internalAttributes
                .get(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_SELECTIVE_STATEMENT_ID);
    }

    public String getUpdateByPrimaryKeyStatementId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_UPDATE_BY_PRIMARY_KEY_STATEMENT_ID);
    }

    public String getUpdateByExampleWithBLOBsStatementId() {
        return this.internalAttributes
                .get(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID);
    }

    public String getUpdateByExampleSelectiveStatementId() {
        return this.internalAttributes
                .get(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_SELECTIVE_STATEMENT_ID);
    }

    public String getUpdateByExampleStatementId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_UPDATE_BY_EXAMPLE_STATEMENT_ID);
    }

    public String getSelectByPrimaryKeyStatementId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_SELECT_BY_PRIMARY_KEY_STATEMENT_ID);
    }

    public String getSelectByExampleWithBLOBsStatementId() {
        return this.internalAttributes
                .get(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_WITH_BLOBS_STATEMENT_ID);
    }

    public String getSelectAllStatementId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_SELECT_ALL_STATEMENT_ID);
    }

    public String getSelectByExampleStatementId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_SELECT_BY_EXAMPLE_STATEMENT_ID);
    }

    public String getInsertSelectiveStatementId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_INSERT_SELECTIVE_STATEMENT_ID);
    }

    public String getInsertStatementId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_INSERT_STATEMENT_ID);
    }

    public String getDeleteByPrimaryKeyStatementId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_DELETE_BY_PRIMARY_KEY_STATEMENT_ID);
    }

    public String getDeleteByExampleStatementId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_DELETE_BY_EXAMPLE_STATEMENT_ID);
    }

    public String getCountByExampleStatementId() {
        return this.internalAttributes.get(InternalAttribute.ATTR_COUNT_BY_EXAMPLE_STATEMENT_ID);
    }

    protected String calculateJavaClientImplementationPackage() {
        JavaClientGeneratorConfiguration config = this.context.getJavaClientGeneratorConfiguration();
        if (config == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (stringHasValue(config.getImplementationPackage())) {
            sb.append(config.getImplementationPackage());
        } else {
            sb.append(config.getTargetPackage());
        }

        sb.append(
                this.fullyQualifiedTable.getSubPackageForClientOrSqlMap(this.isSubPackagesEnabled(config)));

        return sb.toString();
    }

    private boolean isSubPackagesEnabled(PropertyHolder propertyHolder) {
        return isTrue(propertyHolder.getProperty(PropertyRegistry.ANY_ENABLE_SUB_PACKAGES));
    }

    protected String calculateJavaClientInterfacePackage() {
        JavaClientGeneratorConfiguration config = this.context.getJavaClientGeneratorConfiguration();
        if (config == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(config.getTargetPackage());

        sb.append(
                this.fullyQualifiedTable.getSubPackageForClientOrSqlMap(this.isSubPackagesEnabled(config)));

        return sb.toString();
    }

    protected void calculateJavaClientAttributes() {
        if (this.context.getJavaClientGeneratorConfiguration() == null) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(this.calculateJavaClientImplementationPackage());
        sb.append('.');
        sb.append(this.fullyQualifiedTable.getDomainObjectName());
        String sharding = this.context.getProperty("sharding");
        if (Boolean.valueOf(sharding)) {
            sb.append("Sharding");
        }
        sb.append("DAOImpl"); //$NON-NLS-1$
        this.setDAOImplementationType(sb.toString());

        sb.setLength(0);
        sb.append(this.calculateJavaClientInterfacePackage());
        sb.append('.');
        sb.append(this.fullyQualifiedTable.getDomainObjectName());
        if (Boolean.valueOf(sharding)) {
            sb.append("Sharding");
        }
        sb.append("DAO"); //$NON-NLS-1$
        this.setDAOInterfaceType(sb.toString());

        String packageName = this.calculateJavaClientInterfacePackage();

        JavaClientGeneratorConfiguration javaClientGeneratorConfiguration =
                this.context.getJavaClientGeneratorConfiguration();
        String targetRepositoryPackage =
                javaClientGeneratorConfiguration.getProperty("targetRepositoryPackage");
        if (targetRepositoryPackage == null) {
            targetRepositoryPackage = packageName;
        }

        String prefix = this.fullyQualifiedTable.getDomainObjectName();

        sb.setLength(0);
        sb.append(targetRepositoryPackage);
        sb.append(".");
        sb.append(prefix);
        if (Boolean.valueOf(sharding)) {
            sb.append("Sharding");
        }
        sb.append("Repository"); //$NON-NLS-1$
        this.setMyBatis3JavaMapperType(sb.toString());
        sb.setLength(0);

        String targetServicePackage =
                javaClientGeneratorConfiguration.getProperty("targetServicePackage");
        if (targetServicePackage == null) {
            targetServicePackage = packageName;
        }
        // add Service and ServiceImpl
        sb.append(targetServicePackage);
        sb.append(".");
        sb.append(prefix);
        if (Boolean.valueOf(sharding)) {
            sb.append("Sharding");
        }
        sb.append("Service");
        this.setMyBatis3JavaServiceType(sb.toString());
        sb.setLength(0);

        String targetServiceImplPackage =
                javaClientGeneratorConfiguration.getProperty("targetServiceImplPackage");
        if (targetServiceImplPackage == null) {
            targetServiceImplPackage = packageName;
        }
        sb.append(targetServiceImplPackage);
        sb.append(".");
        sb.append(prefix);
        if (Boolean.valueOf(sharding)) {
            sb.append("Sharding");
        }
        sb.append("ServiceImpl");
        this.setMyBatis3JavaServiceImplType(sb.toString());
        sb.setLength(0);

        sb.append(this.calculateJavaClientInterfacePackage());
        sb.append('.');
        if (stringHasValue(this.tableConfiguration.getSqlProviderName())) {
            sb.append(this.tableConfiguration.getSqlProviderName());
        } else {
            if (stringHasValue(this.fullyQualifiedTable.getDomainObjectSubPackage())) {
                sb.append(this.fullyQualifiedTable.getDomainObjectSubPackage());
                sb.append('.');
            }
            sb.append(this.fullyQualifiedTable.getDomainObjectName());
            sb.append("SqlProvider"); //$NON-NLS-1$
        }
        this.setMyBatis3SqlProviderType(sb.toString());
    }

    protected String calculateJavaModelPackage() {
        JavaModelGeneratorConfiguration config = this.context.getJavaModelGeneratorConfiguration();

        StringBuilder sb = new StringBuilder();
        sb.append(config.getTargetPackage());
        sb.append(this.fullyQualifiedTable.getSubPackageForModel(this.isSubPackagesEnabled(config)));

        return sb.toString();
    }

    protected void calculateModelAttributes() {
        String pakkage = this.calculateJavaModelPackage();

        StringBuilder sb = new StringBuilder();
        sb.append(pakkage);
        sb.append('.');
        sb.append(this.fullyQualifiedTable.getDomainObjectName());
        sb.append("Key"); //$NON-NLS-1$
        this.setPrimaryKeyType(sb.toString());

        JavaModelGeneratorConfiguration javaModelGeneratorConfiguration =
                this.context.getJavaModelGeneratorConfiguration();
        String recordPackage = javaModelGeneratorConfiguration.getProperty("targetRecordPackage");
        if (recordPackage == null) {
            recordPackage = pakkage;
        }

        sb.setLength(0);
        sb.append(recordPackage.trim());
        sb.append('.');
        sb.append(this.fullyQualifiedTable.getDomainObjectName());
        this.setBaseRecordType(sb.toString());

        sb.setLength(0);
        sb.append(pakkage);
        sb.append('.');
        sb.append(this.fullyQualifiedTable.getDomainObjectName());
        sb.append("WithBLOBs"); //$NON-NLS-1$
        this.setRecordWithBLOBsType(sb.toString());

        String examplePackage = javaModelGeneratorConfiguration.getProperty("targetExamplePackage");
        if (examplePackage == null) {
            examplePackage = pakkage;
        }
        sb.setLength(0);
        sb.append(examplePackage.trim());
        sb.append('.');
        sb.append(this.fullyQualifiedTable.getDomainObjectName());
        String sharding = this.context.getProperty("sharding");
        if (Boolean.valueOf(sharding)) {
            sb.append("Sharding");
        }
        sb.append("Example"); //$NON-NLS-1$
        this.setExampleType(sb.toString());
    }

    protected String calculateSqlMapPackage() {
        StringBuilder sb = new StringBuilder();
        SqlMapGeneratorConfiguration config = this.context.getSqlMapGeneratorConfiguration();

        // config can be null if the Java client does not require XML
        if (config != null) {
            sb.append(config.getTargetPackage());
            sb.append(this.fullyQualifiedTable
                    .getSubPackageForClientOrSqlMap(this.isSubPackagesEnabled(config)));
            if (stringHasValue(this.tableConfiguration.getMapperName())) {
                String mapperName = this.tableConfiguration.getMapperName();
                int ind = mapperName.lastIndexOf('.');
                if (ind != -1) {
                    sb.append('.').append(mapperName.substring(0, ind));
                }
            } else if (stringHasValue(this.fullyQualifiedTable.getDomainObjectSubPackage())) {
                sb.append('.').append(this.fullyQualifiedTable.getDomainObjectSubPackage());
            }
        }

        return sb.toString();
    }

    protected String calculateIbatis2SqlMapFileName() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.fullyQualifiedTable.getIbatis2SqlMapNamespace());
        sb.append("_SqlMap.xml"); //$NON-NLS-1$
        return sb.toString();
    }

    protected String calculateMyBatis3XmlMapperFileName() {
        StringBuilder sb = new StringBuilder();
        if (stringHasValue(this.tableConfiguration.getMapperName())) {
            String mapperName = this.tableConfiguration.getMapperName();
            int ind = mapperName.lastIndexOf('.');
            if (ind == -1) {
                sb.append(mapperName);
            } else {
                sb.append(mapperName.substring(ind + 1));
            }
            sb.append(".xml"); //$NON-NLS-1$
        } else {
            sb.append(this.fullyQualifiedTable.getDomainObjectName());
            String sharding = this.context.getProperty("sharding");
            if (Boolean.valueOf(sharding)) {
                sb.append("Sharding");
            }
            sb.append("Mapper.xml"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    protected String calculateIbatis2SqlMapNamespace() {
        return this.fullyQualifiedTable.getIbatis2SqlMapNamespace();
    }

    protected String calculateMyBatis3FallbackSqlMapNamespace() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.calculateSqlMapPackage());
        sb.append('.');
        if (stringHasValue(this.tableConfiguration.getMapperName())) {
            sb.append(this.tableConfiguration.getMapperName());
        } else {
            sb.append(this.fullyQualifiedTable.getDomainObjectName());
            sb.append("Mapper"); //$NON-NLS-1$
        }
        return sb.toString();
    }

    protected String calculateSqlMapFullyQualifiedRuntimeTableName() {
        return this.fullyQualifiedTable.getFullyQualifiedTableNameAtRuntime();
    }

    protected String calculateSqlMapAliasedFullyQualifiedRuntimeTableName() {
        return this.fullyQualifiedTable.getAliasedFullyQualifiedTableNameAtRuntime();
    }

    public String getFullyQualifiedTableNameAtRuntime() {
        return this.internalAttributes
                .get(InternalAttribute.ATTR_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME);
    }

    public String getAliasedFullyQualifiedTableNameAtRuntime() {
        return this.internalAttributes
                .get(InternalAttribute.ATTR_ALIASED_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME);
    }

    /**
     * This method can be used to initialize the generators before they will be called.
     *
     * <p>
     * This method is called after all the setX methods, but before getNumberOfSubtasks(),
     * getGeneratedJavaFiles, and getGeneratedXmlFiles.
     *
     * @param warnings the warnings
     * @param progressCallback the progress callback
     */
    public abstract void calculateGenerators(List<String> warnings,
                                             ProgressCallback progressCallback);

    /**
     * This method should return a list of generated Java files related to this table. This list could
     * include various types of model classes, as well as DAO classes.
     *
     * @return the list of generated Java files for this table
     */
    public abstract List<GeneratedJavaFile> getGeneratedJavaFiles();

    /**
     * This method should return a list of generated XML files related to this table. Most
     * implementations will only return one file - the generated SqlMap file.
     *
     * @return the list of generated XML files for this table
     */
    public abstract List<GeneratedXmlFile> getGeneratedXmlFiles();

    /**
     * Denotes whether generated code is targeted for Java version 5.0 or higher.
     *
     * @return true if the generated code makes use of Java5 features
     */
    public abstract boolean isJava5Targeted();

    /**
     * This method should return the number of progress messages that will be send during the
     * generation phase.
     *
     * @return the number of progress messages
     */
    public abstract int getGenerationSteps();

    /**
     * This method exists to give plugins the opportunity to replace the calculated rules if
     * necessary.
     *
     * @param rules the new rules
     */
    public void setRules(Rules rules) {
        this.rules = rules;
    }

    public TableConfiguration getTableConfiguration() {
        return this.tableConfiguration;
    }

    public void setDAOImplementationType(String daoImplementationType) {
        this.internalAttributes.put(InternalAttribute.ATTR_DAO_IMPLEMENTATION_TYPE,
                daoImplementationType);
    }

    public void setDAOInterfaceType(String daoInterfaceType) {
        this.internalAttributes.put(InternalAttribute.ATTR_DAO_INTERFACE_TYPE, daoInterfaceType);
    }

    public void setPrimaryKeyType(String primaryKeyType) {
        this.internalAttributes.put(InternalAttribute.ATTR_PRIMARY_KEY_TYPE, primaryKeyType);
    }

    public void setBaseRecordType(String baseRecordType) {
        this.internalAttributes.put(InternalAttribute.ATTR_BASE_RECORD_TYPE, baseRecordType);
    }

    public void setRecordWithBLOBsType(String recordWithBLOBsType) {
        this.internalAttributes.put(InternalAttribute.ATTR_RECORD_WITH_BLOBS_TYPE, recordWithBLOBsType);
    }

    public void setExampleType(String exampleType) {
        this.internalAttributes.put(InternalAttribute.ATTR_EXAMPLE_TYPE, exampleType);
    }

    public void setIbatis2SqlMapPackage(String sqlMapPackage) {
        this.internalAttributes.put(InternalAttribute.ATTR_IBATIS2_SQL_MAP_PACKAGE, sqlMapPackage);
    }

    public void setIbatis2SqlMapFileName(String sqlMapFileName) {
        this.internalAttributes.put(InternalAttribute.ATTR_IBATIS2_SQL_MAP_FILE_NAME, sqlMapFileName);
    }

    public void setIbatis2SqlMapNamespace(String sqlMapNamespace) {
        this.internalAttributes.put(InternalAttribute.ATTR_IBATIS2_SQL_MAP_NAMESPACE, sqlMapNamespace);
    }

    public void setMyBatis3FallbackSqlMapNamespace(String sqlMapNamespace) {
        this.internalAttributes.put(InternalAttribute.ATTR_MYBATIS3_FALLBACK_SQL_MAP_NAMESPACE,
                sqlMapNamespace);
    }

    public void setSqlMapFullyQualifiedRuntimeTableName(String fullyQualifiedRuntimeTableName) {
        this.internalAttributes.put(InternalAttribute.ATTR_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME,
                fullyQualifiedRuntimeTableName);
    }

    public void setSqlMapAliasedFullyQualifiedRuntimeTableName(
            String aliasedFullyQualifiedRuntimeTableName) {
        boolean sharding = Boolean.parseBoolean(this.getContext().getProperty("sharding"));
        this.internalAttributes.put(
                InternalAttribute.ATTR_ALIASED_FULLY_QUALIFIED_TABLE_NAME_AT_RUNTIME,
                sharding ? "${shardingTable.name}" : aliasedFullyQualifiedRuntimeTableName);
    }

    public String getMyBatis3XmlMapperPackage() {
        return this.internalAttributes.get(InternalAttribute.ATTR_MYBATIS3_XML_MAPPER_PACKAGE);
    }

    public void setMyBatis3XmlMapperPackage(String mybatis3XmlMapperPackage) {
        this.internalAttributes.put(InternalAttribute.ATTR_MYBATIS3_XML_MAPPER_PACKAGE,
                mybatis3XmlMapperPackage);
    }

    public String getMyBatis3XmlMapperFileName() {
        return this.internalAttributes.get(InternalAttribute.ATTR_MYBATIS3_XML_MAPPER_FILE_NAME);
    }

    public void setMyBatis3XmlMapperFileName(String mybatis3XmlMapperFileName) {
        this.internalAttributes.put(InternalAttribute.ATTR_MYBATIS3_XML_MAPPER_FILE_NAME,
                mybatis3XmlMapperFileName);
    }

    public String getMyBatis3JavaMapperType() {
        return this.internalAttributes.get(InternalAttribute.ATTR_MYBATIS3_JAVA_MAPPER_TYPE);
    }

    public void setMyBatis3JavaMapperType(String mybatis3JavaMapperType) {
        this.internalAttributes.put(InternalAttribute.ATTR_MYBATIS3_JAVA_MAPPER_TYPE,
                mybatis3JavaMapperType);
    }

    public String getMyBatis3JavaServiceType() {
        return this.internalAttributes.get(InternalAttribute.ATTR_MYBATIS3_JAVA_SERVICE_TYPE);
    }

    public void setMyBatis3JavaServiceType(String mybatis3JavaServiceType) {
        this.internalAttributes.put(InternalAttribute.ATTR_MYBATIS3_JAVA_SERVICE_TYPE,
                mybatis3JavaServiceType);
    }

    public String getMyBatis3JavaServiceImplType() {
        return this.internalAttributes.get(InternalAttribute.ATTR_MYBATIS3_JAVA_SERVICE_IMPL_TYPE);
    }

    public void setMyBatis3JavaServiceImplType(String mybatis3JavaServiceImplType) {
        this.internalAttributes.put(InternalAttribute.ATTR_MYBATIS3_JAVA_SERVICE_IMPL_TYPE,
                mybatis3JavaServiceImplType);
    }

    public String getMyBatis3SqlProviderType() {
        return this.internalAttributes.get(InternalAttribute.ATTR_MYBATIS3_SQL_PROVIDER_TYPE);
    }

    public void setMyBatis3SqlProviderType(String mybatis3SqlProviderType) {
        this.internalAttributes.put(InternalAttribute.ATTR_MYBATIS3_SQL_PROVIDER_TYPE,
                mybatis3SqlProviderType);
    }

    public TargetRuntime getTargetRuntime() {
        return this.targetRuntime;
    }

    public boolean isImmutable() {
        Properties properties;

        if (this.tableConfiguration.getProperties().containsKey(PropertyRegistry.ANY_IMMUTABLE)) {
            properties = this.tableConfiguration.getProperties();
        } else {
            properties = this.context.getJavaModelGeneratorConfiguration().getProperties();
        }

        return isTrue(properties.getProperty(PropertyRegistry.ANY_IMMUTABLE));
    }

    public boolean isConstructorBased() {
        if (this.isImmutable()) {
            return true;
        }

        Properties properties;

        if (this.tableConfiguration.getProperties()
                .containsKey(PropertyRegistry.ANY_CONSTRUCTOR_BASED)) {
            properties = this.tableConfiguration.getProperties();
        } else {
            properties = this.context.getJavaModelGeneratorConfiguration().getProperties();
        }

        return isTrue(properties.getProperty(PropertyRegistry.ANY_CONSTRUCTOR_BASED));
    }

    /**
     * Should return true if an XML generator is required for this table. This method will be called
     * during validation of the configuration, so it should not rely on database introspection. This
     * method simply tells the validator if an XML configuration is normally required for this
     * implementation.
     *
     * @return true, if successful
     */
    public abstract boolean requiresXMLGenerator();

    public Context getContext() {
        return this.context;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTableType() {
        return this.tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }
}
