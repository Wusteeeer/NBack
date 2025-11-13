package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameType
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import kotlin.math.absoluteValue
import kotlin.math.round

/**
 * This is the Home screen composable
 *
 * Currently this screen shows the saved highscore
 * It also contains a button which can be used to show that the C-integration works
 * Furthermore it contains two buttons that you can use to start a game
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */

@Composable
fun HomeScreen(
    vm: GameViewModel
) {
    val highscore by vm.highscore.collectAsState()  // Highscore is its own StateFlow
    val nback by vm.nBack.collectAsState()
    val interval by vm.interval.collectAsState();
    val rounds by vm.rounds.collectAsState();
    val dims by vm.dimensions.collectAsState();
    val soundBites by vm.soundBites.collectAsState();

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current;


    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState) },
        containerColor = vm.oceanBlue,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                modifier = Modifier.padding(32.dp),
                text = "High-Score = $highscore",
                style = MaterialTheme.typography.headlineLarge
            )

            var nSliderValue by remember { mutableFloatStateOf(nback.toFloat()) }
            var intervalSliderValue by remember { mutableFloatStateOf(interval.toFloat())}
            var roundSliderValue by remember {mutableFloatStateOf(rounds.toFloat())}
            var dimSliderValue by remember {mutableFloatStateOf(dims.toFloat())}
            var audioSliderValue by remember {mutableFloatStateOf(soundBites.toFloat())}

            var nValue by remember { mutableIntStateOf(nback) };
            var intervalValue by remember {mutableIntStateOf(interval)};
            var roundValue by remember {mutableIntStateOf(rounds)};
            var dimValue by remember {mutableIntStateOf(dims)}
            var audioValue by remember {mutableIntStateOf(soundBites)}


            Text("N = $nback", style=MaterialTheme.typography.headlineMedium)
            Slider(
                value=nback.toFloat(),
                onValueChange = {

                    nSliderValue=it
                    nValue = round(nSliderValue.absoluteValue).toInt();
                    vm.updateNBack(nValue)


                },
                steps=5,
                valueRange = 1f..5f,
                modifier=Modifier.width(300.dp)
            )

            Text("Interval = $interval", style=MaterialTheme.typography.headlineMedium)
            Slider(
                value=interval.toFloat(),
                onValueChange = {
                    intervalSliderValue=it;
                    intervalValue = round(intervalSliderValue.absoluteValue).toInt();
                    vm.updateInterval(intervalValue);
                },
                steps=9,
                valueRange = 1f..10f,
                modifier=Modifier.width(300.dp)
            )

            Text("Rounds = $rounds", style=MaterialTheme.typography.headlineMedium)
            Slider(
                value=rounds.toFloat(),
                onValueChange = {
                    roundSliderValue=it
                    roundValue = round(roundSliderValue.absoluteValue).toInt();
                    vm.updateRounds(roundValue);
                },
                steps=15,
                valueRange = 5f..20f,
                modifier=Modifier.width(300.dp)
            )

            Text("Dimension = $dims", style=MaterialTheme.typography.headlineMedium)
            Slider(
                value=dims.toFloat(),
                onValueChange = {
                    dimSliderValue=it
                    dimValue = round(dimSliderValue.absoluteValue).toInt();
                    vm.updateDim(dimValue);
                },
                steps=3,
                valueRange = 2f..5f,
                modifier=Modifier.width(300.dp)
            )

            Text("Sound bites = $soundBites", style=MaterialTheme.typography.headlineMedium)
            Slider(
                value=soundBites.toFloat(),
                onValueChange = {
                    audioSliderValue=it
                    audioValue = round(audioSliderValue.absoluteValue).toInt();
                    vm.updateSound(audioValue)
                },
                steps=24,
                valueRange = 2f..25f,
                modifier=Modifier.width(300.dp)
            )

            Button(onClick = {
                vm.startGame(context)
            }){
                Icon(
                    painter = painterResource(id = R.drawable.play),
                    contentDescription = "Play",
                    modifier = Modifier
                        .height(48.dp)
                        .aspectRatio(3f / 2f)
                )
            }
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Start Game".uppercase(),
                style = MaterialTheme.typography.displaySmall
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {

                var audioSelected by remember { mutableStateOf(false) }
                val audioBgColor = if (audioSelected) vm.darkBlue else vm.skyBlue;

                var visualSelected by remember {mutableStateOf(true)}
                val visualBgColor = if (visualSelected) vm.darkBlue else vm.skyBlue;

                fun setGameType(){
                    if(audioSelected && visualSelected){
                        vm.setGameType(GameType.AudioVisual)
                    }else if(audioSelected){
                        vm.setGameType(GameType.Audio)
                    }else{
                        vm.setGameType(GameType.Visual)
                    }
                }

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor=audioBgColor),
                    onClick = {
                        audioSelected = !audioSelected;

                        if(!audioSelected && !visualSelected){
                            visualSelected = true;
                        }

                        setGameType();
                    }
                )
                {
                    Icon(
                        painter = painterResource(id = R.drawable.sound_on),
                        contentDescription = "Sound",
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = visualBgColor),
                    onClick = {

                        visualSelected = !visualSelected;

                        if(!audioSelected && !visualSelected){
                            audioSelected = true;
                        }

                        setGameType();

                    }) {
                    Icon(
                        painter = painterResource(id = R.drawable.visual),
                        contentDescription = "Visual",
                        modifier = Modifier
                            .height(48.dp)
                            .aspectRatio(3f / 2f)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    // Since I am injecting a VM into my homescreen that depends on Application context, the preview doesn't work.
    Surface(){
        HomeScreen(FakeVM())
    }
}