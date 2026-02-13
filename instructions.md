# VoidWorld — Инструкция по запуску

## Требования

- **Java 17 JDK** — [Eclipse Temurin](https://adoptium.net/temurin/releases?version=17) (рекомендуется)
- **Git**
- **IDE** — IntelliJ IDEA (рекомендуется), Eclipse или VS Code

## Клонирование

```bash
git clone https://github.com/nulnow/voidworld.git
cd voidworld
```

## Запуск на Windows

```bat
rem Генерация run-конфигураций (один раз после клонирования)
.\gradlew.bat genIntellijRuns

rem Запуск клиента Minecraft с модом
.\gradlew.bat runClient

rem Запуск сервера
.\gradlew.bat runServer

rem Сборка JAR-файла
.\gradlew.bat build
```

## Запуск на macOS / Linux

```bash
# Генерация run-конфигураций (один раз после клонирования)
./gradlew genIntellijRuns

# Запуск клиента Minecraft с модом
./gradlew runClient

# Запуск сервера
./gradlew runServer

# Сборка JAR-файла
./gradlew build
```

## Первый запуск

1. Первый `gradlew runClient` будет долгим (5-15 минут) — Gradle скачает Forge, Minecraft, маппинги Parchment и KotlinForForge
2. После загрузки откроется Minecraft с кнопкой **"Enter VoidWorld"** на главном экране
3. Нажмите кнопку — создастся новый мир с настройками VoidWorld
4. В игре введите `/vw world bootstrap` для генерации разметки всех локаций

## Работа в IntelliJ IDEA

1. Откройте папку проекта через **File → Open**
2. IDEA автоматически импортирует Gradle-проект
3. Дождитесь завершения индексации и синхронизации Gradle
4. Выполните `genIntellijRuns` через Gradle-панель или терминал
5. В списке Run Configurations появятся: **runClient**, **runServer**, **runData**
6. Запускайте **runClient** для работы с модом

## Работа с разметкой мира

После запуска клиента и входа в мир:

```
/gamemode creative
/vw world bootstrap          — разметить все измерения
/vw world bootstrap overworld — только overworld
/vw location list             — список всех локаций
/vw location tp voidworld:capital — телепорт в столицу
/vw location info             — какие локации на текущей позиции
```

Для ручной разметки новых локаций:

```
/give @s voidworld:location_wand
```

1. ПКМ по первому углу → ПКМ по второму углу
2. `/vw location create <id> <тип>` — сохранить локацию
3. `/vw location export` — экспортировать всё в JSON

## Структура проекта

```
src/main/
  java/com/voidworld/        — точка входа мода (Java)
  kotlin/com/voidworld/
    core/                    — ядро (реестры, сеть, конфиг, данные, события, команды)
    system/                  — игровые системы (квесты, NPC, экономика, стелс и т.д.)
    entity/                  — кастомные сущности
    block/                   — кастомные блоки
    item/                    — кастомные предметы
    world/                   — мир (локации, генерация, измерения)
    client/                  — клиент (GUI, HUD, рендеринг)
  resources/
    assets/voidworld/        — текстуры, модели, локализация, звуки
    data/voidworld/          — квесты, диалоги, NPC, локации, измерения
docs/
  GDD.md                    — Game Design Document
  ROADMAP.md                — план разработки по фазам
```

## Сборка и распространение

```bash
./gradlew build
```

Готовый JAR будет в `build/libs/voidworld-0.1.0.jar`. Его можно положить в папку `mods/` любой установки Forge 1.20.1.

## Устранение проблем

| Проблема | Решение |
|---|---|
| `java.lang.UnsupportedClassVersionError` | Установите Java 17 и убедитесь, что `JAVA_HOME` указывает на неё |
| Gradle скачивает очень долго | Первый раз — нормально. Проверьте интернет-соединение |
| `module not specified` в IntelliJ | Выполните `genIntellijRuns` заново |
| Мод не загружается | Проверьте, что `mods.toml` и `pack.mcmeta` на месте в `src/main/resources/` |
| Кастомные измерения не появляются | Убедитесь, что JSON в `data/voidworld/dimension/` корректен |
