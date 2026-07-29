# Codacy aligncheck

This is the docker engine we use at Codacy to have [aligncheck](https://gitlab.com/opennota/check) support.

You can also create a docker to integrate the tool and language of your choice!
Check the **Docs** section for more information.

## Requirements

-   Java 1.8
-   SBT
-   Docker

## Usage

### Build docker

```sh
sbt universal:stage
docker build -t codacy-aligncheck .
```

### Run tool

```sh
docker run -it -v $PWD:/src codacy-aligncheck:latest
```

### Run tests

We use the [codacy-plugins-test](https://github.com/codacy/codacy-plugins-test) to test our external tools integration.
You can follow the instructions there to make sure your tool is working as expected.

#### Validate documentation

```sh
sbt "run json codacy-aligncheck:latest"
```

#### Run integration tests

```sh
sbt "run pattern codacy-aligncheck:latest"
sbt "run multiple codacy-aligncheck:latest"
```

## Specification

To read more on how to build a wrapper for a tool like this one check the specification in the
[Tool Developer Guide](https://github.com/codacy/codacy-engine-scala-seed/blob/master/README.md#how-to-integrate-an-external-analysis-tool-on-codacy)

## Agent Playbook: Updating This Repository End-to-End

This section is written for an AI coding agent (or a human) tasked with updating this repo. Follow it top to bottom; it tells you what to change, how to test locally, and how to interpret CI so you can iterate on failures without guessing.

### 1. What this repository is

This is a **Codacy engine**: a thin Scala wrapper (`src/main/scala/com/codacy/tools/aligncheck/*.scala`, built on `codacy-engine-scala-seed`) that packages [aligncheck](https://gitlab.com/opennota/check) — a small Go static-analysis tool that finds inefficiently packed/struct-aligned Go structs — as a Docker image Codacy's platform can run against a customer's source code.

Unlike most Codacy engines, **aligncheck is not pinned to a specific released version**. The `Dockerfile` builds it with `go get -u gitlab.com/opennota/check/cmd/aligncheck`, which always fetches the latest commit on the upstream default branch at image-build time. There is no `aligncheckVersion` field anywhere to bump — "updating the wrapped tool" for this repo, in practice, means rebuilding the Docker image (which naturally pulls whatever is newest upstream) or bumping the Go/JRE base images.

The `docs/` directory (`src/main/resources/docs/`) is machine-consumed configuration, but it is small and **hand-maintained, not generated** — there is no `DocGenerator` or similar source anywhere in this repo:

- `docs/patterns.json` — the single pattern (`aligncheck`) Codacy knows about, its level/category, and whether it's enabled by default. Hand-edited.
- `docs/description/description.json` + `docs/description/aligncheck.md` — the UI title/description for that one pattern. Hand-edited.
- `docs/tool-description.md` — short blurb about the tool. Hand-edited.
- `docs/multiple-tests/{with-config,with-invalid-config,without-config}/` — fixtures (`patterns.xml`, `results.xml`, Go source samples) used by `codacy-plugins-test` to validate the engine's output against real code.

### 2. Files that encode versions — check all of these on every update

| File | What it controls | What to check |
|---|---|---|
| `Dockerfile` → `FROM golang:...` (builder stage) | Go toolchain used to build aligncheck itself | Bump if the Go toolchain is outdated or upstream aligncheck needs a newer Go to build. |
| `Dockerfile` → `RUN go get -u gitlab.com/opennota/check/cmd/aligncheck` | The aligncheck tool version | Nothing to "bump" here — this always fetches upstream's latest commit on rebuild. If upstream ever tags releases, consider pinning to a tag instead of `-u` on latest. |
| `Dockerfile` → `FROM amazoncorretto:...` (runtime stage) | JRE the Scala wrapper itself runs on | Bump when a newer Amazon Corretto 8 Alpine tag is available, or the wrapper needs a newer Java. |
| `build.sbt` → `codacy-engine-scala-seed` | Shared Codacy engine framework version | Bump to the latest published version when available. |
| `.circleci/config.yml` → `codacy/base` orb | Shared CircleCI steps (checkout, sbt, publish, tag) | Check the latest published version. |
| `.circleci/config.yml` → `codacy/plugins-test` orb | Runs `codacy-plugins-test` in CI | Same as above. |

### 3. Step-by-step update procedure

1. **Bump the version(s)** as scoped by the task (base images in `Dockerfile`, orb versions in `.circleci/config.yml`, or the seed library in `build.sbt`).
2. **There is no docs generator to re-run.** If the update changes what the tool reports (unlikely, since aligncheck has a single fixed pattern), manually update `docs/patterns.json` and `docs/description/*` and keep the `docs/multiple-tests/*` fixtures consistent.
3. **Compile, format-check, and unit-test** with `sbt "scalafmt::test; test:scalafmt::test; sbt:scalafmt::test; test"` (this mirrors the `check_format_and_test` CircleCI job).
4. **Build the Docker image**:
   ```sh
   sbt universal:stage
   docker build -t codacy-aligncheck .
   ```
5. **Run `codacy-plugins-test` locally** before pushing — clone https://github.com/codacy/codacy-plugins-test and run, per this repo's own README:
   ```sh
   sbt "run json codacy-aligncheck:latest"
   sbt "run pattern codacy-aligncheck:latest"
   sbt "run multiple codacy-aligncheck:latest"
   ```
6. **Iterate on failures**, re-running only the relevant command after each fix. Note that because aligncheck is pulled at `latest` on every build, a locally-passing test run today is not a permanent guarantee — upstream aligncheck could change behavior on a future rebuild independent of anything in this repo.
7. **Commit** the version bump(s) in one change.
8. **Push and open a PR.**
9. **Poll the PR's real CI checks until they all pass — local validation is NOT the finish line.** After every push, run `gh pr checks <pr-url>` and keep re-polling (short sleep while any check is `pending`) until all checks finish. If a check fails, fetch its actual log (don't guess), find the true root cause, fix it, push again (never `--no-verify`, never force-push), and re-poll. Repeat until every check is green. **The CI environment's toolchain can differ from your local one**, so a clean local run does not guarantee CI passes. Only stop iterating when every check passes, or you hit a genuine product/infra decision that needs a human.

### 4. Common failure modes and fixes

- **Bumping the `golang` builder base image can break `go get -u`** if the new Go toolchain drops support for something upstream aligncheck relies on, or if Go module resolution behavior changes across major Go versions (this happened previously: bumping `golang:1.15.3-alpine3.12` → `golang:1.17.2-alpine3.14` required a follow-up fix to the `multiple-tests` fixtures, see commit `25efdd0`, "fix: Fix multiple tests after golang bump").
- **CircleCI's `codacy/plugins-test` orb job (`run_multiple_tests: true`) exercises the `docs/multiple-tests/*` fixtures** — if a base image or Go version bump changes aligncheck's output formatting, these fixtures' `results.xml` will need to be regenerated/updated by hand.

### 5. Definition of done

- Version bump(s) reflected in the `Dockerfile` (builder and/or runtime base image) and/or `.circleci/config.yml` orbs and/or `build.sbt`, as scoped by the task.
- `docs/patterns.json`, `docs/description/*`, and `docs/multiple-tests/*` fixtures updated by hand if the change affects tool output (rare for this repo).
- `sbt` format-check and unit tests pass locally.
- Docker image builds successfully.
- `codacy-plugins-test` commands (`json`, `pattern`, `multiple`) all pass locally against the freshly built image.
- **After pushing and opening/updating the PR, every CI check on it is green.** Poll `gh pr checks <pr-url>` and iterate on any failure until all pass.

## Limitations

This tool requires the usage of [codacy-analysis-cli](https://github.com/codacy/codacy-analysis-cli) to push the results.

## What is Codacy

[Codacy](https://www.codacy.com/) is an Automated Code Review Tool that monitors your technical debt,
helps you improve your code quality, teaches best practices to your developers, and helps you save time in Code Reviews.

### Among Codacy’s features

-   Identify new Static Analysis issues
-   Commit and Pull Request Analysis with GitHub, Bitbucket and GitLab
-   Auto-comments on Commits and Pull Requests
-   Integrations with Slack, Jira, YouTrack
-   Track issues in Code Style, Security, Error Proneness, Performance, Unused Code and other categories

Codacy also helps keep track of Code Coverage, Code Duplication, and Code Complexity.

Codacy supports PHP, Python, Ruby, Java, JavaScript, and Scala, among others.

### Free for Open Source

Codacy is free for Open Source projects.
