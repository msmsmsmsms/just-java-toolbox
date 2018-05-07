# Creating mocks

Imagine you have a service that relies on constructor injection:

```java
// Service.java

class Service {
  // ...

  public Service(DependencyA a, DependencyB b) {
  }
}
```


To test your service it might be necessary to mock these dependencies. This can quickly turn into verbose code like this:

```java
// ServiceTest.java

@Test
public void testCase() {
  DependencyA mockA = mock(DependencyA.class);
  DependencyB mockB = mock(DependencyB.class);

  Service instance = new Service(mockA, mockB);
}
```

Using the Mocks class provided by the toolbox you can simplify your setup:

```java
// ServiceTest.java

@Test
public void testCase() {
  Service instance = Mocks.injectMocks(Service.class);
}
```

In case you want to provide special behaviour for your mocks, you can pass those in as arguments:

```java
// ServiceTest.java

@Test
public void testCase() {
  DependencyA mockA = mock(DependencyA.class);
  when(mockA.method(any()).thenReturn(true);

  Service instance = Mocks.injectMocks(Service.class, mockA);
}
```
