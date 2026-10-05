#!/usr/bin/env bash
# Downloads the build of the JUnit fork used by this project (junit.version in
# pom.xml) from the releases of the fork and installs it into the local Maven
# repository.
#
#   scripts/install-junit-fork.sh [version]
#
# MAVEN_REPO_LOCAL overrides the local repository (default: ~/.m2/repository).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VERSION="${1:-$(sed -n 's:.*<junit.version>\(.*\)</junit.version>.*:\1:p' "$ROOT/pom.xml")}"
RELEASES="${JUNIT_FORK_RELEASES:-https://github.com/IWillBurn/junit-framework-composition/releases/download}"
REPOSITORY="${MAVEN_REPO_LOCAL:-$HOME/.m2/repository}"
ARCHIVE="junit-$VERSION-maven-repository.zip"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

echo "Downloading JUnit $VERSION"
if ! curl -fsSL -o "$WORK/$ARCHIVE" "$RELEASES/$VERSION/$ARCHIVE"; then
	echo "Cannot download $RELEASES/$VERSION/$ARCHIVE. Is there a release $VERSION of the fork?" >&2
	exit 1
fi

mkdir -p "$REPOSITORY"
if command -v unzip >/dev/null; then
	unzip -oq "$WORK/$ARCHIVE" -d "$REPOSITORY"
else
	(cd "$REPOSITORY" && jar xf "$WORK/$ARCHIVE")
fi
echo "Installed JUnit $VERSION into $REPOSITORY"
