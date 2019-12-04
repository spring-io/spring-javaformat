#!/bin/bash
set -e

source $(dirname $0)/common.sh

buildName=$( cat artifactory-repo/build-info.json | jq -r '.buildInfo.name' )
buildNumber=$( cat artifactory-repo/build-info.json | jq -r '.buildInfo.number' )
groupId=$( cat artifactory-repo/build-info.json | jq -r '.buildInfo.modules[0].id' | sed 's/\(.*\):.*:.*/\1/' )
version=$( cat artifactory-repo/build-info.json | jq -r '.buildInfo.modules[0].id' | sed 's/.*:.*:\(.*\)/\1/' )

echo "Publishing ${buildName}/${buildNumber} to Eclipse Update Site"
# We need to push twice for some reason otherwise we get out of date versions
for i in {1..2}; do
	curl \
		-s \
		--connect-timeout 240 \
		--max-time 2700 \
		-u ${BINTRAY_USERNAME}:${BINTRAY_API_KEY} \
		-f \
		-X PUT \
		-T "artifactory-repo/io/spring/javaformat/io.spring.javaformat.eclipse.site/${version}/io.spring.javaformat.eclipse.site-${version}.zip" \
		"https://api.bintray.com/content/spring/javaformat-eclipse/update-site/${version}/${version}/site.zip?explode=1&publish=1" > /dev/null || { echo "Failed to publish" >&2; exit 1; }
	releasedVersions=$( curl -f -X GET https://api.bintray.com/packages/spring/javaformat-eclipse/update-site | jq -r '.versions[]' )
	sleep 30
done

respositories=""
while read -r releasedVersion; do
	echo "Adding repository for ${releasedVersion}"
	respositories="${respositories}<repository><url>https://dl.bintray.com/spring/javaformat-eclipse/${releasedVersion}</url><layout>p2</layout></repository>"
done <<< "${releasedVersions}"

pushd git-repo > /dev/null
sed "s|##respositories##|${respositories}|" ci/scripts/publish-eclipse-update-site-pom-template.xml > publish-eclipse-update-site-pom.xml
run_maven -f publish-eclipse-update-site-pom.xml clean package || { echo "Failed to publish" >&2; exit 1; }

curl \
		-s \
		--connect-timeout 240 \
		--max-time 2700 \
		-u ${BINTRAY_USERNAME}:${BINTRAY_API_KEY} \
		-f \
		-X PUT \
		-T "target/repository/content.jar" \
		"https://api.bintray.com/content/spring/javaformat-eclipse/content.jar" > /dev/null || { echo "Failed to publish" >&2; exit 1; }

curl \
		-s \
		--connect-timeout 240 \
		--max-time 2700 \
		-u ${BINTRAY_USERNAME}:${BINTRAY_API_KEY} \
		-f \
		-X PUT \
		-T "target/repository/artifacts.jar" \
		"https://api.bintray.com/content/spring/javaformat-eclipse/artifacts.jar" > /dev/null || { echo "Failed to publish" >&2; exit 1; }

popd > /dev/null

echo "Publish complete"
