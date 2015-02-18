/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2013, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
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
package org.hibernate.envers.internal.entities.mapper.relation.component;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.internal.entities.EntityInstantiator;
import org.hibernate.envers.internal.entities.mapper.relation.MiddleIdData;
import org.hibernate.envers.internal.tools.query.Parameters;

import java.util.Map;

/**
 * @author Adam Warski (adam at warski dot org)
 */
public final class MiddleRelatedComponentMapper implements MiddleComponentMapper {
    private final MiddleIdData relatedIdData;

	public MiddleRelatedComponentMapper(MiddleIdData relatedIdData) {
		this.relatedIdData = relatedIdData;
	}

	@Override
	public Object mapToObjectFromFullMap(
			EntityInstantiator entityInstantiator, Map<String, Object> data,
			Object dataObject, Number revision) {
		return entityInstantiator.createInstanceFromVersionsEntity( relatedIdData.getEntityName(), data, revision );
	}

	@Override
	public void mapToMapFromObject(
			SessionImplementor session,
			Map<String, Object> idData,
			Map<String, Object> data,
			Object obj) {
		relatedIdData.getPrefixedMapper().mapToMapFromEntity( idData, obj );
	}

    @Override
    public void addMiddleEqualToQuery(
            Parameters parameters,
            String idPrefix1,
            String prefix1,
            String idPrefix2,
            String prefix2) {
        relatedIdData.getPrefixedMapper().addIdsEqualToQuery( parameters, idPrefix1, idPrefix2 );
    }


    public MiddleIdData getRelatedIdData() {
        return relatedIdData;
    }
}
