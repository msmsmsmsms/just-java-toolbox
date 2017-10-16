/*
 * (c) Copyright 2017 Just Software AG
 *
 * Created on 15.02.2017 by wolfgang
 *
 * This file contains unpublished, proprietary trade secret information of
 * Just Software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * Just Software AG.
 */
package de.justsoftware.toolbox.http;

import java.util.function.BiConsumer;

import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * Test ContentDispositionUtil.
 * 
 * @author wolfgang (initial creation)
 */
@Test
public class ContentDispositionUtilTest {

    public void setContentDispositionAttachmentShoultQuoteFilename() {
        @SuppressWarnings("unchecked")
        final BiConsumer<String, String> setHeader = Mockito.mock(BiConsumer.class);

        ContentDispositionUtil.setContentDispositionAttachment(setHeader, "Hää? 🤔");

        Mockito.verify(setHeader).accept("Content-Disposition",
                "attachment; filename*=UTF-8''H%C3%A4%C3%A4%3F%20%F0%9F%A4%94");
    }
}
