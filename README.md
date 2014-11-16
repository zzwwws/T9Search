T9Search
========

Function:
In order to demo search contacts from T9 input
1.Support Phone Number search
2.Support name search
	(1)support Chinese characters search
	(2)support Pinyin and polyphone search

Process：
1.Read contact to get basic data.
2.Parse basic data according to certain rules.
3.Get T9 input string.
4.Match T9 input string with the data which have parsed.
5.Show match result.

Library：
pinyin4j, a Java library converting Chinese to pinyin.
http://pinyin4j.sourceforge.net/


