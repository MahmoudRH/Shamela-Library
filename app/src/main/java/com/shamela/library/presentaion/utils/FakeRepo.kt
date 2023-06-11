package com.shamela.library.presentaion.utils

import com.shamela.library.presentaion.model.Book
import com.shamela.library.presentaion.model.Section
import kotlinx.coroutines.delay

object FakeRepo {
    suspend fun getUserBooks(): List<Book> {
        delay(10)
        return listOf(
            Book(
                title = "هداية الحيارى في أجوبة اليهود والنصارى",
                author = "ابن القيم"
            ),
            Book(
                title = "نقد مراتب الإجماع",
                author = "ابن تيمية"
            ),
            Book(
                title = "مختصر الصواعق المرسلة على الجهمية والمعطلة",
                author = "ابن القيم"
            ),
            Book(
                title = "قاعدة مختصرة في وجوب طاعة الله ورسوله وولاة الأمور",
                author = "ابن تيمية"
            ),
        )
    }

    suspend fun getUserSections(): List<Section> {
        delay(10)
        return listOf(
            Section(
                title = "كتب ابن القيم",
                numberOfBooks = 35
            ),
            Section(
                title = "كتب ابن تيمية",
                numberOfBooks = 69
            ),
            Section(
                title = "كتب اللغة",
                numberOfBooks = 40
            ),
        )
    }

}