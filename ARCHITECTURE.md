# Архитектура: domain, application, infrastructure

Во всех production-классах и интерфейсах роль указана в Javadoc. `Domain`
обозначает игровые правила и данные, `Application` — сценарии и их координацию,
`Infrastructure` — ввод, файлы, графику и сборку приложения.
Имена существующих пакетов сохранены: `game`, `control`, `input` и `level`
сейчас содержат типы разных слоёв.

Порт — API на границе ядра, через который вызывают сценарий либо подключают
внешнюю возможность. Адаптер переводит вызовы или данные между таким API
и конкретным окружением. Само наличие `implements` ещё не делает класс
инфраструктурным адаптером: например, `TankWorld` реализует доменную политику
`MovementPolicy` и остаётся частью domain.

## Порты и адаптеры

| Контракт ядра | Направление и назначение | Реализация / адаптер |
| --- | --- | --- |
| `GameSession.render/dispose` | Входное API application: выполнить кадр и завершить сеанс | `TankGame` — входной адаптер жизненного цикла LibGDX; отдельного интерфейса для этого небольшого API пока нет |
| `MovementTarget` | Входной API domain: начать движение и узнать активность | `Tank` — реализация внутри ядра; `MoveCommand` вызывает этот API из application |
| `ShootingTarget` | Входной API domain: попытаться выстрелить | `Tank` — реализация внутри ядра; `ShootCommand` вызывает этот API из application |
| `InputAction` | Выходной порт application: обработать управление в начале кадра | `InputHandler`, `MovementInputAction`, `KeyPressCommandInputAction` — адаптеры обработки ввода; `TankSimulation`, `TankController` — внутренние application-сервисы с тем же контрактом |
| `KeyState` | Выходной порт: запросить состояние устройства ввода | `GdxKeyState` — адаптер к LibGDX `Input`; в тестах подставляются лямбды |
| `GameView` | Выходной порт application: показать кадр и освободить представление | `GdxGameView` — адаптер графики LibGDX |
| `LevelSource` | Выходной порт application: получить логический уровень | `FileLevelLoader` — файловый адаптер; `RandomLevelGenerator` — адаптер, выдающий сгенерированные уровни |
| `WorldListener` | Выходной событийный порт domain: сообщить о добавлении/удалении объектов | `WorldSceneRenderer` — адаптер событий мира к графической сцене |

Ввод опрашивается: `TankGame` запускает кадр, `GameSession` вызывает
`InputAction`, обработчик читает `KeyState` и формирует команды.
`KeyboardMovementStrategy` переводит клавиши в команды, поэтому относится
к входным адаптерам, хотя работает через абстрактный источник клавиш.
Адаптер `GdxKeyState` обслуживает запросы этого источника.

## Классификация всех типов

| Слой | Типы | Ответственность |
| --- | --- | --- |
| Domain | `Tank`, `Bullet`, `Tree` | Сущности: здоровье, движение, перезарядка, столкновения и время жизни |
| Domain | `TankWorld`, `Field` | Агрегат мира и модель поля; занятость клеток и взаимодействие объектов |
| Domain | `Direction`, `BulletSettings`, `LevelDefinition` | Значения и настройки игровых правил / логического уровня |
| Domain | `RandomTankGenerator` | Сервис размещения танков в свободных клетках |
| Domain | `MovementTarget`, `ShootingTarget`, `WorldListener` | Входные API сущностей и выходные события мира |
| Domain | `MovementPolicy`, `LevelObject`, `Obstacle`, `Updatable` | Внутренние контракты правил движения, объектов, препятствий и времени |
| Domain | `TankState`, `LightTankState`, `MediumTankState`, `HeavyTankState`, `HealthTankStates` | Поведение состояний и переходы по здоровью |
| Application | `GameSession`, `TankSimulation`, `TankController` | Кадр, создание симуляции, выбор и выполнение команд |
| Application | `Command`, `MoveCommand`, `ShootCommand`, `ToggleHealthBarsCommand` | Контракт команды и действия сценариев |
| Application | `MovementStrategy`, `RandomMovementStrategy` | Выбор следующего действия и политика AI |
| Application | `InputAction`, `KeyState`, `GameView`, `LevelSource` | Контракты подключения ввода, представления и источника уровня |
| Infrastructure | `GameDesktopLauncher`, `DesktopGameFactory` | Запуск LWJGL и composition root: соединение ядра с адаптерами |
| Infrastructure | `TankGame`, `LevelOptions` | Адаптер жизненного цикла LibGDX и разбор командной строки |
| Infrastructure | `GdxKeyState`, `KeyboardMovementStrategy`, `InputHandler`, `MovementInputAction`, `KeyPressCommandInputAction`, `DirectionKeyBinding` | Чтение клавиш, их настройка и преобразование в команды |
| Infrastructure | `FileLevelLoader`, `RandomLevelGenerator` | Источники логического уровня |
| Infrastructure | `GdxGameView`, `WorldSceneRenderer` | Адаптеры представления и событий мира |
| Infrastructure | `FieldRenderer`, `TankRenderer`, `TreeRenderer`, `BulletRenderer`, `HealthBarRenderer`, `GroundMapFactory`, `HealthBarVisibility` | Компоненты графического адаптера, ресурсы и настройка отображения |
| Infrastructure | `SceneRenderer`, `TilePositioner`, `TileMovement`, `GdxGameUtils` | Внутренние контракты и вспомогательные средства графики |

`SceneRenderer` и `TilePositioner` используют LibGDX `Batch`, `Rectangle`
и `GridPoint2`, поэтому это интерфейсы **внутри инфраструктуры**.
Граница ядра с графикой проходит через `GameView` и `WorldListener`.
`MovementStrategy`, `Command` и `TankState` являются контрактами шаблонов
«Стратегия», «Команда» и «Состояние»; они сами по себе не обозначают внешнюю
систему. `RandomMovementStrategy` — внутренняя политика application,
а `RandomLevelGenerator` подключается как сменный источник через `LevelSource`.

## Где границы ещё требуют доработки

1. **Application → конкретный класс представления.**
   `ToggleHealthBarsCommand` импортирует `render.HealthBarVisibility`.
   Здесь не хватает выходного порта application, например
   `HealthBarControl.toggle()`. `HealthBarVisibility` должен реализовать его,
   а `DesktopGameFactory` — передать реализацию команде. Само чтение видимости
   уже задаётся в `HealthBarRenderer` через `BooleanSupplier`.
2. **Application создаёт входной адаптер.**
   `TankSimulation` создаёт `KeyboardMovementStrategy` и знает клавиатурные
   привязки. Для строгой границы нужно собирать стратегии/контроллеры в
   `DesktopGameFactory` и передавать их через существующий `MovementStrategy`
   или фабрику контроллеров. Новый порт к клавиатуре не нужен: есть `KeyState`.
3. **Типы LibGDX входят в domain.**
   Координаты сущностей и сигнатура `MovementPolicy` используют `GridPoint2`.
   Чтобы заменить игровой фреймворк, потребуется собственный тип координат
   domain и преобразования в адаптерах загрузки/отрисовки.
   `Direction.getRotation()` также стоит перенести в графическое отображение
   направлений. Дополнительный интерфейс вокруг `GridPoint2` эту зависимость
   не устранит.
4. **Размещение контрактов размывает слои.**
   `LevelObject` и `TankWorld` импортируют `game.Updatable`: доменный контракт
   физически находится рядом с application. Его следует перенести в domain
   или общий пакет ядра. Аналогично, `LevelDefinition` и `RandomTankGenerator`
   логически относятся к domain, хотя лежат рядом с файловым адаптером.
   Порты `LevelSource`, `KeyState`, `InputAction` стоит отделить от пакетов
   с их инфраструктурными реализациями.
5. **Ошибки файлового ввода входят в общий порт уровня.**
   `LevelSource.load()` объявляет `IOException`. При появлении сетевого
   источника лучше определить ошибку загрузки на уровне application,
   а адаптерам поручить перевод `IOException` и сетевых ошибок в неё.
6. **Подписка представления привязана к конкретному миру.**
   `WorldSceneRenderer` использует у `TankWorld` только операции подписки.
   Если понадобятся другой источник событий, replay или удалённая симуляция,
   полезен выходной контракт `WorldEvents.addListener/removeListener`.
   Сейчас зависимость infrastructure → domain направлена внутрь правильно;
   выделение этого порта нужно для взаимозаменяемости источника событий.

`Consumer<Tank>` для выстрела и удаления соединяют `Tank` с его агрегатом
`TankWorld` внутри domain. Файловых, сетевых или графических операций здесь
нет, поэтому дополнительных инфраструктурных портов этим callback-ам сейчас
не требуется. Отдельный интерфейс для каждого класса также не нужен:
порты выделяются по границам возможностей.

Эти пункты отражают текущее состояние кода. В рамках задания добавлены
архитектурные пометки и анализ; перечисленные переносы пакетов и выделение
дополнительных портов остаются следующими шагами рефакторинга.

## «Состояние» у танка

`Tank` — контекст, `TankState` — интерфейс поведения,
`LightTankState`, `MediumTankState`, `HeavyTankState` — конкретные состояния.
`HealthTankStates` хранит только правило выбора состояния по доле здоровья:

| Состояние | Доля начального здоровья | Скорость движения | Стрельба |
| --- | --- | --- | --- |
| Лёгкое | больше 70% | ×1 | Обычная |
| Среднее | от 15% до 70% включительно | ×1/2 | Обычная |
| Тяжёлое | меньше 15% | ×1/3 | Запрещена |

Пороги сравниваются целочисленно через `long`, чтобы избежать округления
и переполнения при большом начальном здоровье. При нулевом начальном здоровье
выбирается тяжёлое состояние. Сохраняется прежнее правило уничтожения:
танк жив при нуле здоровья и удаляется при значении строго меньше нуля.

Состояние применяется и при прямом вызове `shoot()`, и при выполнении ранее
созданного `ShootCommand`. Изменение состояния сохраняет пройденную долю
движения, резервирование клеток и завершённую часть перезарядки; новые
множители действуют на оставшуюся часть. Во время перезарядки тяжёлого танка
таймер продолжает идти, но выстрел запрещён.

Для нового эффекта достаточно реализовать `TankState` и вызвать
`tank.setState(effect)`. Например, бустер может вернуть `2` из
`movementSpeedMultiplier()` и `reloadSpeedMultiplier()` — танк будет ехать
и перезаряжаться вдвое быстрее. По умолчанию новый урон переводит любой такой
эффект в состояние по здоровью; переопределение `onHealthChanged` позволяет
задать другой переход. Продолжительность и совмещение будущих бустеров
в этом задании не вводятся. Расширение проверяется отдельным тестовым
состоянием без изменений `Tank`, команд или контроллеров.
