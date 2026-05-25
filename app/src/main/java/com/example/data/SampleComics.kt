package com.example.data

import com.example.data.db.Comic

object SampleComics {
    val list = listOf(
        Comic(
            title = "Metal Gear Solid: Bande Dessinée",
            artist = "Ashley Wood",
            volume = "Vol. 1 (Special Issue)",
            filePath = "sample_metal_gear",
            coverPath = "sample_metal_gear",
            pageCount = 10,
            currentPage = 0
        ),
        Comic(
            title = "Gravity Rush: Hekseville Saga",
            artist = "Shigenori Soejima",
            volume = "Vol. 2 (Dual Altar)",
            filePath = "sample_gravity_rush",
            coverPath = "sample_gravity_rush",
            pageCount = 8,
            currentPage = 0
        ),
        Comic(
            title = "PSP™ Quick Start Manga Guide",
            artist = "Sony PlayStation® Japan",
            volume = "Volume 1000",
            filePath = "sample_manga_guide",
            coverPath = "sample_manga_guide",
            pageCount = 6,
            currentPage = 0
        )
    )
}
