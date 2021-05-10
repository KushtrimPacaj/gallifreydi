# gallifreydi
------
Simple dependency injection "framework/library" implemented in Java, as part of my Bachelor's Degree final thesis.

Sample usages:


````java
 public static void main(String[] args) {
        Injector.init(PACKAGE_NAME);

        //Get a generic coffe maker
        CoffeeMaker firstCoffeMaker = Injector.getEntity(CoffeeMaker.class);

        //Get a specific coffe maker that makes nescafe
        CoffeeMaker nescafeMaker = Injector.getEntity("nescafe", CoffeeMaker.class);

        // Create a coffe maker, and inject all it's dependencies.
        CoffeeMaker espressoMaker = new EspressoMaker();
        Injector.inject(espressoMaker);


    }
````          



#### Where does the name come from:
Gallifrey is a fictional planet in the long-running British science fiction television series Doctor Who. Watch it!


#### LICENSE
KushtrimPacaj/gallifreydi is licensed under the **GNU Lesser General Public License v2.1**
Primarily used for software libraries, the GNU LGPL requires that derived works be licensed under the same license, but works that only link to it do not fall under this restriction.
