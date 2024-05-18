package org.example;

import org.gedcomx.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.gedcomx.common.URI;
import org.gedcomx.conclusion.Fact;
import org.gedcomx.conclusion.FamilyView;
import org.gedcomx.conclusion.Person;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.fileformat.GedcomxFile;
import org.gedcomx.fileformat.GedcomxFileEntry;
import org.gedcomx.tools.Gedcom2Gedcomx;
import org.gedcomx.types.FactType;
import org.gedcomx.types.RelationshipType;
import org.xml.sax.SAXParseException;
import org.gedcomx.links.Link;

public class CalculateEthnicityBreakdown {
    Gedcomx treeRoot;
    List<Relationship> parentChildRelationships;
    List<String> results = new ArrayList<>();
    Map<String, Double> mathResults = new HashMap<>();

    List<String> americanLocales = Arrays.asList("United States", "USA", "U.S.A", "U.S.",
            "Canada", "Quebec", "Illinois", "Oregon", "Ohio", "Wisconsin", "New York", "Dakota", "Carolina",
            "Tennessee", "Pennsylvania", "Massachusetts", "Missouri", "New Jersey", "America",
            "Kentucky", "NJ", "NH", "VA");

    public CalculateEthnicityBreakdown() {

    }

    public void calculateEthnicityBreakdown() throws SAXParseException, IOException {
        String[] myArgs = new String[]{"-i", "src/main/resources/Anderson-family-tree.ged", "-o", "src/main/resources/mygedx.gedx"};

        Gedcom2Gedcomx.main(myArgs);

        File file = new File("src/main/resources/mygedx.gedx");

        GedcomxFile gxFile = new GedcomxFile(new JarFile(file));
        Iterable<GedcomxFileEntry> entries = gxFile.getEntries();

        List<GedcomxFileEntry> entriesList = new ArrayList<>();

        entries.forEach(entriesList::add);

        treeRoot = (Gedcomx) gxFile.readResource(entriesList.get(0));

        List<Relationship> relationships = new ArrayList<>();
        treeRoot.relationships().forEach(relationships::add);

        parentChildRelationships = new ArrayList<>();
        treeRoot.relationships().filter(relationship -> relationship.getKnownType() == RelationshipType.ParentChild).forEach(
                parentChildRelationships::add
        );

        // get my parents
        String myId = treeRoot.getPersons().get(0).getId();
        URI myUri = new URI("#" + myId);
        Person me = treeRoot.findPerson(myUri);

        List<Relationship> parents = parentChildRelationships.stream()
                .filter(r -> Objects.equals(r.getPerson2().getResource().toString(), myUri.toString()))
                .collect(Collectors.toList());

        if (parents.size() > 2) return;

        parents.forEach(relationship -> findImmigrantAncestors(relationship, 1, 50.0));

      //  findImmigrantAncestors(relationships.get(0), 1, 50.0);
      //  findImmigrantAncestors(relationships.get(1), 1, 50.0);

        System.out.println("\n\n -- Results -- \n");
        results.forEach(System.out::println);

        System.out.println("\n ---- Math results ---- \n");

        for(Map.Entry<String, Double> entry : mathResults.entrySet()){
            System.out.println(String.format("%,.2f", entry.getValue()) + "% " + entry.getKey());
        }

       // mathResults.values().toArray(Double[]);

       // List<Double> sumArray = Arrays.stream().mapTo

        double sum = 0;

        for(Map.Entry<String, Double> entry : mathResults.entrySet()){
            sum = sum + entry.getValue();
        }

        System.out.println("\ntotal: " + String.format("%,.2f", sum) + "%");

    }

    public void findImmigrantAncestors(Relationship relationship, int generation, double pctDna) {
        System.out.println("generation: " + generation + "  |  % dna inherited: " + pctDna + "%");
        if (relationship != null) {
            URI currentPerson = relationship.getPerson1().getResource();
            Person person = treeRoot.findPerson(currentPerson);
            String name = getName(treeRoot.findPerson(currentPerson));
            System.out.println("looking for parents of " + name + "...");
            String birthplace = null;

            try {
                birthplace = getBirthplace(person);
            } catch (Exception e) {
                System.out.println("\tIssue getting birthplace for " + name);
            }

            List<Relationship> parents = parentChildRelationships.stream()
                    .filter(r -> Objects.equals(r.getPerson2().getResource().toString(), relationship.getPerson1().getResource().toString()))
                    .collect(Collectors.toList());

            if (birthplace != null) {
                String americanBirthplace = americanLocales.stream().filter(birthplace::contains)
                        .findAny()
                        .orElse(null);

                if (americanBirthplace == null || generation > 9 || parents.isEmpty()) {
                    // we found one
                    String[] txt = birthplace.split(",");
                    String country = txt[txt.length - 1].trim();
                    String resultString = "\t" + pctDna + "% " + country.toUpperCase() + " (" + getName(person) + ")";
                    System.out.println(resultString);
                    results.add(resultString);

                    if (mathResults.containsKey(country)) {
                        // add
                        Double existingPctDna = mathResults.get(country);
                        mathResults.put(country, pctDna + existingPctDna);
                    } else {
                        mathResults.put(country, pctDna);
                    }
                    return;
                }
            }

            // we didnt find one. keep looking
            // search mom and dad
            if (parents.size() >= 2) {
                URI momUri = parents.get(0).getPerson1().getResource();
                URI dadUri = parents.get(1).getPerson1().getResource();
                System.out.println("\t" + getName(person) + "'s parents: " + getName(treeRoot.findPerson(momUri)) + " & "
                        + getName(treeRoot.findPerson(dadUri)));

                parents.forEach(parent -> findImmigrantAncestors(parent, generation + 1, pctDna / 2));
            }

//            if (parents.size() >= 2) {
//                URI momUri = parents.get(0).getPerson1().getResource();
//                URI dadUri = parents.get(1).getPerson1().getResource();
//                System.out.println("\t" + getName(person) + "'s parents: " + getName(treeRoot.findPerson(momUri)) + " & "
//                        + getName(treeRoot.findPerson(dadUri)));

               // generation = generation + 1;
//                findImmigrantAncestors(parents.get(0), generation + 1, pctDna / 2);
//                findImmigrantAncestors(parents.get(1), generation + 1, pctDna / 2);
           // }
        }
    }

    public String getName(Person person) {
        return person.getName().getNameForms().get(0).getFullText();
    }

    public String getBirthplace(Person person) {
        return person.getFirstFactOfType(FactType.Birth).getPlace().getOriginal();
    }
}
