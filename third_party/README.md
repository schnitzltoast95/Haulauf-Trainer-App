# Third-party software notices

`app/oss-licenses.gradle` generates offline app assets from the resolved **releaseRuntimeClasspath**, including transitive runtime libraries. It excludes test and build-tool dependencies and deduplicates artifacts with the same Maven coordinate. The task runs before assets are merged, including debug builds (which intentionally show the production inventory).

`catalog.json` records reviewed metadata keyed by exact group:artifact:version. A new or upgraded runtime dependency fails the build until its entry and license text are reviewed. The generator also copies LICENSE, NOTICE and COPYRIGHT files from every shipped JAR/AAR and nested classes.jar/libs/*.jar; lint.jar is not shipped and is excluded. Texts are deduplicated by SHA-256. No license is inferred merely from the group name.

## Updating

1. Resolve releaseRuntimeClasspath and identify the newly reported coordinate.
2. Read its published Maven POM and upstream license/NOTICE at that version. Add its name, project URL and reviewed full license text to the catalog. `licenses`, `notices`, and `sources` reference UTF-8 files in this directory.
3. Review bundled third-party code/data and any additional licenses. Do not assume a POM covers every bundled component.
4. Run `./gradlew :app:generateOssLicenses :app:assembleRelease` and the license device tests. Inspect list and detail screens, including offline text and source notices.

## Current sources

- Names, versions, project URLs and Apache 2.0 declarations come from the resolved artifacts' published POMs. Apache-2.0.txt is copied verbatim from the AndroidX Activity Compose 1.12.4 artifact.
- Coil: https://github.com/coil-kt/coil/blob/2.5.0/LICENSE.txt
- Coroutines: https://github.com/Kotlin/kotlinx.coroutines/blob/1.9.0/LICENSE.txt
- OkHttp: https://github.com/square/okhttp/blob/parent-4.12.0/LICENSE.txt
- Guava ListenableFuture 1.0 inherits its licensing metadata; the source copyright and Apache 2.0 declaration were checked at https://github.com/google/guava/blob/v26.0/guava/src/com/google/common/util/concurrent/ListenableFuture.java
- OkHttp's embedded publicsuffix NOTICE requires MPL 2.0 for its Public Suffix List data. The generator retains that NOTICE. MPL-2.0.txt comes from https://www.mozilla.org/media/MPL/2.0/index.815ca599c9df.txt . public-suffix-list.txt contains the exact rule and exception lists decoded from OkHttp 4.12.0's publicsuffixes.gz (four-byte big-endian lengths followed by UTF-8 rules and exceptions); exception prefixes are restored. It is bundled and readable in the detail screen along with the source-project URL, so the source data remains available offline.

Historical illustrations and texts keep their separate, existing in-context source attributions. Locally authored settings icons are not imported third-party assets. These notices do not change the license of this app's own source code.
