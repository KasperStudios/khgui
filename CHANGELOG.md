# KHGui Changelog

## v1.0.0 - Initial Release (2026-02-24)

### 🎨 Полностью кастомизируемая система тем
- ✅ `ITheme` интерфейс для создания кастомных тем
- ✅ `CyberPurpleTheme` - Catppuccin Mocha тема (по умолчанию, фиолетовая)
- ✅ `DarkTheme` - классическая VS Code тема
- ✅ `ThemeManager` - управление темами с API для регистрации
- ✅ Все цвета берутся из темы (фон, текст, синтаксис, UI элементы)
- ✅ Рендеринг UI с цветами темы вместо текстур

### 🖼️ Система PNG иконок
- ✅ `IconManager` - загрузка и рендеринг PNG иконок
- ✅ Поддержка кастомных иконок для панелей
- ✅ 3 базовых иконки включены (explorer, search, debug)
- ✅ Альфа-канал и прозрачность

### 📐 Улучшенный Layout
- ✅ `GuiLayout` с динамическим размером sidebar (170-400px)
- ✅ Resize handle для изменения размера мышью
- ✅ State management для UI состояний
- ✅ Респонсивная система
- ✅ Относительные размеры (sidebar 15% от ширины экрана)
- ✅ Activity bar 40px фиксированно

### 💡 Автокомплит и подсказки
- ✅ `ICompletionProvider` - API для автокомплита
- ✅ `IHoverProvider` - API для hover-подсказок
- ✅ `CompletionPopup` - всплывающее окно с предложениями
- ✅ `HoverTooltip` - tooltip с документацией
- ✅ Поддержка типов: METHOD, FUNCTION, VARIABLE, KEYWORD, SNIPPET, etc.
- ✅ Иконки и цвета по типу элемента
- ✅ Навигация стрелками, выбор Enter
- ✅ Ctrl+Space для вызова автокомплита

### 🎯 Улучшенный рендеринг
- ✅ `ThemedCodeRenderer` - рендерер с поддержкой тем
- ✅ Синтаксис подсвечивается цветами из темы
- ✅ Курсор использует цвет из темы
- ✅ Номера строк с активной строкой
- ✅ Mouse click cursor positioning - клик мышью ставит курсор

### 🔧 Система панелей
- ✅ `PanelManager` - управление sidebar панелями
- ✅ Регистрация кастомных панелей
- ✅ Переключение между панелями
- ✅ 3 базовых панели (Explorer, Search, Debug)
- ✅ Activity bar toggle только на иконках (не на всей области)

### 🔘 Кастомные кнопки в toolbar
- ✅ `IToolbarButton` - API для кастомных кнопок
- ✅ `ToolbarManager` - управление кнопками
- ✅ Поддержка PNG иконок или текстовых меток
- ✅ Enabled/disabled состояния
- ✅ Tooltips
- ✅ Демо кнопки: Run, Stop, Debug

### ⌨️ Улучшенный ввод
- ✅ `InputHandler` с поддержкой Ctrl+Space
- ✅ Все базовые операции редактирования
- ✅ Навигация курсором
- ✅ Горячие клавиши

### 🎮 Интерактивность
- ✅ `HitMap` система для точных кликов
- ✅ Hover эффекты на UI элементах
- ✅ Активная панель подсвечивается
- ✅ Клик для переключения панелей
- ✅ Скроллинг колесиком мыши
- ✅ Клик мышью для позиционирования курсора

### 🎨 Кастомизация title bar
- ✅ `getTitleIcon()` в IEditorContext
- ✅ Поддержка кастомных иконок/логотипов в title bar
- ✅ Цвет иконки из темы (primary accent)

### 📦 Multiloader
- ✅ Полная поддержка Fabric
- ✅ Полная поддержка NeoForge
- ✅ Паритет функционала между лоадерами

### 📚 Документация
- ✅ README.md с примерами
- ✅ AI_GUIDE.md - техническая документация
- ✅ INTEGRATION_EXAMPLE.md - примеры интеграции
- ✅ TESTING_GUIDE.md - гайд по тестированию
- ✅ Javadoc для всех публичных API

### 🎨 Demo режим
- ✅ Демо-провайдеры для автокомплита и hover
- ✅ Тестовый контент
- ✅ Открывается по нажатию H
- ✅ Демо toolbar кнопки

## API для разработчиков

### Создание кастомной темы
```java
public class MyTheme implements ITheme {
    @Override public int getEditorBackground() { return 0xFF1A1A1A; }
    // ... остальные методы
}

ThemeManager.registerTheme(new MyTheme());
ThemeManager.setTheme("My Theme");
```

### Создание провайдера автокомплита
```java
public class MyCompletionProvider implements ICompletionProvider {
    @Override
    public List<CompletionItem> getCompletions(String line, int column, String prefix) {
        // Ваша логика
    }
}
```

### Создание кастомной панели
```java
public class MyPanel implements IEditorPanel {
    @Override public String getId() { return "mypanel"; }
    @Override public String getIcon() { return "textures/icons/myicon.png"; }
    // ... остальные методы
}
```

## Требования
- Minecraft 1.21.1
- Java 21
- Fabric Loader 0.18.4+ или NeoForge 21.1.215+

## Известные ограничения
- GUI Scale > 1 может вызвать размытие
- Clipboard операции (copy/paste) - заглушки
- Undo/redo - заглушки
- Text selection - не реализовано

## Планы на будущее
- Полная реализация clipboard
- Undo/redo система
- Text selection с мышью
- Контекстные меню
- Tab management (close, reorder)
- Split view
- Minimap
- Search and replace
