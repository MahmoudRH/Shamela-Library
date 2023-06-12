package com.shamela.library.presentation.utils

import com.shamela.library.domain.model.Book
import com.shamela.library.domain.model.Category
import kotlinx.coroutines.delay

object FakeRepo {
    suspend fun getUserBooks(): List<Book> {
        delay(10)
        return listOf(
            Book(
                id = "",
                title = "هداية الحيارى في أجوبة اليهود والنصارى",
                author = "ابن القيم",
                pageCount = 720
            ),
            Book(
                id = "",
                title = "نقد مراتب الإجماع",
                author = "ابن تيمية",
                pageCount = 430
            ),
            Book(
                id = "",
                title = "مختصر الصواعق المرسلة على الجهمية والمعطلة",
                author = "ابن القيم",
                pageCount = 353
            ),
            Book(
                id = "",
                title = "قاعدة مختصرة في وجوب طاعة الله ورسوله وولاة الأمور",
                author = "ابن تيمية",
                pageCount = 1265
            ),
        )
    }

    suspend fun getUserCategories(): List<Category> {
        delay(10)
        return listOf(
            Category(
                id = "",
                name = "كتب ابن القيم",
                bookCount = 35
            ),
            Category(
                id = "",
                name = "كتب ابن تيمية",
                bookCount = 35
            ),
            Category(
                id = "",
                name = "كتب اللغة",
                bookCount = 35
            ),
        )
    }

}