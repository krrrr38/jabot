MAVEN := mvn
APP_MODULE := jabot-app
MAIN_CLASS := com.krrrr38.jabot.JabotRunner
PLUGIN_FILE := jabot-app/src/assemble/plugins.yml
# PLUGIN_FILE := myplugins.yml

release: clean
	$(MAVEN) -Prelease release:clean release:prepare release:perform

deploy: clean
	$(MAVEN) -Prelease deploy

package: clean
	$(MAVEN) -Prelease package

install: clean
	$(MAVEN) install -DskipTests=true

run: install
	$(MAVEN) exec:java -pl $(APP_MODULE) -Dexec.mainClass=$(MAIN_CLASS) -Dexec.args="-c $(PLUGIN_FILE)"

rerun:
	$(MAVEN) exec:java -pl $(APP_MODULE) -Dexec.mainClass=$(MAIN_CLASS) -Dexec.args="-c $(PLUGIN_FILE)"

test: clean
	$(MAVEN) test

clean:
	$(MAVEN) clean

.PHONY: release deploy package install run rerun test clean
