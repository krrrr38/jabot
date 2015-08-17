MAVEN := mvn

release: release-prepare github
	$(MAVEN) relase:perform

release-prepare: clean
	$(MAVEN) release:clean release:prepare

github: package
	$(MAVEN) de.jutzig:github-release-plugin:release -rf :jabot-app

package: clean
	$(MAVEN) package

clean:
	$(MAVEN) clean

.PHONY: release release-prepare github package clean
