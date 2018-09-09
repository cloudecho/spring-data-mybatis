/*
 *
 *   Copyright 2016 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.springframework.data.mybatis.mapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.SimplePropertyHandler;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.data.mybatis.annotations.Entity;
import org.springframework.data.util.ParsingUtils;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.StringUtils;

import java.util.Comparator;

/**
 * @author Jarvis Song
 */
public class MybatisPersistentEntityImpl<T> extends BasicPersistentEntity<T, MybatisPersistentProperty>
        implements MybatisPersistentEntity<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MybatisPersistentEntityImpl.class);

    private final MybatisMappingContext context;

    public MybatisPersistentEntityImpl(MybatisMappingContext context, TypeInformation<T> information) {
        super(information);
        this.context = context;


    }

    public MybatisPersistentEntityImpl(MybatisMappingContext context, TypeInformation<T> information, Comparator<MybatisPersistentProperty> comparator) {
        super(information, comparator);
        this.context = context;
    }

    private volatile boolean completed = false;

    @Override
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public synchronized void verify() {
        super.verify();
        this.completed = true;
        LOGGER.info("Create PersistentEntity {} done", getName());
        this.notifyAll();
    }

    @Override
    public MybatisMappingContext getContext() {
        return this.context;
    }

    @Override
    public String getEntityName() {
        Entity entity = getType().getAnnotation(Entity.class);
        if (null != entity && StringUtils.hasText(entity.name())) {
            return entity.name();
        }
        return StringUtils.uncapitalize(getType().getSimpleName());
    }

    @Override
    public String getSequenceName() {
        String tableName;
        Entity entity = getType().getAnnotation(Entity.class);
        if (null != entity && StringUtils.hasText(entity.table())) {
            tableName = entity.table();
        } else {
            tableName = ParsingUtils.reconcatenateCamelCase(getType().getSimpleName(), "_");
        }
        if (null != entity && StringUtils.hasText(entity.schema())) {
            return entity.schema() + ".seq_" + tableName;
        }
        return "seq_" + tableName;
    }

    @Override
    public MybatisPersistentProperty findByColumnName(final String columnName) {
        final MybatisPersistentProperty[] result = new MybatisPersistentProperty[1];
        doWithProperties(new SimplePropertyHandler() {
            @Override
            public void doWithPersistentProperty(PersistentProperty<?> pp) {
                MybatisPersistentProperty property = (MybatisPersistentProperty) pp;
                if (columnName.equalsIgnoreCase(property.getColumnName())) {
                    result[0] = property;
                    return;
                }
            }
        });
        return result[0];
    }


    @Override
    public String getTableName() {
        String tableName;
        Entity entity = getType().getAnnotation(Entity.class);
        if (null != entity && StringUtils.hasText(entity.table())) {
            tableName = entity.table();
        } else {
            tableName = ParsingUtils.reconcatenateCamelCase(getType().getSimpleName(), "_");
        }
        if (null != entity && StringUtils.hasText(entity.schema())) {
            tableName = entity.schema() + "." + tableName;
        }
        return tableName;
    }
}
