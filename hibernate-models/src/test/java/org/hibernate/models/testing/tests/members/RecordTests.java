/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.testing.tests.members;

import org.hibernate.models.spi.AnnotationTarget;
import org.hibernate.models.spi.ClassDetails;
import org.hibernate.models.spi.RecordComponentDetails;
import org.hibernate.models.spi.SourceModelBuildingContext;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.models.testing.TestHelper.createModelContext;

/**
 * @author Steve Ebersole
 */
public class RecordTests {
	@Test
	void testRecords() {
		final SourceModelBuildingContext buildingContext = createModelContext( Data.class );

		final ClassDetails classDetails = buildingContext
				.getClassDetailsRegistry()
				.findClassDetails( Data.class.getName() );
		assertThat( classDetails ).isNotNull();
		assertThat( classDetails.getFields() ).hasSize( 2 );
		assertThat( classDetails.getRecordComponents() ).hasSize( 2 );

		for ( RecordComponentDetails recordComponent : classDetails.getRecordComponents() ) {
			assertThat( recordComponent.getDeclaringType() ).isSameAs( classDetails );
			assertThat( recordComponent.isPersistable() ).isTrue();
			assertThat( recordComponent.getKind() ).isEqualTo( AnnotationTarget.Kind.RECORD_COMPONENT );
			assertThat( recordComponent.resolveAttributeName() ).isEqualTo( recordComponent.getName() );
		}
	}

	public record Data(Integer key, String name) {
	}
}
