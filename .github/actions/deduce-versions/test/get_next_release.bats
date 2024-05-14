#!./test/libs/bats/bin/bats

load '../../.bats/test_helper/bats-support/load'
load '../../.bats/test_helper/bats-assert/load'

source "$PWD/deduce-versions.sh"

@test "get_next_milestone_release() when has no version should fail" {
	run get_next_milestone_release
	assert [ "$status" -eq 1 ]
  	assert_output "missing get_next_milestone_release() version argument"
}

@test "get_next_rc_release() when has no version should fail" {
	run get_next_rc_release
	assert [ "$status" -eq 1 ]
  	assert_output "missing get_next_rc_release() version argument"
}

@test "get_next_tag_based_release() when has no version should fail" {
	run get_next_tag_based_release
	assert [ "$status" -eq 1 ]
  	assert_output "missing get_next_tag_based_release() version argument"
}

@test "get_next_tag_based_release() when has no tag type should fail" {
	run get_next_tag_based_release "1.2.3"
	assert [ "$status" -eq 1 ]
  	assert_output "missing get_next_tag_based_release() tag type argument"
}

@test "get_next_milestone_release() when has no tag should return M1" {
	repo=$( mock_git_repo )
	cd "$repo"
	run get_next_milestone_release "1.2.3-SNAPSHOT"
	assert_output "1.2.3-M1"
}

@test "get_next_rc_release() when has no tag should return RC1" {
	repo=$( mock_git_repo )
	cd "$repo"
	run get_next_rc_release "1.2.3-SNAPSHOT"
	assert_output "1.2.3-RC1"
}

@test "get_next_tag_based_release() when has no tag and dash SNAPSHOT suffix should return dashed X1" {
	repo=$( mock_git_repo )
	cd "$repo"
	run get_next_tag_based_release "1.2.3-SNAPSHOT" "X"
	assert_output "1.2.3-X1"
}

@test "get_next_tag_based_release() when has no tag and dash BUILD-SNAPSHOT suffix should return dashed X1" {
	repo=$( mock_git_repo )
	cd "$repo"
	run get_next_tag_based_release "1.2.3.BUILD-SNAPSHOT" "X"
	assert_output "1.2.3.X1"
}

@test "get_next_tag_based_release() when has tags and dashed should return dashed X tag+1" {
	repo=$( mock_git_repo "v1.2.3-X1" "v1.2.3-X3" "v1.2.3-X2" )
	cd "$repo"
	run get_next_tag_based_release "1.2.3-SNAPSHOT" "X"
	assert_output "1.2.3-X4"
}

@test "get_next_tag_based_release() when has tags and dashed should return dot X tag+1" {
	repo=$( mock_git_repo "v1.2.3.X1" "v1.2.3.X3" "v1.2.3.X2" )
	cd "$repo"
	run get_next_tag_based_release "1.2.3.BUILD-SNAPSHOT" "X"
	assert_output "1.2.3.X4"
}

@test "get_next_tag_based_release() when has multiple tags should return version match tag+1" {
	repo=$( mock_git_repo "v1.5.0.A1" "v1.5.0.A2" "v1.5.0.B1" "v2.0.0.A1" "v2.0.0.B1" "v2.0.0.B2" )
	cd "$repo"
	run get_next_tag_based_release "1.5.0.BUILD-SNAPSHOT" "A"
	assert_output "1.5.0.A3"
	run get_next_tag_based_release "1.5.0.BUILD-SNAPSHOT" "B"
	assert_output "1.5.0.B2"
	run get_next_tag_based_release "2.0.0.BUILD-SNAPSHOT" "A"
	assert_output "2.0.0.A2"
	run get_next_tag_based_release "2.0.0.BUILD-SNAPSHOT" "B"
	assert_output "2.0.0.B3"
}

@test "get_next_release() should return next release version with release suffix" {
	run get_next_release "1.5.0.BUILD-SNAPSHOT" "RELEASE"
	assert_output "1.5.0.RELEASE"
	run get_next_release "1.5.0-SNAPSHOT" "RELEASE"
	assert_output "1.5.0-RELEASE"
}

@test "get_next_release() should return next release version" {
	run get_next_release "1.5.0.BUILD-SNAPSHOT"
	assert_output "1.5.0"
	run get_next_release "1.5.0-SNAPSHOT"
	assert_output "1.5.0"
}

@test "get_next_release() when has no version should fail" {
	run get_next_release
	assert [ "$status" -eq 1 ]
	assert_output "missing get_next_release() version argument"
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
