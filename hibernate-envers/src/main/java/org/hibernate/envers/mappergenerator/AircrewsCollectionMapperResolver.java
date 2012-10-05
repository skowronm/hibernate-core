/*
 * Copyright (c) 2012, Sabre Holdings. All Rights Reserved.
 */

package org.hibernate.envers.mappergenerator;

import org.hibernate.envers.entities.mapper.PropertyMapper;
import org.hibernate.envers.entities.mapper.relation.BasicCollectionMapper;
import org.hibernate.envers.entities.mapper.relation.CommonCollectionMapperData;
import org.hibernate.envers.entities.mapper.relation.ListCollectionMapper;
import org.hibernate.envers.entities.mapper.relation.MapCollectionMapper;
import org.hibernate.envers.entities.mapper.relation.MiddleComponentData;
import org.hibernate.envers.entities.mapper.relation.SortedMapCollectionMapper;
import org.hibernate.envers.entities.mapper.relation.SortedSetCollectionMapper;
import org.hibernate.envers.entities.mapper.relation.lazy.proxy.ListProxy;
import org.hibernate.envers.entities.mapper.relation.lazy.proxy.MapProxy;
import org.hibernate.envers.entities.mapper.relation.lazy.proxy.SetProxy;
import org.hibernate.envers.entities.mapper.relation.lazy.proxy.SortedMapProxy;
import org.hibernate.envers.entities.mapper.relation.lazy.proxy.SortedSetProxy;
import org.hibernate.mapping.Collection;
import org.hibernate.type.BagType;
import org.hibernate.type.ListType;
import org.hibernate.type.MapType;
import org.hibernate.type.SetType;
import org.hibernate.type.SortedMapType;
import org.hibernate.type.SortedSetType;
import org.hibernate.type.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class AircrewsCollectionMapperResolver implements CollectionMapperResolver {
    public PropertyMapper resolveCollectionMapper(Collection collection, CommonCollectionMapperData commonCollectionMapperData,
                                                  MiddleComponentData elementComponentData, MiddleComponentData indexComponentData) {
        return collection.isLazy() ?
                resolveNonLazyMapper(collection, commonCollectionMapperData, elementComponentData, indexComponentData) :
                resolveNonLazyMapper(collection, commonCollectionMapperData, elementComponentData, indexComponentData);
    }

    private PropertyMapper resolveNonLazyMapper(Collection collection, CommonCollectionMapperData commonCollectionMapperData,
                                                MiddleComponentData elementComponentData, MiddleComponentData indexComponentData) {
        Type type = collection.getType();
        if (type instanceof SortedSetType) {
            return new SortedSetCollectionMapper(commonCollectionMapperData, TreeSet.class, SortedSetProxy.class,
                    elementComponentData,
                    collection.getComparator());
        } else if (type instanceof SetType) {
            return new EagerBasicCollectionMapper<Set>(commonCollectionMapperData, HashSet.class, SetProxy.class,
                    elementComponentData);
        } else if (type instanceof SortedMapType) {
            // Indexed collection, so indexComponentData is not null.
            return new SortedMapCollectionMapper(commonCollectionMapperData, TreeMap.class, SortedMapProxy.class,
                    elementComponentData, indexComponentData,
                    collection.getComparator());
        } else if (type instanceof MapType) {
            // Indexed collection, so indexComponentData is not null.
            return new MapCollectionMapper<Map>(commonCollectionMapperData, HashMap.class, MapProxy.class,
                    elementComponentData, indexComponentData);
        } else if (type instanceof BagType) {
            return new EagerBasicCollectionMapper<List>(commonCollectionMapperData, ArrayList.class, ListProxy.class,
                    elementComponentData);
        } else if (type instanceof ListType) {
            // Indexed collection, so indexComponentData is not null.
            return new ListCollectionMapper(commonCollectionMapperData, elementComponentData, indexComponentData);
        }
        return null;
    }

    private PropertyMapper resolveLazyMapper(Collection collection, CommonCollectionMapperData commonCollectionMapperData,
                                             MiddleComponentData elementComponentData, MiddleComponentData indexComponentData) {
        Type type = collection.getType();
        if (type instanceof SortedSetType) {
            return new SortedSetCollectionMapper(commonCollectionMapperData, TreeSet.class, SortedSetProxy.class,
                    elementComponentData,
                    collection.getComparator());
        } else if (type instanceof SetType) {
            return new BasicCollectionMapper<Set>(commonCollectionMapperData, HashSet.class, SetProxy.class,
                    elementComponentData);
        } else if (type instanceof SortedMapType) {
            // Indexed collection, so indexComponentData is not null.
            return new SortedMapCollectionMapper(commonCollectionMapperData, TreeMap.class, SortedMapProxy.class,
                    elementComponentData, indexComponentData,
                    collection.getComparator());
        } else if (type instanceof MapType) {
            // Indexed collection, so indexComponentData is not null.
            return new MapCollectionMapper<Map>(commonCollectionMapperData, HashMap.class, MapProxy.class,
                    elementComponentData, indexComponentData);
        } else if (type instanceof BagType) {
            return new BasicCollectionMapper<List>(commonCollectionMapperData, ArrayList.class, ListProxy.class,
                    elementComponentData);
        } else if (type instanceof ListType) {
            // Indexed collection, so indexComponentData is not null.
            return new ListCollectionMapper(commonCollectionMapperData, elementComponentData, indexComponentData);
        }
        return null;
    }
}
