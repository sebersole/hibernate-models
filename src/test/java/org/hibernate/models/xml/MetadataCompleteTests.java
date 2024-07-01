/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.models.xml;

import org.hibernate.models.SourceModelTestHelper;
import org.hibernate.models.internal.SourceModelBuildingContextImpl;
import org.hibernate.models.orm.JpaAnnotations;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.ClassDetailsRegistry;
import org.hibernate.models.spi.FieldDetails;
import org.hibernate.models.spi.MutableMemberDetails;

import org.junit.jupiter.api.Test;

import org.jboss.jandex.Index;

import jakarta.persistence.Transient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for expected patterns for handling of "metadata complete" XML mappings.
 *
 * @author Steve Ebersole
 */
public class MetadataCompleteTests {
	@Test
	void testIt() {
		final SourceModelBuildingContextImpl buildingContext = SourceModelTestHelper.createBuildingContext(
				(Index) null,
				SimpleEntity.class
		);

		final ClassDetailsRegistry classDetailsRegistry = buildingContext.getClassDetailsRegistry();
		final ClassDetails classDetails = classDetailsRegistry.getClassDetails( SimpleEntity.class.getName() );

		// A metadata-complete XML mapping means that all "attributes" not explicitly listed in
		// the XML should be ignored.  To support this we will first apply `@Transient` to all
		// persistable members of the class.  Then, as we process the XML and "see" an attribute
		// we will remove that annotation.

		// mark them all transient...
		classDetails.forEachPersistableMember( (member) -> ( (MutableMemberDetails) member ).applyAnnotationUsage( JpaAnnotations.TRANSIENT, buildingContext ) );

		checkFieldIsTransient( classDetails.findFieldByName( "id" ), true );
		checkFieldIsTransient( classDetails.findFieldByName( "name" ), true );
		checkFieldIsTransient( classDetails.findFieldByName( "somethingElse" ), true );

		// the XML lists just `id` and `name`...
		( (MutableMemberDetails) classDetails.findFieldByName( "id" ) ).removeAnnotationUsage( JpaAnnotations.TRANSIENT );
		( (MutableMemberDetails) classDetails.findFieldByName( "name" ) ).removeAnnotationUsage( JpaAnnotations.TRANSIENT );

		checkFieldIsTransient( classDetails.findFieldByName( "id" ), false );
		checkFieldIsTransient( classDetails.findFieldByName( "name" ), false );
		checkFieldIsTransient( classDetails.findFieldByName( "somethingElse" ), true );
	}

	private void checkFieldIsTransient(FieldDetails fieldDetails, boolean expectation) {
		assertThat( fieldDetails.hasDirectAnnotationUsage( Transient.class ) ).isEqualTo( expectation );
	}
}
