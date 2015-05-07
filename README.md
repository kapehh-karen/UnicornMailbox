# UnicornMailbox
Плагин для отправки вещей другим игрокам.

Формат таблицы:
<pre>id
raw - набор байт
info - текстовое представление вещи
sended_date - дата отправления
received_date - дата получения
from - никнейм отправителя
to - никнейм получателя
is_received - статус посылки: 0 - не получен и 1 - получен</pre>

Команды:
<ul>
<li><code>/mail send [nickname]</code> - отправляет вещь в руках игроку <b>nickname</b></li>
<li><code>/mail receiv</code> - получает все посылки</li>
<li><code>/mail give [chestname] [nickname]</code> - отправляет рандомный сундук игроку (см. <a href="//github.com/kapehh-karen/RandomChest">RandomChest</a>)</li>
</ul>
