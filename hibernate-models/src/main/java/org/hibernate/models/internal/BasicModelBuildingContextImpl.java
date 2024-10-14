/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.internal;

import org.hibernate.models.serial.internal.StorableContextImpl;
import org.hibernate.models.serial.spi.StorableContext;
import org.hibernate.models.spi.ClassLoading;
import org.hibernate.models.spi.RegistryPrimer;

/**
 * Standard SourceModelBuildingContext implementation
 *
 * @author Steve Ebersole
 */
public class BasicModelBuildingContextImpl extends AbstractModelBuildingContext {
	private final AnnotationDescriptorRegistryStandard descriptorRegistry;
	private final ClassDetailsRegistryStandard classDetailsRegistry;

	public BasicModelBuildingContextImpl(ClassLoading classLoadingAccess) {
		this( classLoadingAccess, null );
	}

	public BasicModelBuildingContextImpl(ClassLoading classLoadingAccess, RegistryPrimer registryPrimer) {
		super( classLoadingAccess );

		this.descriptorRegistry = new AnnotationDescriptorRegistryStandard( this );
		this.classDetailsRegistry = new ClassDetailsRegistryStandard( this );

		primeRegistries( registryPrimer );
	}

	@Override
	public MutableAnnotationDescriptorRegistry getAnnotationDescriptorRegistry() {
		return descriptorRegistry;
	}

	@Override
	public MutableClassDetailsRegistry getClassDetailsRegistry() {
		return classDetailsRegistry;
	}

	@Override
	public StorableContext toStorableForm() {
		return new StorableContextImpl( classDetailsRegistry.classDetailsMap, descriptorRegistry.descriptorMap );
	}
}
