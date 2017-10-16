/*
 * (c) Copyright 2017 Just Software AG
 *
 * Created on 14.02.2017 by wolfgang
 *
 * This file contains unpublished, proprietary trade secret information of
 * Just Software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * Just Software AG.
 */
package de.justsoftware.toolbox.http;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Charsets;

/**
 * Set Content-Disposition header as described in RFC 6266.
 * 
 * @author wolfgang (initial creation)
 */
@ParametersAreNonnullByDefault
public class ContentDispositionUtil {

    @Nonnull
    private static String encodeURIComponent(final String s) {
        try {
            return URLEncoder.encode(s, Charsets.UTF_8.name()).replace("+", "%20");
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Nonnull
    private static String getContentDispositionValue(final String fileName) {
        return "attachment; filename*=UTF-8''" + encodeURIComponent(fileName);
    }

    /**
     * Set a Content-Disposition header. This method is header implementation agnostic. Simply pass the setHeader method. For
     * Example:
     * 
     * ContentDispositionUtil.setContentDispositionAttachment(request::setHeader, "fileName.txt");
     */
    public static void setContentDispositionAttachment(final BiConsumer<String, String> setHeader, final String fileName) {
        setHeader.accept("Content-Disposition", getContentDispositionValue(fileName));
    }

}
