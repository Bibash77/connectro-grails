# Runtime Issues Analysis & Fixes

## Issues Identified

### 🔴 **Critical Issue #1: Duplicate Bean Definition in resources.groovy**

**File:** `grails-app/conf/spring/resources.groovy`

**Problem:**
```groovy
beans = {
    jsonSlurper(groovy.json.JsonSlurper)
    elasticQueryService(service.ElasticQueryService)  // ❌ OLD SERVICE
    elasticSearchConfig(configurations.ElasticSearchConfig)  // ❌ DUPLICATE
}
```

**Issues:**
1. **Duplicate `ElasticSearchConfig`**: Defined both in `resources.groovy` and as `@Configuration` in `ElasticSearchConfig.java`. This can cause:
   - `BeanDefinitionOverrideException` or
   - Duplicate bean warnings
   
2. **Unused `ElasticQueryService`**: The old Java service is still registered but not used by V0 endpoints. This is unnecessary.

**Fix:** Clean up `resources.groovy` - remove old service registration and duplicate config.

---

### 🟡 **Issue #2: Old Controller Still Exists**

**File:** `grails-app/controllers/connectro/rest/SearchExecutionController.groovy`

**Problem:**
- Controller still exists and depends on old `ElasticQueryService`
- Not accessible via UrlMappings (routes removed), but Spring may still try to instantiate it
- This is not critical if `ElasticQueryService` bean exists, but creates dead code

**Status:** Not blocking, but should be cleaned up.

---

### ✅ **Issue #3: Bean Dependency on ElasticQueryService**

**Current State:**
- `resources.groovy` creates `ElasticQueryService` bean manually
- `ElasticQueryService.java` also has `@Service` annotation
- This could cause issues if constructor injection fails

**Fix:** Remove from `resources.groovy` since `@Service` annotation handles it.

---

## Recommended Fixes

### Fix #1: Update resources.groovy

**Remove unnecessary bean definitions:**

```groovy
package spring

beans = {
    // JsonSlurper is fine - can be a singleton
    jsonSlurper(groovy.json.JsonSlurper)
    
    // REMOVED: elasticQueryService - handled by @Service annotation
    // REMOVED: elasticSearchConfig - handled by @Configuration annotation
}
```

### Fix #2: Verify Spring Component Scanning

**Check `application.yml` has correct package scanning:**
```yaml
grails:
  spring:
    bean-packages:
      - configurations  # ✅ ElasticSearchConfig.java
      - service        # ✅ V0SearchService.groovy (with @Service)
```

This should already be correct.

### Fix #3: Optional - Remove Deprecated Controller

If `SearchExecutionController` is not needed, it can be deleted. However, if it's only blocked by URL mappings, it won't cause runtime issues.

---

## Expected Runtime Behavior After Fixes

1. ✅ `ElasticSearchConfig` bean created once (via `@Configuration`)
2. ✅ `ElasticsearchClient` bean created from `ElasticSearchConfig`
3. ✅ `V0SearchService` bean created via `@Service` + component scanning
4. ✅ `BaseElasticsearchService` abstract class - no bean created (as expected)
5. ✅ No duplicate bean warnings
6. ✅ Spring application starts cleanly

---

## Potential Runtime Warnings (Non-Critical)

1. **Deprecated Controllers**: `SearchExecutionController`, `FieldDataController`, `UserSavedViewController` exist but aren't routed - they won't cause errors, just unused code warnings.

2. **Elasticsearch Connection**: On startup, if Elasticsearch is not running, you may see connection errors. This is expected - the health endpoint will report this.

---

## Testing After Fixes

1. Start application: `./gradlew bootRun` or `./grailsw run-app`
2. Check for bean creation errors in logs
3. Verify no duplicate bean warnings
4. Test health endpoint: `GET /health`
5. Test Elasticsearch health: `GET /health/elastic`
6. Test search endpoints: `GET /search/listings?city=Kathmandu`
