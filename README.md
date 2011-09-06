Информер Яндекс.Метрики для Android-устройств, оформленный в виде экранного виджета

Работает через [API Яндекс.Метрики](http://api.yandex.ru/metrika)
Использует библиотеку [Metrika4j](https://github.com/Arturus/Metrika4j)

Сборка виджета:

1. Перейти в каталог widget
2. Запустить команду `android update project --path .` (подразумевается, что путь к каталогу [Android SDK]/tools есть в PATH)
2. Запустить `ant debug` или `ant release`

Проект также включает Android-модуль для Intellij IDEA 10 (widget.iml)

Готовый виджет, подписанный debug ключом, можно загрузить отсюда: https://github.com/downloads/Arturus/MetrikaWidget/MetrikaWidget-debug.apk
или через страницу с QR кодом: https://github.com/Arturus/MetrikaWidget/MetrikaWidget-debug.apk/qr_code

