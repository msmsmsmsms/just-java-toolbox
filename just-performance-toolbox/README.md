# Usage

## Spring Boot

Create a configuration like below to get aspectj based performance monitoring which can be enabled by a property

```java
    @Aspect
    @Configuration
    @ConditionalOnProperty(name = "performance.logging.enabled")
    public class PerformanceLoggerConfiguration {
    
        {
            PerformanceLogger.setPerformanceLoggerEnabled(true);
        }

        @Around("bean(*) && within(de.justsoftware..*)")
        public Object repositoryAnnotation(final ProceedingJoinPoint thisJoinPoint) throws Throwable {
            return PerformanceLogger.logJoinPoint(thisJoinPoint);
        }

    }
```
