import json
import os
from pathlib import Path


def gradle_property(name):
    for line in Path("gradle.properties").read_text().splitlines():
        key, separator, value = line.partition("=")
        if separator and key.strip() == name:
            return value.strip()
    raise ValueError(f"Missing {name} in gradle.properties")


event = json.loads(Path(os.environ["GITHUB_EVENT_PATH"]).read_text())
release = event.get("release", event)
version = gradle_property("mod_version")
minecraft_version = gradle_property("minecraft_version")
tag = release["tag_name"]
if not tag.endswith(version):
    raise ValueError(f"Release tag {tag!r} does not match mod_version {version!r}")

jar = Path("build/libs") / f"NuclearCraft-{minecraft_version}-{version}.jar"
if not jar.is_file():
    raise FileNotFoundError(f"Distributable JAR missing: {jar}")

changelog = release.get("body") or ""
if not changelog.strip():
    raise ValueError("GitHub release body is empty; add release notes before publishing")

release_type = "release"
if "alpha" in version.lower():
    release_type = "alpha"
elif release.get("prerelease") or any(part in version.lower() for part in ("beta", "rc")):
    release_type = "beta"

name = release.get("name") or f"NuclearCraft {version}"
modrinth = {
    "project_id": "Qvd8pl5G",
    "name": name,
    "version_number": version,
    "changelog": changelog,
    "dependencies": [{"project_id": "mSYf8LRC", "dependency_type": "required"}],
    "game_versions": [minecraft_version],
    "version_type": release_type,
    "loaders": ["neoforge"],
    "featured": False,
    "file_parts": ["file"],
    "primary_file": "file",
}
curseforge = {
    "changelog": changelog,
    "changelogType": "markdown",
    "displayName": name,
    "gameVersionNames": [minecraft_version, "NeoForge"],
    "releaseType": release_type,
    "relations": {"projects": [{"projectID": "1148833", "type": "requiredDependency"}]},
}

temp = Path(os.environ["RUNNER_TEMP"])
modrinth_path = temp / "modrinth-release.json"
curseforge_path = temp / "curseforge-release.json"
modrinth_path.write_text(json.dumps(modrinth), encoding="utf-8")
curseforge_path.write_text(json.dumps(curseforge), encoding="utf-8")

with Path(os.environ["GITHUB_OUTPUT"]).open("a", encoding="utf-8") as output:
    output.write(f"jar={jar.resolve()}\n")
    output.write(f"modrinth_metadata={modrinth_path}\n")
    output.write(f"curseforge_metadata={curseforge_path}\n")
