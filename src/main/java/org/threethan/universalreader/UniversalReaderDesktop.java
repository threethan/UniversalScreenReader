package org.threethan.universalreader;

import org.threethan.universalreader.reader.Application;

/** Simply calls Application.main() as a workaround for build issues on some platforms */
public class UniversalReaderDesktop {
    public static void main(String[] args) {
        Application.main(args);
    }
}
