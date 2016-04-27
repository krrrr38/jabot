MAVEN := mvn
APP_MODULE := jabot-app
MAIN_CLASS := com.krrrr38.jabot.JabotRunner
PLUGIN_FILE := jabot-app/src/assemble/plugins.yml

release: clean
	$(MAVEN) release:clean release:prepare release:perform

deploy: clean
	$(MAVEN) deploy

package: clean
	$(MAVEN) package

install: clean
	$(MAVEN) install -DskipTests=true -Dgpg.skip=true -Dmaven.javadoc.skip=true

run: install
	$(MAVEN) exec:java -pl $(APP_MODULE) -Dexec.mainClass=$(MAIN_CLASS) -Dexec.args="-c $(PLUGIN_FILE)"

test: clean
	$(MAVEN) test

clean:
	$(MAVEN) clean

.PHONY: release deploy package install run test clean
