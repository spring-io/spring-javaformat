repository=${GITHUB_WORKSPACE}/distribution-repository

echo "Staging ${RELEASE_VERSION} to ${repository} (next version will be ${NEXT_VERSION})"

./mvnw versions:set --batch-mode --no-transfer-progress -DnewVersion=${RELEASE_VERSION} -DgenerateBackupPoms=false
./mvnw org.eclipse.tycho:tycho-versions-plugin:update-eclipse-metadata --batch-mode --no-transfer-progress
./mvnw --projects io.spring.javaformat:spring-javaformat-vscode-extension --batch-mode --no-transfer-progress -P '!formatter-dependencies' antrun:run@update-version frontend:install-node-and-npm frontend:npm@update-package-lock

git config user.name "Spring Builds" > /dev/null
git config user.email "spring-builds@users.noreply.github.com" > /dev/null
git add pom.xml > /dev/null
git commit -m"Release v${RELEASE_VERSION}" > /dev/null
git tag -a "v${RELEASE_VERSION}" -m"Release v${RELEASE_VERSION}" > /dev/null

./mvnw clean deploy --batch-mode --no-transfer-progress -U -Dfull -DaltDeploymentRepository=distribution::file://${repository}

git reset --hard HEAD^ > /dev/null
if [[ ${NEXT_VERSION} != ${CURRENT_VERSION} ]]; then
	echo "Setting next development version (v${NEXT_VERSION})"
	./mvnw versions:set --batch-mode --no-transfer-progress -DnewVersion=${NEXT_VERSION} -DgenerateBackupPoms=false
	./mvnw org.eclipse.tycho:tycho-versions-plugin:update-eclipse-metadata --batch-mode --no-transfer-progress
	./mvnw --projects io.spring.javaformat:spring-javaformat-vscode-extension --batch-mode --no-transfer-progress -P '!formatter-dependencies' antrun:run@update-version frontend:npm@update-package-lock
	sed -i "s/:release-version:.*/:release-version: ${RELEASE_VERSION}/g" README.adoc
	sed -i "s/spring-javaformat-gradle-plugin:.*/spring-javaformat-gradle-plugin:${NEXT_VERSION}\"\)/g" samples/spring-javaformat-gradle-sample/build.gradle
	sed -i "s/spring-javaformat-checkstyle:.*/spring-javaformat-checkstyle:${NEXT_VERSION}\"\)/g" samples/spring-javaformat-gradle-sample/build.gradle
	sed -i "s|<spring-javaformat.version>.*</spring-javaformat.version>|<spring-javaformat.version>${NEXT_VERSION}</spring-javaformat.version>|" samples/spring-javaformat-maven-sample/pom.xml
	git add -u . > /dev/null
	git commit -m"Next development version (v${NEXT_VERSION})" > /dev/null
fi;

echo "Staged the following files:"
find ${repository}
