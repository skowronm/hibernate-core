package org.hibernate.envers.query.propertyinitializer.impl;

import org.hibernate.envers.query.propertyinitializer.PropertyInitializer;

public class NullPropertyInitializer implements PropertyInitializer {

	@Override
	public <T> T getInitialValueForProperty(Class<T> propertyClass) {
		return null;
	}
}
