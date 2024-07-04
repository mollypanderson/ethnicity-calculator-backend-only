package org.ethnicitycalculator.service;

import org.ethnicitycalculator.model.FamilyMember;
import org.ethnicitycalculator.util.AmericanBirthplaces;
import org.gedcomx.Gedcomx;
import org.gedcomx.common.URI;
import org.gedcomx.conclusion.Relationship;
import org.gedcomx.types.RelationshipType;

import java.util.*;
import java.util.stream.Collectors;

public class FamilyTreeService {
    Gedcomx familyTree;
    List<Relationship> parentChildRelationships = new ArrayList<>();
    List<String> results = new ArrayList<>();
    Map<String, Double> mathResults = new HashMap<>();

    public FamilyTreeService(Gedcomx familyTree) {
        this.familyTree = familyTree;
    }

    public List<Relationship> getRootPersonParents() {
        //  parentChildRelationships = new ArrayList<>();
        familyTree.relationships().filter(relationship -> relationship.getKnownType() == RelationshipType.ParentChild).forEach(
                parentChildRelationships::add
        );
        String myId = familyTree.getPersons().get(0).getId();
        URI myUri = new URI("#" + myId);

        return parentChildRelationships.stream()
                .filter(r -> Objects.equals(r.getPerson2().getResource().toString(), myUri.toString()))
                .collect(Collectors.toList());
    }

    public Map<String, Double> findImmigrantAncestors(List<Relationship> parents) {
        parents.forEach(relationship -> this.findImmigrantAncestors(relationship, 1, 50.0));
        // resultsList.add(results);
        return mathResults;
    }

    public void findImmigrantAncestors(Relationship relationship, int generation, double pctDna) {
        System.out.println("generation: " + generation + "  |  % dna inherited: " + pctDna + "%");

        if (relationship == null || relationship.getPerson1() == null) {
            System.out.println("There is no ancestor here. AKA one parent is missing. Setting their % to Unknown. ");
            generateSingleResult(pctDna, "Unknown", "Unknown person");
            return;
        }

        if (relationship != null) {
            URI ancestorUri = relationship.getPerson1().getResource();
            FamilyMember ancestor = new FamilyMember(familyTree.findPerson(ancestorUri));
            String ancestorName = ancestor.getFullName();

            System.out.println("Processing " + ancestorName + "...");

            String birthplace = null;
            try {
                birthplace = ancestor.getBirthplace();
            } catch (Exception e) {
                birthplace = "Unknown";
            }

            List<Relationship> parents = new ArrayList<>(parentChildRelationships.stream()
                    .filter(r -> Objects.equals(r.getPerson2().getResource().toString(), relationship.getPerson1().getResource().toString()))
                    .toList());

            if (birthplace != null) {
                String americanBirthplace = AmericanBirthplaces.getAll().stream().filter(birthplace::contains)
                        .findAny()
                        .orElse(null);

                if (americanBirthplace == null || birthplace.equals("Unknown") || generation > 9 || parents.isEmpty()) {
                    // we found one
                    String[] txt = birthplace.split(",");
                    String country = txt[txt.length - 1].trim();
                    generateSingleResult(pctDna, country, ancestor.getFullName());
                    return;
                }
            }

            // Ancestor's birthplace is in America. Check their parents.
            String momName = "none";
            String dadName = "none";
            if (!parents.isEmpty()) {
                URI momUri = parents.get(0).getPerson1().getResource();
                FamilyMember mom = new FamilyMember(familyTree.findPerson(momUri));
                dadName = mom.getFullName();
            }
            if (parents.size() >= 2) {
                URI dadUri = parents.get(1).getPerson1().getResource();
                FamilyMember dad = new FamilyMember(familyTree.findPerson(dadUri));
                momName = dad.getFullName();
            }
            if (parents.size() == 1) {
                System.out.println("Only 1 parent found. Setting parent2 to 'unknown'. ");
                parents.add(new Relationship());
                momName = "Unknown";
            }

            if (parents.size() > 2) {
                URI thirdParentUri = parents.get(2).getPerson1().getResource();
                FamilyMember parent3 = new FamilyMember(familyTree.findPerson(thirdParentUri));
                String thirdParentName = parent3.getFullName();
                System.out.println(ancestorName + " has more than 2 parents bro wtf. 3rd parent name: "
                        + thirdParentName);

                if (parents.size() > 3) {
                    URI fourthParentUri = parents.get(3).getPerson1().getResource();
                    FamilyMember parent4 = new FamilyMember(familyTree.findPerson(fourthParentUri));
                    String fourthParentName = parent4.getFullName();
                    System.out.println(ancestorName + " 4th parent: "
                            + fourthParentName + (" (will ignore)"));
                }
                parents.subList(2, parents.size()).clear();
            }

            System.out.println("\t" + ancestor.getFullName() + "'s parents: " + momName + " & "
                    + dadName);
            parents.forEach(parent -> findImmigrantAncestors(parent, generation + 1, pctDna / 2));
        }
    }

    private void generateSingleResult(double pctDna, String country, String ancestorFullName) {
        String resultString = "\t" + pctDna + "% " + country.toUpperCase() + " (" + ancestorFullName + ")\n";
        System.out.println(resultString);
        results.add(resultString);

        if (mathResults.containsKey(country)) {
            // add
            mathResults.compute(country, (k, existingPctDna) -> pctDna + existingPctDna);
        } else {
            mathResults.put(country, pctDna);
        }
    }
}
