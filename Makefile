VERSION=$$(cat VERSION)

default: test

deep_clean: clean
	(rm -fr ./target **/target ; rm -fr ./project/project ; rm -fr ./project/target) || echo "it's clean"

clean:
	./scripts/spark-cross-clean.sh

fmt:
	sbt soteriaCheckScalaFmtRun

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
	sbt "; project library ;clean ;coverage ;test ;coverageReport ;coveralls"

check_style:
	sbt soteriaCheckScalaFmt

unit_test:
	sbt clean test

test: deep_clean check_style unit_test

mutator_test:
	SBT_OPTS="-XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC -XX:MaxPermSize=4G -Xms4G -Xmx4G -Xss1G -XX:MaxMetaspaceSize=4G" sbt -mem 4096 'set logLevel in Test := Level.Error' 'set parallelExecution in Test := true' 'set soteriaSoftOnCompilerWarning := true' "; project library ; stryker"

mutator_open_results:
	open `find ./target/stryker4s* -type f -iname "*index.html"`

mutator_test_run: mutator_test mutator_open_results
