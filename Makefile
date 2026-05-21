GRADLE = ./gradlew

build:
	$(GRADLE) build

clean:
	$(GRADLE) clean

test:
	$(GRADLE) test

integrationTest:
	$(GRADLE) integrationTest

allTests:
	$(GRADLE) test integrationTest

spotless:
	$(GRADLE) spotlessApply
