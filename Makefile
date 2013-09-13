DIR = $(shell readlink -f $(shell dirname $(shell find . -type f -name "AndroidManifest.xml" 2>/dev/null | sort | head -1) 2>/dev/null) 2>/dev/null)

SDK = ${ANDROID_HOME}
API = $(shell cat $(DIR)/AndroidManifest.xml 2>/dev/null | tr "[:space:]" "\n" | grep minSdkVersion= | cut -d'"' -f2)
PACKAGE = $(shell cat $(DIR)/AndroidManifest.xml 2>/dev/null | tr "[:space:]" "\n" | grep package= | cut -d'"' -f2)
SUPPORT = $(shell find $(SDK)/extras/android/support -type f -name "android-support-v*.jar" 2>/dev/null | head -1)
VERSION_NAME = $(shell cat $(DIR)/AndroidManifest.xml 2>/dev/null | tr "[:space:]" "\n" | grep versionName= | cut -d'"' -f2)
VERSION_CODE = $(shell cat $(DIR)/AndroidManifest.xml 2>/dev/null | tr "[:space:]" "\n" | grep versionCode= | cut -d'"' -f2)

ANDROID = $(shell which android 2>/dev/null)
ADB = $(shell which adb 2>/dev/null)
ANT = $(shell which ant 2>/dev/null)
LINT = $(shell which lint 2>/dev/null)

TARGET = $(shell $(ANDROID) list targets -c 2>/dev/null | tail -1)
DEVICE = $(shell $(ADB) devices -l 2>/dev/null | egrep '^[0-9A-F]' | head -1 | cut -c1-17)

APK = $(PACKAGE).apk
APK_DEBUG = bin/$(PACKAGE)-debug.apk
APK_RELEASE = bin/$(PACKAGE)-release.apk

all:
	@echo "==> Directory"
	@if [ -z "$(DIR)" ] ; then echo "Fatal error" ; exit 1 ; fi
	@echo "- $(DIR)"
	@cd $(DIR) || exit 1
	@echo "==> Manifest"
	@if [ ! -f "AndroidManifest.xml" ] ; then echo "Error: no manifest" ; exit 1 ; fi
	@if [ -z "$(PACKAGE)" ] ; then echo "Error: no package" ; exit 1 ; fi
	@echo "- $(PACKAGE)"
	@if [ -z "$(API)" ] ; then echo "Error: no api" ; exit 1 ; fi
	@echo "- API-$(API)"
	@if [ -z "$(VERSION_NAME)" -o -z "$(VERSION_CODE)" ] ; then echo "Error: no version" ; exit 1 ; fi
	@echo "- v$(VERSION_NAME) b$(VERSION_CODE)"
	@echo "==> SDK"
	@if [ -z "$(SDK)" ] ; then echo "Error: no sdk" ; exit 1 ; fi
	@echo "- $(SDK)"
	@if [ -z "$(TARGET)" ] ; then echo "Error: no target" ; exit 1 ; fi
	@echo "- $(TARGET)"
	@if [ -z "$(SUPPORT)" ] ; then echo "Error: no support library" ; exit 1 ; fi
	@echo "- $(SUPPORT)"
	@echo "==> Device"
	@if [ -n "$(DEVICE)" ] ; then echo "- $(DEVICE)" ; fi
	@echo "==> Binaries"
	@echo "- android"
	@if [ -z "$(ANDROID)" ] ; then echo "Error: command not found 'android'" ; exit 1 ; fi
	@echo "- adb"
	@if [ -z "$(ADB)" ] ; then echo "Error: command not found 'adb'" ; exit 1 ; fi
	@echo "- ant"
	@if [ -z "$(ANT)" ] ; then echo "Error: command not found 'ant'" ; exit 1 ; fi
	@echo "- lint"
	@if [ -z "$(LINT)" ] ; then echo "Error: command not found 'lint'" ; exit 1 ; fi

update: all
	@echo "==> Git"
	@echo "- update"
	@if [ -f ".gitmodules" ] ; then git submodule update --init >/dev/null ; fi
	@echo "==> Support library"
	@echo "- $(shell echo $(SUPPORT) | sed -r 's#$(SDK)/##')"
	@$(foreach p, $(shell find . -type d -name "libs"), cp $(SUPPORT) $p/ ;)
	@echo "==> Libraries"
	@echo "- actionbarsherlock"
	@$(ANDROID) --silent update lib-project --target "$(TARGET)" --path libs/actionbarsherlock/actionbarsherlock
	@echo "- appmsg"
	@$(ANDROID) --silent update lib-project --target "$(TARGET)" --path libs/appmsg/library
	@echo "- collapsiblesearchmenu"
	@$(ANDROID) --silent update lib-project --target "$(TARGET)" --path libs/collapsiblesearchmenu/library
	@echo "- crouton"
	@$(ANDROID) --silent update lib-project --target "$(TARGET)" --path libs/crouton/library
	@echo "- googleplayservice"
	@$(ANDROID) --silent update lib-project --target "$(TARGET)" --path libs/googleplayservice
	@echo "- numberpicker"
	@$(ANDROID) --silent update lib-project --target "$(TARGET)" --path libs/numberpicker/lib
	@echo "- pulltorefresh"
	@$(ANDROID) --silent update lib-project --target "$(TARGET)" --path libs/pulltorefresh
	@echo "- sidenavigation"
	@$(ANDROID) --silent update lib-project --target "$(TARGET)" --path libs/sidenavigation/library
	@echo "==> Project"
	@echo "- $(DIR)"
	@$(ANDROID) --silent update project --name $(PACKAGE) --target "$(TARGET)" --path .

check: all
	@echo "==> Check"
	@if [ ! -d "bin" ] ; then echo "Error: no build" ; exit 1; fi
	@$(LINT) --quiet -w -Xlint:deprecation .

debug: update
	@echo "==> Build"
	@$(ANT) -logfile ant.log debug > /dev/null || exit 1
	@echo "- ant.log"
	@echo "==> Debug"
	@cp $(APK_DEBUG) $(APK) || exit 1
	@echo "==> $(APK)"

release: update
	@echo "==> Build"
	@$(ANT) -logfile ant.log release > /dev/null || exit 1
	@echo "- ant.log"
	@echo "==> Release"
	@cp $(APK_RELEASE) $(APK) || exit 1
	@echo "==> $(APK)"

install: all
	@echo "==> Install"
	@if [ -z "$(DEVICE)" ] ; then echo "Error: no device" ; exit 1 ; fi
	@if [ ! -f "$(APK)" ] ; then echo "Error: no apk" ; exit 1 ; fi
	@$(ADB) install -r $(APK) || exit 1

uninstall: all
	@echo "==> Uninstall"
	@if [ -z "$(DEVICE)" ] ; then echo "Error: no device" ; exit 1 ; fi
	@$(ADB) shell pm uninstall -k $(PACKAGE) || exit 1

clean: all
	@echo "==> Clean"
	@$(ANT) -quiet clean > /dev/null || exit 1

distclean: clean
	@rm -f $(APK)

help:
	@echo "update check debug release clean distclean"

.PHONY: all update check debug release clean distclean help
