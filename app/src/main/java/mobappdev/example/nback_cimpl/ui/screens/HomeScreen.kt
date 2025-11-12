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
    val gameState by vm.gameState.collectAsState()
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

            var nSliderValue by remember { mutableFloatStateOf(2f) }
            var intervalSliderValue by remember { mutableFloatStateOf(1f)}
            var roundSliderValue by remember {mutableFloatStateOf(10f)}
            var dimSliderValue by remember {mutableFloatStateOf(3f)}

            var nValue by remember { mutableIntStateOf(2) };
            var intervalValue by remember {mutableIntStateOf(1)};
            var roundValue by remember {mutableIntStateOf(10)};
            var dimValue by remember {mutableIntStateOf(3)}

            Text("N = $nValue", style=MaterialTheme.typography.headlineMedium)
            Slider(
                value=nSliderValue,
                onValueChange = {
                    nSliderValue=it
                    nValue = round(nSliderValue.absoluteValue).toInt();
                },
                steps=9,
                valueRange = 1f..10f,
                modifier=Modifier.width(300.dp)
            )

            Text("Interval = $intervalValue", style=MaterialTheme.typography.headlineMedium)
            Slider(
                value=intervalSliderValue,
                onValueChange = {
                    intervalSliderValue=it;
                    intervalValue = round(intervalSliderValue.absoluteValue).toInt();
                },
                steps=9,
                valueRange = 1f..10f,
                modifier=Modifier.width(300.dp)
            )

            Text("Rounds = $roundValue", style=MaterialTheme.typography.headlineMedium)
            Slider(
                value=roundSliderValue,
                onValueChange = {
                    roundSliderValue=it
                    roundValue = round(roundSliderValue.absoluteValue).toInt();
                },
                steps=15,
                valueRange = 5f..20f,
                modifier=Modifier.width(300.dp)
            )

            Text("Dimension = $dimValue", style=MaterialTheme.typography.headlineMedium)
            Slider(
                value=dimSliderValue,
                onValueChange = {
                    dimSliderValue=it
                    dimValue = round(dimSliderValue.absoluteValue).toInt();
                },
                steps=3,
                valueRange = 2f..5f,
                modifier=Modifier.width(300.dp)
            )

            Button(onClick = {
                vm.startGame(context, intervalValue, nValue, roundValue, dimValue)
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