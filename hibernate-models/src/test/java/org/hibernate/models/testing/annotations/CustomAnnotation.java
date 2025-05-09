/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright: Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.models.testing.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Steve Ebersole
 */
@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
@CustomMetaAnnotation( someValue = "abc" )
@Inherited
@Repeatable( CustomAnnotations.class )
public @interface CustomAnnotation {
}
