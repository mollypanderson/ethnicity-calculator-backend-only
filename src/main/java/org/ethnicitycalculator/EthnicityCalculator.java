package org.ethnicitycalculator;

import org.ethnicitycalculator.service.FamilyTreeService;
import org.ethnicitycalculator.util.GedcomFileProcessor;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.gedcomx.Gedcomx;
import org.gedcomx.conclusion.Relationship;
import org.xml.sax.SAXParseException;
import org.apache.commons.io.FilenameUtils;

public class EthnicityCalculator {

    public static void main(String[] args) throws IOException, SAXParseException {

        //expect arg like: src/main/resources/Anderson-family-tree.ged
        String gedcomInputFilepath = args[0];
        String fileExtension = FilenameUtils.getExtension(gedcomInputFilepath);
        if (!(fileExtension.equals("ged") || fileExtension.equals("gedx"))) {
            System.out.println("Invalid file extension. Must be either .ged or .gedx. ");
            return;
        }
        if (fileExtension.equals("ged")) {
            GedcomFileProcessor.convertToGedcomx(gedcomInputFilepath);
        }
        File file = new File("src/main/resources/mygedx.gedx");

        Gedcomx familyTree = GedcomFileProcessor.getGedcomTree(file);
        FamilyTreeService familyTreeService = new FamilyTreeService(familyTree);

        List<Relationship> parents = familyTreeService.getRootPersonParents();
        if (parents.size() > 2) {
            System.out.println("Detected " + parents.size() + " parents. Calculation will proceed, " +
                    " but the ethnicity breakdown will add up to over 100 because it is unknown" +
                    " which two parents are biological.");
        }

        //parents.remove(1);
        Map<String, Double> results = familyTreeService.findImmigrantAncestors(parents);

        Map<String, Double> sortedResults = results.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        prettyPrintResults(sortedResults);
    }

    public static void prettyPrintResults(Map<String, Double> mathResults) {
     //   System.out.println("\n\n -- Results -- \n");
     //   results.forEach(System.out::println);
        System.out.println("\n ---- Math results ---- \n");
        for(Map.Entry<String, Double> entry : mathResults.entrySet()){
            System.out.println(String.format("%,.2f", entry.getValue()) + "%," + entry.getKey());
        }
        double sum = 0;
        for(Map.Entry<String, Double> entry : mathResults.entrySet()){
            sum = sum + entry.getValue();
        }
        System.out.println("\ntotal: " + String.format("%,.2f", sum) + "%");
    }
}
