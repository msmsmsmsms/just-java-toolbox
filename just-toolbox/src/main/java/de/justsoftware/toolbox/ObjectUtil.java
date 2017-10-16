/*
 * (c) Copyright 2017 Just Software AG
 *
 * Created on 30.06.2017 by wolfgang
 *
 * This file contains unpublished, proprietary trade secret information of
 * Just Software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * Just Software AG.
 */
package de.justsoftware.toolbox;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Utilities for objects.
 * 
 * @author wolfgang (initial creation)
 */
public class ObjectUtil {

    /**
     * use this instead of {@link Preconditions#checkNotNull()} only if you can not change the nullness
     * of your parameter i.e. in an overridden method.
     * This is because {@link Preconditions#checkNotNull()} is annotaded {@link Nonnull}!
     */
    @Nonnull
    @SuppressFBWarnings(value = "NP_PARAMETER_MUST_BE_NONNULL_BUT_MARKED_AS_NULLABLE",
            justification = "we need a way to throw NPEs in situations where fb thinks nullable but it will never happen")
    public static <T> T checkNotNull(@Nullable final T reference) {
        return Preconditions.checkNotNull(reference);
    }
}
