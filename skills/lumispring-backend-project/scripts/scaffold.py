#!/usr/bin/env python3
"""Create a standalone Kotlin service that consumes LumiSpring Starter artifacts."""

from __future__ import annotations

import argparse
import re
from pathlib import Path


STARTER_ARTIFACTS = {
    "base": "framework-starter-base",
    "web": "framework-starter-web",
    "mysql": "framework-starter-database-mysql",
    "redis": "framework-starter-database-redis",
    "security": "framework-starter-security",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--group-id", required=True)
    parser.add_argument("--artifact-id", required=True)
    parser.add_argument("--package", required=True, dest="package_name")
    parser.add_argument("--application-class", help="Defaults to an artifact-derived name plus Application")
    parser.add_argument("--framework-version", default="3.4.0.0")
    parser.add_argument("--pom-mode", choices=("parent", "bom"), default="parent")
    parser.add_argument(
        "--starter",
        action="append",
        choices=tuple(STARTER_ARTIFACTS),
        default=[],
        help="Repeat to select capabilities; defaults to web.",
    )
    parser.add_argument("--enable-security", action="store_true")
    return parser.parse_args()


def validate(args: argparse.Namespace) -> None:
    if not re.fullmatch(r"[A-Za-z_][A-Za-z0-9_.]*", args.package_name):
        raise SystemExit("--package must be a valid dotted Kotlin package name")
    if not re.fullmatch(r"[A-Za-z0-9_.-]+", args.group_id):
        raise SystemExit("--group-id contains unsupported characters")
    if not re.fullmatch(r"[A-Za-z0-9_.-]+", args.artifact_id):
        raise SystemExit("--artifact-id contains unsupported characters")
    if args.output.exists() and any(args.output.iterdir()):
        raise SystemExit(f"Refusing to overwrite non-empty directory: {args.output}")
    if args.enable_security and "security" not in args.starter:
        raise SystemExit("--enable-security requires --starter security")


def application_class(artifact_id: str) -> str:
    parts = [part for part in re.split(r"[^A-Za-z0-9]+", artifact_id) if part]
    prefix = "".join(part[:1].upper() + part[1:] for part in parts) or "LumiSpring"
    if prefix[0].isdigit():
        prefix = "App" + prefix
    return prefix + "Application"


def effective_starters(requested: list[str]) -> list[str]:
    selected = list(dict.fromkeys(requested or ["web"]))
    if "security" in selected:
        return ["security"]
    if "web" in selected and "mysql" in selected:
        selected.remove("mysql")
    if "web" in selected and "base" in selected:
        selected.remove("base")
    return selected


def dependency_xml(starters: list[str]) -> str:
    blocks = []
    for starter in starters:
        blocks.append(
            "        <dependency>\n"
            "            <groupId>com.lumispring.framework</groupId>\n"
            f"            <artifactId>{STARTER_ARTIFACTS[starter]}</artifactId>\n"
            "        </dependency>"
        )
    return "\n\n".join(blocks) + "\n"


def infrastructure_yaml(enable_security: bool) -> str:
    if not enable_security:
        return ""
    return """  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/app?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
""".rstrip()


def render(template: Path, replacements: dict[str, str]) -> str:
    text = template.read_text(encoding="utf-8")
    for key, value in replacements.items():
        text = text.replace("{{" + key + "}}", value)
    unresolved = sorted(set(re.findall(r"\{\{[A-Z_]+\}\}", text)))
    if unresolved:
        raise RuntimeError(f"Unresolved template placeholders in {template}: {unresolved}")
    return text


def write_rendered(template: Path, destination: Path, replacements: dict[str, str]) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(render(template, replacements), encoding="utf-8", newline="\n")


def main() -> None:
    args = parse_args()
    validate(args)
    starters = effective_starters(args.starter)
    app_class = args.application_class or application_class(args.artifact_id)
    if not re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*", app_class):
        raise SystemExit("--application-class must be a valid Kotlin class name")

    templates = Path(__file__).resolve().parent.parent / "assets" / "standalone"
    replacements = {
        "GROUP_ID": args.group_id,
        "ARTIFACT_ID": args.artifact_id,
        "PACKAGE": args.package_name,
        "APPLICATION_CLASS": app_class,
        "FRAMEWORK_VERSION": args.framework_version,
        "DEPENDENCIES": dependency_xml(starters),
        "SECURITY_ENABLED": "true" if args.enable_security else "false",
        "INFRASTRUCTURE_CONFIG": infrastructure_yaml(args.enable_security),
    }

    package_path = Path(*args.package_name.split("."))
    source_root = args.output / "src" / "main" / "kotlin" / package_path
    write_rendered(
        templates / ("pom-parent.xml.tmpl" if args.pom_mode == "parent" else "pom-bom.xml.tmpl"),
        args.output / "pom.xml",
        replacements,
    )
    write_rendered(templates / "Application.kt.tmpl", source_root / f"{app_class}.kt", replacements)
    if "web" in starters or "security" in starters:
        write_rendered(templates / "HealthController.kt.tmpl", source_root / "web" / "HealthController.kt", replacements)
    write_rendered(templates / "application.yml.tmpl", args.output / "src" / "main" / "resources" / "application.yml", replacements)
    write_rendered(templates / "gitignore.tmpl", args.output / ".gitignore", replacements)

    print(f"Created {args.artifact_id} in {args.output.resolve()}")
    print(f"POM mode: {args.pom_mode}; starters: {', '.join(starters)}")
    if "security" in starters and not args.enable_security:
        print("Security dependency added but disabled until MySQL, Redis, and schema are ready.")


if __name__ == "__main__":
    main()
