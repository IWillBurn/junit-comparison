# junit-comparison

Расширение JUnit Jupiter для сравнения нескольких реализаций одного контракта: тест пишется один раз, а запускается для каждой реализации. Реализации становятся **самым нижним уровнем дерева тестов** — под наборами аргументов `@ParameterizedTest`, повторами `@RepeatedTest` и параметрами `@ParameterizedClass`.

```java
@CompareImplementations(contract = SearchTree.class,
        value = { BinarySearchTree.class, SortedArraySearchTree.class, TreeSetSearchTree.class })
class SearchTreeTest {

    @ParameterizedTest
    @ValueSource(ints = { -7, 0, 42 })
    void containsAddedKey(int key, SearchTree tree) {
        assertTrue(tree.add(key));
        assertTrue(tree.contains(key));
    }
}
```

```
containsAddedKey(int, SearchTree) ✔
├─ [1] key = -7 ✔
│  ├─ BinarySearchTree ✔
│  ├─ SortedArraySearchTree ✔
│  └─ TreeSetSearchTree ✔
├─ [2] key = 0 ✔
│  └─ ...
└─ [3] key = 42 ✔
   └─ ...
```

> **Первая тестовая версия (0.1.0-SNAPSHOT).** Работает только на форке JUnit с `@InvocationComposition`
> ([IWillBurn/junit-framework-composition](https://github.com/IWillBurn/junit-framework-composition)). На релизном
> JUnit проект не компилируется.

## Как запустить

Нужно: JDK 17+ и Maven 3.9+. Собирать форк JUnit не нужно.

```bash
git clone https://github.com/IWillBurn/junit-comparison.git
cd junit-comparison
scripts/install-junit-fork.sh
mvn verify
```

В Git Bash, Linux и macOS — `scripts/install-junit-fork.sh`. В PowerShell или cmd на Windows — `.cmd`-обёртка (она
запускает `install-junit-fork.ps1` с `-ExecutionPolicy Bypass`, менять политику выполнения скриптов не нужно):

```powershell
scripts\install-junit-fork.cmd
```

Ожидаемый итог: 12 тестов библиотеки и 58 листьев примеров, из них 1 пропущен (`@DisabledForImplementation`).

### Откуда берётся JUnit

Сборки форка публикуются [релизами форка](https://github.com/IWillBurn/junit-framework-composition/releases). У каждого
релиза своя версия (`6.2.0-composition-1`, `6.2.0-composition-2`, …) и один файл
`junit-<версия>-maven-repository.zip` — все модули JUnit в раскладке Maven-репозитория.

Скрипт `install-junit-fork` берёт версию из свойства `junit.version` корневого `pom.xml` (или из аргумента), скачивает
файл релиза и распаковывает его в `~/.m2/repository` (другой путь — переменная `MAVEN_REPO_LOCAL`). Дальше Maven
находит JUnit локально, и никаких репозиториев в `pom.xml` не нужно. Версии с суффиксом `composition` не
пересекаются с официальными версиями JUnit.

### Новая сборка форка

1. В форке: **Actions → Publish composition build → Run workflow**, указать новую версию (например,
   `6.2.0-composition-2`) и ветку (по умолчанию `main`). Workflow собирает JUnit и создаёт релиз
   с этой версией.
2. Здесь: поменять `junit.version` в `pom.xml` и снова запустить `install-junit-fork` (`.sh` или `.cmd`).

Для локальной работы с форком без релиза — своя версия в `~/.m2`:

```bash
./gradlew -Pversion=6.2.0-local-SNAPSHOT publishToMavenLocal    # в форке
mvn verify -Djunit.version=6.2.0-local-SNAPSHOT                 # в junit-comparison
```

### Полезные команды

Запускать из корня проекта (иначе модуль примеров не найдёт библиотеку — или сначала `mvn install`):

```bash
# только одна реализация (простые имена классов через запятую)
mvn test -Dcomparison.implementations=BinarySearchTree

# параллельное выполнение
mvn test -Djunit.jupiter.execution.parallel.enabled=true -Djunit.jupiter.execution.parallel.mode.default=concurrent

# демонстрация падения: сломанная реализация роняет только свои листья (сборка падает намеренно)
mvn test -Pfailure-demo

# один метод
mvn test -Dtest='SearchTreeTest#containsAddedKey' -Dsurefire.failIfNoSpecifiedTests=false
```

### Посмотреть дерево

В IntelliJ IDEA: открыть корневой `pom.xml` как проект и запустить тесты модуля `junit-comparison-examples`. Любой
лист (например, `[2] key = 0 > TreeSetSearchTree`) можно перезапустить отдельно — IDE выбирает его по UniqueId.

В консоли — ConsoleLauncher из того же форка:

```bash
mvn test-compile
mvn -N dependency:copy -DoutputDirectory=target -Dmdep.stripVersion=true \
  -Dartifact=org.junit.platform:junit-platform-console-standalone:6.2.0-composition-1
java -jar target/junit-platform-console-standalone.jar execute --details=tree \
  -cp junit-comparison-examples/target/test-classes:junit-comparison-examples/target/classes:junit-comparison/target/classes \
  --select-package org.atpfivt.comparison.examples --exclude-tag failure-demo
```

На Windows разделитель class path — `;`.

### Подключение в свой проект

Сначала `install-junit-fork` (`.sh` или `.cmd`) и `mvn install` в этом репозитории (устанавливается только библиотека),
затем:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.junit</groupId>
            <artifactId>junit-bom</artifactId>
            <version>6.2.0-composition-1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.atpfivt</groupId>
        <artifactId>junit-comparison</artifactId>
        <version>0.1.0-SNAPSHOT</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Gradle:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.2.0-composition-1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.atpfivt:junit-comparison:0.1.0-SNAPSHOT")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
```

Чтобы в дереве были имена аргументов (`key = 42`, а не `42`), компилируйте тесты с `-parameters`.

## API

| Элемент | Назначение |
|---|---|
| `@CompareImplementations(contract, value)` | на тестовом классе: каждый тест-шаблон с параметром типа `contract` выполняется для каждой реализации из `value`. Действует на подклассы и `@Nested`-классы |
| `@ImplementationTest` | замена `@Test` для метода с параметром-контрактом |
| `@DisabledForImplementation(value, reason)` | пропустить тесты метода для перечисленных реализаций с причиной |
| `comparison.implementations` | параметр конфигурации JUnit: оставить только перечисленные реализации |

- Экземпляр реализации создаётся заново для каждого теста (нужен конструктор без аргументов) и внедряется во все параметры типа контракта этого теста, включая `@BeforeEach`/`@AfterEach`. Реализация с `AutoCloseable` закрывается после теста.
- Обычные `@Test` и шаблоны без параметра-контракта не затрагиваются.
- Реализация, не реализующая контракт, или пустой список реализаций — ошибка конфигурации шаблона.

## Как это работает

Аннотация `@CompareImplementations` мета-аннотирована `@ExtendWith(ImplementationsProvider.class)` и
`@InvocationComposition(levels = CompareImplementations.class)`. Провайдер выдаёт по одному вызову на реализацию, а
`@InvocationComposition` из форка ставит эти вызовы уровнем **под** вызовами других провайдеров шаблона вместо
сцепления с ними. Провайдер согласен быть и внешним уровнем (`mayEncloseTestTemplateInvocations() = true`).
Библиотека использует только публичный API JUnit.

## Примеры (`junit-comparison-examples`)

Контракт `SearchTree` (`add`, `contains`, `size`, `toSortedList`) и три короткие реализации: `BinarySearchTree`
(несбалансированное дерево), `SortedArraySearchTree` (отсортированный массив и двоичный поиск), `TreeSetSearchTree`
(обёртка над `java.util.TreeSet`).

| Тест | Что показывает |
|---|---|
| `SearchTreeTest` | `@ImplementationTest`; `@ParameterizedTest` с `@ValueSource` и `@MethodSource`; `@RepeatedTest` (реализации под повторами, `RepetitionInfo` доступен в листе); `@DisabledForImplementation`; обычный `@Test`, который не размножается; `@Nested` с `@BeforeEach`, получающим тот же экземпляр |
| `RandomizedSearchTreeTest` | три уровня: `@ParameterizedClass` × `@ParameterizedTest` × реализации, сравнение с `TreeSet` |
| `RepeatedParameterizedTest` | возможность форка без библиотеки: все наборы аргументов внутри каждого повтора (`@InvocationComposition(levels = ParameterizedTest.class)`) |
| `FailureDemoTest` | только в профиле `failure-demo`: падает лишь `BrokenSearchTree` и лишь на тех аргументах, где он ошибается |

```
FailureDemoTest
└─ keepsKeysSortedAndDistinct(List, SearchTree)
   ├─ [1] keys = [1, 2, 3]
   │  ├─ TreeSetSearchTree ✔
   │  └─ BrokenSearchTree ✔
   ├─ [2] keys = [3, 1, 2]
   │  ├─ TreeSetSearchTree ✔
   │  └─ BrokenSearchTree ✘ expected: <[1, 2, 3]> but was: <[3, 1, 2]>
   └─ [3] keys = [2, 2, 1]
      ├─ TreeSetSearchTree ✔
      └─ BrokenSearchTree ✘ expected: <[1, 2]> but was: <[2, 2, 1]>
```

Полное дерево примеров:

```
SearchTreeTest
├─ containsAddedKey(int, SearchTree)
│  ├─ [1] key = -7
│  │  ├─ BinarySearchTree
│  │  ├─ SortedArraySearchTree
│  │  └─ TreeSetSearchTree
│  ├─ [2] key = 0 ...
│  └─ [3] key = 42 ...
├─ handlesLargeSortedInput(SearchTree)
│  ├─ BinarySearchTree ↷ Disabled for BinarySearchTree ==> unbalanced: degenerates into a list
│  ├─ SortedArraySearchTree
│  └─ TreeSetSearchTree
├─ isEmptyInitially(SearchTree)
│  ├─ BinarySearchTree
│  ├─ SortedArraySearchTree
│  └─ TreeSetSearchTree
├─ keepsKeysSortedAndDistinct(List, SearchTree)
│  ├─ [1] keys = [5, 3, 8, 1] ...
│  ├─ [2] keys = [1, 2, 3, 4] ...
│  └─ [3] keys = [2, 2, 1] ...
├─ plainTestRunsOnce()
├─ matchesTreeSetOnRandomKeys(RepetitionInfo, SearchTree)
│  ├─ repetition 1 of 2
│  │  ├─ BinarySearchTree
│  │  ├─ SortedArraySearchTree
│  │  └─ TreeSetSearchTree
│  └─ repetition 2 of 2 ...
└─ Prefilled
   └─ rejectsDuplicate(int, SearchTree)
      ├─ [1] key = 2 ...
      ├─ [2] key = 4 ...
      └─ [3] key = 6 ...
RandomizedSearchTreeTest
├─ [1] seed = 1
│  └─ matchesTreeSet(int, SearchTree)
│     ├─ [1] count = 10
│     │  ├─ BinarySearchTree
│     │  ├─ SortedArraySearchTree
│     │  └─ TreeSetSearchTree
│     └─ [2] count = 1000 ...
└─ [2] seed = 2 ...
RepeatedParameterizedTest
└─ allArgumentsWithinEachRepetition(int, RepetitionInfo)
   ├─ repetition 1 of 2
   │  ├─ [1] key = 3
   │  ├─ [2] key = 1
   │  └─ [3] key = 2
   └─ repetition 2 of 2 ...
```

## Структура

```
junit-comparison/
├─ pom.xml                         родительский pom: Java 17, junit-bom ${junit.version}, версии плагинов
├─ junit-comparison/               библиотека
│  └─ src/main/java/org/atpfivt/comparison/
│     ├─ CompareImplementations, ImplementationTest, DisabledForImplementation   публичный API
│     └─ internal/                 провайдер вызовов, резолвер параметров, условие (не API)
│  └─ src/test/java/...            тесты библиотеки на EngineTestKit: форма дерева, фильтр, UniqueId, ошибки
├─ junit-comparison-examples/      примеры с деревом поиска (не устанавливается в репозиторий)
└─ scripts/install-junit-fork.*    установка сборки форка из релиза в ~/.m2 (sh; cmd + ps1 для Windows)
```

## Ограничения

- Нужна сборка форка JUnit с `@InvocationComposition` (версия — `junit.version` в `pom.xml`).
- Реализации всегда самый нижний уровень; `@RepeatedTest` нельзя поставить уровнем под реализациями или под
  аргументами — повторы всегда остаются верхним уровнем.
- `@ResourceLock` для отдельной реализации не поддерживается: платформа запрещает динамическим узлам
  эксклюзивные ресурсы.
- Аргументы `@ParameterizedTest` общие для всех реализаций одного набора: изменяемые аргументы, которые тест
  меняет, нужно копировать в тесте.
