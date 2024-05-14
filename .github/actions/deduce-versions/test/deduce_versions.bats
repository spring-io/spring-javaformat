#!./test/libs/bats/bin/bats

load '../../.bats/test_helper/bats-support/load'
load '../../.bats/test_helper/bats-assert/load'

source "$PWD/deduce-versions.sh"

teardown() {
    rm .githuboutput | true
}

@test "deduce_versions() when 'milestone' should export versions" {
	repo=$( mock_git_repo "v1.2.3-M1" )
	cd "$repo"
    GITHUB_OUTPUT=".githuboutput"
    CURRENT_VERSION="1.2.3-SNAPSHOT"
    RELEASE_TYPE="milestone"
    run deduce_versions
    readarray -t githuboutput < .githuboutput
	assert [ "$status" -eq 0 ]
    assert [ "${githuboutput[0]}" = "release-version=1.2.3-M2" ]
    assert [ "${githuboutput[1]}" = "next-version=1.2.3-SNAPSHOT" ]
}

@test "deduce_versions() when 'release-candidate' should export versions" {
	repo=$( mock_git_repo "v1.2.3-M1" "v1.2.3-M2" "v1.2.3-RC1" )
	cd "$repo"
    GITHUB_OUTPUT=".githuboutput"
    CURRENT_VERSION="1.2.3-SNAPSHOT"
    RELEASE_TYPE="release-candidate"
    run deduce_versions
    readarray -t githuboutput < .githuboutput
	assert [ "$status" -eq 0 ]
    assert [ "${githuboutput[0]}" = "release-version=1.2.3-RC2" ]
    assert [ "${githuboutput[1]}" = "next-version=1.2.3-SNAPSHOT" ]
}

@test "deduce_versions() when 'release' should export versions" {
	repo=$( mock_git_repo "v1.2.3-M1" "v1.2.3-M2" "v1.2.3-RC1" )
	cd "$repo"
    GITHUB_OUTPUT=".githuboutput"
    CURRENT_VERSION="1.2.3-SNAPSHOT"
    RELEASE_TYPE="release"
    run deduce_versions
    readarray -t githuboutput < .githuboutput
	assert [ "$status" -eq 0 ]
    assert [ "${githuboutput[0]}" = "release-version=1.2.3" ]
    assert [ "${githuboutput[1]}" = "next-version=1.2.4-SNAPSHOT" ]
}

@test "deduce_versions() when no GITHUB_OUTPUT should fail" {
    CURRENT_VERSION="1.2.3-SNAPSHOT"
    RELEASE_TYPE="release"
    run deduce_versions
	assert [ "$status" -eq 1 ]
  	assert_output "missing GITHUB_OUTPUT environment variable"
}

@test "deduce_versions() when no CURRENT_VERSION should fail" {
    GITHUB_OUTPUT=".githuboutput"
    RELEASE_TYPE="release"
    run deduce_versions
	assert [ "$status" -eq 1 ]
  	assert_output "missing CURRENT_VERSION environment variable"
}

@test "deduce_versions() when no RELEASE_TYPE should fail" {
    GITHUB_OUTPUT=".githuboutput"
    CURRENT_VERSION="1.2.3-SNAPSHOT"
    run deduce_versions
	assert [ "$status" -eq 1 ]
  	assert_output "missing RELEASE_TYPE environment variable"
}

@test "deduce_versions() when wrong RELEASE_TYPE should fail" {
    GITHUB_OUTPUT=".githuboutput"
    CURRENT_VERSION="1.2.3-SNAPSHOT"
    RELEASE_TYPE="nope"
    run deduce_versions
	assert [ "$status" -eq 1 ]
  	assert_output "Unknown release type 'nope'"
}

mock_git_repo() {
	local tmpdir=$(mktemp -d $BATS_TMPDIR/gitrepo.XXXXXX) >&2
	mkdir -p "$tmpdir" >&2
	cd "$tmpdir" >&2
	git init >&2
	echo "foo" > foo.txt
	git add foo.txt >&2
	git commit -m'Initial commit' >&2
	for tag in "$@"; do
		git tag "$tag" >&2
	done
	echo "$tmpdir"
}