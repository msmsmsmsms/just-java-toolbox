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
