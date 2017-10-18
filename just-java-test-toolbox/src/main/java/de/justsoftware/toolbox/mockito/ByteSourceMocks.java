package de.justsoftware.toolbox.mockito;

import java.io.IOException;

import javax.annotation.Nonnull;

import org.mockito.Mockito;

import com.google.common.io.ByteSource;

/**
 * Helper class for mocking ByteSources
 *
 * @author Christian Ewers <christian.ewers@just.social> (initial creation)
 */
public final class ByteSourceMocks {

    @Nonnull
    public static ByteSource mockByteSource(final long filesize) {
        final ByteSource bs = Mockito.mock(ByteSource.class);
        try {
            Mockito.doReturn(Long.valueOf(filesize)).when(bs).size();
        } catch (final IOException e) {
            throw new IllegalStateException("io exception during mocking should not happen");
        }
        return bs;
    }

}
