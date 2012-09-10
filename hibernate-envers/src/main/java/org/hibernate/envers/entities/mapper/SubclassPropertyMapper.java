/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2008, Red Hat Middleware LLC or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Middleware LLC.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.envers.entities.mapper;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.configuration.AuditConfiguration;
import org.hibernate.envers.entities.PropertyData;
import org.hibernate.envers.reader.AuditReaderImplementor;

/**
 * A mapper which maps from a parent mapper and a "main" one, but adds only to the "main". The "main" mapper
 * should be the mapper of the subclass.
 *
 * @author Adam Warski (adam at warski dot org)
 * @author Michal Skowronek (mskowr at o2 dot pl)
 */
public class SubclassPropertyMapper implements ExtendedPropertyMapper {
    private ExtendedPropertyMapper main;
    private ExtendedPropertyMapper parentMapper;

    public SubclassPropertyMapper(ExtendedPropertyMapper main, ExtendedPropertyMapper parentMapper) {
        this.main = main;
        this.parentMapper = parentMapper;
    }

    public boolean map(SessionImplementor session, Map<String, Object> data, String[] propertyNames, Object[] newState, Object[] oldState) {
        boolean parentDiffs = parentMapper.map(session, data, propertyNames, newState, oldState);
        boolean mainDiffs = main.map(session, data, propertyNames, newState, oldState);

        return parentDiffs || mainDiffs;
    }

    public boolean mapToMapFromEntity(SessionImplementor session, Map<String, Object> data, Object newObj, Object oldObj) {
        boolean parentDiffs = parentMapper.mapToMapFromEntity(session, data, newObj, oldObj);
        boolean mainDiffs = main.mapToMapFromEntity(session, data, newObj, oldObj);

        return parentDiffs || mainDiffs;
    }

	@Override
	public void mapModifiedFlagsToMapFromEntity(SessionImplementor session, Map<String, Object> data, Object newObj, Object oldObj) {
		parentMapper.mapModifiedFlagsToMapFromEntity(session, data, newObj, oldObj);
        main.mapModifiedFlagsToMapFromEntity(session, data, newObj, oldObj);
	}

	@Override
	public void mapModifiedFlagsToMapForCollectionChange(String collectionPropertyName, Map<String, Object> data) {
		parentMapper.mapModifiedFlagsToMapForCollectionChange(collectionPropertyName, data);
		main.mapModifiedFlagsToMapForCollectionChange(collectionPropertyName, data);
	}

	public void mapToEntityFromMap(AuditConfiguration verCfg, Object obj, Map data, Object primaryKey, AuditReaderImplementor versionsReader, Number revision) {
        parentMapper.mapToEntityFromMap(verCfg, obj, data, primaryKey, versionsReader, revision);
        main.mapToEntityFromMap(verCfg, obj, data, primaryKey, versionsReader, revision);
    }

    public List<PersistentCollectionChangeData> mapCollectionChanges(String referencingPropertyName,
                                                                                    PersistentCollection newColl,
                                                                                    Serializable oldColl,
                                                                                    Serializable id) {
        List<PersistentCollectionChangeData> parentCollectionChanges = parentMapper.mapCollectionChanges(
                referencingPropertyName, newColl, oldColl, id);

        List<PersistentCollectionChangeData> mainCollectionChanges = main.mapCollectionChanges(
                referencingPropertyName, newColl, oldColl, id);

        if (parentCollectionChanges == null) {
            return mainCollectionChanges;
        } else {
            if (mainCollectionChanges != null) {
                parentCollectionChanges.addAll(mainCollectionChanges);
            }
            return parentCollectionChanges;
        }
    }

    public void addToAuditQuery(QueryBuilder qb) {
        parentMapper.addToAuditQuery(qb);
        main.addToAuditQuery(qb);
    }

    public void initializeInstance(Object instance, Map instanceAttributes, List queryResult, EntityInstantiator entityInstantiator) {
        parentMapper.initializeInstance(instance, instanceAttributes, queryResult, entityInstantiator);
        main.initializeInstance(instance, instanceAttributes, queryResult, entityInstantiator);
    }

    public CompositeMapperBuilder addComponent(PropertyData propertyData, String componentClassName) {
        return main.addComponent(propertyData, componentClassName);
    }

    public void addComposite(PropertyData propertyData, PropertyMapper propertyMapper) {
        main.addComposite(propertyData, propertyMapper);
    }

    public void add(PropertyData propertyData) {
        main.add(propertyData);
    }
}
