package org.hibernate.envers.query.propertyinitializer.impl;

import org.hibernate.envers.query.propertyinitializer.PropertyInitializer;

public class ValuePropertyInitializer implements PropertyInitializer {
	private final Object value;

	public ValuePropertyInitializer(Object value) {
		this.value = value;
	}

	@Override
	public <T> T getInitialValueForProperty(Class<T> propertyClass) {
		//noinspection unchecked
		return (T) value;
	}
}
