#!/bin/bash
set -e

source $(dirname $0)/common.sh
repository=$(pwd)/distribution-repository

pushd git-repo > /dev/null
git fetch --tags --all > /dev/null
popd > /dev/null

git clone git-repo stage-git-repo > /dev/null

pushd stage-git-repo > /dev/null

snapshotVersion=$( xmllint --xpath '/*[local-name()="project"]/*[local-name()="version"]/text()' pom.xml )
if [[ $RELEASE_TYPE = "M" ]]; then
	stageVersion=$( get_next_milestone_release $snapshotVersion)
	nextVersion=$snapshotVersion
elif [[ $RELEASE_TYPE = "RC" ]]; then
	stageVersion=$( get_next_rc_release $snapshotVersion)
	nextVersion=$snapshotVersion
elif [[ $RELEASE_TYPE = "RELEASE" ]]; then
	stageVersion=$( get_next_release $snapshotVersion)
	nextVersion=$( bump_version_number $snapshotVersion)
else
	echo "Unknown release type $RELEASE_TYPE" >&2; exit 1;
fi

echo "Staging ${stageVersion} (next version will be ${nextVersion})"
run_maven versions:set -DnewVersion=${stageVersion} -DgenerateBackupPoms=false
run_maven org.eclipse.tycho:tycho-versions-plugin:update-eclipse-metadata
run_maven --projects io.spring.javaformat:spring-javaformat-vscode-extension -P '!formatter-dependencies' antrun:run@update-version frontend:install-node-and-npm frontend:npm@update-package-lock

git config user.name "Spring Builds" > /dev/null
git config user.email "spring-builds@users.noreply.github.com" > /dev/null
git add pom.xml > /dev/null
git commit -m"Release v${stageVersion}" > /dev/null
git tag -a "v${stageVersion}" -m"Release v${stageVersion}" > /dev/null

run_maven clean deploy -U -Dfull -DaltDeploymentRepository=distribution::default::file://${repository}

git reset --hard HEAD^ > /dev/null
if [[ $nextVersion != $snapshotVersion ]]; then
	echo "Setting next development version (v$nextVersion)"
	run_maven versions:set -DnewVersion=$nextVersion -DgenerateBackupPoms=false
	run_maven org.eclipse.tycho:tycho-versions-plugin:update-eclipse-metadata
	run_maven --projects io.spring.javaformat:spring-javaformat-vscode-extension -P '!formatter-dependencies' antrun:run@update-version frontend:npm@update-package-lock
	sed -i "s/:release-version:.*/:release-version: ${stageVersion}/g" README.adoc
	sed -i "s/spring-javaformat-gradle-plugin:.*/spring-javaformat-gradle-plugin:${nextVersion}\"\)/g" samples/spring-javaformat-gradle-sample/build.gradle
	sed -i "s/spring-javaformat-checkstyle:.*/spring-javaformat-checkstyle:${nextVersion}\"\)/g" samples/spring-javaformat-gradle-sample/build.gradle
	sed -i "s|<spring-javaformat.version>.*</spring-javaformat.version>|<spring-javaformat.version>${nextVersion}</spring-javaformat.version>|" samples/spring-javaformat-maven-sample/pom.xml
	git add -u . > /dev/null
	git commit -m"Next development version (v${nextVersion})" > /dev/null
fi;

popd > /dev/null

echo "Staging Complete"
