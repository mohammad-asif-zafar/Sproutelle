package com.hathway.sproutelle.presentation.picturequest

import com.hathway.sproutelle.domain.model.GameConfig
import com.hathway.sproutelle.domain.model.GameItem
import com.hathway.sproutelle.domain.model.GameLevel
import com.hathway.sproutelle.domain.model.GameProgress
import com.hathway.sproutelle.domain.model.Question
import com.hathway.sproutelle.domain.model.QuestionOption
import com.hathway.sproutelle.domain.model.Subcategory
import com.hathway.sproutelle.domain.model.SubcategoryWithGames
import com.hathway.sproutelle.domain.repository.LearningRepository
import com.hathway.sproutelle.domain.repository.LearningState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PictureQuestViewModelTest {

    private class FakeLearningRepository(
        configToReturn: GameConfig = GameConfig(gameId = "game_1", name = "Picture Quest", pointsPerCorrect = 10),
        levelsToReturn: List<GameLevel> = emptyList(),
        questionsToReturn: List<Question> = emptyList()
    ) : LearningRepository {

        val savedProgress = mutableListOf<GameProgress>()

        private val configFlow = MutableStateFlow<LearningState<GameConfig>>(LearningState.Fresh(configToReturn))
        private val levelsFlow = MutableStateFlow<LearningState<List<GameLevel>>>(LearningState.Fresh(levelsToReturn))
        private val questionsFlow = MutableStateFlow<LearningState<List<Question>>>(LearningState.Fresh(questionsToReturn))

        override fun getGameConfigState(gameId: String): Flow<LearningState<GameConfig>> = configFlow
        override fun getGameLevelsState(gameId: String): Flow<LearningState<List<GameLevel>>> = levelsFlow
        override fun getQuestionsState(subcategoryId: String, gameId: String): Flow<LearningState<List<Question>>> = questionsFlow

        override suspend fun refreshGameConfig(gameId: String): Result<Unit> = Result.success(Unit)
        override suspend fun refreshGameLevels(gameId: String): Result<Unit> = Result.success(Unit)
        override suspend fun refreshQuestions(subcategoryId: String, gameId: String): Result<Unit> = Result.success(Unit)

        override suspend fun saveGameProgress(progress: GameProgress) {
            savedProgress.add(progress)
        }

        override suspend fun getGameProgress(childId: String, gameId: String, levelId: String?): GameProgress? {
            return savedProgress.lastOrNull { it.childId == childId && it.gameId == gameId && it.levelId == levelId }
        }

        override fun getSubcategories(explorerId: String): Flow<List<Subcategory>> = MutableStateFlow(emptyList())
        override fun getSubcategoriesState(explorerId: String): Flow<LearningState<List<Subcategory>>> = MutableStateFlow(LearningState.Loading)
        override suspend fun refreshSubcategories(explorerId: String): Result<Unit> = Result.success(Unit)

        override fun getGamesForSubcategory(subcategoryId: String): Flow<List<GameItem>> = MutableStateFlow(emptyList())
        override fun getGamesForSubcategoryState(subcategoryId: String): Flow<LearningState<List<GameItem>>> = MutableStateFlow(LearningState.Loading)
        override suspend fun refreshGamesForSubcategory(subcategoryId: String): Result<Unit> = Result.success(Unit)

        override fun getGamesForExplorer(explorerId: String): Flow<List<SubcategoryWithGames>> = MutableStateFlow(emptyList())
        override fun getGamesForExplorerState(explorerId: String): Flow<LearningState<List<SubcategoryWithGames>>> = MutableStateFlow(LearningState.Loading)
        override suspend fun refreshGamesForExplorer(explorerId: String): Result<Unit> = Result.success(Unit)

        override fun getQuestions(subcategoryId: String, gameId: String): Flow<List<Question>> = MutableStateFlow(emptyList())

        override fun preloadSubcategories(explorerId: String) {}
        override fun preloadGames(subcategoryId: String) {}
        override fun preloadGamesForExplorer(explorerId: String) {}
        override fun preloadQuestions(subcategoryId: String, gameId: String) {}
    }

    private fun createQuestion(id: String, levelId: String?, text: String): Question {
        return Question(
            id = id,
            gameId = "game_1",
            subcategoryId = "sub_1",
            levelId = levelId,
            text = text,
            type = "find_correct_picture",
            instructions = null,
            sortOrder = 1,
            isDraft = false,
            options = listOf(
                QuestionOption(id = "${id}_opt1", questionId = id, text = "Correct", isCorrect = true, sortOrder = 1),
                QuestionOption(id = "${id}_opt2", questionId = id, text = "Wrong", isCorrect = false, sortOrder = 2)
            )
        )
    }

    @Test
    fun test1_SwitchingBetweenTwoPopulatedLevels(): Unit = runBlocking {
        val level1 = GameLevel("lvl_1", "game_1", 1, "Level 1")
        val level2 = GameLevel("lvl_2", "game_1", 2, "Level 2")

        val q1 = createQuestion("q1", "lvl_1", "Apple")
        val q2 = createQuestion("q2", "lvl_2", "Banana")

        val repo = FakeLearningRepository(
            levelsToReturn = listOf(level1, level2),
            questionsToReturn = listOf(q1, q2)
        )

        val viewModel = PictureQuestViewModel("exp_1", "sub_1", "game_1", "Picture Quest", repo)

        val initialState = viewModel.uiState.first { it is PictureQuestUiState.Success } as PictureQuestUiState.Success
        assertEquals(0, initialState.currentLevelIndex)
        assertEquals(1, initialState.questions.size)
        assertEquals("q1", initialState.questions[0].id)

        // Switch to Level 2
        viewModel.selectLevel(1)

        val level2State = viewModel.uiState.value as PictureQuestUiState.Success
        assertEquals(1, level2State.currentLevelIndex)
        assertEquals(1, level2State.questions.size)
        assertEquals("q2", level2State.questions[0].id)
    }

    @Test
    fun test2_SelectingLevelWithNoQuestions(): Unit = runBlocking {
        val level1 = GameLevel("lvl_1", "game_1", 1, "Level 1")
        val level2 = GameLevel("lvl_2", "game_1", 2, "Level 2")

        val q1 = createQuestion("q1", "lvl_1", "Apple")

        val repo = FakeLearningRepository(
            levelsToReturn = listOf(level1, level2),
            questionsToReturn = listOf(q1)
        )

        val viewModel = PictureQuestViewModel("exp_1", "sub_1", "game_1", "Picture Quest", repo)

        viewModel.uiState.first { it is PictureQuestUiState.Success }

        // Select empty Level 2
        viewModel.selectLevel(1)

        val state = viewModel.uiState.value as PictureQuestUiState.Success
        assertEquals(1, state.currentLevelIndex)
        assertTrue(state.questions.isEmpty())
        assertEquals(2, state.levels.size)
    }

    @Test
    fun test3_SwitchingFromEmptyLevelToPopulatedLevel(): Unit = runBlocking {
        val level1 = GameLevel("lvl_1", "game_1", 1, "Level 1")
        val level2 = GameLevel("lvl_2", "game_1", 2, "Level 2")

        val q1 = createQuestion("q1", "lvl_1", "Apple")

        val repo = FakeLearningRepository(
            levelsToReturn = listOf(level1, level2),
            questionsToReturn = listOf(q1)
        )

        val viewModel = PictureQuestViewModel("exp_1", "sub_1", "game_1", "Picture Quest", repo)

        viewModel.uiState.first { it is PictureQuestUiState.Success }

        // Switch to empty Level 2
        viewModel.selectLevel(1)
        var state = viewModel.uiState.value as PictureQuestUiState.Success
        assertTrue(state.questions.isEmpty())

        // Switch back to Level 1
        viewModel.selectLevel(0)
        state = viewModel.uiState.value as PictureQuestUiState.Success
        assertEquals(0, state.currentLevelIndex)
        assertEquals(1, state.questions.size)
        assertEquals("q1", state.questions[0].id)
    }

    @Test
    fun test4_GameWithLevelsButZeroQuestions(): Unit = runBlocking {
        val level1 = GameLevel("lvl_1", "game_1", 1, "Level 1")
        val level2 = GameLevel("lvl_2", "game_1", 2, "Level 2")

        val repo = FakeLearningRepository(
            levelsToReturn = listOf(level1, level2),
            questionsToReturn = emptyList()
        )

        val viewModel = PictureQuestViewModel("exp_1", "sub_1", "game_1", "Picture Quest", repo)

        val state = viewModel.uiState.first { it is PictureQuestUiState.Success } as PictureQuestUiState.Success
        assertTrue(state.questions.isEmpty())
        assertEquals(2, state.levels.size)
    }

    @Test
    fun test5_GameWithoutLevelsAndZeroQuestions(): Unit = runBlocking {
        val repo = FakeLearningRepository(
            levelsToReturn = emptyList(),
            questionsToReturn = emptyList()
        )

        val viewModel = PictureQuestViewModel("exp_1", "sub_1", "game_1", "Picture Quest", repo)

        val state = viewModel.uiState.first { it is PictureQuestUiState.Empty }
        assertEquals(PictureQuestUiState.Empty, state)
    }

    @Test
    fun test6_SwitchingLevelsAfterSubmittingAnswer(): Unit = runBlocking {
        val level1 = GameLevel("lvl_1", "game_1", 1, "Level 1")
        val level2 = GameLevel("lvl_2", "game_1", 2, "Level 2")

        val q1 = createQuestion("q1", "lvl_1", "Apple")
        val q2 = createQuestion("q2", "lvl_2", "Banana")

        val repo = FakeLearningRepository(
            levelsToReturn = listOf(level1, level2),
            questionsToReturn = listOf(q1, q2)
        )

        val viewModel = PictureQuestViewModel("exp_1", "sub_1", "game_1", "Picture Quest", repo)

        viewModel.uiState.first { it is PictureQuestUiState.Success }

        // Select correct answer on Q1
        viewModel.selectOption("q1_opt1")

        var state = viewModel.uiState.value as PictureQuestUiState.Success
        assertTrue(state.isOptionSubmitted)
        assertEquals(true, state.isCorrect)
        assertEquals(10, state.score)
        assertEquals(1, state.attemptsCount)

        // Switch to Level 2
        viewModel.selectLevel(1)

        state = viewModel.uiState.value as PictureQuestUiState.Success
        assertEquals(1, state.currentLevelIndex)
        assertNull(state.selectedOptionId)
        assertFalse(state.isOptionSubmitted)
        assertNull(state.isCorrect)
        assertEquals(0, state.score)
        assertEquals(0, state.attemptsCount)
        assertTrue(state.completedQuestionIndices.isEmpty())
    }

    @Test
    fun test7_VerifyingAllRequiredGameStateResets(): Unit = runBlocking {
        val level1 = GameLevel("lvl_1", "game_1", 1, "Level 1")
        val level2 = GameLevel("lvl_2", "game_1", 2, "Level 2")

        val q1 = createQuestion("q1", "lvl_1", "Apple")
        val q2 = createQuestion("q2", "lvl_2", "Banana")

        val repo = FakeLearningRepository(
            levelsToReturn = listOf(level1, level2),
            questionsToReturn = listOf(q1, q2)
        )

        val viewModel = PictureQuestViewModel("exp_1", "sub_1", "game_1", "Picture Quest", repo)

        viewModel.uiState.first { it is PictureQuestUiState.Success }

        // Change state on Level 1
        viewModel.selectOption("q1_opt1")
        viewModel.toggleHint()

        // Switch to Level 2
        viewModel.selectLevel(1)

        val state = viewModel.uiState.value as PictureQuestUiState.Success
        assertEquals(0, state.score)
        assertEquals(0, state.attemptsCount)
        assertNull(state.selectedOptionId)
        assertFalse(state.isOptionSubmitted)
        assertNull(state.isCorrect)
        assertTrue(state.completedQuestionIndices.isEmpty())
        assertEquals(0, state.currentQuestionIndex)
        assertFalse(state.showHint)
        assertFalse(state.isGameCompleted)
    }

    @Test
    fun test8_VerifyingProgressUsesSelectedLevelId(): Unit = runBlocking {
        val level1 = GameLevel("lvl_1", "game_1", 1, "Level 1")
        val level2 = GameLevel("lvl_2", "game_1", 2, "Level 2")

        val q1 = createQuestion("q1", "lvl_1", "Apple")
        val q2 = createQuestion("q2", "lvl_2", "Banana")

        val repo = FakeLearningRepository(
            levelsToReturn = listOf(level1, level2),
            questionsToReturn = listOf(q1, q2)
        )

        val viewModel = PictureQuestViewModel("exp_1", "sub_1", "game_1", "Picture Quest", repo)

        viewModel.uiState.first { it is PictureQuestUiState.Success }

        // Switch to Level 2 and answer Q2
        viewModel.selectLevel(1)
        viewModel.selectOption("q2_opt1")

        val lastProgress = repo.savedProgress.lastOrNull()
        assertNotNull(lastProgress)
        assertEquals("lvl_2", lastProgress.levelId)
        assertEquals("game_1", lastProgress.gameId)
        assertEquals(10, lastProgress.score)
    }
}
