buildInfo=$( jfrog rt curl api/build/spring-javaformat-${VERSION}/${BUILD_NUMBER} )
groupId=$( echo ${buildInfo} | jq -r '.buildInfo.modules[0].id' | sed 's/\(.*\):.*:.*/\1/' )
version=$( echo ${buildInfo} | jq -r '.buildInfo.modules[0].id' | sed 's/.*:.*:\(.*\)/\1/' )

echo "Publishing ${buildName}/${buildNumber} (${groupId}:${version}) to Eclipse Update Site"

jfrog rt dl --build spring-javaformat-${VERSION}/${BUILD_NUMBER} '**/io.spring.javaformat.eclipse.site*.zip'

curl \
	-s \
	--connect-timeout 240 \
	--max-time 2700 \
	-u ${ARTIFACTORY_USERNAME}:${ARTIFACTORY_PASSWORD} \
	-f \
	-H "X-Explode-Archive: true" \
	-X PUT \
	-T "io/spring/javaformat/io.spring.javaformat.eclipse.site/${version}/io.spring.javaformat.eclipse.site-${version}.zip" \
	"https://repo.spring.io/javaformat-eclipse-update-site/${version}/" > /dev/null || { echo "Failed to publish" >&2; exit 1; }

releasedVersions=$( curl -s -f -X GET https://repo.spring.io/api/storage/javaformat-eclipse-update-site | jq -r '.children[] | .uri' | cut -c 2- | grep '[0-9].*' | sort -V )

repositories=""
while read -r releasedVersion; do
	echo "Adding repository for ${releasedVersion}"
	repositories="${repositories}<repository><url>https://repo.spring.io/javaformat-eclipse-update-site/${releasedVersion}</url><layout>p2</layout></repository>"
done <<< "${releasedVersions}"

sed "s|##repositories##|${repositories}|" ${GITHUB_ACTION_PATH}/publish-eclipse-update-site-pom-template.xml > publish-eclipse-update-site-pom.xml
./mvnw -f publish-eclipse-update-site-pom.xml clean package || { echo "Failed to publish" >&2; exit 1; }

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

echo "Publish complete"
