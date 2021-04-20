#!/bin/bash
set -e

version=$( cat artifactory-repo/build-info.json | jq -r '.buildInfo.modules[0].id' | sed 's/.*:.*:\(.*\)/\1/' )

java -jar /github-changelog-generator.jar \
  --changelog.repository=spring-io/spring-javaformat \
  ${version} generated-changelog/changelog.md

echo ${version} > generated-changelog/version
echo v${version} > generated-changelog/tag
