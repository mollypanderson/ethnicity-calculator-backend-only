package org.ethnicitycalculator.util;

import org.gedcomx.Gedcomx;
import org.gedcomx.fileformat.GedcomxFile;
import org.gedcomx.fileformat.GedcomxFileEntry;
import org.gedcomx.tools.Gedcom2Gedcomx;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public class GedcomFileProcessor {

    public static void convertToGedcomx(String filepath) throws SAXParseException, IOException {
        String[] myArgs = new String[]{"-i", filepath, "-o", "src/main/resources/mygedx.gedx"};
        Gedcom2Gedcomx.main(myArgs);
    }

    public static Gedcomx getGedcomTree(File file) throws IOException {
        GedcomxFile gxFile = new GedcomxFile(new JarFile(file));
        Iterable<GedcomxFileEntry> entries = gxFile.getEntries();
        List<GedcomxFileEntry> entriesList = new ArrayList<>();
        entries.forEach(entriesList::add);
        return (Gedcomx) gxFile.readResource(entriesList.get(0));
    }
}
