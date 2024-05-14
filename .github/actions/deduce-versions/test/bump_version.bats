#!./test/libs/bats/bin/bats

load '../../.bats/test_helper/bats-support/load'
load '../../.bats/test_helper/bats-assert/load'

source "$PWD/deduce-versions.sh"

@test "bump_version_number() should bump '.M'" {
	run bump_version_number "1.2.0.M2"
	assert_output "1.2.0.M3"
}

@test "bump_version_number() should bump '.RC'" {
	run bump_version_number "1.2.0.RC3"
	assert_output "1.2.0.RC4"
}

@test "bump_version_number() should bump '-M'" {
	run bump_version_number "1.2.0-M2"
	assert_output "1.2.0-M3"
}

@test "bump_version_number() should bump '-RC'" {
	run bump_version_number "1.2.0-RC3"
	assert_output "1.2.0-RC4"
}

@test "bump_version_number() should bump without suffix" {
	run bump_version_number "1.2.0"
	assert_output "1.2.1"
}

@test "bump_version_number() should bump '.RELEASE'" {
	run bump_version_number "1.2.0.RELEASE"
	assert_output "1.2.1.RELEASE"
}

@test "bump_version_number() should bump '-SNAPSHOT'" {
	run bump_version_number "1.2.0-SNAPSHOT"
	assert_output "1.2.1-SNAPSHOT"
}

@test "bump_version_number() should bump '.BUILD-SNAPSHOT'" {
	run bump_version_number "1.2.0.BUILD-SNAPSHOT"
	assert_output "1.2.1.BUILD-SNAPSHOT"
}

@test "bump_version_number() when missing argument should fail" {
	run bump_version_number
	assert_output "missing bump_version_number() argument"
	assert [ "$status" -eq 1 ]
}

@test "bump_version_number() when bad argument should fail" {
	run bump_version_number "foo.bar.baz"
	assert_output "unsupported version number"
	assert [ "$status" -eq 1 ]
}
