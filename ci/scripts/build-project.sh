#!/bin/bash
set -e

source $(dirname $0)/common.sh
repository=$(pwd)/distribution-repository

pushd git-repo > /dev/null
run_maven clean deploy -U -Dfull -DaltDeploymentRepository=distribution::file://${repository}
popd > /dev/null
