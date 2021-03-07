# TestTask
По условию задачи необходимо написать построчную сортировку большого текстового файла, не влезающего в
оперативную память.
Размер требуемой памяти не должен зависеть от размера файла.
Длина строки разумная, одна строка сильно меньше, чем объем памяти.
Для проверки работоспособности нужен генератор таких файлов, принимающий в
качестве параметров количество строк и их максимальную длину.

Для решения задачи использован метод сортировки слиянием в двух версиях:
1) С минимальным использованием оперативной памяти. Класс SortFileImplVerSaveRAM.
В данном решении поочередно считываются по 1 строке из 2 файлов, затем сравниваются и сразу записываются в итоговый файл.
Поэтому использовании RAM минимально.
2) Эффективное использование доступной оперативной памяти. Класс SortFileImplVerEffectiveRAM.
В данном решении используются 3  LinkedList<String> ограниченых размером доступной RAM, 2 для сравнения listCompare1 и listCompare2,
и один результирующий listResult. После заполения резултирующего списка он записывается в файл. Что существенно ускоряет процесс сортировки.

Результаты тестов.

Файл в 1 000 000 000 строк. 
Размер буфера COUNT_LINES_AVAILABLE_RAM = 10_000_000;
Длинна строки не более 20 символов. 

Время создания файла: 536 325 мсек или почти 8 минут 56 сек Размер файла 11,6 ГБ (12 499 969 222 байт)
Время сортировки с эффективным использованием доступной RAM:  5052 072 мсек или 84,2 минуты

Сортировка с минимальным использованием оперативной памяти достаточно медленная, так как происходит очень много операций чтения и записи.

Для сравнения:

Файл в 100_000 строк. 
Размер буфера COUNT_LINES_AVAILABLE_RAM = 1000;
Длинна строки не более 20 символов. 
Время создания файла: 203  мсек или почти 8 минут 56 сек Размер файла 1,19 МБ (1 248 285 байт)

Время сортировки с эффективным использованием доступной RAM: 1 390 мсек или 1,39 секунды
Время сортировки с минимальным использованием доступной RAM:  82 044 мсек или 1 минута 22 секунды

"Эффективный" способ быстрее в 59 раз, зато при ограниченном ресурсе RAM и не ограниченном времени подойдет и "минимальный" способ.

            