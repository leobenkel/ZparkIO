# ZparkIO Dependency and Build Upgrade Notes

This document describes all the changes made to update the ZparkIO project dependencies, build configuration, and CI/CD pipeline.

## Summary of Changes

The project has been updated to use the latest stable versions of all dependencies and build tools. This update includes support for newer Spark versions (3.4.4 and 3.5.6) and updates to the Scala, ZIO, and testing frameworks.

## Build Tool Updates

### sbt
- **Previous:** 1.8.2
- **Updated to:** 1.10.11
- **File:** `project/build.properties`
- **Reason:** Latest stable version with improved performance and bug fixes

## Scala Version Updates

### Scala 2.12
- **Previous:** 2.12.17
- **Updated to:** 2.12.20
- **Reason:** Latest maintenance release with bug fixes and improvements

### Scala 2.13
- **Previous:** 2.13.10
- **Updated to:** 2.13.17
- **Reason:** Latest stable release with performance improvements and bug fixes

**Note:** Scala 2.11 remains at 2.11.12 (still supported for Spark 2.3 and 2.4)

## Spark Version Updates

### New Spark Versions Added
- **Spark 3.4.4** (updated from 3.3.1 as highest version)
- **Spark 3.5.6** (newly added)

### Existing Versions Updated
- **Spark 3.2:** 3.2.3 → 3.2.4
- **Spark 3.3:** 3.3.1 → 3.3.4

### Complete Spark Version Matrix
The project now supports the following Spark versions:
- 2.3.4 (Scala 2.11)
- 2.4.8 (Scala 2.11, 2.12)
- 3.1.3 (Scala 2.12)
- 3.2.4 (Scala 2.12, 2.13)
- 3.3.4 (Scala 2.12, 2.13)
- 3.4.4 (Scala 2.12, 2.13) - **NEW**
- 3.5.6 (Scala 2.12, 2.13) - **NEW**

**File:** `sparkVersions`

## Library Dependency Updates

### ZIO
- **Previous:** 2.0.10
- **Updated to:** 2.1.9
- **Reason:** Latest stable version with new features and bug fixes
- **Breaking Changes:** Minimal - mostly backward compatible, but review ZIO migration guide if issues arise

### ScalaTest
- **Previous:** 3.2.16
- **Updated to:** 3.2.19
- **Reason:** Latest version with bug fixes and improvements
- **Breaking Changes:** None expected - fully backward compatible

### Scallop (Command-line parsing)
- **Previous:** 4.1.0
- **Updated to:** 5.2.0
- **Reason:** Latest version with Scala Native 0.5 support
- **Breaking Changes:** Major version bump - may require code changes. Review the [Scallop changelog](https://github.com/scallop/scallop/releases) for details.
- **File:** `configLibs/Scallop`

### Netty (for Spark 3.x)
- **Previous:** 4.1.94.Final
- **Updated to:** 4.1.115.Final
- **Netty TCNative:** 2.0.61.Final → 2.0.67.Final
- **Reason:** Latest stable versions with security fixes and performance improvements

### spark-testing-base
Updated to use the 2.0.1 release for all Spark 3.x versions:
- **Spark 3.1:** 3.1.2_1.3.0 → 3.1.3_2.0.1
- **Spark 3.2:** 3.2.2_1.3.0 → 3.2.4_2.0.1
- **Spark 3.3:** 3.4.0_1.4.3 → 3.3.4_2.0.1
- **Spark 3.4:** 3.4.4_2.0.1 (NEW)
- **Spark 3.5:** 3.5.6_2.0.1 (NEW)

**Note:** Spark 2.x versions remain unchanged (using older spark-testing-base versions)

## SBT Plugin Updates

### sbt-ci-release
- **Previous:** 1.5.11
- **Updated to:** 1.11.0
- **File:** `project/plugins.sbt`
- **Important:** This version defaults to publishing to the new Central Portal (Maven Central). The Legacy OSSRH endpoint was sunset on June 30, 2025.
- **Action Required:** If you're publishing to Maven Central, ensure you've migrated to the Central Portal.

### Soteria
- **Version:** 0.5.1 (unchanged)
- **Reason:** No newer version found in public repositories

## CI/CD Updates

### GitHub Actions Workflow (`.github/workflows/ci.yml`)
- **actions/checkout:** v2 → v4
- **actions/cache:** v3 → v4
- **actions/setup-java:** v3 → v4
- **Java Distribution:** adopt → temurin (Eclipse Temurin)
- **Java Version:** 8 → 11
- **Reason:** Spark 3.4+ requires Java 11 minimum. Java 8 is no longer sufficient for newer Spark versions.

## Testing and Validation

### Before Merging
1. **Build Verification:**
   ```bash
   ./scripts/spark-cross-compile.sh
   ```
   This will compile the project against all Spark versions.

2. **Code Formatting:**
   ```bash
   ./scripts/spark-cross-fmt.sh
   ```
   Verify code formatting compliance.

3. **Test Execution:**
   ```bash
   ./scripts/spark-cross-test.sh
   ```
   Run all tests across all Spark versions.

4. **Full Test Suite:**
   ```bash
   make test
   ```
   Runs deep clean, style check, and unit tests.

### Known Compatibility Issues to Watch For

1. **Scallop 5.x Migration:**
   - This is a major version upgrade. Check command-line argument parsing code in:
     - `examples/Example1_mini/src/main/scala/com/leobenkel/example1/Arguments.scala`
     - `examples/Example2_small/src/main/scala/com/leobenkel/example2/Arguments.scala`
     - Any custom argument parsing code in your applications

2. **ZIO 2.1.x Migration:**
   - While mostly backward compatible, some APIs may have changed
   - Check for any deprecated warnings during compilation
   - Review error handling code

3. **Java 11 Requirement:**
   - Local development environments must have Java 11 or later
   - Docker images and deployment environments must be updated
   - Some Java 8-specific flags may no longer be valid

## Deployment Considerations

### Java Runtime
- **Minimum Required:** Java 11
- **Recommended:** Java 11 LTS or Java 17 LTS
- Update all deployment environments (Docker, Kubernetes, etc.) to use Java 11+

### Maven Central Publishing
- If you're publishing to Maven Central, ensure you've completed the migration to Central Portal
- Update credentials and configuration as needed
- Test staging releases before production

### Spark Clusters
- Ensure your Spark clusters support the newer Spark versions you plan to use
- Verify Scala version compatibility on your cluster
- Test with your specific Spark configuration (YARN, Kubernetes, standalone, etc.)

## Rollback Plan

If issues are encountered, you can rollback by reverting the following files:
1. `project/build.properties`
2. `build.sbt`
3. `sparkVersions`
4. `project/plugins.sbt`
5. `.github/workflows/ci.yml`

Use git to revert to the previous commit:
```bash
git revert HEAD
```

## Next Steps

1. Review this document carefully
2. Run the build and tests locally
3. Address any compilation errors or test failures
4. Update deployment documentation if needed
5. Communicate changes to the team
6. Update Docker images with Java 11+
7. Test on staging environment before production deployment

## Questions or Issues?

If you encounter problems with this upgrade:
1. Check the [ZparkIO GitHub Issues](https://github.com/leobenkel/ZparkIO/issues)
2. Review individual library migration guides (links below)
3. Consult with the team

## Useful Links

- [Apache Spark 3.5 Release Notes](https://spark.apache.org/releases/spark-release-3-5-0.html)
- [ZIO 2.1 Release Notes](https://github.com/zio/zio/releases)
- [Scallop 5.x Changelog](https://github.com/scallop/scallop/releases)
- [sbt 1.10 Release Notes](https://eed3si9n.com/sbt-1.10.0)
- [Maven Central Migration Guide](https://central.sonatype.org/publish/publish-portal-migration/)
