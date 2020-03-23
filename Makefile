VERSION=$$(cat VERSION)

default: test

deep_clean: clean
	(rm -fr ./target **/target ; rm -fr ./project/project ; rm -fr ./project/target) || echo "it's clean"

clean:
	sbt clean

fmt:
	sbt safetyCheckScalaFmtRun

publishLocal: test
	 sbt 'set isSnapshot := true' publishLocal

publish: test publish_only

publish_only:
	git tag -a $(VERSION) -m $(VERSION)
	git push origin $(VERSION)

test_coverage_run:
	sbt clean coverage test coverageReport
	open ./target/scala-2.12/sbt-1.0/scoverage-report/index.html

test_coverage:
	sbt "; project library ; clean ; coverage ; test"

test_coverage_report:
	sbt "; project library ; coverageReport" && sbt "; project library ; coverageAggregate"

check_style:
	sbt safetyCheckScalaFmt

unit_test:
	sbt clean test

test_all: deep_clean
	sbt "; project root ; test"

test: deep_clean check_style unit_test

mutator_test:
	export SBT_OPTS="-XX:+CMSClassUnloadingEnabled -Xmx4G"
	sbt 'set logLevel in Test := Level.Error' 'set parallelExecution in Test := true' 'set safetySoftOnCompilerWarning := true' "; project library ; stryker"

mutator_open_results:
	open `find ./target/stryker4s* -type f -iname "*index.html"`

mutator_test_run: mutator_test mutator_open_results
