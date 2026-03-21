## [1.1.0] - 2026-03-21

### 🚀 Features

- *(jaxb)* Улучшена работа ChangeHistory. При удалении свойств из наблюдаемых коллекций, наблюдение за ними прекращается
- *(jaxb)* Добавлен ActionTimer
- *(owl)* Добавлен кэш для пользователей LazyOwl
- *(owl)* Добавлен метод fileName() в интерфейс HollowOwl

### 🐛 Bug Fixes

- *(owl)* Добавлен отсутствующий @XmlRootElement в LazyOwlEntity
- *(owl)* Исправлена ошибка проверки создаваемого имени файла на наличие расширения
- *(jaxb)* Исправлена работа ChangeHistory с ObservableList

### 🔨 Dependency Upgrades

- Изменена версия space.sadfox:owlook-bom с 0.7.3 на 1.0.0

### 🚜 Refactor

- *(owl)* Добавление логирования в Owl
- *(owl)* Добавление логирования в OwlFileSystem
- *(owl)* Override equals() and hashCode() in HashMapAdapter
- *(moduleapi)* Изменен модификатор доступа для OwlookModuleInfo.RequireOwlookModuleAdapter
- *(owl)* Автосохранение Owl переделано с помощью ActionTimer

### 🧪 Testing

- *(moduleapi)* Добавлены тесты для VersionFormat
- *(owl)* Добавлены тесты для HashMapAdapter
- Добавлена simple реализация SLF4J для прохождения тестов
- *(owl)* Добавлены тесты для Owl
- *(owl)* Добавлены тесты для Owls
- *(jaxb)* Добавлены тесты для ChangeHistory

### ⚙️ Miscellaneous Tasks

- Исправление .gitignore
## [1.0.0] - 2026-02-24
