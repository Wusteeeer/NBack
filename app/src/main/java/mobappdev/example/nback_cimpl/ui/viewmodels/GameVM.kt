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
import kotlin.math.min


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
    val nBack:StateFlow<Int>
    val interval:StateFlow<Int>
    val rounds:StateFlow<Int>
    val dimensions:StateFlow<Int>
    val soundBites:StateFlow<Int>
    val language: StateFlow<Locale>

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

    fun startGame(context: Context)

    fun checkMatch(buttonType:String):Boolean;

    fun updateNBack(nback:Int);
    fun updateInterval(interval:Int);
    fun updateRounds(round:Int)
    fun updateDim(dim:Int)
    fun updateSound(bite:Int)
    fun updateLanguage(language:String)

    fun endGame();
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
): GameViewModel, ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _nBack = MutableStateFlow(2)
    override val nBack: StateFlow<Int>
        get() = _nBack

    private val _interval = MutableStateFlow(1)
    override val interval:StateFlow<Int>
        get() = _interval

    private val _rounds = MutableStateFlow(10)
    override val rounds:StateFlow<Int>
        get() = _rounds

    private val _dimensions = MutableStateFlow(3)
    override val dimensions:StateFlow<Int>
        get() = _dimensions

    private val _soundBites = MutableStateFlow(5)
    override val soundBites:StateFlow<Int>
        get() = _soundBites

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _language = MutableStateFlow(Locale.ENGLISH)
    override val language: StateFlow<Locale>
        get() = _language

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    private val squares: MutableList<SquareCard> = arrayListOf();
    private val alphabet = arrayListOf<String>("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
    private var textToSpeech: TextToSpeech?=null

    private var correctPositionAnswer: Int = -1;
    private var correctAudioAnswer:Int = -1;
    private var hasClickedPosition: Boolean = false;
    private var hasClickedAudio: Boolean = false;

    private var job: Job? = null  // coroutine job for the game event
    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var events = emptyArray<Int>()  // Array with all events
    private var audioEvents = emptyArray<Int>()

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
        return dimensions.value;
    }

    override fun getTime():Long{
        return _interval.value.toLong()*1000L;
    }

    override fun updateNBack(nback:Int){
        _nBack.value = nback;
    }

    override fun updateInterval(interval:Int){
        _interval.value = interval
    }

    override fun updateRounds(round:Int){
        _rounds.value = round;
    }
    override fun updateDim(dim:Int){
        _dimensions.value = dim;
    }
    override fun updateSound(bite:Int){
        _soundBites.value = bite;
    }
    override fun updateLanguage(language:String){
        when(language){
            "ENGLISH" -> _language.value = Locale.ENGLISH
            "FRENCH" -> _language.value = Locale.FRENCH
            "GERMAN" -> _language.value = Locale.GERMAN
            "ITALIAN" -> _language.value = Locale.ITALIAN
        }
    }

    fun languageToValue():Int{
        when(_language.value){
            Locale.ENGLISH -> return 1
            Locale.FRENCH -> return 2
            Locale.GERMAN -> return 3
            Locale.ITALIAN -> return 4
        }
        return 1;
    }

    fun valueToLanguage(value:Int){
        _language.value = when(value){
            1 -> Locale.ENGLISH
            2 -> Locale.FRENCH
            3 -> Locale.GERMAN
            4 -> Locale.ITALIAN
            else -> Locale.ENGLISH
        }
    }

    override fun startGame(context: Context) {
        job?.cancel()  // Cancel any existing game loop

        setGameState(State.RUNNING)

        //nBack = _nbackTest.value;
        println(_language.value)
        textToSpeech = TextToSpeech(context){
            if(it == TextToSpeech.SUCCESS){
                textToSpeech?.let{txtToSpeech ->
                    txtToSpeech.language = _language.value
                    txtToSpeech.setSpeechRate(1.0f)
                }

            }
        }

        squares.clear()
        for(i in 0..<(dimensions.value*dimensions.value)){
            squares.add(SquareCard(cloudBlue));
        }

        // Get the events from our C-model (returns IntArray, so we need to convert to Array<Int>)
        events = nBackHelper.generateNBackString(rounds.value, dimensions.value*dimensions.value, 30, _nBack.value).toList().toTypedArray()

        audioEvents = nBackHelper.generateNBackString(rounds.value, soundBites.value, 30, _nBack.value).toList().toTypedArray();
        if(_gameState.value.gameType == GameType.AudioVisual) audioEvents.shuffle()

        Log.d("GameVM", "The following sequence was generated: ${events.contentToString()}")
        Log.d("Audio", "The follow audio sequence was generated: ${audioEvents.contentToString()}")
        job = viewModelScope.launch {

            userPreferencesRepository.saveSettings(_nBack.value, _interval.value, _rounds.value, _dimensions.value, _soundBites.value, languageToValue())

            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame(audioEvents)
                GameType.AudioVisual -> runAudioVisualGame(events, audioEvents)
                GameType.Visual -> runVisualGame(events)
            }

            if(score.value > highscore.value)
                userPreferencesRepository.saveHighScore(score.value)


            endGame()
        }
    }

    override fun checkMatch(buttonType:String):Boolean {
        println()
        when(buttonType){
            "Audio" -> {
                if(hasClickedAudio || gameState.value.gameType == GameType.Visual) return false;
                hasClickedAudio = true;

                if(_gameState.value.audioValue == correctAudioAnswer){
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
            _gameState.value = _gameState.value.copy(audioValue=value)
            textToSpeech?.speak(alphabet[value-1], TextToSpeech.QUEUE_FLUSH, null, null);

            if(i >= nBack.value){
                correctAudioAnswer = events[i-nBack.value];
            }
            i++;
            delay(interval.value.toLong()*1000L)
        }


    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun changeColor() = GlobalScope.async{
        delay(round(interval.value.toLong()*1000L/1.1f).toLong())
        squares[_gameState.value.eventValue-1].setColor(cloudBlue);
    }
    private suspend fun runVisualGame(events: Array<Int>){
        var i = 0;
        println("Hello world! Running visual game!")
        for (value in events) {
0
            hasClickedPosition = false;

            _gameState.value = _gameState.value.copy(eventValue = value)
            squares[value-1].setColor(violate);

            changeColor();

            if(i >= nBack.value){
                correctPositionAnswer = events[i-nBack.value];
            }
            i++;

            println("Current: ${_gameState.value.eventValue}, Correct: $correctPositionAnswer")
            delay(interval.value.toLong()*1000L)
        }



    }

    private suspend fun runAudioVisualGame(events: Array<Int>, audioEvents: Array<Int>){

        val length = min(events.size, audioEvents.size)
        for(i in 0..<length){

            val value = events[i]
            val audioValue = audioEvents[i]

            hasClickedAudio = false;
            hasClickedPosition = false;

            _gameState.value = _gameState.value.copy(eventValue=value)
            _gameState.value = _gameState.value.copy(audioValue=audioValue)
            squares[value-1].setColor(violate)
            textToSpeech?.speak(alphabet[audioValue-1], TextToSpeech.QUEUE_FLUSH, null, null);
            changeColor()

            if(i >= nBack.value){
                correctPositionAnswer = events[i-nBack.value]
                correctAudioAnswer = audioEvents[i-nBack.value]
            }

            delay(interval.value.toLong()*1000L)
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
        viewModelScope.launch{
            userPreferencesRepository.nback.collect{
                _nBack.value = it;

            }
        }
        viewModelScope.launch{
            userPreferencesRepository.interval.collect{
                _interval.value = it;

            }
        }
        viewModelScope.launch{
            userPreferencesRepository.rounds.collect{
                _rounds.value = it;

            }
        }
        viewModelScope.launch{
            userPreferencesRepository.dims.collect{
                _dimensions.value = it;
            }
        }
        viewModelScope.launch{
            userPreferencesRepository.sounds.collect{
                _soundBites.value = it;
            }
        }
        viewModelScope.launch{
            userPreferencesRepository.language.collect{
                _language.value = when(it){
                    1 -> Locale.ENGLISH
                    2 -> Locale.FRENCH
                    3 -> Locale.GERMAN
                    4 -> Locale.ITALIAN
                    else -> Locale.ENGLISH
                }
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
    val audioValue: Int = -1,
    val state: State = State.HOME
)

class FakeVM: GameViewModel{
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val nBack: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val interval:StateFlow<Int>
        get() = MutableStateFlow(1).asStateFlow()
    override val rounds:StateFlow<Int>
        get() = MutableStateFlow(1).asStateFlow()
    override val dimensions:StateFlow<Int>
        get() = MutableStateFlow(1).asStateFlow()
    override val soundBites:StateFlow<Int>
        get() = MutableStateFlow(1).asStateFlow()
    override val language:StateFlow<Locale>
        get() = MutableStateFlow(Locale.ENGLISH).asStateFlow()
    override fun setGameType(gameType: GameType) {
    }

    override fun startGame(context: Context) {
    }

    override fun checkMatch(buttonType:String):Boolean {
        return true;
    }

    override fun updateNBack(nback:Int){
    }

    override fun updateInterval(interval: Int) {
    }

    override fun updateRounds(round:Int) {
    }

    override fun updateDim(dim: Int) {

    }

    override fun updateLanguage(language:String){

    }

    override fun updateSound(bite: Int) {
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