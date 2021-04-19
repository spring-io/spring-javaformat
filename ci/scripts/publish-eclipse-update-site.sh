#!/bin/bash
set -e

source $(dirname $0)/common.sh

buildName=$( cat artifactory-repo/build-info.json | jq -r '.buildInfo.name' )
buildNumber=$( cat artifactory-repo/build-info.json | jq -r '.buildInfo.number' )
groupId=$( cat artifactory-repo/build-info.json | jq -r '.buildInfo.modules[0].id' | sed 's/\(.*\):.*:.*/\1/' )
version=$( cat artifactory-repo/build-info.json | jq -r '.buildInfo.modules[0].id' | sed 's/.*:.*:\(.*\)/\1/' )

echo "Publishing ${buildName}/${buildNumber} to Eclipse Update Site"
curl \
	-s \
	--connect-timeout 240 \
	--max-time 2700 \
	-u ${ARTIFACTORY_USERNAME}:${ARTIFACTORY_PASSWORD} \
	-f \
	-H "X-Explode-Archive: true" \
	-X PUT \
	-T "artifactory-repo/io/spring/javaformat/io.spring.javaformat.eclipse.site/${version}/io.spring.javaformat.eclipse.site-${version}.zip" \
	"https://repo.spring.io/javaformat-eclipse-update-site/${version}/" > /dev/null || { echo "Failed to publish" >&2; exit 1; }

releasedVersions=$( curl -s -f -X GET https://repo.spring.io/api/storage/javaformat-eclipse-update-site | jq -r '.children[] | .uri' | cut -c 2- | grep '\d.*' | sort -V )

repositories=""
while read -r releasedVersion; do
	echo "Adding repository for ${releasedVersion}"
	repositories="${repositories}<repository><url>https://repo.spring.io/javaformat-eclipse-update-site/${releasedVersion}</url><layout>p2</layout></repository>"
done <<< "${releasedVersions}"

pushd git-repo > /dev/null
sed "s|##repositories##|${repositories}|" ci/scripts/publish-eclipse-update-site-pom-template.xml > publish-eclipse-update-site-pom.xml
run_maven -f publish-eclipse-update-site-pom.xml clean package || { echo "Failed to publish" >&2; exit 1; }

curl \
		-s \
		--connect-timeout 240 \
		--max-time 2700 \
		-u ${ARTIFACTORY_USERNAME}:${ARTIFACTORY_PASSWORD} \
		-f \
		-X PUT \
		-T "target/repository/content.jar" \
		"https://repo.spring.io/javaformat-eclipse-update-site/" > /dev/null || { echo "Failed to publish" >&2; exit 1; }

curl \
		-s \
		--connect-timeout 240 \
		--max-time 2700 \
		-u ${ARTIFACTORY_USERNAME}:${ARTIFACTORY_PASSWORD} \
		-f \
		-X PUT \
		-T "target/repository/artifacts.jar" \
		"https://repo.spring.io/javaformat-eclipse-update-site/" > /dev/null || { echo "Failed to publish" >&2; exit 1; }

popd > /dev/null

echo "Publish complete"
