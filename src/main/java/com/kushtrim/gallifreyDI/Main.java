package com.kushtrim.gallifreyDI;

import com.kushtrim.gallifreyDI.library.Injector;
import com.kushtrim.gallifreyDI.tests.CoffeeMaker;
import com.kushtrim.gallifreyDI.tests.EspressoMaker;

/**
 * Created by Kushtrim on 05.01.2016.
 */
public class Main {
    public static final String PACKAGE_NAME = "com.kushtrim.gallifreyDI";

    public static void main(String[] args) {
        Injector.init(PACKAGE_NAME);

        //marrim instacë të coffeeMaker
        CoffeeMaker firstCoffeMaker = Injector.getEntity(CoffeeMaker.class);

        //marrim një lloj specifik të coffeeMaker
        CoffeeMaker nescafeMaker = Injector.getEntity("nescafe", CoffeeMaker.class);

        // nëse e kemi të krijuar, vetëm e injektim
        CoffeeMaker espressoMaker = new EspressoMaker();
        Injector.inject(espressoMaker);


    }
}
