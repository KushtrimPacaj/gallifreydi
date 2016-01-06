package com.kushtrim.gallifreyDI.tests;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Kushtrim on 04.01.2016.
 */
@Named("DA")
public class DependencyA implements DependencyAInterface {

    @Inject
    SubDependencyA subDependencyA;

    public void setSubDependencyA(SubDependencyA subDependencyA) {
        this.subDependencyA = subDependencyA;
    }

    public void checkIfDependencySet() {
        System.out.println("subDependencyA is set ?" + (subDependencyA != null));
    }

}
