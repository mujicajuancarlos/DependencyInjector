## Lightweight Dependency Injector
[![Build Status](https://travis-ci.org/ljacqu/DependencyInjector.svg?branch=master)]
(https://travis-ci.org/ljacqu/DependencyInjector) [![Coverage Status]
(https://coveralls.io/repos/github/ljacqu/DependencyInjector/badge.svg?branch=master)](https://coveralls.io/github/ljacqu/DependencyInjector?branch=master) [![Dependency Status]
(https://www.versioneye.com/user/projects/5768397dfdabcd004d3fcbd1/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5768397dfdabcd004d3fcbd1) [![Codacy Badge]
(https://api.codacy.com/project/badge/Grade/8b2ad90b769b45a892dcb4621ce484d6)](https://www.codacy.com/app/ljacqu/DependencyInjector?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ljacqu/DependencyInjector&amp;utm_campaign=Badge_Grade)

Simple but customizable dependency injector for Java 1.7 or above.


### Why use it
- Very lightweight (only requires `javax.inject` dependency)
- Allows gradual transition to injection for existing projects
- You can implement your own injection methods
- Support for projects with optional dependencies

### Integrating it
Using Maven, you can get the injector by adding this to your pom.xml:

```xml
<dependency>
    <groupId>ch.jalu</groupId>
    <artifactId>injector</artifactId>
    <version>0.1</version>
</dependency>
```

### Simple example
By default, the injector supports **constructor injection** and **field injection**.
Consider the following class skeletons:

```java
public class Settings {
  // regular class
}

public class Messages {
  private File messagesFile;

  @Inject
  Messages(Settings settings) {
    messagesFile = new File(settings.getLanguage() + ".txt");
  }
}

public class CalculationService {
  @Inject
  private Messages messages;
  
  @Inject
  private RoundingService roundingService;
}

public class RoundingService {
  private int precision;

  @Inject
  RoundingService(Settings settings) {
    precision = settings.getPrecision();
  }
}
```

At the startup of the application, we might only care about getting an instance of `CalculationService`. All other
classes are required by it to run, but we don't immediately care about them. With the injector, we don't have to
deal with those classes and can just retrieve what we actually want:

```java
public class MyApp {

  public static void main(String... args) {
    Injector injector = new InjectorBuilder().addDefaultHandlers("com.example.my.project").create();
    CalculationService calcService = injector.getSingleton(CalculationService.class);
    calcService.performCalculation();
  }
}
```

... That's all! No need to deal with creating any other classes, but you still have a setup that allows you to easily
unit test or switch a component.

--> Full, runnable example can be found [here](https://github.com/ljacqu/DependencyInjector/tree/master/src/test/java/ch/jalu/injector/demo).

### Handlers
You may implement your own logic to instantiate a class and to resolve dependencies. This allows you for example to 
implement specific behavior for custom annotations. There are four main types of handlers:

 - PreConstructHandler: Validates the request for instantiating a class and offers the possibility to override 
                        the given class with a subclass.
 - InstantiationProvider: May return an Instantiation object for the given class with which the injector will 
                          create an object.
 - DependencyHandler: Offers a way to resolve a given dependency required to instantiate a class, e.g. to implement 
                      custom behavior for annotations.
 - PostConstructHandler: Allows you to perform an action on an object that has been created with the injector. 
                         You can support @PostConstruct methods this way or perform some form of validation.

