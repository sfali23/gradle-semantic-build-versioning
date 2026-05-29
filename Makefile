GRADLE = ./gradlew

build:
	$(GRADLE) build

clean:
	$(GRADLE) clean

test:
	$(GRADLE) test

spotless:
	$(GRADLE) spotlessApply

publishLocal:
	$(GRADLE) publishToMavenLocal

release:
	$(GRADLE) setReleaseVersion createTag pushTag
