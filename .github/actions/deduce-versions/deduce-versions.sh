#!/usr/bin/env bash

# Get the next milestone release for the given number by inspecting current tags
get_next_milestone_release() {
	[[ -n $1 ]] || { echo "missing get_next_milestone_release() version argument" >&2; return 1; }
	get_next_tag_based_release "$1" "M"
}

# Get the next RC release for the given number by inspecting current tags
get_next_rc_release() {
	[[ -n $1 ]] || { echo "missing get_next_rc_release() version argument" >&2; return 1; }
	get_next_tag_based_release "$1" "RC"
}

# Get the next release for the given number
get_next_release() {
	[[ -n $1 ]] || { echo "missing get_next_release() version argument" >&2; return 1; }
	if [[ $1 =~ ^(.*)\.BUILD-SNAPSHOT$ ]]; then
		local join="."
	else
		local join="-"
	fi
	local version
	local result
	version=$( strip_snapshot_suffix "$1" )
	if [[ -n $2 ]]; then
	  result="${version}${join}${2}"
	else
	  result="${version}"
	fi
	echo $result
}

# Get the next milestone or RC release for the given number by inspecting current tags
get_next_tag_based_release() {
	[[ -n $1 ]] || { echo "missing get_next_tag_based_release() version argument" >&2; return 1; }
	[[ -n $2 ]] || { echo "missing get_next_tag_based_release() tag type argument" >&2; return 1; }
	if [[ $1 =~ ^(.*)\.BUILD-SNAPSHOT$ ]]; then
		local join="."
	else
		local join="-"
	fi
	local version
	local last
	version=$( strip_snapshot_suffix "$1" )
	git fetch --tags --all > /dev/null
	last=$( git tag --list "v${version}${join}${2}*" | sed -E "s/^.*${2}([0-9]+)$/\1/g" | sort -rn | head -n1 )
	if [[ -z $last ]]; then
		last="0"
	fi
	last="${version}${join}${2}${last}"
	bump_version_number "$last"
}

# Remove any "-SNAPSHOT" or ".BUILD-SNAPSHOT" suffix
strip_snapshot_suffix() {
	[[ -n $1 ]] || { echo "missing get_relase_version() argument" >&2; return 1; }
	if [[ $1 =~ ^(.*)\.BUILD-SNAPSHOT$ ]]; then
		echo "${BASH_REMATCH[1]}"
	elif [[ $1 =~ ^(.*)-SNAPSHOT$ ]]; then
		echo "${BASH_REMATCH[1]}"
	else
		echo "$1"
	fi
}

# Bump version number by incrementing the last numeric, RC or M token
bump_version_number() {
	local version=$1
	[[ -n $version ]] || { echo "missing bump_version_number() argument" >&2; return 1; }
	if [[ $version =~ ^(.*(\.|-)([A-Za-z]+))([0-9]+)$ ]]; then
		local prefix=${BASH_REMATCH[1]}
		local suffix=${BASH_REMATCH[4]}
		(( suffix++ ))
		echo "${prefix}${suffix}"
		return 0;
	fi
	local suffix
	if [[ $version =~ ^(.*)(\-SNAPSHOT)$ ]]; then
		version=${BASH_REMATCH[1]}
		suffix="-SNAPSHOT"
	fi
	tokens=(${version//\./ })
	local bumpIndex
	for i in "${!tokens[@]}"; do
		if [[ "${tokens[$i]}" =~ ^[0-9]+$ ]] ; then
			bumpIndex=$i
		fi
	done
	[[ -n $bumpIndex ]] || { echo "unsupported version number" >&2; return 1; }
	(( tokens[bumpIndex]++ ))
	local bumpedVersion
	IFS=. eval 'bumpedVersion="${tokens[*]}"'
	echo "${bumpedVersion}${suffix}"
}

# Deduce versions
deduce_versions() {
	[[ -n ${GITHUB_OUTPUT} ]] || { echo "missing GITHUB_OUTPUT environment variable" >&2; return 1; }
	[[ -n ${CURRENT_VERSION} ]] || { echo "missing CURRENT_VERSION environment variable" >&2; return 1; }
	[[ -n ${RELEASE_TYPE} ]] || { echo "missing RELEASE_TYPE environment variable" >&2; return 1; }
    if [[ ${RELEASE_TYPE,,} = "milestone" ]]; then
	    releaseVersion=$( get_next_milestone_release ${CURRENT_VERSION})
	    nextVersion=${CURRENT_VERSION}
    elif [[ ${RELEASE_TYPE,,} = "release-candidate" ]]; then
	    releaseVersion=$( get_next_rc_release ${CURRENT_VERSION})
	    nextVersion=${CURRENT_VERSION}
    elif [[ ${RELEASE_TYPE,,} = "release" ]]; then
	    releaseVersion=$( get_next_release ${CURRENT_VERSION})
	    nextVersion=$( bump_version_number ${CURRENT_VERSION})
    else
	    echo "Unknown release type '${RELEASE_TYPE}'" >&2; exit 1;
    fi
    echo "release-version=${releaseVersion}" >> "$GITHUB_OUTPUT"
    echo "next-version=${nextVersion}" >> "$GITHUB_OUTPUT"
}
