package mobappdev.example.nback_cimpl.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository
import kotlin.math.round
import android.speech.tts.TextToSpeech
import java.util.Locale


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

    fun setGameState(gameState: State)

    fun getTime():Long;

    fun getDim():Int;

    fun getSquare(index:Int):SquareCard;

    fun startGame(context: Context, interval:Int, nback:Int, rounds:Int, dim:Int)

    fun checkMatch(buttonType:String):Boolean;

    fun endGame();
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
    private val alphabet = arrayListOf<String>("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
    private var textToSpeech: TextToSpeech?=null


    override var nBack: Int = 2

    private var correctPositionAnswer: Int = -1;
    private var correctAudioAnswer:Int = -1;
    private var hasClickedPosition: Boolean = false;
    private var hasClickedAudio: Boolean = false;

    private var job: Job? = null  // coroutine job for the game event
    private var eventInterval: Long = 2000L  // 2000 ms (2s)
    private var dimensions:Int = 3; //3x3

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var events = emptyArray<Int>()  // Array with all events

    override fun setGameType(gameType: GameType) {

        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
        println(_gameState.value.gameType)
    }

    override fun setGameState(gameState: State){
        _gameState.value = _gameState.value.copy(state=gameState)
    }

    override fun getSquare(index:Int):SquareCard{
        return squares[index];
    }

    override fun getDim():Int{
        return dimensions;
    }

    override fun getTime():Long{
        return eventInterval;
    }

    override fun startGame(context: Context, interval:Int, nback:Int, rounds:Int, dim:Int) {
        job?.cancel()  // Cancel any existing game loop

        dimensions = dim;
        setGameState(State.RUNNING)

        eventInterval = interval.toLong()*1000L;
        nBack = nback;

        textToSpeech = TextToSpeech(context){
            if(it == TextToSpeech.SUCCESS){
                textToSpeech?.let{txtToSpeech ->
                    txtToSpeech.language = Locale.ENGLISH
                    txtToSpeech.setSpeechRate(1.0f)
                }

            }
        }

        squares.clear()
        for(i in 0..<(dimensions*dimensions)){
            squares.add(SquareCard(cloudBlue));
        }

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        events = nBackHelper.generateNBackString(rounds, dimensions*dimensions, 30, nBack).toList().toTypedArray()
        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")

        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame(events)
                GameType.AudioVisual -> runAudioVisualGame(events)
                GameType.Visual -> runVisualGame(events)
            }

            if(score.value > highscore.value)
                userPreferencesRepository.saveHighScore(score.value)

            endGame()
        }
    }

    override fun checkMatch(buttonType:String):Boolean {

        when(buttonType){
            "Audio" -> {
                if(hasClickedAudio || gameState.value.gameType == GameType.Visual) return false;
                hasClickedAudio = true;

                if(_gameState.value.eventValue == correctAudioAnswer){
                    _score.value++;
                    return true;
                }

            }
            "Visual" -> {
                if(hasClickedPosition || gameState.value.gameType == GameType.Audio) return false;
                hasClickedPosition = true;


                if(_gameState.value.eventValue == correctPositionAnswer){
                    _score.value++;
                    return true;
                }
            }
        }

        return false;

    }
    private suspend fun runAudioGame(events: Array<Int>) {

        var i = 0;
        for(value in events){
            hasClickedAudio = false;
            _gameState.value = _gameState.value.copy(eventValue = value)
            textToSpeech?.speak(alphabet[value-1], TextToSpeech.QUEUE_FLUSH, null, null);

            if(i >= nBack){
                correctAudioAnswer = events[i-nBack];
            }
            i++;
            delay(eventInterval)
        }


    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun changeColor() = GlobalScope.async{
        delay(round(eventInterval/1.1f).toLong())
        squares[_gameState.value.eventValue-1].setColor(cloudBlue);
    }
    private suspend fun runVisualGame(events: Array<Int>){
        var i = 0;
        for (value in events) {
0
            hasClickedPosition = false;

            _gameState.value = _gameState.value.copy(eventValue = value)
            squares[value-1].setColor(violate);

            changeColor();

            if(i >= nBack){
                correctPositionAnswer = events[i-nBack];
            }
            i++;

            println("Current: ${_gameState.value.eventValue}, Correct: $correctPositionAnswer")
            delay(eventInterval)
        }



    }

    private suspend fun runAudioVisualGame(events: Array<Int>){

        var i = 0;
        for(value in events){
            hasClickedAudio = false;
            hasClickedPosition = false;

            _gameState.value = _gameState.value.copy(eventValue=value)
            squares[value-1].setColor(violate)
            textToSpeech?.speak(alphabet[value-1], TextToSpeech.QUEUE_FLUSH, null, null);
            changeColor()

            if(i >= nBack){
                correctPositionAnswer = events[i-nBack]
                correctAudioAnswer = events[i-nBack]
            }
            i++;
            delay(eventInterval)
        }
    }

    override fun endGame(){
        job?.cancel();

        if(textToSpeech?.isSpeaking == true){
            textToSpeech?.stop()
        }
        squares.clear()

        _score.value = 0;

        setGameState(State.HOME)
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

enum class State{
    HOME,
    RUNNING
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val eventValue: Int = -1,  // The value of the array string
    val state: State = State.HOME
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

    override fun startGame(context: Context, interval:Int, nback:Int, rounds:Int, dim:Int) {
    }

    override fun checkMatch(buttonType:String):Boolean {
        return true;
    }

    override fun getSquare(index:Int):SquareCard{
        return SquareCard(oceanBlue);
    }
    override fun endGame(){
    }

    override fun getTime():Long{
        return 1000L;
    }

    override fun setGameState(gameState: State){

    }

    override fun getDim():Int{
        return 3;
    }
}