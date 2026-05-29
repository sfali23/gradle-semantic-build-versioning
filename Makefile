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
	$(GRADLE) setReleaseVersion publishToMavenLocal

release:
	$(GRADLE) setReleaseVersion createTag pushTag
