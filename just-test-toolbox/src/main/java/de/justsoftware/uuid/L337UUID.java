/*
 * (c) Copyright 2016 Just Software AG
 * 
 * Created on 18.01.2016 by Jan Burkhardt (jan.burkhardt@just.social)
 * 
 * This file contains unpublished, proprietary trade secret information of
 * Just Software AG. Use, transcription, duplication and
 * modification are strictly prohibited without prior written consent of
 * Just Software AG.
 */
package de.justsoftware.uuid;

import java.util.Random;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.primitives.UnsignedLong;

/**
 * utility to create {@link UUID}s which are more "readable" than fully random generated {@link UUID}s
 * 
 * @author Jan Burkhardt (jan.burkhardt@just.social) (initial creation)
 */
@ParametersAreNonnullByDefault
public class L337UUID {

    private static final int HEX_RADIX = 16;
    private static final int LONG_HEX_DIGITS = 16;

    private static final Random RANDOM = new Random();

    @Nonnull
    public static UUID l337RandomUUID(final String s) {
        final StringBuilder sb = new StringBuilder();
        for (final char ch : s.toCharArray()) {
            sb.append(convert(ch));
        }
        final String string = sb
                .append("00")
                .append(Long.toHexString(RANDOM.nextLong()))
                .toString();
        final String l33tHex = string.substring(0, Math.min(string.length(), LONG_HEX_DIGITS));
        return new UUID(UnsignedLong.valueOf(l33tHex, HEX_RADIX).longValue(), RANDOM.nextLong());
    }

    //CSOFF: NCSS|CyclomaticComplexity|MethodLength
    @Nonnull
    private static String convert(final char ch) {
        //CSON: .
        switch (Character.toLowerCase(ch)) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return String.valueOf(ch);
            case 'g':
                return "9";
            case 'h':
                return "4";
            case 'i':
            case 'j':
                return "1";
            case 'k':
                return "1c";
            case 'l':
                return "1";
            case 'm':
                return "3";
            case 'n':
                return "2";
            case 'o':
                return "0";
            case 'p':
                return "d";
            case 'q':
                return "9";
            case 'r':
                return "12";
            case 's':
                return "5";
            case 't':
                return "7";
            case 'u':
            case 'v':
                return "2";
            case 'w':
                return "3";
            case 'x':
                return "8";
            case 'y':
                return "9";
            case 'z':
                return "2";
            case 'ä':
                return "a";
            case 'ü':
                return "2";
            case 'ö':
                return "0";
            case 'ß':
                return "13";
            default:
                return "8";
        }
    }

}
