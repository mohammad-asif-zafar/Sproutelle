package com.hathway.sproutelle.presentation.picturequest.components

object PictureQuestEmojiMapper {
    fun getPictureEmoji(text: String): String {
        val clean = text.lowercase()
        return when {
            clean.contains("mushroom") -> "🍄"
            clean.contains("chili") || clean.contains("pepper") -> "🌶️"
            clean.contains("pumpkin") -> "🎃"
            clean.contains("pear") -> "🍐"
            clean.contains("orange") -> "🍊"
            clean.contains("mango") -> "🥭"
            clean.contains("apple") -> "🍎"
            clean.contains("banana") -> "🍌"
            clean.contains("strawberry") -> "🍓"
            clean.contains("watermelon") -> "🍉"
            clean.contains("dog") -> "🐶"
            clean.contains("cat") -> "🐱"
            clean.contains("lion") -> "🦁"
            clean.contains("bear") -> "🐻"
            clean.contains("car") -> "🚗"
            clean.contains("plane") -> "✈️"
            clean.contains("star") -> "⭐"
            clean.contains("ball") -> "⚽"
            else -> "🖼️"
        }
    }
}
