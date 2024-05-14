#!./test/libs/bats/bin/bats

load '../../.bats/test_helper/bats-support/load'
load '../../.bats/test_helper/bats-assert/load'

source "$PWD/deduce-versions.sh"

@test "strip_snapshot_suffix() should strip '-SNAPSHOT" {
	run strip_snapshot_suffix "1.2.0-SNAPSHOT"
	assert_output "1.2.0"
}

@test "strip_snapshot_suffix() should strip '.BUILD-SNAPSHOT" {
	run strip_snapshot_suffix "1.2.0.BUILD-SNAPSHOT"
	assert_output "1.2.0"
}

@test "strip_snapshot_suffix() when no suffix should return unchanged" {
	run strip_snapshot_suffix "1.2.0"
	assert_output "1.2.0"
}
