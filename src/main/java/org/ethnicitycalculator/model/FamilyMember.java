package org.ethnicitycalculator.model;

import org.gedcomx.conclusion.Person;
import org.gedcomx.types.FactType;

public class FamilyMember {
    Person person;

    public FamilyMember(Person person) {
        this.person = person;
    }

    public String getFullName() {
        return person.getName().getNameForms().get(0).getFullText();
    }

    public String getBirthplace() {
        return person.getFirstFactOfType(FactType.Birth).getPlace().getOriginal();
    }
}
