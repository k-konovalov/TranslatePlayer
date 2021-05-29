package ru.konovalovk.translateplayer.logic

import java.util.concurrent.TimeUnit

fun convertSecondsToHMmSs(millis: Long): String {
    return String.format("%02d:%02d:%02d",
        TimeUnit.MILLISECONDS.toHours(millis),
        TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)), // The change is in this line
        TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
}

val transparentMap = mutableMapOf<Int, String>().apply {
    set(100, "FF")
    set(99, "FC")
    set(98, "FA")
    set(97, "F7")
    set(96, "F5")
    set(95, "F2")
    set(94, "F0")
    set(93, "ED")
    set(92, "EB")
    set(91, "E8")
    set(90, "E6")
    set(89, "E3")
    set(88, "E0")
    set(87, "DE")
    set(86, "DB")
    set(85, "D9")
    set(84, "D6")
    set(83, "D4")
    set(82, "D1")
    set(81, "CF")
    set(80, "CC")
    set(79, "C9")
    set(78, "C7")
    set(77, "C4")
    set(76, "C2")
    set(75, "BF")
    set(74, "BD")
    set(73, "BA")
    set(72, "B8")
    set(71, "B5")
    set(70, "B3")
    set(69, "B0")
    set(68, "AD")
    set(67, "AB")
    set(66, "A8")
    set(65, "A6")
    set(64, "A3")
    set(63, "A1")
    set(62, "9E")
    set(61, "9C")
    set(60, "99")
    set(59, "96")
    set(58, "94")
    set(57, "91")
    set(56, "8F")
    set(55, "8C")
    set(54, "8A")
    set(53, "87")
    set(52, "85")
    set(51, "82")
    set(50, "80")
    set(49, "7D")
    set(48, "7A")
    set(47, "78")
    set(46, "75")
    set(45, "73")
    set(44, "70")
    set(43, "6E")
    set(42, "6B")
    set(41, "69")
    set(40, "66")
    set(39, "63")
    set(38, "61")
    set(37, "5E")
    set(36, "5C")
    set(35, "59")
    set(34, "57")
    set(33, "54")
    set(32, "52")
    set(31, "4F")
    set(30, "4D")
    set(29, "4A")
    set(28, "47")
    set(27, "45")
    set(26, "42")
    set(25, "40")
    set(24, "3D")
    set(23, "3B")
    set(22, "38")
    set(21, "36")
    set(20, "33")
    set(19, "30")
    set(18, "2E")
    set(17, "2B")
    set(16, "29")
    set(15, "26")
    set(14, "24")
    set(13, "21")
    set(12, "1F")
    set(11, "1C")
    set(10, "1A")
    set(9, "17")
    set(8, "14")
    set(7, "12")
    set(6, "0F")
    set(5, "0D")
    set(4, "0A")
    set(3, "08")
    set(2, "05")
    set(1, "03")
    set(0, "00")
}