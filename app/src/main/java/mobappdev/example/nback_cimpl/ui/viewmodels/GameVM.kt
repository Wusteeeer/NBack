package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository

/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: Int

    val purple: Color
        get() = Color(0xFF35143e)
    val red: Color
        get() = Color(0xFF541646)
    val violate: Color
        get() = Color(0xFF5d2d75)
    val darkBlue: Color
        get() = Color(0xFF644f9c)
    val skyBlue: Color
        get() = Color(0xFF6d7ab7)
    val oceanBlue: Color
        get() = Color(0xFF89b7d0)
    val cloudBlue: Color
        get() = Color(0xFFa6e6e5)
    val lightBlue: Color
        get() = Color(0xFFc7f0e6)

    fun setGameType(gameType: GameType)

    fun getSquare(index:Int):SquareCard;


    fun startGame()

    fun checkMatch()
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
): GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    private val squares: MutableList<SquareCard> = arrayListOf();


    // nBack is currently hardcoded
    override val nBack: Int = 2

    private var correctPositionAnswer: Int = -1;
    private var hasClickedPosition: Boolean = false;

    private var job: Job? = null  // coroutine job for the game event
    private val eventInterval: Long = 2000L  // 2000 ms (2s)

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var events = emptyArray<Int>()  // Array with all events


    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun getSquare(index:Int):SquareCard{
        return squares[index];
    }

    override fun startGame() {
        job?.cancel()  // Cancel any existing game loop

        for(i in 0..9){
            squares.add(SquareCard(cloudBlue));
        }

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        events = nBackHelper.generateNBackString(10, 9, 30, nBack).toList().toTypedArray()  // Todo Higher Grade: currently the size etc. are hardcoded, make these based on user input
        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame()
                GameType.AudioVisual -> runAudioVisualGame()
                GameType.Visual -> runVisualGame(events)
            }
            // Todo: update the highscore
        }
    }

    private fun checkVisual(){
        if(_gameState.value.eventValue == correctPositionAnswer){
            squares[correctPositionAnswer-1].color = Color.Green;
            //TODO: Fix scoring!
            _score.value++;
        }else{
            squares[_gameState.value.eventValue-1].color = Color.Red;
        }
    }

    override fun checkMatch() {

        when(_gameState.value.gameType){
            GameType.Audio -> {

            }
            GameType.Visual -> {
                if(hasClickedPosition) return;

                hasClickedPosition = true;
                checkVisual();
            }
            GameType.AudioVisual -> {
                if(hasClickedPosition) return;
            }
        }


    }
    private fun runAudioGame() {
        // Todo: Make work for Basic grade
        println("Running audio game!");
    }

    private suspend fun runVisualGame(events: Array<Int>){
        // Todo: Replace this code for actual game code
        println("Running visual game!");
        var i = 1;
        for (value in events) {
0
            hasClickedPosition = false;
            for(square in squares){
                square.color = cloudBlue;
            }

            _gameState.value = _gameState.value.copy(eventValue = value)
            squares[_gameState.value.eventValue-1].color = oceanBlue;

            println("Current: ${_gameState.value.eventValue}, Correct: $correctPositionAnswer")
            if(i >= nBack){
                correctPositionAnswer = events[i-nBack];
            }
            i++;
            delay(eventInterval)
        }



    }

    private fun runAudioVisualGame(){
        // Todo: Make work for Higher grade
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        // Code that runs during creation of the vm
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}

// Class with the different game types
enum class GameType{
    Audio,
    Visual,
    AudioVisual
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val eventValue: Int = -1  // The value of the array string
)

class FakeVM: GameViewModel{
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val nBack: Int
        get() = 2

    override fun setGameType(gameType: GameType) {
    }

    override fun startGame() {
    }

    override fun checkMatch() {
    }

    override fun getSquare(index:Int):SquareCard{
        return SquareCard(oceanBlue);
    }

}