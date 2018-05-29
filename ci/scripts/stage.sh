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
if [[ $RELEASE_TYPE = "RELEASE" ]]; then
	stageVersion=$( strip_snapshot_suffix $snapshotVersion)
	nextVersion=$( bump_version_number $snapshotVersion)
else
	echo "Unknown release type $RELEASE_TYPE" >&2; exit 1;
fi

echo "Staging $stageVersion (next version will be $nextVersion)"
run_maven versions:set -DnewVersion=$stageVersion -DgenerateBackupPoms=false
run_maven org.eclipse.tycho:tycho-versions-plugin:update-eclipse-metadata

git config user.name "Spring Buildmaster" > /dev/null
git config user.email "buildmaster@springframework.org" > /dev/null
git add pom.xml > /dev/null
git commit -m"Release v$stageVersion" > /dev/null
git tag -a "v$stageVersion" -m"Release v$stageVersion" > /dev/null

run_maven clean deploy -U -Dfull -DaltDeploymentRepository=distribution::default::file://${repository}

git reset --hard HEAD^ > /dev/null
echo "Setting next development version (v$nextVersion)"
run_maven versions:set -DnewVersion=$nextVersion -DgenerateBackupPoms=false
run_maven org.eclipse.tycho:tycho-versions-plugin:update-eclipse-metadata
sed -i "s/:release-version:.*/:release-version: ${stageVersion}/g" README.adoc
git add . > /dev/null
git commit -m"Next development version (v$nextVersion)" > /dev/null

echo "DONE"

popd > /dev/null
