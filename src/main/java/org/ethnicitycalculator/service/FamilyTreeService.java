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
        if (relationship != null) {
            URI ancestorUri = relationship.getPerson1().getResource();
            FamilyMember ancestor = new FamilyMember(familyTree.findPerson(ancestorUri));
            String ancestorName = ancestor.getFullName();

            System.out.println("looking for parents of " + ancestorName + "...");

            String birthplace = null;
            try {
                birthplace = ancestor.getBirthplace();
            } catch (Exception e) {
                System.out.println("\tIssue getting birthplace for " + ancestorName);
            }

            List<Relationship> parents = parentChildRelationships.stream()
                    .filter(r -> Objects.equals(r.getPerson2().getResource().toString(), relationship.getPerson1().getResource().toString()))
                    .toList();

            if (birthplace != null) {
                String americanBirthplace = AmericanBirthplaces.getAll().stream().filter(birthplace::contains)
                        .findAny()
                        .orElse(null);

                if (americanBirthplace == null || generation > 9 || parents.isEmpty()) {
                    // we found one
                    String[] txt = birthplace.split(",");
                    String country = txt[txt.length - 1].trim();
                    String resultString = "\t" + pctDna + "% " + country.toUpperCase() + " (" + ancestor.getFullName() + ")";
                    System.out.println(resultString);
                    results.add(resultString);

                    if (mathResults.containsKey(country)) {
                        // add
                        mathResults.compute(country, (k, existingPctDna) -> pctDna + existingPctDna);
                    } else {
                        mathResults.put(country, pctDna);
                    }
                    return;
                }
            }

            // Ancestor's birthplace is in America. Check their parents.
            String momName = "none";
            String dadName = "none";
            if (!parents.isEmpty()) {
                URI momUri = parents.get(0).getPerson1().getResource();
                FamilyMember mom = new FamilyMember(familyTree.findPerson(momUri));
                momName = mom.getFullName();
            }
            if (parents.size() >= 2) {
                URI dadUri = parents.get(1).getPerson1().getResource();
                FamilyMember dad = new FamilyMember(familyTree.findPerson(dadUri));
                dadName = dad.getFullName();
            }

            if (parents.size() > 2) {
                URI thirdParentUri = parents.get(2).getPerson1().getResource();
                FamilyMember parent3 = new FamilyMember(familyTree.findPerson(thirdParentUri));
                dadName = parent3.getFullName();
                System.out.println(ancestorName + "has more than 2 parents bro wtf. 3rd parent name: "
                        + parent3);
            }

            System.out.println("\t" + ancestor.getFullName() + "'s parents: " + momName + " & "
                    + dadName);
            parents.forEach(parent -> findImmigrantAncestors(parent, generation + 1, pctDna / 2));
        }
    }
}
