/*
 * (c) Copyright 2013 Just Software AG
 * 
 * Created on 16.10.2013 by Jan Burkhardt (jan.burkhardt@justsoftwareag.com)
 * 
 * This file contains unpublished, proprietary trade secret information of
 * Just Software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * Just Software AG.
 */
package de.justsoftware.toolbox.model;

import java.util.Arrays;

import javax.annotation.Nonnull;

/**
 * extend this class to get equals and hashCode implementation based on your class and the supplied members
 * 
 * @author Jan Burkhardt (jan.burkhardt@justsoftwareag.com) (initial creation)
 */
public class AbstractImmutableEqualsObject {

    private final Object[] _hashObjects;
    private final int _hashCode;

    protected AbstractImmutableEqualsObject(@Nonnull final Object... hashObjects) {
        _hashObjects = hashObjects;
        _hashCode = Arrays.hashCode(hashObjects);
    }

    @Override
    public final int hashCode() {
        return getClass().getName().hashCode() ^ _hashCode;
    }

    @Override
    public final boolean equals(final Object obj) {
        return this == obj || obj != null && getClass().equals(obj.getClass())
                && Arrays.equals(_hashObjects, ((AbstractImmutableEqualsObject) obj)._hashObjects);
    }

}
