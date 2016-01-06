package com.kushtrim.gallifreyDI.tests;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Kushtrim on 04.01.2016.
 */

public class CoffeeMaker {

    @Inject
    @Named("DA")
    DependencyAInterface dependencyA;

    public CoffeeMaker() {

    }

    public void setDependencyA(DependencyAInterface dependencyA) {
        this.dependencyA = dependencyA;
    }

    public void checkIfDependencySet() {
        System.out.println("dependencyA is set ?" + (dependencyA != null));
        if (dependencyA != null) {
            dependencyA.checkIfDependencySet();
        }
    }
}
