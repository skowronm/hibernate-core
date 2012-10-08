/*
 * Copyright (c) 2012, Sabre Holdings. All Rights Reserved.
 */

package org.hibernate.envers.mappergenerator;

import com.sun.istack.internal.Nullable;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.configuration.AuditConfiguration;
import org.hibernate.envers.configuration.AuditEntitiesConfiguration;
import org.hibernate.envers.entities.EntityInstantiator;
import org.hibernate.envers.entities.PropertyData;
import org.hibernate.envers.entities.mapper.id.IdMapper;
import org.hibernate.envers.entities.mapper.id.QueryParameterData;
import org.hibernate.envers.entities.mapper.relation.BasicCollectionMapper;
import org.hibernate.envers.entities.mapper.relation.CommonCollectionMapperData;
import org.hibernate.envers.entities.mapper.relation.MiddleComponentData;
import org.hibernate.envers.entities.mapper.relation.MiddleIdData;
import org.hibernate.envers.entities.mapper.relation.component.MiddleComponentMapper;
import org.hibernate.envers.entities.mapper.relation.component.MiddleRelatedComponentMapper;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.reader.AuditReaderImplementor;
import org.hibernate.envers.tools.Pair;
import org.hibernate.envers.tools.query.Parameters;
import org.hibernate.envers.tools.query.QueryBuilder;
import org.hibernate.envers.tools.reflection.ReflectionTools;
import org.hibernate.property.Getter;
import org.hibernate.property.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EagerBasicCollectionMapper<T extends Collection> extends BasicCollectionMapper<T> {

    public EagerBasicCollectionMapper(CommonCollectionMapperData commonCollectionMapperData, Class<? extends T> collectionClass, Class<? extends T> proxyClass,
                                      MiddleComponentData elementComponentData) {
        super(commonCollectionMapperData, collectionClass, proxyClass, elementComponentData);
    }

    @Override
    public void mapToEntityFromMap(AuditConfiguration verCfg, Object obj, Map data, Object primaryKey, AuditReaderImplementor versionsReader, Number revision) {
    }

    @Override
    public void initializeResultEntities(List entities, List<Map> entitiesAttributes, EntityInstantiator entityInstantiator, Session session, Number revision) {
        AuditEntitiesConfiguration verEntCfg = commonCollectionMapperData.getVerEntCfg();
        String versionsMiddleEntityName = commonCollectionMapperData.getVersionsMiddleEntityName();
        String originalId = verEntCfg.getOriginalIdPropName();

        IdMapper originalMapper = commonCollectionMapperData.getReferencingIdData().getOriginalMapper();
        IdMapper prefixedMapper = commonCollectionMapperData.getReferencingIdData().getPrefixedMapper();
        List<QueryParameterData> nonPrefixedIdParameterDatas = originalMapper.mapToQueryParametersFromId(null);
        List<QueryParameterData> prefixedIdParameterDatas = prefixedMapper.mapToQueryParametersFromId(null);

        List<List> ids = new ArrayList<List>();
        Map<Object, List<Integer>> revisions = new HashMap<Object, List<Integer>>();
        Map<Pair<Object, Integer>, Object> entitiesByIdAndRevision = new HashMap<Pair<Object, Integer>, Object>();
        int minimumRev = Integer.MAX_VALUE;
        int maximumRev = 0;

        Iterator entIt = entities.iterator();
        for (Map attributes : entitiesAttributes) {
            Number entityRevision = revision == null ? resolveRevisionNumber(attributes) : revision;
            int revisionAsInt = entityRevision.intValue();
            if (revisionAsInt > maximumRev) {
                maximumRev = revisionAsInt;
            }
            if (revisionAsInt < minimumRev) {
                minimumRev = revisionAsInt;
            }
            List idProperties = new ArrayList();
            for (QueryParameterData parameterData : nonPrefixedIdParameterDatas) {
                String idProperty = parameterData.getProperty(null);
                Object idPropertyValue = ((Map) attributes.get(originalId)).get(idProperty);
                idProperties.add(idPropertyValue);
            }
            ids.add(idProperties);
            Object id = originalMapper.mapToIdFromMap((Map) attributes.get(originalId));
            Pair<Object, Integer> idRevisionPair = Pair.make(id, revisionAsInt);
            entitiesByIdAndRevision.put(idRevisionPair, entIt.next());
            List<Integer> revisionsForEntity = revisions.get(id);
            if (revisionsForEntity == null) {
                revisionsForEntity = new ArrayList<Integer>();
                revisions.put(id, revisionsForEntity);
            }
            revisionsForEntity.add(revisionAsInt);
        }

        if (!commonCollectionMapperData.isWithMiddleTable()) {
            originalId = null;
        }

        Map<Pair<Object, Integer>, List<Map>> elementsOfEntities = new HashMap<Pair<Object, Integer>, List<Map>>();
        Map<Pair<Object, Integer>, List<Map>> actualElementsOfEntities = null;

        if (!ids.isEmpty()) {
            QueryBuilder mBuilder = new QueryBuilder(versionsMiddleEntityName, "m");
            addIdsCriteria(mBuilder, originalId, prefixedIdParameterDatas, ids);
            addRevisionCriteria(mBuilder, minimumRev, maximumRev);
            addRevTypeCriteria(mBuilder);

            Query query = mBuilder.toQuery(session);
            List results = query.list();
            elementsOfEntities = extractElementsFromResults(results, prefixedMapper, revisions, originalId);
        }

        MiddleComponentMapper elementMapper = elementComponentData.getComponentMapper();
        IdMapper elementOwnerIdMapper = prefixedMapper;
        if (elementMapper instanceof MiddleRelatedComponentMapper && !(versionsMiddleEntityName.equals(
                ((MiddleRelatedComponentMapper) elementMapper).getRelatedIdData().getAuditEntityName()))) {
            String relatedEntityName =
                    ((MiddleRelatedComponentMapper) elementMapper).getRelatedIdData().getEntityName();
            String relatedAuditEntityName =
                    ((MiddleRelatedComponentMapper) elementMapper).getRelatedIdData().getAuditEntityName();
            MiddleIdData relatedIdData = ((MiddleRelatedComponentMapper) elementMapper).getRelatedIdData();
            IdMapper prefixedElementIdMapper = relatedIdData.getPrefixedMapper();
            IdMapper elementIdMapper = relatedIdData.getOriginalMapper();
            List<QueryParameterData> prefixedParameterDatas = prefixedElementIdMapper.mapToQueryParametersFromId(null);
            List<QueryParameterData> originalParameterDatas = elementIdMapper.mapToQueryParametersFromId(null);
            List<List> elementIds = new ArrayList<List>();
            Map<Object, List<Integer>> elementRevisions = new HashMap<Object, List<Integer>>();

            int elementMinimumRev = Integer.MAX_VALUE;
            int elementMaximumRev = 0;

            for (List<Map> elements : elementsOfEntities.values()) {
                for (Map element : elements) {
                    int elementRevNumber =
                            revision == null ? resolveRevisionNumber(element).intValue() : revision.intValue();
                    if (elementRevNumber < elementMinimumRev) {
                        elementMinimumRev = elementRevNumber;
                    }
                    if (elementRevNumber > elementMaximumRev) {
                        elementMaximumRev = elementRevNumber;
                    }
                    List idProperties = new ArrayList();
                    for (QueryParameterData parameterData : prefixedParameterDatas) {
                        String idProperty = parameterData.getProperty(null);
                        Object idPropertyValue = ((Map) element.get(originalId)).get(idProperty);
                        idProperties.add(idPropertyValue);
                    }
                    elementIds.add(idProperties);

                    Object elementId = prefixedElementIdMapper.mapToIdFromMap((Map) element.get(originalId));
                    List<Integer> revisionsForElement = elementRevisions.get(elementId);
                    if (revisionsForElement == null) {
                        revisionsForElement = new ArrayList<Integer>();
                        elementRevisions.put(elementId, revisionsForElement);
                    }
                    revisionsForElement.add(elementRevNumber);
                }
            }

            if (!elementIds.isEmpty()) {
                if (relatedAuditEntityName != null) {
                    QueryBuilder eeBuilder = new QueryBuilder(relatedAuditEntityName, "ee");
                    addIdsCriteria(eeBuilder, originalId, originalParameterDatas, elementIds);
                    addRevisionCriteria(eeBuilder, elementMinimumRev, elementMaximumRev);
                    addRevTypeCriteria(eeBuilder);
                    Query elementQuery = eeBuilder.toQuery(session);
                    List elementResults = elementQuery.list();
                    actualElementsOfEntities =
                            extractElementsFromResults(elementResults, elementIdMapper, elementRevisions, originalId);
                    elementOwnerIdMapper = prefixedElementIdMapper;
                } else { // target - collection element is not audited
                    QueryBuilder eeBuilder = new QueryBuilder(relatedEntityName, "ee");
                    addIdsCriteria(eeBuilder, null, originalParameterDatas, elementIds);
                    Query elementQuery = eeBuilder.toQuery(session);
                    List elementResults = elementQuery.list();
                    Map elementsById = new HashMap();

                    for (Object result : elementResults) {
                        Object id = elementIdMapper.mapToIdFromEntity(result);
                        elementsById.put(id, result);
                    }

                    for (Map.Entry<Pair<Object, Integer>, Object> entry : entitiesByIdAndRevision.entrySet()) {
                        Pair<Object, Integer> idRev = entry.getKey();
                        Object entity = entry.getValue();

                        T collection = setupCollection(entity);

                        List<Map> elements = elementsOfEntities.get(idRev);
                        if (elements == null) {
                            continue;
                        }
                        for (Map element : elements) {
                            Object elementId;
                            if (originalId != null) {
                                elementId = prefixedElementIdMapper.mapToIdFromMap((Map) element.get(originalId));
                            } else {
                                elementId = prefixedElementIdMapper.mapToIdFromMap(element);
                            }
                            Object e = elementsById.get(elementId);
                            collection.add(e);
                        }
                    }
                    return;
                }
            }
        }

        for (Map.Entry<Pair<Object, Integer>, Object> entry : entitiesByIdAndRevision.entrySet()) {
            Pair<Object, Integer> idRev = entry.getKey();
            Object entity = entry.getValue();

            T collection = setupCollection(entity);

            List<Map> elements = elementsOfEntities.get(idRev);
            if (elements == null) {
                continue;
            }
            for (Map element : elements) {
                Object elementId;
                if (originalId != null) {
                    elementId = elementOwnerIdMapper.mapToIdFromMap((Map) element.get(originalId));
                } else {
                    elementId = elementOwnerIdMapper.mapToIdFromMap(element);
                }
                int elementRevNumber =
                        revision == null ? resolveRevisionNumber(element).intValue() : revision.intValue();
                Pair<Object, Integer> elIdRev = Pair.make(elementId, elementRevNumber);

                if (actualElementsOfEntities != null) {
                    for (Map map : actualElementsOfEntities.get(elIdRev)) {
                        Object elementObj =
                                elementMapper.mapToObjectFromFullMap(entityInstantiator, map, null, elementRevNumber);
                        collection.add(elementObj);
                    }
                } else {
                    Object elementObj =
                            elementMapper.mapToObjectFromFullMap(entityInstantiator, element, null, elementRevNumber);
                    collection.add(elementObj);
                }
            }
        }

    }

    private T setupCollection(Object entity) {
        T collection;
        try {
            collection = collectionClass.newInstance();
        } catch (InstantiationException e) {
            throw new AuditException(e);
        } catch (IllegalAccessException e) {
            throw new AuditException(e);
        }

        PropertyData propertyName = commonCollectionMapperData.getCollectionReferencingPropertyData();
        String[] pathToProperty = propertyName.getName().split("_");
        Object currentElement = entity;
        for (int i = 0; i < pathToProperty.length - 1; ++i) {
            Getter getter = ReflectionTools.getGetter(currentElement.getClass(), pathToProperty[i], "field");
            currentElement = getter.get(currentElement);
        }
        Setter setter = ReflectionTools.getSetter(currentElement.getClass(), pathToProperty[pathToProperty.length - 1],
                "field");
        setter.set(currentElement, collection, null);
        return collection;
    }

    private Map<Pair<Object, Integer>, List<Map>> extractElementsFromResults(List results, IdMapper idMapper, Map<Object, List<Integer>> revisionsOfEntities, @Nullable String originalId) {
        Map<Pair<Object, Integer>, List<Map>> elementsOfEntities = new HashMap<Pair<Object, Integer>, List<Map>>();
        for (Object result : results) {
            Map resultAttr = (Map) result;
            Object id;
            if (originalId != null) {
                id = idMapper.mapToIdFromMap((Map) resultAttr.get(originalId));
            } else {
                id = idMapper.mapToIdFromMap(resultAttr);
            }
            Number revisionNumber = resolveRevisionNumber(resultAttr);
            Number revEndNumber = resolveRevEndNumber(resultAttr);
            List<Integer> revisionsOfEntity = revisionsOfEntities.get(id);
            for (Integer revision : revisionsOfEntity) {
                if (revisionNumber.intValue() <= revision && (revEndNumber == null || revEndNumber.intValue() > revision)) {
                    Pair<Object, Integer> idRevision = Pair.make(id, revision);
                    List<Map> elementsOfEntity = elementsOfEntities.get(idRevision);
                    if (elementsOfEntity == null) {
                        elementsOfEntity = new ArrayList<Map>();
                        elementsOfEntities.put(idRevision, elementsOfEntity);
                    }
                    elementsOfEntity.add(resultAttr);
                }
            }
        }
        return elementsOfEntities;
    }

    private Number resolveRevisionNumber(Map attributes) {
        AuditEntitiesConfiguration verEntCfg = commonCollectionMapperData.getVerEntCfg();
        String originalId = verEntCfg.getOriginalIdPropName();
        String revisionPropertyName = verEntCfg.getRevisionFieldName();
        return (Number) ((HibernateProxy) ((Map) attributes.get(originalId)).get(
                revisionPropertyName)).getHibernateLazyInitializer().getIdentifier();
    }

    private Number resolveRevEndNumber(Map attributes) {
        AuditEntitiesConfiguration verEntCfg = commonCollectionMapperData.getVerEntCfg();
        String revisionEndFieldName = verEntCfg.getRevisionEndFieldName();
        HibernateProxy revendProxy = (HibernateProxy) attributes.get(revisionEndFieldName);
        if (revendProxy == null) {
            return null;
        }
        return (Number) revendProxy.getHibernateLazyInitializer().getIdentifier();
    }

    private void addRevisionCriteria(QueryBuilder builder, int minimumRev, int maximumRev) {
        AuditEntitiesConfiguration verEntCfg = commonCollectionMapperData.getVerEntCfg();
        String originalId = verEntCfg.getOriginalIdPropName();
        String revisionPropertyName = verEntCfg.getRevisionFieldName();
        String revisionEndFieldName = verEntCfg.getRevisionEndFieldName();
        Parameters rootParameters = builder.getRootParameters();
        rootParameters.addWhereWithParam(originalId + "." + revisionPropertyName + ".id", "<=", maximumRev);
        Parameters subParam = rootParameters.addSubParameters("or");
        subParam.addWhereWithParam(revisionEndFieldName + ".id", ">", minimumRev);
        subParam.addNullRestriction(revisionEndFieldName + ".id", true);
    }

    private void addIdsCriteria(QueryBuilder builder, @Nullable String originalId, List<QueryParameterData> idParameterDatas, List<List> ids) {
        Parameters rootParameters = builder.getRootParameters();
        if (idParameterDatas.size() == 1) {
            rootParameters.addWhereWithParams(createLeftHand(originalId, idParameterDatas), "in (", ids.toArray(), ")",
                    false);
        } else {
            Parameters or = rootParameters.addSubParameters("or");
            for (List idProps : ids) {
                Parameters and = or.addSubParameters("and");
                Iterator idProp = idProps.iterator();
                for (QueryParameterData idParameterData : idParameterDatas) {
                    String property = idParameterData.getProperty(null);
                    if (originalId != null) {
                        property = originalId + "." + property;
                    }
                    and.addWhereWithParam(property, "=", idProp.next());
                }
            }
        }
    }

    private void addRevTypeCriteria(QueryBuilder builder) {
        String revisionTypePropName = commonCollectionMapperData.getVerEntCfg().getRevisionTypePropName();
        Parameters rootParameters = builder.getRootParameters();
        rootParameters.addWhereWithParam(revisionTypePropName, "<>", RevisionType.DEL);
    }

    private String createLeftHand(@Nullable String originalId, List<QueryParameterData> prefixedIdParameterDatas) {
        StringBuilder leftHand = new StringBuilder();
        if (prefixedIdParameterDatas.size() > 1) {
            leftHand.append("(");
        }
        Iterator<QueryParameterData> iterator = prefixedIdParameterDatas.iterator();
        while (iterator.hasNext()) {
            String property = iterator.next().getProperty(null);
            if (originalId != null) {
                leftHand.append(originalId).append('.');
            }
            leftHand.append(property);
            if (iterator.hasNext()) {
                leftHand.append(", ");
            }
        }
        if (prefixedIdParameterDatas.size() > 1) {
            leftHand.append(")");
        }
        return leftHand.toString();
    }

}
