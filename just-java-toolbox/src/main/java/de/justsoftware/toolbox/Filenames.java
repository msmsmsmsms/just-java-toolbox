package de.justsoftware.toolbox;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * utilities for filenames
 * 
 * @author Jan Burkhardt (jan.burkhardt@justsoftwareag.com) (initial creation)
 */
public class Filenames {

    /**
     * limits a filename and tries to preserve extension and filename parts
     */
    @Nonnull
    public static String limitFilenameLength(@Nonnull final String filename, final int limit) {
        int totalCharsToDelete = filename.length() - limit;
        if (totalCharsToDelete <= 0) {
            return filename;
        }
        final Iterable<String> split = Splitter.on('.').split(filename);

        int totalRemovableChars = 0;
        //CSOFF:MagicNumber
        int minPartLength = 6;
        while (minPartLength > 3 && totalRemovableChars < totalCharsToDelete) {
            //CSON:.
            // reduction need to be placed here, so the last used is the current value, a for loop would decrese first and then check
            minPartLength--;
            totalRemovableChars = 0;
            for (final String part : split) {
                if (part.length() > minPartLength) {
                    final int removableChars = part.length() - minPartLength;
                    totalRemovableChars += removableChars;
                }
            }
        }
        if (totalRemovableChars < totalCharsToDelete) {
            return filename.substring(0, limit);
        }
        final List<String> limitedParts = Lists.newLinkedList();
        for (final String part : split) {
            // can't reduce part or no need to reduce anymore
            if (part.length() < minPartLength || totalCharsToDelete <= 0) {
                limitedParts.add(part);
            } else {
                final int removableChars = part.length() - minPartLength;
                // Math.min -> prevent to delete more chars than available
                // + totalRemovableChars / 2 -> round the result
                // removableChars / totalRemovableChars -> proportion how many need to could be deleted in this part
                final int charsToDelete =
                        Math.min(removableChars, (removableChars * totalCharsToDelete + totalRemovableChars / 2)
                                / totalRemovableChars);
                limitedParts.add(part.substring(0, part.length() - charsToDelete));
                totalCharsToDelete -= charsToDelete;
                totalRemovableChars -= removableChars;
            }
        }

        return Joiner.on('.').join(limitedParts);
    }

    /**
     * replaces ~ by the users home directory for easier configuration
     */
    @Nonnull
    public static String replaceTildeByUserHome(@Nonnull final String s) {
        return s.replace("~", System.getProperty("user.home"));
    }
}
