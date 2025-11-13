package mobappdev.example.nback_cimpl.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import mobappdev.example.nback_cimpl.ui.viewmodels.GameVM
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.toPath
import mobappdev.example.nback_cimpl.R
import mobappdev.example.nback_cimpl.ui.viewmodels.SquareCard
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import mobappdev.example.nback_cimpl.ui.viewmodels.FakeVM
import mobappdev.example.nback_cimpl.ui.viewmodels.GameViewModel
import androidx.compose.runtime.setValue
import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import mobappdev.example.nback_cimpl.ui.viewmodels.State
import java.util.Locale
import kotlin.concurrent.timer

@Composable
fun MyCard(it: Int, vm: GameViewModel){
    val squareAnimation = rememberInfiniteTransition(label="infinite animation");
    val morphProgress = squareAnimation.animateFloat(
        initialValue = 0f,
        targetValue=1f,
        animationSpec = infiniteRepeatable(
            tween(3000, easing=LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label="morph"
    );

    val rotationAnimation = rememberInfiniteTransition(label="rotation animation");
    val rotationProgress = rotationAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(10000, easing= LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label="rotate"
    );


    Card(
        modifier = Modifier.size(80.dp).padding(6.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors= CardDefaults.cardColors(containerColor = vm.cloudBlue)
    ) {
        Box(
            modifier=Modifier.fillMaxSize().drawWithCache{
                val square = vm.getSquare(it).createSquare(size.minDimension, size.width, size.height);
                val roundedSquare = vm.getSquare(it).createRoundedSquare(size.minDimension, size.width, size.height, 50f, 10f);

                val squareMorph = Morph(start = roundedSquare, end = square);
                val squarePath = squareMorph.toPath(progress = morphProgress.value).asComposePath();


                onDrawBehind {
                    rotate(rotationProgress.value){
                        drawPath(squarePath, color = vm.getSquare(it).secondColor.value)
                    }
                }
            },
            contentAlignment = Alignment.Center,
        ){
        }
    }
}


@Composable
fun GameScreen(
    vm: GameViewModel
){
    val gameState by vm.gameState.collectAsState()
    val timerAnim = rememberInfiniteTransition(label="timer");
    val timerProgress = timerAnim.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween((vm.getTime().toInt()), easing=LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(){
        Box(
            modifier=Modifier
                .background(vm.violate, RoundedCornerShape(4.dp))
                .fillMaxWidth(fraction=timerProgress.value)
                .height(20.dp)
                .padding(8.dp)
        ){
        }
        Row(
            horizontalArrangement = Arrangement.End
        ){
            Button(
                onClick={
                    vm.endGame()
                }
            ){
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Note",
                    modifier = Modifier
                        .height(48.dp)
                        .aspectRatio(3f / 2f)
                )
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top=20.dp)
    ){

        Text(text="Score: ${vm.score.collectAsState().value}", style=MaterialTheme.typography.headlineLarge)
        Text(text="N = ${vm.nBack.collectAsState().value}", style=MaterialTheme.typography.headlineLarge)
        LazyVerticalGrid(
            columns= GridCells.Fixed(vm.getDim()),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center
        ) {
            items(vm.getDim()*vm.getDim()){
                MyCard(it, vm);
            }
        }

    }

    Row(
        verticalAlignment = Alignment.Bottom
    ){

        var audioCorrect by remember {mutableStateOf<Boolean>(false)}
        var baseFormAudio by remember {mutableStateOf(true)}
        val audioColor = animateColorAsState(
            targetValue = if(baseFormAudio) vm.darkBlue else if(audioCorrect) vm.lightBlue else vm.red,
            animationSpec = tween(500, 0, LinearEasing),
            finishedListener = {baseFormAudio = true}
        )
        Button(
            onClick={
                baseFormAudio = false;
                audioCorrect = vm.checkMatch("Audio")
            },
            shape = RectangleShape,
            modifier=Modifier.fillMaxWidth(0.5f).height(175.dp),
            colors= ButtonDefaults.buttonColors(containerColor=audioColor.value),

            ){
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "SOUND", style = MaterialTheme.typography.titleLarge);
                Icon(
                    painter = painterResource(id = R.drawable.note),
                    contentDescription = "Note",
                    modifier = Modifier
                        .height(48.dp)
                        .aspectRatio(3f / 2f)
                )
            }
        }

        var positionCorrect by remember {mutableStateOf<Boolean>(false)}
        var baseFormPosition by remember {mutableStateOf(true)}
        val positionColor = animateColorAsState(
            targetValue = if(baseFormPosition) vm.darkBlue else if(positionCorrect) vm.lightBlue else vm.red,
            animationSpec = tween(500, 0, LinearEasing),
            finishedListener = {baseFormPosition = true}
        )
        Button(
            onClick={
                baseFormPosition=false;
                positionCorrect = vm.checkMatch("Visual")
            },
            shape=RectangleShape,
            modifier=Modifier.fillMaxWidth().height(175.dp),
            colors= ButtonDefaults.buttonColors(containerColor=positionColor.value),
        ){
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "POSITION", style = MaterialTheme.typography.titleLarge);
                Icon(
                    painter = painterResource(id = R.drawable.position),
                    contentDescription = "Note",
                    modifier = Modifier
                        .height(48.dp)
                        .aspectRatio(3f / 2f)
                )
            }
        }
    }





    /*if (gameState.eventValue != -1) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Current eventValue is: ${gameState.eventValue}",
                textAlign = TextAlign.Center
            )
        }*/
}

@Preview
@Composable
fun GameScreenPreview(){
    Surface(){
        GameScreen(FakeVM())
    }
}