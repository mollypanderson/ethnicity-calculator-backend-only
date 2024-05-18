package org.example;

import java.io.IOException;
import org.xml.sax.SAXParseException;

public class Main {
    public static void main(String[] args) throws IOException, SAXParseException {
        CalculateEthnicityBreakdown calc = new CalculateEthnicityBreakdown();
        calc.calculateEthnicityBreakdown();
    }
}