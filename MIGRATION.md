# Migration

## Java 17

### Threads

- `ThreadGroup::stop()` is deprecated, stop threads one by one (remake to `interrupt`?)

### JEE 9
- `javax.*` -> `jakarta.*` (especially `persistence`)
- new maven dependencies
- requires `Spring 6`, `Hibernate 6`, `Jetty 11`
- to migrate gradually first used `hibernate-core-jakarta` `5.6` (same hibernate with `jakarta.persistence` package)

## Hibernate 6

https://github.com/hibernate/hibernate-orm/blob/6.0/migration-guide.adoc

### Types
- a stricter type system, had to reimplement custom data types

- weird problems with `PersistanceDateTime`, `TIMESTAMP` precision is overridden in `UserType`, `HSQLDialect`'s precision for tests was not used
```java
@Override
public int getDefaultSqlPrecision(Dialect dialect, JdbcType jdbcType) {
  return dialect.getDefaultTimestampPrecision();
}
```

### [Fetch circularity](https://github.com/hibernate/hibernate-orm/blob/6.0/migration-guide.adoc#fetch-circularity-determination)
- change of depth-first to width-first search

- had to change the fetch strategy to `FetchType.LAZY` for some fields (`Executable`'s `migrateFromExecutable` and `migrateToExecutable`)
- caused fetching whole `Executable` as `HibernateProxy`
- lot of problems (`casting`, and `instanceof`) (e.g. `Migration::perform()`)
- added `@Proxy(lazy = false)` to `Executable` to force eager fetching (does not fetch `LAZY` fields)

- why also `@Access(AccessType.FIELD)` for `Reservation` `id`???

### [Dialects](https://github.com/hibernate/hibernate-orm/blob/6.0/migration-guide.adoc#version-specific-and-spatial-specific-dialects-are-deprecated)
- version-specific dialects are deprecated

### [Sequences](https://github.com/hibernate/hibernate-orm/blob/6.0/migration-guide.adoc#implicit-identifier-sequence-and-table-name)
- with new version every entity has its own sequence (specifically named `<entity name>_seq`)
- for now just used property `hibernate.id.db_structure_naming_strategy=legacy` (uses same behavior as Hibernate 5.3–5.6)
- should be reworked in the future

### HQL
- strict type system, had to extract query parts
- e.g., use `UsedRoomEndpoint = :executable` only if `executable` is of type `UsedRoomEndpoint`

### CriteriaQuery
- a strict type system, had to use the correct type
- e.g. `criteriaBuilder.equal(resourceTagRoot.get("tag"), tagId)` -> `criteriaBuilder.equal(resourceTagRoot.get("tag").get("id"), tagId)`

### [Hibernate 6.2](https://github.com/hibernate/hibernate-orm/blob/6.2/migration-guide.adoc)

#### CustomUniqueDelegate drop constraint

#### remove getCause() wrapping in `PersistenceTest`

#### [@OneToOne UNIQUE](https://github.com/hibernate/hibernate-orm/blob/6.2/migration-guide.adoc#logical-1-1-unique)

- `@OneToOne` makes now foreign key `UNIQUE` by default
- this showed multiple bad mappings
- Had to change `@OneToOne` to `@ManyToOne` if there are duplicates. Beware of `optional = false`!
- SQL check
```sql
SELECT x.allocation_id, COUNT(*) FROM reservation AS x GROUP BY x.allocation_id HAVING COUNT(*) > 1;

CREATE OR REPLACE FUNCTION find_duplicates(
    p_table_name TEXT,
    p_column_name TEXT
) RETURNS TABLE (column_value TEXT, duplicate_count BIGINT)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY EXECUTE FORMAT(
        'SELECT x.%I::TEXT, COUNT(*) FROM %I AS x GROUP BY x.%I HAVING COUNT(*) > 1',
        p_column_name, p_table_name, p_column_name
    );
END;
$$;

SELECT * FROM find_duplicates('reservation', 'allocation_id');
```

```sql
CREATE OR REPLACE FUNCTION find_duplicates(
    p_table_name TEXT,
    p_column_name TEXT
) RETURNS TABLE (column_value TEXT, duplicate_count BIGINT, generated_sql TEXT)
LANGUAGE plpgsql
AS $$
DECLARE
    sql_query TEXT;
BEGIN
    -- Construct the dynamic SQL query
    sql_query := FORMAT(
        'SELECT %I::TEXT, COUNT(*) FROM %I GROUP BY %I HAVING COUNT(*) > 1',
        p_column_name, p_table_name, p_column_name
    );

    -- First row: return the SQL query
    RETURN QUERY SELECT NULL::TEXT, NULL::BIGINT, sql_query;

    -- Execute the dynamic query and return results
    RETURN QUERY EXECUTE FORMAT(
        'SELECT t.*, NULL::TEXT AS generated_sql FROM (%s) AS t',
        sql_query
    );

END;
$$;
```

### [Hibernate 6.3](https://github.com/hibernate/hibernate-orm/blob/6.3/migration-guide.adoc)

#### [HQL null literal comparison](https://github.com/hibernate/hibernate-orm/blob/6.3/migration-guide.adoc#hql-null-literal-comparison)
Changed `= NULL` to `IS NULL` and `!= NULL` to `IS NOT NULL` in HQL queries

### [Hibernate 6.6](https://github.com/hibernate/hibernate-orm/blob/6.6/migration-guide.adoc)

#### [@Table and SINGLE_TABLE inheritance](https://github.com/hibernate/hibernate-orm/blob/6.6/migration-guide.adoc#table-and-single_table-inheritance)
`ManagedMode` inherits from `Mode` which uses `InheritanceType.SINGLE_TABLE` strategy
and so the `@Table` in the `ManagedMode` was ignored.

#### [Merge versioned entity when row is deleted](https://github.com/hibernate/hibernate-orm/blob/6.6/migration-guide.adoc#merge-versioned-entity-when-row-is-deleted)
Previously, merging a detached entity resulted in a SQL insert whenever there was no matching row in the database
(for example, if the object had been deleted in another transaction).
This behavior was unexpected and violated the rules of optimistic locking.

#### [Minimum SQL dialect version](https://docs.jboss.org/hibernate/orm/6.6/dialect/dialect.html)
- minimum `PostgreSQLDialect` is `12`, but we still use `11`
- it looks like there are no breaking changes, but in the end we decided to stay at hibernate `6.4` for now

## Spring 6

No changes were needed.

## Spring Boot 3

1. added a separate `Application` class as a project starter (was `Controller` before)
and moved command line arguments handling there
2. `ControllerConfiguration` was edited to work as Spring's `@Configuration` class
3. Created `Config` class and its `TestConfig` counterpart (with different `@Profile`) where `@Bean`s can be defined,
and Spring's default behaviour can be customed
(many advantages, thanks to later improved dependency injection, we don't have to replace parts of code programatically, but just switch Spring profile)
4. Created `EntityManagerFactory` `@Bean`, and removed it's creation from Controller.init() (and later also tests)
5. Removed `Controller`'s many static constructors, as the first step towards removing static code and replacing it with dependency injection (explain why is DI so good, especially for testability)
6. Created special `TestController` for tests, e.g. overriding jade control
7. Changed `Controller` to Spring's component, so it can use DI by itself, other components like Services,
8. `Cache`, `Executor` and so on followed
9. bumped jackson version to the one that Spring uses 
10. Finally, switched to SpringBoot, annotated `Application` with `@SpringBootApplication` and tests with `@SpringBootTest` \
InterDomainTest problem, creates new WebAppContext that creates `Bean`s again and does not share the config.
Had to make fall-back to constants for `hibernate` configuration.
11. removed creation of `ControllerConfiguration` for every Shongo `Component` and test.
All of codebase now uses the same `ControllerConfiguration` instance, enhancing dependency injection.
12. Wrong test order caused `ClassNotFoundException` in RPC handling.
    Root cause was that not all packages are loaded at the first time `ClassHelper::getPackages` is called.
13. Functional both application and tests

### [Properties](https://docs.spring.io/spring-boot/reference/features/external-config.html#features.external-config.files.importing)

- properties were remade from XML format to `application.yml` format (many advantages)
- possible to extend properties with `-Dspring.config.additional-location=file:path/to/file.yml`

- `ControllerConfiguration` now uses `Environment` to get properties
- thanks to this we could remove `CombinedConfiguration` and its XML parsing,
with the exception of SSL configuration (because SSL context is loaded using `ConfiguredSSLContext`
from `shongo-common` module), which got its own `SSLConfiguration` class

- added prefix `configuration`, found error that `timezone` property is set in tests but not used in `Controller`, 
because it reads it too early and it worked just because default is the same as in tests

- possibility to use `@ConfigurationProperties` in future to map configuration to POJOs,
  for now stayed with `ControllerConfiguration`, and getting properties by their name

### Dependency injection

#### Get rid of static instance (and methods)

- rewrite `DummyAuthorization` to `Bean` (do not create new instance for every test). \
Had to add clearing. Found some problems (e.g., missing a map clear in `AuthorizationCache.clear()`).

- some things could not be injected because the `InterDomainController` uses separate application context\
(e.g. `Authorization` in `AuthorizationServiceImpl`


## Scheduler

After the migration to Spring Boot, the `WorkerThread` was removed and replaced with Spring's `@Scheduled` annotation.

But first, several components had to be redone to Spring's `@Component` and `@Service` annotations.

The most tricky was abstract `Authorization`.
It has 2 implementation: `ServerAuthorization` for production and `DummyAuthorization` for tests.
Many tests create new `DummyAuthorization` instance, setting it to `Controller`'s static attribute.

`Preprocessor` and `Scheduler` followed.

`StringToPeriodConverter` bean was added to convert String from configuration to `Period` object.
This was used with Spring's `@Value("${configuration.worker.lookahead}") private Period lookahead;`.

- `@EnabledScheduling` annotation was added to `Config` class to enable scheduling
- `WorkerThread` manipulation was removed from `Controller` and renamed to `ScheduledWorker`

- `ScheduledWorker` is now using Spring's `@Scheduled` annotation, gets rid of busy waiting (`Thread.sleep()`), and other advantages

```java
try {
    Thread.sleep(period.getMillis());
}
catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}

while (!Thread.interrupted()) {
    work();
    try {
        Thread.sleep(period.getMillis());
    }
    catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```
changed to simple:
```java
@Scheduled(fixedRateString = "${configuration.worker.period}", initialDelayString = "${configuration.worker.period}")
public void runScheduled()
{
    work();
}
```

## Caching

- `@Cacheable` and `@CacheEvict` annotations are used to cache the results of methods and evict them when needed
- at least this was a plan for `Cache` in the `shongo-client-web` module,
but controller uses only a simple `ExpirationMap` to both put and get values
